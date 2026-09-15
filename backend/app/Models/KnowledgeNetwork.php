<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class KnowledgeNetwork extends Model
{
    use HasFactory;

    protected $fillable = ['nombre'];

    protected $allowIncluded = ['trainingPrograms'];

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

    public function trainingPrograms()
    {
        return $this->hasMany(TrainingProgram::class, 'knowledge_network_id');
    }
}
