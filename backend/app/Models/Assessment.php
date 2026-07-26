<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Assessment extends Model
{
    use HasFactory;

    protected $fillable = ['decision', 'puntaje', 'tipo_revision', 'texto', 'fecha', 'tiempo_respuesta_dias', 'id_proyecto', 'id_instructor'];

    protected $allowIncluded = ['project', 'instructor'];

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

    public function project()
    {
        return $this->belongsTo(Project::class, 'id_proyecto');
    }

    public function instructor()
    {
        return $this->belongsTo(Instructor::class, 'id_instructor');
    }
}
