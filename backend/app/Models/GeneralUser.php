<?php

namespace App\Models;

use Illuminate\Auth\Authenticatable;
use Illuminate\Auth\Passwords\CanResetPassword;
use Illuminate\Contracts\Auth\Authenticatable as AuthenticatableContract;
use Illuminate\Contracts\Auth\CanResetPassword as CanResetPasswordContract;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class GeneralUser extends Model implements AuthenticatableContract, CanResetPasswordContract
{
    use Authenticatable, CanResetPassword, HasFactory, HasApiTokens, Notifiable;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['nombre', 'apellido', 'correo', 'password', 'foto_url', 'rol', 'estado'];

    // Datos sensibles que nunca salen por la API (login/me/listados).
    protected $hidden = ['password', 'remember_token'];

    protected $casts = ['estado' => 'boolean'];

    // El correo es el identificador de acceso (no existe columna `email`).
    public function getEmailForPasswordReset()
    {
        return $this->correo;
    }

    // Las notificaciones por correo se envían al `correo` institucional.
    public function routeNotificationForMail()
    {
        return [$this->correo => trim(($this->nombre ?? '') . ' ' . ($this->apellido ?? ''))];
    }

    public function sendPasswordResetNotification($token)
    {
        $this->notify(new \App\Notifications\ResetPasswordNotification($token));
    }

    protected $allowIncluded = ['apprentice', 'instructor', 'admin', 'projects', 'notifications', 'comments', 'bugReports'];

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

    public function scopeByTrainingCenter(Builder $query, $trainingCenterId)
    {
        if (empty($trainingCenterId) || $trainingCenterId === 'todos') return $query;
        return $query->whereHas('apprentice.classGroup', fn (Builder $q) => $q->where('training_center_id', $trainingCenterId));
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

    // ---------------------------------------------------------------- Relaciones
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

    // ---------------------------------------------------------------- Rol / centro

    public function esSuperadmin(): bool
    {
        return $this->rol === 'superadmin';
    }

    // Admin de centro: cualquiera con rol admin que no sea superadmin.
    // Su alcance son los datos de su centro (ver centroId()).
    public function esAdminDeCentro(): bool
    {
        return $this->rol === 'admin';
    }

    // Centro del admin. null = global (superadmin) o admin sin centro asignado.
    public function centroId(): ?int
    {
        $id = optional($this->admin)->training_center_id;
        return $id !== null ? (int) $id : null;
    }

    // ¿Pertenece al centro dado? El aprendiz por su ficha, el instructor por
    // alguna de sus fichas y el admin por su perfil. El superadmin es global.
    public function perteneceAlCentro(?int $centroId): bool
    {
        if ($this->rol === 'aprendiz') {
            return (int) optional(optional($this->apprentice)->classGroup)->training_center_id === (int) $centroId;
        }
        if ($this->rol === 'instructor') {
            $instructor = $this->instructor;
            return $instructor
                ? $instructor->classGroups()->where('training_center_id', $centroId)->exists()
                : false;
        }
        if ($this->rol === 'admin') {
            return (int) optional($this->admin)->training_center_id === (int) $centroId;
        }
        return false;
    }
}
