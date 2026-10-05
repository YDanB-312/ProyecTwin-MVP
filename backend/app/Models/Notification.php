<?php

namespace App\Models;

use App\Support\Included;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Notification extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['titulo', 'descripcion', 'tipo', 'enlace', 'leida', 'fecha', 'id_usuario'];

    // Normaliza cualquier fecha ISO/datetime a la columna `date`.
    public function setFechaAttribute($valor): void
    {
        $this->attributes['fecha'] = $valor ? substr((string) $valor, 0, 10) : null;
    }

    public $allowIncluded = ['user'];

    public function scopeIncluded(Builder $query)
    {
        Included::aplicar($query, $this, request('included'));
    }

    public function user()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }
}
