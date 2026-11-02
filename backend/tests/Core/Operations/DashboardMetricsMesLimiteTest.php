<?php

namespace Tests\Core\Operations;

use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Project;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// La serie mensual debe construirse desde el inicio del mes. Restar meses
// directamente desde un día 29/30/31 puede duplicar un bucket o saltarse otro.
class DashboardMetricsMesLimiteTest extends TestCase
{
    use DatabaseTransactions;

    private const PASSWORD = 'claveActual123';

    protected function tearDown(): void
    {
        Carbon::setTestNow();

        parent::tearDown();
    }

    public function test_la_serie_de_tres_meses_no_duplica_buckets_en_fin_de_mes(): void
    {
        $escenarios = [
            '2026-10-31 23:59:59' => ['2026-08', '2026-09', '2026-10'],
            '2026-01-31 23:59:59' => ['2025-11', '2025-12', '2026-01'],
            '2026-02-28 23:59:59' => ['2025-12', '2026-01', '2026-02'],
        ];

        foreach ($escenarios as $fecha => $clavesEsperadas) {
            Carbon::setTestNow(Carbon::parse($fecha));

            $usuario = GeneralUser::create([
                'nombre' => 'Metrica',
                'apellido' => 'Mes '.uniqid(),
                'correo' => 'mes.'.uniqid().'@metricas.local',
                'username' => 'mes_'.uniqid(),
                'password' => Hash::make(self::PASSWORD),
                'rol' => 'instructor',
                'estado' => true,
            ]);

            $instructor = Instructor::create([
                'fecha_ingreso' => now()->toDateString(),
                'id_usuario' => $usuario->id,
            ]);

            $proyecto = Project::create([
                'titulo' => 'Proyecto mes '.uniqid(),
                'estado' => 'pendiente',
                'id_creador' => $usuario->id,
                'id_instructor_asignado' => $instructor->id,
            ]);

            foreach ($clavesEsperadas as $clave) {
                DB::table('project_histories')->insert([
                    'id_proyecto' => $proyecto->id,
                    'id_usuario' => $usuario->id,
                    'accion' => 'aprobada',
                    'detalle' => null,
                    'created_at' => Carbon::parse($clave.'-15 12:00:00'),
                    'updated_at' => Carbon::parse($clave.'-15 12:00:00'),
                ]);
            }

            $respuesta = $this->postJson('/v1/auth/login', [
                'correo' => $usuario->correo,
                'password' => self::PASSWORD,
            ])->assertOk()->json();

            $this->app['auth']->forgetGuards();

            $evolucion = $this->withToken($respuesta['token'])
                ->getJson('/v1/dashboard-metrics?periodo=3m')
                ->assertOk()
                ->json('evolucion');

            $this->assertSame($clavesEsperadas, array_column($evolucion, 'clave'));
            $this->assertSame([1, 1, 1], array_column($evolucion, 'total'));
        }
    }
}
