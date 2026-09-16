<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class MotorConfig extends Model
{
    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['umbral', 'meses'];

    protected $casts = [
        'umbral' => 'float',
        'meses' => 'integer',
    ];
}
