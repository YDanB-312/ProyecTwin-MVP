<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class ApprenticeProject extends Model
{
    use HasFactory;

    public function apprentice()
    {
        return $this->belongsTo(Apprentice::class, 'id_aprendiz');
    }

    public function project()
    {
        return $this->belongsTo(Project::class, 'id_proyecto');
    }
}
