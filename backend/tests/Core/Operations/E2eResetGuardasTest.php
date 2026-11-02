<?php

namespace Tests\Core\Operations;

use App\Console\Commands\E2eReset;
use Illuminate\Console\Command;
use Symfony\Component\Console\Input\ArrayInput;
use Symfony\Component\Console\Output\BufferedOutput;
use Tests\TestCase;

/**
 * C-06: cubre las ramas de aborto de `e2e:reset` sin tocar una base real.
 *
 * Las pruebas de guardas previas a la conexión usan un doble que registra si
 * se consultó la conexión o se intentó migrar. Las pruebas de conexión usan
 * una identidad simulada; nunca se abre una conexión MySQL ni se ejecuta
 * `migrate:fresh`. El lock se prueba con un archivo temporal fuera del repo.
 */
class E2eResetGuardasTest extends TestCase
{
    private const DB_VALIDA = 'ptwin_e2e_6an_20260101_000000';

    /** @var array<string, array{getenv: string|false, env: mixed, server: mixed, env_exists: bool, server_exists: bool}> */
    private array $envOriginal = [];

    private string $envAppOriginal = 'testing';

    private ?string $lockTemporal = null;

    private ?string $configCacheTemporal = null;

    protected function setUp(): void
    {
        parent::setUp();

        $this->envAppOriginal = (string) $this->app['env'];
        $this->app['env'] = 'e2e';

        config([
            'database.default' => 'mysql',
            'database.connections.mysql.driver' => 'mysql',
            'database.connections.mysql.host' => '127.0.0.1',
            'database.connections.mysql.port' => '3306',
            'database.connections.mysql.database' => self::DB_VALIDA,
            'database.connections.mysql.url' => null,
        ]);

        $this->setEnv('E2E_ALLOW_RESET', '1');
        $this->setEnv('E2E_DB_DATABASE', self::DB_VALIDA);
        $this->setEnv('DATABASE_URL', null);

        $this->lockTemporal = sys_get_temp_dir().DIRECTORY_SEPARATOR.'ptwin-e2e-lock-'.bin2hex(random_bytes(6)).'.lock';
        config(['e2e.reset_lock_path' => $this->lockTemporal]);
    }

    protected function tearDown(): void
    {
        foreach ($this->envOriginal as $clave => $original) {
            if ($original['env_exists']) {
                $_ENV[$clave] = $original['env'];
            } else {
                unset($_ENV[$clave]);
            }

            if ($original['server_exists']) {
                $_SERVER[$clave] = $original['server'];
            } else {
                unset($_SERVER[$clave]);
            }

            if ($original['getenv'] === false) {
                putenv($clave);
            } else {
                putenv($clave.'='.$original['getenv']);
            }
        }

        $this->app['env'] = $this->envAppOriginal;

        if ($this->lockTemporal !== null) {
            @unlink($this->lockTemporal);
        }

        if ($this->configCacheTemporal !== null) {
            @unlink($this->configCacheTemporal);
        }

        parent::tearDown();
    }

