<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Notification;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Event;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Atomicidad de las escrituras compuestas: si algo falla a mitad, no quedan
// estados parciales (la operación se revierte por completo).
class AtomicidadTest extends TestCase
{
    use DatabaseTransactions;

    // Evento de Eloquent que se fuerza a fallar para provocar el rollback.
    private const EVENTO_FALLO = 'eloquent.creating: App\Models\Notification';

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Atom',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
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

    private function ficha(string $estado = 'activo'): ClassGroup
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);
        $instructor = Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $this->usuario('instructor')->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'at-' . uniqid(),
            'nombre' => 'Ficha atom',
            'estado' => $estado,
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    private function aprendizEn(ClassGroup $ficha): Apprentice
    {
        return Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $this->usuario('aprendiz')->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);
    }

    // Fuerza el fallo del alta de notificación durante la petición.
    private function forzarFalloDeNotificacion(): void
    {
        Event::listen(self::EVENTO_FALLO, function () {
            throw new \RuntimeException('Fallo forzado de notificación');
        });
    }

    public function test_enviar_propuesta_es_atomico(): void
    {
        $ficha = $this->ficha();
        $aprendiz = $this->aprendizEn($ficha);

        // El borrador se crea sin notificar (no falla nada todavía).
        $creada = $this->como($aprendiz->generalUser)->postJson('/v1/projects', [
            'titulo' => 'Propuesta atómica',
            'resumen' => 'Resumen suficientemente largo para la propuesta de prueba.',
            'area_aplicacion' => 'Tecnología',
            'objetivo_general' => 'Validar el envío atómico de la propuesta.',
            'objetivos_especificos' => [
                'Registrar la propuesta completa.',
                'Verificar la atomicidad del envío.',
            ],
        ])->assertCreated();

        // El envío notifica al instructor: si la notificación falla, todo vuelve.
        $this->forzarFalloDeNotificacion();
        try {
            $this->como($aprendiz->generalUser)
                ->postJson('/v1/projects/' . $creada->json('id') . '/enviar')
                ->assertStatus(500);
        } finally {
            Event::forget(self::EVENTO_FALLO);
        }

        // La propuesta sigue en borrador y sin huella de envío (sin cambio parcial).
        $fresca = Project::findOrFail($creada->json('id'));
        $this->assertSame('borrador', $fresca->estado);
        $this->assertNull($fresca->huella_envio);
    }

    public function test_unirse_a_ficha_es_atomico(): void
    {
        $fichaOrigen = $this->ficha();
        $fichaDestino = $this->ficha();
        $aprendiz = $this->aprendizEn($fichaOrigen);
        $this->forzarFalloDeNotificacion();

        try {
            $this->como($aprendiz->generalUser)
                ->postJson('/v1/apprentices/me/ficha', ['codigo' => $fichaDestino->codigo])
                ->assertStatus(500);
        } finally {
            Event::forget(self::EVENTO_FALLO);
        }

        // El aprendiz siguió en su ficha original (no hubo cambio parcial).
        $this->assertSame($fichaOrigen->id, (int) $aprendiz->fresh()->id_class_group);
    }
}
