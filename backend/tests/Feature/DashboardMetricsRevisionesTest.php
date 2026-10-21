<?php

namespace Tests\Feature;

use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Project;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// El gráfico del instructor cuenta solo sus decisiones académicas
// (aprobada/rechazada) sobre proyectos de su ámbito.
class DashboardMetricsRevisionesTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol, string $sufijo = ''): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Metrica',
            'apellido' => 'Revision ' . $sufijo,
            'correo' => 'revision.' . $sufijo . '.' . uniqid() . '@metricas.local',
            'username' => 'revision_' . $sufijo . '_' . uniqid(),
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    private function instructor(GeneralUser $usuario): Instructor
    {
        return Instructor::create([
            'id_usuario' => $usuario->id,
            'fecha_ingreso' => now()->toDateString(),
        ]);
    }

    private function proyecto(GeneralUser $creador, ?Instructor $instructor = null): Project
    {
        return Project::create([
            'titulo' => 'Proyecto metrica ' . uniqid(),
            'estado' => 'pendiente',
            'id_creador' => $creador->id,
            'id_instructor_asignado' => $instructor?->id,
        ]);
    }

    private function historial(Project $proyecto, GeneralUser $actor, string $accion, Carbon $fecha): void
    {
        DB::table('project_histories')->insert([
            'id_proyecto' => $proyecto->id,
            'id_usuario' => $actor->id,
            'accion' => $accion,
            'detalle' => null,
            'created_at' => $fecha,
            'updated_at' => $fecha,
        ]);
    }

    private function metricas(GeneralUser $usuario, string $periodo): array
    {
        $token = $this->postJson('/v1/auth/login', [
            'username' => $usuario->username,
            'password' => '123456',
        ])->assertOk()->json('token');

        $this->app['auth']->forgetGuards();

        return $this->withToken($token)
            ->getJson('/v1/dashboard-metrics?periodo=' . $periodo)
            ->assertOk()
            ->json();
    }

    private function totalEvolucion(array $respuesta): int
    {
        return (int) collect($respuesta['evolucion'] ?? [])->sum('total');
    }

    public function test_cuenta_solo_decisiones_propias_en_el_ambito_y_periodo(): void
    {
        $instructorUser = $this->usuario('instructor', 'principal');
        $instructor = $this->instructor($instructorUser);
        $otroInstructorUser = $this->usuario('instructor', 'otro');
        $otroInstructor = $this->instructor($otroInstructorUser);
        $creador = $this->usuario('aprendiz');

        $proyectoPropio = $this->proyecto($creador, $instructor);
        $proyectoOtro = $this->proyecto($creador, $otroInstructor);

        $ahora = now()->subDay();
        $diezDias = now()->subDays(10);
        $cuarentaYCincoDias = now()->subDays(45);
        $fueraPeriodo = now()->subMonths(4);

        // Decisiones propias que deben contar.
        $this->historial($proyectoPropio, $instructorUser, 'aprobada', $ahora);
        $this->historial($proyectoPropio, $instructorUser, 'rechazada', $ahora);
        $this->historial($proyectoPropio, $instructorUser, 'aprobada', $diezDias);
        // Dentro de 3m, pero fuera de 30d y 7d.
        $this->historial($proyectoPropio, $instructorUser, 'aprobada', $cuarentaYCincoDias);

        // Misma propuesta, decisión de otro usuario: no cuenta.
        $this->historial($proyectoPropio, $otroInstructorUser, 'aprobada', $ahora);

        // Acciones que no son decisiones académicas: no cuentan.
        foreach (['creada', 'enviada', 'reenviada', 'actualizada', 'devuelta_revision'] as $accion) {
            $this->historial($proyectoPropio, $instructorUser, $accion, $ahora);
        }

        // Proyecto fuera del ámbito del instructor: no cuenta.
        $this->historial($proyectoOtro, $instructorUser, 'aprobada', $ahora);

        // Decisión propia fuera de la ventana de 3 meses: no cuenta.
        $this->historial($proyectoPropio, $instructorUser, 'rechazada', $fueraPeriodo);

        $tresMeses = $this->metricas($instructorUser, '3m');
        $treintaDias = $this->metricas($instructorUser, '30d');
        $sieteDias = $this->metricas($instructorUser, '7d');

        $this->assertSame(4, $this->totalEvolucion($tresMeses));
        $this->assertSame(3, $this->totalEvolucion($treintaDias));
        $this->assertSame(2, $this->totalEvolucion($sieteDias));

        $this->assertCount(3, $tresMeses['evolucion']);
        $this->assertCount(30, $treintaDias['evolucion']);
        $this->assertCount(7, $sieteDias['evolucion']);

        $clavesTresMeses = [];
        for ($i = 2; $i >= 0; $i--) {
            $clavesTresMeses[] = now()->subMonths($i)->format('Y-m');
        }
        $clavesTreintaDias = [];
        for ($i = 29; $i >= 0; $i--) {
            $clavesTreintaDias[] = now()->subDays($i)->format('Y-m-d');
        }
        $clavesSieteDias = [];
        for ($i = 6; $i >= 0; $i--) {
            $clavesSieteDias[] = now()->subDays($i)->format('Y-m-d');
        }

        $this->assertSame($clavesTresMeses, array_column($tresMeses['evolucion'], 'clave'));
        $this->assertSame($clavesTreintaDias, array_column($treintaDias['evolucion'], 'clave'));
        $this->assertSame($clavesSieteDias, array_column($sieteDias['evolucion'], 'clave'));
    }
}
