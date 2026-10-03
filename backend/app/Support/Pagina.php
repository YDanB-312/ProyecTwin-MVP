<?php

namespace App\Support;

use Illuminate\Contracts\Pagination\LengthAwarePaginator;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Http\Request;
use Illuminate\Support\Collection;

// Paginación opt-in: si el cliente envía `page`, se devuelve un paginador
// (data + total + last_page); si no, la colección completa (compatibilidad con
// las vistas que aún filtran en el navegador).
class Pagina
{
    public static function aplicar(Builder $query, Request $request, int $porDefecto = 15): Collection|LengthAwarePaginator
    {
        if (!$request->filled('page')) {
            return $query->get();
        }

        $porPagina = (int) $request->query('per_page', $porDefecto);
        $porPagina = min(max($porPagina, 1), 100);

        return $query->paginate($porPagina);
    }
}
