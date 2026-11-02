<?php

namespace Tests\Core\Auth;

use App\Models\GeneralUser;
use App\Notifications\ResetPasswordNotification;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Política de contraseñas y regresión del recorte de `password_actual`.
class PasswordPolicyTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $password = 'claveActual123'): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Clave',
            'apellido' => 'Política',
            'correo' => 'clave.' . uniqid() . '@test.local',
            'password' => Hash::make($password),
            'rol' => 'aprendiz',
            'estado' => true,
        ]);
    }

    private function token(GeneralUser $usuario, string $password = 'claveActual123'): string
    {
        return $this->postJson('/v1/auth/login', [
            'correo' => $usuario->correo,
            'password' => $password,
        ])->assertOk()->json('token');
    }

    public function test_password_de_11_falla_y_de_12_funciona(): void
    {
        $user = $this->usuario();
        $token = $this->token($user);

        $this->withToken($token)->putJson('/v1/auth/password', [
            'password_actual' => 'claveActual123',
            'password' => '12345678901',
            'password_confirmation' => '12345678901',
        ])->assertStatus(422);

        $this->withToken($token)->putJson('/v1/auth/password', [
            'password_actual' => 'claveActual123',
            'password' => 'Frase larga 1',
            'password_confirmation' => 'Frase larga 1',
        ])->assertOk();

        $this->assertTrue(Hash::check('Frase larga 1', $user->fresh()->password));
    }

    public function test_password_con_espacios_en_extremos_funciona_en_login_y_cambio(): void
    {
        $password = '  clave con espacios  ';
        $user = $this->usuario($password);
        $token = $this->token($user, $password);

        $this->withToken($token)->putJson('/v1/auth/password', [
            'password_actual' => $password,
            'password' => 'Frase nueva 2026',
            'password_confirmation' => 'Frase nueva 2026',
        ])->assertOk();

        $this->assertTrue(Hash::check('Frase nueva 2026', $user->fresh()->password));
    }

    public function test_permite_frases_largas_con_espacios_sin_exigir_complejidad(): void
    {
        $user = $this->usuario();
        $token = $this->token($user);

        $this->withToken($token)->putJson('/v1/auth/password', [
            'password_actual' => 'claveActual123',
            'password' => 'mi frase segura con espacios',
            'password_confirmation' => 'mi frase segura con espacios',
        ])->assertOk();

        $this->assertTrue(Hash::check('mi frase segura con espacios', $user->fresh()->password));
    }

    public function test_rechaza_password_comun_de_12_caracteres(): void
    {
        $user = $this->usuario();
        $token = $this->token($user);

        $this->withToken($token)->putJson('/v1/auth/password', [
            'password_actual' => 'claveActual123',
            'password' => 'password1234',
            'password_confirmation' => 'password1234',
        ])->assertStatus(422);

        $this->assertTrue(Hash::check('claveActual123', $user->fresh()->password));
    }

    public function test_reset_tambien_exige_12_y_permite_frase(): void
    {
        Notification::fake();
        $user = $this->usuario();

        $this->postJson('/v1/auth/forgot-password', ['correo' => $user->correo])->assertOk();
        $token = null;
        Notification::assertSentTo($user, ResetPasswordNotification::class, function ($n) use (&$token) {
            $token = $n->token;
            return true;
        });

        $this->postJson('/v1/auth/reset-password', [
            'correo' => $user->correo,
            'token' => $token,
            'password' => '12345678901',
            'password_confirmation' => '12345678901',
        ])->assertStatus(422);

        $this->postJson('/v1/auth/reset-password', [
            'correo' => $user->correo,
            'token' => $token,
            'password' => 'frase de recuperación segura',
            'password_confirmation' => 'frase de recuperación segura',
        ])->assertOk();

        $this->assertTrue(Hash::check('frase de recuperación segura', $user->fresh()->password));
    }
}
