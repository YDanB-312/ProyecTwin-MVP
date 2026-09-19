<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\Instructor;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class InstructorController extends Controller
{
    public function index(Request $request)
    {
        $user = $request->user();

        // Admin ve todos; instructor su propia fila; aprendiz los de su ficha.
        if (optional($user)->rol === 'admin') {
            return Instructor::included()->get();
        }
        if ($user && $user->rol === 'instructor') {
            return Instructor::included()->where('id_usuario', $user->id)->get();
        }

        $fichaIds = Apprentice::where('id_usuario', optional($user)->id)->pluck('id_class_group')->filter();
        $instructorIds = ClassGroup::whereIn('id', $fichaIds)->pluck('id_instructor')->filter();

        return Instructor::included()->whereIn('id', $instructorIds)->get();
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
