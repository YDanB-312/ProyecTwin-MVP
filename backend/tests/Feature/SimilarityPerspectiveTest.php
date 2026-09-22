<?php

namespace Tests\Feature;

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

// Regla de perspectiva: una propuesta pendiente puede ver sus coincidencias con
// aprobadas, pero NUNCA figura como coincidencia ajena. En listados solo pares
// con ambas aprobadas; las vistas por propuesta sí muestran la sonda pendiente.
class SimilarityPerspectiveTest extends TestCase
{
    use DatabaseTransactions;

    private GeneralUser $aprobada;      // dueña de la propuesta aprobada
    private GeneralUser $pendiente;     // dueña de la propuesta pendiente
    private GeneralUser $instructorUser;
    private GeneralUser $admin;
    private Project $projectAprobada;
    private Project $projectPendiente;
    private Similarity $par;

    protected function setUp(): void
    {
        parent::setUp();

        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);

        $this->instructorUser = $this->usuario('instructor');
        $i = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $this->instructorUser->id]);
        $ficha = ClassGroup::create([
            'codigo' => 'sp-' . uniqid(),
            'nombre' => 'Ficha perspectiva',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $i->id,
        ]);

        $this->aprobada = $this->usuario('aprendiz');
        $this->pendiente = $this->usuario('aprendiz');
        $this->admin = $this->usuario('admin');

        $this->projectAprobada = $this->proyecto('Propuesta aprobada', $this->aprobada, $ficha, 'aprobado');
        $this->projectPendiente = $this->proyecto('Propuesta pendiente', $this->pendiente, $ficha, 'pendiente');

        $this->par = $this->similaridad($this->projectAprobada, $this->projectPendiente);
    }

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Persp',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    // Crea un instructor nuevo y devuelve el id de su fila de perfil.
    private function instructorId(): int
    {
        $user = $this->usuario('instructor');
        return Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $user->id,
        ])->id;
    }

    private function proyecto(string $titulo, GeneralUser $creador, ClassGroup $ficha, string $estado): Project
    {
        $p = Project::create([
            'titulo' => $titulo,
            'resumen' => 'Resumen ' . $titulo,
            'area_aplicacion' => 'Tecnología',
            'estado' => $estado,
            'id_creador' => $creador->id,
            'id_class_group' => $ficha->id,
        ]);

        // El creador entra al pivote (como hace el alta real).
        $ap = \App\Models\Apprentice::firstOrCreate(
            ['id_usuario' => $creador->id],
            [
                'codigo' => 'AP-' . uniqid(),
                'id_class_group' => $ficha->id,
                'id_programa' => $ficha->id_programa,
            ]
        );
        \App\Models\ApprenticeProject::firstOrCreate(['id_aprendiz' => $ap->id, 'id_proyecto' => $p->id]);

        return $p;
    }

    private function similaridad(Project $a, Project $b): Similarity
    {
        [$p1, $p2] = $a->id < $b->id ? [$a->id, $b->id] : [$b->id, $a->id];

        return Similarity::create([
            'id_proyecto_1' => $p1,
            'id_proyecto_2' => $p2,
            'porcentaje' => 55,
            'fecha' => now()->toDateString(),
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

    private function idsListado(GeneralUser $user, array $query = []): array
    {
        return collect($this->como($user)->getJson('/v1/similarities?' . http_build_query($query))->assertOk()->json())
            ->pluck('id')->all();
    }

    public function test_el_dueno_de_la_aprobada_no_ve_la_pendiente(): void
    {
        $this->assertNotContains($this->par->id, $this->idsListado($this->aprobada));

        $this->como($this->aprobada)
            ->getJson('/v1/similarities/' . $this->par->id)
            ->assertStatus(403);
    }

    public function test_el_dueno_de_la_pendiente_si_ve_su_coincidencia_aprobada(): void
    {
        $this->assertContains($this->par->id, $this->idsListado($this->pendiente));

        $this->como($this->pendiente)
            ->getJson('/v1/similarities/' . $this->par->id)
            ->assertOk();
    }

    public function test_el_instructor_no_la_ve_en_el_listado_pero_si_por_propuesta(): void
    {
        $this->assertNotContains($this->par->id, $this->idsListado($this->instructorUser));

        // Vista por propuesta (revisión): sí aparece su coincidencia aprobada.
        $this->assertContains(
            $this->par->id,
            $this->idsListado($this->instructorUser, ['proyecto_id' => $this->projectPendiente->id])
        );

        // Y puede abrir el detalle desde la revisión.
        $this->como($this->instructorUser)
            ->getJson('/v1/similarities/' . $this->par->id)
            ->assertOk();
    }

    public function test_el_admin_tampoco_la_ve_en_listado_pero_si_por_propuesta(): void
    {
        $this->assertNotContains($this->par->id, $this->idsListado($this->admin));

        $this->assertContains(
            $this->par->id,
            $this->idsListado($this->admin, ['proyecto_id' => $this->projectPendiente->id])
        );
    }

    public function test_si_ambas_son_mias_se_muestra(): void
    {
        // Mismo aprendiz con una aprobada y una pendiente, relacionadas.
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);
        $ficha = ClassGroup::create([
            'codigo' => 'sp2-' . uniqid(),
            'nombre' => 'Ficha perspectiva 2',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $this->instructorId(),
        ]);

        $mia1 = $this->proyecto('Mía aprobada', $this->aprobada, $ficha, 'aprobado');
        $mia2 = $this->proyecto('Mía pendiente', $this->aprobada, $ficha, 'pendiente');
        $parMio = $this->similaridad($mia1, $mia2);

        $this->assertContains($parMio->id, $this->idsListado($this->aprobada));
    }

    public function test_la_vista_por_propuesta_exige_poder_ver_la_propuesta(): void
    {
        // Un aprendiz de otra ficha no puede pedir los pares de una propuesta ajena.
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);
        $otraFicha = ClassGroup::create([
            'codigo' => 'sp3-' . uniqid(),
            'nombre' => 'Ficha ajena',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $this->instructorId(),
        ]);
        $ajeno = $this->usuario('aprendiz');
        \App\Models\Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $ajeno->id,
            'id_class_group' => $otraFicha->id,
            'id_programa' => $programa->id,
        ]);

        $this->como($ajeno)
            ->getJson('/v1/similarities?proyecto_id=' . $this->projectPendiente->id)
            ->assertStatus(403);
    }
}
