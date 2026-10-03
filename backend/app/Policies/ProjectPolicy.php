<?php

namespace App\Policies;

use App\Models\GeneralUser;
use App\Models\Project;

// Reglas de autorización del dominio de propuestas. La edición/revisión sigue
// en el controlador por ahora; aquí vive la lectura y el borrado.
class ProjectPolicy
{
    // Visible si está dentro del alcance del usuario (autor/equipo/ficha/admin).
    public function view(GeneralUser $user, Project $project): bool
    {
        return Project::where('id', $project->id)->paraDetalle($user)->exists();
    }

    // Solo el creador aprendiz (dentro de su ficha activa) o un admin.
    public function delete(GeneralUser $user, Project $project): bool
    {
        return $user->rol === 'admin'
            || ($user->rol === 'aprendiz'
                && (int) $project->id_creador === (int) $user->id
                && $project->puedeEscribir($user));
    }
}
