<?php

namespace Tests\Core\Academic;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingProgram;
use App\Notifications\ResetPasswordNotification;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Flujo institucional: el admin crea cuentas pendientes de activación y puede
// exportar el PDF con usuario y estado, sin contraseñas.
class CredencialesTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol, array $extra = []): GeneralUser
    {
        return GeneralUser::create(array_merge([
            'nombre' => ucfirst($rol),
            'apellido' => 'Cred',
            'correo' => $rol . '.' . uniqid() . '@correo.com',
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
            'knowledge_network_id' => $red->id,
        ]);
    }

    // Alta de usuario por el admin (devuelve el estado de la activación).
    private function alta(array $extra = [])
    {
        $admin = $this->usuario('admin');

        return $this->como($admin)->postJson('/v1/general-users', array_merge([
            'nombre' => 'Carlos',
            'apellido' => 'Martínez',
            'tipo_documento' => 'CC',
            'numero_documento' => (string) random_int(1000000, 9999999),
            'correo' => 'carlos.' . uniqid() . '@correo.com',
            'rol' => 'aprendiz',
        ], $extra));
    }

    // ---------------------------------------------------------------- Alta

    public function test_el_admin_crea_la_cuenta_pendiente_con_username(): void
    {
        Notification::fake();
        $respuesta = $this->alta()->assertCreated();

        $username = $respuesta->json('usuario.username');
        $this->assertMatchesRegularExpression('/^CaMz_[A-Za-z0-9]{5}$/', $username);
        $respuesta->assertJsonMissingPath('credenciales');
        $respuesta->assertJsonMissingPath('usuario.must_change_password');
        $respuesta->assertJsonMissingPath('usuario.password_temporal');
        $this->assertTrue((bool) $respuesta->json('activacion.enviada'));

        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $this->assertNull($user->email_verified_at);
        $this->assertDatabaseHas('account_activations', ['user_id' => $user->id]);
        $this->assertDatabaseHas('audit_logs', ['accion' => 'crear_usuario', 'entidad_id' => $user->id]);
    }

    public function test_el_alta_normaliza_el_correo_y_rechaza_duplicados_con_otra_capitalizacion(): void
    {
        Notification::fake();
        $correo = 'Persona.' . uniqid() . '@Correo.com';
        $normalizado = strtolower(trim($correo));

        $this->alta(['correo' => '  ' . strtoupper($correo) . '  '])->assertCreated();
        $this->assertDatabaseHas('general_users', ['correo' => $normalizado]);

        $this->alta(['correo' => $normalizado])->assertStatus(422);
    }

    public function test_dos_altas_con_el_mismo_nombre_generan_usernames_distintos(): void
    {
        Notification::fake();
        $primero = $this->alta(['numero_documento' => '81000001'])->assertCreated();
        $segundo = $this->alta([
            'nombre' => 'Carlos',
            'apellido' => 'Martínez',
            'numero_documento' => '81000002',
            'correo' => 'carlos2.' . uniqid() . '@correo.com',
        ])->assertCreated();

        $username1 = $primero->json('usuario.username');
        $username2 = $segundo->json('usuario.username');

        $this->assertMatchesRegularExpression('/^CaMz_[A-Za-z0-9]{5}$/', $username1);
        $this->assertMatchesRegularExpression('/^CaMz_[A-Za-z0-9]{5}$/', $username2);
        $this->assertNotSame($username1, $username2);
    }

    // ---------------------------------------------------------------- Ficha

    public function test_la_ficha_se_crea_con_aprendices_seleccionados(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();

        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $this->usuario('instructor')->id,
        ]);
        $aprendizA = $this->usuario('aprendiz');
        $aprendizB = $this->usuario('aprendiz');

        $this->como($admin)->postJson('/v1/class-groups', [
            'numero' => '7010001',
            'nombre' => 'Ficha con aprendices',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
            'aprendices' => [$aprendizA->id, $aprendizB->id],
        ])->assertCreated();

        $this->assertDatabaseHas('apprentices', [
            'id_usuario' => $aprendizA->id,
            'id_class_group' => ClassGroup::where('numero', '7010001')->value('id'),
            'id_programa' => $programa->id,
        ]);
        $this->assertSame(1, Apprentice::where('id_usuario', $aprendizB->id)->count());
    }

    public function test_editar_la_ficha_retira_a_los_aprendices_no_seleccionados(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $this->usuario('instructor')->id,
        ]);
        $aprendizA = $this->usuario('aprendiz');
        $aprendizB = $this->usuario('aprendiz');

        $ficha = ClassGroup::create([
            'codigo' => 'ed-' . uniqid(),
            'numero' => '7010002',
            'nombre' => 'Ficha editable',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);

        $this->como($admin)->putJson('/v1/class-groups/' . $ficha->id, [
            'numero' => '7010002',
            'nombre' => $ficha->nombre,
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
            'aprendices' => [$aprendizA->id, $aprendizB->id],
        ])->assertOk();

        // Se retira a B: sale de la ficha conservando su fila.
        $this->como($admin)->putJson('/v1/class-groups/' . $ficha->id, [
            'numero' => '7010002',
            'nombre' => $ficha->nombre,
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
            'aprendices' => [$aprendizA->id],
        ])->assertOk();

        $this->assertSame($ficha->id, (int) Apprentice::where('id_usuario', $aprendizA->id)->value('id_class_group'));
        $this->assertNull(Apprentice::where('id_usuario', $aprendizB->id)->value('id_class_group'));
    }

    // ---------------------------------------------------------------- Exportación

    public function test_exportar_credenciales_de_una_ficha_genera_pdf(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $this->usuario('instructor')->id,
        ]);

        $creada = $this->como($admin)->postJson('/v1/class-groups', [
            'numero' => '7010003',
            'nombre' => 'Ficha exportable',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ])->assertCreated();
        $fichaId = $creada->json('id');

        $respuesta = $this->como($admin)->get('/v1/class-groups/' . $fichaId . '/credenciales');
        $respuesta->assertOk();
        $this->assertStringContainsString('application/pdf', $respuesta->headers->get('content-type'));
        $this->assertDatabaseHas('audit_logs', ['accion' => 'exportar_credenciales', 'entidad_id' => $fichaId]);
    }

    public function test_exportar_credenciales_de_usuarios_seleccionados_solo_admin(): void
    {
        $admin = $this->usuario('admin');
        $aprendiz = $this->usuario('aprendiz');

        // Un aprendiz no puede exportar credenciales.
        $this->como($aprendiz)->postJson('/v1/general-users/credenciales', ['ids' => [$aprendiz->id]])
            ->assertStatus(403);

        $respuesta = $this->como($admin)->post('/v1/general-users/credenciales', ['ids' => [$aprendiz->id]]);
        $respuesta->assertOk();
        $this->assertStringContainsString('application/pdf', $respuesta->headers->get('content-type'));
    }

    // ---------------------------------------------------------------- Búsqueda y paginación

    public function test_la_busqueda_encuentra_por_documento_y_username(): void
    {
        $admin = $this->usuario('admin');
        $username = 'lgomez' . random_int(1000, 9999);
        $this->usuario('aprendiz', [
            'nombre' => 'Lucía',
            'apellido' => 'Gómez',
            'username' => $username,
            'numero_documento' => '55667788',
        ]);

        $porDocumento = $this->como($admin)->getJson('/v1/general-users?search=55667788')->assertOk()->json();
        $this->assertCount(1, $porDocumento);

        $porUsername = $this->como($admin)->getJson('/v1/general-users?search=' . $username)->assertOk()->json();
        $this->assertCount(1, $porUsername);
    }

    public function test_el_listado_paginado_no_devuelve_todo(): void
    {
        $admin = $this->usuario('admin');
        for ($i = 0; $i < 3; $i++) {
            $this->usuario('aprendiz');
        }

        $pagina = $this->como($admin)
            ->getJson('/v1/general-users?paginado=1&por_pagina=2')
            ->assertOk()
            ->json();

        $this->assertArrayHasKey('data', $pagina);
        $this->assertCount(2, $pagina['data']);
        $this->assertSame(2, $pagina['per_page']);
    }

    // ---------------------------------------------------------------- Restablecer

    public function test_el_admin_restablece_enviando_enlace_y_revocando_acceso(): void
    {
        Notification::fake();
        $respuesta = $this->alta()->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $user->forceFill(['email_verified_at' => now()])->save();
        $admin = $this->usuario('admin');

        // Simula una sesión/token previo que debe quedar revocado.
        $user->createToken('sesion-previa');
        $this->assertSame(1, $user->tokens()->count());

        $respuestaReset = $this->como($admin)
            ->postJson('/v1/general-users/' . $user->id . '/credenciales/restablecer')
            ->assertOk();

        // No se devuelve ni genera una contraseña temporal.
        $respuestaReset->assertJsonMissingPath('credenciales');
        $this->assertSame(0, $user->fresh()->tokens()->count());
        Notification::assertSentTo($user, ResetPasswordNotification::class);

        $this->assertDatabaseHas('audit_logs', [
            'accion' => 'restablecer_clave_administrativa',
            'entidad_id' => $user->id,
        ]);
    }

    public function test_solo_el_admin_restablece_contrasena(): void
    {
        Notification::fake();
        $respuesta = $this->alta()->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $aprendiz = $this->usuario('aprendiz');

        $this->como($aprendiz)
            ->postJson('/v1/general-users/' . $user->id . '/credenciales/restablecer')
            ->assertStatus(403);
    }
}
