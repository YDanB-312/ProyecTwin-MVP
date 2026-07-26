<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class ClassGroup extends Model
{
    use HasFactory;

    protected $fillable = ['codigo', 'nombre', 'estado', 'id_programa', 'id_instructor'];

    protected $allowIncluded = ['program', 'instructor', 'apprentices'];

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
