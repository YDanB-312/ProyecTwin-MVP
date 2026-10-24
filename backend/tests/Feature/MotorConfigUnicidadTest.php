<?php

namespace Tests\Feature;

use App\Models\MotorConfig;
use Illuminate\Database\UniqueConstraintViolationException;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\DB;
use Symfony\Component\Process\Process;
use Tests\TestCase;

// D-02: la configuración del motor es una fila global. La inicialización usa
// la clave primaria existente (id = 1) y no requiere una migración adicional.
class MotorConfigUnicidadTest extends TestCase
{
    use DatabaseTransactions;

    /** @var string[] */
    private array $archivosTemporales = [];

    protected function setUp(): void
    {
        parent::setUp();

        $this->beforeApplicationDestroyed(function () {
            foreach ($this->archivosTemporales as $archivo) {
                if (is_file($archivo)) {
                    @unlink($archivo);
                }
            }
        });
    }

    private function temporal(string $prefijo): string
    {
        $ruta = tempnam(sys_get_temp_dir(), $prefijo);
        @unlink($ruta);
        $this->archivosTemporales[] = $ruta;

        return $ruta;
    }

    private function conexionAuxiliar()
    {
        $base = config('database.connections.mysql');

        // La prueba concurrente usa conexiones y COMMITs reales; nunca debe
        // ejecutarse contra la base de desarrollo.
        $this->assertSame('proyectwin_test', $base['database'], 'La prueba concurrente exige proyectwin_test.');

        config(['database.connections.mysql_aux' => $base]);
        DB::purge('mysql_aux');

        $aux = DB::connection('mysql_aux');
        $aux->statement('SET SESSION innodb_lock_wait_timeout = 5');

        return $aux;
    }

    public function test_la_configuracion_unica_se_crea_con_id_uno_y_no_duplica(): void
    {
        MotorConfig::query()->delete();

        $primera = MotorConfig::actual();
        $segunda = MotorConfig::actual();

        $this->assertSame(1, (int) $primera->id);
        $this->assertSame((int) $primera->id, (int) $segunda->id);
        $this->assertSame(1, MotorConfig::count());
        $this->assertEqualsWithDelta(0.30, (float) $primera->umbral, 0.0001);
        $this->assertSame(12, (int) $primera->meses);
    }

    public function test_una_configuracion_existente_con_id_distinto_no_se_sobrescribe(): void
    {
        MotorConfig::query()->delete();
        $existente = MotorConfig::create(['id' => 42, 'umbral' => 0.77, 'meses' => 3]);

        $actual = MotorConfig::actual();

        $this->assertSame((int) $existente->id, (int) $actual->id);
        $this->assertEqualsWithDelta(0.77, (float) $actual->umbral, 0.0001);
        $this->assertSame(3, (int) $actual->meses);
        $this->assertSame(1, MotorConfig::count());
    }

    public function test_actual_selecciona_siempre_la_fila_de_menor_id_si_hay_varias(): void
    {
        MotorConfig::query()->delete();
        MotorConfig::create(['id' => 20, 'umbral' => 0.60, 'meses' => 6]);
        MotorConfig::create(['id' => 10, 'umbral' => 0.40, 'meses' => 4]);

        $primera = MotorConfig::actual();
        $segunda = MotorConfig::actual();

        $this->assertSame(10, (int) $primera->id);
        $this->assertSame(10, (int) $segunda->id);
        $this->assertEqualsWithDelta(0.40, (float) $primera->umbral, 0.0001);
        $this->assertSame(4, (int) $primera->meses);
        $this->assertSame(2, MotorConfig::count(), 'La selección no debe borrar ni fusionar filas.');
    }

    public function test_la_restriccion_de_clave_primaria_rechaza_una_segunda_fila_inicial(): void
    {
        MotorConfig::query()->delete();
        $config = MotorConfig::actual();

        try {
            MotorConfig::create(['id' => 1, 'umbral' => 0.50, 'meses' => 6]);
            $this->fail('La base de datos debió rechazar una segunda fila con id = 1.');
        } catch (UniqueConstraintViolationException) {
            // La PK de id es la restricción que protege la inicialización.
        }

        $this->assertSame(1, MotorConfig::count());
        $this->assertSame((int) $config->id, (int) MotorConfig::first()->id);
        $this->assertEqualsWithDelta(0.30, (float) MotorConfig::first()->umbral, 0.0001);
    }

