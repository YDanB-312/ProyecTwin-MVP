<?php

namespace App\Http\Controllers\Api;

use App\Models\Admin;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class AdminController extends Controller
{
    public function index()
    {
        $items = Admin::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'area_encargada' => 'required|max:255',
            'notif_correo' => 'nullable|boolean',
            'alertas_usuarios' => 'nullable|boolean',
            'id_usuario' => 'required|exists:general_users,id',
        ]);

        $item = Admin::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = Admin::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, Admin $admin)
    {
        $request->validate([
            'area_encargada' => 'required|max:255',
            'notif_correo' => 'nullable|boolean',
            'alertas_usuarios' => 'nullable|boolean',
            'id_usuario' => 'required|exists:general_users,id',
        ]);

        $admin->update($request->all());
        return $admin;
    }

    public function destroy(Admin $admin)
    {
        $admin->delete();
        return $admin;
    }
}
