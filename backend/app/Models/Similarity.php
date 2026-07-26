<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Similarity extends Model
{
    use HasFactory;

    protected $fillable = ['porcentaje', 'estado', 'detalles', 'fecha', 'id_proyecto_1', 'id_proyecto_2', 'id_instructor'];

    protected $allowIncluded = ['project1', 'project2', 'instructor'];

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
