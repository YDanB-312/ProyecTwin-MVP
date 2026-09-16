<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class TrainingProgram extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['nombre', 'nivel', 'num_trimestres', 'knowledge_network_id'];

    protected $allowIncluded = ['apprentices', 'classGroups', 'knowledgeNetwork'];

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

    public function apprentices()
    {
        return $this->hasMany(Apprentice::class, 'id_programa');
    }

    public function classGroups()
    {
        return $this->hasMany(ClassGroup::class, 'id_programa');
    }

    public function knowledgeNetwork()
    {
        return $this->belongsTo(KnowledgeNetwork::class, 'knowledge_network_id');
    }
}
