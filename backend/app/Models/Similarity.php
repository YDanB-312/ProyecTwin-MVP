<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Similarity extends Model
{
    use HasFactory;

    protected $fillable = ['porcentaje', 'estado', 'detalles', 'fecha', 'id_proyecto_1', 'id_proyecto_2', 'id_instructor'];

    protected $casts = [
        'detalles' => 'array',
    ];

    protected $allowIncluded = ['project1', 'project2', 'instructor'];

    public function scopeIncluded(Builder $query)
    {
        if (empty($this->allowIncluded) || empty(request('included'))) {
            return;
        }
        $relations = explode(',', request('included'));
        $allowIncluded = collect($this->allowIncluded);
        foreach ($relations as $key => $relationship) {
            if (!$allowIncluded->contains($relationship)) {
                unset($relations[$key]);
            }
        }
        $query->with($relations);
    }

    public function scopeSearch(Builder $query, ?string $term)
    {
        if (empty($term)) return $query;
        $t = "%{$term}%";
        return $query->where(function (Builder $q) use ($t) {
            $q->whereHas('project1', fn (Builder $qq) => $qq->where('titulo', 'like', $t))
              ->orWhereHas('project2', fn (Builder $qq) => $qq->where('titulo', 'like', $t));
        });
    }

    public function scopeByCentro(Builder $query, $centroId)
    {
        if (empty($centroId) || $centroId === 'todos') return $query;
        return $query->where(function (Builder $q) use ($centroId) {
            $q->whereHas('project1.classGroup', fn (Builder $qq) => $qq->where('centro_id', $centroId))
              ->orWhereHas('project2.classGroup', fn (Builder $qq) => $qq->where('centro_id', $centroId));
        });
    }

    public function scopeByFicha(Builder $query, $fichaId)
    {
        if (empty($fichaId) || $fichaId === 'todos') return $query;
        return $query->where(function (Builder $q) use ($fichaId) {
            $q->whereHas('project1', fn (Builder $qq) => $qq->where('id_class_group', $fichaId))
              ->orWhereHas('project2', fn (Builder $qq) => $qq->where('id_class_group', $fichaId));
        });
    }

    public function scopeByPrograma(Builder $query, ?string $programa)
    {
        if (empty($programa) || $programa === 'todos') return $query;
        return $query->where(function (Builder $q) use ($programa) {
            $q->whereHas('project1.classGroup.program', fn (Builder $qq) => $qq->where('nombre', $programa))
              ->orWhereHas('project2.classGroup.program', fn (Builder $qq) => $qq->where('nombre', $programa));
        });
    }

    public function project1()
    {
        return $this->belongsTo(Project::class, 'id_proyecto_1');
    }

    public function project2()
    {
        return $this->belongsTo(Project::class, 'id_proyecto_2');
    }

    public function instructor()
    {
        return $this->belongsTo(Instructor::class, 'id_instructor');
    }
}
