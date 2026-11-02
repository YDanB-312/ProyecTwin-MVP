<?php

namespace Tests;

use Illuminate\Contracts\Console\Kernel;
use Illuminate\Foundation\Application;

trait CreatesApplication
{
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
     * MySQL llamada `mysql`, la base `proyectwin_test` en loopback:3306 y sin
     * una URL alternativa que pueda redirigir la conexión. Cualquier otra
     * combinación aborta durante el bootstrap, antes de cualquier setUp o
     * escritura.
     */
    private function afirmarEntornoDePruebasSeguro(Application $app): void
    {
        if (filled(env('DATABASE_URL'))) {
            throw new \RuntimeException(
                'Entorno de pruebas inseguro: DATABASE_URL está definido. '.
                'PHPUnit solo admite la conexión explícita a proyectwin_test en loopback.'
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
                'PHPUnit solo admite la conexión explícita a proyectwin_test en loopback.'
            );
        }

        if ($entorno !== 'testing'
            || $conexion !== 'mysql'
            || $driver !== 'mysql'
            || $baseDatos !== 'proyectwin_test'
            || ! $hostSeguro
            || $puerto !== '3306') {
            throw new \RuntimeException(sprintf(
                'Entorno de pruebas inseguro: APP_ENV="%s", conexión="%s", driver="%s", BD="%s", host="%s", puerto="%s". '.
                'PHPUnit debe resolver a testing + mysql + proyectwin_test en loopback:3306 '.
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
}
