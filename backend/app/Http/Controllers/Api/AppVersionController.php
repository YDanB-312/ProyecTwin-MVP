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

        return response()->json([
            'latest_version' => (string) $config['version'],
            'minimum_supported_version' => (string) $config['min_version'],
            'enforce_minimum' => (bool) $config['enforce_minimum'],
            // El APK histórico no está autorizado para distribución pública.
            // Se mantiene la clave del contrato para clientes antiguos, pero
            // siempre vacía hasta que Oscar autorice su publicación.
            'download_url' => '',
            'release_notes' => (string) $config['release_notes'],
        ]);
    }
}
