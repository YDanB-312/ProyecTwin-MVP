<?php

namespace Tests\Core\Operations;

use App\Models\BugReport;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Los campos escalares de la API deben rechazar arreglos con 422; un valor
// compuesto no puede llegar a Eloquent/PDO y convertirse en un 500.
class ValidacionEntradaTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Validacion',
            'correo' => $rol . '.' . uniqid() . '@validacion.local',
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

    private function proyecto(GeneralUser $creador): Project
    {
        return Project::create([
            'titulo' => 'Propuesta de validación',
            'estado' => 'borrador',
            'id_creador' => $creador->id,
        ]);
    }

    public function test_propuesta_rechaza_campos_de_texto_no_escalares(): void
    {
        $admin = $this->usuario('admin');
        $proyecto = $this->proyecto($admin);

        $this->como($admin)
            ->putJson('/v1/projects/' . $proyecto->id, [
                'titulo' => ['no', 'es', 'texto'],
                'resumen' => ['tampoco'],
            ])
            ->assertStatus(422)
            ->assertJsonValidationErrors(['titulo', 'resumen']);

        $this->assertDatabaseHas('projects', [
            'id' => $proyecto->id,
            'titulo' => 'Propuesta de validación',
        ]);
    }

    public function test_observacion_rechaza_texto_y_proyecto_no_escalares(): void
    {
        $admin = $this->usuario('admin');
        $proyecto = $this->proyecto($admin);

        $this->como($admin)
            ->postJson('/v1/comments', [
                'texto' => ['no', 'es', 'texto'],
                'id_proyecto' => [$proyecto->id],
            ])
            ->assertStatus(422)
            ->assertJsonValidationErrors(['texto', 'id_proyecto']);
    }

    public function test_soporte_rechaza_texto_no_escalar_y_estado_nulo(): void
    {
        $admin = $this->usuario('admin');
        $solicitante = $this->usuario('aprendiz');

        $this->como($admin)
            ->postJson('/v1/bug-reports', [
                'descripcion' => ['no', 'es', 'texto'],
                'tipo' => 'sistema',
                'fecha' => now()->toDateString(),
            ])
            ->assertStatus(422)
            ->assertJsonValidationErrors('descripcion');

        $reporte = BugReport::create([
            'titulo' => 'Reporte de validación',
            'descripcion' => 'Descripción válida para la prueba.',
            'tipo' => 'sistema',
            'estado' => 'pendiente',
            'fecha' => now()->toDateString(),
            'id_usuario' => $solicitante->id,
        ]);

        $this->como($admin)
            ->putJson('/v1/bug-reports/' . $reporte->id, [
                'descripcion' => $reporte->descripcion,
                'tipo' => $reporte->tipo,
                'estado' => null,
                'fecha' => $reporte->fecha,
                'id_usuario' => $reporte->id_usuario,
            ])
            ->assertStatus(422)
            ->assertJsonValidationErrors('estado');

        $this->assertSame('pendiente', $reporte->fresh()->estado);
    }

    public function test_notificacion_rechaza_titulo_y_destino_no_escalares(): void
    {
        $admin = $this->usuario('admin');

        $this->como($admin)
            ->postJson('/v1/notifications', [
                'titulo' => ['no', 'es', 'texto'],
                'tipo' => 'sistema',
                'fecha' => now()->toDateString(),
                'id_usuario' => $admin->id,
            ])
            ->assertStatus(422)
            ->assertJsonValidationErrors('titulo');

        $this->como($admin)
            ->postJson('/v1/notifications', [
                'titulo' => 'Aviso de validación',
                'tipo' => 'sistema',
                'fecha' => now()->toDateString(),
                'id_usuario' => [$admin->id],
            ])
            ->assertStatus(422)
            ->assertJsonValidationErrors('id_usuario');
    }

    public function test_alta_de_usuario_rechaza_nombre_no_escalar(): void
    {
        $admin = $this->usuario('admin');

        $this->como($admin)
            ->postJson('/v1/general-users', [
                'nombre' => ['no', 'es', 'texto'],
                'apellido' => 'Prueba',
                'tipo_documento' => 'CC',
                'numero_documento' => (string) random_int(1000000, 9999999),
                'correo' => 'validacion.' . uniqid() . '@test.local',
                'rol' => 'aprendiz',
            ])
            ->assertStatus(422)
            ->assertJsonValidationErrors('nombre');
    }

    public function test_catalogo_y_ficha_rechazan_nombre_no_escalar(): void
    {
        $admin = $this->usuario('admin');
        $red = KnowledgeNetwork::create(['nombre' => 'Red validación ' . uniqid()]);

        $this->como($admin)
            ->postJson('/v1/training-programs', [
                'nombre' => ['no', 'es', 'texto'],
                'nivel' => 'Tecnologo',
                'knowledge_network_id' => $red->id,
            ])
            ->assertStatus(422)
            ->assertJsonValidationErrors('nombre');

        $programa = TrainingProgram::create([
            'nombre' => 'Programa validación ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $this->usuario('instructor')->id,
        ]);

        $this->como($admin)
            ->postJson('/v1/class-groups', [
                'numero' => (string) random_int(100000, 999999),
                'nombre' => ['no', 'es', 'texto'],
                'estado' => 'activo',
                'id_programa' => $programa->id,
                'id_instructor' => $instructor->id,
            ])
            ->assertStatus(422)
            ->assertJsonValidationErrors('nombre');
    }
}
