<?php

namespace Tests\Core\Database;

use App\Models\Apprentice;
use App\Models\AuditLog;
use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\Comment;
use App\Models\GeneralUser;
use App\Models\KnowledgeNetwork;
use App\Models\Notification;
use App\Models\Project;
use App\Models\ProjectHistory;
use App\Models\Similarity;
use App\Models\TrainingProgram;
use App\Support\CuentasDemo;
use Database\Seeders\DatabaseSeeder;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use PDO;
use Tests\TestCase;

// El escenario demo debe poder ejecutarse dos veces sin duplicar ni adoptar
// registros ajenos. Se prueba en una base temporal desechable, nunca en
// proyectwin_test ni en proyectwin_mvp.
class SeederIdempotenciaTest extends TestCase
{
    private ?string $baseTemporal = null;

    private string $conexion = 'demo_tmp';

    protected function setUp(): void
    {
        parent::setUp();

        $base = config('database.connections.mysql');

        $this->assertSame('proyectwin_test', $base['database'], 'La prueba exige la base de testing aislada.');
        $this->assertContains($base['host'], ['127.0.0.1', 'localhost', '::1'], 'La prueba exige loopback.');

        $nombre = 'proyectwin_demo_'.substr(uniqid(), -8);
        $pdo = $this->pdoSinBase($base);

        try {
            $pdo->exec('CREATE DATABASE `'.$nombre.'` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
        } catch (\Throwable $e) {
            $this->markTestSkipped('No se pudo crear la base temporal demo: '.$e->getMessage());
        }

        $this->baseTemporal = $nombre;
        config([
            "database.connections.{$this->conexion}" => array_merge($base, [
                'database' => $nombre,
            ]),
        ]);
        DB::purge($this->conexion);
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

    public function test_el_escenario_demo_es_idempotente_y_no_usa_correos_institucionales(): void
    {
        $this->artisan('migrate', ['--database' => $this->conexion, '--force' => true])
            ->assertSuccessful();

        app(DatabaseSeeder::class)->run();

        $tablas = [
            'usuarios' => GeneralUser::class,
            'redes' => KnowledgeNetwork::class,
            'programas' => TrainingProgram::class,
            'fichas' => ClassGroup::class,
            'aprendices' => Apprentice::class,
            'propuestas' => Project::class,
            'comentarios' => Comment::class,
            'notificaciones' => Notification::class,
            'reportes' => BugReport::class,
            'similitudes' => Similarity::class,
            'historial' => ProjectHistory::class,
            'auditoria' => AuditLog::class,
        ];

        $antes = [];
        foreach ($tablas as $nombre => $modelo) {
            $antes[$nombre] = $modelo::count();
        }
        $antes['registro_demo'] = DB::table('demo_seed_records')->count();

        $this->assertSame(14, $antes['usuarios']);
        $this->assertSame(8, $antes['aprendices']);
        $this->assertSame(2, $antes['redes']);
        $this->assertSame(3, $antes['programas']);
        $this->assertSame(4, $antes['fichas']);
        $this->assertSame(16, $antes['propuestas']);
        $this->assertGreaterThan(0, $antes['similitudes']);
        $this->assertGreaterThan(0, $antes['notificaciones']);
        $this->assertGreaterThan(0, $antes['historial']);
        $this->assertGreaterThan(0, $antes['auditoria']);

        $this->assertSame(0, GeneralUser::where('correo', 'like', '%@soy.sena.edu.co')->count());
        $this->assertSame(0, GeneralUser::where('correo', 'like', '%@sena.edu.co')->count());
        $this->assertTrue(Hash::check(
            CuentasDemo::password(),
            GeneralUser::where('correo', 'maria.gonzalez@example.com')->firstOrFail()->password
        ));

        app(DatabaseSeeder::class)->run();

        foreach ($tablas as $nombre => $modelo) {
            $this->assertSame($antes[$nombre], $modelo::count(), "El seeder duplicó {$nombre} en la segunda ejecución.");
        }
        $this->assertSame($antes['registro_demo'], DB::table('demo_seed_records')->count());
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
