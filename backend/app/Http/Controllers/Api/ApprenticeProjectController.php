<?php

namespace App\Http\Controllers\Api;

use App\Models\ApprenticeProject;
use App\Models\Instructor;
use App\Models\Project;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class ApprenticeProjectController extends Controller
{
    public function index()
    {
        $items = ApprenticeProject::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'id_aprendiz' => 'required|exists:apprentices,id',
            'id_proyecto' => 'required|exists:projects,id',
        ]);

        if (!$this->puedeGestionarEquipo($request, (int) $request->id_proyecto)) {
            return response()->json(['message' => 'No puedes gestionar el equipo de esta propuesta.'], 403);
        }

        $item = ApprenticeProject::firstOrCreate([
            'id_aprendiz' => $request->id_aprendiz,
            'id_proyecto' => $request->id_proyecto,
        ]);
        return $item;
    }

    public function show($id)
    {
        $item = ApprenticeProject::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, ApprenticeProject $apprentice_project)
    {
        $request->validate([
            'id_aprendiz' => 'required|exists:apprentices,id',
            'id_proyecto' => 'required|exists:projects,id',
        ]);

        if (!$this->puedeGestionarEquipo($request, (int) $apprentice_project->id_proyecto)
            || !$this->puedeGestionarEquipo($request, (int) $request->id_proyecto)) {
            return response()->json(['message' => 'No puedes gestionar el equipo de esta propuesta.'], 403);
        }

        $apprentice_project->update($request->all());
        return $apprentice_project;
    }

    public function destroy(Request $request, ApprenticeProject $apprentice_project)
    {
        if (!$this->puedeGestionarEquipo($request, (int) $apprentice_project->id_proyecto)) {
            return response()->json(['message' => 'No puedes gestionar el equipo de esta propuesta.'], 403);
        }

        $apprentice_project->delete();
        return $apprentice_project;
    }

    // Admin cualquiera; el creador aprendiz el suyo; el instructor su ficha.
    private function puedeGestionarEquipo(Request $request, int $idProyecto): bool
    {
        $user = $request->user();
        if (!$user) return false;
        if ($user->rol === 'admin') return true;

        $project = Project::find($idProyecto);
        if (!$project) return false;

        if ($user->rol === 'aprendiz') {
            return (int) $project->id_creador === (int) $user->id;
        }

        if ($user->rol === 'instructor') {
            $instructor = Instructor::where('id_usuario', $user->id)->first();
            if (!$instructor) return false;
            return (int) $project->id_instructor_asignado === (int) $instructor->id
                || (int) optional($project->classGroup)->id_instructor === (int) $instructor->id;
        }

        return false;
    }
}
