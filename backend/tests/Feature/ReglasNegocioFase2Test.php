<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\ClassGroup;
use App\Models\Comment;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Fase 2: guardas de programa, degradación de instructor, autor aprendiz,
// alcances de fichas/equipo/comentarios, filtro por programa y cambio de clave.
class ReglasNegocioFase2Test extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol, bool $estado = true): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Fase2',
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

    private function instructor(): Instructor
    {
        return Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $this->usuario('instructor')->id,
        ]);
    }

    private function ficha(TrainingProgram $programa, Instructor $instructor): ClassGroup
    {
        return ClassGroup::create([
            'codigo' => 'f2-' . uniqid(),
            'nombre' => 'Ficha Fase2',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    private function aprendizEn(ClassGroup $ficha): GeneralUser
    {
        $user = $this->usuario('aprendiz');
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $user->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);
        return $user;
    }

    private function proyecto(string $titulo, GeneralUser $creador, ?ClassGroup $ficha = null, ?int $instructorId = null): Project
    {
        return Project::create([
            'titulo' => $titulo,
            'resumen' => 'Resumen de la propuesta de prueba Fase2.',
            'area_aplicacion' => 'Tecnología',
            'estado' => 'pendiente',
            'id_creador' => $creador->id,
            'id_class_group' => $ficha?->id,
            'id_instructor_asignado' => $instructorId,
        ]);
    }

    // ---------------------------------------------------------------- Programa

    public function test_admin_borra_un_programa_con_fichas_en_cascada(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();
        $ficha = $this->ficha($programa, $this->instructor());

        $this->como($admin)->deleteJson('/v1/training-programs/' . $programa->id)->assertOk();
        $this->assertDatabaseMissing('training_programs', ['id' => $programa->id]);
        $this->assertDatabaseMissing('class_groups', ['id' => $ficha->id]);
    }

    public function test_admin_borra_un_programa_con_aprendices_en_cascada(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();
        $aprendiz = Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $this->usuario('aprendiz')->id,
            'id_programa' => $programa->id,
        ]);

        $this->como($admin)->deleteJson('/v1/training-programs/' . $programa->id)->assertOk();
        $this->assertDatabaseMissing('training_programs', ['id' => $programa->id]);
        $this->assertDatabaseMissing('apprentices', ['id' => $aprendiz->id]);
    }

    public function test_se_borra_un_programa_vacio(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();

        $this->como($admin)->deleteJson('/v1/training-programs/' . $programa->id)->assertOk();
        $this->assertDatabaseMissing('training_programs', ['id' => $programa->id]);
    }

    // ---------------------------------------------------------------- Rol

    public function test_degradar_instructor_con_fichas_da_409(): void
    {
        $admin = $this->usuario('admin');
        $instructor = $this->instructor();
        $this->ficha($this->programa(), $instructor);

        $this->como($admin)->putJson('/v1/general-users/' . $instructor->id_usuario, [
            'nombre' => 'Docente',
            'apellido' => 'Fase2',
            'correo' => $instructor->generalUser->correo,
            'rol' => 'aprendiz',
        ])->assertStatus(409);
    }

    public function test_degradar_instructor_sin_fichas_es_valido(): void
    {
        $admin = $this->usuario('admin');
        $instructor = $this->instructor();

        $this->como($admin)->putJson('/v1/general-users/' . $instructor->id_usuario, [
            'nombre' => 'Docente',
            'apellido' => 'Fase2',
            'correo' => $instructor->generalUser->correo,
            'rol' => 'aprendiz',
        ])->assertOk();
    }

    // ---------------------------------------------------------------- Autor

    public function test_instructor_no_crea_propuestas(): void
    {
        $instructor = $this->instructor();

        $this->como($instructor->generalUser)->postJson('/v1/projects', [
            'titulo' => 'Propuesta de instructor',
            'resumen' => 'Resumen de la propuesta de prueba del instructor.',
            'area_aplicacion' => 'Tecnología',
        ])->assertStatus(403);
    }

    public function test_aprendiz_no_reasigna_el_instructor(): void
    {
        $programa = $this->programa();
        $instructor = $this->instructor();
        $otro = $this->instructor();
        $ficha = $this->ficha($programa, $instructor);
        $aprendiz = $this->aprendizEn($ficha);

        $project = $this->proyecto('Mía', $aprendiz, $ficha, $instructor->id);

        $this->como($aprendiz)->putJson('/v1/projects/' . $project->id, [
            'titulo' => 'Mía',
            'resumen' => 'Resumen de la propuesta de prueba Fase2.',
            'area_aplicacion' => 'Tecnología',
            'id_creador' => $aprendiz->id,
            'id_instructor_asignado' => $otro->id, // intento de secuestro
        ])->assertOk();

        $this->assertSame($instructor->id, (int) $project->fresh()->id_instructor_asignado);
    }

    // ---------------------------------------------------------------- Comentarios

    public function test_no_se_comenta_una_propuesta_ajena(): void
    {
        $fichaA = $this->ficha($this->programa(), $this->instructor());
        $fichaB = $this->ficha($this->programa(), $this->instructor());
        $a = $this->aprendizEn($fichaA);
        $b = $this->aprendizEn($fichaB);
        $ajena = $this->proyecto('Ajena', $b, $fichaB);

        $this->como($a)->postJson('/v1/comments', [
            'texto' => 'Comentario intruso',
            'id_proyecto' => $ajena->id,
        ])->assertStatus(403);
    }

    public function test_una_respuesta_no_cruza_de_proyecto(): void
    {
        $ficha = $this->ficha($this->programa(), $this->instructor());
        $a = $this->aprendizEn($ficha);
        $p = $this->proyecto('Propuesta P', $a, $ficha);
        $q = $this->proyecto('Propuesta Q', $a, $ficha);

        $comentarioQ = Comment::create([
            'texto' => 'Comentario en Q',
            'id_proyecto' => $q->id,
            'id_usuario' => $a->id,
        ]);

        $this->como($a)->postJson('/v1/comments', [
            'texto' => 'Respuesta cruzada',
            'id_proyecto' => $p->id,
            'respuesta_a' => $comentarioQ->id,
        ])->assertStatus(422);
    }

    // ---------------------------------------------------------------- Alcances

    public function test_instructor_solo_ve_sus_fichas(): void
    {
        $i1 = $this->instructor();
        $i2 = $this->instructor();
        $f1 = $this->ficha($this->programa(), $i1);
        $f2 = $this->ficha($this->programa(), $i2);

        $ids = collect($this->como($i1->generalUser)->getJson('/v1/class-groups')->assertOk()->json())
            ->pluck('id')->all();

        $this->assertContains($f1->id, $ids);
        $this->assertNotContains($f2->id, $ids);
    }

    public function test_aprendiz_solo_ve_su_ficha(): void
    {
        $ficha = $this->ficha($this->programa(), $this->instructor());
        $otra = $this->ficha($this->programa(), $this->instructor());
        $aprendiz = $this->aprendizEn($ficha);

        $ids = collect($this->como($aprendiz)->getJson('/v1/class-groups')->assertOk()->json())
            ->pluck('id')->all();

        $this->assertSame([$ficha->id], $ids);
        $this->assertNotContains($otra->id, $ids);
    }

    public function test_aprendiz_solo_ve_su_equipo(): void
    {
        $fichaA = $this->ficha($this->programa(), $this->instructor());
        $fichaB = $this->ficha($this->programa(), $this->instructor());
        $a = $this->aprendizEn($fichaA);
        $b = $this->aprendizEn($fichaB);

        $mio = $this->proyecto('Mío', $a, $fichaA);
        $ajeno = $this->proyecto('Ajeno', $b, $fichaB);
        $filaA = Apprentice::where('id_usuario', $a->id)->firstOrFail();
        ApprenticeProject::create(['id_aprendiz' => $filaA->id, 'id_proyecto' => $mio->id]);
        ApprenticeProject::create(['id_aprendiz' => Apprentice::where('id_usuario', $b->id)->value('id'), 'id_proyecto' => $ajeno->id]);

        $ids = collect($this->como($a)->getJson('/v1/apprentice-projects')->assertOk()->json())
            ->pluck('id_proyecto')->all();

        $this->assertContains($mio->id, $ids);
        $this->assertNotContains($ajeno->id, $ids);
    }

    // ---------------------------------------------------------------- Filtro programa

    public function test_el_filtro_por_programa_no_filtra_a_otros(): void
    {
        $admin = $this->usuario('admin');
        $programaA = $this->programa();
        $programaB = $this->programa();

        $fichaA = $this->ficha($programaA, $this->instructor());
        $fichaB = $this->ficha($programaB, $this->instructor());
        $apA = $this->aprendizEn($fichaA);
        $apB = $this->aprendizEn($fichaB);
        // B suspendido: no debe aparecer al filtrar por activos del programa A.
        $apB->update(['estado' => false]);

        $ids = collect($this->como($admin)
            ->getJson('/v1/general-users?' . http_build_query([
                'role' => 'aprendiz',
                'estado' => 'activo',
                'programa' => $programaA->nombre,
            ]))->assertOk()->json())->pluck('id')->all();

        $this->assertContains($apA->id, $ids);
        $this->assertNotContains($apB->id, $ids);
    }

    // ---------------------------------------------------------------- Contraseña

    public function test_cambiar_la_propia_contrasena(): void
    {
        $user = $this->usuario('aprendiz');

        $this->como($user)->putJson('/v1/auth/password', [
            'password_actual' => '123456',
            'password' => 'nuevaClave123',
            'password_confirmation' => 'nuevaClave123',
        ])->assertOk();

        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => 'nuevaClave123',
        ])->assertOk();
    }

    public function test_cambiar_contrasena_con_actual_incorrecta_falla(): void
    {
        $user = $this->usuario('aprendiz');

        $this->como($user)->putJson('/v1/auth/password', [
            'password_actual' => 'incorrecta',
            'password' => 'nuevaClave123',
            'password_confirmation' => 'nuevaClave123',
        ])->assertStatus(422);

        $this->assertTrue(Hash::check('123456', $user->fresh()->password));
    }
}
