<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Assessment extends Model
{
    use HasFactory;

    public function project()
    {
        return $this->belongsTo(Project::class, 'id_proyecto');
    }

    public function instructor()
    {
        return $this->belongsTo(Instructor::class, 'id_instructor');
    }
}
