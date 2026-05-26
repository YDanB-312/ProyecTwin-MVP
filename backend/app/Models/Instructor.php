<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Instructor extends Model
{
    use HasFactory;

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

