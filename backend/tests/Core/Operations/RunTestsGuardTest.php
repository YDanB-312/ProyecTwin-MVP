<?php

declare(strict_types=1);

namespace Tests\Core\Operations;

use App\Support\Pruebas\ReglasBaseDatos;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

class RunTestsGuardTest extends TestCase
{
    public function test_rechaza_cache_por_defecto(): void
    {
        $backend = $this->directorioTemporal();
        $cache = ReglasBaseDatos::rutaCachePorDefecto($backend);

        try {
            mkdir(dirname($cache), 0777, true);
            file_put_contents($cache, "<?php\nreturn [];\n");

            $this->assertSame($cache, ReglasBaseDatos::rutaCacheEfectiva($backend));
        } finally {
            @unlink($cache);
            @rmdir(dirname($cache));
            @rmdir($backend.DIRECTORY_SEPARATOR.'bootstrap');
            @rmdir($backend);
        }
    }

    public function test_rechaza_app_config_cache_alternativo(): void
    {
        $cache = tempnam(sys_get_temp_dir(), 'ptwin-config-');

        if ($cache === false) {
            $this->fail('No se pudo crear el archivo temporal.');
        }

        try {
            file_put_contents($cache, "<?php\nreturn [];\n");

            $this->assertSame(
                $cache,
                ReglasBaseDatos::rutaCacheEfectiva(sys_get_temp_dir(), $cache)
            );
        } finally {
            @unlink($cache);
        }
    }

    public function test_rechaza_database_url_en_entorno(): void
    {
        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('DATABASE_URL y DB_URL no están permitidos');

        ReglasBaseDatos::validarConfiguracion(
            'local',
            'mysql',
            $this->configuracion(),
            'mysql://usuario:clave@127.0.0.1:3306/otra',
            null,
        );
    }

    public function test_rechaza_db_url_en_entorno(): void
    {
        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('DATABASE_URL y DB_URL no están permitidos');

        ReglasBaseDatos::validarConfiguracion(
            'local',
            'mysql',
            $this->configuracion(),
            null,
            'mysql://usuario:clave@127.0.0.1:3306/otra',
        );
    }

    public function test_rechaza_url_en_configuracion(): void
    {
        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('DATABASE_URL y DB_URL no están permitidos');

        ReglasBaseDatos::validarConfiguracion(
            'local',
            'mysql',
            $this->configuracion(['url' => 'mysql://usuario:clave@127.0.0.1:3306/otra']),
            null,
            null,
        );
    }

    public function test_rechaza_entorno_no_permitido(): void
    {
        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('solo se permite local o testing');

        ReglasBaseDatos::validarConfiguracion(
            'production',
            'mysql',
            $this->configuracion(),
            null,
            null,
        );
    }

    public function test_acepta_local_y_testing(): void
    {
        foreach (['local', 'testing'] as $entorno) {
            ReglasBaseDatos::validarConfiguracion($entorno, 'mysql', $this->configuracion(), null, null);
        }

        $this->addToAssertionCount(2);
    }

    public function test_rechaza_driver_no_mysql(): void
    {
        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('driver de la conexión debe ser mysql');

        ReglasBaseDatos::validarConfiguracion(
            'local',
            'mysql',
            $this->configuracion(['driver' => 'sqlite']),
            null,
            null,
        );
    }

    public function test_rechaza_conexion_no_mysql(): void
    {
        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('La conexión por defecto debe ser "mysql"');

        ReglasBaseDatos::validarConfiguracion(
            'local',
            'sqlite',
            $this->configuracion(),
            null,
            null,
        );
    }

    public function test_rechaza_host_no_loopback(): void
    {
        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('debe estar en loopback');

        ReglasBaseDatos::validarConfiguracion(
            'local',
            'mysql',
            $this->configuracion(['host' => '192.168.1.10']),
            null,
            null,
        );
    }

