<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class MotorConfig extends Model
{
    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['umbral', 'meses', 'training_center_id'];

    protected $allowIncluded = ['trainingCenter'];

    protected $casts = [
        'umbral' => 'float',
        'meses' => 'integer',
    ];

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

    public function trainingCenter()
    {
        return $this->belongsTo(TrainingCenter::class, 'training_center_id');
    }

    // Config vigente de un centro: la suya si la definió, si no la global.
    public static function paraCentro(?int $centroId): self
    {
        if ($centroId) {
            $propia = static::where('training_center_id', $centroId)->first();
            if ($propia) return $propia;
        }
        return static::global();
    }

    // Fila por defecto (sin centro). Se crea si no existe.
    public static function global(): self
    {
        return static::firstOrCreate(
            ['training_center_id' => null],
            ['umbral' => 0.2, 'meses' => 12]
        );
    }
}
