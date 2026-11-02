<?php

namespace Tests\Core\Auth;

use App\Models\GeneralUser;
use App\Notifications\ResetPasswordNotification;
use App\Support\SesionesUsuario;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Invalidación de sesiones, tokens Bearer, remember_token y enlaces de
// recuperación en los tres flujos críticos.
class SesionesInvalidacionTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Sesión',
            'apellido' => 'Invalidación',
            'correo' => 'sesion.' . uniqid() . '@test.local',
            'password' => Hash::make('claveActual123'),
            'rol' => 'aprendiz',
            'estado' => true,
        ]);
    }

    private function token(GeneralUser $usuario): string
    {
        return $this->postJson('/v1/auth/login', [
            'correo' => $usuario->correo,
            'password' => 'claveActual123',
        ])->assertOk()->json('token');
    }

    private function origenStateful(): string
    {
        $dominio = trim((string) (config('sanctum.stateful')[0] ?? 'localhost'));

        return 'http://' . ltrim($dominio, '/');
    }

    private function loginConSesion(GeneralUser $usuario)
    {
        return $this->withHeader('Origin', $this->origenStateful())
            ->postJson('/v1/auth/login', [
                'correo' => $usuario->correo,
                'password' => 'claveActual123',
            ]);
    }

    private function cookieDe($response): string
    {
        $cookie = $response->getCookie(config('session.cookie'), false);
        $this->assertNotNull($cookie);

        return $cookie->getValue();
    }

    private function insertarSesiones(GeneralUser $usuario): void
    {
        foreach (['s1', 's2'] as $id) {
            DB::table('sessions')->insert([
                'id' => $id . '-' . uniqid(),
                'user_id' => $usuario->id,
                'ip_address' => '127.0.0.1',
                'user_agent' => 'test',
                'payload' => '',
                'last_activity' => time(),
            ]);
        }
    }

    private function insertarResetToken(GeneralUser $usuario): void
    {
        DB::table('password_reset_tokens')->updateOrInsert(
            ['email' => $usuario->correo],
            ['token' => Hash::make('token-de-prueba'), 'created_at' => now()]
        );
    }

    public function test_cambiar_password_conserva_token_actual_y_revoca_los_demas(): void
    {
        $user = $this->usuario();
        $tokenActual = $this->token($user);
        $tokenOtro = $this->token($user);
        $rememberAnterior = $user->fresh()->remember_token;
        $this->insertarSesiones($user);
        $this->insertarResetToken($user);

        $this->withToken($tokenActual)->putJson('/v1/auth/password', [
            'password_actual' => 'claveActual123',
            'password' => 'Frase nueva 2026',
            'password_confirmation' => 'Frase nueva 2026',
        ])->assertOk();

        $this->app['auth']->forgetGuards();
        $this->withToken($tokenActual)->getJson('/v1/auth/me')->assertOk();

        $this->app['auth']->forgetGuards();
        $this->withToken($tokenOtro)->getJson('/v1/auth/me')->assertStatus(401);

        $fresco = $user->fresh();
        $this->assertNotSame($rememberAnterior, $fresco->remember_token);
        $this->assertDatabaseMissing('sessions', ['user_id' => $user->id]);
        $this->assertDatabaseMissing('password_reset_tokens', ['email' => $user->correo]);
        $this->assertTrue(Hash::check('Frase nueva 2026', $fresco->password));
    }

    public function test_cambiar_password_desde_cookie_conserva_la_sesion_actual_y_elimina_sesiones_ajenas(): void
    {
        $user = $this->usuario();
        $cookie1 = $this->cookieDe($this->loginConSesion($user)->assertOk());

        DB::table('sessions')->insert([
            'id' => 'otra-sesion-' . uniqid(),
            'user_id' => $user->id,
            'ip_address' => '127.0.0.1',
            'user_agent' => 'otra',
            'payload' => '',
            'last_activity' => time(),
        ]);

        $this->app['auth']->forgetGuards();
        $this->withCredentials()
            ->withCookie(config('session.cookie'), $cookie1)
            ->withHeader('Origin', $this->origenStateful())
            ->putJson('/v1/auth/password', [
                'password_actual' => 'claveActual123',
                'password' => 'Frase nueva 2026',
                'password_confirmation' => 'Frase nueva 2026',
            ])->assertOk();

        $this->app['auth']->forgetGuards();
        $this->withCredentials()
            ->withCookie(config('session.cookie'), $cookie1)
            ->withHeader('Origin', $this->origenStateful())
            ->getJson('/v1/auth/me')->assertOk();

        $this->assertDatabaseMissing('sessions', ['user_id' => $user->id]);
    }

    public function test_revocar_otras_sesiones_conserva_el_id_indicado(): void
    {
        $user = $this->usuario();
        $ids = ['actual-' . uniqid(), 'ajena-' . uniqid()];

        foreach ($ids as $id) {
            DB::table('sessions')->insert([
                'id' => $id,
                'user_id' => $user->id,
                'ip_address' => '127.0.0.1',
                'user_agent' => 'test',
                'payload' => '',
                'last_activity' => time(),
            ]);
        }

        SesionesUsuario::revocarOtras($user, $ids[0]);

        $this->assertDatabaseHas('sessions', ['id' => $ids[0]]);
        $this->assertDatabaseMissing('sessions', ['id' => $ids[1]]);
    }

    public function test_reset_password_revoca_todos_los_tokens_y_el_remember(): void
    {
        Notification::fake();
        $user = $this->usuario();
        $token1 = $this->token($user);
        $token2 = $this->token($user);
        $rememberAnterior = $user->fresh()->remember_token;

        $this->postJson('/v1/auth/forgot-password', ['correo' => $user->correo])->assertOk();
        $tokenReset = null;
        Notification::assertSentTo($user, ResetPasswordNotification::class, function ($n) use (&$tokenReset) {
            $tokenReset = $n->token;
            return true;
        });

        $this->postJson('/v1/auth/reset-password', [
            'correo' => $user->correo,
            'token' => $tokenReset,
            'password' => 'Frase nueva 2026',
            'password_confirmation' => 'Frase nueva 2026',
        ])->assertOk();

        $this->app['auth']->forgetGuards();
        $this->withToken($token1)->getJson('/v1/auth/me')->assertStatus(401);
        $this->app['auth']->forgetGuards();
        $this->withToken($token2)->getJson('/v1/auth/me')->assertStatus(401);

        $this->assertSame(0, $user->fresh()->tokens()->count());
        $this->assertNotSame($rememberAnterior, $user->fresh()->remember_token);
        $this->assertDatabaseMissing('password_reset_tokens', ['email' => $user->correo]);
    }
}
