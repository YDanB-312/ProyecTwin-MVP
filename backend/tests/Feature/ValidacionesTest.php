<?php

namespace Tests\Feature;

use App\Models\Admin;
use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Reglas de validación (códigos 422/404) de los flujos principales.
class ValidacionesTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol, array $extra = []): GeneralUser
    {
        return GeneralUser::create(array_merge([
            'nombre' => ucfirst($rol),
            'apellido' => 'Val',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ], $extra));
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

    private function ficha(string $estado = 'activo'): ClassGroup
    {
        $instructor = Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $this->usuario('instructor')->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'val-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha validación',
            'estado' => $estado,
            'id_programa' => $this->programa()->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    // ---------------------------------------------------------------- Usuarios

    // Alta del administrador: el username y la contraseña temporal se generan.
    private function altaUsuario(array $extra = [])
    {
        $admin = $this->usuario('admin');

        return $this->como($admin)->postJson('/v1/general-users', array_merge([
            'nombre' => 'Nuevo',
            'apellido' => 'Usuario',
            'tipo_documento' => 'CC',
            'numero_documento' => (string) random_int(1000000, 9999999),
            'correo' => 'nuevo.' . uniqid() . '@correo.com',
            'rol' => 'aprendiz',
        ], $extra));
    }

    public function test_crear_usuario_con_correo_duplicado_falla(): void
    {
        $existente = $this->usuario('aprendiz');

        $this->altaUsuario(['correo' => $existente->correo])
            ->assertStatus(422)
            ->assertJsonValidationErrors('correo');
    }

    public function test_crear_usuario_con_rol_invalido_falla(): void
    {
        $this->altaUsuario(['rol' => 'hacker'])
            ->assertStatus(422)
            ->assertJsonValidationErrors('rol');
    }

    public function test_crear_usuario_con_documento_duplicado_falla(): void
    {
        $existente = $this->usuario('aprendiz', ['numero_documento' => '900123456']);

        $this->altaUsuario(['numero_documento' => $existente->numero_documento])
            ->assertStatus(422)
            ->assertJsonValidationErrors('numero_documento');
    }

    public function test_el_alta_de_usuarios_exige_sesion_de_administrador(): void
    {
        // Ya no hay registro público: sin sesión, la ruta responde 401.
        $this->postJson('/v1/general-users', [
            'nombre' => 'Sin',
            'apellido' => 'Sesion',
            'tipo_documento' => 'CC',
            'numero_documento' => '900999999',
            'correo' => 'sin.sesion.' . uniqid() . '@correo.com',
            'rol' => 'aprendiz',
        ])->assertStatus(401);
    }

    // ---------------------------------------------------------------- Fichas

    public function test_crear_ficha_sin_instructor_falla(): void
    {
        $admin = $this->usuario('admin');

        $this->como($admin)
            ->postJson('/v1/class-groups', [
                'codigo' => 'sincod-' . uniqid(),
                'nombre' => 'Sin instructor',
                'estado' => 'activo',
                'id_programa' => $this->programa()->id,
            ])
            ->assertStatus(422);
    }

    public function test_crear_ficha_con_numero_duplicado_falla(): void
    {
        $admin = $this->usuario('admin');
        $existente = $this->ficha();

        $this->como($admin)
            ->postJson('/v1/class-groups', [
                'numero' => $existente->numero,
                'nombre' => 'Duplicada',
                'estado' => 'activo',
                'id_programa' => $existente->id_programa,
                'id_instructor' => $existente->id_instructor,
            ])
            ->assertStatus(422);
    }

    // ---------------------------------------------------------------- Mi ficha

    public function test_unirse_con_codigo_invalido_da_404(): void
    {
        $this->como($this->usuario('aprendiz'))
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => 'zzz-zzzz'])
            ->assertStatus(404);
    }

    public function test_unirse_a_ficha_finalizada_falla(): void
    {
        $ficha = $this->ficha('finalizado');

        $this->como($this->usuario('aprendiz'))
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => $ficha->codigo])
            ->assertStatus(422);
    }

    public function test_unirse_a_la_misma_ficha_falla(): void
    {
        $ficha = $this->ficha();
        $aprendiz = $this->usuario('aprendiz');
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $aprendiz->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);

        $this->como($aprendiz)
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => $ficha->codigo])
            ->assertStatus(422);
    }

    // ---------------------------------------------------------------- Motor

    public function test_motor_fuera_de_rango_falla(): void
    {
        $admin = $this->usuario('admin');
        Admin::create(['id_usuario' => $admin->id]);

        $this->como($admin)
            ->putJson('/v1/config-similitud', ['umbral' => 2, 'meses' => 12])
            ->assertStatus(422);

        $this->como($admin)
            ->putJson('/v1/config-similitud', ['umbral' => 0.2, 'meses' => 999])
            ->assertStatus(422);
    }

    // ---------------------------------------------------------------- Propuestas

    public function test_crear_propuesta_sin_campos_falla(): void
    {
        $this->como($this->usuario('aprendiz'))
            ->postJson('/v1/projects', [])
            ->assertStatus(422);
    }

    // ---------------------------------------------------------------- Perfil

    public function test_actualizar_solo_la_foto_no_exige_apellido(): void
    {
        // Admin sin apellido (como el seed): guardar SOLO la foto no debe fallar.
        $admin = GeneralUser::create([
            'nombre' => 'Admin',
            'apellido' => '',
            'correo' => 'foto.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => 'admin',
            'estado' => true,
        ]);

        $this->como($admin)
            ->putJson('/v1/general-users/' . $admin->id, ['foto_url' => 'data:image/png;base64,AAAA'])
            ->assertOk()
            ->assertJsonPath('foto_url', 'data:image/png;base64,AAAA');
    }
}