    public function test_sin_autorizacion_explicita_aborta_sin_conectar_ni_migrar(): void
    {
        $this->setEnv('E2E_ALLOW_RESET', null);

        [$exit, $salida, $comando] = $this->ejecutar($this->comandoEspia());

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('Falta autorización explícita', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
    }

    public function test_app_env_incorrecto_aborta_sin_conectar_ni_migrar(): void
    {
        $this->app['env'] = 'testing';

        [$exit, $salida, $comando] = $this->ejecutar($this->comandoEspia());

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('debe ser "e2e"', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
    }

    public function test_base_protegida_como_desechable_aborta_sin_conectar_ni_migrar(): void
    {
        $this->setEnv('E2E_DB_DATABASE', 'proyectwin_mvp');
        config(['database.connections.mysql.database' => 'proyectwin_mvp']);

        [$exit, $salida, $comando] = $this->ejecutar($this->comandoEspia());

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('solo se permite', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
    }

    public function test_nombre_desechable_invalido_aborta_sin_conectar_ni_migrar(): void
    {
        $this->setEnv('E2E_DB_DATABASE', 'ptwin_e2e_6an_AB');
        config(['database.connections.mysql.database' => 'ptwin_e2e_6an_AB']);

        [$exit, $salida, $comando] = $this->ejecutar($this->comandoEspia());

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('solo se permite', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
    }

    public function test_db_database_distinto_aborta_sin_conectar_ni_migrar(): void
    {
        $otra = 'ptwin_e2e_6an_20260101_000001';
        $this->setEnv('E2E_DB_DATABASE', self::DB_VALIDA);
        config(['database.connections.mysql.database' => $otra]);

        [$exit, $salida, $comando] = $this->ejecutar($this->comandoEspia());

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('no coincide con E2E_DB_DATABASE', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
    }

    public function test_database_url_presente_aborta_sin_conectar_ni_migrar(): void
    {
        $this->setEnv('DATABASE_URL', 'mysql://usuario:clave@127.0.0.1:3306/otra');

        [$exit, $salida, $comando] = $this->ejecutar($this->comandoEspia());

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('DATABASE_URL no está permitido', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
    }

    public function test_configuracion_cacheada_aborta_sin_conectar_ni_migrar(): void
    {
        $archivo = tempnam(sys_get_temp_dir(), 'ptwin-config-');

        if ($archivo === false) {
            $this->fail('No se pudo crear el archivo temporal de configuración.');
        }

        file_put_contents($archivo, "<?php\nreturn [];\n");
        $this->configCacheTemporal = $archivo;

        // En Windows la ruta absoluta empieza por la unidad (C:); Laravel solo
        // reconoce prefijos absolutos configurados.
        $this->app->addAbsoluteCachePathPrefix(substr(sys_get_temp_dir(), 0, 2));
        $this->setEnv('APP_CONFIG_CACHE', $archivo);

        [$exit, $salida, $comando] = $this->ejecutar($this->comandoEspia());

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('configuración cacheada', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
    }

    public function test_host_no_loopback_aborta_sin_conectar_ni_migrar(): void
    {
        config(['database.connections.mysql.host' => '192.168.1.10']);

        [$exit, $salida, $comando] = $this->ejecutar($this->comandoEspia());

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('solo se permite loopback', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
    }

    public function test_conexion_efectiva_con_otra_base_aborta_sin_migrar_y_libera_lock(): void
    {
        [$exit, $salida, $comando] = $this->ejecutar(
            $this->comandoEspia((object) ['base' => 'otra_base', 'puerto' => '3306'])
        );

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('conexión efectiva selecciona una base distinta', $salida);
        $this->assertTrue($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
        $this->assertLockLibre();
    }

    public function test_conexion_efectiva_con_otro_puerto_aborta_sin_migrar(): void
    {
        [$exit, $salida, $comando] = $this->ejecutar(
            $this->comandoEspia((object) ['base' => self::DB_VALIDA, 'puerto' => '3307'])
        );

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('puerto real de la conexión no coincide', $salida);
        $this->assertTrue($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
        $this->assertLockLibre();
    }

    public function test_lock_ocupado_aborta_sin_conectar_ni_migrar(): void
    {
        [$exit, $salida, $comando] = $this->ejecutar(
            $this->comandoEspia(null, null, false)
        );

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('ya hay un reseed E2E en curso', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
    }

    public function test_fallo_de_migrate_fresh_y_seed_se_propaga_y_libera_lock(): void
    {
        [$exit, $salida, $comando] = $this->ejecutar(
            $this->comandoEspia(
                (object) ['base' => self::DB_VALIDA, 'puerto' => '3306'],
                Command::FAILURE
            )
        );

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('migrate:fresh devolvió 1', $salida);
        $this->assertTrue($comando->migrateLlamado);
        $this->assertLockLibre();
    }

    public function test_reseed_exitoso_reporta_completado_y_libera_lock(): void
    {
        [$exit, $salida, $comando] = $this->ejecutar(
            $this->comandoEspia(
                (object) ['base' => self::DB_VALIDA, 'puerto' => '3306'],
                Command::SUCCESS
            )
        );

        $this->assertSame(Command::SUCCESS, $exit, $salida);
        $this->assertStringContainsString('Reseed E2E completado', $salida);
        $this->assertTrue($comando->migrateLlamado);
        $this->assertLockLibre();
    }

    public function test_check_only_no_adquiere_lock_ni_migra(): void
    {
        [$exit, $salida, $comando] = $this->ejecutar(
            $this->comandoEspia((object) ['base' => self::DB_VALIDA, 'puerto' => '3306']),
            ['--check-only' => true]
        );

        $this->assertSame(Command::SUCCESS, $exit, $salida);
        $this->assertStringContainsString('Verificación E2E OK', $salida);
        $this->assertTrue($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
        $this->assertFileDoesNotExist((string) $this->lockTemporal);
    }

    public function test_driver_distinto_de_mysql_aborta_sin_conectar_ni_migrar(): void
    {
        config(['database.connections.mysql.driver' => 'sqlite']);

        [$exit, $salida, $comando] = $this->ejecutar($this->comandoEspia());

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('E2E solo admite mysql', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
    }

    public function test_excepcion_al_verificar_conexion_aborta_sin_migrar_y_libera_lock(): void
    {
        [$exit, $salida, $comando] = $this->ejecutar(
            $this->comandoEspia(null, null, true, 'exception')
        );

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('No se pudo verificar la conexión efectiva', $salida);
        $this->assertTrue($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
        $this->assertLockLibre();
    }

    public function test_identidad_de_conexion_nula_aborta_sin_migrar_y_libera_lock(): void
    {
        [$exit, $salida, $comando] = $this->ejecutar(
            $this->comandoEspia(null, null, true, 'null')
        );

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('no devolvió identidad de base', $salida);
        $this->assertTrue($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
        $this->assertLockLibre();
    }

    public function test_check_only_con_conexion_incorrecta_aborta_sin_lock_ni_migrar(): void
    {
        [$exit, $salida, $comando] = $this->ejecutar(
            $this->comandoEspia((object) ['base' => 'otra_base', 'puerto' => '3306']),
            ['--check-only' => true]
        );

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('conexión efectiva selecciona una base distinta', $salida);
        $this->assertTrue($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
        $this->assertFileDoesNotExist((string) $this->lockTemporal);
    }

    public function test_base_protegida_por_defecto_aborta_sin_conectar_ni_migrar(): void
    {
        $this->setEnv('E2E_DB_DATABASE', null);
        config([
            'database.connections.mysql.database' => 'proyectwin_e2e',
            'database.connections.mysql.host' => 'localhost',
        ]);

        [$exit, $salida, $comando] = $this->ejecutar(
            $this->comandoEspia((object) ['base' => 'proyectwin_e2e', 'puerto' => '3306']),
            ['--check-only' => true]
        );

        $this->assertSame(Command::FAILURE, $exit, $salida);
        $this->assertStringContainsString('E2E_DB_DATABASE es obligatorio', $salida);
        $this->assertFalse($comando->conexionConsultada);
        $this->assertFalse($comando->migrateLlamado);
        $this->assertFileDoesNotExist((string) $this->lockTemporal);
    }

    /**
     * @return array{0: int, 1: string, 2: E2eReset}
     */
    private function ejecutar(E2eReset $comando, array $parametros = []): array
    {
        $comando->setLaravel($this->app);

        $input = new ArrayInput($parametros, $comando->getDefinition());
        $output = new BufferedOutput();
        $exit = $comando->run($input, $output);

        return [$exit, $output->fetch(), $comando];
    }

    private function comandoEspia(
        ?object $fila = null,
        ?int $resultadoMigrate = null,
        bool $lockDisponible = true,
        ?string $modoConsulta = null
    ): E2eReset {
        return new class($fila, $resultadoMigrate, $lockDisponible, $modoConsulta) extends E2eReset {
            public bool $conexionConsultada = false;

            public bool $migrateLlamado = false;

            public function __construct(
                private ?object $fila,
                private ?int $resultadoMigrate,
                private bool $lockDisponible,
                private ?string $modoConsulta
            ) {
                parent::__construct();
            }

            protected function identidadConexion(): ?object
            {
                $this->conexionConsultada = true;

                if ($this->modoConsulta === 'exception') {
                    throw new \RuntimeException('fallo de conexión simulado');
                }

                if ($this->modoConsulta === 'null') {
                    return null;
                }

                return $this->fila ?? (object) [
                    'base' => 'ptwin_e2e_6an_20260101_000000',
                    'puerto' => '3306',
                ];
            }

            protected function ejecutarMigrateFresh(): int
            {
                $this->migrateLlamado = true;

                return $this->resultadoMigrate ?? parent::ejecutarMigrateFresh();
            }

            protected function adquirirLock(string $ruta): mixed
            {
                if (! $this->lockDisponible) {
                    return null;
                }

                return parent::adquirirLock($ruta);
            }
        };
    }

    private function assertLockLibre(): void
    {
        $ruta = (string) $this->lockTemporal;
        $handle = fopen($ruta, 'c');

        $this->assertIsResource($handle, 'No se pudo abrir el lock temporal.');

        $this->assertTrue(
            flock($handle, LOCK_EX | LOCK_NB),
            'El lock no se liberó después del comando.'
        );

        flock($handle, LOCK_UN);
        fclose($handle);
    }

    private function setEnv(string $clave, ?string $valor): void
    {
        if (! array_key_exists($clave, $this->envOriginal)) {
            $this->envOriginal[$clave] = [
                'getenv' => getenv($clave),
                'env' => $_ENV[$clave] ?? null,
                'server' => $_SERVER[$clave] ?? null,
                'env_exists' => array_key_exists($clave, $_ENV),
                'server_exists' => array_key_exists($clave, $_SERVER),
            ];
        }

        if ($valor === null) {
            putenv($clave);
            unset($_ENV[$clave], $_SERVER[$clave]);

            return;
        }

        putenv($clave.'='.$valor);
        $_ENV[$clave] = $valor;
        $_SERVER[$clave] = $valor;
    }
}
