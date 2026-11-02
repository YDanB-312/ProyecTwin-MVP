<?php

declare(strict_types=1);

namespace App\Support\Pruebas;

use Illuminate\Foundation\Application;

final class ReglasBaseDatos
{
    public const PREFIJO = 'ptwin_test_';

    public const PATRON = '/^ptwin_test_[a-z0-9_]{6,50}$/D';

    public const HOSTS_LOOPBACK = ['127.0.0.1', 'localhost', '::1'];

    public const BASES_PROTEGIDAS = [
        'proyectwin_mvp',
        'proyectwin_test',
        'proyectwin_e2e',
    ];

    public static function rutaCachePorDefecto(string $backend): string
    {
        return $backend.DIRECTORY_SEPARATOR.'bootstrap'.DIRECTORY_SEPARATOR.'cache'.DIRECTORY_SEPARATOR.'config.php';
    }

    public static function rutaCacheEfectiva(string $backend, ?string $rutaAplicacion = null, ?string $rutaEntorno = null): ?string
    {
        $rutas = [self::rutaCachePorDefecto($backend)];

        foreach ([$rutaAplicacion, $rutaEntorno] as $ruta) {
            if (is_string($ruta) && trim($ruta) !== '') {
                $rutas[] = $ruta;
            }
        }

        foreach (array_unique($rutas) as $ruta) {
            if (is_file($ruta)) {
                return $ruta;
            }
        }

        return null;
    }

    public static function rutaCacheDeAplicacion(Application $app): ?string
    {
        $ruta = $app->getCachedConfigPath();

        return is_string($ruta) && trim($ruta) !== '' ? $ruta : null;
    }

    public static function esBaseProtegida(string $base): bool
    {
        return in_array($base, self::BASES_PROTEGIDAS, true);
    }

    public static function esBasePermitida(string $base): bool
    {
        return ! self::esBaseProtegida($base) && preg_match(self::PATRON, $base) === 1;
    }

    public static function generarNombreDesechable(): string
    {
        $nombre = self::PREFIJO.gmdate('Ymd_His').'_'.bin2hex(random_bytes(6));

        if (! self::esBasePermitida($nombre)) {
            throw new \RuntimeException('No se pudo generar un nombre de base desechable válido.');
        }

        return $nombre;
    }

    public static function validarBaseDestino(string $base): void
    {
        if (self::esBaseProtegida($base)) {
            throw new \RuntimeException(
                'La base "'.$base.'" está protegida. No se puede usar como destino de pruebas.'
            );
        }

        if (preg_match(self::PATRON, $base) !== 1) {
            throw new \RuntimeException(
                'La base "'.$base.'" no es válida: solo se admite el prefijo '.self::PREFIJO.'.'
            );
        }
    }

    /**
     * Valida la conexión configurada que se usará para crear la base y migrar.
     *
     * La base configurada en el .env no es el destino: el runner siempre
     * genera una base desechable. Por eso este método no valida "database"
     * como nombre de destino; solo valida el servidor y la ausencia de URLs
     * alternativas.
     */
    public static function validarConfiguracion(
        string $entorno,
        string $conexion,
        array $config,
        ?string $databaseUrl,
        ?string $dbUrl,
    ): void {
        $entorno = strtolower(trim($entorno));

        if (! in_array($entorno, ['local', 'testing'], true)) {
            throw new \RuntimeException(
                'APP_ENV="'.$entorno.'"; solo se permite local o testing para ejecutar el runner.'
            );
        }

        if ($conexion !== 'mysql') {
            throw new \RuntimeException(
                'La conexión por defecto debe ser "mysql"; se recibió "'.$conexion.'".'
            );
        }

        $driver = (string) ($config['driver'] ?? '');

        if ($driver !== 'mysql') {
            throw new \RuntimeException(
                'El driver de la conexión debe ser mysql; se recibió "'.$driver.'".'
            );
        }

        $host = (string) ($config['host'] ?? '');

        if (! in_array($host, self::HOSTS_LOOPBACK, true)) {
            throw new \RuntimeException(
                'El host de la base de pruebas debe estar en loopback; se recibió "'.$host.'".'
            );
        }

        $puerto = (string) ($config['port'] ?? '');

        if ($puerto !== '3306') {
            throw new \RuntimeException(
                'El puerto de la base de pruebas debe ser 3306; se recibió "'.$puerto.'".'
            );
        }

        $socket = (string) ($config['unix_socket'] ?? '');

        if (trim($socket) !== '') {
            throw new \RuntimeException(
                'DB_SOCKET debe estar vacío para pruebas; se recibió "'.$socket.'".'
            );
        }

        $urlConfig = $config['url'] ?? null;

        if (self::textoNoVacio($urlConfig)
            || self::textoNoVacio($databaseUrl)
            || self::textoNoVacio($dbUrl)) {
            throw new \RuntimeException(
                'DATABASE_URL y DB_URL no están permitidos; usa DB_HOST/DB_PORT/DB_DATABASE explícitos.'
            );
        }
    }

    public static function configuracionValidada(Application $app): array
    {
        $conexion = (string) config('database.default');
        $config = (array) config('database.connections.mysql');

        self::validarConfiguracion(
            $app->environment(),
            $conexion,
            $config,
            env('DATABASE_URL'),
            env('DB_URL'),
        );

        return $config;
    }

    public static function validarIdentidadConexion(object|array $fila, string $base, string $puerto): void
    {
        $datos = is_array($fila) ? $fila : (array) $fila;
        $baseReal = (string) ($datos['base'] ?? '');
        $puertoReal = (string) ($datos['puerto'] ?? '');

        if ($baseReal !== $base || $puertoReal !== $puerto) {
            throw new \RuntimeException(
                'La conexión efectiva no coincide con la base autorizada: '.
                'esperada="'.$base.'" puerto="'.$puerto.'", '.
                'real="'.$baseReal.'" puerto="'.$puertoReal.'".'
            );
        }
    }

    private static function textoNoVacio(mixed $valor): bool
    {
        return is_string($valor) && trim($valor) !== '';
    }
}
