<?php

namespace Tests\Feature;

use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Permisos por rol: cada rol accede solo a lo que le corresponde.
class PermisosTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Per',
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

    private function fichaDe(GeneralUser $instructorUser): ClassGroup
    {
        $instructor = Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $instructorUser->id,
        ]);
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'per-' . uniqid(),
            'nombre' => 'Ficha permisos',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    public function test_el_aprendiz_no_accede_a_gestion(): void
    {
        $aprendiz = $this->usuario('aprendiz');

        $this->como($aprendiz)->getJson('/v1/general-users')->assertStatus(403);
        $this->como($aprendiz)->getJson('/v1/audit-logs')->assertStatus(403);
        $this->como($aprendiz)
            ->postJson('/v1/class-groups', ['codigo' => 'x', 'nombre' => 'x', 'estado' => 'activo', 'id_programa' => 1, 'id_instructor' => 1])
            ->assertStatus(403);
        $this->como($aprendiz)
            ->postJson('/v1/similarities', ['porcentaje' => 10, 'id_proyecto_1' => 1, 'id_proyecto_2' => 2])
            ->assertStatus(403);
    }

    public function test_el_instructor_no_accede_a_catalogos_ni_usuarios(): void
    {
        $instructor = $this->usuario('instructor');

        $this->como($instructor)->postJson('/v1/knowledge-networks', ['nombre' => 'Red ' . uniqid()])->assertStatus(403);
        $this->como($instructor)->postJson('/v1/training-programs', [
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => 1,
        ])->assertStatus(403);
        $this->como($instructor)->getJson('/v1/general-users')->assertStatus(403);
    }

    public function test_el_instructor_no_edita_una_ficha_ajena(): void
    {
        $dueno = $this->usuario('instructor');
        $ficha = $this->fichaDe($dueno);
        $ajeno = $this->usuario('instructor');
        Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $ajeno->id]);

        $this->como($ajeno)
            ->putJson('/v1/class-groups/' . $ficha->id, [
                'codigo' => $ficha->codigo,
                'nombre' => 'Intento ajeno',
                'estado' => 'activo',
                'id_programa' => $ficha->id_programa,
                'id_instructor' => $ficha->id_instructor,
            ])
            ->assertStatus(403);
    }

    public function test_el_admin_gestiona_catalogos_usuarios_y_bitacora(): void
    {
        $admin = $this->usuario('admin');

        $this->como($admin)->postJson('/v1/knowledge-networks', ['nombre' => 'Red ' . uniqid()])->assertStatus(201);
        $this->como($admin)->getJson('/v1/general-users')->assertOk();
        $this->como($admin)->getJson('/v1/audit-logs')->assertOk();
    }

    public function test_el_admin_gestiona_el_motor(): void
    {
        $admin = $this->usuario('admin');

        $this->como($admin)
            ->putJson('/v1/config-similitud', ['umbral' => 0.3, 'meses' => 12])
            ->assertOk();
    }
}
