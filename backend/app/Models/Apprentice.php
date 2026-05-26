<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Apprentice extends Model
{
    use HasFactory;

    public function generalUser()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }

    public function classGroup()
    {
        return $this->belongsTo(ClassGroup::class, 'id_class_group');
    }

    public function program()
    {
        return $this->belongsTo(TrainingProgram::class, 'id_programa');
    }

    public function projects()
    {
        return $this->belongsToMany(Project::class, 'apprentice_projects', 'id_aprendiz', 'id_proyecto');
    }
}
