<?php

namespace App\Http\Controllers\Api;

use App\Models\Project;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class ProjectController extends Controller
{
    public function index(Request $request)
    {
        $items = Project::included()
            ->search($request->query('search'))
            ->byEstado($request->query('estado'))
            ->byTrainingCenter($request->query('training_center_id'))
            ->byFicha($request->query('ficha_id'))
            ->byPrograma($request->query('programa'))
            ->get();
        return $items;
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

        $project->update($request->all());
        return $project;
    }

    public function destroy(Project $project)
    {
        $project->delete();
        return $project;
    }
}
