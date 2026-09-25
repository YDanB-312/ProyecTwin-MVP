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

// Gestión del equipo (pivote): el creador no se puede quitar, el retiro es
// idempotente, no se agregan integrantes de otra ficha y el listado trae el
// aprendiz con su usuario. La ruta PUT se eliminó.
class EquipoTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Equipo',
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

    private function ficha(TrainingProgram $programa): ClassGroup
    {
        $instructor = Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $this->usuario('instructor')->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'eq-' . uniqid(),
            'nombre' => 'Ficha equipo',
            'estado' => 'activo',
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

    private function proyecto(GeneralUser $creador, ClassGroup $ficha): Project
    {
        return Project::create([
            'titulo' => 'Propuesta equipo',
            'resumen' => 'Resumen de la propuesta de equipo para la prueba.',
            'area_aplicacion' => 'Tecnología',
            'estado' => 'pendiente',
            'id_creador' => $creador->id,
            'id_class_group' => $ficha->id,
        ]);
    }

    public function test_no_se_puede_quitar_al_creador(): void
    {
        $ficha = $this->ficha($this->programa());
        $creador = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($creador->generalUser, $ficha);
        $pivote = ApprenticeProject::create([
            'id_aprendiz' => $creador->id,
            'id_proyecto' => $proyecto->id,
        ]);

        $this->como($creador->generalUser)
            ->deleteJson('/v1/apprentice-projects/' . $pivote->id)
            ->assertStatus(409);

        $this->assertDatabaseHas('apprentice_projects', ['id' => $pivote->id]);
    }

    public function test_retirar_a_un_integrante_es_idempotente(): void
    {
        $ficha = $this->ficha($this->programa());
        $creador = $this->aprendizEn($ficha);
        $miembro = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($creador->generalUser, $ficha);
        $pivote = ApprenticeProject::create([
            'id_aprendiz' => $miembro->id,
            'id_proyecto' => $proyecto->id,
        ]);

        $this->como($creador->generalUser)
            ->deleteJson('/v1/apprentice-projects/' . $pivote->id)
            ->assertOk();
        $this->assertDatabaseMissing('apprentice_projects', ['id' => $pivote->id]);

        // Segunda vez: no existe, pero no es un error.
        $this->como($creador->generalUser)
            ->deleteJson('/v1/apprentice-projects/' . $pivote->id)
            ->assertOk();
    }

    public function test_no_se_agrega_un_aprendiz_de_otra_ficha(): void
    {
        $programa = $this->programa();
        $ficha = $this->ficha($programa);
        $otraFicha = $this->ficha($programa);
        $creador = $this->aprendizEn($ficha);
        $ajeno = $this->aprendizEn($otraFicha);
        $proyecto = $this->proyecto($creador->generalUser, $ficha);

        $this->como($creador->generalUser)
            ->postJson('/v1/apprentice-projects', [
                'id_aprendiz' => $ajeno->id,
                'id_proyecto' => $proyecto->id,
            ])
            ->assertStatus(422);

        $this->assertDatabaseMissing('apprentice_projects', [
            'id_aprendiz' => $ajeno->id,
            'id_proyecto' => $proyecto->id,
        ]);
    }

    public function test_el_listado_incluye_el_aprendiz_con_su_usuario(): void
    {
        $ficha = $this->ficha($this->programa());
        $creador = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($creador->generalUser, $ficha);
        ApprenticeProject::create(['id_aprendiz' => $creador->id, 'id_proyecto' => $proyecto->id]);

        $filas = $this->como($creador->generalUser)
            ->getJson('/v1/apprentice-projects?included=apprentice.generalUser')
            ->assertOk()
            ->json();

        $this->assertNotEmpty($filas);
        $this->assertSame($creador->generalUser->nombre, $filas[0]['apprentice']['generalUser']['nombre']);
    }

    public function test_la_ruta_put_del_equipo_ya_no_existe(): void
    {
        $ficha = $this->ficha($this->programa());
        $creador = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($creador->generalUser, $ficha);
        $pivote = ApprenticeProject::create([
            'id_aprendiz' => $creador->id,
            'id_proyecto' => $proyecto->id,
        ]);

        $this->como($creador->generalUser)
            ->putJson('/v1/apprentice-projects/' . $pivote->id, [
                'id_aprendiz' => $creador->id,
                'id_proyecto' => $proyecto->id,
            ])
            ->assertStatus(405);
    }
}
