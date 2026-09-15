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
            ->byCentro($request->query('centro_id'))
            ->byFicha($request->query('ficha_id'))
            ->byPrograma($request->query('programa'))
            ->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'titulo' => 'required|max:255',
            'tipo_proyecto' => 'nullable|max:255',
            'resumen' => 'required',
            'palabras_clave' => 'required',
            'area_aplicacion' => 'required|max:255',
            'tecnologias' => 'required',
            'objetivos' => 'required|array',
            'entregables' => 'required|array',
            'url_logo' => 'nullable|max:255',
            'estado' => 'nullable|in:borrador,pendiente,en_revision,aprobado,rechazado,requiere_ajustes,en_progreso,completado,cancelado',
            'observaciones' => 'nullable',
            'id_creador' => 'required|exists:general_users,id',
            'id_instructor_asignado' => 'nullable|exists:instructors,id',
            'id_class_group' => 'nullable|exists:class_groups,id',
        ]);

        $item = Project::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = Project::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, Project $project)
    {
        $request->validate([
            'titulo' => 'required|max:255',
            'tipo_proyecto' => 'nullable|max:255',
            'resumen' => 'required',
            'palabras_clave' => 'required',
            'area_aplicacion' => 'required|max:255',
            'tecnologias' => 'required',
            'objetivos' => 'required|array',
            'entregables' => 'required|array',
            'url_logo' => 'nullable|max:255',
            'estado' => 'nullable|in:borrador,pendiente,en_revision,aprobado,rechazado,requiere_ajustes,en_progreso,completado,cancelado',
            'observaciones' => 'nullable',
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
