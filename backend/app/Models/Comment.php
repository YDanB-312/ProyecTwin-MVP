<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Comment extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['texto', 'id_proyecto', 'id_usuario', 'respuesta_a'];

    protected $allowIncluded = ['project', 'user', 'parent', 'replies'];

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

    public function project()
    {
        return $this->belongsTo(Project::class, 'id_proyecto');
    }

    public function user()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }

    public function parent()
    {
        return $this->belongsTo(Comment::class, 'respuesta_a');
    }

    public function replies()
    {
        return $this->hasMany(Comment::class, 'respuesta_a');
    }
}
