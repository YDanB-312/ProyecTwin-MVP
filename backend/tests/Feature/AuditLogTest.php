<?php

namespace Tests\Feature;

use App\Models\AuditLog;
use App\Models\GeneralUser;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// La bitácora registra acciones sensibles, es inmutable y solo la lee el admin.
class AuditLogTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol, bool $estado = true): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Audit',
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

    private function como(GeneralUser $user)
    {
        $token = $this->token($user);
        $this->app['auth']->forgetGuards();
        return $this->withToken($token);
    }

    public function test_crear_un_usuario_queda_registrado(): void
    {
        $admin = $this->usuario('admin');

        $this->como($admin)->postJson('/v1/general-users', [
            'nombre' => 'Nuevo',
            'apellido' => 'Usuario',
            'correo' => 'nuevo.' . uniqid() . '@test.local',
            'password' => '123456',
            'rol' => 'aprendiz',
        ])->assertCreated();

        $this->assertDatabaseHas('audit_logs', [
            'id_usuario' => $admin->id,
            'accion' => 'crear_usuario',
            'entidad' => 'general_users',
        ]);
    }

    public function test_el_cambio_de_correo_queda_registrado(): void
    {
        $user = $this->usuario('aprendiz');
        $nuevo = 'correo.' . uniqid() . '@test.local';

        $this->como($user)
            ->putJson('/v1/auth/email', ['correo' => $nuevo, 'password_actual' => '123456'])
            ->assertOk();

        $this->assertDatabaseHas('audit_logs', [
            'id_usuario' => $user->id,
            'accion' => 'cambiar_correo',
        ]);
    }

    public function test_eliminar_un_usuario_queda_registrado(): void
    {
        $admin = $this->usuario('admin');
        $objetivo = $this->usuario('aprendiz');

        $this->como($admin)->deleteJson('/v1/general-users/' . $objetivo->id)->assertOk();

        $this->assertDatabaseHas('audit_logs', [
            'id_usuario' => $admin->id,
            'accion' => 'eliminar_usuario',
            'entidad_id' => $objetivo->id,
        ]);
    }

    public function test_la_bitacora_nunca_guarda_contrasenas(): void
    {
        $admin = $this->usuario('admin');
        $objetivo = $this->usuario('aprendiz');

        $this->como($admin)->putJson('/v1/general-users/' . $objetivo->id, [
            'nombre' => $objetivo->nombre,
            'apellido' => $objetivo->apellido,
            'correo' => $objetivo->correo,
            'password' => 'claveSecreta123',
        ])->assertOk();

        $log = AuditLog::where('accion', 'actualizar_usuario')->latest('id')->first();
        $this->assertNotNull($log);
        $this->assertStringNotContainsString('claveSecreta123', json_encode($log->detalle));
        $this->assertTrue((bool) ($log->detalle['password_reset'] ?? false));
    }

    public function test_solo_el_admin_lee_la_bitacora(): void
    {
        $aprendiz = $this->usuario('aprendiz');
        $admin = $this->usuario('admin');

        $this->como($aprendiz)->getJson('/v1/audit-logs')->assertStatus(403);
        $this->como($admin)->getJson('/v1/audit-logs')->assertOk();
    }
}
