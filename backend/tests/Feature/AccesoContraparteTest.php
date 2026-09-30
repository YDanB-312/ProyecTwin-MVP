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
            'num_trimestres' => 6,
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
}
