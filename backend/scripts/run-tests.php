<?php

declare(strict_types=1);

use App\Support\Pruebas\EjecutorPhpUnit;
use App\Support\Pruebas\ReglasBaseDatos;
use Illuminate\Contracts\Console\Kernel;

// Ejecutor de PHPUnit para desarrollo local.
//
// Valida una conexión MySQL local, crea una base desechable `ptwin_test_*`,
// migra y ejecuta la suite en esa misma base. La migración se ejecuta en este
// proceso para que no pueda usar una configuración distinta. La base se
// elimina siempre al terminar, salvo que el proceso sea terminado de forma
// abrupta.

$backend = dirname(__DIR__);
$cachePorDefecto = $backend.DIRECTORY_SEPARATOR.'bootstrap'.DIRECTORY_SEPARATOR.'cache'.DIRECTORY_SEPARATOR.'config.php';

if (is_file($cachePorDefecto)) {
    fwrite(STDERR, '[test] ABORTADO: existe configuración cacheada en '.$cachePorDefecto.'. Ejecuta `php artisan config:clear`.'."\n");
    exit(1);
}

$cacheEntorno = getenv('APP_CONFIG_CACHE');

if (is_string($cacheEntorno) && trim($cacheEntorno) !== '' && file_exists($cacheEntorno)) {
    fwrite(STDERR, '[test] ABORTADO: existe configuración cacheada en '.$cacheEntorno.'. Ejecuta `php artisan config:clear`.'."\n");
    exit(1);
}

$envFile = $backend.DIRECTORY_SEPARATOR.'.env';

if (! is_file($envFile)) {
    fwrite(STDERR, "[test] Falta backend/.env. Copia .env.example a .env y configura la conexión local.\n");
    exit(1);
}

$phpunit = $backend.DIRECTORY_SEPARATOR.'vendor'.DIRECTORY_SEPARATOR.'bin'.DIRECTORY_SEPARATOR.'phpunit';

if (! is_file($phpunit)) {
    fwrite(STDERR, "[test] Falta vendor/. Ejecuta `composer install` en backend.\n");
    exit(1);
}

require $backend.DIRECTORY_SEPARATOR.'vendor'.DIRECTORY_SEPARATOR.'autoload.php';

$app = require $backend.DIRECTORY_SEPARATOR.'bootstrap'.DIRECTORY_SEPARATOR.'app.php';
$app->make(Kernel::class)->bootstrap();

try {
    $cacheEfectivo = ReglasBaseDatos::rutaCacheEfectiva(
        $backend,
        $app->getCachedConfigPath(),
        env('APP_CONFIG_CACHE'),
    );

    if ($cacheEfectivo !== null) {
        throw new RuntimeException(
            'Existe configuración cacheada en '.$cacheEfectivo.'. Ejecuta `php artisan config:clear`.'
        );
    }

    $configuracion = ReglasBaseDatos::configuracionValidada($app);
} catch (Throwable $e) {
    fwrite(STDERR, '[test] ABORTADO: '.$e->getMessage()."\n");
    exit(1);
}

$ejecutor = new EjecutorPhpUnit($backend, $configuracion, array_slice($argv, 1));

exit($ejecutor->ejecutar());
