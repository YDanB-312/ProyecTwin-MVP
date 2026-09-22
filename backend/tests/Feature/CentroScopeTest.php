<?php

namespace Tests\Feature;

use App\Models\Admin;
use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\MotorConfig;
use App\Models\Project;
use App\Models\Similarity;
use App\Models\TrainingCenter;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Fase 2: el admin administra SOLO su centro; el superadmin todo. El motor es
// por centro (umbral/ventana por centro, con valor por defecto heredado) y solo
// compara propuestas del mismo programa y centro.
class CentroScopeTest extends TestCase
{
    use DatabaseTransactions;

    private TrainingProgram $programa;
    private TrainingCenter $centroA;
    private TrainingCenter $centroB;
    private GeneralUser $adminA;
    private GeneralUser $adminB;
    private GeneralUser $superadmin;
    private ClassGroup $fichaA;
    private ClassGroup $fichaB;

    protected function setUp(): void
    {
        parent::setUp();

        $this->programa = $this->programa();
        $this->centroA = TrainingCenter::create(['name' => 'Centro A ' . uniqid()]);
        $this->centroB = TrainingCenter::create(['name' => 'Centro B ' . uniqid()]);

        $this->adminA = $this->adminDe($this->centroA->id);
        $this->adminB = $this->adminDe($this->centroB->id);
        $this->superadmin = $this->usuario('superadmin');

        $this->fichaA = $this->ficha($this->centroA->id);
        $this->fichaB = $this->ficha($this->centroB->id);
    }

    // ---------------------------------------------------------------- Helpers

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Centro',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    private function adminDe(int $centroId): GeneralUser
    {
        $user = $this->usuario('admin');
        Admin::create(['id_usuario' => $user->id, 'training_center_id' => $centroId]);
        return $user;
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

    private function ficha(int $centroId): ClassGroup
    {
        $user = $this->usuario('instructor');
        $instructor = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $user->id]);

