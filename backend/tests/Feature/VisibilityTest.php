<?php

namespace Tests\Feature;

use App\Models\Apprentice;
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

// Privacidad de listados: nadie ve propuestas/perfiles/comentarios fuera de su
// alcance. El instructor sí puede leer en modo lectura las de su mismo programa.
class VisibilityTest extends TestCase
{
    use DatabaseTransactions;

    private TrainingProgram $programa1;
    private TrainingProgram $programa2;
    private ClassGroup $fichaA;
    private ClassGroup $fichaA2;
    private ClassGroup $fichaB;
    private GeneralUser $aprendizA;
    private GeneralUser $aprendizA2;
    private GeneralUser $aprendizB;
    private GeneralUser $instructorA;
    private GeneralUser $admin;
    private Project $projectA;
    private Project $projectA2;
    private Project $projectB;

    protected function setUp(): void
    {
        parent::setUp();

        $this->programa1 = $this->programa();
        $this->programa2 = $this->programa();

        $this->instructorA = $this->usuario('instructor');
        $iA = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $this->instructorA->id]);

        $this->fichaA = $this->ficha($this->programa1, $iA->id);
        $this->fichaA2 = $this->ficha($this->programa1, null); // mismo programa, sin instructor
        $this->fichaB = $this->ficha($this->programa2, null);

        $this->aprendizA = $this->usuario('aprendiz');
        $this->aprendizA2 = $this->usuario('aprendiz');
        $this->aprendizB = $this->usuario('aprendiz');
        $this->admin = $this->usuario('admin');

        $this->aprendiz($this->aprendizA, $this->fichaA);
        $this->aprendiz($this->aprendizA2, $this->fichaA2);
        $this->aprendiz($this->aprendizB, $this->fichaB);

        $this->projectA = $this->proyecto('Propuesta A', $this->aprendizA, $this->fichaA);
        $this->projectA2 = $this->proyecto('Propuesta A2', $this->aprendizA2, $this->fichaA2);
        $this->projectB = $this->proyecto('Propuesta B', $this->aprendizB, $this->fichaB);
    }

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Vis',
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
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);
    }

    private function ficha(TrainingProgram $programa, ?int $instructorId = null): ClassGroup
    {
        // Toda ficha necesita un instructor: se crea uno propio si no se indica.
        if ($instructorId === null) {
            $user = $this->usuario('instructor');
            $instructorId = Instructor::create([
                'fecha_ingreso' => '2024-01-01',
                'id_usuario' => $user->id,
            ])->id;
        }

        return ClassGroup::create([
            'codigo' => 'vis-' . uniqid(),
            'nombre' => 'Ficha vis',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructorId,
        ]);
    }

    private function aprendiz(GeneralUser $user, ClassGroup $ficha): void
    {
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $user->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);
    }

    private function proyecto(string $titulo, GeneralUser $creador, ClassGroup $ficha): Project
    {
        return Project::create([
            'titulo' => $titulo,
            'resumen' => 'Resumen ' . $titulo,
            'area_aplicacion' => 'Tecnología',
            'estado' => 'aprobado',
            'id_creador' => $creador->id,
            'id_class_group' => $ficha->id,
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

    private function idsProyectos(GeneralUser $user): array
    {
        return collect($this->como($user)->getJson('/v1/projects')->assertOk()->json())
            ->pluck('id')->all();
    }

    public function test_el_aprendiz_solo_lista_las_propuestas_de_su_alcance(): void
    {
        $ids = $this->idsProyectos($this->aprendizB);

        $this->assertContains($this->projectB->id, $ids);
        $this->assertNotContains($this->projectA->id, $ids);
        $this->assertNotContains($this->projectA2->id, $ids);
    }

    public function test_el_aprendiz_no_lee_por_id_una_propuesta_de_otra_ficha(): void
    {
        $this->como($this->aprendizB)
            ->getJson('/v1/projects/' . $this->projectA->id)
            ->assertStatus(403);
    }

    public function test_el_aprendiz_si_lee_su_propuesta_y_las_de_su_ficha(): void
    {
        $this->como($this->aprendizA)->getJson('/v1/projects/' . $this->projectA->id)->assertOk();
    }

    public function test_el_docente_lista_solo_las_propuestas_de_sus_fichas(): void
    {
        $ids = $this->idsProyectos($this->instructorA);

        $this->assertContains($this->projectA->id, $ids);
        $this->assertNotContains($this->projectA2->id, $ids);
        $this->assertNotContains($this->projectB->id, $ids);
    }

    public function test_el_docente_lee_en_modo_lectura_las_de_su_mismo_programa(): void
    {
        // Mismo programa, otra ficha → permitido (la UI lo muestra en lectura).
        $this->como($this->instructorA)
            ->getJson('/v1/projects/' . $this->projectA2->id)
            ->assertOk();

        // Otro programa → 403.
        $this->como($this->instructorA)
            ->getJson('/v1/projects/' . $this->projectB->id)
            ->assertStatus(403);
    }

    public function test_el_admin_lista_todas_las_propuestas(): void
    {
        $ids = $this->idsProyectos($this->admin);

        $this->assertContains($this->projectA->id, $ids);
        $this->assertContains($this->projectA2->id, $ids);
        $this->assertContains($this->projectB->id, $ids);
    }

    public function test_las_observaciones_solo_son_visibles_para_los_autores(): void
    {
        $comentarioAjeno = Comment::create([
            'texto' => 'Observación ajena',
            'id_proyecto' => $this->projectB->id,
            'id_usuario' => $this->aprendizB->id,
        ]);

        // El compañero de ficha no ve las observaciones de una propuesta ajena.
        $lista = $this->como($this->aprendizA)
            ->getJson('/v1/comments?id_proyecto=' . $this->projectB->id)
            ->assertOk()
            ->json();
        $this->assertCount(0, $lista);

        $this->como($this->aprendizA)
            ->getJson('/v1/comments/' . $comentarioAjeno->id)
            ->assertStatus(403);
    }

    public function test_los_aprendices_solo_ven_los_de_su_ficha(): void
    {
        $ids = collect($this->como($this->aprendizB)->getJson('/v1/apprentices')->assertOk()->json())
            ->pluck('id_usuario')->all();

        $this->assertContains($this->aprendizB->id, $ids);
        $this->assertNotContains($this->aprendizA->id, $ids);
    }

    public function test_el_docente_solo_ve_los_aprendices_de_sus_fichas(): void
    {
        $ids = collect($this->como($this->instructorA)->getJson('/v1/apprentices')->assertOk()->json())
            ->pluck('id_usuario')->all();

        $this->assertContains($this->aprendizA->id, $ids);
        $this->assertNotContains($this->aprendizB->id, $ids);
    }

    public function test_el_admin_ve_todos_los_aprendices(): void
    {
        $ids = collect($this->como($this->admin)->getJson('/v1/apprentices')->assertOk()->json())
            ->pluck('id_usuario')->all();

        $this->assertContains($this->aprendizA->id, $ids);
        $this->assertContains($this->aprendizB->id, $ids);
    }

    public function test_el_docente_solo_ve_su_propia_fila_de_instructor(): void
    {
        $filas = $this->como($this->instructorA)->getJson('/v1/instructors')->assertOk()->json();

        $this->assertCount(1, $filas);
        $this->assertSame($this->instructorA->id, $filas[0]['id_usuario']);
    }
}
