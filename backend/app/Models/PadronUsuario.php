<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class PadronUsuario extends Model
{
    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $table = 'padron_usuarios';

    protected $fillable = [
        'tipo_documento', 'numero_documento', 'nombre', 'apellido',
        'correo', 'rol', 'id_programa', 'id_class_group', 'id_usuario', 'activo',
    ];

    protected $casts = ['activo' => 'boolean'];

    public function scopeSearch(Builder $query, ?string $term)
    {
        if (empty($term)) return $query;
        $t = "%{$term}%";
        return $query->where(function (Builder $q) use ($t) {
            $q->where('nombre', 'like', $t)
              ->orWhere('apellido', 'like', $t)
              ->orWhere('correo', 'like', $t)
              ->orWhere('numero_documento', 'like', $t);
        });
    }

    public function scopeByRol(Builder $query, ?string $rol)
    {
        if (empty($rol) || $rol === 'todos') return $query;
        return $query->where('rol', $rol);
    }

    public function scopeByFicha(Builder $query, $fichaId)
    {
        if (empty($fichaId) || $fichaId === 'todos') return $query;
        return $query->where('id_class_group', $fichaId);
    }

    public function programa()
    {
        return $this->belongsTo(TrainingProgram::class, 'id_programa');
    }

    public function classGroup()
    {
        return $this->belongsTo(ClassGroup::class, 'id_class_group');
    }

    public function user()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }
}
