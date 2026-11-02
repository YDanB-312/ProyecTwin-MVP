<?php

declare(strict_types=1);

namespace Tests\Core\Operations;

use App\Support\Pruebas\EjecutorPhpUnit;
use App\Support\Pruebas\ReglasBaseDatos;
use PHPUnit\Framework\TestCase;

class RunTestsExecutorTest extends TestCase
{
    public function test_cache_existente_aborta_antes_de_crear_la_base(): void
    {
        $backend = $this->directorioTemporal();
        $cache = ReglasBaseDatos::rutaCachePorDefecto($backend);
        mkdir(dirname($cache), 0777, true);
        file_put_contents($cache, "<?php\nreturn [];\n");

        try {
            $ejecutor = $this->ejecutor($backend);

            $this->assertSame(1, $ejecutor->ejecutar());
            $this->assertEmpty($this->eventosConPrefijo($ejecutor->eventos, 'crear'));
            $this->assertStringContainsString('configuración cacheada', implode(' ', $ejecutor->errores));
        } finally {
            @unlink($cache);
            @rmdir(dirname($cache));
            @rmdir($backend.DIRECTORY_SEPARATOR.'bootstrap');
            @rmdir($backend);
        }
    }

    public function test_fallo_de_conexion_no_migra_ni_elimina(): void
    {
        $ejecutor = $this->ejecutor();
        $ejecutor->fallaConexion = true;

        $this->assertSame(1, $ejecutor->ejecutar());
        $this->assertContains('conectar', $ejecutor->eventos);
        $this->assertEmpty($this->eventosConPrefijo($ejecutor->eventos, 'migrar'));
        $this->assertEmpty($this->eventosConPrefijo($ejecutor->eventos, 'eliminar'));
    }

    public function test_fallo_de_verificacion_elimina_la_base(): void
    {
        $ejecutor = $this->ejecutor();
        $ejecutor->fallaVerificacion = true;

        $this->assertSame(1, $ejecutor->ejecutar());
        $this->assertNotEmpty($this->eventosConPrefijo($ejecutor->eventos, 'crear'));
        $this->assertNotEmpty($this->eventosConPrefijo($ejecutor->eventos, 'verificar'));
        $this->assertNotEmpty($this->eventosConPrefijo($ejecutor->eventos, 'eliminar'));
        $this->assertEmpty($this->eventosConPrefijo($ejecutor->eventos, 'migrar'));
    }

    public function test_fallo_de_migracion_elimina_la_base(): void
    {
        $ejecutor = $this->ejecutor();
        $ejecutor->fallaMigracion = true;

        $this->assertSame(1, $ejecutor->ejecutar());
        $this->assertNotEmpty($this->eventosConPrefijo($ejecutor->eventos, 'migrar'));
        $this->assertNotEmpty($this->eventosConPrefijo($ejecutor->eventos, 'eliminar'));
        $this->assertEmpty($this->eventosConPrefijo($ejecutor->eventos, 'phpunit'));
    }

    public function test_fallo_de_phpunit_elimina_la_base(): void
    {
        $ejecutor = $this->ejecutor();
        $ejecutor->falloPhpUnit = 1;

        $this->assertSame(1, $ejecutor->ejecutar());
        $this->assertNotEmpty($this->eventosConPrefijo($ejecutor->eventos, 'phpunit'));
        $this->assertNotEmpty($this->eventosConPrefijo($ejecutor->eventos, 'eliminar'));
    }

    public function test_fallo_de_limpieza_se_reporta(): void
    {
        $ejecutor = $this->ejecutor();
        $ejecutor->fallaLimpieza = true;

        $this->assertSame(1, $ejecutor->ejecutar());
        $this->assertStringContainsString('no se pudo eliminar', implode(' ', $ejecutor->errores));
    }

    public function test_nunca_usa_una_base_protegida(): void
    {
        $ejecutor = $this->ejecutor();

        $this->assertSame(0, $ejecutor->ejecutar());

        $nombres = [];

        foreach ($ejecutor->eventos as $evento) {
            if (str_contains($evento, ':')) {
                $nombres[] = explode(':', $evento, 2)[1];
            }
        }

        $this->assertNotEmpty($nombres);

        foreach ($nombres as $nombre) {
            $this->assertTrue(ReglasBaseDatos::esBasePermitida($nombre), $nombre);
            $this->assertFalse(ReglasBaseDatos::esBaseProtegida($nombre), $nombre);
        }
    }

