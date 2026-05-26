<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Admin extends Model
{
    use HasFactory;

    public function generalUser()
    {
        return $this->belongsTo(GeneralUser::class, 'id_usuario');
    }

    public function bugReports()
    {
        return $this->hasMany(BugReport::class, 'id_admin');
    }
}
