<?php

// Router del servidor integrado de PHP (`php artisan serve`).
// Laravel sirve archivos estáticos existentes antes de llegar a las rutas; el
// APK histórico se conserva en public/apk, pero no debe ser descargable.
// Nginx también bloquea /apk en producción.

$publicPath = getcwd();

$uri = urldecode(
    parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH) ?? ''
);

$uriNormalizada = strtolower($uri);

if ($uriNormalizada === '/apk' || str_starts_with($uriNormalizada, '/apk/')) {
    http_response_code(404);
    echo 'Not Found';
    return;
}

if ($uri !== '/' && file_exists($publicPath.$uri)) {
    return false;
}

require_once $publicPath.'/index.php';
