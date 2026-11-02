<?php

declare(strict_types=1);

namespace App\Support\Pruebas;

use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\DB;
use PDO;
use Symfony\Component\Process\Process;
use Throwable;

class EjecutorPhpUnit
{
    private bool $baseCreada = false;

    private bool $baseEliminada = false;

    public function __construct(
        private readonly string $backend,
        private readonly array $configuracion,
        private readonly array $argumentos = [],
        private readonly bool $registrarLimpiezaPorApagado = true,
    ) {}

    public function ejecutar(): int
    {
        $cache = ReglasBaseDatos::rutaCacheEfectiva(
            $this->backend,
            null,
            is_string(getenv('APP_CONFIG_CACHE')) ? getenv('APP_CONFIG_CACHE') : null,
        );

        if ($cache !== null) {
            $this->error('[test] ABORTADO: existe configuración cacheada en '.$cache.'. Ejecuta `php artisan config:clear`.');
            return 1;
        }

        try {
            $nombre = ReglasBaseDatos::generarNombreDesechable();
            ReglasBaseDatos::validarBaseDestino($nombre);
        } catch (Throwable $e) {
            $this->error('[test] ABORTADO: '.$e->getMessage());
            return 1;
        }

        $pdo = null;
        $codigo = 1;

        try {
            $pdo = $this->crearConexion();
            $this->crearBase($pdo, $nombre);
            $this->baseCreada = true;

            if ($this->registrarLimpiezaPorApagado) {
                $this->registrarLimpiezaPorApagado($pdo, $nombre);
            }

            $this->configurarConexion($nombre);
            $this->verificarConexion($nombre);

            $this->info('[test] Base desechable: '.$nombre);

            $migracion = $this->migrar($nombre);

            if ($migracion !== 0) {
                $this->error('[test] La migración falló con código '.$migracion.'.');
                $codigo = 1;
            } else {
                $codigo = $this->ejecutarPhpUnit($nombre, $this->argumentos);
            }
        } catch (Throwable $e) {
            $this->error('[test] ABORTADO: '.$e->getMessage());
            $codigo = 1;
        } finally {
            if ($this->baseCreada && ! $this->baseEliminada) {
                try {
                    if ($pdo === null) {
                        throw new \RuntimeException('No hay conexión administrativa para eliminar la base.');
                    }

                    $this->eliminarBase($pdo, $nombre);
                    $this->baseEliminada = true;
                    $this->info('[test] Base desechable eliminada: '.$nombre);
                } catch (Throwable $e) {
                    $this->error('[test] ERROR: no se pudo eliminar la base '.$nombre.': '.$e->getMessage());
                    $codigo = 1;
                }
            }
        }

        return $codigo;
    }

    protected function crearConexion(): object
    {
        $host = (string) ($this->configuracion['host'] ?? '');
        $puerto = (string) ($this->configuracion['port'] ?? '');
        $usuario = (string) ($this->configuracion['username'] ?? '');
        $password = (string) ($this->configuracion['password'] ?? '');

        return new PDO(
            "mysql:host={$host};port={$puerto};charset=utf8mb4",
            $usuario,
            $password,
            [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION],
        );
    }

