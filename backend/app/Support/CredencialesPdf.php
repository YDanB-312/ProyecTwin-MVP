<?php

namespace App\Support;

use App\Models\GeneralUser;
use Barryvdh\DomPDF\Facade\Pdf;
use Illuminate\Support\Collection;
use Illuminate\Support\Facades\Crypt;
use Symfony\Component\HttpFoundation\Response;

// PDF con las credenciales iniciales para entregar a los usuarios. La
// contraseña temporal solo se descifra aquí y solo mientras el usuario no haya
// definido la definitiva (después se muestra "ya establecida").
class CredencialesPdf
{
    public static function generar(Collection $usuarios, array $contexto = []): Response
    {
        $filas = $usuarios->map(function (GeneralUser $usuario) use ($contexto) {
            $temporal = null;
            if ($usuario->must_change_password && $usuario->password_temporal) {
                try {
                    $temporal = Crypt::decryptString($usuario->password_temporal);
                } catch (\Throwable $e) {
                    $temporal = null;
                }
            }

            // Ficha/programa del usuario: la del aprendiz o la primera del instructor.
            $ficha = optional(optional($usuario->apprentice)->classGroup);
            $programa = optional(optional($ficha)->program)->nombre
                ?? optional(optional(optional($usuario->instructor)->classGroups)->first())->program->nombre
                ?? null;

            return [
                'nombre' => trim($usuario->nombre . ' ' . $usuario->apellido),
                'username' => $usuario->username,
                'password' => $temporal ?? 'Contraseña ya establecida',
                'ficha' => $ficha?->numero ?? $contexto['ficha'] ?? '—',
                'programa' => $programa ?? $contexto['programa'] ?? '—',
                'rol' => ucfirst($usuario->rol),
            ];
        });

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
