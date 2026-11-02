<?php

namespace App\Support;

use App\Models\GeneralUser;
use Barryvdh\DomPDF\Facade\Pdf;
use Illuminate\Support\Collection;
use Symfony\Component\HttpFoundation\Response;

// PDF con el usuario y el estado de acceso. No descifra ni expone contraseñas:
// las cuentas nuevas se activan con el enlace enviado a su correo personal.
class CredencialesPdf
{
    // Filas del PDF (separadas de la generación para poder verificar el
    // contenido en pruebas sin depender del binario comprimido del PDF).
    public static function filas(Collection $usuarios, array $contexto = []): Collection
    {
        return $usuarios->map(function (GeneralUser $usuario) use ($contexto) {
            $estado = $usuario->email_verified_at === null
                ? 'Pendiente de activación'
                : 'Contraseña ya establecida';

            // Ficha/programa del usuario: la del aprendiz o la primera del instructor.
            $ficha = optional(optional($usuario->apprentice)->classGroup);
            $programa = optional(optional($ficha)->program)->nombre
                ?? optional(optional(optional($usuario->instructor)->classGroups)->first())->program->nombre
                ?? null;

            return [
                'nombre' => trim($usuario->nombre . ' ' . $usuario->apellido),
                'username' => $usuario->username,
                'estado' => $estado,
                'ficha' => $ficha?->numero ?? $contexto['ficha'] ?? '—',
                'programa' => $programa ?? $contexto['programa'] ?? '—',
                'rol' => ucfirst($usuario->rol),
            ];
        });
    }

    public static function generar(Collection $usuarios, array $contexto = []): Response
    {
        $filas = self::filas($usuarios, $contexto);

        $pdf = Pdf::loadView('pdf.credenciales', [
            'filas' => $filas,
            'contexto' => $contexto,
            'generado' => now()->format('d/m/Y H:i'),
        ])->setPaper('letter');

        $nombre = !empty($contexto['ficha'])
            ? 'credenciales-ficha-' . $contexto['ficha'] . '.pdf'
            : 'credenciales-usuarios.pdf';

        return $pdf->download($nombre);
    }
}
