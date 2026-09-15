<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;
use Laravel\Sanctum\HasApiTokens;

class GeneralUser extends Model
{
    use HasFactory, HasApiTokens;

    protected $fillable = ['nombre', 'apellido', 'correo', 'password', 'foto_url', 'rol', 'estado', 'notif_similitud', 'notif_comentarios_instructor', 'notif_nuevos_proyectos', 'notif_revisiones_pendientes', 'notif_noticias_sistema'];

    protected $allowIncluded = ['apprentice', 'instructor', 'admin', 'projects', 'notifications', 'comments', 'bugReports'];

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

    public function scopeSearch(Builder $query, ?string $term)
    {
        if (empty($term)) return $query;
        $t = "%{$term}%";
        return $query->where(function (Builder $q) use ($t) {
            $q->where('nombre', 'like', $t)->orWhere('correo', 'like', $t);
        });
    }

    public function scopeByRol(Builder $query, ?string $rol)
    {
        if (empty($rol) || $rol === 'todos') return $query;
        return $query->where('rol', $rol);
    }

    public function scopeByEstado(Builder $query, ?string $estado)
    {
        if (empty($estado) || $estado === 'todos') return $query;
        $val = $estado === 'activo' ? 1 : ($estado === 'suspendido' ? 0 : $estado);
        return $query->where('estado', $val);
    }

    public function scopeByCentro(Builder $query, $centroId)
    {
        if (empty($centroId) || $centroId === 'todos') return $query;
        return $query->whereHas('apprentice.classGroup', fn (Builder $q) => $q->where('centro_id', $centroId));
    }

    public function scopeByFicha(Builder $query, $fichaId)
    {
        if (empty($fichaId) || $fichaId === 'todos') return $query;
        return $query->whereHas('apprentice', fn (Builder $q) => $q->where('id_class_group', $fichaId));
    }

    public function scopeByPrograma(Builder $query, ?string $programa)
    {
        if (empty($programa) || $programa === 'todos') return $query;
        return $query->whereHas('apprentice.program', fn (Builder $q) => $q->where('nombre', $programa))
            ->orWhereHas('apprentice.classGroup.program', fn (Builder $q) => $q->where('nombre', $programa));
    }

    public function apprentice()
    {
        return $this->hasOne(Apprentice::class, 'id_usuario');
    }

    public function instructor()
    {
        return $this->hasOne(Instructor::class, 'id_usuario');
    }

    public function admin()
    {
        return $this->hasOne(Admin::class, 'id_usuario');
    }

    public function projects()
    {
        return $this->hasMany(Project::class, 'id_creador');
    }

    public function notifications()
    {
        return $this->hasMany(Notification::class, 'id_usuario');
    }

    public function comments()
    {
        return $this->hasMany(Comment::class, 'id_usuario');
    }

    public function bugReports()
    {
        return $this->hasMany(BugReport::class, 'id_usuario');
    }
}
