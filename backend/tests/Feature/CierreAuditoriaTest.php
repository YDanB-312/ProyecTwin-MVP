<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ApprenticeProject;
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
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Regresión del cierre de auditoría: BUG-001..027 y NEW-001..004.
class CierreAuditoriaTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Cierre',
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

    private function programa(): TrainingProgram
    {
        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        return TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);
    }

    private function ficha(TrainingProgram $programa, GeneralUser $instructorUser, string $estado = 'activo'): ClassGroup
    {
        $instructor = Instructor::create([
            'fecha_ingreso' => '2024-01-01',
            'id_usuario' => $instructorUser->id,
        ]);

        return ClassGroup::create([
            'codigo' => 'ci-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha cierre',
            'estado' => $estado,
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

    private function proyecto(GeneralUser $creador, ClassGroup $ficha, string $estado = 'pendiente'): Project
    {
        return Project::create([
            'titulo' => 'Propuesta ' . uniqid(),
            'resumen' => 'Resumen de prueba del cierre de auditoría.',
            'area_aplicacion' => 'Tecnología',
            'estado' => $estado,
            'id_creador' => $creador->id,
            'id_class_group' => $ficha->id,
        ]);
    }

    // ---------------------------------------------------------------- BUG-001

    public function test_el_include_no_expone_datos_sensibles_de_terceros(): void
    {
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($this->programa(), $instructorUser);
        $maria = $this->aprendizEn($ficha);
        $this->aprendizEn($ficha);

        $resp = $this->como($maria)
            ->getJson('/v1/class-groups/' . $ficha->id . '?included=apprentices.generalUser')
            ->assertOk()->json();

        $primero = collect($resp['apprentices'])->pluck('generalUser')->first();
        $this->assertArrayNotHasKey('numero_documento', $primero);
        $this->assertArrayNotHasKey('tipo_documento', $primero);
        $this->assertArrayNotHasKey('username', $primero);
        $this->assertArrayNotHasKey('must_change_password', $primero);
        $this->assertArrayNotHasKey('credenciales_error', $primero);

        // La administración sí recibe los campos institucionales.
        $admin = $this->usuario('admin');
        $fila = $this->como($admin)
            ->getJson('/v1/general-users?paginado=1')
            ->assertOk()->json('data.0');
        $this->assertArrayHasKey('numero_documento', $fila);
        $this->assertArrayHasKey('username', $fila);
    }

    // ---------------------------------------------------------------- BUG-002

    public function test_instructor_no_traslada_aprendices_de_otra_ficha(): void
    {
        $programa = $this->programa();
        $instructorA = $this->usuario('instructor');
        $instructorB = $this->usuario('instructor');
        $fichaA = $this->ficha($programa, $instructorA);
        $fichaB = $this->ficha($programa, $instructorB);
        $aprendiz = $this->aprendizEn($fichaA);

        // El instructor B no puede llevarlo a su ficha.
        $this->como($instructorB)
            ->postJson('/v1/class-groups/' . $fichaB->id . '/aprendices', [
                'usuario_id' => $aprendiz->id,
            ])
            ->assertStatus(422);
        $this->assertSame(
            (int) $fichaA->id,
            (int) Apprentice::where('id_usuario', $aprendiz->id)->value('id_class_group')
        );

        // El admin sí puede trasladarlo.
        $admin = $this->usuario('admin');
        $this->como($admin)
            ->postJson('/v1/class-groups/' . $fichaB->id . '/aprendices', [
                'usuario_id' => $aprendiz->id,
            ])
            ->assertOk();
        $this->assertSame(
            (int) $fichaB->id,
            (int) Apprentice::where('id_usuario', $aprendiz->id)->value('id_class_group')
        );
    }

    // ---------------------------------------------------------------- BUG-003

    public function test_proyecto_id_autoriza_propuestas_propias_o_contrapartes(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);
        // Un tercero de OTRA ficha: sin relación académica ni similitud.
        $fichaC = $this->ficha($programa, $this->usuario('instructor'));
        $c = $this->aprendizEn($fichaC);

        $pA = $this->proyecto($a, $ficha, 'aprobado');
        $pB = $this->proyecto($b, $ficha, 'aprobado');
        Similarity::create([
            'id_proyecto_1' => $pA->id,
            'id_proyecto_2' => $pB->id,
            'porcentaje' => 80,
            'fecha' => now()->toDateString(),
        ]);

        // La contraparte autorizada puede consultar los pares de su similitud.
        $this->como($b)->getJson('/v1/similarities?proyecto_id=' . $pA->id)->assertOk()->assertJsonCount(1);

        // Un tercero sin relación sigue bloqueado (sin bypass por proyecto_id).
        $this->como($c)->getJson('/v1/similarities?proyecto_id=' . $pA->id)->assertStatus(403);

        // El dueño ve los suyos.
        $this->como($a)->getJson('/v1/similarities?proyecto_id=' . $pA->id)->assertOk()->assertJsonCount(1);
    }

    // ---------------------------------------------------------------- BUG-005

    public function test_intervencion_admin_archiva_y_redetecta(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);

        $texto = 'Herramienta web para controlar inventarios de almacén con alertas de stock y reportes de trazabilidad.';
        $pA = Project::create([
            'titulo' => 'Inventarios del almacén',
            'resumen' => $texto,
            'area_aplicacion' => 'Tecnología',
            'estado' => 'aprobado',
            'id_creador' => $a->id,
            'id_class_group' => $ficha->id,
        ]);
        $pB = Project::create([
            'titulo' => 'Inventarios del almacén',
            'resumen' => $texto,
            'area_aplicacion' => 'Tecnología',
            'estado' => 'aprobado',
            'id_creador' => $b->id,
            'id_class_group' => $ficha->id,
        ]);
        $par = Similarity::create([
            'id_proyecto_1' => $pA->id,
            'id_proyecto_2' => $pB->id,
            'porcentaje' => 100,
            'fecha' => now()->toDateString(),
        ]);

        // El admin interviene la aprobada (cambio de contenido → vuelve a revisión).
        $admin = $this->usuario('admin');
        $this->como($admin)
            ->putJson('/v1/projects/' . $pA->id, [
                'titulo' => 'Inventarios del almacén con trazabilidad',
            ])
            ->assertOk();

        $this->assertSame('pendiente', Project::find($pA->id)->estado);
        // La detección anterior queda como evidencia histórica.
        $this->assertFalse((bool) $par->fresh()->vigente);
        // Y se analiza el contenido nuevo.
        $nueva = Similarity::where(function ($q) use ($pA) {
            $q->where('id_proyecto_1', $pA->id)->orWhere('id_proyecto_2', $pA->id);
        })->where('vigente', true)->first();
        $this->assertNotNull($nueva);
        $this->assertNotSame($par->id, $nueva->id);
    }

    // ---------------------------------------------------------------- BUG-007

    public function test_borrar_instructor_conserva_aprendices_y_equipos(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $fichaA = $this->ficha($programa, $instructorUser);
        $x = $this->aprendizEn($fichaA);

        // Proyecto en OTRA ficha con X en el equipo.
        $otroInstructor = $this->usuario('instructor');
        $fichaB = $this->ficha($programa, $otroInstructor);
        $creador = $this->aprendizEn($fichaB);
        $proyecto = $this->proyecto($creador, $fichaB, 'borrador');
        $pivote = ApprenticeProject::create([
            'id_aprendiz' => Apprentice::where('id_usuario', $x->id)->value('id'),
            'id_proyecto' => $proyecto->id,
        ]);

        $admin = $this->usuario('admin');
        $this->como($admin)->deleteJson('/v1/general-users/' . $instructorUser->id)->assertOk();

        $filaX = Apprentice::where('id_usuario', $x->id)->first();
        $this->assertNotNull($filaX);
        $this->assertNull($filaX->id_class_group);
        $this->assertDatabaseHas('apprentice_projects', ['id' => $pivote->id]);
    }

    // ---------------------------------------------------------------- BUG-008

    public function test_el_reporte_nace_pendiente_aunque_el_cliente_mande_estado(): void
    {
        $aprendiz = $this->usuario('aprendiz');
        $resp = $this->como($aprendiz)
            ->postJson('/v1/bug-reports', [
                'titulo' => 'Falla',
                'descripcion' => 'Descripción',
                'tipo' => 'sistema',
                'estado' => 'resuelto',
                'fecha' => now()->toDateString(),
            ])
            ->assertCreated()
            ->json();

        $this->assertSame('pendiente', $resp['estado']);
    }

    // ---------------------------------------------------------------- BUG-010

    public function test_roster_de_ficha_finalizada_bloqueado_para_instructor(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser, 'finalizado');

        $this->como($instructorUser)
            ->putJson('/v1/class-groups/' . $ficha->id, [
                'numero' => $ficha->numero,
                'nombre' => $ficha->nombre,
                'estado' => 'finalizado',
                'id_programa' => $programa->id,
                'id_instructor' => $ficha->id_instructor,
                'aprendices' => [],
            ])
            ->assertStatus(422);
    }

    // ---------------------------------------------------------------- BUG-013

    public function test_comentario_en_ficha_finalizada_no_se_edita(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser, 'finalizado');
        $autor = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($autor, $ficha, 'aprobado');
        $comment = Comment::create([
            'texto' => 'Observación original',
            'id_proyecto' => $proyecto->id,
            'id_usuario' => $autor->id,
        ]);

        $this->como($autor)
            ->putJson('/v1/comments/' . $comment->id, [
                'texto' => 'Editada',
                'id_proyecto' => $proyecto->id,
            ])
            ->assertStatus(403);
        $this->assertSame('Observación original', $comment->fresh()->texto);
    }

    // ---------------------------------------------------------------- BUG-014

    public function test_instructor_no_edita_aprendiz_sin_ficha(): void
    {
        $instructorUser = $this->usuario('instructor');
        $this->ficha($this->programa(), $instructorUser);

        $suelto = $this->usuario('aprendiz');
        $fila = Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $suelto->id,
            'id_class_group' => null,
            'id_programa' => null,
        ]);

        $this->como($instructorUser)
            ->putJson('/v1/apprentices/' . $fila->id, [
                'codigo' => $fila->codigo,
                'id_usuario' => $suelto->id,
                'id_class_group' => null,
            ])
            ->assertStatus(403);
    }

    // ---------------------------------------------------------------- BUG-016

    public function test_borrar_usuario_limpia_tokens_y_avisos_de_reportes(): void
    {
        $autor = $this->usuario('aprendiz');
        $autor->createToken('test');

        $reporte = \App\Models\BugReport::create([
            'titulo' => 'Falla',
            'descripcion' => 'Descripción',
            'tipo' => 'sistema',
            'estado' => 'pendiente',
            'fecha' => now()->toDateString(),
            'id_usuario' => $autor->id,
        ]);
        $aviso = Notification::create([
            'titulo' => 'Nuevo reporte',
            'tipo' => 'sistema',
            'enlace' => 'reporte:' . $reporte->id,
            'leida' => false,
            'fecha' => now()->toDateString(),
            'id_usuario' => $this->usuario('admin')->id,
        ]);

        $admin = $this->usuario('admin');
        $this->como($admin)->deleteJson('/v1/general-users/' . $autor->id)->assertOk();

        $this->assertSame(0, DB::table('personal_access_tokens')->where('tokenable_id', $autor->id)->count());
        $this->assertDatabaseMissing('notifications', ['id' => $aviso->id]);
    }

    // ---------------------------------------------------------------- BUG-027

    public function test_similitud_manual_valida_rango_y_self_pair(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);
        $pA = $this->proyecto($a, $ficha);
        $pB = $this->proyecto($b, $ficha);

        $this->como($admin)->postJson('/v1/similarities', [
            'porcentaje' => 150,
            'id_proyecto_1' => $pA->id,
            'id_proyecto_2' => $pB->id,
        ])->assertStatus(422);

        $this->como($admin)->postJson('/v1/similarities', [
            'porcentaje' => 50,
            'id_proyecto_1' => $pA->id,
            'id_proyecto_2' => $pA->id,
        ])->assertStatus(422);
    }

    // ---------------------------------------------------------------- NEW-001

    public function test_alta_duplicada_de_aprendiz_da_422(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $ficha = $this->ficha($programa, $instructorUser);
        $aprendiz = $this->aprendizEn($ficha);

        $this->como($admin)
            ->postJson('/v1/apprentices', [
                'codigo' => 'AP-' . uniqid(),
                'id_usuario' => $aprendiz->id,
                'id_class_group' => $ficha->id,
            ])
            ->assertStatus(422);
    }

    // ---------------------------------------------------------------- NEW-002

    public function test_login_con_sesion_no_emite_token(): void
    {
        $user = $this->usuario('aprendiz');
        $antes = DB::table('personal_access_tokens')->count();

        // Petición con sesión (flujo SPA): no debe crear un token Bearer.
        $request = \Illuminate\Http\Request::create('/v1/auth/login', 'POST', [
            'correo' => $user->correo,
            'password' => '123456',
        ]);
        $request->setLaravelSession(app('session.store'));

        $response = app(\App\Http\Controllers\Api\AuthController::class)->login($request);
        $data = json_decode($response->getContent(), true);

        $this->assertNull($data['token']);
        $this->assertSame($antes, DB::table('personal_access_tokens')->count());
    }

    // ---------------------------------------------------------------- NEW-004

    public function test_el_dueno_de_una_notificacion_solo_marca_leida(): void
    {
        $user = $this->usuario('aprendiz');
        $notif = Notification::create([
            'titulo' => 'Original',
            'tipo' => 'sistema',
            'enlace' => null,
            'leida' => false,
            'fecha' => now()->toDateString(),
            'id_usuario' => $user->id,
        ]);

        $this->como($user)
            ->putJson('/v1/notifications/' . $notif->id, [
                'titulo' => 'Hackeado',
                'leida' => true,
            ])
            ->assertOk();

        $this->assertSame('Original', $notif->fresh()->titulo);
        $this->assertTrue((bool) $notif->fresh()->leida);
    }
}
