<?php

namespace App\Models;

use App\Support\Included;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class ProjectHistory extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['id_proyecto', 'id_usuario', 'accion', 'detalle'];

    protected $casts = ['detalle' => 'array'];

    public $allowIncluded = ['user'];

    public function scopeIncluded(Builder $query)
    {
        Included::aplicar($query, $this, request('included'));
    }

    public function project()
    {
        return $this->belongsTo(Project::class, 'id_proyecto');
    }

    public function user()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }
}
