<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class GeneralUser extends Model
{
    use HasFactory;

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
