<?php

namespace Tests\Core\Academic;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingProgram;
use App\Notifications\ActivacionCuentaNotification;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Alta/asociación atómica de aprendices desde una ficha: el usuario existente
// solo se asocia; el nuevo se crea pendiente y recibe activación por correo.
class FichaAprendizTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'FichaA',
            'correo' => $rol . '.' . uniqid() . '@correo.com',
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

    private function ficha(): ClassGroup
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $this->usuario('instructor')->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'fa-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha aprendiz',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    public function test_asociar_un_usuario_existente_no_genera_credenciales(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $ficha = $this->ficha();
        $aprendiz = $this->usuario('aprendiz');

        $respuesta = $this->como($admin)
            ->postJson("/v1/class-groups/{$ficha->id}/aprendices", ['usuario_id' => $aprendiz->id])
            ->assertOk();

        $this->assertFalse($respuesta->json('creada'));
        $respuesta->assertJsonMissingPath('credenciales_enviadas');
        $respuesta->assertJsonMissingPath('credenciales');
        Notification::assertNothingSent();

        $this->assertDatabaseHas('apprentices', [
            'id_usuario' => $aprendiz->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);
        // La cuenta existente conserva su contraseña y su username.
        $this->assertTrue(Hash::check('123456', $aprendiz->fresh()->password));
    }

    public function test_crear_un_aprendiz_nuevo_envia_activacion_y_lo_asocia(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $ficha = $this->ficha();

        $respuesta = $this->como($admin)->postJson("/v1/class-groups/{$ficha->id}/aprendices", [
            'nombre' => 'Carlos',
            'apellido' => 'Martínez',
            'tipo_documento' => 'CC',
            'numero_documento' => (string) random_int(1000000, 9999999),
            'correo' => 'carlos.ficha.' . uniqid() . '@correo.com',
        ])->assertCreated();

        $this->assertTrue($respuesta->json('creada'));
        $respuesta->assertJsonMissingPath('credenciales_enviadas');
        $respuesta->assertJsonMissingPath('credenciales');
        $this->assertTrue($respuesta->json('activacion.enviada'));

        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $this->assertNull($user->email_verified_at);
        $this->assertDatabaseHas('account_activations', ['user_id' => $user->id]);
        Notification::assertSentTo($user, ActivacionCuentaNotification::class);

        $this->assertDatabaseHas('apprentices', [
            'id_usuario' => $user->id,
            'id_class_group' => $ficha->id,
        ]);
    }

    public function test_no_se_asocia_un_instructor_como_aprendiz(): void
    {
        $admin = $this->usuario('admin');
        $ficha = $this->ficha();
        $instructor = $this->usuario('instructor');

        $this->como($admin)
            ->postJson("/v1/class-groups/{$ficha->id}/aprendices", ['usuario_id' => $instructor->id])
            ->assertStatus(422);
    }

    public function test_un_documento_duplicado_falla(): void
    {
        $admin = $this->usuario('admin');
        $ficha = $this->ficha();
        $existente = $this->usuario('aprendiz');
        $existente->update(['numero_documento' => '99887766']);

        $this->como($admin)->postJson("/v1/class-groups/{$ficha->id}/aprendices", [
            'nombre' => 'Otro',
            'apellido' => 'Aprendiz',
            'tipo_documento' => 'CC',
            'numero_documento' => '99887766',
            'correo' => 'otro.' . uniqid() . '@correo.com',
        ])->assertStatus(422);
    }

    public function test_el_instructor_no_recibe_estado_al_asociar_un_aprendiz(): void
    {
        $ficha = $this->ficha();
        $instructor = $ficha->instructor->generalUser;
        $aprendiz = $this->usuario('aprendiz');

        $respuesta = $this->como($instructor)
            ->postJson("/v1/class-groups/{$ficha->id}/aprendices", ['usuario_id' => $aprendiz->id])
            ->assertOk();

        $this->assertSame($aprendiz->correo, $respuesta->json('usuario.correo'));
        $this->assertArrayNotHasKey('estado', $respuesta->json('usuario'));
    }

    public function test_la_previsualizacion_por_codigo_no_expone_estado_del_instructor(): void
    {
        $ficha = $this->ficha();
        $aprendiz = $this->usuario('aprendiz');

        $respuesta = $this->como($aprendiz)
            ->getJson('/v1/apprentices/me/ficha/codigo/' . strtolower($ficha->codigo))
            ->assertOk();

        $this->assertSame($ficha->nombre, $respuesta->json('nombre'));
        $this->assertArrayNotHasKey('correo', $respuesta->json('instructor.generalUser'));
        $this->assertArrayNotHasKey('estado', $respuesta->json('instructor.generalUser'));
    }

    public function test_unirse_a_ficha_no_expone_contacto_ni_estado_del_instructor(): void
    {
        Notification::fake();
        $ficha = $this->ficha();
        $aprendiz = $this->usuario('aprendiz');

        $respuesta = $this->como($aprendiz)
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => strtolower($ficha->codigo)])
            ->assertOk();

        $this->assertSame($ficha->id, $respuesta->json('id_class_group'));
        $this->assertSame(
            $ficha->instructor->generalUser->nombre,
            $respuesta->json('classGroup.instructor.generalUser.nombre')
        );
        $this->assertArrayNotHasKey('correo', $respuesta->json('classGroup.instructor.generalUser'));
        $this->assertArrayNotHasKey('estado', $respuesta->json('classGroup.instructor.generalUser'));
    }

    public function test_un_instructor_ajeno_no_agrega_aprendices(): void
    {
        $ficha = $this->ficha();
        $ajeno = $this->usuario('instructor');
        $aprendiz = $this->usuario('aprendiz');

        $this->como($ajeno)
            ->postJson("/v1/class-groups/{$ficha->id}/aprendices", ['usuario_id' => $aprendiz->id])
            ->assertStatus(403);
    }
}
