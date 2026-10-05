<?php

namespace App\Models;

use App\Support\Included;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class TrainingProgram extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['nombre', 'nivel', 'num_trimestres', 'knowledge_network_id'];

    // Solo lo que el frontend necesita: las rutas públicas de catálogos no
    // deben poder cargar aprendices ni fichas (PII y códigos de unión).
    public $allowIncluded = ['knowledgeNetwork'];

    public function scopeIncluded(Builder $query)
    {
        Included::aplicar($query, $this, request('included'));
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
