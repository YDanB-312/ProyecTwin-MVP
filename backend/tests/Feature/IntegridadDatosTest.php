<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Integridad de datos (Fase 1): códigos de aprendiz únicos, sin integrantes
// duplicados en el equipo, pares de similitud normalizados (menor primero) y
// el programa se limpia al salir de la ficha (opción A).
class IntegridadDatosTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Integridad',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    private function token(GeneralUser $user): string
    {
        return $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => '123456',
        ])->assertOk()->json('token');
    }

    private function como(GeneralUser $user)
    {
        $token = $this->token($user);
        $this->app['auth']->forgetGuards();
        return $this->withToken($token);
    }

    private function programa(): TrainingProgram
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        return TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);
    }

    private function ficha(TrainingProgram $programa): ClassGroup
    {
        $instructor = Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $this->usuario('instructor')->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'int-' . uniqid(),
            'nombre' => 'Ficha integridad',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    // ---------------------------------------------------------------- Códigos

    public function test_no_se_pueden_repetir_codigos_de_aprendiz(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();
        $codigo = 'AP-' . uniqid();

        $this->como($admin)->postJson('/v1/apprentices', [
            'codigo' => $codigo,
            'id_usuario' => $this->usuario('aprendiz')->id,
            'id_programa' => $programa->id,
        ])->assertCreated();

        $this->como($admin)->postJson('/v1/apprentices', [
            'codigo' => $codigo,
            'id_usuario' => $this->usuario('aprendiz')->id,
            'id_programa' => $programa->id,
        ])->assertStatus(422);
    }

    public function test_actualizar_su_propio_codigo_no_colisiona(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();
        $aprendiz = Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $this->usuario('aprendiz')->id,
            'id_programa' => $programa->id,
        ]);

        // Reenviar su mismo código debe ser válido (unique ignora el propio id).
        $this->como($admin)->putJson('/v1/apprentices/' . $aprendiz->id, [
            'codigo' => $aprendiz->codigo,
            'id_usuario' => $aprendiz->id_usuario,
            'id_programa' => $programa->id,
        ])->assertOk();
    }

    // ---------------------------------------------------------------- Pivote

    public function test_el_pivote_no_duplica_integrantes(): void
    {
        $admin = $this->usuario('admin');
        $aprendiz = Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $this->usuario('aprendiz')->id,
            'id_programa' => $this->programa()->id,
        ]);
        $proyecto = Project::create([
            'titulo' => 'Propuesta equipo',
            'resumen' => 'Resumen de la propuesta de equipo para la prueba.',
            'area_aplicacion' => 'Tecnología',
            'id_creador' => $admin->id,
        ]);

        $payload = ['id_aprendiz' => $aprendiz->id, 'id_proyecto' => $proyecto->id];
        // Primera vez: se crea (201). Segunda: ya existe, no duplica (200).
        $this->como($admin)->postJson('/v1/apprentice-projects', $payload)->assertCreated();
        $this->como($admin)->postJson('/v1/apprentice-projects', $payload)->assertOk();

        $this->assertSame(1, ApprenticeProject::where('id_aprendiz', $aprendiz->id)
            ->where('id_proyecto', $proyecto->id)->count());
    }

    // ---------------------------------------------------------------- Pares

    public function test_los_pares_se_guardan_con_el_id_menor_primero(): void
    {
        $admin = $this->usuario('admin');
        $p1 = Project::create([
            'titulo' => 'Propuesta menor',
            'resumen' => 'Resumen de la propuesta menor para la prueba.',
            'area_aplicacion' => 'Tecnología',
            'id_creador' => $admin->id,
        ]);
        $p2 = Project::create([
            'titulo' => 'Propuesta mayor',
            'resumen' => 'Resumen de la propuesta mayor para la prueba.',
            'area_aplicacion' => 'Tecnología',
            'id_creador' => $admin->id,
        ]);

        // Se envía invertido (mayor, menor): debe guardarse (menor, mayor).
        $res = $this->como($admin)->postJson('/v1/similarities', [
            'porcentaje' => 55,
            'id_proyecto_1' => $p2->id,
            'id_proyecto_2' => $p1->id,
        ])->assertCreated();

        $this->assertSame($p1->id, (int) $res->json('id_proyecto_1'));
        $this->assertSame($p2->id, (int) $res->json('id_proyecto_2'));
    }

    // ---------------------------------------------------------------- Programa

    public function test_salir_de_la_ficha_limpia_el_programa(): void
    {
        $ficha = $this->ficha($this->programa());
        $user = $this->usuario('aprendiz');
        $aprendiz = Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $user->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);

        $this->como($user)->deleteJson('/v1/apprentices/me/ficha')->assertOk();

        $fresco = $aprendiz->fresh();
        $this->assertNull($fresco->id_class_group);
        $this->assertNull($fresco->id_programa);
    }

    public function test_no_se_borra_un_programa_con_aprendices_a_nivel_bd(): void
    {
        $programa = $this->programa();
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $this->usuario('aprendiz')->id,
            'id_programa' => $programa->id,
        ]);

        $this->expectException(\Illuminate\Database\QueryException::class);
        $programa->delete();
    }
}
