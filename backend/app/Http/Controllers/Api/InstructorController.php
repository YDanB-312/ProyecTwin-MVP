<?php

namespace App\Http\Controllers\Api;

use App\Models\Instructor;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class InstructorController extends Controller
{
    public function index()
    {
        return Instructor::included()->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'fecha_ingreso' => 'required|date',
            'id_usuario' => 'required|exists:general_users,id',
        ]);

        $item = Instructor::create($request->all());
        return response()->json($item, 201);
    }

    public function show($id)
    {
        return Instructor::included()->findOrFail($id);
    }

    public function update(Request $request, Instructor $instructor)
    {
        $request->validate([
            'fecha_ingreso' => 'required|date',
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
