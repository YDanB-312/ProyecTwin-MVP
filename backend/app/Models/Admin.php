<?php

namespace App\Models;

use App\Support\Included;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Builder;

class Admin extends Model
{
    use HasFactory;

    // Relaciones en camelCase en el JSON (el frontend es JS).
    public static $snakeAttributes = false;

    protected $fillable = ['id_usuario'];

    public $allowIncluded = ['generalUser'];

    public function scopeIncluded(Builder $query)
    {
        Included::aplicar($query, $this, request('included'));
    }

    public function generalUser()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }
}
