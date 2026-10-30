<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\Similarity;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// D-10: las cascadas administrativas conservan los perfiles de aprendiz al
// eliminar programas/redes y mantienen la anulación de fichas con historial.
class BorradoEstructuraTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Estructura',
            'correo' => $rol . '.' . uniqid() . '@estructura.local',
            'username' => 'estructura_' . uniqid(),
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    private function como(GeneralUser $usuario)
    {
        $token = $this->postJson('/v1/auth/login', [
            'correo' => $usuario->correo,
            'password' => '123456',
        ])->assertOk()->json('token');

        $this->app['auth']->forgetGuards();

        return $this->withToken($token);
    }

    private function contexto(): array
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red estructura ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa estructura ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);
        $instructorUser = $this->usuario('instructor');
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $instructorUser->id,
        ]);
        $ficha = ClassGroup::create([
            'codigo' => 'est-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha estructura ' . uniqid(),
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
        $aprendizUser = $this->usuario('aprendiz');
        $aprendiz = Apprentice::create([
            'codigo' => 'EST-' . uniqid(),
            'id_usuario' => $aprendizUser->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $programa->id,
        ]);
        $proyecto = Project::create([
            'titulo' => 'Propuesta estructura ' . uniqid(),
            'resumen' => 'Resumen de prueba para la cascada estructural.',
            'area_aplicacion' => 'Tecnología',
            'estado' => 'pendiente',
            'id_creador' => $aprendizUser->id,
            'id_class_group' => $ficha->id,
        ]);

        return [$red, $programa, $ficha, $aprendiz, $proyecto];
    }

    public function test_eliminar_programa_conserva_aprendiz_y_elimina_sus_dependencias(): void
    {
        [$red, $programa, $ficha, $aprendiz, $proyecto] = $this->contexto();
        $admin = $this->usuario('admin');

        $this->como($admin)
            ->deleteJson('/v1/training-programs/' . $programa->id)
            ->assertOk();

        $this->assertDatabaseMissing('training_programs', ['id' => $programa->id]);
        $this->assertDatabaseMissing('class_groups', ['id' => $ficha->id]);
        $this->assertDatabaseHas('projects', ['id' => $proyecto->id, 'id_class_group' => null]);
        $this->assertDatabaseHas('apprentices', [
            'id' => $aprendiz->id,
            'id_class_group' => null,
            'id_programa' => null,
        ]);
        $this->assertDatabaseHas('general_users', ['id' => $aprendiz->id_usuario]);
        $this->assertDatabaseHas('knowledge_networks', ['id' => $red->id]);
    }

    public function test_eliminar_red_conserva_aprendiz_y_elimina_programa_y_ficha(): void
    {
        [$red, $programa, $ficha, $aprendiz, $proyecto] = $this->contexto();
        $admin = $this->usuario('admin');

        $this->como($admin)
            ->deleteJson('/v1/knowledge-networks/' . $red->id)
            ->assertOk();

        $this->assertDatabaseMissing('knowledge_networks', ['id' => $red->id]);
        $this->assertDatabaseMissing('training_programs', ['id' => $programa->id]);
        $this->assertDatabaseMissing('class_groups', ['id' => $ficha->id]);
        $this->assertDatabaseHas('projects', ['id' => $proyecto->id, 'id_class_group' => null]);
        $this->assertDatabaseHas('apprentices', [
            'id' => $aprendiz->id,
            'id_class_group' => null,
            'id_programa' => null,
        ]);
        $this->assertDatabaseHas('general_users', ['id' => $aprendiz->id_usuario]);
    }

    public function test_programa_con_evidencia_conserva_las_propuestas_y_sus_pares(): void
    {
        [, $programa, $ficha, $aprendiz, $proyecto] = $this->contexto();
        $otroProyecto = Project::create([
            'titulo' => 'Propuesta evidencia estructura ' . uniqid(),
            'resumen' => 'Resumen de prueba para preservar la evidencia compartida.',
            'area_aplicacion' => 'Tecnología',
            'estado' => 'aprobado',
            'id_creador' => $aprendiz->id_usuario,
            'id_class_group' => $ficha->id,
        ]);
        $par = Similarity::create([
            'porcentaje' => 82,
            'fecha' => now()->toDateString(),
            'id_proyecto_1' => min((int) $proyecto->id, (int) $otroProyecto->id),
            'id_proyecto_2' => max((int) $proyecto->id, (int) $otroProyecto->id),
        ]);
        $admin = $this->usuario('admin');

        $this->como($admin)
            ->deleteJson('/v1/training-programs/' . $programa->id)
            ->assertOk();

        $this->assertDatabaseMissing('training_programs', ['id' => $programa->id]);
        $this->assertDatabaseMissing('class_groups', ['id' => $ficha->id]);
        $this->assertDatabaseHas('apprentices', [
            'id' => $aprendiz->id,
            'id_class_group' => null,
            'id_programa' => null,
        ]);
        $this->assertDatabaseHas('projects', ['id' => $proyecto->id, 'id_class_group' => null]);
        $this->assertDatabaseHas('projects', ['id' => $otroProyecto->id, 'id_class_group' => null]);
        $this->assertDatabaseHas('similarities', ['id' => $par->id]);
    }

    public function test_ficha_con_datos_se_anula_y_conserva_propuestas_y_aprendiz(): void
    {
        [, , $ficha, $aprendiz, $proyecto] = $this->contexto();
        $admin = $this->usuario('admin');

        $this->como($admin)
            ->deleteJson('/v1/class-groups/' . $ficha->id)
            ->assertOk()
            ->assertJsonPath('accion', 'anulada');

        $this->assertDatabaseHas('class_groups', [
            'id' => $ficha->id,
            'estado' => 'anulada',
            'numero' => null,
        ]);
        $this->assertDatabaseHas('apprentices', [
            'id' => $aprendiz->id,
            'id_class_group' => $ficha->id,
        ]);
        $this->assertDatabaseHas('projects', ['id' => $proyecto->id]);
    }

    public function test_ficha_vacia_se_elimina_fisicamente(): void
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red vacía ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa vacío ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);
        $instructorUser = $this->usuario('instructor');
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $instructorUser->id,
        ]);
        $ficha = ClassGroup::create([
            'codigo' => 'vac-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha vacía ' . uniqid(),
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
        $admin = $this->usuario('admin');

        $this->como($admin)
            ->deleteJson('/v1/class-groups/' . $ficha->id)
            ->assertOk()
            ->assertJsonPath('accion', 'eliminada');

        $this->assertDatabaseMissing('class_groups', ['id' => $ficha->id]);
    }
}
