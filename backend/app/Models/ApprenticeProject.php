<?php

namespace App\Models;

use App\Support\Included;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class ApprenticeProject extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['id_aprendiz', 'id_proyecto'];

    public $allowIncluded = ['apprentice', 'apprentice.generalUser', 'project'];

    public function scopeIncluded(Builder $query)
    {
        Included::aplicar($query, $this, request('included'));
    }

    public function apprentice()
    {
        return $this->belongsTo(Apprentice::class, 'id_aprendiz');
    }

    public function project()
    {
        return $this->belongsTo(Project::class, 'id_proyecto');
    }
}
