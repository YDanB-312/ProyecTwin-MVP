<?php

namespace Tests\Feature;

use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use PDO;
use Tests\TestCase;

// C-05: valida en una base MySQL temporal y desechable que las migraciones se
// aplican, se revierten por completo y se vuelven a aplicar. No toca
// proyectwin_test (más allá de la guarda de aislamiento) ni proyectwin_mvp.
class RollbackAisladoTest extends TestCase
{
    private ?string $baseTemporal = null;

    private string $conexion = 'rollback_tmp';

    protected function setUp(): void
    {
        parent::setUp();

        $base = config('database.connections.mysql');

        $this->assertSame('proyectwin_test', $base['database'], 'La prueba exige la base de testing aislada.');
        $this->assertContains($base['host'], ['127.0.0.1', 'localhost', '::1'], 'La prueba exige loopback.');

        $nombre = 'proyectwin_rollback_'.substr(uniqid(), -8);
        $pdo = $this->pdoSinBase($base);

        try {
            $pdo->exec('CREATE DATABASE `'.$nombre.'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
        } catch (\Throwable $e) {
            $this->markTestSkipped('No se pudo crear la base temporal de rollback: '.$e->getMessage());
        }

        $this->baseTemporal = $nombre;
        config([
            "database.connections.{$this->conexion}" => array_merge($base, [
                'database' => $nombre,
            ]),
        ]);
        DB::purge($this->conexion);
    }

    protected function tearDown(): void
    {
        if ($this->baseTemporal !== null) {
            DB::purge($this->conexion);

            try {
                $pdo = $this->pdoSinBase(config('database.connections.mysql'));
                $pdo->exec('DROP DATABASE IF EXISTS `'.$this->baseTemporal.'`');
            } catch (\Throwable) {
                // La limpieza no debe ocultar el resultado de la prueba.
            }
        }

        parent::tearDown();
    }

    public function test_migraciones_y_rollback_en_base_temporal(): void
    {
        $this->artisan('migrate', ['--database' => $this->conexion, '--force' => true])
            ->assertSuccessful();

        foreach (['general_users', 'projects', 'similarities', 'motor_configs', 'sessions'] as $tabla) {
            $this->assertTrue(
                Schema::connection($this->conexion)->hasTable($tabla),
                'Debe existir la tabla '.$tabla.' después de migrar.'
            );
        }

        $this->artisan('migrate:rollback', ['--database' => $this->conexion, '--force' => true])
            ->assertSuccessful();

        foreach (['general_users', 'projects', 'similarities', 'motor_configs', 'sessions'] as $tabla) {
            $this->assertFalse(
                Schema::connection($this->conexion)->hasTable($tabla),
                'La tabla '.$tabla.' debe desaparecer tras el rollback.'
            );
        }

        // La re-migración comprueba que el esquema queda recuperable.
        $this->artisan('migrate', ['--database' => $this->conexion, '--force' => true])
            ->assertSuccessful();

        $this->assertTrue(Schema::connection($this->conexion)->hasTable('motor_configs'));
    }

    private function pdoSinBase(array $base): PDO
    {
        $dsn = sprintf(
            'mysql:host=%s;port=%s;charset=utf8mb4',
            $base['host'],
            $base['port'] ?? 3306
        );

        return new PDO($dsn, $base['username'], $base['password'], [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        ]);
    }
}
