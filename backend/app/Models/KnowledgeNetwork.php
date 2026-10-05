<?php

namespace App\Models;

use App\Support\Included;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class KnowledgeNetwork extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['nombre'];

    // Sin relaciones incluibles: la lectura es pública y no debe exponer
    // fichas ni aprendices.
    public $allowIncluded = [];

    public function scopeIncluded(Builder $query)
    {
        Included::aplicar($query, $this, request('included'));
    }

    public function trainingPrograms()
    {
        return $this->hasMany(TrainingProgram::class, 'knowledge_network_id');
    }
}
