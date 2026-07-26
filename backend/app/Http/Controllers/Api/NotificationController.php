<?php

namespace App\Http\Controllers\Api;

use App\Models\Notification;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class NotificationController extends Controller
{
    public function index()
    {
        $items = Notification::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'titulo' => 'required|max:255',
            'descripcion' => 'nullable',
            'tipo' => 'required|in:similitud,revision,mensaje,sistema',
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
            'tipo' => 'required|in:similitud,revision,mensaje,sistema',
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