    public function test_el_hijo_recibe_exactamente_la_base_generada(): void
    {
        $ejecutor = $this->ejecutor();
        $nombre = 'ptwin_test_20260101_000000_abcdef';
        $entorno = $ejecutor->entorno($nombre);

        $this->assertSame($nombre, $entorno['DB_DATABASE']);
        $this->assertSame($nombre, $entorno['PTWIN_TEST_DATABASE']);
        $this->assertSame('testing', $entorno['APP_ENV']);
        $this->assertFileDoesNotExist($entorno['APP_CONFIG_CACHE']);
        $this->assertArrayNotHasKey('DATABASE_URL', $entorno);
        $this->assertArrayNotHasKey('DB_URL', $entorno);
    }

    public function test_flujo_usa_la_misma_base_en_todas_las_etapas(): void
    {
        $ejecutor = $this->ejecutor();

        $this->assertSame(0, $ejecutor->ejecutar());

        $bases = [];

        foreach ($ejecutor->eventos as $evento) {
            if (str_contains($evento, ':')) {
                $bases[] = explode(':', $evento, 2)[1];
            }
        }

        $this->assertCount(6, $bases, implode(', ', $ejecutor->eventos));
        $this->assertCount(1, array_unique($bases));
        $this->assertTrue(ReglasBaseDatos::esBasePermitida($bases[0]));
    }

    private function eventosConPrefijo(array $eventos, string $prefijo): array
    {
        return array_values(array_filter(
            $eventos,
            static fn (string $evento): bool => str_starts_with($evento, $prefijo.':'),
        ));
    }

    private function ejecutor(string $backend = ''): object
    {
        $backend = $backend !== '' ? $backend : sys_get_temp_dir();

        return new class($backend, [
            'driver' => 'mysql',
            'host' => '127.0.0.1',
            'port' => '3306',
            'username' => 'usuario',
            'password' => 'clave',
        ], [], false) extends EjecutorPhpUnit {
            public array $eventos = [];

            public array $errores = [];

            public bool $fallaConexion = false;

            public bool $fallaVerificacion = false;

            public bool $fallaMigracion = false;

            public int $falloPhpUnit = 0;

            public bool $fallaLimpieza = false;

            protected function crearConexion(): object
            {
                $this->eventos[] = 'conectar';

                if ($this->fallaConexion) {
                    throw new \RuntimeException('fallo de conexión simulado');
                }

                return new \stdClass();
            }

            protected function crearBase(object $pdo, string $nombre): void
            {
                $this->eventos[] = 'crear:'.$nombre;
            }

            protected function configurarConexion(string $nombre): void
            {
                $this->eventos[] = 'configurar:'.$nombre;
            }

            protected function verificarConexion(string $nombre): void
            {
                $this->eventos[] = 'verificar:'.$nombre;

                if ($this->fallaVerificacion) {
                    throw new \RuntimeException('identidad de conexión simulada incorrecta');
                }
            }

            protected function migrar(string $nombre): int
            {
                $this->eventos[] = 'migrar:'.$nombre;

                if ($this->fallaMigracion) {
                    throw new \RuntimeException('fallo de migración simulado');
                }

                return 0;
            }

            protected function ejecutarPhpUnit(string $nombre, array $argumentos): int
            {
                $this->eventos[] = 'phpunit:'.$nombre;

                return $this->falloPhpUnit;
            }

            protected function eliminarBase(object $pdo, string $nombre): void
            {
                $this->eventos[] = 'eliminar:'.$nombre;

                if ($this->fallaLimpieza) {
                    throw new \RuntimeException('fallo de limpieza simulado');
                }
            }

            public function entorno(string $nombre): array
            {
                return $this->construirEntornoPhpUnit($nombre);
            }

            protected function info(string $mensaje): void
            {
                //
            }

            protected function error(string $mensaje): void
            {
                $this->errores[] = $mensaje;
            }
        };
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
