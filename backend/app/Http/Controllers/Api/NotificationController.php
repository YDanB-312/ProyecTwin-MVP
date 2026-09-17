<?php

namespace App\Http\Controllers\Api;

use App\Models\Notification;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class NotificationController extends Controller
{
    public function index(Request $request)
    {
        $user = $request->user();

        // Cada usuario solo ve sus propias notificaciones (antes se devolvían
        // las de todos y el cliente filtraba, lo que exponía datos ajenos).
        $query = Notification::included()->orderByDesc('id');

        // Un admin puede consultar la bandeja de otro usuario con ?id_usuario=.
        $idUsuario = $request->query('id_usuario');
        if ($idUsuario && $user->rol === 'admin') {
            $query->where('id_usuario', $idUsuario);
        } else {
            $query->where('id_usuario', $user->id);
        }

        return $query->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'titulo' => 'required|max:255',
            'descripcion' => 'nullable',
            'tipo' => 'required|in:similitud,observacion,revision,mensaje,sistema',
            'enlace' => 'nullable|max:255',
            'leida' => 'nullable|boolean',
            'fecha' => 'required|date',
            'id_usuario' => 'required|exists:general_users,id',
        ]);

        $item = Notification::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = Notification::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, Notification $notification)
    {
        $request->validate([
            'titulo' => 'required|max:255',
            'descripcion' => 'nullable',
            'tipo' => 'required|in:similitud,observacion,revision,mensaje,sistema',
            'enlace' => 'nullable|max:255',
            'leida' => 'nullable|boolean',
            'fecha' => 'required|date',
            'id_usuario' => 'required|exists:general_users,id',
        ]);

        $notification->update($request->all());
        return $notification;
    }

    public function destroy(Notification $notification)
    {
        $notification->delete();
        return $notification;
    }
}
