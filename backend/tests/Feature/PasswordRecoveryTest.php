<?php

namespace Tests\Feature;

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
            ->assertJsonPath('message', 'Si el correo está registrado, te enviamos un enlace para restablecer tu contraseña.');

        $this->assertDatabaseHas('password_reset_tokens', ['email' => $user->correo]);
        Notification::assertSentTo($user, ResetPasswordNotification::class);
    }

    public function test_solicitar_con_correo_inexistente_responde_lo_mismo(): void
    {
        Notification::fake();

        $this->postJson('/v1/auth/forgot-password', ['correo' => 'nadie@test.local'])
            ->assertOk()
            ->assertJsonPath('message', 'Si el correo está registrado, te enviamos un enlace para restablecer tu contraseña.');

        $this->assertDatabaseCount('password_reset_tokens', 0);
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
