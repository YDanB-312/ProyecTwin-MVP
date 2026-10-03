<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ActivacionCuenta extends Model
{
    public static $snakeAttributes = false;

    protected $table = 'activaciones_cuenta';

    protected $fillable = ['id_usuario', 'codigo_hash', 'expira_en', 'usado_en'];

    protected $casts = [
        'expira_en' => 'datetime',
        'usado_en' => 'datetime',
    ];

    public function user()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }
}
