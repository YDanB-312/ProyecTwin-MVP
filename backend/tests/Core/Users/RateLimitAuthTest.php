<?php

namespace Tests\Core\Users;

use App\Models\GeneralUser;
use App\Models\Project;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Limitadores específicos de autenticación. La caché de pruebas es `array`,
// por lo que cada test parte de un estado limpio de la aplicación.
class RateLimitAuthTest extends TestCase
{
    use DatabaseTransactions;

    private const PASSWORD = 'claveActual123';

    private function usuario(): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Rate',
            'apellido' => 'Limit',
            'correo' => 'rate.' . uniqid() . '@test.local',
            'password' => Hash::make('claveActual123'),
            'rol' => 'aprendiz',
            'estado' => true,
        ]);
    }

    public function test_login_limita_por_correo_e_ip(): void
    {
        $user = $this->usuario();

        for ($i = 0; $i < 10; $i++) {
            $this->postJson('/v1/auth/login', [
                'correo' => $user->correo,
                'password' => 'incorrecta',
            ])->assertStatus(422);
        }

        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => 'incorrecta',
        ])->assertStatus(429)
          ->assertJsonPath('message', 'Demasiados intentos. Espera un momento e inténtalo de nuevo.');
    }

    public function test_login_con_correo_array_no_produce_500(): void
    {
        $this->postJson('/v1/auth/login', [
            'correo' => ['no-es-un-correo'],
            'password' => 'x',
        ])->assertStatus(422);
    }

    public function test_recuperacion_limita_por_correo_e_ip(): void
    {
        Notification::fake();
        $user = $this->usuario();

        for ($i = 0; $i < 3; $i++) {
            $this->postJson('/v1/auth/forgot-password', ['correo' => $user->correo])
                ->assertOk();
        }

        $this->postJson('/v1/auth/forgot-password', ['correo' => $user->correo])
            ->assertStatus(429);
    }

    public function test_reset_limita_por_correo_e_ip(): void
    {
        $user = $this->usuario();

        for ($i = 0; $i < 5; $i++) {
            $this->postJson('/v1/auth/reset-password', [
                'correo' => $user->correo,
                'token' => 'invalido',
                'password' => 'Frase segura 2026',
                'password_confirmation' => 'Frase segura 2026',
            ])->assertStatus(422);
        }

        $this->postJson('/v1/auth/reset-password', [
            'correo' => $user->correo,
            'token' => 'invalido',
            'password' => 'Frase segura 2026',
            'password_confirmation' => 'Frase segura 2026',
        ])->assertStatus(429);
    }

    public function test_activacion_limita_por_ip(): void
    {
        for ($i = 0; $i < 5; $i++) {
            $this->postJson('/v1/auth/activation', [
                'token' => 'invalido',
                'password' => 'Frase segura 2026',
                'password_confirmation' => 'Frase segura 2026',
            ])->assertStatus(422);
        }

        $this->postJson('/v1/auth/activation', [
            'token' => 'invalido',
            'password' => 'Frase segura 2026',
            'password_confirmation' => 'Frase segura 2026',
        ])->assertStatus(429);
    }

    public function test_reenvio_de_activacion_limita_por_administrador(): void
    {
        Notification::fake();
        $admin = GeneralUser::create([
            'nombre' => 'Admin',
            'apellido' => 'Rate',
            'correo' => 'admin.rate.' . uniqid() . '@test.local',
            'password' => Hash::make('claveActual123'),
            'rol' => 'admin',
            'estado' => true,
        ]);

        $token = $this->postJson('/v1/auth/login', [
            'correo' => $admin->correo,
            'password' => 'claveActual123',
        ])->assertOk()->json('token');

        $alta = $this->withToken($token)->postJson('/v1/general-users', [
            'nombre' => 'Pendiente',
            'apellido' => 'Rate',
            'tipo_documento' => 'CC',
            'numero_documento' => (string) random_int(1000000, 9999999),
            'correo' => 'pendiente.rate.' . uniqid() . '@test.local',
            'rol' => 'aprendiz',
        ])->assertCreated();

        $id = $alta->json('usuario.id');

        for ($i = 0; $i < 5; $i++) {
            $this->withToken($token)
                ->postJson("/v1/general-users/{$id}/activacion/reenviar")
                ->assertOk();
        }

        $this->withToken($token)
            ->postJson("/v1/general-users/{$id}/activacion/reenviar")
            ->assertStatus(429);
    }

    public function test_reset_administrativo_pendiente_no_evade_el_throttle_de_reenvio(): void
    {
        Notification::fake();
        $admin = GeneralUser::create([
            'nombre' => 'Admin',
            'apellido' => 'Reset',
            'correo' => 'admin.reset.' . uniqid() . '@test.local',
            'password' => Hash::make('claveActual123'),
            'rol' => 'admin',
            'estado' => true,
        ]);

        $token = $this->postJson('/v1/auth/login', [
            'correo' => $admin->correo,
            'password' => 'claveActual123',
        ])->assertOk()->json('token');

        $alta = $this->withToken($token)->postJson('/v1/general-users', [
            'nombre' => 'Pendiente',
            'apellido' => 'Reset',
            'tipo_documento' => 'CC',
            'numero_documento' => (string) random_int(1000000, 9999999),
            'correo' => 'pendiente.reset.' . uniqid() . '@test.local',
            'rol' => 'aprendiz',
        ])->assertCreated();

        $id = $alta->json('usuario.id');

        for ($i = 0; $i < 5; $i++) {
            $this->withToken($token)
                ->postJson("/v1/general-users/{$id}/credenciales/restablecer")
                ->assertOk();
        }

        $this->withToken($token)
            ->postJson("/v1/general-users/{$id}/credenciales/restablecer")
            ->assertStatus(429);
    }

    public function test_la_demo_publica_limita_por_ip(): void
    {
        for ($i = 0; $i < 10; $i++) {
            $this->postJson('/v1/public/demo-similitud', [
                'texto' => 'Gestión de inventarios con trazabilidad',
            ])->assertOk();
        }

        $this->postJson('/v1/public/demo-similitud', [
            'texto' => 'Gestión de inventarios con trazabilidad',
        ])->assertStatus(429);
    }

    public function test_la_deteccion_autenticada_limita_por_usuario(): void
    {
        $user = GeneralUser::create([
            'nombre' => 'Rate',
            'apellido' => 'Motor',
            'correo' => 'rate.motor.'.uniqid().'@test.local',
            'username' => 'rate_motor_'.uniqid(),
            'password' => Hash::make(self::PASSWORD),
            'rol' => 'admin',
            'estado' => true,
        ]);

        $proyecto = Project::create([
            'titulo' => 'Propuesta para límite de detección',
            'estado' => 'pendiente',
            'id_creador' => $user->id,
        ]);

        $token = $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => self::PASSWORD,
        ])->assertOk()->json('token');

        $this->app['auth']->forgetGuards();

        for ($i = 0; $i < 5; $i++) {
            $this->withToken($token)
                ->postJson('/v1/similarities/detect', ['id_proyecto' => $proyecto->id])
                ->assertOk();
        }

        $this->withToken($token)
            ->postJson('/v1/similarities/detect', ['id_proyecto' => $proyecto->id])
            ->assertStatus(429);
    }
}
