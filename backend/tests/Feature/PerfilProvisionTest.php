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

// Provisión de perfiles: una cuenta de instructor nace con su perfil (verificado
// si la crea un admin), la API no auto-crea perfiles al consultar, y borrar un
// instructor con fichas lo hace en cascada.
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
            'tipo_documento' => 'CC',
            'numero_documento' => (string) random_int(1000000, 9999999),
            'correo' => 'docente.' . uniqid() . '@test.local',
            'rol' => 'instructor',
        ])->assertCreated();

        $this->assertDatabaseHas('general_users', [
            'id' => $respuesta->json('usuario.id'),
            'rol' => 'instructor',
            'must_change_password' => true,
        ]);
        $this->assertDatabaseHas('instructors', ['id_usuario' => $respuesta->json('usuario.id')]);
    }

    public function test_un_instructor_sin_perfil_no_se_crea_al_consultarlo(): void
    {
        $user = $this->usuario('instructor');
        $this->assertDatabaseMissing('instructors', ['id_usuario' => $user->id]);

        $this->withToken($this->token($user))->getJson('/v1/instructors')->assertOk();

        // La API no auto-crea perfiles: el alta del admin los provisiona.
        $this->assertDatabaseMissing('instructors', ['id_usuario' => $user->id]);
    }

    public function test_el_comando_de_backfill_crea_perfiles_faltantes(): void
    {
        $user = $this->usuario('instructor');
        $this->assertDatabaseMissing('instructors', ['id_usuario' => $user->id]);

        $this->artisan('perfiles:docentes')->assertSuccessful();

        $this->assertDatabaseHas('instructors', ['id_usuario' => $user->id]);
    }

    public function test_borrar_instructor_con_fichas_borra_en_cascada(): void
    {
        $admin = $this->usuario('admin');
        $instructorUser = $this->usuario('instructor');
        $instructor = Instructor::create([
            'id_usuario' => $instructorUser->id,
            'fecha_ingreso' => now()->toDateString(),
        ]);
        $ficha = ClassGroup::create([
            'codigo' => 'cod-' . uniqid(),
            'nombre' => 'Ficha E2E',
            'estado' => 'activo',
            'id_programa' => $this->programa()->id,
            'id_instructor' => $instructor->id,
        ]);

        // Admin sin restricciones: borra el instructor y su ficha en cascada.
        $this->withToken($this->token($admin))
            ->deleteJson('/v1/instructors/' . $instructor->id)
            ->assertOk();

        $this->assertDatabaseMissing('instructors', ['id' => $instructor->id]);
        $this->assertDatabaseMissing('class_groups', ['id' => $ficha->id]);
    }
}
