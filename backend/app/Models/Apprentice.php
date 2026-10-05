<?php

namespace App\Models;

use App\Support\Included;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Apprentice extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['codigo', 'id_class_group', 'id_usuario', 'id_programa'];

    public $allowIncluded = ['generalUser', 'classGroup', 'program', 'projects'];

    public function scopeIncluded(Builder $query)
    {
        Included::aplicar($query, $this, request('included'));
    }

    // Código correlativo del aprendiz (AP-001…), generado por el servidor.
    public static function codigoDisponible(): string
    {
        $n = (int) static::max('id') + 1;
        do {
            $codigo = 'AP-' . str_pad((string) $n, 3, '0', STR_PAD_LEFT);
            $n++;
        } while (static::where('codigo', $codigo)->exists());

        return $codigo;
    }

    public function generalUser()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }

    public function classGroup()
    {
        return $this->belongsTo(ClassGroup::class, 'id_class_group');
    }

    public function program()
    {
        return $this->belongsTo(TrainingProgram::class, 'id_programa');
    }

    public function projects()
    {
        return $this->belongsToMany(Project::class, 'apprentice_projects', 'id_aprendiz', 'id_proyecto');
    }
}
