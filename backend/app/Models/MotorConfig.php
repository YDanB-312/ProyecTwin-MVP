<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\UniqueConstraintViolationException;

class MotorConfig extends Model
{
    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    // `id` sólo se usa para el alta inicial del singleton con id = 1.
    protected $fillable = ['id', 'umbral', 'meses'];

    protected $casts = [
        'umbral' => 'float',
        'meses' => 'integer',
    ];

    // Recupera la configuración global. Si ya existe cualquier fila, la
    // reutiliza; si no existe, crea la fila canónica con id = 1. La clave
    // primaria impide que dos procesos concurrentes creen dos filas iniciales.
    // No hay una restricción adicional que impida inserciones directas con
    // otros ids; todos los caminos de la aplicación pasan por este método.
    public static function actual(): self
    {
        $config = static::query()->orderBy('id')->first();
        if ($config) {
            return $config;
        }

        try {
            return static::firstOrCreate(
                ['id' => 1],
                ['umbral' => 0.30, 'meses' => 12],
            );
        } catch (UniqueConstraintViolationException $e) {
            // Si otra transacción acaba de crear id = 1, la instantánea
            // REPEATABLE READ puede no verla con una lectura normal. La
            // lectura de bloqueo sí ve lo último confirmado.
            $config = static::whereKey(1)->lockForUpdate()->first();
            if (! $config) {
                throw $e;
            }

            return $config;
        }
    }
}
