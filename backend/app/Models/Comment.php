<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Comment extends Model
{
    use HasFactory;

    public function project()
    {
        return $this->belongsTo(Project::class, 'id_proyecto');
    }

    public function user()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }
}
