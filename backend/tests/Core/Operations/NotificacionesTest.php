<?php

namespace Tests\Core\Operations;

use App\Models\Apprentice;
use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Notification;
use App\Models\TrainingProgram;
use App\Services\NotificacionesService;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Bandeja de notificaciones: aislamiento por usuario, alcance del instructor,
// marca de lectura y administración. Son las reglas que sostienen las alertas
// de propuestas, similitudes, fichas y soporte.
class NotificacionesTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Notificaciones',
            'correo' => $rol . '.' . uniqid() . '@notificaciones.local',
            'username' => 'notif_' . uniqid(),
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

    private function notificacion(GeneralUser $user, array $extra = []): Notification
    {
        return Notification::create(array_merge([
            'titulo' => 'Aviso de prueba',
            'tipo' => 'sistema',
            'leida' => false,
            'fecha' => now()->toDateString(),
            'id_usuario' => $user->id,
        ], $extra));
    }

    private function fichaConAprendiz(GeneralUser $instructorUser): array
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $programa = TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $instructorUser->id,
        ]);
        $ficha = ClassGroup::create([
            'codigo' => 'nt-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha notificaciones',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
        $aprendiz = $this->usuario('aprendiz');
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $aprendiz->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $programa->id,
        ]);

        return [$ficha, $aprendiz];
    }

    public function test_cada_usuario_solo_ve_su_bandeja_y_el_admin_puede_consultar_otra(): void
    {
        $admin = $this->usuario('admin');
        $instructor = $this->usuario('instructor');
        $aprendiz = $this->usuario('aprendiz');

        $this->notificacion($instructor, ['titulo' => 'Propia del instructor']);
        $this->notificacion($aprendiz, ['titulo' => 'Propia del aprendiz']);

        $propias = $this->como($instructor)->getJson('/v1/notifications')->assertOk();
        $this->assertCount(1, $propias->json());
        $this->assertSame('Propia del instructor', $propias->json('0.titulo'));

        $this->app['auth']->forgetGuards();
        $deAprendiz = $this->como($admin)
            ->getJson('/v1/notifications?id_usuario=' . $aprendiz->id)
            ->assertOk();
        $this->assertCount(1, $deAprendiz->json());
        $this->assertSame('Propia del aprendiz', $deAprendiz->json('0.titulo'));

        // El admin sin filtro ve la suya, no la de todos.
        $this->app['auth']->forgetGuards();
        $this->assertCount(0, $this->como($admin)->getJson('/v1/notifications')->assertOk()->json());
    }

    public function test_el_dueno_marca_leida_y_no_puede_editar_contenido_ni_avisos_ajenos(): void
    {
        $dueno = $this->usuario('aprendiz');
        $otro = $this->usuario('aprendiz');
        $admin = $this->usuario('admin');
        $aviso = $this->notificacion($dueno, ['titulo' => 'Titulo original']);

        $this->como($dueno)
            ->putJson('/v1/notifications/' . $aviso->id, [
                'leida' => true,
                'titulo' => 'Titulo cambiado por el dueno',
            ])
            ->assertOk();

        $aviso->refresh();
        $this->assertSame(1, (int) $aviso->leida);
        $this->assertSame('Titulo original', $aviso->titulo);

        $this->app['auth']->forgetGuards();
        $this->como($otro)
            ->putJson('/v1/notifications/' . $aviso->id, ['leida' => true])
            ->assertStatus(403);

        // El admin sí puede corregir el contenido.
        $this->app['auth']->forgetGuards();
        $this->como($admin)
            ->putJson('/v1/notifications/' . $aviso->id, ['titulo' => 'Titulo corregido por admin'])
            ->assertOk();
        $this->assertSame('Titulo corregido por admin', $aviso->fresh()->titulo);
    }

    public function test_el_instructor_solo_avisa_a_aprendices_de_sus_fichas(): void
    {
        $instructor = $this->usuario('instructor');
        $admin = $this->usuario('admin');
        [$ficha, $aprendizPropio] = $this->fichaConAprendiz($instructor);
        $aprendizAjeno = $this->usuario('aprendiz');

        $datos = [
            'titulo' => 'Revisa el avance de tu propuesta',
            'tipo' => 'mensaje',
            'fecha' => now()->toDateString(),
        ];

        $this->como($instructor)
            ->postJson('/v1/notifications', $datos + ['id_usuario' => $aprendizPropio->id])
            ->assertCreated()
            ->assertJsonPath('id_usuario', $aprendizPropio->id);

        $this->app['auth']->forgetGuards();
        $this->como($instructor)
            ->postJson('/v1/notifications', $datos + ['id_usuario' => $aprendizAjeno->id])
            ->assertStatus(403);

        // El admin puede avisar a cualquiera.
        $this->app['auth']->forgetGuards();
        $this->como($admin)
            ->postJson('/v1/notifications', $datos + ['id_usuario' => $aprendizAjeno->id])
            ->assertCreated();

        // El destinatario y la ficha usados por el alcance son los esperados.
        $this->assertSame($ficha->id, $aprendizPropio->fresh()->apprentice->id_class_group);
    }

    public function test_valida_tipo_enlace_y_reserva_el_borrado_al_admin(): void
    {
        $admin = $this->usuario('admin');
        $aprendiz = $this->usuario('aprendiz');

        $this->como($admin)
            ->postJson('/v1/notifications', [
                'titulo' => 'Enlace invalido',
                'tipo' => 'sistema',
                'enlace' => 'https://ejemplo.test',
                'fecha' => now()->toDateString(),
                'id_usuario' => $aprendiz->id,
            ])
            ->assertStatus(422);

        $this->app['auth']->forgetGuards();
        $this->como($admin)
            ->postJson('/v1/notifications', [
                'titulo' => 'Tipo invalido',
                'tipo' => 'urgente',
                'fecha' => now()->toDateString(),
                'id_usuario' => $aprendiz->id,
            ])
            ->assertStatus(422);

        $this->app['auth']->forgetGuards();
        $creada = $this->como($admin)->postJson('/v1/notifications', [
            'titulo' => 'Propuesta lista para revisar',
            'tipo' => 'revision',
            'enlace' => 'proyecto:42',
            'fecha' => now()->toDateString(),
            'id_usuario' => $aprendiz->id,
        ])->assertCreated();

        $this->app['auth']->forgetGuards();
        $this->como($aprendiz)
            ->deleteJson('/v1/notifications/' . $creada->json('id'))
            ->assertStatus(403);

        $this->app['auth']->forgetGuards();
        $this->como($admin)
            ->deleteJson('/v1/notifications/' . $creada->json('id'))
            ->assertOk();
        $this->assertDatabaseMissing('notifications', ['id' => $creada->json('id')]);
    }

    public function test_nadie_puede_crear_manualmente_una_notificacion_sistema(): void
    {
        $admin = $this->usuario('admin');
        $instructor = $this->usuario('instructor');
        $aprendiz = $this->usuario('aprendiz');

        // Cualquier rol, incluido admin, recibe el rechazo y no deja fila.
        foreach ([[$admin, $aprendiz], [$instructor, $aprendiz], [$aprendiz, $aprendiz]] as [$autor, $destino]) {
            $titulo = 'Sistema manual ' . uniqid();

            $this->app['auth']->forgetGuards();
            $this->como($autor)
                ->postJson('/v1/notifications', [
                    'titulo' => $titulo,
                    'tipo' => 'sistema',
                    'fecha' => now()->toDateString(),
                    'id_usuario' => $destino->id,
                ])
                ->assertStatus(422)
                ->assertJsonPath(
                    'message',
                    'Las notificaciones de tipo "sistema" son generadas automáticamente por el backend y no pueden crearse manualmente.'
                );

            $this->assertDatabaseMissing('notifications', ['titulo' => $titulo]);
        }
    }

    public function test_el_backend_sigue_creando_notificaciones_sistema_internamente(): void
    {
        $solicitante = $this->usuario('aprendiz');
        $reporte = BugReport::create([
            'titulo' => 'No puedo unirme a mi ficha',
            'descripcion' => 'La ficha aparece ocupada.',
            'tipo' => 'sistema',
            'estado' => 'pendiente',
            'fecha' => now()->toDateString(),
            'id_usuario' => $solicitante->id,
        ]);

        app(NotificacionesService::class)->soporteResuelto($reporte);

        $this->assertDatabaseHas('notifications', [
            'id_usuario' => $solicitante->id,
            'tipo' => 'sistema',
            'titulo' => 'Tu solicitud de soporte #' . $reporte->id . ' fue atendida.',
            'enlace' => 'reporte:' . $reporte->id,
        ]);
    }

    public function test_las_notificaciones_manuales_de_otros_tipos_conservan_sus_reglas(): void
    {
        $instructor = $this->usuario('instructor');
        $admin = $this->usuario('admin');
        $aprendiz = $this->usuario('aprendiz');
        [, $aprendizPropio] = $this->fichaConAprendiz($instructor);

        // Instructor → aprendiz de su ficha.
        $this->como($instructor)
            ->postJson('/v1/notifications', [
                'titulo' => 'Revisa el avance de tu propuesta',
                'tipo' => 'mensaje',
                'fecha' => now()->toDateString(),
                'id_usuario' => $aprendizPropio->id,
            ])
            ->assertCreated()
            ->assertJsonPath('tipo', 'mensaje');

        // Usuario → su propia bandeja.
        $this->app['auth']->forgetGuards();
        $this->como($aprendiz)
            ->postJson('/v1/notifications', [
                'titulo' => 'Recordatorio personal',
                'tipo' => 'observacion',
                'fecha' => now()->toDateString(),
                'id_usuario' => $aprendiz->id,
            ])
            ->assertCreated();

        // Admin → cualquiera.
        $this->app['auth']->forgetGuards();
        $this->como($admin)
            ->postJson('/v1/notifications', [
                'titulo' => 'Propuesta lista para revisar',
                'tipo' => 'revision',
                'enlace' => 'proyecto:42',
                'fecha' => now()->toDateString(),
                'id_usuario' => $aprendizPropio->id,
            ])
            ->assertCreated()
            ->assertJsonPath('enlace', 'proyecto:42');
    }

    public function test_put_no_convierte_una_notificacion_manual_en_sistema_pero_si_marca_leida_una_sistema(): void
    {
        $admin = $this->usuario('admin');
        $dueno = $this->usuario('aprendiz');
        $manual = $this->notificacion($dueno, ['tipo' => 'mensaje', 'titulo' => 'Mensaje manual']);
        $sistema = $this->notificacion($dueno, ['tipo' => 'sistema', 'titulo' => 'Aviso del sistema']);

        // Ni el dueño ni el admin convierten una notificación manual en sistema.
        foreach ([$dueno, $admin] as $autor) {
            $this->app['auth']->forgetGuards();
            $this->como($autor)
                ->putJson('/v1/notifications/' . $manual->id, [
                    'tipo' => 'sistema',
                    'leida' => true,
                ])
                ->assertStatus(422)
                ->assertJsonPath(
                    'message',
                    'Las notificaciones de tipo "sistema" son generadas automáticamente por el backend y no pueden crearse manualmente.'
                );

            $manual->refresh();
            $this->assertSame('mensaje', $manual->tipo);
            $this->assertSame(0, (int) $manual->leida);
        }

        // Marcar leída una que ya es sistema sigue funcionando aunque el
        // cliente reenvíe el objeto completo (tipo incluido).
        $this->app['auth']->forgetGuards();
        $this->como($dueno)
            ->putJson('/v1/notifications/' . $sistema->id, [
                'tipo' => 'sistema',
                'leida' => true,
                'titulo' => 'Intento de cambio',
            ])
            ->assertOk();

        $sistema->refresh();
        $this->assertSame(1, (int) $sistema->leida);
        $this->assertSame('sistema', $sistema->tipo);
        $this->assertSame('Aviso del sistema', $sistema->titulo);
    }
}
