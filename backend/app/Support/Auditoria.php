<?php

namespace App\Support;

use App\Models\AuditLog;

// Registro de acciones sensibles. Nunca rompe la acción principal: si falla el
// insert de la bitácora, se ignora silenciosamente.
class Auditoria
{
    public static function registrar(string $accion, ?string $entidad = null, $entidadId = null, array $detalle = []): void
    {
        try {
            self::guardar($accion, $entidad, $entidadId, $detalle);
        } catch (\Throwable $e) {
            // Silencio deliberado para la acción principal (P-02): la bitácora
            // nunca la rompe, pero la traza queda registrada para diagnóstico.
            report($e);
        }
    }

    // Variante estricta para operaciones que deben abortar si la auditoría no
    // puede registrarse. El llamador decide la transacción; aquí solo se
    // propaga la excepción.
    public static function registrarEstricto(string $accion, ?string $entidad = null, $entidadId = null, array $detalle = []): void
    {
        self::guardar($accion, $entidad, $entidadId, $detalle);
    }

    private static function guardar(string $accion, ?string $entidad = null, $entidadId = null, array $detalle = []): void
    {
        $request = request();

        // En rutas públicas (registro) el guard por defecto no resuelve el
        // token; se consulta explícitamente el de Sanctum para saber quién fue.
        $actor = $request->user() ?? auth('sanctum')->user();

        AuditLog::create([
            'id_usuario' => optional($actor)->id,
            'accion' => $accion,
            'entidad' => $entidad,
            'entidad_id' => $entidadId,
            'detalle' => $detalle ?: null,
            'ip' => $request->ip(),
        ]);
    }
}
