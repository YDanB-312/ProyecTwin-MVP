<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ApprenticeProject;
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
            ->search($request->query('search'))
            ->byEstado($request->query('estado'))
            ->byTrainingCenter($request->query('training_center_id'))
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
        $datos = $request->all();
        // El creador es siempre quien envía la propuesta (un admin sí puede
        // registrarla a nombre de otro).
        if (optional($usuario)->rol !== 'admin' || empty($datos['id_creador'])) {
            $datos['id_creador'] = $usuario->id;
        }

        $item = Project::create($datos);

        // El creador forma parte del equipo: se registra en el pivote para que
        // los conteos y el listado de integrantes sean consistentes.
        $this->asegurarCreadorEnEquipo($item);

        // Avisa al instructor a cargo que hay una propuesta pendiente de revisión.
        $this->notificarInstructor($item);

        return response()->json($item, 201);
    }

    public function show($id)
    {
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
        } elseif ($rol === 'instructor') {
            if (!$this->esInstructorDeLaPropuesta($project, $usuario)) {
                return response()->json(['message' => 'No puedes editar una propuesta fuera de tus fichas.'], 403);
            }
        } elseif ($rol !== 'admin') {
            return response()->json(['message' => 'Sin permiso para editar propuestas.'], 403);
        }

        $datos = $request->all();

        // El aprendiz no aprueba/rechaza: al reenviar una rechazada vuelve a
        // pendiente para una nueva revisión del instructor.
        if ($rol === 'aprendiz') {
            $datos['estado'] = 'pendiente';
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

        // Solo el dueño aprendiz o un admin (el instructor no borra propuestas).
        $permitido = $rol === 'admin'
            || ($rol === 'aprendiz' && $this->esPropiaDelAprendiz($project, $usuario));
        if (!$permitido) {
            return response()->json(['message' => 'No puedes eliminar esta propuesta.'], 403);
        }

        $project->delete();
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
