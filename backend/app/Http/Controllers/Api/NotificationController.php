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

        // Solo instructor/admin avisan a otros; nadie crea avisos para terceros.
        $user = $request->user();
        $destino = (int) $request->id_usuario;
        $permitido = in_array($user->rol, ['admin', 'instructor'], true) || $destino === (int) $user->id;
        if (!$permitido) {
            return response()->json(['message' => 'No puedes crear notificaciones para otro usuario.'], 403);
        }

        $item = Notification::create($request->all());
        return $item;
    }

    public function show(Request $request, $id)
    {
        $item = Notification::included()->findOrFail($id);
        $user = $request->user();
        if ((int) $item->id_usuario !== (int) $user->id && $user->rol !== 'admin') {
            return response()->json(['message' => 'No tienes acceso a esta notificación.'], 403);
        }
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

        // Marcar como leída es del dueño de la bandeja (o de un admin).
        $user = $request->user();
        if ((int) $notification->id_usuario !== (int) $user->id && $user->rol !== 'admin') {
            return response()->json(['message' => 'No puedes modificar notificaciones de otro usuario.'], 403);
        }

        $notification->update($request->all());
        return $notification;
    }

    public function destroy(Request $request, Notification $notification)
    {
        if ($request->user()->rol !== 'admin') {
            return response()->json(['message' => 'Solo un administrador puede eliminar notificaciones.'], 403);
        }

        $notification->delete();
        return $notification;
    }
}
