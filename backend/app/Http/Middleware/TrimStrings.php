<?php

namespace App\Http\Middleware;

use Illuminate\Foundation\Http\Middleware\TrimStrings as Middleware;

class TrimStrings extends Middleware
{
    /**
     * The names of the attributes that should not be trimmed.
     *
     * @var array<int, string>
     */
    protected $except = [
        'current_password',
        'password',
        'password_confirmation',
        // El proyecto nombra este campo en español; sin esta excepción una
        // contraseña con espacios en los extremos funciona en login pero
        // falla al cambiarla o al cambiar el correo.
        'password_actual',
    ];
}