    protected function crearBase(object $pdo, string $nombre): void
    {
        ReglasBaseDatos::validarBaseDestino($nombre);

        $consulta = $pdo->prepare('SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = ?');
        $consulta->execute([$nombre]);

        if ($consulta->fetchColumn() !== false) {
            throw new \RuntimeException('La base '.$nombre.' ya existe.');
        }

        $pdo->exec("CREATE DATABASE `{$nombre}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    protected function configurarConexion(string $nombre): void
    {
        ReglasBaseDatos::validarBaseDestino($nombre);

        config([
            'database.default' => 'mysql',
            'database.connections.mysql.database' => $nombre,
            'database.connections.mysql.url' => null,
            'database.connections.mysql.unix_socket' => '',
        ]);

        DB::purge('mysql');
    }

    protected function verificarConexion(string $nombre): void
    {
        $fila = DB::connection('mysql')->selectOne('select database() as base, @@port as puerto');

        ReglasBaseDatos::validarIdentidadConexion(
            $fila,
            $nombre,
            (string) ($this->configuracion['port'] ?? '3306'),
        );
    }

    protected function migrar(string $nombre): int
    {
        $codigo = (int) Artisan::call('migrate', ['--force' => true]);
        $salida = Artisan::output();

        if ($salida !== '') {
            $this->info(rtrim($salida));
        }

        return $codigo;
    }

    protected function ejecutarPhpUnit(string $nombre, array $argumentos): int
    {
        $phpunit = $this->backend.DIRECTORY_SEPARATOR.'vendor'.DIRECTORY_SEPARATOR.'bin'.DIRECTORY_SEPARATOR.'phpunit';

        if (! is_file($phpunit)) {
            throw new \RuntimeException('No se encontró vendor/bin/phpunit.');
        }

        $comando = array_merge(
            [PHP_BINARY, $phpunit, '--do-not-cache-result'],
            array_values(array_filter($argumentos, static fn (string $arg): bool => $arg !== '--')),
        );

        $proceso = new Process($comando, $this->backend, $this->construirEntornoPhpUnit($nombre));
        $proceso->setTimeout(null);
        $proceso->run(function (string $tipo, string $buffer): void {
            fwrite($tipo === Process::ERR ? STDERR : STDOUT, $buffer);
        });

        return $proceso->getExitCode() ?? 1;
    }

    protected function construirEntornoPhpUnit(string $nombre): array
    {
        ReglasBaseDatos::validarBaseDestino($nombre);

        $configCache = tempnam(sys_get_temp_dir(), 'ptwin-test-config-');

        if ($configCache === false) {
            throw new \RuntimeException('No se pudo preparar una ruta temporal para APP_CONFIG_CACHE.');
        }

        @unlink($configCache);

        $entorno = getenv();

        if (! is_array($entorno)) {
            $entorno = [];
        }

        $entorno['APP_ENV'] = 'testing';
        $entorno['APP_CONFIG_CACHE'] = $configCache;
        $entorno['PTWIN_TEST_DATABASE'] = $nombre;
        $entorno['DB_CONNECTION'] = 'mysql';
        $entorno['DB_HOST'] = (string) ($this->configuracion['host'] ?? '');
        $entorno['DB_PORT'] = (string) ($this->configuracion['port'] ?? '');
        $entorno['DB_DATABASE'] = $nombre;
        $entorno['DB_USERNAME'] = (string) ($this->configuracion['username'] ?? '');
        $entorno['DB_PASSWORD'] = (string) ($this->configuracion['password'] ?? '');
        $entorno['DB_SOCKET'] = '';

        unset($entorno['DATABASE_URL'], $entorno['DB_URL']);

        return $entorno;
    }

    protected function eliminarBase(object $pdo, string $nombre): void
    {
        ReglasBaseDatos::validarBaseDestino($nombre);

        DB::disconnect('mysql');
        $pdo->exec("DROP DATABASE IF EXISTS `{$nombre}`");
    }

    protected function registrarLimpiezaPorApagado(object $pdo, string $nombre): void
    {
        register_shutdown_function(function () use ($pdo, $nombre): void {
            if (! $this->baseCreada || $this->baseEliminada) {
                return;
            }

            try {
                $this->eliminarBase($pdo, $nombre);
                $this->baseEliminada = true;
                $this->info('[test] Base desechable eliminada durante el cierre: '.$nombre);
            } catch (Throwable $e) {
                $this->error('[test] ERROR: no se pudo eliminar la base '.$nombre.' durante el cierre: '.$e->getMessage());
            }
        });
    }

    protected function info(string $mensaje): void
    {
        fwrite(STDOUT, $mensaje.PHP_EOL);
    }

    protected function error(string $mensaje): void
    {
        fwrite(STDERR, $mensaje.PHP_EOL);
    }
}
