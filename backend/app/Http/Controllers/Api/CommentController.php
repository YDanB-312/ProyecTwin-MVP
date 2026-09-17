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
            ->when($request->query('id_proyecto'), fn ($q, $id) => $q->where('id_proyecto', $id))
            ->orderByDesc('id')
            ->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'texto' => 'required',
            'id_proyecto' => 'required|exists:projects,id',
            'id_usuario' => 'required|exists:general_users,id',
            'respuesta_a' => 'nullable|exists:comments,id',
        ]);

        $item = Comment::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = Comment::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, Comment $comment)
    {
        $request->validate([
            'texto' => 'required',
            'id_proyecto' => 'required|exists:projects,id',
            'id_usuario' => 'required|exists:general_users,id',
            'respuesta_a' => 'nullable|exists:comments,id',
        ]);

        $comment->update($request->all());
        return $comment;
    }

    public function destroy(Comment $comment)
    {
        $comment->delete();
        return $comment;
    }
}
