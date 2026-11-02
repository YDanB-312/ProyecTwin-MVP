<?php

namespace Tests\Core\Auth;

use App\Models\GeneralUser;
use App\Notifications\ResetPasswordNotification;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Recuperación de contraseña por correo con token (broker de Laravel).
class PasswordRecoveryTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Reset',
            'apellido' => 'E2E',
            'correo' => 'reset.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => 'aprendiz',
            'estado' => true,
        ]);
    }

    public function test_solicitar_con_correo_existente_responde_generico_y_crea_token(): void
    {
        Notification::fake();
        $user = $this->usuario();

        $this->postJson('/v1/auth/forgot-password', ['correo' => $user->correo])
            ->assertOk()
            ->assertJsonPath('message', 'Si el correo está registrado, te enviamos un enlace para activar tu cuenta o restablecer tu contraseña.');

        $this->assertDatabaseHas('password_reset_tokens', ['email' => $user->correo]);
        Notification::assertSentTo($user, ResetPasswordNotification::class);
    }

    public function test_el_enlace_de_reset_lleva_token_y_correo_en_el_fragmento(): void
    {
        Notification::fake();
        $user = $this->usuario();

        $this->postJson('/v1/auth/forgot-password', ['correo' => $user->correo])->assertOk();

        Notification::assertSentTo($user, ResetPasswordNotification::class, function ($n) use ($user) {
            $url = $n->toMail($user)->actionUrl;

            $this->assertStringContainsString('/restablecer-contrasena#', $url);
            $this->assertStringContainsString('token=', $url);
            $this->assertStringNotContainsString('?token=', $url);

            return true;
        });
    }

    public function test_solicitar_con_correo_inexistente_responde_lo_mismo(): void
    {
        Notification::fake();

        $this->postJson('/v1/auth/forgot-password', ['correo' => 'nadie@test.local'])
            ->assertOk()
            ->assertJsonPath('message', 'Si el correo está registrado, te enviamos un enlace para activar tu cuenta o restablecer tu contraseña.');

        $this->assertDatabaseCount('password_reset_tokens', 0);
    }

    public function test_si_el_mailer_falla_la_respuesta_sigue_siendo_generica_y_no_deja_token(): void
    {
        $user = $this->usuario();

        config([
            'mail.default' => 'smtp',
            'mail.mailers.smtp.host' => '127.0.0.1',
            'mail.mailers.smtp.port' => 1,
            'mail.mailers.smtp.encryption' => null,
            'mail.mailers.smtp.username' => null,
            'mail.mailers.smtp.password' => null,
            'mail.mailers.smtp.timeout' => 1,
        ]);

        $this->postJson('/v1/auth/forgot-password', ['correo' => $user->correo])
            ->assertOk()
            ->assertJsonPath('message', 'Si el correo está registrado, te enviamos un enlace para activar tu cuenta o restablecer tu contraseña.');

        $this->assertDatabaseMissing('password_reset_tokens', ['email' => $user->correo]);
    }

    public function test_reset_con_token_valido_cambia_la_contrasena_y_lo_consume(): void
    {
        Notification::fake();
        $user = $this->usuario();

        $this->postJson('/v1/auth/forgot-password', ['correo' => $user->correo])->assertOk();

        $token = null;
        Notification::assertSentTo($user, ResetPasswordNotification::class, function ($n) use (&$token) {
            $token = $n->token;
            return true;
        });
        $this->assertNotNull($token);

        $payload = [
            'correo' => $user->correo,
            'token' => $token,
            'password' => 'nuevaClave123',
            'password_confirmation' => 'nuevaClave123',
        ];

        $this->postJson('/v1/auth/reset-password', $payload)->assertOk();
        $this->assertTrue(Hash::check('nuevaClave123', $user->fresh()->password));

        // Puede iniciar sesión con la nueva.
        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => 'nuevaClave123',
        ])->assertOk();

        // El token ya se consumió.
        $this->postJson('/v1/auth/reset-password', $payload)->assertStatus(422);
    }

    public function test_reset_con_token_invalido_falla(): void
    {
        $user = $this->usuario();

        $this->postJson('/v1/auth/reset-password', [
            'correo' => $user->correo,
            'token' => 'token-falso',
            'password' => 'nuevaClave123',
            'password_confirmation' => 'nuevaClave123',
        ])->assertStatus(422);

        $this->assertTrue(Hash::check('123456', $user->fresh()->password));
    }

    public function test_token_invalido_no_revela_si_la_password_coincide_con_la_actual(): void
    {
        $user = $this->usuario();
        $user->forceFill(['password' => Hash::make('claveActual123')])->save();

        // Antes de la corrección, esta petición devolvía un mensaje distinto
        // ("debe ser distinta a la actual") y permitía adivinar la contraseña.
        $this->postJson('/v1/auth/reset-password', [
            'correo' => $user->correo,
            'token' => 'token-falso',
            'password' => 'claveActual123',
            'password_confirmation' => 'claveActual123',
        ])->assertStatus(422)
          ->assertJsonPath('message', 'El enlace es inválido o venció. Solicita uno nuevo.');

        $this->assertTrue(Hash::check('claveActual123', $user->fresh()->password));
    }

    public function test_reset_exige_confirmacion_de_la_contrasena(): void
    {
        $user = $this->usuario();

        $this->postJson('/v1/auth/reset-password', [
            'correo' => $user->correo,
            'token' => 'cualquiera',
            'password' => 'nuevaClave123',
            'password_confirmation' => 'otraClave123',
        ])->assertStatus(422);
    }
}
