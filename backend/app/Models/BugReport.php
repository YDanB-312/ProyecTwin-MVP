<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class BugReport extends Model
{
    use HasFactory;

    public function generalUser()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }

    public function admin()
    {
        return $this->belongsTo(Admin::class, 'id_admin');
    }
}
