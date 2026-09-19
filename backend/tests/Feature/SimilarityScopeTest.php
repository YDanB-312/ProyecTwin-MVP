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

// Aislamiento del listado de similitudes: cada rol solo ve lo que le
// corresponde. Antes cualquier autenticado recibía TODOS los pares del sistema.
class SimilarityScopeTest extends TestCase
{
    use DatabaseTransactions;

    private ClassGroup $ficha1;
    private ClassGroup $ficha2;
    private GeneralUser $aprendizA;
    private GeneralUser $aprendizB;
    private GeneralUser $instructor1;
    private GeneralUser $instructor2;
    private GeneralUser $admin;

    private Project $projA;
    private Project $projB;
    private Project $projC;
    private Project $team;
    private Project $projD;

    private Similarity $sim1;
    private Similarity $sim2;
    private Similarity $sim3;

    protected function setUp(): void
    {
        parent::setUp();

        // La base local trae similitudes del seed; para que el alcance sea
        // determinista se limpian (la transacción las restaura al terminar).
        Similarity::query()->delete();

        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);

        $this->instructor1 = $this->usuario('instructor');
        $this->instructor2 = $this->usuario('instructor');
        $i1 = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $this->instructor1->id]);
        $i2 = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $this->instructor2->id]);

        $this->ficha1 = ClassGroup::create([
            'codigo' => 'scp-aaaa', 'numero' => '1', 'nombre' => 'Ficha 1', 'estado' => 'activo',
            'id_programa' => $programa->id, 'id_instructor' => $i1->id,
        ]);
        $this->ficha2 = ClassGroup::create([
            'codigo' => 'scp-bbbb', 'numero' => '2', 'nombre' => 'Ficha 2', 'estado' => 'activo',
            'id_programa' => $programa->id, 'id_instructor' => $i2->id,
        ]);

        $this->aprendizA = $this->usuario('aprendiz');
        $this->aprendizB = $this->usuario('aprendiz');
        $this->admin = $this->usuario('admin');

        $apA = Apprentice::create(['codigo' => 'AP-901', 'id_usuario' => $this->aprendizA->id, 'id_class_group' => $this->ficha1->id, 'id_programa' => $programa->id]);
        $apB = Apprentice::create(['codigo' => 'AP-902', 'id_usuario' => $this->aprendizB->id, 'id_class_group' => $this->ficha2->id, 'id_programa' => $programa->id]);

        $this->projA = $this->proyecto('Propuesta A', $this->aprendizA, $this->ficha1);
        $this->projB = $this->proyecto('Propuesta B', $this->aprendizB, $this->ficha2);
        $this->projC = $this->proyecto('Propuesta C', $this->aprendizB, $this->ficha2);
        $this->projD = $this->proyecto('Propuesta D', $this->aprendizB, $this->ficha2);

        // A participa en el equipo de `team` sin ser el creador.
        $this->team = $this->proyecto('Propuesta en equipo', $this->aprendizB, $this->ficha2);
        $this->team->apprentices()->attach($apA->id);

        $this->sim1 = $this->similaridad($this->projA, $this->projB);
        $this->sim2 = $this->similaridad($this->projB, $this->projC);
        $this->sim3 = $this->similaridad($this->team, $this->projD);

        unset($apB);
    }

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Scope',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    private function proyecto(string $titulo, GeneralUser $creador, ClassGroup $ficha): Project
    {
        return Project::create([
            'titulo' => $titulo,
            'resumen' => 'Resumen de ' . $titulo,
            'area_aplicacion' => 'Tecnología',
            'estado' => 'aprobado',
            'id_creador' => $creador->id,
            'id_class_group' => $ficha->id,
        ]);
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

    private function idsVisibles(GeneralUser $user, array $query = []): array
    {
        $token = $this->token($user);
        // El guard cachea el usuario entre peticiones del mismo test; se reinicia
        // para que el token de cada rol aplique de verdad.
        $this->app['auth']->forgetGuards();

        $res = $this->withToken($token)
            ->getJson('/v1/similarities?' . http_build_query($query))
            ->assertOk()
            ->json();

        return collect($res)->pluck('id')->sort()->values()->all();
    }

    public function test_el_aprendiz_solo_ve_los_pares_de_sus_proyectos(): void
    {
        // A es creador de projA y miembro del equipo de `team`.
        $this->assertSame(
            collect([$this->sim1->id, $this->sim3->id])->sort()->values()->all(),
            $this->idsVisibles($this->aprendizA)
        );
    }

    public function test_el_aprendiz_no_ve_pares_de_proyectos_ajenos(): void
    {
        $this->assertNotContains($this->sim2->id, $this->idsVisibles($this->aprendizA));
    }

    public function test_el_docente_solo_ve_los_pares_de_sus_fichas(): void
    {
        $this->assertSame(
            collect([$this->sim1->id])->sort()->values()->all(),
            $this->idsVisibles($this->instructor1)
        );

        $this->assertSame(
            collect([$this->sim1->id, $this->sim2->id, $this->sim3->id])->sort()->values()->all(),
            $this->idsVisibles($this->instructor2)
        );
    }

    public function test_el_admin_ve_todos_los_pares(): void
    {
        $this->assertSame(
            collect([$this->sim1->id, $this->sim2->id, $this->sim3->id])->sort()->values()->all(),
            $this->idsVisibles($this->admin)
        );
    }

    public function test_related_to_aisla_lo_que_toca_un_proyecto(): void
    {
        $this->assertSame(
            [$this->sim1->id],
            $this->idsVisibles($this->aprendizB, ['related_to' => $this->projA->id])
        );

        $this->assertSame(
            collect([$this->sim1->id, $this->sim2->id])->sort()->values()->all(),
            $this->idsVisibles($this->aprendizB, ['related_to' => $this->projB->id])
        );
    }

    public function test_related_to_acepta_varios_proyectos(): void
    {
        $this->assertSame(
            collect([$this->sim1->id, $this->sim2->id, $this->sim3->id])->sort()->values()->all(),
            $this->idsVisibles($this->aprendizB, ['related_to' => $this->projA->id . ',' . $this->projC->id . ',' . $this->team->id])
        );
    }
}
