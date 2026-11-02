<?php

namespace App\Http\Controllers\Api;

use App\Models\Comment;
use App\Models\Project;
use App\Support\PerfilEnRespuesta;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class CommentController extends Controller
{
    // Lista observaciones. Se puede acotar por proyecto (?id_proyecto=N) y
    // siempre llegan las más recientes primero.
    public function index(Request $request)
    {
        $request->validate(['id_proyecto' => 'nullable|integer']);

        $items = Comment::included()
            ->paraUsuario($request->user())
            ->when($request->query('id_proyecto'), fn ($q, $id) => $q->where('id_proyecto', $id))
            ->orderByDesc('id')
            ->get();

        return $this->respuestaComentarios($request->user(), $items);
    }

    public function store(Request $request)
    {
        $request->validate([
            'texto' => 'required|string',
            'id_proyecto' => 'bail|required|integer|exists:projects,id',
            'id_usuario' => 'bail|nullable|integer|exists:general_users,id',
            'respuesta_a' => 'bail|nullable|integer|exists:comments,id',
        ]);

        // Comentar exige poder ESCRIBIR: dentro de la ficha ACTIVA (o ser el
        // instructor/admin). Fuera de la ficha o con la ficha finalizada → solo
        // lectura.
        $proyecto = Project::findOrFail($request->id_proyecto);
        if (!$proyecto->puedeEscribir($request->user())) {
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
        $visible = Comment::where('id', $id)->paraUsuario($request->user())->exists();
        if (!$visible) {
            return response()->json(['message' => 'No tienes acceso a esta observación.'], 403);
        }

        return $this->respuestaComentarios(
            $request->user(),
            Comment::included()->findOrFail($id)
        );
    }

    // La relación `user` no debe exponer datos de contacto/estado fuera de
    // administración. Se reutiliza el mismo criterio que propuestas y
    // similitudes; el admin conserva la respuesta completa.
    private function respuestaComentarios($usuario, $items)
    {
        if (optional($usuario)->rol === 'admin') {
            return $items;
        }

        $datos = $items->toArray();

        return response()->json(
            PerfilEnRespuesta::redactar($datos, optional($usuario)->rol)
        );
    }

    public function update(Request $request, Comment $comment)
    {
        // Solo el autor (o un admin) edita la observación.
        $user = $request->user();
        if ((int) $comment->id_usuario !== (int) $user->id && $user->rol !== 'admin') {
            return response()->json(['message' => 'Solo puedes editar tus propias observaciones.'], 403);
        }

        // La regla Classroom también aplica al editar: ficha activa y pertenencia.
        if ($user->rol !== 'admin') {
            $proyecto = Project::find($comment->id_proyecto);
            if (!$proyecto || !$proyecto->puedeEscribir($user)) {
                return response()->json(['message' => 'Solo lectura: ya no puedes editar esta observación.'], 403);
            }
        }

        $request->validate([
            'texto' => 'required|string',
        ]);

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
        $user = $request->user();
        if ((int) $comment->id_usuario !== (int) $user->id && $user->rol !== 'admin') {
            return response()->json(['message' => 'Solo puedes eliminar tus propias observaciones.'], 403);
        }

        // La regla Classroom también aplica al borrar: ficha activa y pertenencia.
        if ($user->rol !== 'admin') {
            $proyecto = Project::find($comment->id_proyecto);
            if (!$proyecto || !$proyecto->puedeEscribir($user)) {
                return response()->json(['message' => 'Solo lectura: ya no puedes eliminar esta observación.'], 403);
            }
        }

        $comment->delete();
        return $comment;
    }
}
