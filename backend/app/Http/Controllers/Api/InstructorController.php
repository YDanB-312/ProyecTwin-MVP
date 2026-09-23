<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
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
            // Auto-sanado: garantiza que el instructor tenga perfil propio.
            Instructor::firstOrCreate(
                ['id_usuario' => $user->id],
                ['fecha_ingreso' => now()->toDateString()]
            );
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

        $usuario = GeneralUser::findOrFail($request->id_usuario);
        if ($usuario->rol !== 'instructor') {
            return response()->json(['message' => 'El usuario no tiene rol de instructor.'], 422);
        }

        $item = Instructor::firstOrCreate(
            ['id_usuario' => $request->id_usuario],
            ['fecha_ingreso' => $request->fecha_ingreso]
        );
        return response()->json($item, 201);
    }

    public function show($id)
    {
        return Instructor::included()->findOrFail($id);
    }

    public function update(Request $request, Instructor $instructor)
    {
        // El dueño del perfil no cambia: solo la fecha de ingreso.
        $request->validate(['fecha_ingreso' => 'required|date']);

        $instructor->update(['fecha_ingreso' => $request->fecha_ingreso]);
        return $instructor;
    }

    public function destroy(Instructor $instructor)
    {
        // La FK de class_groups impide borrar un instructor con fichas a cargo:
        // se responde 409 con motivo en vez de un 500.
        try {
            $instructor->delete();
        } catch (\Illuminate\Database\QueryException $e) {
            return response()->json([
                'message' => 'No se puede eliminar: el instructor tiene fichas a cargo. Reasígnalas primero.',
            ], 409);
        }
        return $instructor;
    }
}
