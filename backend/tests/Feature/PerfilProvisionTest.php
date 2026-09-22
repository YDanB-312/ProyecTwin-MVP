<?php

namespace Tests\Feature;

use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingCenter;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Provisión de perfiles: una cuenta de instructor nace con su perfil (o se
// sana al usarla), y borrar un instructor con fichas responde 409.
class PerfilProvisionTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Perfil',
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

    public function test_crear_instructor_provisiona_su_perfil(): void
    {
        $admin = $this->usuario('admin');

        $respuesta = $this->withToken($this->token($admin))->postJson('/v1/general-users', [
            'nombre' => 'Nuevo',
            'apellido' => 'Docente',
            'correo' => 'docente.' . uniqid() . '@test.local',
            'password' => '123456',
            'rol' => 'instructor',
        ])->assertCreated();

        $this->assertDatabaseHas('instructors', ['id_usuario' => $respuesta->json('id')]);
    }

    public function test_un_instructor_sin_perfil_se_sana_al_consultarlo(): void
    {
        $user = $this->usuario('instructor');
        $this->assertDatabaseMissing('instructors', ['id_usuario' => $user->id]);

        $this->withToken($this->token($user))->getJson('/v1/instructors')->assertOk();

        $this->assertDatabaseHas('instructors', ['id_usuario' => $user->id]);
    }

    public function test_el_comando_de_backfill_crea_perfiles_faltantes(): void
    {
        $user = $this->usuario('instructor');
        $this->assertDatabaseMissing('instructors', ['id_usuario' => $user->id]);

        $this->artisan('perfiles:docentes')->assertSuccessful();

        $this->assertDatabaseHas('instructors', ['id_usuario' => $user->id]);
    }

    public function test_borrar_instructor_con_fichas_responde_409(): void
    {
        $admin = $this->usuario('admin');
        $instructorUser = $this->usuario('instructor');
        $instructor = Instructor::create([
            'id_usuario' => $instructorUser->id,
            'fecha_ingreso' => now()->toDateString(),
        ]);
        $centro = TrainingCenter::create(['name' => 'Centro ' . uniqid(), 'city' => 'Popayán']);

        ClassGroup::create([
            'codigo' => 'cod-' . uniqid(),
            'nombre' => 'Ficha E2E',
            'estado' => 'activo',
            'id_programa' => $this->programa()->id,
            'id_instructor' => $instructor->id,
            'training_center_id' => $centro->id,
        ]);

        $this->withToken($this->token($admin))
            ->deleteJson('/v1/instructors/' . $instructor->id)
            ->assertStatus(409);
    }
}
