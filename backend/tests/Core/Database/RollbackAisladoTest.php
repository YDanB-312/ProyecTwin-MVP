<?php

namespace Tests\Core\Database;

use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use PDO;
use Tests\TestCase;

// C-05: valida en una base MySQL temporal y desechable que las migraciones se
// aplican, se revierten por completo y se vuelven a aplicar. No toca la base
// de desarrollo proyectwin_mvp ni bases ajenas.
class RollbackAisladoTest extends TestCase
{
    private ?string $baseTemporal = null;

    private string $conexion = 'rollback_tmp';

    protected function setUp(): void
    {
        parent::setUp();

        $base = config('database.connections.mysql');

        $this->assertTrue(
            $this->baseDePruebasSegura((string) $base['database']),
            'La prueba exige una base de testing aislada.'
        );
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

        foreach (['general_users', 'projects', 'similarities', 'motor_configs', 'sessions', 'account_activations'] as $tabla) {
            $this->assertTrue(
                Schema::connection($this->conexion)->hasTable($tabla),
                'Debe existir la tabla '.$tabla.' después de migrar.'
            );
        }

        $this->assertTrue(
            Schema::connection($this->conexion)->hasColumn('general_users', 'email_verified_at'),
            'Debe existir general_users.email_verified_at después de migrar.'
        );

        $tipoExpiracion = DB::connection($this->conexion)->selectOne(
            'SELECT DATA_TYPE FROM information_schema.columns ' .
            'WHERE table_schema = ? AND table_name = ? AND column_name = ?',
            [$this->baseTemporal, 'account_activations', 'expires_at']
        )?->DATA_TYPE;

        $this->assertSame(
            'datetime',
            strtolower((string) $tipoExpiracion),
            'account_activations.expires_at debe ser DATETIME para no usar ON UPDATE CURRENT_TIMESTAMP.'
        );

        foreach (['must_change_password', 'password_temporal', 'credenciales_enviadas_en', 'credenciales_error'] as $columna) {
            $this->assertFalse(
                Schema::connection($this->conexion)->hasColumn('general_users', $columna),
                'La columna legacy '.$columna.' debe desaparecer tras migrar.'
            );
        }

        $this->artisan('migrate:rollback', ['--database' => $this->conexion, '--force' => true])
            ->assertSuccessful();

        foreach (['general_users', 'projects', 'similarities', 'motor_configs', 'sessions', 'account_activations'] as $tabla) {
            $this->assertFalse(
                Schema::connection($this->conexion)->hasTable($tabla),
                'La tabla '.$tabla.' debe desaparecer tras el rollback.'
            );
        }

        // La re-migración comprueba que el esquema queda recuperable.
        $this->artisan('migrate', ['--database' => $this->conexion, '--force' => true])
            ->assertSuccessful();

        $this->assertTrue(Schema::connection($this->conexion)->hasTable('motor_configs'));
        $this->assertTrue(Schema::connection($this->conexion)->hasTable('account_activations'));
        $this->assertTrue(Schema::connection($this->conexion)->hasColumn('general_users', 'email_verified_at'));

        foreach (['must_change_password', 'password_temporal', 'credenciales_enviadas_en', 'credenciales_error'] as $columna) {
            $this->assertFalse(
                Schema::connection($this->conexion)->hasColumn('general_users', $columna),
                'La columna legacy '.$columna.' no debe reaparecer tras re-migrar.'
            );
        }
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
