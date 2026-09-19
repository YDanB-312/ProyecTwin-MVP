<?php

namespace App\Http\Controllers\Api;

use App\Models\Comment;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class CommentController extends Controller
{
    // Lista observaciones. Se puede acotar por proyecto (?id_proyecto=N) y
    // siempre llegan las más recientes primero.
    public function index(Request $request)
    {
        return Comment::included()
            ->paraUsuario($request->user())
            ->when($request->query('id_proyecto'), fn ($q, $id) => $q->where('id_proyecto', $id))
            ->orderByDesc('id')
            ->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'texto' => 'required',
            'id_proyecto' => 'required|exists:projects,id',
            'id_usuario' => 'nullable|exists:general_users,id',
            'respuesta_a' => 'nullable|exists:comments,id',
        ]);

        // La observación siempre se firma con el usuario del token.
        $item = Comment::create([
            'texto' => $request->texto,
            'id_proyecto' => $request->id_proyecto,
            'id_usuario' => $request->user()->id,
            'respuesta_a' => $request->respuesta_a,
        ]);
        return $item;
    }

    public function show(Request $request, $id)
    {
        $visible = Comment::where('id', $id)->paraUsuario($request->user())->exists();
        if (!$visible) {
            return response()->json(['message' => 'No tienes acceso a esta observación.'], 403);
        }

        return Comment::included()->findOrFail($id);
    }

    public function update(Request $request, Comment $comment)
    {
        $request->validate([
            'texto' => 'required',
            'id_proyecto' => 'required|exists:projects,id',
            'id_usuario' => 'nullable|exists:general_users,id',
            'respuesta_a' => 'nullable|exists:comments,id',
        ]);

        // Solo el autor (o un admin) edita la observación.
        $user = $request->user();
        if ((int) $comment->id_usuario !== (int) $user->id && $user->rol !== 'admin') {
            return response()->json(['message' => 'Solo puedes editar tus propias observaciones.'], 403);
        }

        $comment->update([
            'texto' => $request->texto,
            'id_proyecto' => $request->id_proyecto,
            'respuesta_a' => $request->respuesta_a,
        ]);
        return $comment;
    }

    public function destroy(Request $request, Comment $comment)
    {
        // Solo el autor (o un admin) elimina la observación.
        $user = $request->user();
        if ((int) $comment->id_usuario !== (int) $user->id && $user->rol !== 'admin') {
            return response()->json(['message' => 'Solo puedes eliminar tus propias observaciones.'], 403);
        }

        $comment->delete();
        return $comment;
    }
}
