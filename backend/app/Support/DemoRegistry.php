<?php

namespace App\Support;

use Illuminate\Support\Facades\DB;

// Propiedad de los registros demo. La clave es estable y no depende de campos
// editables (correo, título, etc.). El seeder solo adopta/crea registros que
// identifica explícitamente; nunca modifica entidades ajenas al escenario.
class DemoRegistry
{
    public static function id(string $tipo, string $clave): ?int
    {
        $id = DB::table('demo_seed_records')
            ->where('tipo', $tipo)
            ->where('clave', $clave)
            ->value('entidad_id');

        return $id === null ? null : (int) $id;
    }

    public static function registrar(string $tipo, string $clave, int $entidadId): void
    {
        DB::table('demo_seed_records')->updateOrInsert(
            ['tipo' => $tipo, 'clave' => $clave],
            [
                'entidad_id' => $entidadId,
                'updated_at' => now(),
                'created_at' => now(),
            ]
        );
    }

    public static function existe(string $tipo, string $clave): bool
    {
        return self::id($tipo, $clave) !== null;
    }
}
