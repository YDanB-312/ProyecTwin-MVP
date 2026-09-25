<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\Instructor;
use App\Models\Project;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

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
        $request->validate([
            'codigo' => 'required|max:255|unique:class_groups,codigo',
            'numero' => 'nullable|max:255',
            'nombre' => 'required|max:255',
            'estado' => 'required|in:activo,finalizado',
            'id_programa' => 'required|exists:training_programs,id',
            'id_instructor' => 'required|exists:instructors,id',
        ]);

        $datos = $request->all();
        // Un instructor solo crea fichas a su nombre (el admin asigna a quien sea).
        if ($request->user()->rol === 'instructor') {
            $mio = $this->miFilaInstructor($request);
            if (!$mio) {
                return response()->json(['message' => 'Tu cuenta no tiene perfil de instructor.'], 422);
            }
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

        $request->validate([
            'codigo' => 'required|max:255|unique:class_groups,codigo,' . $class_group->id,
            'numero' => 'nullable|max:255',
            'nombre' => 'required|max:255',
            'estado' => 'required|in:activo,finalizado',
            'id_programa' => 'required|exists:training_programs,id',
            'id_instructor' => 'required|exists:instructors,id',
        ]);

        $datos = $request->all();
        // Un instructor no puede reasignar su propia ficha a otro (eso es del admin).
        if ($request->user()->rol === 'instructor') {
            $datos['id_instructor'] = $class_group->id_instructor;
        }

        $programaAnterior = $class_group->id_programa;
        $estadoAnterior = $class_group->estado;
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

        return $class_group;
    }

    public function destroy(Request $request, ClassGroup $class_group)
    {
        if (!$this->puedeGestionar($request, $class_group)) {
            return response()->json(['message' => 'No puedes eliminar una ficha que no está a tu cargo.'], 403);
        }

        // Admin sin restricciones: borra la ficha y sus dependientes en cascada
        // (propuestas con similitudes/observaciones/equipo y aprendices).
        $impacto = [
            'aprendices' => $class_group->apprentices()->count(),
            'propuestas' => Project::where('id_class_group', $class_group->id)->count(),
        ];

        \App\Support\BorradoCascada::ficha($class_group);

        \App\Support\Auditoria::registrar('eliminar_ficha', 'class_groups', $class_group->id, [
            'codigo' => $class_group->codigo,
            'nombre' => $class_group->nombre,
            'impacto' => $impacto,
        ]);

        return $class_group;
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

    private function miFilaInstructor(Request $request): ?Instructor
    {
        // Auto-sanado: si el instructor no tiene perfil, se crea al primer uso.
        return Instructor::firstOrCreate(
            ['id_usuario' => $request->user()->id],
            ['fecha_ingreso' => now()->toDateString()]
        );
    }
}
