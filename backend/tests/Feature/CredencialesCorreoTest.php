<?php

namespace Tests\Feature;

use App\Models\GeneralUser;
use App\Notifications\CredencialesUsuario;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Envío de credenciales al correo personal: al crear la cuenta y al reenviar
// (solo mientras la contraseña siga siendo temporal). Si el correo falla, la
// cuenta se conserva y queda registrado el error.
class CredencialesCorreoTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Correo',
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

    private function alta(GeneralUser $admin, string $rol = 'aprendiz')
    {
        return $this->como($admin)->postJson('/v1/general-users', [
            'nombre' => 'Nuevo',
            'apellido' => 'Correo',
            'tipo_documento' => 'CC',
            'numero_documento' => (string) random_int(1000000, 9999999),
            'correo' => 'nuevo.' . uniqid() . '@correo.com',
            'rol' => $rol,
        ]);
    }

    public function test_crear_usuario_envia_credenciales_al_correo(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');

        $respuesta = $this->alta($admin)->assertCreated();
        $this->assertTrue($respuesta->json('credenciales.enviadas'));

        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        Notification::assertSentTo($user, CredencialesUsuario::class);
        $this->assertNotNull($user->credenciales_enviadas_en);
        $this->assertNull($user->credenciales_error);
    }

    public function test_si_el_correo_falla_la_cuenta_se_conserva(): void
    {
        // SMTP a un puerto cerrado: el envío falla, el alta no se revierte.
        config([
            'mail.default' => 'smtp',
            'mail.mailers.smtp.host' => '127.0.0.1',
            'mail.mailers.smtp.port' => 1,
            'mail.mailers.smtp.timeout' => 2,
        ]);
        $admin = $this->usuario('admin');

        $respuesta = $this->alta($admin)->assertCreated();
        $this->assertFalse($respuesta->json('credenciales.enviadas'));

        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $this->assertNotNull($user->credenciales_error);
        $this->assertNull($user->credenciales_enviadas_en);
        // La cuenta sigue existiendo y con su contraseña temporal.
        $this->assertTrue($user->must_change_password);
    }

    public function test_reenviar_credenciales_mientras_siguen_vigentes(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $creada = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($creada->json('usuario.id'));

        $this->como($admin)
            ->postJson('/v1/general-users/' . $user->id . '/credenciales/reenviar')
            ->assertOk();

        Notification::assertSentTo($user, CredencialesUsuario::class);
        $this->assertDatabaseHas('audit_logs', [
            'accion' => 'reenviar_credenciales',
            'entidad_id' => $user->id,
        ]);
    }

    public function test_no_se_reenvian_tras_cambiar_la_contrasena(): void
    {
        $admin = $this->usuario('admin');
        $creada = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($creada->json('usuario.id'));

        // El usuario ya definió su contraseña definitiva.
        $user->update(['must_change_password' => false, 'password_temporal' => null]);

        $this->como($admin)
            ->postJson('/v1/general-users/' . $user->id . '/credenciales/reenviar')
            ->assertStatus(422);
    }

    public function test_solo_un_admin_reenvia_credenciales(): void
    {
        $admin = $this->usuario('admin');
        $creada = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($creada->json('usuario.id'));
        $aprendiz = $this->usuario('aprendiz');

        $this->como($aprendiz)
            ->postJson('/v1/general-users/' . $user->id . '/credenciales/reenviar')
            ->assertStatus(403);
    }
}
