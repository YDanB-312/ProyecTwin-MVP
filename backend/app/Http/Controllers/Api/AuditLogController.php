<?php

namespace App\Http\Controllers\Api;

use App\Models\AuditLog;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class AuditLogController extends Controller
{
    // Últimas acciones registradas (solo admin). No hay ruta de escritura ni de
    // borrado: la bitácora es inmutable desde la API.
    public function index(Request $request)
    {
        // Filtros validados: una fecha o un id inválidos no rompen la consulta.
        $request->validate([
            'accion' => 'nullable|string',
            'entidad' => 'nullable|string',
            'id_usuario' => 'nullable|integer',
            'desde' => 'nullable|date',
            'hasta' => 'nullable|date',
        ]);

        return AuditLog::included()
            ->when($request->query('accion'), fn ($q, $a) => $q->where('accion', $a))
            ->when($request->query('entidad'), fn ($q, $e) => $q->where('entidad', $e))
            ->when($request->query('id_usuario'), fn ($q, $u) => $q->where('id_usuario', $u))
            ->when($request->query('desde'), fn ($q, $d) => $q->whereDate('created_at', '>=', $d))
            ->when($request->query('hasta'), fn ($q, $h) => $q->whereDate('created_at', '<=', $h))
            ->orderByDesc('id')
            ->limit(500)
            ->get();
    }
}
