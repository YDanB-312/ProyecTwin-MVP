<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class TrainingProgram extends Model
{
    use HasFactory;

    public function apprentices()
    {
        return $this->hasMany(Apprentice::class, 'id_programa');
    }
}
