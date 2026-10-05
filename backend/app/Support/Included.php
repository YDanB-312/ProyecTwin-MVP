<?php

namespace App\Support;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

// Filtra el parámetro `included` contra la lista blanca del modelo.
// Cada entrada es una ruta COMPLETA permitida ("instructor.generalUser"):
// validar solo la raíz dejaba pasar cadenas anidadas que exponían datos de
// otros recursos (p. ej. creator.projects.apprentices.generalUser).
class Included
{
    public static function aplicar(Builder $query, Model $modelo, $included): void
    {
        if (empty($modelo->allowIncluded) || !is_string($included) || $included === '') {
            return;
        }

        $validas = [];
        foreach (explode(',', $included) as $ruta) {
            $ruta = trim($ruta);
            if ($ruta !== '' && in_array($ruta, $modelo->allowIncluded, true)) {
                $validas[] = $ruta;
            }
        }

        if (!empty($validas)) {
            $query->with($validas);
        }
    }
}
