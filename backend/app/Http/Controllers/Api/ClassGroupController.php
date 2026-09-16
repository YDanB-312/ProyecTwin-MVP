<?php

namespace App\Http\Controllers\Api;

use App\Models\ClassGroup;
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
            'id_instructor' => 'nullable|exists:instructors,id',
            'training_center_id' => 'nullable|exists:training_centers,id',
        ]);

        $item = ClassGroup::create($request->all());
        return response()->json($item, 201);
    }

    public function show($id)
    {
        return ClassGroup::included()->findOrFail($id);
    }

    public function update(Request $request, ClassGroup $class_group)
    {
        $request->validate([
            'codigo' => 'required|max:255|unique:class_groups,codigo,' . $class_group->id,
            'numero' => 'nullable|max:255',
            'nombre' => 'required|max:255',
            'estado' => 'required|in:activo,inactivo,finalizado,archivado',
            'id_programa' => 'required|exists:training_programs,id',
            'id_instructor' => 'nullable|exists:instructors,id',
            'training_center_id' => 'nullable|exists:training_centers,id',
        ]);

        $class_group->update($request->all());
        return $class_group;
    }

    public function destroy(ClassGroup $class_group)
    {
        $class_group->delete();
        return $class_group;
    }
}
