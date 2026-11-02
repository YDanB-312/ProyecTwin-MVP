<?php

namespace Tests\Core\Users;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\Comment;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Notification;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// La autorización se resuelve antes de validar el payload: un usuario ajeno
// no debe obtener 422 ni descubrir IDs/recursos mediante errores de validación.
class AuthorizationBeforeValidationTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Validacion',
            'correo' => $rol.'.'.uniqid().'@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    private function como(GeneralUser $user)
    {
        $token = $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => '123456',
        ])->assertOk()->json('token');

        $this->app['auth']->forgetGuards();

        return $this->withToken($token);
    }

    private function ficha(Instructor $instructor): ClassGroup
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red '.uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa '.uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'validacion-'.uniqid(),
            'nombre' => 'Ficha validación',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    public function test_propuesta_ajena_con_payload_invalido_responde_403(): void
    {
        $autor = $this->usuario('aprendiz');
        $ajeno = $this->usuario('aprendiz');

        $proyecto = Project::create([
            'titulo' => 'Propuesta ajena',
            'estado' => 'pendiente',
            'id_creador' => $autor->id,
        ]);

        $this->como($ajeno)
            ->putJson('/v1/projects/'.$proyecto->id, [
                'titulo' => '',
                'id_creador' => 999999,
                'estado' => 'invalido',
            ])
            ->assertStatus(403);
    }

    public function test_notificacion_ajena_con_payload_invalido_responde_403(): void
    {
        $autor = $this->usuario('aprendiz');
        $ajeno = $this->usuario('aprendiz');

        $notificacion = Notification::create([
            'titulo' => 'Aviso',
            'tipo' => 'mensaje',
            'fecha' => now()->toDateString(),
            'id_usuario' => $autor->id,
        ]);

        $this->como($ajeno)
            ->putJson('/v1/notifications/'.$notificacion->id, [
                'tipo' => 'invalido',
                'fecha' => 'no-es-fecha',
                'id_usuario' => 999999,
            ])
            ->assertStatus(403);
    }

    public function test_comentario_ajeno_con_payload_invalido_responde_403(): void
    {
        $autor = $this->usuario('aprendiz');
        $ajeno = $this->usuario('aprendiz');
        $proyecto = Project::create([
            'titulo' => 'Propuesta comentada',
            'estado' => 'pendiente',
            'id_creador' => $autor->id,
        ]);
        $comentario = Comment::create([
            'texto' => 'Observación',
            'id_proyecto' => $proyecto->id,
            'id_usuario' => $autor->id,
        ]);

        $this->como($ajeno)
            ->putJson('/v1/comments/'.$comentario->id, [
                'texto' => '',
                'id_proyecto' => 999999,
            ])
            ->assertStatus(403);
    }

    public function test_instructor_ajeno_con_payload_invalido_responde_403(): void
    {
        $instructorA = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $this->usuario('instructor')->id,
        ]);
        $instructorB = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $this->usuario('instructor')->id,
        ]);
        $fichaA = $this->ficha($instructorA);
        $this->ficha($instructorB);

        $aprendiz = $this->usuario('aprendiz');
        $fila = Apprentice::create([
            'codigo' => 'AP-'.uniqid(),
            'id_usuario' => $aprendiz->id,
            'id_class_group' => $fichaA->id,
            'id_programa' => $fichaA->id_programa,
        ]);

        $this->como($instructorB->generalUser)
            ->putJson('/v1/apprentices/'.$fila->id, [
                'codigo' => $fila->codigo,
                'id_usuario' => 999999,
                'id_class_group' => 999999,
                'id_programa' => 999999,
            ])
            ->assertStatus(403);
    }
}
