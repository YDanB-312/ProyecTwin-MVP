<?php

namespace App\Http\Controllers\Api;

use App\Models\ClassGroup;
use App\Models\Instructor;
use App\Models\Project;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class ClassGroupController extends Controller
{
    public function index()
    {
        return ClassGroup::included()->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'codigo' => 'required|max:255|unique:class_groups,codigo',
            'numero' => 'nullable|max:255',
            'nombre' => 'required|max:255',
            'estado' => 'required|in:activo,inactivo,finalizado,archivado',
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
            'estado' => 'required|in:activo,inactivo,finalizado,archivado',
            'id_programa' => 'required|exists:training_programs,id',
            'id_instructor' => 'required|exists:instructors,id',
        ]);

        $datos = $request->all();
        // Un instructor no puede reasignar su propia ficha a otro (eso es del admin).
        if ($request->user()->rol === 'instructor') {
            $datos['id_instructor'] = $class_group->id_instructor;
        }

        $estadoAnterior = $class_group->estado;
        $class_group->update($datos);

        if ($estadoAnterior !== 'archivado' && $class_group->estado === 'archivado') {
            \App\Support\Auditoria::registrar('archivar_ficha', 'class_groups', $class_group->id, [
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

        // Borrar una ficha elimina sus aprendices (cascade) y deja las propuestas
        // sin ficha. Si tiene historial, se archiva en vez de borrarla.
        $aprendices = $class_group->apprentices()->count();
        $propuestas = Project::where('id_class_group', $class_group->id)->count();

        if ($aprendices > 0 || $propuestas > 0) {
            return response()->json([
                'message' => "No se puede eliminar: tiene {$aprendices} aprendiz(ces) y {$propuestas} propuesta(s). Archívala para conservar el historial.",
            ], 409);
        }

        $class_group->delete();

        \App\Support\Auditoria::registrar('eliminar_ficha', 'class_groups', $class_group->id, [
            'codigo' => $class_group->codigo,
            'nombre' => $class_group->nombre,
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