    public function test_dos_inicializaciones_simultaneas_dejan_una_sola_fila(): void
    {
        $aux = $this->conexionAuxiliar();
        $aux->delete('DELETE FROM motor_configs');
        $this->assertSame(0, (int) $aux->selectOne('SELECT COUNT(*) AS c FROM motor_configs')->c);

        $listoA = $this->temporal('motor_config_listo_a_');
        $listoB = $this->temporal('motor_config_listo_b_');
        $inicio = $this->temporal('motor_config_inicio_');
        $salidaA = $this->temporal('motor_config_salida_a_');
        $salidaB = $this->temporal('motor_config_salida_b_');

        $hijoA = null;
        $hijoB = null;

        try {
            $hijoA = $this->lanzarHijo($listoA, $inicio, $salidaA);
            $hijoB = $this->lanzarHijo($listoB, $inicio, $salidaB);

            $this->esperarArchivo($listoA, $hijoA);
            $this->esperarArchivo($listoB, $hijoB);

            // Ambos hijos ya ejecutaron su primer SELECT y fijaron su
            // instantánea REPEATABLE READ. Al liberarlos, ambos intentan crear
            // la misma PK: uno gana y el otro pasa por el catch de actual().
            file_put_contents($inicio, '1');

            $hijoA->wait();
            $hijoB->wait();

            $this->assertTrue($hijoA->isSuccessful(), 'Hijo A: '.$hijoA->getErrorOutput());
            $this->assertTrue($hijoB->isSuccessful(), 'Hijo B: '.$hijoB->getErrorOutput());

            $a = json_decode((string) file_get_contents($salidaA), true);
            $b = json_decode((string) file_get_contents($salidaB), true);

            $this->assertIsArray($a);
            $this->assertIsArray($b);
            $this->assertSame(1, (int) $a['id'], 'Ambos procesos deben recuperar la fila canónica.');
            $this->assertSame(1, (int) $b['id']);
            $this->assertSame(1, (int) $a['count']);
            $this->assertSame(1, (int) $b['count']);
            $this->assertEqualsWithDelta(0.30, (float) $a['umbral'], 0.0001);
            $this->assertSame(12, (int) $a['meses']);
            $this->assertGreaterThanOrEqual(1, (int) $a['inserts'], 'A debió intentar el INSERT.');
            $this->assertGreaterThanOrEqual(1, (int) $b['inserts'], 'B debió intentar el INSERT.');
            $this->assertTrue((bool) $a['for_update'] xor (bool) $b['for_update'], 'Exactamente un proceso debe usar FOR UPDATE.');
            $this->assertSame(1, (int) $aux->selectOne('SELECT COUNT(*) AS c FROM motor_configs')->c);
        } finally {
            if ($hijoA && $hijoA->isRunning()) {
                $hijoA->stop(1);
            }
            if ($hijoB && $hijoB->isRunning()) {
                $hijoB->stop(1);
            }
            $aux->delete('DELETE FROM motor_configs');
        }
    }

    private function lanzarHijo(string $listo, string $inicio, string $salida): Process
    {
        $script = $this->temporal('motor_config_hijo_');
        $lineas = [
            'use Illuminate\\Support\\Facades\\DB;',
            'require '.var_export(base_path('vendor/autoload.php'), true).';',
            '$app = require '.var_export(base_path('bootstrap/app.php'), true).';',
            '$app->make(\Illuminate\Contracts\Console\Kernel::class)->bootstrap();',
            'if (config(\'database.connections.mysql.database\') !== \'proyectwin_test\') {',
            '    fwrite(STDERR, "BD de prueba insegura"); exit(2);',
            '}',
            '$bloqueado = false;',
            '$insertos = 0;',
            '$forUpdate = false;',
            'DB::connection()->beforeExecuting(function ($query) use (&$insertos) {',
            '    $sql = strtolower(ltrim($query));',
            '    if (str_starts_with($sql, \'insert into\') && str_contains($sql, \'motor_configs\')) {',
            '        $insertos++;',
            '    }',
            '});',
            'DB::listen(function ($query) use (&$bloqueado, &$forUpdate) {',
            '    $sql = strtolower($query->sql);',
            '    if (str_contains($sql, \'motor_configs\') && str_contains($sql, \'for update\')) {',
            '        $forUpdate = true;',
            '    }',
            '    if (! $bloqueado',
            '        && str_starts_with(ltrim($sql), \'select\')',
            '        && str_contains($sql, \'motor_configs\')',
            '        && ! str_contains($sql, \'for update\')) {',
            '        $bloqueado = true;',
            '        file_put_contents('.var_export($listo, true).', "1");',
            '        $limite = microtime(true) + 15;',
            '        while (! is_file('.var_export($inicio, true).')) {',
            '            if (microtime(true) > $limite) { exit(4); }',
            '            usleep(2000);',
            '        }',
            '    }',
            '});',
            'DB::statement(\'SET SESSION innodb_lock_wait_timeout = 3\');',
            'DB::statement(\'SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ\');',
            '$c = DB::transaction(fn () => \App\Models\MotorConfig::actual());',
            'file_put_contents('.var_export($salida, true).', json_encode([',
            '    "id" => (int) $c->id,',
            '    "umbral" => (float) $c->umbral,',
            '    "meses" => (int) $c->meses,',
            '    "count" => \App\Models\MotorConfig::count(),',
            '    "inserts" => $insertos,',
            '    "for_update" => $forUpdate,',
            ']));',
        ];

        file_put_contents($script, "<?php\n".implode("\n", $lineas)."\n");

        $proceso = new Process([PHP_BINARY, $script], base_path(), ['APP_ENV' => 'testing']);
        $proceso->setTimeout(30);
        $proceso->start();

        return $proceso;
    }

    private function esperarArchivo(string $ruta, Process $proceso, int $timeoutMs = 10000): void
    {
        $fin = microtime(true) + $timeoutMs / 1000;
        while (microtime(true) < $fin) {
            if (is_file($ruta)) {
                return;
            }
            if (! $proceso->isRunning()) {
                $this->fail('El proceso hijo terminó antes de la barrera. '.$proceso->getErrorOutput());
            }
            usleep(50000);
        }

        $this->fail('El proceso hijo no alcanzó el marcador esperado: '.$ruta);
    }
}
