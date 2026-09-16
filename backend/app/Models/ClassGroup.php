<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class ClassGroup extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['codigo', 'numero', 'nombre', 'estado', 'id_programa', 'id_instructor', 'training_center_id'];

    protected $allowIncluded = ['program', 'instructor', 'apprentices', 'trainingCenter'];

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

    public function program()
    {
        return $this->belongsTo(TrainingProgram::class, 'id_programa');
    }

    public function instructor()
    {
        return $this->belongsTo(Instructor::class, 'id_instructor');
    }

    public function apprentices()
    {
        return $this->hasMany(Apprentice::class, 'id_class_group');
    }

    public function trainingCenter()
    {
        return $this->belongsTo(TrainingCenter::class, 'training_center_id');
    }
}
