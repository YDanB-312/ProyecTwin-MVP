<?php

namespace App\Policies;

use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;

// Gestión de fichas: admin cualquiera; instructor solo las suyas. Mantiene el
// auto-sanado del perfil de instructor (igual que el controlador).
class ClassGroupPolicy
{
    public function manage(GeneralUser $user, ClassGroup $ficha): bool
    {
        if ($user->rol === 'admin') return true;
        if ($user->rol !== 'instructor') return false;

        $instructor = Instructor::firstOrCreate(
            ['id_usuario' => $user->id],
            ['fecha_ingreso' => now()->toDateString()]
        );

        return (int) $ficha->id_instructor === (int) $instructor->id;
    }
}
