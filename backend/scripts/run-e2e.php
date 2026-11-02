<?php

declare(strict_types=1);

use Illuminate\Contracts\Console\Kernel;

// Ejecutor E2E para desarrollo local.
//
// Verifica Mailpit, crea una base desechable `ptwin_e2e_6an_*`, ejecuta
// Playwright con E2E_ALLOW_RESET y elimina la base al terminar. No usa la base
// de desarrollo ni imprime credenciales.

$backend = dirname(__DIR__);
$repo = dirname($backend);
$frontend = $repo.DIRECTORY_SEPARATOR.'frontend';
$envFile = $backend.DIRECTORY_SEPARATOR.'.env';

if (! is_file($envFile)) {
    fwrite(STDERR, "[e2e] Falta backend/.env. Copia .env.example a .env y configura la conexión local.\n");
    exit(1);
}

if (! is_dir($frontend)) {
    fwrite(STDERR, "[e2e] No se encontró frontend/.\n");
    exit(1);
}

$mailSmtp = @fsockopen('127.0.0.1', 1025, $errno, $errstr, 2);
if ($mailSmtp === false) {
    fwrite(STDERR, "[e2e] ABORTADO: Mailpit no responde en 127.0.0.1:1025.\n");
    fwrite(STDERR, "[e2e] Inicia Mailpit y vuelve a ejecutar `npm run test:e2e`.\n");
    exit(1);
}
fclose($mailSmtp);

$mailHttp = @fsockopen('127.0.0.1', 8025, $errno, $errstr, 2);
if ($mailHttp === false) {
    fwrite(STDERR, "[e2e] ABORTADO: Mailpit no responde en 127.0.0.1:8025.\n");
    exit(1);
}
fclose($mailHttp);

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
    $error = 'La conexión E2E debe ser mysql.';
} elseif (! in_array($host, ['127.0.0.1', 'localhost', '::1'], true)) {
    $error = 'La base E2E debe estar en loopback.';
} elseif ($port !== '3306') {
    $error = 'La base E2E debe usar el puerto 3306.';
}

if ($error !== null) {
    fwrite(STDERR, '[e2e] ABORTADO: '.$error."\n");
    exit(1);
}

$name = 'ptwin_e2e_6an_'.gmdate('Ymd_His').'_'.bin2hex(random_bytes(3));
if (! preg_match('/^ptwin_e2e_6an_[a-z0-9_]{6,40}$/D', $name)) {
    fwrite(STDERR, "[e2e] ABORTADO: nombre de base desechable inválido.\n");
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
        fwrite(STDERR, "[e2e] ABORTADO: la base {$name} ya existe.\n");
        exit(1);
    }

    $pdo->exec("CREATE DATABASE `{$name}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
} catch (\PDOException $e) {
    fwrite(STDERR, "[e2e] ABORTADO: no se pudo preparar la base desechable ({$e->getMessage()}).\n");
    exit(1);
}

$exitCode = 1;

try {
    putenv('E2E_ALLOW_RESET=1');
    putenv('E2E_DB_DATABASE='.$name);

    $args = array_slice($argv, 1);
    // Se invoca Playwright directamente: `npm run ... -- --grep` trataría
    // `--grep` como configuración de npm y no llegaría al runner.
    $command = 'npx playwright test';
    foreach ($args as $arg) {
        if ($arg !== '--') {
            $command .= ' '.escapeshellarg($arg);
        }
    }

    fwrite(STDOUT, "[e2e] Base desechable: {$name}\n");
    chdir($frontend);
    passthru($command, $exitCode);
} finally {
    try {
        $pdo->exec("DROP DATABASE IF EXISTS `{$name}`");
        fwrite(STDOUT, "[e2e] Base desechable eliminada: {$name}\n");
    } catch (\PDOException $e) {
        fwrite(STDERR, "[e2e] ADVERTENCIA: no se pudo eliminar {$name}: {$e->getMessage()}\n");
    }
}

exit($exitCode);
