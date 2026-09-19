<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Similarity extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['porcentaje', 'detalles', 'fecha', 'id_proyecto_1', 'id_proyecto_2'];

    // Normaliza cualquier fecha ISO/datetime a la columna `date`.
    public function setFechaAttribute($valor): void
    {
        $this->attributes['fecha'] = $valor ? substr((string) $valor, 0, 10) : null;
    }

    protected $casts = [
        'detalles' => 'array',
    ];

    protected $allowIncluded = ['project1', 'project2'];

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

    // ---------------------------------------------------------------- Filtros de listado
    public function scopeSearch(Builder $query, ?string $term)
    {
        if (empty($term)) return $query;
        $t = "%{$term}%";
        return $query->where(function (Builder $q) use ($t) {
            $q->whereHas('project1', fn (Builder $qq) => $qq->where('titulo', 'like', $t))
              ->orWhereHas('project2', fn (Builder $qq) => $qq->where('titulo', 'like', $t));
        });
    }

    public function scopeByTrainingCenter(Builder $query, $trainingCenterId)
    {
        if (empty($trainingCenterId) || $trainingCenterId === 'todos') return $query;
        return $query->where(function (Builder $q) use ($trainingCenterId) {
            $q->whereHas('project1.classGroup', fn (Builder $qq) => $qq->where('training_center_id', $trainingCenterId))
              ->orWhereHas('project2.classGroup', fn (Builder $qq) => $qq->where('training_center_id', $trainingCenterId));
        });
    }

    public function scopeByFicha(Builder $query, $fichaId)
    {
        if (empty($fichaId) || $fichaId === 'todos') return $query;
        return $query->where(function (Builder $q) use ($fichaId) {
            $q->whereHas('project1', fn (Builder $qq) => $qq->where('id_class_group', $fichaId))
              ->orWhereHas('project2', fn (Builder $qq) => $qq->where('id_class_group', $fichaId));
        });
    }

    public function scopeByPrograma(Builder $query, ?string $programa)
    {
        if (empty($programa) || $programa === 'todos') return $query;
        return $query->where(function (Builder $q) use ($programa) {
            $q->whereHas('project1.classGroup.program', fn (Builder $qq) => $qq->where('nombre', $programa))
              ->orWhereHas('project2.classGroup.program', fn (Builder $qq) => $qq->where('nombre', $programa));
        });
    }

    // ---------------------------------------------------------------- Alcance

    // Aísla los pares según el rol. Sin esto, cualquier autenticado recibía
    // TODAS las similitudes del sistema (incluidas las de proyectos ajenos).
    public function scopeParaUsuario(Builder $query, $user): Builder
    {
        if (!$user) return $query->whereRaw('1 = 0');
        if ($user->rol === 'admin') return $query;

        if ($user->rol === 'instructor') {
            $fichaIds = ClassGroup::whereHas('instructor', fn (Builder $q) => $q->where('id_usuario', $user->id))
                ->pluck('id');
            return $query->where(function (Builder $q) use ($fichaIds) {
                $q->whereHas('project1', fn (Builder $qq) => $qq->whereIn('id_class_group', $fichaIds))
                  ->orWhereHas('project2', fn (Builder $qq) => $qq->whereIn('id_class_group', $fichaIds));
            });
        }

        // Aprendiz: solo pares que tocan alguno de sus proyectos (creador o equipo).
        return $query->where(function (Builder $q) use ($user) {
            $propio = fn (Builder $qq) => $qq->where('id_creador', $user->id)
                ->orWhereHas('apprentices', fn (Builder $a) => $a->where('id_usuario', $user->id));
            $q->whereHas('project1', $propio)
              ->orWhereHas('project2', $propio);
        });
    }

    // Filtra los pares que tocan uno o varios proyectos (lista separada por comas).
    public function scopeRelatedTo(Builder $query, $projectIds): Builder
    {
        $ids = collect(is_array($projectIds) ? $projectIds : explode(',', (string) $projectIds))
            ->map(fn ($id) => (int) trim((string) $id))
            ->filter()
            ->values();

        if ($ids->isEmpty()) return $query;

        return $query->where(function (Builder $q) use ($ids) {
            $q->whereIn('id_proyecto_1', $ids)->orWhereIn('id_proyecto_2', $ids);
        });
    }

    // ---------------------------------------------------------------- Relaciones
    public function project1()
    {
        return $this->belongsTo(Project::class, 'id_proyecto_1');
    }

    public function project2()
    {
        return $this->belongsTo(Project::class, 'id_proyecto_2');
    }
}
