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
        $apk = public_path('apk/ProyecTWin.apk');
        $apkExiste = is_file($apk);

        $url = trim((string) ($config['download_url'] ?? ''));
        if ($url !== '' && str_starts_with($url, '/')) {
            $url = rtrim((string) config('app.url'), '/') . $url;
        }
        // La app solo acepta descargas HTTPS. Una URL relativa que termine en
        // HTTP local se devuelve vacía para no ofrecer un enlace inseguro.
        if (! str_starts_with($url, 'https://')) {
            $url = '';
        }

        $sha256 = strtolower(trim((string) ($config['sha256'] ?? '')));
        if ($sha256 === '' && $apkExiste) {
            $sha256 = hash_file('sha256', $apk) ?: '';
        }
        $size = (int) ($config['size'] ?? 0);
        if ($size <= 0 && $apkExiste) {
            $size = (int) filesize($apk);
        }
        $publishedAt = trim((string) ($config['published_at'] ?? ''));
        if ($publishedAt === '' && $apkExiste) {
            $publishedAt = gmdate('c', (int) filemtime($apk));
        }

        return response()->json([
            'latest_version' => (string) $config['version'],
            'latest_version_code' => (int) $config['version_code'],
            'minimum_supported_version' => (string) $config['min_version'],
            'minimum_supported_version_code' => (int) $config['min_version_code'],
            'enforce_minimum' => (bool) $config['enforce_minimum'],
            'download_url' => $url,
            'apk_sha256' => $sha256,
            'apk_size' => $size,
            'published_at' => $publishedAt,
            'release_notes' => (string) $config['release_notes'],
        ]);
    }
}
