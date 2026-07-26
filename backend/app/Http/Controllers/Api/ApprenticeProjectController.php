<?php

namespace App\Http\Controllers\Api;

use App\Models\ApprenticeProject;
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

        $item = ApprenticeProject::create($request->all());
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

        $apprentice_project->update($request->all());
        return $apprentice_project;
    }

    public function destroy(ApprenticeProject $apprentice_project)
    {
        $apprentice_project->delete();
        return $apprentice_project;
    }
}
