<?php

declare(strict_types=1);

use Illuminate\Contracts\Console\Kernel;

// Ejecutor de PHPUnit para desarrollo local.
//
// Crea una base desechable `ptwin_test_*`, ejecuta la suite con esa base y la
// elimina al terminar. No usa `proyectwin_mvp` como objetivo ni imprime
// credenciales. Si falta una condición de seguridad, aborta antes de crear
// nada.

$backend = dirname(__DIR__);
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

$config = (array) config('database.connections.mysql');
$driver = (string) ($config['driver'] ?? '');
$host = (string) ($config['host'] ?? '');
$port = (string) ($config['port'] ?? '');
$username = (string) ($config['username'] ?? '');
$password = (string) ($config['password'] ?? '');
$url = $config['url'] ?? env('DATABASE_URL');

$error = null;

if (filled($url)) {
    $error = 'DATABASE_URL no está permitido; usa DB_HOST/DB_PORT/DB_DATABASE explícitos.';
} elseif ($driver !== 'mysql') {
    $error = 'La conexión de pruebas debe ser mysql.';
} elseif (! in_array($host, ['127.0.0.1', 'localhost', '::1'], true)) {
    $error = 'La base de pruebas debe estar en loopback.';
} elseif ($port !== '3306') {
    $error = 'La base de pruebas debe usar el puerto 3306.';
}

if ($error !== null) {
    fwrite(STDERR, '[test] ABORTADO: '.$error."\n");
    exit(1);
}

$name = 'ptwin_test_'.gmdate('Ymd_His').'_'.bin2hex(random_bytes(3));
if (! preg_match('/^ptwin_test_[a-z0-9_]{6,50}$/D', $name)) {
    fwrite(STDERR, "[test] ABORTADO: nombre de base desechable inválido.\n");
    exit(1);
}

try {
    $pdo = new \PDO(
        "mysql:host={$host};port={$port};charset=utf8mb4",
        $username,
        $password,
        [\PDO::ATTR_ERRMODE => \PDO::ERRMODE_EXCEPTION]
    );

    $consulta = $pdo->prepare('SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = ?');
    $consulta->execute([$name]);

    if ($consulta->fetchColumn() !== false) {
        fwrite(STDERR, "[test] ABORTADO: la base {$name} ya existe.\n");
        exit(1);
    }

    $pdo->exec("CREATE DATABASE `{$name}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
} catch (\PDOException $e) {
    fwrite(STDERR, "[test] ABORTADO: no se pudo preparar la base desechable ({$e->getMessage()}).\n");
    exit(1);
}

$exitCode = 1;

try {
    putenv('APP_ENV=testing');
    putenv('DB_CONNECTION=mysql');
    putenv('DB_HOST='.$host);
    putenv('DB_PORT='.$port);
    putenv('DB_DATABASE='.$name);

    fwrite(STDOUT, "[test] Base desechable: {$name}\n");

    $migrate = escapeshellarg(PHP_BINARY).' '.escapeshellarg($backend.DIRECTORY_SEPARATOR.'artisan').' migrate --force --no-interaction';
    passthru($migrate, $migrateCode);

    if ($migrateCode !== 0) {
        $exitCode = $migrateCode;
    } else {
        $args = array_slice($argv, 1);
        $command = escapeshellarg(PHP_BINARY).' '.escapeshellarg($phpunit).' --do-not-cache-result';
        foreach ($args as $arg) {
            if ($arg !== '--') {
                $command .= ' '.escapeshellarg($arg);
            }
        }

        passthru($command, $exitCode);
    }
} finally {
    try {
        $pdo->exec("DROP DATABASE IF EXISTS `{$name}`");
        fwrite(STDOUT, "[test] Base desechable eliminada: {$name}\n");
    } catch (\PDOException $e) {
        fwrite(STDERR, "[test] ADVERTENCIA: no se pudo eliminar {$name}: {$e->getMessage()}\n");
    }
}

exit($exitCode);
