<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ApprenticeProject;
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

// El aprendiz puede abrir (solo lectura) la CONTRAPARTE de una similitud propia
// aunque sea de otra ficha; pero no una propuesta ajena sin relación.
class AccesoContraparteTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Contra',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
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

    private function ficha(TrainingProgram $programa, int $instructorId): ClassGroup
    {
        return ClassGroup::create([
            'codigo' => 'contra-' . uniqid(),
            'nombre' => 'Ficha contra',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructorId,
        ]);
    }

    private function aprendiz(GeneralUser $u, ClassGroup $ficha, TrainingProgram $programa): void
    {
        Apprentice::create([
            'codigo' => 'AC-' . uniqid(),
            'id_usuario' => $u->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $programa->id,
        ]);
    }

    private function proyecto(string $titulo, GeneralUser $creador, ClassGroup $ficha, string $estado): Project
    {
        return Project::create([
            'titulo' => $titulo,
            'resumen' => 'Resumen de ' . $titulo,
            'area_aplicacion' => 'Tecnología',
            'estado' => $estado,
            'id_creador' => $creador->id,
            'id_instructor_asignado' => $ficha->id_instructor,
            'id_class_group' => $ficha->id,
        ]);
    }

    private function token(GeneralUser $u): string
    {
        return $this->postJson('/v1/auth/login', [
            'correo' => $u->correo,
            'password' => '123456',
        ])->assertOk()->json('token');
    }

    public function test_el_aprendiz_puede_ver_la_contraparte_de_su_similitud(): void
    {
        $programa = $this->programa();
        $instructor = $this->usuario('instructor');
        $inst = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $instructor->id]);

        $fichaA = $this->ficha($programa, $inst->id);
        $fichaB = $this->ficha($programa, $inst->id);

        $a = $this->usuario('aprendiz');
        $b = $this->usuario('aprendiz');
        $this->aprendiz($a, $fichaA, $programa);
        $this->aprendiz($b, $fichaB, $programa);

        $mia = $this->proyecto('Mi propuesta', $a, $fichaA, 'pendiente');
        $similar = $this->proyecto('Propuesta similar', $b, $fichaB, 'aprobado');   // contraparte
        $ajena = $this->proyecto('Propuesta ajena', $b, $fichaB, 'aprobado');      // sin relación

        [$p1, $p2] = $mia->id < $similar->id ? [$mia->id, $similar->id] : [$similar->id, $mia->id];
        Similarity::create([
            'id_proyecto_1' => $p1,
            'id_proyecto_2' => $p2,
            'porcentaje' => 80,
            'fecha' => now()->toDateString(),
        ]);

        $token = $this->token($a);

        // Contraparte de la similitud → accesible (solo lectura).
        $this->withToken($token)->getJson('/v1/projects/' . $similar->id)->assertOk();

        // Propuesta ajena sin relación → sigue bloqueada.
        $this->withToken($token)->getJson('/v1/projects/' . $ajena->id)->assertStatus(403);
    }

    private function similaridad(Project $x, Project $y): Similarity
    {
        [$p1, $p2] = $x->id < $y->id ? [$x->id, $y->id] : [$y->id, $x->id];
        return Similarity::create([
            'id_proyecto_1' => $p1,
            'id_proyecto_2' => $p2,
            'porcentaje' => 80,
            'fecha' => now()->toDateString(),
        ]);
    }

    public function test_la_contraparte_es_solo_lectura_y_no_expone_datos_ni_pares_ajenos(): void
    {
        $programa = $this->programa();
        $instructor = $this->usuario('instructor');
        $inst = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $instructor->id]);

        $fichaA = $this->ficha($programa, $inst->id);
        $fichaB = $this->ficha($programa, $inst->id);

        $a = $this->usuario('aprendiz');
        $b = $this->usuario('aprendiz');
        $c = $this->usuario('aprendiz');
        $this->aprendiz($a, $fichaA, $programa);
        $this->aprendiz($b, $fichaB, $programa);
        $this->aprendiz($c, $fichaB, $programa);

        $mia = $this->proyecto('Mi propuesta', $a, $fichaA, 'aprobado');
        $similar = $this->proyecto('Propuesta similar', $b, $fichaB, 'aprobado');
        $otra = $this->proyecto('Otra aprobada', $c, $fichaB, 'aprobado');

        $par = $this->similaridad($mia, $similar);
        $ajena = $this->similaridad($similar, $otra);

        // El equipo de la contraparte, para comprobar que no se filtran datos.
        $apB = Apprentice::where('id_usuario', $b->id)->value('id');
        ApprenticeProject::create(['id_aprendiz' => $apB, 'id_proyecto' => $similar->id]);

        $token = $this->token($a);

        // 1) Puede VER la contraparte (solo lectura).
        $this->withToken($token)->getJson('/v1/projects/' . $similar->id)->assertOk();

        // 2) No puede GESTIONARLA.
        $this->withToken($token)->putJson('/v1/projects/' . $similar->id, ['titulo' => 'Hackeado'])->assertStatus(403);
        $this->withToken($token)->deleteJson('/v1/projects/' . $similar->id)->assertStatus(403);
        $this->withToken($token)->postJson('/v1/projects/' . $similar->id . '/enviar')->assertStatus(403);
        $this->withToken($token)->postJson('/v1/apprentice-projects', [
            'id_aprendiz' => $apB,
            'id_proyecto' => $similar->id,
        ])->assertStatus(403);

        // 3) Sin datos personales/sensibles del propietario ni del equipo.
        $this->withToken($token)
            ->getJson('/v1/projects/' . $similar->id . '?included=creator,apprentices.generalUser')
            ->assertOk()
            ->assertJsonMissingPath('creator.numero_documento')
            ->assertJsonMissingPath('creator.username')
            ->assertJsonMissingPath('apprentices.0.generalUser.numero_documento')
            ->assertJsonMissingPath('apprentices.0.generalUser.username');

        // 4) ?proyecto_id de la contraparte: solo los pares que tocan MI propuesta.
        $resp = $this->withToken($token)
            ->getJson('/v1/similarities?proyecto_id=' . $similar->id)
            ->assertOk()->json();
        $this->assertCount(1, $resp);
        $this->assertSame($par->id, $resp[0]['id']);

        // 5) Un proyecto sin relación sigue bloqueado por proyecto_id.
        $this->withToken($token)->getJson('/v1/similarities?proyecto_id=' . $otra->id)->assertStatus(403);

        // 6) La evidencia histórica del par propio se conserva y es visible.
        $par->update(['vigente' => false]);
        $hist = $this->withToken($token)
            ->getJson('/v1/similarities?proyecto_id=' . $similar->id . '&historial=1')
            ->assertOk()->json();
        $this->assertContains($par->id, collect($hist)->pluck('id')->all());
        $this->assertNotContains($ajena->id, collect($hist)->pluck('id')->all());
    }
}
