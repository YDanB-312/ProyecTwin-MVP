<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class ApprenticeProject extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['id_aprendiz', 'id_proyecto'];

    protected $allowIncluded = ['apprentice', 'project'];

    public function scopeIncluded(Builder $query)
    {
        if (empty($this->allowIncluded) || empty(request('included'))) {
            return;
        }
        $relations = explode(',', request('included'));
        $allowIncluded = collect($this->allowIncluded);
        foreach ($relations as $key => $relationship) {
            // Admite rutas anidadas (classGroup.program): valida la raiz.
            if (!$allowIncluded->contains(explode('.', $relationship)[0])) {
                unset($relations[$key]);
            }
        }
        $query->with($relations);
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
