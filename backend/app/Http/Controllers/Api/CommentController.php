<?php

namespace App\Http\Controllers\Api;

use App\Models\Comment;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class CommentController extends Controller
{
    public function index()
    {
        $items = Comment::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'texto' => 'required',
            'id_proyecto' => 'required|exists:projects,id',
            'id_usuario' => 'required|exists:general_users,id',
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
