<?php

namespace App\Http\Controllers\Api;

use App\Models\Instructor;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class InstructorController extends Controller
{
    public function index()
    {
        $items = Instructor::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'fecha_ingreso' => 'required|date',
            'plantilla_comentarios' => 'nullable|max:255',
            'id_usuario' => 'required|exists:general_users,id',
        ]);

        $item = Instructor::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = Instructor::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, Instructor $instructor)
    {
        $request->validate([
            'fecha_ingreso' => 'required|date',
            'plantilla_comentarios' => 'nullable|max:255',
            'id_usuario' => 'required|exists:general_users,id',
        ]);

        $instructor->update($request->all());
        return $instructor;
    }

    public function destroy(Instructor $instructor)
    {
        $instructor->delete();
        return $instructor;
    }
}
