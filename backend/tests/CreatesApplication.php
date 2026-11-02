<?php

namespace Tests;

use Illuminate\Contracts\Console\Kernel;
use Illuminate\Foundation\Application;

trait CreatesApplication
{
    // Las pruebas solo pueden usar una base desechable con este prefijo.
    // El patrón rechaza bases fuera del prefijo reservado, incluidas las de
    // desarrollo.
    private const BASE_DESECHABLE = '/^ptwin_test_[a-z0-9_]{6,50}$/D';

    /**
     * Creates the application.
     */
    public function createApplication(): Application
    {
        $app = require __DIR__.'/../bootstrap/app.php';

        $app->make(Kernel::class)->bootstrap();

        $this->afirmarEntornoDePruebasSeguro($app);

        return $app;
    }

    /**
     * Guarda dura de aislamiento (sprint final, Paso 1).
     *
     * Las pruebas SOLO pueden ejecutarse contra APP_ENV=testing, una conexión
     * MySQL llamada `mysql`, una base desechable `ptwin_test_*` en
     * loopback:3306 y sin una URL alternativa que pueda redirigir la conexión.
     * Cualquier otra combinación aborta durante el bootstrap, antes de
     * cualquier setUp o escritura.
     */
    private function afirmarEntornoDePruebasSeguro(Application $app): void
    {
        if (filled(env('DATABASE_URL'))) {
            throw new \RuntimeException(
                'Entorno de pruebas inseguro: DATABASE_URL está definido. '.
                'PHPUnit solo admite una base desechable ptwin_test_* en loopback.'
            );
        }

        $entorno = $app->environment();
        $conexion = (string) config('database.default');
        $config = (array) config("database.connections.{$conexion}");
        $driver = (string) ($config['driver'] ?? '');
        $baseDatos = (string) ($config['database'] ?? '');
        $host = (string) ($config['host'] ?? '');
        $puerto = (string) ($config['port'] ?? '');
        $url = $config['url'] ?? null;

        $hostSeguro = in_array($host, ['127.0.0.1', 'localhost', '::1'], true);

        if (filled($url)) {
            throw new \RuntimeException(
                'Entorno de pruebas inseguro: la conexión de base de datos tiene una URL alternativa. '.
                'PHPUnit solo admite una base desechable ptwin_test_* en loopback.'
            );
        }

        if ($entorno !== 'testing'
            || $conexion !== 'mysql'
            || $driver !== 'mysql'
            || ! $this->baseDePruebasSegura($baseDatos)
            || ! $hostSeguro
            || $puerto !== '3306') {
            throw new \RuntimeException(sprintf(
                'Entorno de pruebas inseguro: APP_ENV="%s", conexión="%s", driver="%s", BD="%s", host="%s", puerto="%s". '.
                'PHPUnit debe resolver a testing + mysql + ptwin_test_* en loopback:3306 '.
                '(ver backend/phpunit.xml).',
                $entorno,
                $conexion,
                $driver,
                $baseDatos,
                $host,
                $puerto,
            ));
        }
    }

    /**
     * La suite solo puede usar una base desechable `ptwin_test_*`; las pruebas
     * de aislamiento comparten esta comprobación.
     */
    protected function baseDePruebasSegura(string $base): bool
    {
        return preg_match(self::BASE_DESECHABLE, $base) === 1;
    }
}
