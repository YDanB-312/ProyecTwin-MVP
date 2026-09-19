<?php

namespace App\Http\Controllers\Api;

use App\Models\Admin;
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
        ]);

        $item = Admin::create($request->only(['id_usuario']));
        return response()->json($item, 201);
    }

    public function show($id)
    {
        return Admin::included()->findOrFail($id);
    }

    public function update(Request $request, Admin $admin)
    {
        $request->validate([
            'id_usuario' => 'required|exists:general_users,id',
        ]);

        $admin->update($request->only(['id_usuario']));
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
