<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Instructor extends Model
{
    use HasFactory;

    protected $fillable = ['fecha_ingreso', 'plantilla_comentarios', 'id_usuario'];

    protected $allowIncluded = ['generalUser', 'projects', 'assessments', 'similarities', 'classGroups'];

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

    public function generalUser()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }

    public function projects()
    {
        return $this->hasMany(Project::class, 'id_instructor_asignado');
    }

    public function assessments()
    {
        return $this->hasMany(Assessment::class, 'id_instructor');
    }

    public function similarities()
    {
        return $this->hasMany(Similarity::class, 'id_instructor');
    }

    public function classGroups()
    {
        return $this->hasMany(ClassGroup::class, 'id_instructor');
    }
}
