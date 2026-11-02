<?php

declare(strict_types=1);

namespace Tests\Core\Operations;

use App\Support\Pruebas\ReglasBaseDatos;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

// Comprobación de integración: Laravel, Artisan y la conexión efectiva deben
// apuntar a la misma base desechable creada por el runner.
class RunTestsIntegrationTest extends TestCase
{
    public function test_laravel_y_artisan_usan_la_misma_base_desechable(): void
    {
        $baseConfigurada = (string) config('database.connections.mysql.database');
        $baseEsperada = env('PTWIN_TEST_DATABASE');
        $baseEsperada = is_string($baseEsperada) ? trim($baseEsperada) : '';

        $this->assertTrue(ReglasBaseDatos::esBasePermitida($baseConfigurada), $baseConfigurada);
        $this->assertSame($baseConfigurada, DB::connection('mysql')->getDatabaseName());

        if ($baseEsperada !== '') {
            $this->assertSame($baseEsperada, $baseConfigurada);
        }

        $fila = DB::connection('mysql')->selectOne('select database() as base, @@port as puerto');

        $this->assertNotNull($fila);
        $this->assertSame($baseConfigurada, (string) $fila->base);
        $this->assertSame('3306', (string) $fila->puerto);

        $this->assertSame(0, Artisan::call('migrate:status'));
        $this->assertSame(0, Artisan::call('migrate', ['--force' => true]));
    }
}
