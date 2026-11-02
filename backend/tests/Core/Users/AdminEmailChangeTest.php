<?php

namespace Tests\Core\Users;

use App\Models\GeneralUser;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Un administrador no puede cambiar el correo de otro administrador: con el
// correo nuevo podría pedir la recuperación de contraseña y tomar la cuenta.
class AdminEmailChangeTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Email',
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

    public function test_admin_no_puede_cambiar_el_correo_de_otro_admin(): void
    {
        $otroAdmin = $this->usuario('admin');
        $admin = $this->usuario('admin');
        $token = $this->token($admin);
        $correoOriginal = $otroAdmin->correo;
        $correoNuevo = 'tomado.'.uniqid().'@test.local';

        $this->app['auth']->forgetGuards();

        $this->withToken($token)
            ->putJson("/v1/general-users/{$otroAdmin->id}", ['correo' => $correoNuevo])
            ->assertStatus(422)
            ->assertJsonPath(
                'message',
                'El correo de otro administrador solo puede cambiarlo su titular con la contraseña actual.'
            );

        $this->assertDatabaseHas('general_users', ['id' => $otroAdmin->id, 'correo' => $correoOriginal]);
        $this->assertDatabaseMissing('general_users', ['correo' => $correoNuevo]);
    }

    public function test_admin_no_puede_degradar_a_otro_admin_para_tomar_su_cuenta(): void
    {
        $otroAdmin = $this->usuario('admin');
        $admin = $this->usuario('admin');
        $token = $this->token($admin);

        $this->app['auth']->forgetGuards();

        $this->withToken($token)
            ->putJson("/v1/general-users/{$otroAdmin->id}", ['rol' => 'aprendiz'])
            ->assertStatus(422)
            ->assertJsonPath('message', 'El rol de otro administrador no puede cambiarse desde esta vía.');

        $this->assertDatabaseHas('general_users', ['id' => $otroAdmin->id, 'rol' => 'admin']);
    }

    public function test_admin_si_puede_corregir_el_correo_de_un_aprendiz(): void
    {
        $admin = $this->usuario('admin');
        $aprendiz = $this->usuario('aprendiz');
        $token = $this->token($admin);
        $correoNuevo = 'corregido.'.uniqid().'@test.local';

        $this->app['auth']->forgetGuards();

        $this->withToken($token)
            ->putJson("/v1/general-users/{$aprendiz->id}", ['correo' => $correoNuevo])
            ->assertOk();

        $this->assertDatabaseHas('general_users', ['id' => $aprendiz->id, 'correo' => $correoNuevo]);
    }
}