    public function test_rechaza_puerto_distinto(): void
    {
        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('debe ser 3306');

        ReglasBaseDatos::validarConfiguracion(
            'local',
            'mysql',
            $this->configuracion(['port' => '3307']),
            null,
            null,
        );
    }

    public function test_rechaza_socket_no_vacio(): void
    {
        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('DB_SOCKET debe estar vacío');

        ReglasBaseDatos::validarConfiguracion(
            'local',
            'mysql',
            $this->configuracion(['unix_socket' => '/tmp/mysql.sock']),
            null,
            null,
        );
    }

    #[DataProvider('basesProtegidas')]
    public function test_rechaza_base_protegida(string $base): void
    {
        $this->assertTrue(ReglasBaseDatos::esBaseProtegida($base));
        $this->assertFalse(ReglasBaseDatos::esBasePermitida($base));

        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('está protegida');

        ReglasBaseDatos::validarBaseDestino($base);
    }

    public static function basesProtegidas(): array
    {
        return [
            ['proyectwin_mvp'],
            ['proyectwin_test'],
            ['proyectwin_e2e'],
        ];
    }

    public function test_rechaza_nombre_fuera_de_prefijo(): void
    {
        foreach (['otra_base', 'ptwin_otra', 'ptwin_test_', 'proyectwin_rollback_12345678'] as $base) {
            $this->assertFalse(ReglasBaseDatos::esBasePermitida($base), $base);

            try {
                ReglasBaseDatos::validarBaseDestino($base);
                $this->fail('Se aceptó una base no permitida: '.$base);
            } catch (\RuntimeException $e) {
                $this->assertStringContainsString('no es válida', $e->getMessage());
            }
        }

        $this->addToAssertionCount(4);
    }

    public function test_acepta_y_genera_nombres_desechables(): void
    {
        for ($i = 0; $i < 20; $i++) {
            $base = ReglasBaseDatos::generarNombreDesechable();

            $this->assertTrue(ReglasBaseDatos::esBasePermitida($base), $base);
            $this->assertStringStartsWith(ReglasBaseDatos::PREFIJO, $base);
        }
    }

    public function test_identidad_de_conexion_debe_coincidir(): void
    {
        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('La conexión efectiva no coincide');

        ReglasBaseDatos::validarIdentidadConexion(
            (object) ['base' => 'proyectwin_mvp', 'puerto' => '3306'],
            'ptwin_test_20260101_000000_abcdef',
            '3306',
        );
    }

    public function test_identidad_de_conexion_correcta(): void
    {
        ReglasBaseDatos::validarIdentidadConexion(
            ['base' => 'ptwin_test_20260101_000000_abcdef', 'puerto' => '3306'],
            'ptwin_test_20260101_000000_abcdef',
            '3306',
        );

        $this->addToAssertionCount(1);
    }

    public function test_la_base_del_env_no_es_el_destino(): void
    {
        // El .env local puede apuntar a la base de desarrollo; el runner no la
        // usa como destino. La base protegida solo se rechaza como destino.
        ReglasBaseDatos::validarConfiguracion(
            'local',
            'mysql',
            $this->configuracion(['database' => 'proyectwin_mvp']),
            null,
            null,
        );

        $this->expectException(\RuntimeException::class);
        $this->expectExceptionMessage('está protegida');

        ReglasBaseDatos::validarBaseDestino('proyectwin_mvp');
    }

    private function configuracion(array $overrides = []): array
    {
        return array_merge([
            'driver' => 'mysql',
            'host' => '127.0.0.1',
            'port' => '3306',
            'unix_socket' => '',
            'url' => null,
            'database' => 'proyectwin_mvp',
        ], $overrides);
    }

    private function directorioTemporal(): string
    {
        $directorio = sys_get_temp_dir().DIRECTORY_SEPARATOR.'ptwin-test-'.bin2hex(random_bytes(6));

        if (! mkdir($directorio, 0777, true)) {
            $this->fail('No se pudo crear el directorio temporal.');
        }

        return $directorio;
    }
}
