<?php

namespace Tests\Feature;

use App\Models\GeneralUser;
use App\Models\KnowledgeNetwork;
use App\Models\PadronUsuario;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Identidad institucional: padrón, código de activación, login por documento y
// suspensión con revocación de tokens.
class IdentidadTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol, bool $estado = true): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Identidad',
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

    private function programa(): TrainingProgram
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        return TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);
    }

    private function padron(array $overrides = []): PadronUsuario
    {
        return PadronUsuario::create(array_merge([
            'tipo_documento' => 'CC',
            'numero_documento' => (string) random_int(10000000, 99999999),
            'nombre' => 'Nuevo',
            'apellido' => 'Ingreso',
            'correo' => 'nuevo.' . uniqid() . '@test.local',
            'rol' => 'aprendiz',
            'id_programa' => null,
            'id_class_group' => null,
            'id_usuario' => null,
            'activo' => true,
        ], $overrides));
    }

    // ---------------------------------------------------------------- Padrón

    public function test_validar_padron_confirma_la_identidad(): void
    {
        $fila = $this->padron();

        $this->postJson('/v1/auth/validar-padron', [
            'tipo_documento' => $fila->tipo_documento,
            'numero_documento' => $fila->numero_documento,
            'correo' => $fila->correo,
        ])->assertOk()
          ->assertJsonPath('coincide', true)
          ->assertJsonPath('rol', 'aprendiz');
    }

    public function test_validar_padron_rechaza_datos_desconocidos(): void
    {
        $this->postJson('/v1/auth/validar-padron', [
            'tipo_documento' => 'CC',
            'numero_documento' => '9999999999',
            'correo' => 'nadie.' . uniqid() . '@test.local',
        ])->assertStatus(422)
          ->assertJsonMissingPath('coincide');
    }

    // ---------------------------------------------------------------- Registro

    public function test_registro_publico_sin_padron_falla(): void
    {
        $this->postJson('/v1/general-users', [
            'tipo_documento' => 'CC',
            'numero_documento' => '8888888888',
            'correo' => 'intruso.' . uniqid() . '@test.local',
            'password' => 'clave12345',
            'password_confirmation' => 'clave12345',
        ])->assertStatus(422);
    }

    public function test_el_rol_lo_define_el_padron_no_el_request(): void
    {
        $fila = $this->padron(['rol' => 'aprendiz']);

        $respuesta = $this->postJson('/v1/general-users', [
            'tipo_documento' => $fila->tipo_documento,
            'numero_documento' => $fila->numero_documento,
            'correo' => $fila->correo,
            'password' => 'clave12345',
            'password_confirmation' => 'clave12345',
            'rol' => 'admin',
        ])->assertCreated();

        $this->assertSame('aprendiz', $respuesta->json('user.rol'));
        $this->assertDatabaseHas('general_users', [
            'correo' => $fila->correo,
            'rol' => 'aprendiz',
            'estado' => 0,
        ]);
    }

    public function test_un_documento_reclamado_no_se_puede_registrar_dos_veces(): void
    {
        $existente = $this->usuario('aprendiz');
        $fila = $this->padron(['id_usuario' => $existente->id, 'correo' => $existente->correo]);

        $this->postJson('/v1/general-users', [
            'tipo_documento' => $fila->tipo_documento,
            'numero_documento' => $fila->numero_documento,
            'correo' => $fila->correo,
            'password' => 'clave12345',
            'password_confirmation' => 'clave12345',
        ])->assertStatus(422);
    }

    // ---------------------------------------------------------------- Activación

    public function test_flujo_completo_registro_activacion_login(): void
    {
        config(['identidad.activacion.exponer_codigo' => true]);
        $fila = $this->padron();
        $password = 'clave12345';

        $registro = $this->postJson('/v1/general-users', [
            'tipo_documento' => $fila->tipo_documento,
            'numero_documento' => $fila->numero_documento,
            'correo' => $fila->correo,
            'password' => $password,
            'password_confirmation' => $password,
        ])->assertCreated()->assertJsonPath('activacion_requerida', true);

        $codigo = (string) $registro->json('codigo');
        $this->assertNotSame('', $codigo);

        // Sin activar: el login explica que falta activar.
        $this->postJson('/v1/auth/login', ['correo' => $fila->correo, 'password' => $password])
            ->assertStatus(403)
            ->assertJsonPath('pendiente_activacion', true);

        // Código erróneo.
        $erroneo = $codigo === '111111' ? '222222' : '111111';
        $this->postJson('/v1/auth/activar', ['correo' => $fila->correo, 'codigo' => $erroneo])
            ->assertStatus(422);

        // Activación correcta y login.
        $this->postJson('/v1/auth/activar', ['correo' => $fila->correo, 'codigo' => $codigo])
            ->assertOk();

        $this->postJson('/v1/auth/login', ['correo' => $fila->correo, 'password' => $password])
            ->assertOk()
            ->assertJsonPath('rol', 'aprendiz');
    }

    public function test_login_por_numero_de_documento(): void
    {
        $user = $this->usuario('aprendiz');
        $this->padron(['id_usuario' => $user->id, 'correo' => $user->correo]);

        $this->postJson('/v1/auth/login', [
            'identificador' => PadronUsuario::where('id_usuario', $user->id)->value('numero_documento'),
            'password' => '123456',
        ])->assertOk()->assertJsonPath('user.id', $user->id);
    }

    // ---------------------------------------------------------------- Suspensión

    public function test_suspender_una_cuenta_revoca_sus_tokens(): void
    {
        $admin = $this->usuario('admin');
        $objetivo = $this->usuario('aprendiz');
        $tokenViejo = $objetivo->createToken('test')->plainTextToken;

        $this->como($admin)
            ->putJson('/v1/general-users/' . $objetivo->id, ['estado' => false])
            ->assertOk();

        // El guard cachea el usuario entre peticiones del mismo test.
        $this->app['auth']->forgetGuards();
        $this->withToken($tokenViejo)->getJson('/v1/notifications')->assertStatus(401);
        $this->assertSame(0, $objetivo->tokens()->count());
    }
}
