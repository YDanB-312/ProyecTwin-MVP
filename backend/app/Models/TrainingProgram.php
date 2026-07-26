<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class TrainingProgram extends Model
{
    use HasFactory;

    protected $fillable = ['nombre', 'nivel', 'num_trimestres'];

    protected $allowIncluded = ['apprentices', 'classGroups'];

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

    public function apprentices()
    {
        return $this->hasMany(Apprentice::class, 'id_programa');
    }

    public function classGroups()
    {
        return $this->hasMany(ClassGroup::class, 'id_programa');
    }
}
