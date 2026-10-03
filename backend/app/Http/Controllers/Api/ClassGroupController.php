<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\Instructor;
use App\Models\Project;
use App\Support\Pagina;
use App\Http\Controllers\Controller;
use App\Http\Requests\ClassGroupRequest;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Gate;

class ClassGroupController extends Controller
{
    // Alcance por rol: admin todas; instructor SOLO las que creó/gestiona;
    // aprendiz SOLO su ficha actual.
    public function index(Request $request)
    {
        $user = $request->user();
        $query = ClassGroup::included();

        if (optional($user)->rol === 'admin') {
            return Pagina::aplicar($query, $request, 15);
        }

        if ($user && $user->rol === 'instructor') {
            $instructorId = Instructor::where('id_usuario', $user->id)->value('id');
            if (!$instructorId) return collect();
            return Pagina::aplicar($query->where('id_instructor', $instructorId), $request, 15);
        }

        $fichaId = Apprentice::where('id_usuario', optional($user)->id)->value('id_class_group');
        if (!$fichaId) return collect();
        return Pagina::aplicar($query->where('id', $fichaId), $request, 15);
    }

    public function store(ClassGroupRequest $request)
    {
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

    public function show(Request $request, ClassGroup $class_group)
    {
        $user = $request->user();

        // Mismo alcance que index(): admin todas; instructor las suyas; aprendiz
        // solo su ficha actual (el código es credencial y no debe filtrarse).
        if (optional($user)->rol === 'instructor') {
            $instructor = $this->miFilaInstructor($request);
            if (!$instructor || (int) $class_group->id_instructor !== (int) $instructor->id) {
                return response()->json(['message' => 'No puedes ver una ficha que no está a tu cargo.'], 403);
            }
        } elseif ($user && $user->rol === 'aprendiz') {
            $fichaId = Apprentice::where('id_usuario', $user->id)->value('id_class_group');
            if ((int) $fichaId !== (int) $class_group->id) {
                return response()->json(['message' => 'No puedes ver una ficha que no es la tuya.'], 403);
            }
        } elseif (optional($user)->rol !== 'admin') {
            return response()->json(['message' => 'Sin permiso para esta acción.'], 403);
        }

        return ClassGroup::included()->findOrFail($class_group->id);
    }

    public function update(ClassGroupRequest $request, ClassGroup $class_group)
    {
        if (!Gate::allows('manage', $class_group)) {
            return response()->json(['message' => 'No puedes editar una ficha que no está a tu cargo.'], 403);
        }

        $datos = $request->all();
        // Un instructor no puede reasignar su propia ficha a otro (eso es del admin).
        if ($request->user()->rol === 'instructor') {
            $datos['id_instructor'] = $class_group->id_instructor;
        }

        $programaAnterior = $class_group->id_programa;
        $estadoAnterior = $class_group->estado;

        // Cambio de ficha atómico: ficha + aprendices + recálculo (todo o nada).
        DB::transaction(function () use ($class_group, $datos, $programaAnterior, $estadoAnterior) {
            $class_group->update($datos);

            // Si cambió el programa de la ficha, se sincroniza el programa de sus
            // aprendices y se recalcula el corpus (los pares exigen mismo programa).
            if ((int) $programaAnterior !== (int) $class_group->id_programa) {
                \App\Models\Apprentice::where('id_class_group', $class_group->id)
                    ->update(['id_programa' => $class_group->id_programa]);
                // Mismo servicio que el comando y el endpoint de recálculo.
                app(\App\Similarity\Recomputador::class)->recalcular();
            }

            if ($estadoAnterior !== 'finalizado' && $class_group->estado === 'finalizado') {
                \App\Support\Auditoria::registrar('finalizar_ficha', 'class_groups', $class_group->id, [
                    'codigo' => $class_group->codigo,
                ]);
            }
        });

        return $class_group;
    }

    public function destroy(Request $request, ClassGroup $class_group)
    {
        if (!Gate::allows('manage', $class_group)) {
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

    private function miFilaInstructor(Request $request): ?Instructor
    {
        // Auto-sanado: si el instructor no tiene perfil, se crea al primer uso.
        return Instructor::firstOrCreate(
            ['id_usuario' => $request->user()->id],
            ['fecha_ingreso' => now()->toDateString()]
        );
    }
}
