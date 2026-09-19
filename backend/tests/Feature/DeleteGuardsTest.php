<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Guardas de borrado: no se destruye historial académico; se suspende/archiva.
// Y autoprotección del admin: sin dejar el sistema sin administradores.
class DeleteGuardsTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol, bool $estado = true): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Guarda',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => $estado,
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

    private function ficha(?string $codigo = null): ClassGroup
    {
        return ClassGroup::create([
            'codigo' => $codigo ?: 'gd-' . uniqid(),
            'nombre' => 'Ficha guarda',
            'estado' => 'activo',
            'id_programa' => $this->programa()->id,
        ]);
    }

    public function test_una_ficha_con_aprendices_no_se_borra(): void
    {
        $admin = $this->usuario('admin');
        $ficha = $this->ficha();
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $this->usuario('aprendiz')->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);

        $this->como($admin)
            ->deleteJson('/v1/class-groups/' . $ficha->id)
            ->assertStatus(409);

        $this->assertDatabaseHas('class_groups', ['id' => $ficha->id]);
    }

    public function test_una_ficha_con_propuestas_no_se_borra(): void
    {
        $admin = $this->usuario('admin');
        $ficha = $this->ficha();
        Project::create([
            'titulo' => 'Propuesta en ficha',
            'resumen' => 'Resumen',
            'area_aplicacion' => 'Tecnología',
            'id_creador' => $admin->id,
            'id_class_group' => $ficha->id,
        ]);

        $this->como($admin)
            ->deleteJson('/v1/class-groups/' . $ficha->id)
            ->assertStatus(409);
    }

    public function test_una_ficha_vacia_si_se_borra(): void
    {
        $admin = $this->usuario('admin');
        $ficha = $this->ficha();

        $this->como($admin)
            ->deleteJson('/v1/class-groups/' . $ficha->id)
            ->assertOk();

        $this->assertDatabaseMissing('class_groups', ['id' => $ficha->id]);
    }

    public function test_un_usuario_con_propuestas_creadas_no_se_borra(): void
    {
        $admin = $this->usuario('admin');
        $autor = $this->usuario('aprendiz');
        Project::create([
            'titulo' => 'Su propuesta',
            'resumen' => 'Resumen',
            'area_aplicacion' => 'Tecnología',
            'id_creador' => $autor->id,
        ]);

        $this->como($admin)
            ->deleteJson('/v1/general-users/' . $autor->id)
            ->assertStatus(409);

        $this->assertDatabaseHas('general_users', ['id' => $autor->id]);
    }

    public function test_un_instructor_con_fichas_no_se_borra(): void
    {
        $admin = $this->usuario('admin');
        $instructorUser = $this->usuario('instructor');
        $instructor = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $instructorUser->id]);
        $ficha = $this->ficha();
        $ficha->update(['id_instructor' => $instructor->id]);

        $this->como($admin)
            ->deleteJson('/v1/general-users/' . $instructorUser->id)
            ->assertStatus(409);
    }

    public function test_un_usuario_sin_historial_si_se_borra(): void
    {
        $admin = $this->usuario('admin');
        $nuevo = $this->usuario('aprendiz');

        $this->como($admin)
            ->deleteJson('/v1/general-users/' . $nuevo->id)
            ->assertOk();

        $this->assertDatabaseMissing('general_users', ['id' => $nuevo->id]);
    }

    public function test_nadie_borra_su_propia_cuenta(): void
    {
        $admin = $this->usuario('admin');

        $this->como($admin)
            ->deleteJson('/v1/general-users/' . $admin->id)
            ->assertStatus(422);

        $this->assertDatabaseHas('general_users', ['id' => $admin->id]);
    }

    public function test_no_se_puede_suspender_al_ultimo_admin_activo(): void
    {
        // Deja un solo admin activo en el escenario (se revierte al terminar).
        GeneralUser::where('rol', 'admin')->update(['estado' => false]);
        $admin = $this->usuario('admin', true);

        $this->como($admin)
            ->putJson('/v1/general-users/' . $admin->id, [
                'nombre' => $admin->nombre,
                'apellido' => $admin->apellido,
                'correo' => $admin->correo,
                'estado' => false,
            ])
            ->assertStatus(409);

        $this->assertTrue($admin->fresh()->estado);
    }
}
