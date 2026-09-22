<?php

namespace App\Http\Controllers\Api;

use App\Models\Admin;
use App\Models\GeneralUser;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class AdminController extends Controller
{
    public function index()
    {
        return Admin::included()->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'id_usuario' => 'required|exists:general_users,id',
            'training_center_id' => 'nullable|exists:training_centers,id',
        ]);

        $usuario = GeneralUser::findOrFail($request->id_usuario);
        if ($usuario->rol !== 'admin') {
            return response()->json(['message' => 'El usuario no tiene rol de administrador.'], 422);
        }

        $centroId = $request->input('training_center_id');
        if ($centroId && Admin::where('training_center_id', $centroId)
            ->where('id_usuario', '!=', $usuario->id)->exists()) {
            return response()->json(['message' => 'Ese centro ya tiene un administrador asignado.'], 422);
        }

        $item = Admin::updateOrCreate(
            ['id_usuario' => $usuario->id],
            ['training_center_id' => $centroId]
        );
        return response()->json($item, 201);
    }

    public function show($id)
    {
        return Admin::included()->findOrFail($id);
    }

    public function update(Request $request, Admin $admin)
    {
        // El dueño del perfil no cambia: solo se (re)asigna su centro.
        $request->validate([
            'training_center_id' => 'nullable|exists:training_centers,id',
        ]);

        $centroId = $request->input('training_center_id');
        if ($centroId && Admin::where('training_center_id', $centroId)
            ->where('id', '!=', $admin->id)->exists()) {
            return response()->json(['message' => 'Ese centro ya tiene un administrador asignado.'], 422);
        }

        $admin->update(['training_center_id' => $centroId]);
        return $admin;
    }

    public function destroy(Request $request, Admin $admin)
    {
        // Autoprotección: no se borra el propio perfil de admin.
        if ((int) $admin->id_usuario === (int) $request->user()->id) {
            return response()->json(['message' => 'No puedes eliminar tu propio perfil de administrador.'], 422);
        }

        $admin->delete();
        return $admin;
    }
}
