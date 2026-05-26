<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Similarity extends Model
{
    use HasFactory;

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
