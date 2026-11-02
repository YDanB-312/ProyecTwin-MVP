<?php

namespace App\Support;

use App\Models\GeneralUser;
use Illuminate\Http\Exceptions\HttpResponseException;

/**
 * Serializa las operaciones que pueden reducir el número de administradores
 * activos. Debe invocarse dentro de una transacción: bloquea las filas de
 * todos los administradores activos y recién entonces cuenta.
 */
class GuardiaAdministradores
{
    public static function bloquearYAfirmar(): void
    {
        // La colección bloqueada es la fuente del conteo: un SELECT COUNT()
        // posterior podría usar una instantánea REPEATABLE READ anterior a la
        // transacción concurrente y dejar pasar el último admin.
        $activos = GeneralUser::where('rol', 'admin')
            ->where('estado', true)
            ->orderBy('id')
            ->lockForUpdate()
            ->get();

        if ($activos->count() <= 1) {
            throw new HttpResponseException(response()->json([
                'message' => 'No puedes dejar el sistema sin administradores activos.',
            ], 409));
        }
    }
}
