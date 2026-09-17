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
            'id_creador' => 'required|exists:general_users,id',
            'id_instructor_asignado' => 'nullable|exists:instructors,id',
            'id_class_group' => 'nullable|exists:class_groups,id',
        ]);

        $item = Project::create($request->all());

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
        $esAprendiz = optional($usuario)->rol === 'aprendiz';

        // Un aprendiz solo puede editar sus propias propuestas (creador o equipo).
        if ($esAprendiz && !$this->esPropiaDelAprendiz($project, $usuario)) {
            return response()->json(['message' => 'No puedes editar una propuesta que no es tuya.'], 403);
        }

        $datos = $request->all();

        // El aprendiz no aprueba/rechaza: al reenviar una rechazada vuelve a
        // pendiente para una nueva revisión del instructor.
        if ($esAprendiz) {
            $datos['estado'] = 'pendiente';
        }

        $project->update($datos);

        return $project;
    }

    public function destroy(Project $project)
    {
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
