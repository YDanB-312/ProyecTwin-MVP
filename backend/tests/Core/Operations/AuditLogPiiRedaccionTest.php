<?php

namespace Tests\Core\Operations;

use App\Models\AuditLog;
use App\Models\GeneralUser;
use App\Support\Auditoria;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// La redacción se aplica al escribir, antes de persistir el detalle.
class AuditLogPiiRedaccionTest extends TestCase
{
    use DatabaseTransactions;

    public function test_auditoria_redacta_correos_credenciales_y_respuestas(): void
    {
        Auditoria::registrar('prueba_pii', 'general_users', 1, [
            'correo' => 'persona@example.com',
            'correo_anterior' => 'anterior@example.com',
            'username' => 'usuario_largo',
            'password' => 'secreto',
            'token' => 'token-plano',
            'respuesta' => 'Texto con posible información personal',
            'anidado' => [
                'email' => 'otra@example.com',
                'token_hash' => 'hash',
            ],
            'resultado' => 'ok',
        ]);

        $detalle = AuditLog::where('accion', 'prueba_pii')->latest('id')->firstOrFail()->detalle;

        $this->assertSame('p***@example.com', $detalle['correo']);
        $this->assertSame('a***@example.com', $detalle['correo_anterior']);
        $this->assertSame('us***', $detalle['username']);
        $this->assertSame('[redactado]', $detalle['password']);
        $this->assertSame('[redactado]', $detalle['token']);
        $this->assertSame('[texto omitido]', $detalle['respuesta']);
        $this->assertSame('o***@example.com', $detalle['anidado']['email']);
        $this->assertSame('[redactado]', $detalle['anidado']['token_hash']);
        $this->assertSame('ok', $detalle['resultado']);

        $json = json_encode($detalle);
        $this->assertStringNotContainsString('persona@example.com', $json);
        $this->assertStringNotContainsString('secreto', $json);
        $this->assertStringNotContainsString('token-plano', $json);
        $this->assertStringNotContainsString('Texto con posible información personal', $json);
    }

    public function test_forgot_password_no_persiste_el_correo_completo(): void
    {
        $this->postJson('/v1/auth/forgot-password', [
            'correo' => 'tercero@example.com',
        ])->assertOk();

        $log = AuditLog::where('accion', 'solicitar_reset')->latest('id')->firstOrFail();

        $this->assertSame('t***@example.com', $log->detalle['correo'] ?? null);
        $this->assertStringNotContainsString('tercero@example.com', json_encode($log->detalle));
    }

    public function test_el_listado_redacta_detalles_historicos(): void
    {
        $admin = GeneralUser::create([
            'nombre' => 'Admin',
            'apellido' => 'PII',
            'correo' => 'admin.pii.'.uniqid().'@test.local',
            'password' => Hash::make('123456'),
            'rol' => 'admin',
            'estado' => true,
        ]);

        AuditLog::create([
            'id_usuario' => $admin->id,
            'accion' => 'historico_pii',
            'entidad' => 'general_users',
            'entidad_id' => $admin->id,
            'detalle' => ['correo' => 'viejo@example.com', 'token' => 'secreto'],
            'ip' => '127.0.0.1',
        ]);

        $token = $this->postJson('/v1/auth/login', [
            'correo' => $admin->correo,
            'password' => '123456',
        ])->assertOk()->json('token');

        $this->app['auth']->forgetGuards();

        $respuesta = $this->withToken($token)
            ->getJson('/v1/audit-logs?paginado=1&id_usuario='.$admin->id)
            ->assertOk();

        $this->assertStringNotContainsString('viejo@example.com', $respuesta->getContent());
        $this->assertStringNotContainsString('secreto', $respuesta->getContent());
        $this->assertStringContainsString('v***@example.com', $respuesta->getContent());
    }
}
