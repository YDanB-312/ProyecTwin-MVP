<?php

namespace App\Models;

use App\Support\Included;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class ClassGroup extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['codigo', 'numero', 'nombre', 'estado', 'id_programa', 'id_instructor'];

    public $allowIncluded = ['program', 'instructor', 'instructor.generalUser', 'apprentices', 'apprentices.generalUser'];

    public function scopeIncluded(Builder $query)
    {
        Included::aplicar($query, $this, request('included'));
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
}
