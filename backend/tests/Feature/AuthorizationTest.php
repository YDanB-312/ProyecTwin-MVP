<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Autorización por recurso: cada quien solo toca lo suyo (o el admin lo que sea).
class AuthorizationTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol, bool $estado = true): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Authz',
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

    // El guard de Sanctum cachea el usuario entre peticiones del mismo test.
    private function como(GeneralUser $user)
    {
        $token = $this->token($user);
        $this->app['auth']->forgetGuards();
        return $this->withToken($token);
    }

    private function proyectoDe(GeneralUser $creador, string $estado = 'pendiente'): Project
    {
        return Project::create([
            'titulo' => 'Propuesta ' . uniqid(),
            'resumen' => 'Resumen de prueba',
            'area_aplicacion' => 'Tecnología',
            'estado' => $estado,
            'id_creador' => $creador->id,
        ]);
    }

    public function test_un_aprendiz_no_puede_editar_a_otro_usuario(): void
    {
        $a = $this->usuario('aprendiz');
        $b = $this->usuario('aprendiz');

        $this->como($a)
            ->putJson('/v1/general-users/' . $b->id, [
                'nombre' => 'Hackeado',
                'apellido' => 'X',
                'correo' => $b->correo,
                'password' => 'nueva123',
            ])
            ->assertStatus(403);

        $this->assertSame('Aprendiz', $b->fresh()->nombre);
        $this->assertTrue(Hash::check('123456', $b->fresh()->password));
    }

    public function test_un_aprendiz_si_puede_editar_su_propio_perfil(): void
    {
        $a = $this->usuario('aprendiz');

        $this->como($a)
            ->putJson('/v1/general-users/' . $a->id, [
                'nombre' => 'Mi Nombre',
                'apellido' => $a->apellido,
                'correo' => $a->correo,
            ])
            ->assertOk();

        $this->assertSame('Mi Nombre', $a->fresh()->nombre);
    }

    public function test_un_admin_si_puede_editar_a_otro(): void
    {
        $admin = $this->usuario('admin');
        $objetivo = $this->usuario('aprendiz');

        $this->como($admin)
            ->putJson('/v1/general-users/' . $objetivo->id, [
                'nombre' => 'Corregido',
                'apellido' => $objetivo->apellido,
                'correo' => $objetivo->correo,
            ])
            ->assertOk();

        $this->assertSame('Corregido', $objetivo->fresh()->nombre);
    }

    public function test_el_listado_de_usuarios_es_solo_para_admin(): void
    {
        $aprendiz = $this->usuario('aprendiz');
        $admin = $this->usuario('admin');

        $this->como($aprendiz)->getJson('/v1/general-users')->assertStatus(403);
        $this->como($admin)->getJson('/v1/general-users')->assertOk();
    }

    public function test_el_detalle_ajeno_es_privado_pero_el_perfil_publico_no(): void
    {
        $a = $this->usuario('aprendiz');
        $b = $this->usuario('aprendiz');

        $this->como($a)->getJson('/v1/general-users/' . $b->id)->assertStatus(403);

        $this->como($a)
            ->getJson('/v1/general-users/' . $b->id . '/perfil')
            ->assertOk()
            ->assertJsonPath('id', $b->id)
            ->assertJsonMissingPath('estado')
            ->assertJsonMissingPath('password');
    }

    public function test_un_aprendiz_no_borra_propuestas_ajenas(): void
    {
        $a = $this->usuario('aprendiz');
        $b = $this->usuario('aprendiz');
        $proyecto = $this->proyectoDe($b);

        $this->como($a)->deleteJson('/v1/projects/' . $proyecto->id)->assertStatus(403);
        $this->assertDatabaseHas('projects', ['id' => $proyecto->id]);
    }

    public function test_escribir_similitudes_es_exclusivo_del_admin(): void
    {
        $aprendiz = $this->usuario('aprendiz');

        $this->como($aprendiz)
            ->postJson('/v1/similarities', [
                'porcentaje' => 90,
                'id_proyecto_1' => 1,
                'id_proyecto_2' => 2,
            ])
            ->assertStatus(403);
    }

    public function test_la_observacion_se_firma_con_el_token_y_solo_la_borra_su_autor(): void
    {
        $a = $this->usuario('aprendiz');
        $b = $this->usuario('aprendiz');
        $proyecto = $this->proyectoDe($a);

        // Aunque el cliente mande otro id_usuario, se firma con el del token.
        $creada = $this->como($a)
            ->postJson('/v1/comments', [
                'texto' => 'Comentario de prueba',
                'id_proyecto' => $proyecto->id,
                'id_usuario' => $b->id,
            ])
            ->assertCreated()
            ->json();

        $this->assertSame($a->id, $creada['id_usuario']);

        // B no puede borrar la observación de A; A sí.
        $this->como($b)->deleteJson('/v1/comments/' . $creada['id'])->assertStatus(403);
        $this->como($a)->deleteJson('/v1/comments/' . $creada['id'])->assertOk();
    }

    public function test_un_aprendiz_no_crea_notificaciones_para_otros(): void
    {
        $a = $this->usuario('aprendiz');
        $b = $this->usuario('aprendiz');

        $this->como($a)
            ->postJson('/v1/notifications', [
                'titulo' => 'Aviso falso',
                'tipo' => 'mensaje',
                'fecha' => now()->toDateString(),
                'id_usuario' => $b->id,
            ])
            ->assertStatus(403);
    }

    public function test_un_instructor_si_puede_notificar_a_un_aprendiz(): void
    {
        $instructor = $this->usuario('instructor');
        $aprendiz = $this->usuario('aprendiz');

        $this->como($instructor)
            ->postJson('/v1/notifications', [
                'titulo' => 'Tu propuesta fue revisada',
                'tipo' => 'revision',
                'fecha' => now()->toDateString(),
                'id_usuario' => $aprendiz->id,
            ])
            ->assertCreated();
    }

    public function test_solo_un_admin_actualiza_reportes_de_falla(): void
    {
        $aprendiz = $this->usuario('aprendiz');

        $reporte = $this->como($aprendiz)
            ->postJson('/v1/bug-reports', [
                'titulo' => 'Falla',
                'descripcion' => 'Descripción',
                'tipo' => 'sistema',
                'fecha' => now()->toDateString(),
            ])
            ->assertCreated()
            ->json();

        $this->assertSame($aprendiz->id, $reporte['id_usuario']);

        $this->como($aprendiz)
            ->putJson('/v1/bug-reports/' . $reporte['id'], [
                'titulo' => 'Falla',
                'descripcion' => 'Otra',
                'tipo' => 'sistema',
                'fecha' => now()->toDateString(),
                'id_usuario' => $aprendiz->id,
            ])
            ->assertStatus(403);

        $this->como($this->usuario('admin'))
            ->putJson('/v1/bug-reports/' . $reporte['id'], [
                'titulo' => 'Falla',
                'descripcion' => 'Revisada',
                'tipo' => 'sistema',
                'estado' => 'resuelto',
                'fecha' => now()->toDateString(),
                'id_usuario' => $aprendiz->id,
            ])
            ->assertOk();
    }

    public function test_el_equipo_solo_lo_gestiona_el_dueno_o_su_instructor(): void
    {
        $dueno = $this->usuario('aprendiz');
        $ajeno = $this->usuario('aprendiz');
        $proyecto = $this->proyectoDe($dueno);

        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);
        $aprendiz = Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $dueno->id,
            'id_programa' => $programa->id,
        ]);
        $pivote = ApprenticeProject::create([
            'id_aprendiz' => $aprendiz->id,
            'id_proyecto' => $proyecto->id,
        ]);

        // Un tercero no puede gestionar el equipo de una propuesta ajena.
        $this->como($ajeno)->deleteJson('/v1/apprentice-projects/' . $pivote->id)->assertStatus(403);

        // El dueño de la propuesta sí.
        $this->como($dueno)->deleteJson('/v1/apprentice-projects/' . $pivote->id)->assertOk();
    }

    private function instructorConFicha(): array
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);
        $user = $this->usuario('instructor');
        $instructor = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $user->id]);
        $ficha = ClassGroup::create([
            'codigo' => 'au-' . uniqid(),
            'nombre' => 'Ficha autorizada',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);

        return [$user, $instructor, $ficha, $programa];
    }

    public function test_un_instructor_no_edita_fichas_ajenas(): void
    {
        [, , $fichaAjena] = $this->instructorConFicha();
        [$ajeno] = $this->instructorConFicha();

        $this->como($ajeno)
            ->putJson('/v1/class-groups/' . $fichaAjena->id, [
                'codigo' => $fichaAjena->codigo,
                'nombre' => 'Secuestrada',
                'estado' => 'activo',
                'id_programa' => $fichaAjena->id_programa,
            ])
            ->assertStatus(403);

        $this->assertSame('Ficha autorizada', $fichaAjena->fresh()->nombre);
    }

    public function test_un_instructor_si_gestiona_su_ficha_y_no_la_reasigna(): void
    {
        [$user, $instructor, $ficha, $programa] = $this->instructorConFicha();
        [, $otroInstructor] = $this->instructorConFicha();

        $this->como($user)
            ->putJson('/v1/class-groups/' . $ficha->id, [
                'codigo' => $ficha->codigo,
                'nombre' => 'Mi ficha actualizada',
                'estado' => 'activo',
                'id_programa' => $programa->id,
                'id_instructor' => $otroInstructor->id, // intento de reasignar
            ])
            ->assertOk();

        $fresca = $ficha->fresh();
        $this->assertSame('Mi ficha actualizada', $fresca->nombre);
        // La reasignación se ignora: la ficha sigue siendo suya.
        $this->assertSame($instructor->id, $fresca->id_instructor);
    }

    public function test_un_aprendiz_no_dispara_el_motor_sobre_propuestas_ajenas(): void
    {
        $a = $this->usuario('aprendiz');
        $b = $this->usuario('aprendiz');
        $ajena = $this->proyectoDe($b);
        $propia = $this->proyectoDe($a);

        $this->como($a)
            ->postJson('/v1/similarities/detect', ['id_proyecto' => $ajena->id])
            ->assertStatus(403);

        $this->como($a)
            ->postJson('/v1/similarities/detect', ['id_proyecto' => $propia->id])
            ->assertOk();
    }
}
