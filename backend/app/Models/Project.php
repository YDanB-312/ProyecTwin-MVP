<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Project extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = [
        'titulo', 'resumen', 'palabras_clave', 'area_aplicacion',
        'objetivo_general', 'objetivos_especificos', 'estado',
        'id_creador', 'id_instructor_asignado', 'id_class_group',
    ];

    protected $casts = [
        'objetivos_especificos' => 'array',
    ];

    protected $allowIncluded = ['creator', 'instructor', 'classGroup', 'apprentices', 'comments', 'similaritiesAsOrigin', 'similaritiesAsDestination'];

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
            $q->where('titulo', 'like', $t)->orWhereHas('creator', fn (Builder $qq) => $qq->where('nombre', 'like', $t));
        });
    }

    public function scopeByEstado(Builder $query, ?string $estado)
    {
        if (empty($estado) || $estado === 'todos') return $query;
        return $query->where('estado', $estado);
    }

    public function scopeByTrainingCenter(Builder $query, $trainingCenterId)
    {
        if (empty($trainingCenterId) || $trainingCenterId === 'todos') return $query;
        return $query->whereHas('classGroup', fn (Builder $q) => $q->where('training_center_id', $trainingCenterId));
    }

    public function scopeByFicha(Builder $query, $fichaId)
    {
        if (empty($fichaId) || $fichaId === 'todos') return $query;
        return $query->where('id_class_group', $fichaId);
    }

    public function scopeByPrograma(Builder $query, ?string $programa)
    {
        if (empty($programa) || $programa === 'todos') return $query;
        return $query->whereHas('classGroup.program', fn (Builder $q) => $q->where('nombre', $programa));
    }

    // Alcance por centro (coordinador): solo lo de SU centro. Un centro null
    // agrupa las propuestas sin centro (ámbito global/superadmin).
    public function scopeDelCentro(Builder $query, ?int $centroId): Builder
    {
        return $query->whereHas('classGroup', fn (Builder $q) => $q->where('training_center_id', $centroId));
    }

    // Alcance por rol para LISTADOS. Sin esto, cualquier autenticado listaba
    // TODAS las propuestas (título, resumen, objetivos).
    public function scopeParaUsuario(Builder $query, $user): Builder
    {
        if (!$user) return $query->whereRaw('1 = 0');
        if ($user->esSuperadmin()) return $query;
        if ($user->rol === 'admin') return $query->delCentro($user->centroId());

        if ($user->rol === 'instructor') {
            $instructorId = Instructor::where('id_usuario', $user->id)->value('id');
            if (!$instructorId) return $query->whereRaw('1 = 0');
            return $query->where(function (Builder $q) use ($instructorId) {
                $q->where('id_instructor_asignado', $instructorId)
                  ->orWhereHas('classGroup', fn (Builder $qq) => $qq->where('id_instructor', $instructorId));
            });
        }

        // Aprendiz: sus propuestas (creador o equipo) + las de su ficha (aula).
        $fichaIds = Apprentice::where('id_usuario', $user->id)->pluck('id_class_group')->filter();
        return $query->where(function (Builder $q) use ($user, $fichaIds) {
            $q->where('id_creador', $user->id)
              ->orWhereHas('apprentices', fn (Builder $a) => $a->where('id_usuario', $user->id));
            if ($fichaIds->isNotEmpty()) {
                $q->orWhereIn('id_class_group', $fichaIds);
            }
        });
    }

    // Propuestas "propias" (para comentarios): el aprendiz NO ve las ajenas de
    // su ficha; el instructor sí las de sus fichas/asignadas.
    public function scopeDeAutor(Builder $query, $user): Builder
    {
        if (!$user) return $query->whereRaw('1 = 0');
        if ($user->esSuperadmin()) return $query;
        if ($user->rol === 'admin') return $query->delCentro($user->centroId());

        if ($user->rol === 'instructor') {
            $instructorId = Instructor::where('id_usuario', $user->id)->value('id');
            if (!$instructorId) return $query->whereRaw('1 = 0');
            return $query->where(function (Builder $q) use ($instructorId) {
                $q->where('id_instructor_asignado', $instructorId)
                  ->orWhereHas('classGroup', fn (Builder $qq) => $qq->where('id_instructor', $instructorId));
            });
        }

        return $query->where(function (Builder $q) use ($user) {
            $q->where('id_creador', $user->id)
              ->orWhereHas('apprentices', fn (Builder $a) => $a->where('id_usuario', $user->id));
        });
    }

    // Detalle (show): el instructor puede leer en modo lectura las propuestas de
    // su mismo programa (así lo permite la UI); el aprendiz, las suyas y su ficha.
    public function scopeParaDetalle(Builder $query, $user): Builder
    {
        if (!$user) return $query->whereRaw('1 = 0');
        if ($user->esSuperadmin()) return $query;
        if ($user->rol === 'admin') return $query->delCentro($user->centroId());

        if ($user->rol === 'instructor') {
            $instructorId = Instructor::where('id_usuario', $user->id)->value('id');
            $programaIds = $instructorId
                ? ClassGroup::where('id_instructor', $instructorId)->pluck('id_programa')->filter()->unique()
                : collect();
            if (!$instructorId && $programaIds->isEmpty()) return $query->whereRaw('1 = 0');
            return $query->where(function (Builder $q) use ($instructorId, $programaIds) {
                if ($instructorId) {
                    $q->where('id_instructor_asignado', $instructorId)
                      ->orWhereHas('classGroup', fn (Builder $qq) => $qq->where('id_instructor', $instructorId));
                }
                if ($programaIds->isNotEmpty()) {
                    $q->orWhereHas('classGroup', fn (Builder $qq) => $qq->whereIn('id_programa', $programaIds));
                }
            });
        }

        $fichaIds = Apprentice::where('id_usuario', $user->id)->pluck('id_class_group')->filter();
        return $query->where(function (Builder $q) use ($user, $fichaIds) {
            $q->where('id_creador', $user->id)
              ->orWhereHas('apprentices', fn (Builder $a) => $a->where('id_usuario', $user->id));
            if ($fichaIds->isNotEmpty()) {
                $q->orWhereIn('id_class_group', $fichaIds);
            }
        });
    }

    // ---------------------------------------------------------------- Relaciones
    public function creator()
    {
        return $this->belongsTo(GeneralUser::class, 'id_creador');
    }

    public function instructor()
    {
        return $this->belongsTo(Instructor::class, 'id_instructor_asignado');
    }

    public function classGroup()
    {
        return $this->belongsTo(ClassGroup::class, 'id_class_group');
    }

    public function apprentices()
    {
        return $this->belongsToMany(Apprentice::class, 'apprentice_projects', 'id_proyecto', 'id_aprendiz');
    }

    public function comments()
    {
        return $this->hasMany(Comment::class, 'id_proyecto');
    }

    public function similaritiesAsOrigin()
    {
        return $this->hasMany(Similarity::class, 'id_proyecto_1');
    }

    public function similaritiesAsDestination()
    {
        return $this->hasMany(Similarity::class, 'id_proyecto_2');
    }
}
