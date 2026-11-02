<?php

namespace Tests\Core\Academic;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Catálogos institucionales: lectura pública sin PII, escritura exclusiva del
// admin y borrado en cascada que conserva propuestas e historiales.
class CatalogosTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Catalogos',
            'correo' => $rol . '.' . uniqid() . '@catalogos.local',
            'username' => 'cat_' . uniqid(),
            'password' => Hash::make('ClaveSegura#2026'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    private function como(GeneralUser $user)
    {
        $token = $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => 'ClaveSegura#2026',
        ])->assertOk()->json('token');

        $this->app['auth']->forgetGuards();

        return $this->withToken($token);
    }

    private function red(string $nombre = null): KnowledgeNetwork
    {
        return KnowledgeNetwork::create(['nombre' => $nombre ?? 'Red ' . uniqid()]);
    }

    private function programa(KnowledgeNetwork $red, string $nombre = null): TrainingProgram
    {
        return TrainingProgram::create([
            'nombre' => $nombre ?? 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);
    }

    public function test_los_catalogos_son_publicos_e_incluyen_su_relacion(): void
    {
        $red = $this->red('Red pública ' . uniqid());
        $programa = $this->programa($red, 'Programa público ' . uniqid());

        $redes = $this->getJson('/v1/knowledge-networks')->assertOk();
        $this->assertTrue(collect($redes->json())->contains('nombre', $red->nombre));

        // La relación permitida se incluye; no se exponen fichas ni aprendices.
        $lista = $this->getJson('/v1/training-programs?included=knowledgeNetwork')->assertOk();
        $fila = collect($lista->json())->firstWhere('id', $programa->id);
        $this->assertNotNull($fila);
        $this->assertSame($red->nombre, $fila['knowledgeNetwork']['nombre']);
        $this->assertArrayNotHasKey('apprentices', $fila);
        $this->assertArrayNotHasKey('classGroups', $fila);

        $this->getJson('/v1/training-programs/' . $programa->id . '?included=knowledgeNetwork')
            ->assertOk()
            ->assertJsonPath('knowledgeNetwork.nombre', $red->nombre);
    }

    public function test_solo_el_admin_escribe_los_catalogos(): void
    {
        $admin = $this->usuario('admin');
        $instructor = $this->usuario('instructor');
        $red = $this->red();

        $this->como($instructor)
            ->postJson('/v1/knowledge-networks', ['nombre' => 'Red no autorizada'])
            ->assertStatus(403);

        $this->app['auth']->forgetGuards();
        $creada = $this->como($admin)
            ->postJson('/v1/knowledge-networks', ['nombre' => 'Red admin ' . uniqid()])
            ->assertCreated();

        // El nombre de la red es único.
        $this->app['auth']->forgetGuards();
        $this->como($admin)
            ->postJson('/v1/knowledge-networks', ['nombre' => $creada->json('nombre')])
            ->assertStatus(422);

        $this->app['auth']->forgetGuards();
        $programa = $this->como($admin)
            ->postJson('/v1/training-programs', [
                'nombre' => 'Programa admin ' . uniqid(),
                'nivel' => 'Tecnologo',
                'knowledge_network_id' => $creada->json('id'),
            ])
            ->assertCreated();

        $this->app['auth']->forgetGuards();
        $this->como($admin)
            ->putJson('/v1/training-programs/' . $programa->json('id'), [
                'nombre' => $programa->json('nombre') . ' actualizado',
                'nivel' => 'Tecnico',
                'knowledge_network_id' => $creada->json('id'),
            ])
            ->assertOk()
            ->assertJsonPath('nivel', 'Tecnico');

        $this->app['auth']->forgetGuards();
        $this->como($instructor)
            ->putJson('/v1/training-programs/' . $programa->json('id'), [
                'nombre' => 'Intento ajeno',
                'nivel' => 'Tecnologo',
                'knowledge_network_id' => $red->id,
            ])
            ->assertStatus(403);

        $this->app['auth']->forgetGuards();
        $this->como($admin)
            ->deleteJson('/v1/training-programs/' . $programa->json('id'))
            ->assertOk();
        $this->assertDatabaseMissing('training_programs', ['id' => $programa->json('id')]);
    }

    public function test_borrar_un_programa_conserva_propuestas_y_aprendices(): void
    {
        $admin = $this->usuario('admin');
        $instructorUser = $this->usuario('instructor');
        $aprendizUser = $this->usuario('aprendiz');
        $red = $this->red();
        $programa = $this->programa($red);
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $instructorUser->id,
        ]);
        $ficha = ClassGroup::create([
            'codigo' => 'cat-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha del programa',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
        $aprendiz = Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $aprendizUser->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $programa->id,
        ]);
        $proyecto = Project::create([
            'titulo' => 'Propuesta que debe conservarse',
            'estado' => 'aprobado',
            'id_creador' => $aprendizUser->id,
            'id_class_group' => $ficha->id,
        ]);

        $this->como($admin)
            ->deleteJson('/v1/training-programs/' . $programa->id)
            ->assertOk();

        // La estructura desaparece; la historia académica no.
        $this->assertDatabaseMissing('training_programs', ['id' => $programa->id]);
        $this->assertDatabaseMissing('class_groups', ['id' => $ficha->id]);
        $this->assertDatabaseHas('apprentices', [
            'id' => $aprendiz->id,
            'id_class_group' => null,
            'id_programa' => null,
        ]);
        $this->assertDatabaseHas('projects', [
            'id' => $proyecto->id,
            'id_class_group' => null,
            'titulo' => 'Propuesta que debe conservarse',
        ]);

        // La operación sensible queda en la bitácora.
        $this->assertDatabaseHas('audit_logs', [
            'accion' => 'eliminar_programa',
            'entidad_id' => $programa->id,
        ]);
    }
}
