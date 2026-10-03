<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Control de acceso a recursos por id: fichas, aprendices e instructores solo
// dentro del alcance del rol (evita IDOR y fuga del código de ficha).
class IdorTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Idor',
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

    // Crea una ficha con su instructor. Devuelve [ficha, instructor, usuario].
    private function ficha(TrainingProgram $programa): array
    {
        $user = $this->usuario('instructor');
        $instructor = Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $user->id,
        ]);
        $ficha = ClassGroup::create([
            'codigo' => 'f-' . uniqid(),
            'nombre' => 'Ficha ' . uniqid(),
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
        return [$ficha, $instructor, $user];
    }

    private function aprendiz(ClassGroup $ficha, TrainingProgram $programa): array
    {
        $user = $this->usuario('aprendiz');
        $aprendiz = Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $user->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $programa->id,
        ]);
        return [$aprendiz, $user];
    }

    // ---------------------------------------------------------------- Fichas

    public function test_el_aprendiz_solo_ve_su_ficha(): void
    {
        $programa = $this->programa();
        [$fichaA] = $this->ficha($programa);
        [$fichaB] = $this->ficha($programa);
        [, $userA] = $this->aprendiz($fichaA, $programa);

        $this->como($userA)->getJson('/v1/class-groups/' . $fichaA->id)->assertOk();
        $this->como($userA)->getJson('/v1/class-groups/' . $fichaB->id)->assertStatus(403);
    }

    public function test_el_instructor_solo_ve_sus_fichas(): void
    {
        $programa = $this->programa();
        [$fichaA] = $this->ficha($programa);
        [$fichaB, , $userB] = $this->ficha($programa);

        $this->como($userB)->getJson('/v1/class-groups/' . $fichaB->id)->assertOk();
        $this->como($userB)->getJson('/v1/class-groups/' . $fichaA->id)->assertStatus(403);
    }

    // ---------------------------------------------------------------- Aprendices

    public function test_el_aprendiz_ve_a_sus_companeros_de_ficha(): void
    {
        $programa = $this->programa();
        [$fichaA] = $this->ficha($programa);
        [, $userA] = $this->aprendiz($fichaA, $programa);
        [$companero] = $this->aprendiz($fichaA, $programa);

        $this->como($userA)->getJson('/v1/apprentices/' . $companero->id)->assertOk();
    }

    public function test_el_instructor_no_ve_aprendices_de_otra_ficha(): void
    {
        $programa = $this->programa();
        [$fichaA] = $this->ficha($programa);
        [$fichaB, , $userB] = $this->ficha($programa);
        [$aprendizA] = $this->aprendiz($fichaA, $programa);
        [$aprendizB] = $this->aprendiz($fichaB, $programa);

        $this->como($userB)->getJson('/v1/apprentices/' . $aprendizB->id)->assertOk();
        $this->como($userB)->getJson('/v1/apprentices/' . $aprendizA->id)->assertStatus(403);
    }

    public function test_el_instructor_no_puede_mover_un_aprendiz_ajeno(): void
    {
        $programa = $this->programa();
        [$fichaA, , $userA] = $this->ficha($programa);
        [$fichaB] = $this->ficha($programa);
        [$aprendizB] = $this->aprendiz($fichaB, $programa);

        // Intenta llevarlo a su propia ficha: 403.
        $this->como($userA)->putJson('/v1/apprentices/' . $aprendizB->id, [
            'codigo' => $aprendizB->codigo,
            'id_usuario' => $aprendizB->id_usuario,
            'id_class_group' => $fichaA->id,
        ])->assertStatus(403);

        // Y tampoco puede reasignar cuentas ajenas.
        $intruso = $this->usuario('aprendiz');
        $this->como($userA)->putJson('/v1/apprentices/' . $aprendizB->id, [
            'codigo' => $aprendizB->codigo,
            'id_usuario' => $intruso->id,
            'id_class_group' => $fichaB->id,
        ])->assertStatus(403);
    }

    // ---------------------------------------------------------------- Instructores

    public function test_el_instructor_no_ve_el_perfil_de_otro_instructor(): void
    {
        $programa = $this->programa();
        [, , $userA] = $this->ficha($programa);
        [, $instructorB, $userB] = $this->ficha($programa);

        $this->como($userB)->getJson('/v1/instructors/' . $instructorB->id)->assertOk();
        $this->como($userA)->getJson('/v1/instructors/' . $instructorB->id)->assertStatus(403);
    }

    // ---------------------------------------------------------------- Perfil

    public function test_el_perfil_oculta_el_correo_sin_vinculo_de_ficha(): void
    {
        $programa = $this->programa();
        [$fichaA] = $this->ficha($programa);
        [$fichaB] = $this->ficha($programa);
        [, $userA] = $this->aprendiz($fichaA, $programa);
        [, $userCompanero] = $this->aprendiz($fichaA, $programa);
        [, $userB] = $this->aprendiz($fichaB, $programa);

        // Compañero de ficha: correo visible.
        $this->como($userA)->getJson('/v1/general-users/' . $userCompanero->id . '/perfil')
            ->assertOk()
            ->assertJsonPath('correo', $userCompanero->correo);

        // Otra ficha: correo oculto.
        $this->como($userA)->getJson('/v1/general-users/' . $userB->id . '/perfil')
            ->assertOk()
            ->assertJsonPath('correo', null);
    }
}
