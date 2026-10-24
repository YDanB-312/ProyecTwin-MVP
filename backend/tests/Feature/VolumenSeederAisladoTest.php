<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Project;
use Database\Seeders\DatabaseSeeder;
use Database\Seeders\VolumenSeeder;
use Illuminate\Support\Facades\DB;
use PDO;
use Tests\TestCase;

// D-05: valida el seeder opcional contra una base temporal desechable. No lo
// ejecuta sobre proyectwin_test ni sobre ninguna base compartida.
class VolumenSeederAisladoTest extends TestCase
{
    private ?string $baseTemporal = null;

    private string $conexion = 'volumen_tmp';

    protected function setUp(): void
    {
        parent::setUp();

        $base = config('database.connections.mysql');

        $this->assertSame('proyectwin_test', $base['database'], 'La prueba exige la base de testing aislada.');
        $this->assertContains($base['host'], ['127.0.0.1', 'localhost', '::1'], 'La prueba exige loopback.');

        $nombre = 'proyectwin_volumen_'.substr(uniqid(), -8);
        $pdo = $this->pdoSinBase($base);

        try {
            $pdo->exec('CREATE DATABASE `'.$nombre.'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
        } catch (\Throwable $e) {
            $this->markTestSkipped('No se pudo crear la base temporal de volumen: '.$e->getMessage());
        }

        $this->baseTemporal = $nombre;
        config([
            "database.connections.{$this->conexion}" => array_merge($base, [
                'database' => $nombre,
            ]),
        ]);
        DB::purge($this->conexion);

        // Los seeders usan modelos sin conexión explícita.
        DB::setDefaultConnection($this->conexion);
    }

    protected function tearDown(): void
    {
        DB::setDefaultConnection('mysql');

        if ($this->baseTemporal !== null) {
            DB::purge($this->conexion);

            try {
                $pdo = $this->pdoSinBase(config('database.connections.mysql'));
                $pdo->exec('DROP DATABASE IF EXISTS `'.$this->baseTemporal.'`');
            } catch (\Throwable) {
                // La limpieza no debe ocultar el resultado.
            }
        }

        parent::tearDown();
    }

    public function test_volumen_aislado_genera_relaciones_y_codigos_unicos(): void
    {
        $this->artisan('migrate', ['--database' => $this->conexion, '--force' => true])
            ->assertSuccessful();

        app(DatabaseSeeder::class)->run();

        $antes = [
            'usuarios' => GeneralUser::count(),
            'instructores' => Instructor::count(),
            'fichas' => ClassGroup::count(),
            'aprendices' => Apprentice::count(),
            'propuestas' => Project::count(),
            'reportes' => BugReport::count(),
        ];

        app(VolumenSeeder::class)->run();

        $this->assertSame($antes['usuarios'] + 45, GeneralUser::count());
        $this->assertSame($antes['instructores'] + 5, Instructor::count());
        $this->assertSame($antes['fichas'] + 5, ClassGroup::count());
        $this->assertSame($antes['aprendices'] + 40, Apprentice::count());
        $this->assertSame($antes['propuestas'] + 80, Project::count());
        $this->assertSame($antes['reportes'] + 12, BugReport::count());

        $codigos = Apprentice::where('codigo', 'like', 'VOL-%')->pluck('codigo');
        $this->assertCount(40, $codigos);
        $this->assertCount(40, $codigos->unique());
        $this->assertSame(0, Apprentice::whereIn('codigo', $codigos)->whereNull('id_class_group')->count());
        $this->assertSame(0, Apprentice::whereIn('codigo', $codigos)->whereNull('id_programa')->count());
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
