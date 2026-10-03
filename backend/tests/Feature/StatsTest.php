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

// Conteos agregados y filtros directos: los tableros ya no descargan
// colecciones completas para pintar números o equipos.
class StatsTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Stats',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    private function token(GeneralUser $user): string
    {
        return $this->postJson('/v1/auth/login', ['correo' => $user->correo, 'password' => '123456'])
            ->assertOk()->json('token');
    }

    private function ficha(): array
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
            'codigo' => 'st-' . uniqid(),
            'nombre' => 'Ficha Stats',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
        return [$ficha, $instructor, $programa];
    }

    private function proyecto(GeneralUser $creador, ClassGroup $ficha, Instructor $instructor): Project
    {
        return Project::create([
            'titulo' => 'Propuesta ' . uniqid(),
            'resumen' => 'Resumen de la propuesta',
            'palabras_clave' => 'prueba',
            'area_aplicacion' => 'Test',
            'objetivo_general' => 'Objetivo general',
            'objetivos_especificos' => ['Objetivo uno'],
            'estado' => 'pendiente',
            'id_creador' => $creador->id,
            'id_instructor_asignado' => $instructor->id,
            'id_class_group' => $ficha->id,
        ]);
    }

    public function test_el_admin_recibe_los_conteos_del_tablero(): void
    {
        $admin = $this->usuario('admin');
        $this->usuario('aprendiz');

        $respuesta = $this->withToken($this->token($admin))
            ->getJson('/v1/stats/resumen')
            ->assertOk()
            ->assertJsonStructure([
                'usuarios' => ['total', 'suspendidos', 'porRol'],
                'proyectos' => ['total', 'pendiente', 'aprobado', 'rechazado'],
                'similitudes',
                'reportes' => ['total', 'abiertos'],
                'fichas' => ['total', 'activo', 'finalizado'],
                'notificaciones_no_leidas',
                'motor' => ['umbral', 'meses'],
            ]);

        $this->assertGreaterThanOrEqual(2, $respuesta->json('usuarios.total'));
        $this->assertGreaterThanOrEqual(1, $respuesta->json('usuarios.porRol.aprendiz'));
    }

    public function test_el_aprendiz_recibe_solo_sus_conteos(): void
    {
        $aprendiz = $this->usuario('aprendiz');
        [$ficha, $instructor] = $this->ficha();
        $this->proyecto($aprendiz, $ficha, $instructor);

        $respuesta = $this->withToken($this->token($aprendiz))
            ->getJson('/v1/stats/resumen')
            ->assertOk()
            ->assertJsonStructure(['propuestas', 'propuestas_aprobadas', 'similitudes', 'notificaciones_no_leidas']);

        $this->assertGreaterThanOrEqual(1, $respuesta->json('propuestas'));
    }

    public function test_el_equipo_se_filtra_por_propuesta(): void
    {
        $admin = $this->usuario('admin');
        [$ficha, $instructor] = $this->ficha();
        $creador = $this->usuario('aprendiz');
        $p1 = $this->proyecto($creador, $ficha, $instructor);
        $p2 = $this->proyecto($creador, $ficha, $instructor);

        $usuarioAprendiz = $this->usuario('aprendiz');
        $aprendiz = Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $usuarioAprendiz->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);
        ApprenticeProject::create(['id_aprendiz' => $aprendiz->id, 'id_proyecto' => $p1->id]);
        ApprenticeProject::create(['id_aprendiz' => $aprendiz->id, 'id_proyecto' => $p2->id]);

        $respuesta = $this->withToken($this->token($admin))
            ->getJson('/v1/apprentice-projects?id_proyecto=' . $p1->id)
            ->assertOk();

        $this->assertCount(1, $respuesta->json());
        $this->assertSame($p1->id, $respuesta->json('0.id_proyecto'));
    }
}
