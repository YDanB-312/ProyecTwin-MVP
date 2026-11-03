<?php

namespace Tests\Core\Auth;

use App\Models\GeneralUser;
use App\Notifications\ActivacionCuentaNotification;
use App\Services\ActivacionCuenta;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Activación de cuentas nuevas: alta pendiente, token de un solo uso,
// vencimiento, reenvío y compatibilidad con cuentas existentes.
class ActivacionCuentaTest extends TestCase
{
    use DatabaseTransactions;

    private const PASSWORD = 'Frase segura 2026';

    private function usuario(string $rol = 'aprendiz', array $extra = []): GeneralUser
    {
        return GeneralUser::create(array_merge([
            'nombre' => ucfirst($rol),
            'apellido' => 'Activación',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('claveActual123'),
            'rol' => $rol,
            'estado' => true,
        ], $extra));
    }

    private function tokenDe(GeneralUser $usuario): string
    {
        return $this->postJson('/v1/auth/login', [
            'correo' => $usuario->correo,
            'password' => 'claveActual123',
        ])->assertOk()->json('token');
    }

    private function como(GeneralUser $usuario)
    {
        $token = $this->tokenDe($usuario);
        $this->app['auth']->forgetGuards();

        return $this->withToken($token);
    }

    private function alta(GeneralUser $admin, array $extra = [])
    {
        return $this->como($admin)->postJson('/v1/general-users', array_merge([
            'nombre' => 'Nuevo',
            'apellido' => 'Activación',
            'tipo_documento' => 'CC',
            'numero_documento' => (string) random_int(1000000, 9999999),
            'correo' => 'nuevo.' . uniqid() . '@test.local',
            'rol' => 'aprendiz',
        ], $extra));
    }

