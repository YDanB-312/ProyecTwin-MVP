<?php

namespace Tests\Feature;

use App\Models\GeneralUser;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Reglas del perfil: nadie cambia su propio rol; el correo solo se edita con
// los campos obligatorios y el rol nunca viaja si el cliente no lo envía.
class GeneralUserRolTest extends TestCase
{
    use DatabaseTransactions;

    private function crearUsuario(string $rol, bool $estado = true): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'E2E',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => $estado,
        ]);
    }

    private function token(GeneralUser $user): string
    {
        return $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => '123456',
        ])->assertOk()->json('token');
    }

    public function test_un_aprendiz_no_puede_cambiar_su_rol(): void
    {
        $user = $this->crearUsuario('aprendiz');

        $this->withToken($this->token($user))
            ->putJson('/v1/general-users/' . $user->id, [
                'nombre' => $user->nombre,
                'apellido' => $user->apellido,
                'correo' => $user->correo,
                'rol' => 'admin',
            ])
            ->assertStatus(403);

        $this->assertSame('aprendiz', $user->fresh()->rol);
    }

    public function test_un_instructor_tampoco_puede_ascenderse_a_admin(): void
    {
        $user = $this->crearUsuario('instructor');

        $this->withToken($this->token($user))
            ->putJson('/v1/general-users/' . $user->id, [
                'nombre' => $user->nombre,
                'apellido' => $user->apellido,
                'correo' => $user->correo,
                'rol' => 'admin',
            ])
            ->assertStatus(403);

        $this->assertSame('instructor', $user->fresh()->rol);
    }

    public function test_el_perfil_se_actualiza_sin_enviar_rol_y_este_no_cambia(): void
    {
        $user = $this->crearUsuario('aprendiz');

        $this->withToken($this->token($user))
            ->putJson('/v1/general-users/' . $user->id, [
                'nombre' => 'Nombre Nuevo',
                'apellido' => 'Apellido Nuevo',
                'correo' => 'correo.nuevo.' . uniqid() . '@test.local',
            ])
            ->assertOk();

        $fresco = $user->fresh();
        $this->assertSame('Nombre Nuevo', $fresco->nombre);
        $this->assertSame('aprendiz', $fresco->rol);
    }

    public function test_un_admin_si_puede_cambiar_el_rol_de_otro(): void
    {
        $admin = $this->crearUsuario('admin');
        $objetivo = $this->crearUsuario('aprendiz');

        $this->withToken($this->token($admin))
            ->putJson('/v1/general-users/' . $objetivo->id, [
                'nombre' => $objetivo->nombre,
                'apellido' => $objetivo->apellido,
                'correo' => $objetivo->correo,
                'rol' => 'instructor',
            ])
            ->assertOk();

        $this->assertSame('instructor', $objetivo->fresh()->rol);
    }

    public function test_un_admin_no_puede_quitarse_su_propio_rol(): void
    {
        $admin = $this->crearUsuario('admin');

        $this->withToken($this->token($admin))
            ->putJson('/v1/general-users/' . $admin->id, [
                'nombre' => $admin->nombre,
                'apellido' => $admin->apellido,
                'correo' => $admin->correo,
                'rol' => 'aprendiz',
            ])
            ->assertStatus(422);

        $this->assertSame('admin', $admin->fresh()->rol);
    }

    public function test_un_aprendiz_no_puede_suspender_a_otro_usuario(): void
    {
        $aprendiz = $this->crearUsuario('aprendiz');
        $objetivo = $this->crearUsuario('aprendiz');

        $this->withToken($this->token($aprendiz))
            ->putJson('/v1/general-users/' . $objetivo->id, [
                'nombre' => $objetivo->nombre,
                'apellido' => $objetivo->apellido,
                'correo' => $objetivo->correo,
                'estado' => false,
            ])
            ->assertStatus(403);

        $this->assertTrue($objetivo->fresh()->estado);
    }
}
