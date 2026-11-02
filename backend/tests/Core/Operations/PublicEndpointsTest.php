<?php

namespace Tests\Core\Operations;

use App\Models\GeneralUser;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// C-09: cubre el resumen público sin PII y la lectura pública / escritura
// administrativa de la configuración del motor.
class PublicEndpointsTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Público',
            'correo' => $rol.'.'.uniqid().'@test.local',
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

    public function test_resumen_publico_no_expone_datos_personales(): void
    {
        $this->getJson('/v1/public/resumen')
            ->assertOk()
            ->assertJsonStructure([
                'umbral',
                'meses',
                'total_propuestas',
                'total_programas',
            ])
            ->assertJsonMissingPath('correo')
            ->assertJsonMissingPath('nombre')
            ->assertJsonMissingPath('apellido')
            ->assertJsonMissingPath('numero_documento')
            ->assertJsonMissingPath('password');
    }

    public function test_configuracion_del_motor_es_publica_pero_solo_admin_escribe(): void
    {
        $this->getJson('/v1/config-similitud')->assertOk();

        $admin = $this->usuario('admin');
        $tokenAdmin = $this->token($admin);
        $this->app['auth']->forgetGuards();

        $this->withToken($tokenAdmin)
            ->putJson('/v1/config-similitud', ['umbral' => 0.42, 'meses' => 12])
            ->assertOk()
            ->assertJsonPath('umbral', 0.42)
            ->assertJsonPath('meses', 12);

        $aprendiz = $this->usuario('aprendiz');
        $tokenAprendiz = $this->token($aprendiz);
        $this->app['auth']->forgetGuards();

        $this->withToken($tokenAprendiz)
            ->putJson('/v1/config-similitud', ['umbral' => 0.50, 'meses' => 6])
            ->assertStatus(403);
    }
}
