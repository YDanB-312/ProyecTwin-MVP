<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingProgram;
use App\Notifications\CredencialesUsuario;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Alta/asociación atómica de aprendices desde una ficha: el usuario existente
// solo se asocia (sin credenciales); el nuevo se crea con username + temporal,
// se asocia y recibe las credenciales en su correo personal.
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
        $this->assertFalse($respuesta->json('credenciales_enviadas'));
        Notification::assertNothingSent();

        $this->assertDatabaseHas('apprentices', [
            'id_usuario' => $aprendiz->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);
        // La cuenta existente conserva su contraseña y su username.
        $this->assertFalse($aprendiz->fresh()->must_change_password);
    }

    public function test_crear_un_aprendiz_nuevo_envia_credenciales_y_lo_asocia(): void
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
        $this->assertTrue($respuesta->json('credenciales_enviadas'));
        $this->assertMatchesRegularExpression('/^CaMz_[A-Za-z0-9]{5}$/', $respuesta->json('credenciales.username'));

        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $this->assertTrue($user->must_change_password);
        Notification::assertSentTo($user, CredencialesUsuario::class);

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
