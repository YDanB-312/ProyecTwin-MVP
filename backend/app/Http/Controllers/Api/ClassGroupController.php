<?php

namespace App\Http\Controllers\Api;

use App\Models\ClassGroup;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class ClassGroupController extends Controller
{
    public function index()
    {
        $items = ClassGroup::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'codigo' => 'required|max:255',
            'nombre' => 'required|max:255',
            'estado' => 'required|in:activo,inactivo',
            'id_programa' => 'required|exists:training_programs,id',
            'id_instructor' => 'required|exists:instructors,id',
        ]);

        $item = ClassGroup::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = ClassGroup::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, ClassGroup $class_group)
    {
        $request->validate([
            'codigo' => 'required|max:255',
            'nombre' => 'required|max:255',
            'estado' => 'required|in:activo,inactivo',
            'id_programa' => 'required|exists:training_programs,id',
            'id_instructor' => 'required|exists:instructors,id',
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
