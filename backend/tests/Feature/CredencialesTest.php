<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Crypt;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Flujo institucional: el admin crea las cuentas, el sistema genera username y
// contraseña temporal (obligatoria de cambiar al primer ingreso) y el admin
// puede exportar las credenciales iniciales en PDF.
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

    private function token(GeneralUser $user, string $campo = 'correo'): string
    {
        return $this->postJson('/v1/auth/login', [
            $campo => $campo === 'username' ? $user->username : $user->correo,
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

    // Alta de usuario por el admin (devuelve credenciales una sola vez).
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

    public function test_el_admin_crea_la_cuenta_con_username_y_temporal(): void
    {
        $respuesta = $this->alta()->assertCreated();

        $username = $respuesta->json('credenciales.username');
        $temporal = $respuesta->json('credenciales.password_temporal');
        $this->assertSame('cmartinez', $username);
        $this->assertMatchesRegularExpression('/^[A-Z0-9]{4}-[A-Z0-9]{4}$/', $temporal);

        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $this->assertTrue($user->must_change_password);
        $this->assertTrue(Hash::check($temporal, $user->password)); // hash, no texto plano
        $this->assertSame($temporal, Crypt::decryptString($user->password_temporal));
        $this->assertDatabaseHas('audit_logs', ['accion' => 'crear_usuario', 'entidad_id' => $user->id]);
    }

    public function test_el_username_se_desambigua_con_sufijo(): void
    {
        $primero = $this->alta(['numero_documento' => '81000001'])->assertCreated();
        $segundo = $this->alta([
            'nombre' => 'Carlos',
            'apellido' => 'Martínez',
            'numero_documento' => '81000002',
            'correo' => 'carlos2.' . uniqid() . '@correo.com',
        ])->assertCreated();

        $this->assertSame('cmartinez', $primero->json('credenciales.username'));
        $this->assertSame('cmartinez1', $segundo->json('credenciales.username'));
    }

    // ---------------------------------------------------------------- Login

    public function test_el_login_es_por_username_y_obliga_a_cambiar_la_temporal(): void
    {
        $respuesta = $this->alta()->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $temporal = $respuesta->json('credenciales.password_temporal');

        $login = $this->postJson('/v1/auth/login', [
            'username' => $user->username,
            'password' => $temporal,
        ])->assertOk();

        $this->assertTrue((bool) $login->json('user.must_change_password'));
        $token = $login->json('token');
        $this->app['auth']->forgetGuards();

        // Con la temporal pendiente no se puede usar el sistema…
        $this->withToken($token)->getJson('/v1/projects')
            ->assertStatus(403)
            ->assertJsonPath('must_change_password', true);

        // …pero sí cambiar la contraseña.
        $this->withToken($token)->putJson('/v1/auth/password', [
            'password_actual' => $temporal,
            'password' => 'definitiva123',
            'password_confirmation' => 'definitiva123',
        ])->assertOk();

        // Se limpia la temporal y ya puede operar.
        $fresco = $user->fresh();
        $this->assertFalse($fresco->must_change_password);
        $this->assertNull($fresco->password_temporal);

        $this->app['auth']->forgetGuards();
        $this->withToken($token)->getJson('/v1/projects')->assertOk();

        // La temporal dejó de servir; la definitiva sí.
        $this->postJson('/v1/auth/login', ['username' => $user->username, 'password' => $temporal])
            ->assertStatus(422);
        $this->postJson('/v1/auth/login', ['username' => $user->username, 'password' => 'definitiva123'])
            ->assertOk();
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

    public function test_el_admin_restablece_la_temporal_generada_por_el_servidor(): void
    {
        $respuesta = $this->alta()->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $temporalOriginal = $respuesta->json('credenciales.password_temporal');
        $admin = $this->usuario('admin');

        $nueva = $this->como($admin)
            ->postJson('/v1/general-users/' . $user->id . '/credenciales/restablecer')
            ->assertOk()
            ->json('credenciales.password_temporal');

        // Mismo formato institucional que el alta y distinta a la anterior.
        $this->assertMatchesRegularExpression('/^[A-Z0-9]{4}-[A-Z0-9]{4}$/', $nueva);
        $this->assertNotSame($temporalOriginal, $nueva);

        // La nueva temporal funciona y sigue obligando al cambio.
        $fresco = $user->fresh();
        $this->assertTrue(Hash::check($nueva, $fresco->password));
        $this->assertTrue((bool) $fresco->must_change_password);
        $this->assertTrue(Crypt::decryptString($fresco->password_temporal) === $nueva);

        $this->postJson('/v1/auth/login', [
            'username' => $user->username,
            'password' => $nueva,
        ])->assertOk()->assertJsonPath('user.must_change_password', true);

        $this->assertDatabaseHas('audit_logs', [
            'accion' => 'restablecer_credenciales',
            'entidad_id' => $user->id,
        ]);
    }

    public function test_solo_el_admin_restablece_credenciales(): void
    {
        $respuesta = $this->alta()->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('usuario.id'));
        $aprendiz = $this->usuario('aprendiz');

        $this->como($aprendiz)
            ->postJson('/v1/general-users/' . $user->id . '/credenciales/restablecer')
            ->assertStatus(403);
    }
}
