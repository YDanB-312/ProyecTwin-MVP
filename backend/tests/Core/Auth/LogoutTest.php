<?php

namespace Tests\Core\Auth;

use App\Models\GeneralUser;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// C-09: el logout debe revocar el token Bearer actual sin invalidar los demás
// tokens del mismo usuario.
class LogoutTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Logout',
            'apellido' => 'Test',
            'correo' => 'logout.'.uniqid().'@test.local',
            'password' => Hash::make('123456'),
            'rol' => 'aprendiz',
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

    public function test_logout_revoca_el_token_actual_y_conserva_los_demas(): void
    {
        $user = $this->usuario();
        $user->forceFill(['remember_token' => 'remember-anterior'])->save();
        $tokenA = $this->token($user);

        $this->app['auth']->forgetGuards();
        $tokenB = $this->token($user);

        $this->app['auth']->forgetGuards();
        $this->withToken($tokenA)->postJson('/v1/auth/logout')->assertOk();

        $this->app['auth']->forgetGuards();
        $this->withToken($tokenA)->getJson('/v1/auth/me')->assertStatus(401);

        $this->app['auth']->forgetGuards();
        $this->withToken($tokenB)->getJson('/v1/auth/me')->assertOk();

        $this->assertNotSame('remember-anterior', $user->fresh()->remember_token);
    }
}
