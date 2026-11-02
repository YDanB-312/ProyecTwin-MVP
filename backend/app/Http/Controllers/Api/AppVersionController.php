<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;

class AppVersionController extends Controller
{
    // Consulta pública de la versión publicada del APK Android (detección de
    // actualizaciones en la app móvil). No exige token ni expone datos
    // personales: solo el contrato de versión configurado en app_version.php.
    public function android()
    {
        $config = config('app_version.android');
        $download = (string) ($config['download_url'] ?? '');

        if ($download !== '' && str_starts_with($download, '/')) {
            if (! is_file(public_path(ltrim($download, '/')))) {
                // La ruta local está configurada pero el APK no está presente:
                // no anunciamos una descarga que terminaría en 404.
                $download = '';
            } elseif (str_starts_with((string) config('app.url'), 'https://')) {
                // Android exige URLs absolutas https://; el SPA acepta ambas.
                $download = rtrim((string) config('app.url'), '/').$download;
            }
        }

        return response()->json([
            'latest_version' => (string) $config['version'],
            'minimum_supported_version' => (string) $config['min_version'],
            'enforce_minimum' => (bool) $config['enforce_minimum'],
            'download_url' => $download,
            'release_notes' => (string) $config['release_notes'],
        ]);
    }
}
