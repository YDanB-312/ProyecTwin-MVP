<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\Comment;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\Similarity;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Contrato de las gráficas de estado: cobertura y antigüedad del instructor,
// similitud y antigüedad visibles para el aprendiz.
class DashboardMetricsGraficasTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Métricas',
            'correo' => $rol . '.' . uniqid() . '@metricas.local',
            'username' => 'metricas_' . uniqid(),
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    private function token(GeneralUser $usuario): string
    {
        return $this->postJson('/v1/auth/login', [
            'username' => $usuario->username,
            'password' => '123456',
        ])->assertOk()->json('token');
    }

    private function programa(): TrainingProgram
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red métricas ' . uniqid()]);

        return TrainingProgram::create([
            'nombre' => 'Programa métricas ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);
    }

    private function ficha(TrainingProgram $programa, Instructor $instructor): ClassGroup
    {
        return ClassGroup::create([
            'codigo' => 'met-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha métricas ' . uniqid(),
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    private function proyecto(GeneralUser $creador, ClassGroup $ficha, string $estado): Project
    {
        return Project::create([
            'titulo' => 'Propuesta métricas ' . uniqid(),
            'resumen' => 'Resumen de prueba para métricas de dashboard.',
            'area_aplicacion' => 'Tecnología',
            'estado' => $estado,
            'id_creador' => $creador->id,
            'id_class_group' => $ficha->id,
        ]);
    }

    public function test_instructor_recibe_cobertura_y_antiguedad_de_sus_fichas(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $instructorUser->id,
        ]);
        $ficha = $this->ficha($programa, $instructor);
        $aprendizUser = $this->usuario('aprendiz');
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $aprendizUser->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $programa->id,
        ]);
        $this->proyecto($aprendizUser, $ficha, 'pendiente');
        $this->proyecto($aprendizUser, $ficha, 'aprobado');

        // Ficha ajena: no debe aparecer en el detalle.
        $otroInstructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $this->usuario('instructor')->id,
        ]);
        $ajena = $this->ficha($programa, $otroInstructor);
        $this->proyecto($aprendizUser, $ajena, 'pendiente');

        $token = $this->token($instructorUser);
        $this->app['auth']->forgetGuards();

        $res = $this->withToken($token)
            ->getJson('/v1/dashboard-metrics')
            ->assertOk();

        $this->assertSame(1, $res->json('fichas.total'));
        $detalle = collect($res->json('fichas.detalle'));
        $this->assertCount(1, $detalle);
        $fila = $detalle->firstWhere('id', (int) $ficha->id);
        $this->assertNotNull($fila);
        $this->assertSame(1, (int) $fila['pendientes']);
        $this->assertSame(2, (int) $fila['propuestas']);
        $this->assertSame(1, (int) $fila['aprendices']);
        $this->assertSame(0, (int) $fila['borradores']);
        $this->assertSame(1, (int) $fila['aprobadas']);
        $this->assertSame(0, (int) $fila['rechazadas']);
        $this->assertSame(2, (int) $fila['revisables']);
        $this->assertSame(1, (int) $fila['revisadas']);
        $this->assertSame(50, (int) $fila['cobertura']);

        $antiguedad = collect($res->json('antiguedadPendientes'))->pluck('value', 'label');
        $this->assertSame(1, (int) $antiguedad['0–2 días']);
        $this->assertSame(0, (int) $antiguedad['3–7 días']);
        $this->assertSame(0, (int) $antiguedad['8–15 días']);
        $this->assertSame(0, (int) $antiguedad['Más de 15 días']);
    }

    public function test_aprendiz_recibe_sus_similitudes_y_antiguedad(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $instructorUser->id,
        ]);
        $ficha = $this->ficha($programa, $instructor);
        $aprendizUser = $this->usuario('aprendiz');
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $aprendizUser->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $programa->id,
        ]);

        $propio = $this->proyecto($aprendizUser, $ficha, 'aprobado');
        $sinPares = $this->proyecto($aprendizUser, $ficha, 'pendiente');
        Comment::create([
            'texto' => 'Revisa la delimitación del problema.',
            'id_proyecto' => $propio->id,
            'id_usuario' => $instructorUser->id,
        ]);
        foreach ([35, 75, 92] as $porcentaje) {
            $otro = $this->proyecto($this->usuario('aprendiz'), $ficha, 'aprobado');
            Similarity::create([
                'id_proyecto_1' => min((int) $propio->id, (int) $otro->id),
                'id_proyecto_2' => max((int) $propio->id, (int) $otro->id),
                'porcentaje' => $porcentaje,
                'fecha' => now()->toDateString(),
            ]);
        }

        $token = $this->token($aprendizUser);
        $this->app['auth']->forgetGuards();

        $res = $this->withToken($token)
            ->getJson('/v1/dashboard-metrics')
            ->assertOk();

        $niveles = collect($res->json('similitudesNiveles'))->pluck('value', 'label');
        $this->assertSame(1, (int) $niveles['30–49 %']);
        $this->assertSame(0, (int) $niveles['50–69 %']);
        $this->assertSame(1, (int) $niveles['70–89 %']);
        $this->assertSame(1, (int) $niveles['90–100 %']);
        $this->assertSame(0, (int) $niveles['0–29 %']);

        $porPropuesta = collect($res->json('similitudesPropuestas'));
        $this->assertCount(2, $porPropuesta);

        $fila = $porPropuesta->firstWhere('id', (int) $propio->id);
        $this->assertNotNull($fila);
        $this->assertSame(92, (int) $fila['max']);
        $this->assertSame(3, (int) $fila['count']);

        $filaSinPares = $porPropuesta->firstWhere('id', (int) $sinPares->id);
        $this->assertNotNull($filaSinPares);
        $this->assertSame(0, (int) $filaSinPares['max']);
        $this->assertSame(0, (int) $filaSinPares['count']);
        $this->assertSame((int) $propio->id, (int) $porPropuesta->first()['id']);

        $detalle = collect($res->json('propuestasDetalle'));
        $this->assertCount(2, $detalle);

        $detallePendiente = $detalle->firstWhere('id', (int) $sinPares->id);
        $this->assertNotNull($detallePendiente);
        $this->assertSame('pendiente', $detallePendiente['estado']);
        $this->assertSame(0, (int) $detallePendiente['diasEnRevision']);
        $this->assertSame(0, (int) $detallePendiente['maxSimilitud']);
        $this->assertSame(0, (int) $detallePendiente['similitudes']);

        $detalleAprobada = $detalle->firstWhere('id', (int) $propio->id);
        $this->assertNotNull($detalleAprobada);
        $this->assertSame('aprobado', $detalleAprobada['estado']);
        $this->assertNull($detalleAprobada['diasEnRevision']);
        $this->assertSame(92, (int) $detalleAprobada['maxSimilitud']);
        $this->assertSame(3, (int) $detalleAprobada['similitudes']);
        $this->assertSame(1, (int) $detalleAprobada['observaciones']);

        $antiguedad = collect($res->json('antiguedadPendientes'))->pluck('value', 'label');
        $this->assertSame(1, (int) $antiguedad['0–2 días']);
        $this->assertSame(0, (int) $antiguedad['3–7 días']);
        $this->assertSame(0, (int) $antiguedad['8–15 días']);
        $this->assertSame(0, (int) $antiguedad['Más de 15 días']);
    }

    public function test_antiguedad_pendientes_usa_el_ultimo_envio(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $instructorUser->id,
        ]);
        $ficha = $this->ficha($programa, $instructor);
        $creador = $this->usuario('aprendiz');

        $cincoDias = $this->proyecto($creador, $ficha, 'pendiente');
        $doceDias = $this->proyecto($creador, $ficha, 'pendiente');
        $veinteDias = $this->proyecto($creador, $ficha, 'pendiente');

        $historial = function (Project $proyecto, string $accion, int $dias) use ($instructorUser) {
            DB::table('project_histories')->insert([
                'id_proyecto' => $proyecto->id,
                'id_usuario' => $instructorUser->id,
                'accion' => $accion,
                'detalle' => null,
                'created_at' => now()->subDays($dias),
                'updated_at' => now()->subDays($dias),
            ]);
        };

        $historial($cincoDias, 'enviada', 5);
        // El reenvío reciente es la referencia, no el envío antiguo.
        $historial($doceDias, 'enviada', 30);
        $historial($doceDias, 'reenviada', 12);
        $historial($veinteDias, 'enviada', 20);

        $token = $this->token($instructorUser);
        $this->app['auth']->forgetGuards();

        $res = $this->withToken($token)
            ->getJson('/v1/dashboard-metrics')
            ->assertOk();

        $antiguedad = collect($res->json('antiguedadPendientes'))->pluck('value', 'label');
        $this->assertSame(0, (int) $antiguedad['0–2 días']);
        $this->assertSame(1, (int) $antiguedad['3–7 días']);
        $this->assertSame(1, (int) $antiguedad['8–15 días']);
        $this->assertSame(1, (int) $antiguedad['Más de 15 días']);
    }
}
