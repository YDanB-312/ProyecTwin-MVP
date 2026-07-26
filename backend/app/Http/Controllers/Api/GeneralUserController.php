<?php

namespace App\Http\Controllers\Api;

use App\Models\GeneralUser;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class GeneralUserController extends Controller
{
    public function index()
    {
        $items = GeneralUser::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'apellido' => 'required|max:255',
            'correo' => 'required|email',
            'password' => 'required|max:255',
            'foto_url' => 'nullable|max:255',
            'rol' => 'required|in:aprendiz,instructor,admin',
            'estado' => 'nullable|boolean',
            'notif_similitud' => 'nullable|boolean',
            'notif_comentarios_instructor' => 'nullable|boolean',
            'notif_nuevos_proyectos' => 'nullable|boolean',
            'notif_revisiones_pendientes' => 'nullable|boolean',
            'notif_noticias_sistema' => 'nullable|boolean',
        ]);

        $item = GeneralUser::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = GeneralUser::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, GeneralUser $general_user)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'apellido' => 'required|max:255',
            'correo' => 'required|email',
            'password' => 'nullable|max:255',
            'foto_url' => 'nullable|max:255',
            'rol' => 'required|in:aprendiz,instructor,admin',
            'estado' => 'nullable|boolean',
            'notif_similitud' => 'nullable|boolean',
            'notif_comentarios_instructor' => 'nullable|boolean',
            'notif_nuevos_proyectos' => 'nullable|boolean',
            'notif_revisiones_pendientes' => 'nullable|boolean',
            'notif_noticias_sistema' => 'nullable|boolean',
        ]);

        $general_user->update($request->all());
        return $general_user;
    }

    public function destroy(GeneralUser $general_user)
    {
        $general_user->delete();
        return $general_user;
    }
}
