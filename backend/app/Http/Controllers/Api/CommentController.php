<?php

namespace App\Http\Controllers\Api;

use App\Models\Comment;
use App\Models\Project;
use App\Http\Controllers\Controller;
use App\Http\Requests\CommentRequest;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;

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

    public function store(CommentRequest $request)
    {
        // Comentar exige poder ESCRIBIR: dentro de la ficha ACTIVA (o ser el
        // instructor/admin). Fuera de la ficha o con la ficha finalizada → solo
        // lectura.
        $proyecto = Project::findOrFail($request->id_proyecto);
        if (!Gate::allows('create', [Comment::class, $proyecto])) {
            return response()->json([
                'message' => 'Solo lectura: ya no puedes comentar esta propuesta.',
            ], 403);
        }

        // La respuesta debe pertenecer al MISMO proyecto: no se cruzan hilos.
        if ($request->filled('respuesta_a')) {
            $padre = Comment::findOrFail($request->respuesta_a);
            if ((int) $padre->id_proyecto !== (int) $request->id_proyecto) {
                return response()->json(['message' => 'La respuesta pertenece a otra observación.'], 422);
            }
        }

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
        $comment = Comment::findOrFail($id);
        if (!Gate::allows('view', $comment)) {
            return response()->json(['message' => 'No tienes acceso a esta observación.'], 403);
        }

        return Comment::included()->findOrFail($id);
    }

    public function update(CommentRequest $request, Comment $comment)
    {
        // Solo el autor (o un admin) edita la observación.
        if (!Gate::allows('update', $comment)) {
            return response()->json(['message' => 'Solo puedes editar tus propias observaciones.'], 403);
        }

        // Solo cambia el texto: la observación no se mueve de proyecto ni de hilo
        // (trazabilidad del hilo de revisión).
        $comment->update([
            'texto' => $request->texto,
        ]);
        return $comment;
    }

    public function destroy(Request $request, Comment $comment)
    {
        // Solo el autor (o un admin) elimina la observación.
        if (!Gate::allows('delete', $comment)) {
            return response()->json(['message' => 'Solo puedes eliminar tus propias observaciones.'], 403);
        }

        $comment->delete();
        return $comment;
    }
}