        return ClassGroup::create([
            'codigo' => 'cen-' . uniqid(),
            'nombre' => 'Ficha centro',
            'estado' => 'activo',
            'id_programa' => $this->programa->id,
            'id_instructor' => $instructor->id,
            'training_center_id' => $centroId,
        ]);
    }

    private function proyecto(string $titulo, ClassGroup $ficha, string $estado = 'aprobado'): Project
    {
        $aprendizUser = $this->usuario('aprendiz');
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $aprendizUser->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);

        return Project::create([
            'titulo' => $titulo,
            'resumen' => 'Resumen ' . $titulo,
            'area_aplicacion' => 'Tecnología',
            'estado' => $estado,
            'id_creador' => $aprendizUser->id,
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

    // ---------------------------------------------------------------- Fichas

    public function test_el_admin_solo_lista_las_fichas_de_su_centro(): void
    {
        $ids = collect($this->como($this->adminA)->getJson('/v1/class-groups')->assertOk()->json())
            ->pluck('id')->all();

        $this->assertContains($this->fichaA->id, $ids);
        $this->assertNotContains($this->fichaB->id, $ids);
    }

    public function test_el_admin_no_puede_editar_una_ficha_de_otro_centro(): void
    {
        $this->como($this->adminA)
            ->putJson('/v1/class-groups/' . $this->fichaB->id, [
                'codigo' => $this->fichaB->codigo,
                'nombre' => 'Cambio ajeno',
                'estado' => 'activo',
                'id_programa' => $this->programa->id,
                'id_instructor' => $this->fichaB->id_instructor,
            ])
            ->assertStatus(403);
    }

    public function test_el_admin_crea_fichas_solo_en_su_centro(): void
    {
        $instructor = Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $this->usuario('instructor')->id,
        ]);

        $res = $this->como($this->adminA)
            ->postJson('/v1/class-groups', [
                'codigo' => 'nueva-' . uniqid(),
                'nombre' => 'Ficha nueva',
                'estado' => 'activo',
                'id_programa' => $this->programa->id,
                'id_instructor' => $instructor->id,
                // Intento de colarlo en otro centro: el backend lo ignora.
                'training_center_id' => $this->centroB->id,
            ])
            ->assertStatus(201);

        $this->assertSame($this->centroA->id, (int) $res->json('training_center_id'));
    }

    public function test_el_superadmin_ve_todas_las_fichas(): void
    {
        $ids = collect($this->como($this->superadmin)->getJson('/v1/class-groups')->assertOk()->json())
            ->pluck('id')->all();

        $this->assertContains($this->fichaA->id, $ids);
        $this->assertContains($this->fichaB->id, $ids);
    }

    // ---------------------------------------------------------------- Motor por centro

    public function test_el_admin_edita_el_motor_de_su_centro_y_hereda_por_defecto(): void
    {
        // Hereda el valor por defecto (no hay fila de su centro todavía).
        $inicial = $this->como($this->adminA)->getJson('/v1/config-similitud/actual')->assertOk()->json();
        $this->assertTrue($inicial['hereda']);

        // Cambia el umbral de su centro.
        $this->como($this->adminA)
            ->putJson('/v1/config-similitud', ['umbral' => 0.5, 'meses' => 6])
            ->assertOk();

        $propia = $this->como($this->adminA)->getJson('/v1/config-similitud/actual')->assertOk()->json();
        $this->assertFalse($propia['hereda']);
        $this->assertSame(0.5, round((float) $propia['umbral'], 2));

        // El otro centro sigue con el valor por defecto (no se contamina).
        $otro = $this->como($this->adminB)->getJson('/v1/config-similitud/actual')->assertOk()->json();
        $this->assertTrue($otro['hereda']);

        // Restaurar vuelve a heredar.
        $this->como($this->adminA)->deleteJson('/v1/config-similitud')->assertOk();
        $volvio = $this->como($this->adminA)->getJson('/v1/config-similitud/actual')->assertOk()->json();
        $this->assertTrue($volvio['hereda']);
    }

    public function test_el_motor_solo_compara_dentro_del_centro(): void
    {
        $texto = 'Sistema de monitoreo de cultivos con sensores IoT y visualización de datos en tiempo real para optimizar el riego.';
        $base = $this->proyecto($texto, $this->fichaA, 'aprobado');
        $mismoCentro = $this->proyecto($texto, $this->fichaA, 'aprobado');
        $otroCentro = $this->proyecto($texto, $this->fichaB, 'aprobado');
        $origen = $this->proyecto($texto, $this->fichaA, 'pendiente');

        $this->como($this->superadmin)
            ->postJson('/v1/similarities/detect', ['id_proyecto' => $origen->id])
            ->assertOk();

        $pares = Similarity::query()
            ->where(function ($q) use ($origen) {
                $q->where('id_proyecto_1', $origen->id)->orWhere('id_proyecto_2', $origen->id);
            })
            ->get();

        $idsPareja = $pares->flatMap(fn ($s) => [$s->id_proyecto_1, $s->id_proyecto_2])->all();

        $this->assertContains($mismoCentro->id, $idsPareja);
        $this->assertContains($base->id, $idsPareja);
        $this->assertNotContains($otroCentro->id, $idsPareja);
    }

    // ---------------------------------------------------------------- Administradores

    public function test_un_centro_solo_admite_un_administrador(): void
    {
        // adminA ya ocupa el centro A. Un nuevo admin en el mismo centro → 422.
        $nuevo = $this->usuario('admin');

        $this->como($this->superadmin)
            ->postJson('/v1/admins', [
                'id_usuario' => $nuevo->id,
                'training_center_id' => $this->centroA->id,
            ])
            ->assertStatus(422);
    }

    public function test_la_bitacora_esta_disponible_para_admin_y_superadmin(): void
    {
        $this->como($this->adminA)->getJson('/v1/audit-logs')->assertOk();
        $this->como($this->superadmin)->getJson('/v1/audit-logs')->assertOk();
    }

    public function test_el_admin_no_accede_a_rutas_solo_superadmin(): void
    {
        // Catálogos y centros: solo superadmin.
        $this->como($this->adminA)
            ->postJson('/v1/training-centers', ['name' => 'Centro pirata ' . uniqid()])
            ->assertStatus(403);

        $this->como($this->adminA)
            ->getJson('/v1/admins')
            ->assertStatus(403);

        // El superadmin sí puede.
        $this->como($this->superadmin)
            ->getJson('/v1/admins')
            ->assertOk();

        $this->como($this->superadmin)
            ->postJson('/v1/training-centers', ['name' => 'Centro nuevo ' . uniqid()])
            ->assertStatus(201);
    }

    public function test_motor_por_defecto_es_una_sola_fila_global(): void
    {
        $this->assertSame(1, MotorConfig::whereNull('training_center_id')->count());
    }
}

