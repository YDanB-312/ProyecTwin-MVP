<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Instructor extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['fecha_ingreso', 'id_usuario'];

    // Normaliza cualquier fecha ISO/datetime a la columna `date`.
    public function setFechaIngresoAttribute($valor): void
    {
        $this->attributes['fecha_ingreso'] = $valor ? substr((string) $valor, 0, 10) : null;
    }

    protected $allowIncluded = ['generalUser', 'projects', 'classGroups'];

    public function scopeIncluded(Builder $query)
    {
        if (empty($this->allowIncluded) || empty(request('included'))) {
            return;
        }
        $relations = explode(',', request('included'));
        $allowIncluded = collect($this->allowIncluded);
        foreach ($relations as $key => $relationship) {
            // Admite rutas anidadas (classGroup.program): valida la raiz.
            if (!$allowIncluded->contains(explode('.', $relationship)[0])) {
                unset($relations[$key]);
            }
        }
        $query->with($relations);
    }

    public function generalUser()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }

    public function projects()
    {
        return $this->hasMany(Project::class, 'id_instructor_asignado');
    }

    public function classGroups()
    {
        return $this->hasMany(ClassGroup::class, 'id_instructor');
    }
}
