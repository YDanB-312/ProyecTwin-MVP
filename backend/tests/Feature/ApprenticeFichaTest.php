<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Notification;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Salir de la ficha y unirse a otra con el código que comparte el instructor.
class ApprenticeFichaTest extends TestCase
{
    use DatabaseTransactions;

    private int $contador = 0;

    private function red(): KnowledgeNetwork
    {
        return KnowledgeNetwork::create(['nombre' => 'Red E2E ' . uniqid()]);
    }

    private function programa(KnowledgeNetwork $red): TrainingProgram
    {
        return TrainingProgram::create([
            'nombre' => 'Programa E2E ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);
    }

    private function instructor(): Instructor
    {
        $user = GeneralUser::create([
            'nombre' => 'Instructor',
            'apellido' => 'E2E',
            'correo' => 'instructor.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => 'instructor',
            'estado' => true,
        ]);

        return Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $user->id]);
    }

    private function ficha(string $estado = 'activo', string $codigo = 'tst-aaaa'): ClassGroup
    {
        return ClassGroup::create([
            'codigo' => $codigo,
            'numero' => '9999',
            'nombre' => 'Ficha E2E ' . $codigo,
            'estado' => $estado,
            'id_programa' => $this->programa($this->red())->id,
            'id_instructor' => $this->instructor()->id,
        ]);
    }

    private function usuarioAprendiz(): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Aprendiz',
            'apellido' => 'E2E',
            'correo' => 'aprendiz.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => 'aprendiz',
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

    public function test_aprendiz_sin_fila_se_une_con_codigo_valido_y_avisa_al_instructor(): void
    {
        $ficha = $this->ficha();
        $user = $this->usuarioAprendiz();
        $token = $this->token($user);

        $this->withToken($token)
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => $ficha->codigo])
            ->assertOk()
            ->assertJsonPath('id_class_group', $ficha->id);

        $this->assertDatabaseHas('apprentices', [
            'id_usuario' => $user->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);
        $this->assertDatabaseHas('notifications', [
            'id_usuario' => $ficha->instructor->id_usuario,
            'tipo' => 'sistema',
        ]);
    }

    public function test_el_codigo_es_insensible_a_mayusculas_y_espacios(): void
    {
        $ficha = $this->ficha();
        $user = $this->usuarioAprendiz();

        $this->withToken($this->token($user))
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => '  ' . strtoupper($ficha->codigo) . '  '])
            ->assertOk();
    }

    public function test_un_codigo_inexistente_no_une(): void
    {
        $user = $this->usuarioAprendiz();

        $this->withToken($this->token($user))
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => 'no-existo'])
            ->assertStatus(404);

        $this->assertDatabaseMissing('apprentices', ['id_usuario' => $user->id]);
    }

    public function test_una_ficha_inactiva_no_acepta_nuevos_integrantes(): void
    {
        $ficha = $this->ficha('inactivo', 'tst-inac');
        $user = $this->usuarioAprendiz();

        $this->withToken($this->token($user))
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => $ficha->codigo])
            ->assertStatus(422)
            ->assertJsonPath('message', 'Esa ficha no acepta nuevos integrantes.');
    }

    public function test_no_puede_unirse_dos_veces_a_la_misma_ficha(): void
    {
        $ficha = $this->ficha();
        $user = $this->usuarioAprendiz();
        $token = $this->token($user);

        $this->withToken($token)->postJson('/v1/apprentices/me/ficha', ['codigo' => $ficha->codigo])->assertOk();
        $this->withToken($token)
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => $ficha->codigo])
            ->assertStatus(422)
            ->assertJsonPath('message', 'Ya perteneces a esta ficha.');
    }

    public function test_salir_de_la_ficha_deja_sin_ficha_y_notifica(): void
    {
        $ficha = $this->ficha();
        $user = $this->usuarioAprendiz();
        $aprendiz = Apprentice::create([
            'codigo' => 'AP-900',
            'id_usuario' => $user->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);

        $this->withToken($this->token($user))
            ->deleteJson('/v1/apprentices/me/ficha')
            ->assertOk();

        $this->assertNull($aprendiz->fresh()->id_class_group);
        $this->assertDatabaseHas('notifications', [
            'id_usuario' => $ficha->instructor->id_usuario,
            'tipo' => 'sistema',
        ]);
    }

    public function test_salir_sin_pertenecer_a_ninguna_ficha_no_hace_nada(): void
    {
        $user = $this->usuarioAprendiz();

        $this->withToken($this->token($user))
            ->deleteJson('/v1/apprentices/me/ficha')
            ->assertStatus(422);
    }

    public function test_un_aprendiz_no_puede_mover_a_otro(): void
    {
        $ficha = $this->ficha();
        $a = $this->usuarioAprendiz();
        $b = $this->usuarioAprendiz();

        $this->withToken($this->token($a))
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => $ficha->codigo])
            ->assertOk();

        // El segundo sigue sin ficha: el endpoint solo actúa sobre el token.
        $this->assertDatabaseMissing('apprentices', ['id_usuario' => $b->id]);
        $this->assertDatabaseHas('apprentices', ['id_usuario' => $a->id, 'id_class_group' => $ficha->id]);
    }

    public function test_un_instructor_no_puede_usar_los_endpoints_de_mi_ficha(): void
    {
        $ficha = $this->ficha();
        $instructorUser = GeneralUser::create([
            'nombre' => 'Instructor',
            'apellido' => 'E2E',
            'correo' => 'instructor.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => 'instructor',
            'estado' => true,
        ]);

        $this->withToken($this->token($instructorUser))
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => $ficha->codigo])
            ->assertStatus(403);
    }

    public function test_la_previsualizacion_devuelve_la_ficha_con_su_estado(): void
    {
        $ficha = $this->ficha();
        $user = $this->usuarioAprendiz();

        $this->withToken($this->token($user))
            ->getJson('/v1/apprentices/me/ficha/codigo/' . $ficha->codigo)
            ->assertOk()
            ->assertJsonPath('codigo', $ficha->codigo)
            ->assertJsonPath('estado', 'activo')
            ->assertJsonPath('apprentices_count', 0);

        $this->withToken($this->token($user))
            ->getJson('/v1/apprentices/me/ficha/codigo/no-existo')
            ->assertStatus(404);
    }

    public function test_cada_union_notifica_una_sola_vez_al_instructor(): void
    {
        $ficha = $this->ficha();
        $user = $this->usuarioAprendiz();

        $this->withToken($this->token($user))
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => $ficha->codigo])
            ->assertOk();

        $this->assertSame(1, Notification::where('id_usuario', $ficha->instructor->id_usuario)
            ->where('tipo', 'sistema')
            ->count());
    }
}
