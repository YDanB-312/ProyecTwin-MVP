<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\Instructor;
use App\Models\Project;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class ClassGroupController extends Controller
{
    // Alcance por rol: admin todas; instructor SOLO las que creó/gestiona;
    // aprendiz SOLO su ficha actual. Evita exponer los códigos de todas las
    // fichas (el código es la credencial para unirse).
    public function index(Request $request)
    {
        $user = $request->user();
        $query = ClassGroup::included();

        if (optional($user)->rol === 'admin') {
            return $query->get();
        }

        if ($user && $user->rol === 'instructor') {
            $instructorId = Instructor::where('id_usuario', $user->id)->value('id');
            return $instructorId ? $query->where('id_instructor', $instructorId)->get() : collect();
        }

        $fichaId = Apprentice::where('id_usuario', optional($user)->id)->value('id_class_group');
        return $fichaId ? $query->where('id', $fichaId)->get() : collect();
    }

    public function store(Request $request)
    {
        $user = $request->user();

        // Solo un instructor verificado crea fichas (el admin puede cualquiera).
        $mio = null;
        if (optional($user)->rol === 'instructor') {
            $mio = $this->miFilaInstructor($request);
            if (!$mio) {
                return response()->json(['message' => 'Tu cuenta no tiene perfil de instructor.'], 422);
            }
            if (!$user->estaVerificado()) {
                return response()->json(['message' => 'Tu cuenta de instructor está pendiente de verificación.'], 403);
            }
        }

        $request->validate([
            'numero' => 'required|max:255|unique:class_groups,numero',
            'nombre' => 'required|max:255',
            'estado' => 'required|in:activo,finalizado',
            'id_programa' => 'required|exists:training_programs,id',
            'id_instructor' => 'required|exists:instructors,id',
        ]);

        $datos = $request->all();
        // El código de unión lo genera el servidor: el cliente no lo elige.
        $datos['codigo'] = $this->codigoDisponible();
        // Un instructor solo crea fichas a su nombre (el admin asigna a quien sea).
        if ($mio) {
            $datos['id_instructor'] = $mio->id;
        }

        $item = ClassGroup::create($datos);
        return response()->json($item, 201);
    }

    public function show($id)
    {
        return ClassGroup::included()->findOrFail($id);
    }

    public function update(Request $request, ClassGroup $class_group)
    {
        if (!$this->puedeGestionar($request, $class_group)) {
            return response()->json(['message' => 'No puedes editar una ficha que no está a tu cargo.'], 403);
        }

        // Una ficha anulada solo la interviene un administrador.
        if ($class_group->estado === 'anulada' && optional($request->user())->rol !== 'admin') {
            return response()->json(['message' => 'Una ficha anulada solo la puede modificar un administrador.'], 403);
        }

        $request->validate([
            'numero' => 'required|max:255|unique:class_groups,numero,' . $class_group->id,
            'nombre' => 'required|max:255',
            'estado' => 'required|in:activo,finalizado,anulada',
            'id_programa' => 'required|exists:training_programs,id',
            'id_instructor' => 'required|exists:instructors,id',
        ]);

        $datos = $request->all();
        unset($datos['codigo']); // El código de unión es inmutable.
        // Un instructor no puede reasignar su propia ficha a otro (eso es del admin).
        if ($request->user()->rol === 'instructor') {
            $datos['id_instructor'] = $class_group->id_instructor;
        }

        $programaAnterior = $class_group->id_programa;
        $estadoAnterior = $class_group->estado;
        $numeroAnterior = $class_group->numero;
        $nombreAnterior = $class_group->nombre;
        $instructorAnterior = $class_group->id_instructor;

        // Reasignación de responsable: acción administrativa que debe quedar
        // trazada con instructor anterior y nuevo.
        $cambiaInstructor = array_key_exists('id_instructor', $datos)
            && (int) $datos['id_instructor'] !== (int) $instructorAnterior;

        // Anular libera el número: la ficha conserva su historial, el número no.
        $seAnula = $datos['estado'] === 'anulada' && $estadoAnterior !== 'anulada';
        if ($seAnula) {
            $datos['numero'] = null;
        }

        // Cambio de ficha atómico: ficha + aprendices + recálculo (todo o nada).
        DB::transaction(function () use ($class_group, $datos, $programaAnterior, $estadoAnterior, $numeroAnterior, $nombreAnterior, $instructorAnterior, $cambiaInstructor, $seAnula) {
            $class_group->update($datos);

            // Si cambió el programa de la ficha, se sincroniza el programa de sus
            // aprendices y se recalcula el corpus (los pares exigen mismo programa).
            if ((int) $programaAnterior !== (int) $class_group->id_programa) {
                \App\Models\Apprentice::where('id_class_group', $class_group->id)
                    ->update(['id_programa' => $class_group->id_programa]);
                app(\App\Http\Controllers\Api\SimilarityController::class)->recalculate();
            }

            if ($estadoAnterior !== 'finalizado' && $class_group->estado === 'finalizado') {
                \App\Support\Auditoria::registrar('finalizar_ficha', 'class_groups', $class_group->id, [
                    'codigo' => $class_group->codigo,
                ]);
            }

            if ($seAnula) {
                \App\Support\Auditoria::registrar('anular_ficha', 'class_groups', $class_group->id, [
                    'codigo' => $class_group->codigo,
                    'numero_anterior' => $numeroAnterior,
                ]);
            }

            if ($cambiaInstructor) {
                \App\Support\Auditoria::registrar('reasignar_ficha', 'class_groups', $class_group->id, [
                    'codigo' => $class_group->codigo,
                    'de' => $instructorAnterior,
                    'a' => $class_group->id_instructor,
                ]);
            }

            // Modificaciones administrativas de la ficha (antes → después).
            $cambios = array_filter([
                'nombre' => $class_group->nombre !== $nombreAnterior
                    ? ['de' => $nombreAnterior, 'a' => $class_group->nombre] : null,
                'numero' => !$seAnula && $numeroAnterior !== $class_group->numero
                    ? ['de' => $numeroAnterior, 'a' => $class_group->numero] : null,
                'programa' => (int) $programaAnterior !== (int) $class_group->id_programa
                    ? ['de' => $programaAnterior, 'a' => $class_group->id_programa] : null,
            ]);
            if ($cambios) {
                \App\Support\Auditoria::registrar('actualizar_ficha', 'class_groups', $class_group->id, [
                    'codigo' => $class_group->codigo,
                    'cambios' => $cambios,
                ]);
            }
        });

        return $class_group;
    }

    public function destroy(Request $request, ClassGroup $class_group)
    {
        if (!$this->puedeGestionar($request, $class_group)) {
            return response()->json(['message' => 'No puedes eliminar una ficha que no está a tu cargo.'], 403);
        }

        $impacto = [
            'aprendices' => $class_group->apprentices()->count(),
            'propuestas' => Project::where('id_class_group', $class_group->id)->count(),
        ];

        // Con información asociada no se borra físicamente: se anula y se libera
        // el número para que otra ficha pueda usarlo (el historial se conserva).
        if ($impacto['aprendices'] > 0 || $impacto['propuestas'] > 0) {
            $numeroAnterior = $class_group->numero;
            $class_group->update(['estado' => 'anulada', 'numero' => null]);

            \App\Support\Auditoria::registrar('anular_ficha', 'class_groups', $class_group->id, [
                'codigo' => $class_group->codigo,
                'nombre' => $class_group->nombre,
                'numero_anterior' => $numeroAnterior,
                'impacto' => $impacto,
            ]);

            return response()->json(['accion' => 'anulada', 'ficha' => $class_group->fresh()]);
        }

        // Sin información: se borra de verdad y el número queda disponible.
        $numeroAnterior = $class_group->numero;
        \App\Support\BorradoCascada::ficha($class_group);

        \App\Support\Auditoria::registrar('eliminar_ficha', 'class_groups', $class_group->id, [
            'codigo' => $class_group->codigo,
            'nombre' => $class_group->nombre,
            'numero' => $numeroAnterior,
        ]);

        return response()->json(['accion' => 'eliminada', 'ficha' => $class_group]);
    }

    // Admin cualquiera; instructor verificado solo sus fichas.
    private function puedeGestionar(Request $request, ClassGroup $ficha): bool
    {
        $user = $request->user();
        if (!$user) return false;
        if ($user->rol === 'admin') return true;
        if ($user->rol !== 'instructor') return false;

        $instructor = $this->miFilaInstructor($request);
        return $instructor
            && $user->estaVerificado()
            && (int) $ficha->id_instructor === (int) $instructor->id;
    }

    // Perfil de instructor del usuario autenticado (null si no existe). No se
    // auto-crea: la verificación se resuelve en el registro, no al usar la API.
    private function miFilaInstructor(Request $request): ?Instructor
    {
        return Instructor::where('id_usuario', $request->user()->id)->first();
    }

    // Código de unión generado por el servidor (formato xxx-xxxx en minúsculas).
    private function codigoDisponible(): string
    {
        $letras = 'abcdefghijklmnopqrstuvwxyz';
        do {
            $codigo = '';
            for ($i = 0; $i < 7; $i++) {
                if ($i === 3) $codigo .= '-';
                $codigo .= $letras[random_int(0, 25)];
            }
        } while (ClassGroup::where('codigo', $codigo)->exists());

        return $codigo;
    }
}
