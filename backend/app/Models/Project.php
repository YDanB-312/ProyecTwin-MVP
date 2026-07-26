<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Project extends Model
{
    use HasFactory;

    protected $fillable = ['titulo', 'tipo_proyecto', 'resumen', 'palabras_clave', 'area_aplicacion', 'tecnologias', 'objetivos', 'entregables', 'url_logo', 'estado', 'observaciones', 'id_creador', 'id_instructor_asignado'];

    protected $allowIncluded = ['creator', 'instructor', 'apprentices', 'assessments', 'comments', 'similaritiesAsOrigin', 'similaritiesAsDestination'];

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

    public function creator()
    {
        return $this->belongsTo(GeneralUser::class, 'id_creador');
    }

    public function instructor()
    {
        return $this->belongsTo(Instructor::class, 'id_instructor_asignado');
    }

    public function apprentices()
    {
        return $this->belongsToMany(Apprentice::class, 'apprentice_projects', 'id_proyecto', 'id_aprendiz');
    }

    public function assessments()
    {
        return $this->hasMany(Assessment::class, 'id_proyecto');
    }

    public function comments()
    {
        return $this->hasMany(Comment::class, 'id_proyecto');
    }

    public function similaritiesAsOrigin()
    {
        return $this->hasMany(Similarity::class, 'id_proyecto_1');
    }

    public function similaritiesAsDestination()
    {
        return $this->hasMany(Similarity::class, 'id_proyecto_2');
    }
}
