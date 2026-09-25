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

// Regla Classroom: se PARTICIPA (comentar/editar/eliminar/equipo) solo dentro
// de la ficha ACTIVA. Fuera de la ficha o con la ficha finalizada es solo
// lectura. Instructor de la ficha y admin siempre pueden.
class InteraccionPropuestaTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Inter',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
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

    private function instructor(?GeneralUser &$user = null): Instructor
    {
        $user = $this->usuario('instructor');
        return Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $user->id,
        ]);
    }

    private function ficha(Instructor $instructor, string $estado = 'activo'): ClassGroup
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'in-' . uniqid(),
            'nombre' => 'Ficha interacción',
            'estado' => $estado,
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    private function aprendizEn(?ClassGroup $ficha): Apprentice
    {
        return Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $this->usuario('aprendiz')->id,
            'id_class_group' => $ficha?->id,
            'id_programa' => $ficha?->id_programa,
        ]);
    }

    private function proyecto(Apprentice $creador, ClassGroup $ficha): Project
    {
        return Project::create([
            'titulo' => 'Propuesta interacción',
            'resumen' => 'Resumen de la propuesta para la prueba de interacción.',
            'area_aplicacion' => 'Tecnología',
            'estado' => 'pendiente',
            'id_creador' => $creador->id_usuario,
            'id_class_group' => $ficha->id,
            'id_instructor_asignado' => $ficha->id_instructor,
        ]);
    }

    private function payloadEdicion(Project $p): array
    {
        return [
            'titulo' => 'Editada',
            'resumen' => 'Resumen editado de la propuesta de prueba.',
            'area_aplicacion' => 'Tecnología',
            'id_creador' => $p->id_creador,
        ];
    }

    public function test_fuera_de_la_ficha_ve_pero_no_escribe(): void
    {
        $instructor = $this->instructor();
        $ficha = $this->ficha($instructor);
        $creador = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($creador, $ficha);

        // El creador sale de la ficha (retiro/traslado).
        $creador->update(['id_class_group' => null, 'id_programa' => null]);
        $user = $creador->generalUser;

        // Ve su propuesta (historial).
        $this->como($user)->getJson('/v1/projects/' . $proyecto->id)->assertOk();

        // Pero no comenta, ni edita, ni elimina.
        $this->como($user)->postJson('/v1/comments', [
            'texto' => 'Comentario fuera de la ficha',
            'id_proyecto' => $proyecto->id,
        ])->assertStatus(403);

        $this->como($user)->putJson('/v1/projects/' . $proyecto->id, $this->payloadEdicion($proyecto))
            ->assertStatus(403);

        $this->como($user)->deleteJson('/v1/projects/' . $proyecto->id)->assertStatus(403);
    }

    public function test_un_integrante_del_equipo_dentro_de_la_ficha_si_participa(): void
    {
        $instructor = $this->instructor();
        $ficha = $this->ficha($instructor);
        $creador = $this->aprendizEn($ficha);
        $miembro = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($creador, $ficha);
        ApprenticeProject::create(['id_aprendiz' => $miembro->id, 'id_proyecto' => $proyecto->id]);

        // Cualquier integrante del equipo (en la ficha) comenta y edita.
        $this->como($miembro->generalUser)->postJson('/v1/comments', [
            'texto' => 'Observación del equipo',
            'id_proyecto' => $proyecto->id,
        ])->assertStatus(201);

        $this->como($miembro->generalUser)
            ->putJson('/v1/projects/' . $proyecto->id, $this->payloadEdicion($proyecto))
            ->assertOk();
    }

    public function test_ficha_finalizada_es_solo_lectura_para_el_aprendiz(): void
    {
        $instructor = $this->instructor();
        $ficha = $this->ficha($instructor, 'finalizado');
        $creador = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($creador, $ficha);
        $user = $creador->generalUser;

        $this->como($user)->getJson('/v1/projects/' . $proyecto->id)->assertOk();

        $this->como($user)->postJson('/v1/comments', [
            'texto' => 'Comentario en ficha finalizada',
            'id_proyecto' => $proyecto->id,
        ])->assertStatus(403);

        $this->como($user)->putJson('/v1/projects/' . $proyecto->id, $this->payloadEdicion($proyecto))
            ->assertStatus(403);
    }

    public function test_el_instructor_si_participa_en_ficha_finalizada(): void
    {
        $instructorUser = null;
        $instructor = $this->instructor($instructorUser);
        $ficha = $this->ficha($instructor, 'finalizado');
        $creador = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($creador, $ficha);

        // El instructor puede seguir gestionando el registro.
        $this->como($instructorUser)->postJson('/v1/comments', [
            'texto' => 'Observación del instructor',
            'id_proyecto' => $proyecto->id,
        ])->assertStatus(201);
    }

    public function test_el_admin_siempre_puede(): void
    {
        $instructor = $this->instructor();
        $ficha = $this->ficha($instructor, 'finalizado');
        $creador = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($creador, $ficha);
        $admin = $this->usuario('admin');

        $this->como($admin)->postJson('/v1/comments', [
            'texto' => 'Observación del admin',
            'id_proyecto' => $proyecto->id,
        ])->assertStatus(201);
    }
}
