<?php

namespace App\Support;

use App\Models\ActivacionCuenta;
use Illuminate\Support\Facades\Hash;

// Código de activación de cuentas: se guarda con hash (nunca en claro), es de
// un solo uso y vence. Lo entrega el admin/instructor a la persona.
class CodigoActivacion
{
    public static function generar(int $idUsuario): string
    {
        $codigo = str_pad((string) random_int(0, 999999), 6, '0', STR_PAD_LEFT);

        // Un solo código vigente por usuario: se anulan los anteriores.
        ActivacionCuenta::where('id_usuario', $idUsuario)->whereNull('usado_en')->delete();

        ActivacionCuenta::create([
            'id_usuario' => $idUsuario,
            'codigo_hash' => Hash::make($codigo),
            'expira_en' => now()->addMinutes((int) config('identidad.activacion.minutos')),
        ]);

        return $codigo;
    }

    public static function validar(int $idUsuario, string $codigo): bool
    {
        $activacion = static::vigente($idUsuario);
        if (!$activacion || !Hash::check($codigo, $activacion->codigo_hash)) {
            return false;
        }

        $activacion->update(['usado_en' => now()]);

        return true;
    }

    public static function vigente(int $idUsuario): ?ActivacionCuenta
    {
        $activacion = ActivacionCuenta::where('id_usuario', $idUsuario)
            ->whereNull('usado_en')
            ->latest('id')
            ->first();

        return ($activacion && $activacion->expira_en->isFuture()) ? $activacion : null;
    }
}
