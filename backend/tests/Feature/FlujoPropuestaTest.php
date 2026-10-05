<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\Comment;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Notification;
use App\Models\Project;
use App\Models\Similarity;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Flujo de propuestas: nace en borrador, se envía explícitamente (validación +
// detección + aviso al instructor) y el instructor solo revisa lo enviado.
class FlujoPropuestaTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Flujo',
            'correo' => $rol . '.' . uniqid() . '@correo.com',
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

    private function ficha(TrainingProgram $programa, GeneralUser $instructorUser): ClassGroup
    {
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $instructorUser->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'fl-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha flujo',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    private function aprendizEn(ClassGroup $ficha): GeneralUser
    {
        $user = $this->usuario('aprendiz');
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $user->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);
        return $user;
    }

    private function borradorCompleto(GeneralUser $aprendiz): array
    {
        return [
            'titulo' => 'Sistema de inventarios para tienda',
            'resumen' => 'Herramienta web para controlar inventarios de almacén con alertas de stock y reportes.',
            'area_aplicacion' => 'Desarrollo Web',
            'objetivo_general' => 'Controlar los inventarios del almacén con alertas por stock mínimo.',
            'objetivos_especificos' => [
                'Registrar entradas y salidas de productos.',
                'Generar reportes de trazabilidad por lote.',
            ],
            'id_creador' => $aprendiz->id,
        ];
    }

    // ---------------------------------------------------------------- Borrador

    public function test_crear_propuesta_nace_en_borrador_sin_notificar(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $aprendiz = $this->aprendizEn($ficha);

        $creada = $this->como($aprendiz)->postJson('/v1/projects', [
            'titulo' => 'Borrador inicial',
            'id_creador' => $aprendiz->id,
        ])->assertCreated();

        $this->assertSame('borrador', $creada->json('estado'));
        // Sin envío no hay aviso al instructor ni detección para esa propuesta.
        $this->assertDatabaseMissing('notifications', ['id_usuario' => $instructorUser->id]);
        $this->assertSame(0, Similarity::where('id_proyecto_1', $creada->json('id'))
            ->orWhere('id_proyecto_2', $creada->json('id'))
            ->count());
    }

    // ---------------------------------------------------------------- Envío

    public function test_enviar_exige_la_propuesta_completa(): void
    {
        $programa = $this->programa();
        $ficha = $this->ficha($programa, $this->usuario('instructor'));
        $aprendiz = $this->aprendizEn($ficha);

        $creada = $this->como($aprendiz)->postJson('/v1/projects', [
            'titulo' => 'Incompleta',
            'id_creador' => $aprendiz->id,
        ])->assertCreated();

        $this->como($aprendiz)
            ->postJson('/v1/projects/' . $creada->json('id') . '/enviar')
            ->assertStatus(422)
            ->assertJsonPath('message', fn ($m) => str_contains($m, 'Completa la propuesta'));
    }

    public function test_enviar_completa_pasa_a_pendiente_detecta_y_notifica(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $aprendiz = $this->aprendizEn($ficha);

        // Corpus aprobado del mismo programa (para que el motor encuentre un par).
        $otro = $this->usuario('aprendiz');
        Project::create([
            'titulo' => 'Sistema de inventarios para tienda',
            'resumen' => 'Herramienta web para controlar inventarios de almacén con alertas de stock y reportes.',
            'area_aplicacion' => 'Desarrollo Web',
            'estado' => 'aprobado',
            'id_creador' => $otro->id,
            'id_class_group' => $ficha->id,
        ]);

        $creada = $this->como($aprendiz)
            ->postJson('/v1/projects', $this->borradorCompleto($aprendiz))
            ->assertCreated();

        $enviada = $this->como($aprendiz)
            ->postJson('/v1/projects/' . $creada->json('id') . '/enviar')
            ->assertOk();

        $this->assertSame('pendiente', $enviada->json('estado'));
        $this->assertNotNull($enviada->json('huella_envio'));

        // El motor corrió en el servidor y guardó el par.
        $this->assertGreaterThan(0, Similarity::count());

        // El instructor recibió el aviso de revisión.
        $this->assertDatabaseHas('notifications', [
            'id_usuario' => $instructorUser->id,
            'tipo' => 'revision',
        ]);

        // Y el aviso de similitudes encontradas por el motor.
        $avisoSim = Notification::where('id_usuario', $instructorUser->id)
            ->where('tipo', 'similitud')
            ->latest('id')
            ->first();
        $this->assertNotNull($avisoSim);
        $this->assertStringContainsString('coincidencia', $avisoSim->titulo);

        // Ya enviada: no se puede volver a enviar.
        $this->como($aprendiz)
            ->postJson('/v1/projects/' . $creada->json('id') . '/enviar')
            ->assertStatus(422);
    }

    public function test_aprendiz_no_edita_una_propuesta_en_revision(): void
    {
        $programa = $this->programa();
        $ficha = $this->ficha($programa, $this->usuario('instructor'));
        $aprendiz = $this->aprendizEn($ficha);

        $creada = $this->como($aprendiz)
            ->postJson('/v1/projects', $this->borradorCompleto($aprendiz))
            ->assertCreated();
        $this->como($aprendiz)->postJson('/v1/projects/' . $creada->json('id') . '/enviar')->assertOk();

        $this->como($aprendiz)
            ->putJson('/v1/projects/' . $creada->json('id'), array_merge(
                $this->borradorCompleto($aprendiz),
                ['titulo' => 'Intento de edición']
            ))
            ->assertStatus(422);
    }

    // ---------------------------------------------------------------- Rechazo y reenvío

    public function test_rechazo_guarda_observacion_y_permite_reenviar_con_cambios(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $aprendiz = $this->aprendizEn($ficha);

        $creada = $this->como($aprendiz)
            ->postJson('/v1/projects', $this->borradorCompleto($aprendiz))
            ->assertCreated();
        $this->como($aprendiz)->postJson('/v1/projects/' . $creada->json('id') . '/enviar')->assertOk();

        // El instructor rechaza con observación.
        $this->como($instructorUser)
            ->putJson('/v1/projects/' . $creada->json('id'), array_merge($creada->json(), [
                'estado' => 'rechazado',
                'observacion' => 'Falta definir el diferencial del proyecto.',
            ]))
            ->assertOk();

        $this->assertSame('rechazado', Project::find($creada->json('id'))->estado);
        $this->assertDatabaseHas('comments', [
            'id_proyecto' => $creada->json('id'),
            'texto' => 'Falta definir el diferencial del proyecto.',
        ]);

        // El aprendiz recibe la notificación con la observación.
        $avisoRechazo = Notification::where('id_usuario', $aprendiz->id)->latest('id')->first();
        $this->assertStringContainsString('fue rechazada', $avisoRechazo->titulo);
        $this->assertStringContainsString('Falta definir el diferencial', $avisoRechazo->titulo);

        // Reenviar sin cambios reales falla.
        $this->como($aprendiz)
            ->postJson('/v1/projects/' . $creada->json('id') . '/enviar')
            ->assertStatus(422)
            ->assertJsonPath('message', fn ($m) => str_contains($m, 'No hay cambios'));

        // Con cambios reales vuelve a revisión.
        $this->como($aprendiz)
            ->putJson('/v1/projects/' . $creada->json('id'), array_merge(
                $this->borradorCompleto($aprendiz),
                ['titulo' => 'Sistema de inventarios con diferencial IoT']
            ))
            ->assertOk();

        $this->como($aprendiz)
            ->postJson('/v1/projects/' . $creada->json('id') . '/enviar')
            ->assertOk()
            ->assertJsonPath('estado', 'pendiente');

        // El instructor recibe el aviso de reenvío.
        $avisoReenvio = Notification::where('id_usuario', $instructorUser->id)->latest('id')->first();
        $this->assertStringContainsString('reenviada', $avisoReenvio->titulo);
    }

    public function test_aprobar_notifica_al_aprendiz(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $aprendiz = $this->aprendizEn($ficha);

        $creada = $this->como($aprendiz)
            ->postJson('/v1/projects', $this->borradorCompleto($aprendiz))
            ->assertCreated();
        $this->como($aprendiz)->postJson('/v1/projects/' . $creada->json('id') . '/enviar')->assertOk();

        $this->como($instructorUser)
            ->putJson('/v1/projects/' . $creada->json('id'), array_merge(
                Project::find($creada->json('id'))->toArray(),
                ['estado' => 'aprobado']
            ))
            ->assertOk();

        $aviso = Notification::where('id_usuario', $aprendiz->id)->latest('id')->first();
        $this->assertStringContainsString('fue aprobada', $aviso->titulo);
    }

    public function test_el_rechazo_sin_observacion_tambien_es_valido(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $aprendiz = $this->aprendizEn($ficha);

        $creada = $this->como($aprendiz)
            ->postJson('/v1/projects', $this->borradorCompleto($aprendiz))
            ->assertCreated();
        $this->como($aprendiz)->postJson('/v1/projects/' . $creada->json('id') . '/enviar')->assertOk();

        $this->como($instructorUser)
            ->putJson('/v1/projects/' . $creada->json('id'), array_merge($creada->json(), [
                'estado' => 'rechazado',
            ]))
            ->assertOk();

        $this->assertSame('rechazado', Project::find($creada->json('id'))->estado);
        $this->assertSame(0, Comment::where('id_proyecto', $creada->json('id'))->count());
    }

    public function test_no_se_aprueba_un_borrador_ni_se_envia_una_aprobada(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $aprendiz = $this->aprendizEn($ficha);

        $creada = $this->como($aprendiz)
            ->postJson('/v1/projects', $this->borradorCompleto($aprendiz))
            ->assertCreated();

        // Aprobar un borrador (nunca enviado) no es una transición válida.
        $this->como($instructorUser)
            ->putJson('/v1/projects/' . $creada->json('id'), array_merge($creada->json(), [
                'estado' => 'aprobado',
            ]))
            ->assertStatus(422);
    }

    // ---------------------------------------------------------------- Historial

    public function test_el_historial_registra_el_ciclo_completo(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $aprendiz = $this->aprendizEn($ficha);
        $ajeno = $this->usuario('aprendiz');

        $creada = $this->como($aprendiz)
            ->postJson('/v1/projects', $this->borradorCompleto($aprendiz))
            ->assertCreated();
        $id = $creada->json('id');

        $this->como($aprendiz)->postJson("/v1/projects/$id/enviar")->assertOk();

        $this->como($instructorUser)
            ->putJson("/v1/projects/$id", array_merge($creada->json(), [
                'estado' => 'rechazado',
                'observacion' => 'Agrega el diferencial.',
            ]))
            ->assertOk();

        $this->como($aprendiz)
            ->putJson("/v1/projects/$id", array_merge($this->borradorCompleto($aprendiz), [
                'titulo' => 'Sistema de inventarios con diferencial IoT',
            ]))
            ->assertOk();
        $this->como($aprendiz)->postJson("/v1/projects/$id/enviar")->assertOk();

        $this->como($instructorUser)
            ->putJson("/v1/projects/$id", array_merge(
                Project::find($id)->toArray(),
                ['estado' => 'aprobado']
            ))
            ->assertOk();

        // El historial queda ordenado del evento más reciente al más antiguo.
        $historial = $this->como($aprendiz)
            ->getJson("/v1/projects/$id/historial?included=user")
            ->assertOk()
            ->json();

        $acciones = array_column($historial, 'accion');
        $this->assertSame(['aprobada', 'reenviada', 'actualizada', 'rechazada', 'enviada', 'creada'], $acciones);

        $rechazo = collect($historial)->firstWhere('accion', 'rechazada');
        $this->assertSame('Agrega el diferencial.', $rechazo['detalle']['observacion']);

        // El instructor de la ficha también lo ve; un tercero no.
        $this->como($instructorUser)->getJson("/v1/projects/$id/historial")->assertOk();
        $this->como($ajeno)->getJson("/v1/projects/$id/historial")->assertStatus(403);
    }
}
