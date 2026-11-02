<?php

namespace Tests\Core\Auth;

use App\Models\GeneralUser;
use App\Notifications\CorreoCambiadoNotification;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Cambio de correo del propio usuario: exige la contraseña actual y no debe
// poder hacerse desde el update genérico de perfil.
class ChangeEmailTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol = 'aprendiz'): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Correo',
            'apellido' => 'E2E',
            'correo' => 'correo.' . uniqid() . '@test.local',
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

    public function test_cambia_el_correo_con_la_contrasena_actual(): void
    {
        $user = $this->usuario();
        $nuevo = 'nuevo.' . uniqid() . '@test.local';

        $this->withToken($this->token($user))
            ->putJson('/v1/auth/email', ['correo' => $nuevo, 'password_actual' => '123456'])
            ->assertOk()
            ->assertJsonPath('correo', $nuevo);

        $this->assertSame($nuevo, $user->fresh()->correo);
    }

    public function test_una_contrasena_incorrecta_no_cambia_el_correo(): void
    {
        $user = $this->usuario();
        $original = $user->correo;

        $this->withToken($this->token($user))
            ->putJson('/v1/auth/email', [
                'correo' => 'nuevo.' . uniqid() . '@test.local',
                'password_actual' => 'incorrecta',
            ])
            ->assertStatus(422)
            ->assertJsonPath('message', 'La contraseña actual no es correcta.');

        $this->assertSame($original, $user->fresh()->correo);
    }

    public function test_correo_existente_con_password_incorrecta_no_revela_la_unicidad(): void
    {
        $user = $this->usuario();
        $otro = $this->usuario();

        $this->withToken($this->token($user))
            ->putJson('/v1/auth/email', [
                'correo' => $otro->correo,
                'password_actual' => 'incorrecta',
            ])
            ->assertStatus(422)
            ->assertJsonPath('errors.password_actual.0', 'La contraseña actual no es correcta.')
            ->assertJsonMissingPath('errors.correo');

        $this->assertSame($user->correo, $user->fresh()->correo);
    }

    public function test_no_permite_un_correo_ya_registrado(): void
    {
        $user = $this->usuario();
        $otro = $this->usuario();

        $this->withToken($this->token($user))
            ->putJson('/v1/auth/email', ['correo' => $otro->correo, 'password_actual' => '123456'])
            ->assertStatus(422);

        $this->assertSame($user->correo, $user->fresh()->correo);
    }

    public function test_rechaza_repetir_el_correo_actual(): void
    {
        $user = $this->usuario();

        $this->withToken($this->token($user))
            ->putJson('/v1/auth/email', ['correo' => $user->correo, 'password_actual' => '123456'])
            ->assertStatus(422)
            ->assertJsonPath('message', 'Ese ya es tu correo actual.');
    }

    public function test_al_cambiarlo_se_revocan_las_demas_sesiones(): void
    {
        $user = $this->usuario();
        $token1 = $this->token($user);
        $this->token($user); // segunda sesión

        $this->assertSame(2, $user->tokens()->count());

        $this->withToken($token1)
            ->putJson('/v1/auth/email', [
                'correo' => 'nuevo.' . uniqid() . '@test.local',
                'password_actual' => '123456',
            ])
            ->assertOk();

        // Solo queda viva la sesión que hizo el cambio.
        $this->assertSame(1, $user->fresh()->tokens()->count());
    }

    public function test_al_cambiarlo_rota_remember_borra_sesiones_y_avisa_al_correo_anterior(): void
    {
        Notification::fake();
        $user = $this->usuario();
        $anterior = $user->correo;
        $rememberAnterior = $user->remember_token;
        $nuevo = 'nuevo.' . uniqid() . '@test.local';

        DB::table('sessions')->insert([
            'id' => 'sesion-cambio-' . uniqid(),
            'user_id' => $user->id,
            'ip_address' => '127.0.0.1',
            'user_agent' => 'test',
            'payload' => '',
            'last_activity' => time(),
        ]);

        $this->withToken($this->token($user))
            ->putJson('/v1/auth/email', [
                'correo' => $nuevo,
                'password_actual' => '123456',
            ])
            ->assertOk();

        $this->assertNotSame($rememberAnterior, $user->fresh()->remember_token);
        $this->assertDatabaseMissing('sessions', ['user_id' => $user->id]);

        Notification::assertSentOnDemand(
            CorreoCambiadoNotification::class,
            function ($notification, $channels, $notifiable) use ($anterior) {
                return ($notifiable->routes['mail'] ?? null) === $anterior;
            }
        );
    }
}
