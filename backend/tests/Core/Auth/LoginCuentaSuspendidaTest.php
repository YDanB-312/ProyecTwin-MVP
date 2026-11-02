<?php

namespace Tests\Core\Auth;

use App\Models\GeneralUser;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Contrato de login suspendido: 403 con mensaje exacto y sin token; las
// credenciales incorrectas siguen dando 422 para no revelar la suspensión.
class LoginCuentaSuspendidaTest extends TestCase
{
    use DatabaseTransactions;

    private function suspendido(): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Suspendido',
            'apellido' => 'Contrato',
            'correo' => 'suspendido.' . uniqid() . '@login.local',
            'password' => Hash::make('123456'),
            'rol' => 'aprendiz',
            'estado' => false,
        ]);
    }

    public function test_login_de_cuenta_suspendida_responde_403_con_mensaje_exacto(): void
    {
        $user = $this->suspendido();

        $res = $this->postJson('/v1/auth/login', [
            'username' => $user->username,
            'password' => '123456',
        ])->assertStatus(403)
            ->assertJsonPath('message', 'Cuenta suspendida. Contacta al administrador.');

        $res->assertJsonCount(1);
        $this->assertNull($res->json('token'));
        $this->assertSame(0, $user->tokens()->count());

        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => '123456',
        ])->assertStatus(403)
            ->assertJsonPath('message', 'Cuenta suspendida. Contacta al administrador.');
    }

    public function test_credenciales_incorrectas_de_cuenta_suspendida_siguen_dando_422(): void
    {
        $user = $this->suspendido();

        $this->postJson('/v1/auth/login', [
            'username' => $user->username,
            'password' => 'clave-incorrecta',
        ])->assertStatus(422)
            ->assertJsonPath('message', 'Credenciales incorrectas. Verifica tus datos.');
    }
}
