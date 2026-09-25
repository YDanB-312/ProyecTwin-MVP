<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Notification;
use App\Models\Project;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class ProjectController extends Controller
{
    public function index(Request $request)
    {
        return Project::included()
            ->paraUsuario($request->user())
            ->search($request->query('search'))
            ->byEstado($request->query('estado'))
            ->byFicha($request->query('ficha_id'))
            ->byPrograma($request->query('programa'))
            ->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'titulo' => 'required|max:255',
            'resumen' => 'required',
            'palabras_clave' => 'nullable|max:255',
            'area_aplicacion' => 'required|max:255',
            'objetivo_general' => 'nullable',
            'objetivos_especificos' => 'nullable|array',
            'estado' => 'nullable|in:pendiente,aprobado,rechazado',
            'id_creador' => 'nullable|exists:general_users,id',
            'id_instructor_asignado' => 'nullable|exists:instructors,id',
            'id_class_group' => 'nullable|exists:class_groups,id',
        ]);

        $usuario = $request->user();
        $rol = optional($usuario)->rol;
        $datos = $request->all();

        if ($rol === 'aprendiz') {
            // La propuesta vive en la ficha ACTUAL del aprendiz (activa) y con
            // el instructor de esa ficha: no se confía en el request.
            $aprendiz = Apprentice::where('id_usuario', $usuario->id)->first();
            if (!$aprendiz || !$aprendiz->id_class_group) {
                return response()->json(['message' => 'Únete a una ficha para registrar propuestas.'], 422);
            }
            $ficha = ClassGroup::find($aprendiz->id_class_group);
            if (!$ficha || $ficha->estado !== 'activo') {
                return response()->json(['message' => 'Tu ficha no está activa para recibir propuestas.'], 422);
            }
            $datos['id_class_group'] = $ficha->id;
            $datos['id_instructor_asignado'] = $ficha->id_instructor;
            $datos['id_creador'] = $usuario->id;
        } elseif ($rol === 'admin') {
            // Un admin puede registrarla a nombre de otro, pero el autor debe
            // ser un aprendiz (integridad de dominio).
            if (!empty($datos['id_creador'])) {
                $creador = GeneralUser::find($datos['id_creador']);
                if (!$creador || $creador->rol !== 'aprendiz') {
                    return response()->json(['message' => 'La propuesta debe pertenecer a un aprendiz.'], 422);
                }
            } else {
                $datos['id_creador'] = $usuario->id;
            }
        } else {
            // Solo aprendices (o un admin a nombre de uno) pueden ser autores:
            // una propuesta sin autor aprendiz rompe el dominio del motor.
            return response()->json(['message' => 'Solo un aprendiz puede registrar propuestas.'], 403);
        }

        $item = Project::create($datos);

        // El creador forma parte del equipo: se registra en el pivote para que
        // los conteos y el listado de integrantes sean consistentes.
        $this->asegurarCreadorEnEquipo($item);

        // Avisa al instructor a cargo que hay una propuesta pendiente de revisión.
        $this->notificarInstructor($item);

        return response()->json($item, 201);
    }

    public function show(Request $request, $id)
    {
        // El detalle completo solo si la propuesta está dentro de su alcance.
        $visible = Project::where('id', $id)->paraDetalle($request->user())->exists();
        if (!$visible) {
            return response()->json(['message' => 'No tienes acceso a esta propuesta.'], 403);
        }

        return Project::included()->findOrFail($id);
    }

    public function update(Request $request, Project $project)
    {
        $request->validate([
            'titulo' => 'required|max:255',
            'resumen' => 'required',
            'palabras_clave' => 'nullable|max:255',
            'area_aplicacion' => 'required|max:255',
            'objetivo_general' => 'nullable',
            'objetivos_especificos' => 'nullable|array',
            'estado' => 'nullable|in:pendiente,aprobado,rechazado',
            'id_creador' => 'required|exists:general_users,id',
            'id_instructor_asignado' => 'nullable|exists:instructors,id',
            'id_class_group' => 'nullable|exists:class_groups,id',
        ]);

        $usuario = $request->user();
        $rol = optional($usuario)->rol;

        // Autorización por rol: aprendiz solo las suyas; instructor solo las de
        // su ficha o asignadas; admin cualquiera.
        if ($rol === 'aprendiz') {
            if (!$this->esPropiaDelAprendiz($project, $usuario)) {
                return response()->json(['message' => 'No puedes editar una propuesta que no es tuya.'], 403);
            }
            // Solo lectura si ya no perteneces a la ficha (o está finalizada).
            if (!$project->puedeEscribir($usuario)) {
                return response()->json([
                    'message' => 'Solo lectura: ya no perteneces a la ficha de esta propuesta.',
                ], 403);
            }
        } elseif ($rol === 'instructor') {
            if (!$this->esInstructorDeLaPropuesta($project, $usuario)) {
                return response()->json(['message' => 'No puedes editar una propuesta fuera de tus fichas.'], 403);
            }
        } elseif ($rol !== 'admin') {
            return response()->json(['message' => 'Sin permiso para editar propuestas.'], 403);
        }

        $datos = $request->all();

        // La propuesta no cambia de ficha ni de creador por esta vía
        // (trazabilidad histórica); se conservan los valores actuales.
        unset($datos['id_class_group'], $datos['id_creador']);
        $datos['id_creador'] = $project->id_creador;
        // El instructor asignado solo lo reasigna un admin (evita que el aprendiz
        // o el equipo "secuestren" la asignación).
        if ($rol !== 'admin') {
            unset($datos['id_instructor_asignado']);
        }

        // El aprendiz no aprueba/rechaza: al reenviar una rechazada vuelve a
        // pendiente para una nueva revisión del instructor.
        if ($rol === 'aprendiz') {
            $datos['estado'] = 'pendiente';
        }

        // Contenido de una propuesta APROBADA editado por staff → nueva revisión.
        if ($rol !== 'aprendiz' && $project->estado === 'aprobado') {
            $campos = ['titulo', 'resumen', 'palabras_clave', 'area_aplicacion', 'objetivo_general', 'objetivos_especificos'];
            foreach ($campos as $c) {
                if (!$request->has($c)) continue;
                if (json_encode($request->input($c)) !== json_encode($project->{$c})) {
                    $datos['estado'] = 'pendiente';
                    break;
                }
            }
        }

        $estadoAnterior = $project->estado;
        $project->update($datos);

        // Aprobar/rechazar queda en la bitácora (decisión con efecto académico).
        if ($rol !== 'aprendiz' && $estadoAnterior !== $project->estado) {
            \App\Support\Auditoria::registrar('revisar_propuesta', 'projects', $project->id, [
                'de' => $estadoAnterior,
                'a' => $project->estado,
                'titulo' => $project->titulo,
            ]);
        }

        return $project;
    }

    public function destroy(Request $request, Project $project)
    {
        $usuario = $request->user();
        $rol = optional($usuario)->rol;

        // Solo el CREADOR aprendiz (dentro de su ficha activa) o un admin.
        $permitido = $rol === 'admin'
            || ($rol === 'aprendiz'
                && (int) $project->id_creador === (int) $usuario->id
                && $project->puedeEscribir($usuario));
        if (!$permitido) {
            return response()->json(['message' => 'No puedes eliminar esta propuesta.'], 403);
        }

        $project->delete();
        // Evita notificaciones con enlace a una propuesta ya inexistente.
        Notification::where('enlace', 'proyecto:' . $project->id)->delete();
        return $project;
    }

    // ---------------------------------------------------------------- Internos

    // ¿La propuesta es del aprendiz (creador o integrante del equipo)?
    private function esPropiaDelAprendiz(Project $project, $usuario): bool
    {
        if ((int) $project->id_creador === (int) $usuario->id) return true;

        $filaAprendiz = Apprentice::where('id_usuario', $usuario->id)->first();
        if (!$filaAprendiz) return false;

        return ApprenticeProject::where('id_proyecto', $project->id)
            ->where('id_aprendiz', $filaAprendiz->id)
            ->exists();
    }

    // ¿El instructor está asignado a la propuesta o es el de su ficha?
    private function esInstructorDeLaPropuesta(Project $project, $usuario): bool
    {
        $instructor = Instructor::where('id_usuario', $usuario->id)->first();
        if (!$instructor) return false;

        if ((int) $project->id_instructor_asignado === (int) $instructor->id) return true;

        return (int) optional($project->classGroup)->id_instructor === (int) $instructor->id;
    }

    // Inserta la fila del creador en el pivote si aún no está.
    private function asegurarCreadorEnEquipo(Project $project): void
    {
        $filaAprendiz = Apprentice::where('id_usuario', $project->id_creador)->first();
        if (!$filaAprendiz) return;

        ApprenticeProject::firstOrCreate([
            'id_proyecto' => $project->id,
            'id_aprendiz' => $filaAprendiz->id,
        ]);
    }

    // Notifica al instructor asignado (o al de la ficha) sobre la propuesta nueva.
    private function notificarInstructor(Project $project): void
    {
        $idUsuario = null;
        if ($project->id_instructor_asignado) {
            $idUsuario = optional(Instructor::find($project->id_instructor_asignado))->id_usuario;
        }
        if (!$idUsuario && $project->id_class_group) {
            $idUsuario = optional(optional(\App\Models\ClassGroup::find($project->id_class_group))->instructor)->id_usuario;
        }
        if (!$idUsuario) return;

        Notification::create([
            'titulo' => 'Nueva propuesta pendiente de revisión: "' . $project->titulo . '"',
            'tipo' => 'revision',
            'enlace' => 'proyecto:' . $project->id,
            'leida' => false,
            'fecha' => now()->toDateString(),
            'id_usuario' => $idUsuario,
        ]);
    }
}
