<?php

namespace Tests\Feature;

use App\Models\Admin;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingCenter;
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

    private function adminDeCentro(): GeneralUser
    {
        $user = $this->usuario('admin');
        $centro = TrainingCenter::create(['name' => 'Centro ' . uniqid()]);
        Admin::create(['id_usuario' => $user->id, 'training_center_id' => $centro->id]);
        return $user;
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
    }

    public function test_el_instructor_no_accede_a_catalogos_ni_admins(): void
    {
        $instructor = $this->usuario('instructor');

        $this->como($instructor)->postJson('/v1/training-centers', ['name' => 'Centro ' . uniqid()])->assertStatus(403);
        $this->como($instructor)->getJson('/v1/admins')->assertStatus(403);
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

    public function test_el_coordinador_no_gestiona_centros_ni_admins(): void
    {
        $admin = $this->adminDeCentro();

        $this->como($admin)->postJson('/v1/training-centers', ['name' => 'Centro ' . uniqid()])->assertStatus(403);
        $this->como($admin)->getJson('/v1/admins')->assertStatus(403);
        // Sí puede ver su bitácora y sus usuarios.
        $this->como($admin)->getJson('/v1/audit-logs')->assertOk();
        $this->como($admin)->getJson('/v1/general-users')->assertOk();
    }

    public function test_el_superadmin_gestiona_centros_y_admins(): void
    {
        $superadmin = $this->usuario('superadmin');

        $this->como($superadmin)->postJson('/v1/training-centers', ['name' => 'Centro nuevo ' . uniqid()])->assertStatus(201);
        $this->como($superadmin)->getJson('/v1/admins')->assertOk();
        $this->como($superadmin)->getJson('/v1/general-users')->assertOk();
    }
}
