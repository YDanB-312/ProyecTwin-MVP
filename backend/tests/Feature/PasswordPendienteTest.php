<?php

namespace Tests\Feature;

use App\Models\GeneralUser;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Clave temporal del admin: la cuenta no puede usar la API hasta cambiarla.
class PasswordPendienteTest extends TestCase
{
    use DatabaseTransactions;

    public function test_la_clave_temporal_bloquea_la_api_hasta_cambiarla(): void
    {
        $admin = GeneralUser::create([
            'nombre' => 'Admin',
            'apellido' => 'Temp',
            'correo' => 'admin.temp.' . uniqid() . '@test.local',
            'password' => Hash::make('clave12345'),
            'rol' => 'admin',
            'estado' => true,
        ]);
        $tokenAdmin = $this->postJson('/v1/auth/login', [
            'correo' => $admin->correo,
            'password' => 'clave12345',
        ])->assertOk()->json('token');

        // El alta del admin nace con clave temporal.
        $correo = 'docente.temp.' . uniqid() . '@test.local';
        $this->withToken($tokenAdmin)->postJson('/v1/general-users', [
            'nombre' => 'Nuevo',
            'apellido' => 'Docente',
            'correo' => $correo,
            'password' => 'temporal123',
            'rol' => 'instructor',
        ])->assertCreated();

        $login = $this->postJson('/v1/auth/login', ['correo' => $correo, 'password' => 'temporal123'])
            ->assertOk();
        $this->assertTrue((bool) $login->json('debe_cambiar_password'));
        $token = $login->json('token');

        // El resto de la API queda bloqueado con un código claro.
        // (El guard cachea el usuario del request anterior: se reinicia.)
        $this->app['auth']->forgetGuards();
        $this->withToken($token)->getJson('/v1/notifications')
            ->assertStatus(403)
            ->assertJsonPath('code', 'password_pendiente');

        // Cambiar la clave lo desbloquea sin emitir token nuevo.
        $this->withToken($token)->putJson('/v1/auth/password', [
            'password_actual' => 'temporal123',
            'password' => 'nuevaClave123',
            'password_confirmation' => 'nuevaClave123',
        ])->assertOk();

        $this->app['auth']->forgetGuards();
        $this->withToken($token)->getJson('/v1/notifications')->assertOk();
    }

    public function test_una_cuenta_registrada_por_padron_no_queda_bloqueada(): void
    {
        $user = GeneralUser::create([
            'nombre' => 'Normal',
            'apellido' => 'Padron',
            'correo' => 'normal.' . uniqid() . '@test.local',
            'password' => Hash::make('clave12345'),
            'rol' => 'aprendiz',
            'estado' => true,
        ]);

        $login = $this->postJson('/v1/auth/login', ['correo' => $user->correo, 'password' => 'clave12345'])
            ->assertOk();
        $this->assertFalse((bool) $login->json('debe_cambiar_password'));

        $this->withToken($login->json('token'))->getJson('/v1/notifications')->assertOk();
    }
}