    public function test_alta_nueva_crea_cuenta_pendiente_y_envia_activacion(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');

        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));

        $this->assertTrue((bool) $respuesta->json('activacion.enviada'));
        $respuesta->assertJsonMissingPath('credenciales');
        $this->assertNull($user->email_verified_at);
        $this->assertDatabaseHas('account_activations', ['user_id' => $user->id]);
        Notification::assertSentTo($user, ActivacionCuentaNotification::class);
    }

    public function test_el_enlace_de_activacion_lleva_el_token_en_el_fragmento(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));

        Notification::assertSentTo($user, ActivacionCuentaNotification::class, function ($n) use ($user) {
            $url = $n->toMail($user)->actionUrl;

            $this->assertStringContainsString('/activar-cuenta#token=', $url);
            $this->assertStringNotContainsString('?token=', $url);

            return true;
        });
    }

    public function test_mostrar_activacion_enmascara_el_correo_y_no_consume_el_token(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $token = app(ActivacionCuenta::class)->crearToken($user);

        $mostrar = $this->postJson('/v1/auth/activation/validate', ['token' => $token])->assertOk();

        // La ruta por path ya no existe: el token no debe viajar en la URL.
        $this->getJson('/v1/auth/activation/' . $token)->assertNotFound();

        $this->assertStringContainsString('*', $mostrar->json('correo'));
        $this->assertStringNotContainsString($user->correo, $mostrar->json('correo'));

        // El token sigue vigente para el POST.
        $this->postJson('/v1/auth/activation', [
            'token' => $token,
            'password' => self::PASSWORD,
            'password_confirmation' => self::PASSWORD,
        ])->assertOk();
    }

    public function test_activacion_valida_guarda_password_y_permite_login_por_correo(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $token = app(ActivacionCuenta::class)->crearToken($user);

        $this->postJson('/v1/auth/activation', [
            'token' => $token,
            'password' => self::PASSWORD,
            'password_confirmation' => self::PASSWORD,
        ])->assertOk();

        $fresco = $user->fresh();
        $this->assertNotNull($fresco->email_verified_at);
        $this->assertTrue(Hash::check(self::PASSWORD, $fresco->password));

        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => self::PASSWORD,
        ])->assertOk();

        // El token ya se consumió.
        $this->postJson('/v1/auth/activation', [
            'token' => $token,
            'password' => self::PASSWORD,
            'password_confirmation' => self::PASSWORD,
        ])->assertStatus(422);
    }

    public function test_activacion_vencida_no_cambia_la_cuenta(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $token = app(ActivacionCuenta::class)->crearToken($user);

        $user->accountActivation->forceFill(['expires_at' => now()->subMinute()])->save();

        $this->postJson('/v1/auth/activation', [
            'token' => $token,
            'password' => self::PASSWORD,
            'password_confirmation' => self::PASSWORD,
        ])->assertStatus(422);

        $this->assertNull($user->fresh()->email_verified_at);
    }

    public function test_activacion_con_token_invalido_usa_mensaje_generico(): void
    {
        $this->postJson('/v1/auth/activation', [
            'token' => 'token-invalido',
            'password' => self::PASSWORD,
            'password_confirmation' => self::PASSWORD,
        ])->assertStatus(422)
          ->assertJsonPath('message', 'El enlace de activación es inválido o venció. Solicita uno nuevo al administrador.');
    }

    public function test_no_activa_una_cuenta_suspendida(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $token = app(ActivacionCuenta::class)->crearToken($user);

        $user->update(['estado' => false]);

        $this->postJson('/v1/auth/activation', [
            'token' => $token,
            'password' => self::PASSWORD,
            'password_confirmation' => self::PASSWORD,
        ])->assertStatus(422);

        $this->assertNull($user->fresh()->email_verified_at);
    }

    public function test_suspender_una_cuenta_pendiente_borra_su_activacion(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));

        $this->assertDatabaseHas('account_activations', ['user_id' => $user->id]);

        $this->withToken($this->tokenDe($admin))
            ->putJson('/v1/general-users/' . $user->id, ['estado' => false])
            ->assertOk();

        $this->assertFalse((bool) $user->fresh()->estado);
        $this->assertDatabaseMissing('account_activations', ['user_id' => $user->id]);
    }

    public function test_reenvio_invalida_el_token_anterior(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $tokenViejo = app(ActivacionCuenta::class)->crearToken($user);
        $hashViejo = hash('sha256', $tokenViejo);

        $this->como($admin)
            ->postJson('/v1/general-users/' . $user->id . '/activacion/reenviar')
            ->assertOk();

        $this->assertNotSame($hashViejo, $user->accountActivation()->value('token_hash'));

        $this->postJson('/v1/auth/activation', [
            'token' => $tokenViejo,
            'password' => self::PASSWORD,
            'password_confirmation' => self::PASSWORD,
        ])->assertStatus(422);
    }

    public function test_login_pendiente_responde_403_sin_crear_sesion(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $user->forceFill(['password' => Hash::make('claveActual123')])->save();

        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => 'claveActual123',
        ])->assertStatus(403)
          ->assertJsonPath('code', 'cuenta_no_activada');
    }

    public function test_token_previo_de_una_cuenta_pendiente_no_sirve_y_se_revoca_al_activar(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $tokenPrevio = $user->createToken('previo')->plainTextToken;

        $this->assertNull($user->email_verified_at);

        $this->app['auth']->forgetGuards();
        $this->withToken($tokenPrevio)->getJson('/v1/auth/me')
            ->assertStatus(403)
            ->assertJsonPath('code', 'cuenta_no_activada');

        $tokenActivacion = app(ActivacionCuenta::class)->crearToken($user);
        $this->postJson('/v1/auth/activation', [
            'token' => $tokenActivacion,
            'password' => self::PASSWORD,
            'password_confirmation' => self::PASSWORD,
        ])->assertOk();

        $this->assertSame(0, $user->fresh()->tokens()->count());

        $this->app['auth']->forgetGuards();
        $this->withToken($tokenPrevio)->getJson('/v1/auth/me')->assertStatus(401);
    }

    public function test_cuentas_existentes_entran_por_correo_y_el_username_ya_no_autentica(): void
    {
        $user = $this->usuario('aprendiz');

        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => 'claveActual123',
        ])->assertOk();

        $this->postJson('/v1/auth/login', [
            'username' => strtolower((string) $user->username),
            'password' => 'claveActual123',
        ])->assertStatus(422)
            ->assertJsonValidationErrors('username');
    }

    public function test_si_el_correo_de_activacion_falla_la_cuenta_queda_pendiente_y_registra_el_error(): void
    {
        $admin = $this->usuario('admin');

        config([
            'mail.default' => 'smtp',
            'mail.mailers.smtp.host' => '127.0.0.1',
            'mail.mailers.smtp.port' => 1,
            'mail.mailers.smtp.encryption' => null,
            'mail.mailers.smtp.username' => null,
            'mail.mailers.smtp.password' => null,
            'mail.mailers.smtp.timeout' => 1,
        ]);

        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));

        $this->assertFalse((bool) $respuesta->json('activacion.enviada'));
        $this->assertNull($user->email_verified_at);
        $this->assertNotNull($user->accountActivation);
        $this->assertNotNull($user->accountActivation->error);
    }

    public function test_activacion_rechaza_password_corta_y_conserva_el_token(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $respuesta = $this->alta($admin)->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $token = app(ActivacionCuenta::class)->crearToken($user);

        $this->postJson('/v1/auth/activation', [
            'token' => $token,
            'password' => 'corta123',
            'password_confirmation' => 'corta123',
        ])->assertStatus(422);

        $this->assertNull($user->fresh()->email_verified_at);

        $this->postJson('/v1/auth/activation', [
            'token' => $token,
            'password' => self::PASSWORD,
            'password_confirmation' => self::PASSWORD,
        ])->assertOk();
    }

    public function test_el_envio_conserva_la_expiracion_de_la_activacion(): void
    {
        Notification::fake();

        $usuario = GeneralUser::create([
            'nombre' => 'Expiracion',
            'apellido' => 'Activacion',
            'correo' => 'expiracion.' . uniqid() . '@example.com',
            'password' => Hash::make('claveDescartable#2026'),
            'rol' => 'aprendiz',
            'estado' => true,
            'email_verified_at' => null,
        ]);

        $resultado = app(ActivacionCuenta::class)->crearYEnviar($usuario);

        $this->assertTrue($resultado['enviado']);
        Notification::assertSentTo($usuario, ActivacionCuentaNotification::class);

        $activacion = $usuario->accountActivation()->firstOrFail();

        $this->assertTrue(
            $activacion->expires_at->greaterThan(now()->addMinutes(23 * 60)),
            'La expiración debe seguir siendo de 24 horas después del envío.'
        );
    }
}
