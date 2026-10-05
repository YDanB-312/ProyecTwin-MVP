<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Notification;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Normalización F7 (G1): cierre de `included` anidado, alcance de los detalles,
// ownership de instructores/aprendices, mass assignment y endurecimientos de
// bajo impacto (cuenta activa y contraseña distinta a la actual).
class NormalizacionSeguridadTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol, bool $estado = true): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Seg',
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

    // Ficha activa con instructor propio (crea la fila de perfil si hace falta).
    private function ficha(?GeneralUser $instructorUser = null): ClassGroup
    {
        $instructorUser = $instructorUser ?: $this->usuario('instructor');
        $instructor = Instructor::firstOrCreate(
            ['id_usuario' => $instructorUser->id],
            ['fecha_ingreso' => '2024-01-01']
        );
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'sg-' . uniqid(),
            'nombre' => 'Ficha seg',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    private function aprendizEn(GeneralUser $user, ClassGroup $ficha): Apprentice
    {
        return Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $user->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);
    }

    // ---------------------------------------------------------------- included

    public function test_los_catalogos_publicos_no_exponen_aprendices_ni_fichas(): void
    {
        $ficha = $this->ficha();
        $this->aprendizEn($this->usuario('aprendiz'), $ficha);

        $programas = $this->getJson('/v1/training-programs?included=apprentices.generalUser,classGroups')
            ->assertOk()->json();
        $this->assertNotEmpty($programas);
        $this->assertArrayNotHasKey('apprentices', $programas[0]);
        $this->assertArrayNotHasKey('classGroups', $programas[0]);

        // El include legítimo sigue funcionando.
        $conRed = $this->getJson('/v1/training-programs?included=knowledgeNetwork')->assertOk()->json();
        $this->assertArrayHasKey('knowledgeNetwork', $conRed[0]);

        $redes = $this->getJson('/v1/knowledge-networks?included=trainingPrograms.classGroups')
            ->assertOk()->json();
        $this->assertNotEmpty($redes);
        $this->assertArrayNotHasKey('trainingPrograms', $redes[0]);
    }

    public function test_el_include_anidado_no_autorizado_se_ignora(): void
    {
        $aprendiz = $this->usuario('aprendiz');
        $ficha = $this->ficha();
        $this->aprendizEn($aprendiz, $ficha);
        $proyecto = Project::create([
            'titulo' => 'Propuesta ' . uniqid(),
            'estado' => 'pendiente',
            'id_creador' => $aprendiz->id,
            'id_class_group' => $ficha->id,
        ]);

        $respuesta = $this->como($aprendiz)
            ->getJson('/v1/projects/' . $proyecto->id . '?included=creator,creator.projects')
            ->assertOk()->json();

        // El include legítimo llega; la cadena anidada no autorizada se ignora.
        $this->assertArrayHasKey('creator', $respuesta);
        $this->assertArrayNotHasKey('projects', $respuesta['creator']);
    }

    // ---------------------------------------------------------------- alcance show

    public function test_un_aprendiz_solo_ve_su_ficha_y_sus_companeros(): void
    {
        $fichaPropia = $this->ficha();
        $fichaAjena = $this->ficha();
        $aprendiz = $this->usuario('aprendiz');
        $companero = $this->usuario('aprendiz');
        $ajeno = $this->usuario('aprendiz');
        $filaPropia = $this->aprendizEn($aprendiz, $fichaPropia);
        $filaCompanero = $this->aprendizEn($companero, $fichaPropia);
        $filaAjena = $this->aprendizEn($ajeno, $fichaAjena);

        $this->como($aprendiz)->getJson('/v1/class-groups/' . $fichaPropia->id)->assertOk();
        $this->como($aprendiz)->getJson('/v1/class-groups/' . $fichaAjena->id)->assertStatus(403);

        $this->como($aprendiz)->getJson('/v1/apprentices/' . $filaPropia->id)->assertOk();
        $this->como($aprendiz)->getJson('/v1/apprentices/' . $filaCompanero->id)->assertOk();
        $this->como($aprendiz)->getJson('/v1/apprentices/' . $filaAjena->id)->assertStatus(403);
    }

    public function test_un_instructor_solo_gestiona_sus_fichas_y_aprendices(): void
    {
        $instructor = $this->usuario('instructor');
        $fichaPropia = $this->ficha($instructor);
        $fichaAjena = $this->ficha();
        $ajeno = $this->usuario('aprendiz');
        $filaAjena = $this->aprendizEn($ajeno, $fichaAjena);

        $this->como($instructor)->getJson('/v1/class-groups/' . $fichaPropia->id)->assertOk();
        $this->como($instructor)->getJson('/v1/class-groups/' . $fichaAjena->id)->assertStatus(403);

        // No inscribe en ficha ajena…
        $nuevo = $this->usuario('aprendiz');
        $this->como($instructor)->postJson('/v1/apprentices', [
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $nuevo->id,
            'id_class_group' => $fichaAjena->id,
        ])->assertStatus(403);

        // …pero sí en la suya.
        $creada = $this->como($instructor)->postJson('/v1/apprentices', [
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $nuevo->id,
            'id_class_group' => $fichaPropia->id,
        ])->assertCreated()->json();

        // No mueve aprendices de otra ficha…
        $this->como($instructor)->putJson('/v1/apprentices/' . $filaAjena->id, [
            'codigo' => $filaAjena->codigo,
            'id_usuario' => $ajeno->id,
            'id_class_group' => $fichaPropia->id,
        ])->assertStatus(403);

        // …ni reasigna la cuenta dueña de una fila propia.
        $this->como($instructor)->putJson('/v1/apprentices/' . $creada['id'], [
            'codigo' => $creada['codigo'],
            'id_usuario' => $this->usuario('aprendiz')->id,
            'id_class_group' => $fichaPropia->id,
        ])->assertStatus(403);
    }

    public function test_un_instructor_no_edita_el_perfil_de_otro(): void
    {
        $a = $this->usuario('instructor');
        $b = $this->usuario('instructor');
        $this->ficha($a);
        $filaB = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $b->id]);

        $this->como($a)->putJson('/v1/instructors/' . $filaB->id, ['fecha_ingreso' => '2025-01-01'])
            ->assertStatus(403);

        $filaA = Instructor::where('id_usuario', $a->id)->firstOrFail();
        $this->como($a)->putJson('/v1/instructors/' . $filaA->id, ['fecha_ingreso' => '2025-01-01'])
            ->assertOk();
    }

    // ---------------------------------------------------------------- mass assignment

    public function test_no_se_puede_falsificar_la_huella_de_envio(): void
    {
        $aprendiz = $this->usuario('aprendiz');
        $ficha = $this->ficha();
        $this->aprendizEn($aprendiz, $ficha);
        $proyecto = Project::create([
            'titulo' => 'Propuesta ' . uniqid(),
            'estado' => 'rechazado',
            'huella_envio' => 'huella-real',
            'id_creador' => $aprendiz->id,
            'id_class_group' => $ficha->id,
        ]);

        $this->como($aprendiz)->putJson('/v1/projects/' . $proyecto->id, [
            'titulo' => 'Propuesta corregida ' . uniqid(),
            'huella_envio' => 'huella-falsa',
        ])->assertOk();

        $this->assertSame('huella-real', $proyecto->fresh()->huella_envio);
    }

    public function test_el_update_generico_no_cambia_campos_internos(): void
    {
        $aprendiz = $this->usuario('aprendiz');
        $usernameOriginal = $aprendiz->username;

        // La contraseña propia no se cambia por esta vía (exige la actual).
        $this->como($aprendiz)->putJson('/v1/general-users/' . $aprendiz->id, [
            'nombre' => $aprendiz->nombre,
            'apellido' => $aprendiz->apellido,
            'correo' => $aprendiz->correo,
            'password' => 'nueva123',
            'username' => 'otro_username',
            'must_change_password' => true,
        ])->assertStatus(422);

        $fresco = $aprendiz->fresh();
        $this->assertTrue(Hash::check('123456', $fresco->password));
        $this->assertSame($usernameOriginal, $fresco->username);
        $this->assertFalse((bool) $fresco->must_change_password);

        // Los campos internos se ignoran aunque el update sea válido.
        $this->como($aprendiz)->putJson('/v1/general-users/' . $aprendiz->id, [
            'nombre' => 'Nombre Nuevo',
            'apellido' => $aprendiz->apellido,
            'correo' => $aprendiz->correo,
            'username' => 'otro_username',
        ])->assertOk();

        $this->assertSame($usernameOriginal, $aprendiz->fresh()->username);
        $this->assertSame('Nombre Nuevo', $aprendiz->fresh()->nombre);
    }

    public function test_no_se_puede_reasignar_una_notificacion_a_otro_usuario(): void
    {
        $a = $this->usuario('aprendiz');
        $b = $this->usuario('aprendiz');

        $creada = $this->como($a)->postJson('/v1/notifications', [
            'titulo' => 'Aviso propio',
            'tipo' => 'mensaje',
            'fecha' => now()->toDateString(),
            'id_usuario' => $a->id,
        ])->assertCreated()->json();

        $this->como($a)->putJson('/v1/notifications/' . $creada['id'], [
            'titulo' => 'Aviso propio',
            'tipo' => 'mensaje',
            'fecha' => now()->toDateString(),
            'id_usuario' => $b->id,
        ])->assertOk();

        $this->assertSame($a->id, Notification::findOrFail($creada['id'])->id_usuario);
    }

    // ---------------------------------------------------------------- contraseñas / sesión

    public function test_la_nueva_contrasena_no_puede_repetir_la_actual(): void
    {
        $user = $this->usuario('aprendiz');

        $this->como($user)->putJson('/v1/auth/password', [
            'password_actual' => '123456',
            'password' => '123456',
            'password_confirmation' => '123456',
        ])->assertStatus(422);

        $this->assertTrue(Hash::check('123456', $user->fresh()->password));
    }

    public function test_una_cuenta_suspendida_no_usa_su_token_vigente(): void
    {
        $user = $this->usuario('aprendiz');
        $token = $this->token($user);

        $user->update(['estado' => false]);
        $this->app['auth']->forgetGuards();

        $this->withToken($token)->getJson('/v1/auth/me')->assertStatus(403);
    }
}
