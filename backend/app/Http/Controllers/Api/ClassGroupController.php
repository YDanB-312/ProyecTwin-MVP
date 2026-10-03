<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Project;
use App\Support\CredencialesPdf;
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

        // Solo un instructor con perfil crea fichas (el admin puede cualquiera).
        $mio = null;
        if (optional($user)->rol === 'instructor') {
            $mio = $this->miFilaInstructor($request);
            if (!$mio) {
                return response()->json(['message' => 'Tu cuenta no tiene perfil de instructor.'], 422);
            }
        }

        $request->validate([
            'numero' => 'required|max:255|unique:class_groups,numero',
            'nombre' => 'required|max:255',
            'estado' => 'required|in:activo,finalizado',
            'id_programa' => 'required|exists:training_programs,id',
            'id_instructor' => 'required|exists:instructors,id',
            'aprendices' => 'nullable|array',
            'aprendices.*' => 'integer|exists:general_users,id',
        ]);

        $datos = $request->all();
        unset($datos['aprendices']);
        // El código de unión lo genera el servidor: el cliente no lo elige.
        $datos['codigo'] = $this->codigoDisponible();
        // Un instructor solo crea fichas a su nombre (el admin asigna a quien sea).
        if ($mio) {
            $datos['id_instructor'] = $mio->id;
        }

        // Alta atómica: ficha + aprendices seleccionados.
        $item = DB::transaction(function () use ($datos, $request) {
            $item = ClassGroup::create($datos);
            $this->sincronizarAprendices($item, $request->input('aprendices', []));
            return $item;
        });

        return response()->json($item, 201);
    }

    public function show($id)
    {
        return ClassGroup::included()->findOrFail($id);
    }

    // Exporta en PDF las credenciales iniciales de la ficha (aprendices +
    // instructor responsable). Solo admin; queda en la bitácora.
    public function credenciales(ClassGroup $class_group)
    {
        $ids = Apprentice::where('id_class_group', $class_group->id)->pluck('id_usuario')->all();
        if ($class_group->instructor) {
            $ids[] = $class_group->instructor->id_usuario;
        }

        $usuarios = GeneralUser::with('apprentice.classGroup.program', 'instructor.classGroups.program')
            ->whereIn('id', $ids)
            ->orderBy('nombre')
            ->get();

        \App\Support\Auditoria::registrar('exportar_credenciales', 'class_groups', $class_group->id, [
            'usuarios' => $usuarios->count(),
            'temporales' => $usuarios->where('must_change_password', true)->count(),
        ]);

        return CredencialesPdf::generar($usuarios, [
            'ficha' => $class_group->numero,
            'programa' => optional($class_group->program)->nombre,
        ]);
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
            'aprendices' => 'nullable|array',
            'aprendices.*' => 'integer|exists:general_users,id',
        ]);

        $datos = $request->all();
        unset($datos['codigo']); // El código de unión es inmutable.
        unset($datos['aprendices']); // Se sincronizan aparte (relación aprendiz-ficha).
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
        DB::transaction(function () use ($class_group, $datos, $request, $programaAnterior, $estadoAnterior, $numeroAnterior, $nombreAnterior, $instructorAnterior, $cambiaInstructor, $seAnula) {
            $class_group->update($datos);

            // Aprendices seleccionados en el formulario (si vino el campo).
            if ($request->has('aprendices')) {
                $this->sincronizarAprendices($class_group, $request->input('aprendices', []));
            }

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

    // Admin cualquiera; instructor solo sus fichas.
    private function puedeGestionar(Request $request, ClassGroup $ficha): bool
    {
        $user = $request->user();
        if (!$user) return false;
        if ($user->rol === 'admin') return true;
        if ($user->rol !== 'instructor') return false;

        $instructor = $this->miFilaInstructor($request);
        return $instructor && (int) $ficha->id_instructor === (int) $instructor->id;
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

    // Sincroniza la pertenencia de los aprendices a la ficha: los seleccionados
    // quedan en la ficha (se crea su fila si no existía) y los no seleccionados
    // salen conservando su historial (fila con ficha/programa en null).
    private function sincronizarAprendices(ClassGroup $ficha, array $ids): void
    {
        $seleccionados = collect($ids)->map(fn ($id) => (int) $id)->filter()->unique()->values();

        // Retira a quienes ya no están en la selección.
        Apprentice::where('id_class_group', $ficha->id)
            ->when($seleccionados->isNotEmpty(), fn ($q) => $q->whereNotIn('id_usuario', $seleccionados))
            ->update(['id_class_group' => null, 'id_programa' => null]);

        // Agrega/actualiza a los seleccionados (solo usuarios con rol aprendiz).
        foreach ($seleccionados as $idUsuario) {
            $usuario = \App\Models\GeneralUser::find($idUsuario);
            if (!$usuario || $usuario->rol !== 'aprendiz') continue;

            $aprendiz = Apprentice::firstOrCreate(
                ['id_usuario' => $usuario->id],
                [
                    'codigo' => $this->codigoAprendizDisponible(),
                    'id_class_group' => $ficha->id,
                    'id_programa' => $ficha->id_programa,
                ]
            );
            $aprendiz->update([
                'id_class_group' => $ficha->id,
                'id_programa' => $ficha->id_programa,
            ]);
        }

        if ($seleccionados->isNotEmpty()) {
            \App\Support\Auditoria::registrar('asignar_aprendices', 'class_groups', $ficha->id, [
                'aprendices' => $seleccionados->count(),
            ]);
        }
    }

    private function codigoAprendizDisponible(): string
    {
        $n = (int) Apprentice::max('id') + 1;
        do {
            $codigo = 'AP-' . str_pad((string) $n, 3, '0', STR_PAD_LEFT);
            $n++;
        } while (Apprentice::where('codigo', $codigo)->exists());

        return $codigo;
    }
}
