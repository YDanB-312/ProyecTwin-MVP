<?php

namespace App\Policies;

use App\Models\Comment;
use App\Models\GeneralUser;
use App\Models\Project;

// Reglas de las observaciones: visibilidad por alcance, y edición/borrado solo
// del autor o un admin. Comentar exige poder escribir la propuesta.
class CommentPolicy
{
    public function view(GeneralUser $user, Comment $comment): bool
    {
        return Comment::where('id', $comment->id)->paraUsuario($user)->exists();
    }

    public function create(GeneralUser $user, Project $project): bool
    {
        return $project->puedeEscribir($user);
    }

    public function update(GeneralUser $user, Comment $comment): bool
    {
        return (int) $comment->id_usuario === (int) $user->id || $user->rol === 'admin';
    }

    public function delete(GeneralUser $user, Comment $comment): bool
    {
        return (int) $comment->id_usuario === (int) $user->id || $user->rol === 'admin';
    }
}
