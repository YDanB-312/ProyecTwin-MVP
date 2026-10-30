<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\AuditLog;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\Similarity;
use App\Models\TrainingProgram;
use App\Support\SesionesUsuario;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// D-01: sin confirmación explícita, el borrado de usuarios verifica la
// evidencia compartida de su subárbol —propuestas de sus fichas como instructor
// y propuestas propias— antes de mutar nada. Con cualquier par (vigente o
// histórico, interno o cross) responde 409 y conserva todo. La guarda aplica
// también a cuentas admin. Con `confirmar_eliminacion`, el administrador puede
// eliminar definitivamente al usuario y sus registros; la desactivación sigue
// siendo la alternativa que conserva autoría, propuestas y similitudes.
class BorradoUsuarioEvidenciaTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Borrado',
            'correo' => $rol . '.' . uniqid() . '@borrado.local',
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

    private function origenStateful(): string
    {
        $dominio = trim((string) (config('sanctum.stateful')[0] ?? 'localhost'));

        return 'http://' . ltrim($dominio, '/');
    }

    private function loginConSesion(GeneralUser $user)
    {
        return $this->withHeader('Origin', $this->origenStateful())
            ->postJson('/v1/auth/login', [
                'correo' => $user->correo,
                'password' => '123456',
            ]);
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

    private function instructorDe(): array
    {
        $user = $this->usuario('instructor');
        $fila = Instructor::create(['fecha_ingreso' => '2024-01-01', 'id_usuario' => $user->id]);

        return [$user, $fila];
    }

    private function ficha(TrainingProgram $programa, Instructor $instructor, string $nombre = null): ClassGroup
    {
        return ClassGroup::create([
            'codigo' => 'bu-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => $nombre ?? 'Ficha ' . uniqid(),
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

    private function proyecto(GeneralUser $creador, ClassGroup $ficha, string $estado = 'pendiente'): Project
    {
        return Project::create([
            'titulo' => 'Propuesta ' . uniqid(),
            'resumen' => 'Resumen de prueba del borrado de usuarios con evidencia.',
            'area_aplicacion' => 'Desarrollo Web',
            'estado' => $estado,
            'id_creador' => $creador->id,
            'id_class_group' => $ficha->id,
        ]);
    }

    private function par(Project $a, Project $b, bool $vigente = true): Similarity
    {
        return Similarity::create([
            'porcentaje' => 80,
            'fecha' => now()->toDateString(),
            'id_proyecto_1' => $a->id,
            'id_proyecto_2' => $b->id,
            'vigente' => $vigente,
        ]);
    }

    private function msgUsuario(): string
    {
        return 'No puedes eliminar al usuario: sus propuestas o las de sus fichas tienen similitudes compartidas (vigentes o históricas). La evidencia pertenece también a las demás propuestas.';
    }

    private function confirmarEliminacionDefinitiva(GeneralUser $admin, GeneralUser $target)
    {
        $inicial = $this->como($admin)
            ->deleteJson('/v1/general-users/' . $target->id);
        $inicial->assertStatus(409);

        return $this->como($admin)
            ->deleteJson('/v1/general-users/' . $target->id, [
                'confirmar_eliminacion' => true,
                'confirmacion_hash' => $inicial->json('confirmacion_hash'),
            ]);
    }

    // ------------------------------------------------------------ bloqueos

    public function test_instructor_con_par_vigente_responde_409_y_conserva_todo(): void
    {
        $programa = $this->programa();
        [$userA, $instrA] = $this->instructorDe();
        [$userB, $instrB] = $this->instructorDe();
        $fichaA = $this->ficha($programa, $instrA, 'Ficha A');
        $fichaB = $this->ficha($programa, $instrB, 'Ficha B');
        $pA = $this->proyecto($this->aprendizEn($fichaA), $fichaA, 'aprobado');
        $pB = $this->proyecto($this->aprendizEn($fichaB), $fichaB, 'aprobado');
        $par = $this->par($pA, $pB, true);

        $this->como($this->usuario('admin'))
            ->deleteJson('/v1/general-users/' . $userA->id)
            ->assertStatus(409)
            ->assertJsonPath('message', $this->msgUsuario());

        $this->assertDatabaseHas('general_users', ['id' => $userA->id]);
        $this->assertDatabaseHas('instructors', ['id' => $instrA->id]);
        $this->assertDatabaseHas('class_groups', ['id' => $fichaA->id]);
        $this->assertDatabaseHas('projects', ['id' => $pA->id]);
        $this->assertDatabaseHas('projects', ['id' => $pB->id]);
        $this->assertDatabaseHas('similarities', ['id' => $par->id]);
    }

    public function test_instructor_con_par_historico_tambien_bloquea(): void
    {
        $programa = $this->programa();
        [$userA, $instrA] = $this->instructorDe();
        [$userB, $instrB] = $this->instructorDe();
        $fichaA = $this->ficha($programa, $instrA);
        $fichaB = $this->ficha($programa, $instrB);
        $pA = $this->proyecto($this->aprendizEn($fichaA), $fichaA, 'rechazado');
        $pB = $this->proyecto($this->aprendizEn($fichaB), $fichaB, 'aprobado');
        $par = $this->par($pA, $pB, false);

        $this->como($this->usuario('admin'))
            ->deleteJson('/v1/general-users/' . $userA->id)
            ->assertStatus(409)
            ->assertJsonPath('message', $this->msgUsuario());

        $this->assertFalse((bool) $par->fresh()->vigente);
        $this->assertDatabaseHas('general_users', ['id' => $userA->id]);
        $this->assertDatabaseHas('instructors', ['id' => $instrA->id]);
        $this->assertDatabaseHas('class_groups', ['id' => $fichaA->id]);
        $this->assertDatabaseHas('projects', ['id' => $pA->id]);
        $this->assertDatabaseHas('similarities', ['id' => $par->id]);
    }

    public function test_aprendiz_creador_con_par_cross_responde_409_y_conserva_la_contraparte(): void
    {
        $programa = $this->programa();
        [$userInstrA, $instrA] = $this->instructorDe();
        [$userInstrB, $instrB] = $this->instructorDe();
        $fichaA = $this->ficha($programa, $instrA);
        $fichaB = $this->ficha($programa, $instrB);
        $creador = $this->aprendizEn($fichaA);
        $contraparte = $this->aprendizEn($fichaB);
        $pA = $this->proyecto($creador, $fichaA, 'aprobado');
        $pB = $this->proyecto($contraparte, $fichaB, 'aprobado');
        $par = $this->par($pA, $pB, true);

        $this->como($this->usuario('admin'))
            ->deleteJson('/v1/general-users/' . $creador->id)
            ->assertStatus(409)
            ->assertJsonPath('message', $this->msgUsuario());

        $this->assertDatabaseHas('general_users', ['id' => $creador->id]);
        $this->assertDatabaseHas('projects', ['id' => $pA->id]);
        $this->assertDatabaseHas('similarities', ['id' => $par->id]);
        // La contraparte (otra ficha) queda intacta.
        $this->assertDatabaseHas('general_users', ['id' => $contraparte->id]);
        $this->assertDatabaseHas('projects', ['id' => $pB->id]);
    }

    // ------------------------------------------------------------ permitidos

    public function test_aprendiz_creador_con_propuesta_sin_similitud_requiere_confirmacion_y_luego_se_borra(): void
    {
        $programa = $this->programa();
        [$userInstr, $instr] = $this->instructorDe();
        $ficha = $this->ficha($programa, $instr);
        $creador = $this->aprendizEn($ficha);
        $proyecto = $this->proyecto($creador, $ficha);
        $admin = $this->usuario('admin');

        // Caso B sin similitudes: la propuesta ya es historial relevante.
        $inicial = $this->como($admin)
            ->deleteJson('/v1/general-users/' . $creador->id);
        $inicial->assertStatus(409)
            ->assertJsonPath('requiere_confirmacion', true)
            ->assertJsonPath('impacto.propuestas', 1);

        $this->assertDatabaseHas('general_users', ['id' => $creador->id]);
        $this->assertDatabaseHas('projects', ['id' => $proyecto->id]);

        $this->como($admin)
            ->deleteJson('/v1/general-users/' . $creador->id, [
                'confirmar_eliminacion' => true,
                'confirmacion_hash' => $inicial->json('confirmacion_hash'),
            ])
            ->assertOk();

        $this->assertDatabaseMissing('general_users', ['id' => $creador->id]);
        $this->assertDatabaseMissing('projects', ['id' => $proyecto->id]);
        // La ficha y su instructor no forman parte del subárbol del aprendiz.
        $this->assertDatabaseHas('class_groups', ['id' => $ficha->id]);
        $this->assertDatabaseHas('instructors', ['id' => $instr->id]);
    }

    public function test_aprendiz_sin_propuestas_se_borra(): void
    {
        $admin = $this->usuario('admin');
        $aprendiz = $this->usuario('aprendiz');

        $this->como($admin)
            ->deleteJson('/v1/general-users/' . $aprendiz->id)
            ->assertOk();

        $this->assertDatabaseMissing('general_users', ['id' => $aprendiz->id]);
    }

    public function test_borrar_usuario_revoca_sus_sesiones_y_conserva_las_ajenas(): void
    {
        $aprendiz = $this->usuario('aprendiz');
        $otro = $this->usuario('aprendiz');

        foreach ([$aprendiz, $otro] as $usuario) {
            DB::table('sessions')->insert([
                'id' => 'sesion-' . uniqid(),
                'user_id' => $usuario->id,
                'ip_address' => '127.0.0.1',
                'user_agent' => 'test',
                'payload' => '',
                'last_activity' => time(),
            ]);
        }

        $this->como($this->usuario('admin'))
            ->deleteJson('/v1/general-users/' . $aprendiz->id)
            ->assertOk();

        $this->assertDatabaseMissing('sessions', ['user_id' => $aprendiz->id]);
        $this->assertDatabaseHas('sessions', ['user_id' => $otro->id]);
    }

    public function test_admin_objetivo_con_par_tambien_responde_409_y_conserva_evidencia(): void
    {
        $solicitante = $this->usuario('admin');
        $objetivo = $this->usuario('admin');
        $programa = $this->programa();
        [$userInstr, $instr] = $this->instructorDe();
        $ficha = $this->ficha($programa, $instr);
        $contraparte = $this->aprendizEn($ficha);

        $pObjetivo = $this->proyecto($objetivo, $ficha, 'aprobado');
        $pContraparte = $this->proyecto($contraparte, $ficha, 'aprobado');
        $par = $this->par($pObjetivo, $pContraparte, true);

        $this->como($solicitante)
            ->deleteJson('/v1/general-users/' . $objetivo->id)
            ->assertStatus(409)
            ->assertJsonPath('message', $this->msgUsuario())
            ->assertJsonPath('requiere_confirmacion', true)
            ->assertJsonPath('puede_desactivar', true)
            ->assertJsonPath('accion_recomendada', 'desactivar')
            ->assertJsonPath('impacto.propuestas', 1);

        $this->assertDatabaseHas('general_users', ['id' => $objetivo->id]);
        $this->assertDatabaseHas('projects', ['id' => $pObjetivo->id]);
        $this->assertDatabaseHas('similarities', ['id' => $par->id]);
        $this->assertDatabaseHas('projects', ['id' => $pContraparte->id]);
        $this->assertDatabaseHas('audit_logs', [
            'accion' => 'eliminar_usuario_bloqueado',
            'entidad' => 'general_users',
            'entidad_id' => $objetivo->id,
        ]);
    }

    public function test_desactivar_usuario_con_propuestas_conserva_autoria_y_registros(): void
    {
        $programa = $this->programa();
        [$userInstr, $instr] = $this->instructorDe();
        $ficha = $this->ficha($programa, $instr);
        $creador = $this->aprendizEn($ficha);
        $contraparte = $this->aprendizEn($ficha);
        $pA = $this->proyecto($creador, $ficha, 'aprobado');
        $pB = $this->proyecto($contraparte, $ficha, 'aprobado');
        $par = $this->par($pA, $pB, true);

        $this->como($this->usuario('admin'))
            ->putJson('/v1/general-users/' . $creador->id, ['estado' => false])
            ->assertOk();

        $this->assertDatabaseHas('general_users', ['id' => $creador->id, 'estado' => false]);
        $this->assertDatabaseHas('apprentices', ['id_usuario' => $creador->id]);
        $this->assertDatabaseHas('projects', ['id' => $pA->id, 'id_creador' => $creador->id]);
        $this->assertDatabaseHas('projects', ['id' => $pB->id, 'id_creador' => $contraparte->id]);
        $this->assertDatabaseHas('similarities', ['id' => $par->id]);
    }

    public function test_eliminacion_definitiva_con_propuestas_y_similitudes_aplica_cascada_y_audita(): void
    {
        $programa = $this->programa();
        [$userInstr, $instr] = $this->instructorDe();
        $ficha = $this->ficha($programa, $instr);
        $creador = $this->aprendizEn($ficha);
        $contraparte = $this->aprendizEn($ficha);
        $pA = $this->proyecto($creador, $ficha, 'aprobado');
        $pB = $this->proyecto($contraparte, $ficha, 'aprobado');
        $par = $this->par($pA, $pB, true);
        $admin = $this->usuario('admin');
        $correo = $creador->correo;

        // Primer intento: no muta nada y deja rastro del bloqueo.
        $inicial = $this->como($admin)
            ->deleteJson('/v1/general-users/' . $creador->id);
        $inicial->assertStatus(409);

        $this->assertDatabaseHas('general_users', ['id' => $creador->id]);
        $this->assertDatabaseHas('projects', ['id' => $pA->id]);
        $this->assertDatabaseHas('similarities', ['id' => $par->id]);
        $this->assertDatabaseHas('audit_logs', [
            'id_usuario' => $admin->id,
            'accion' => 'eliminar_usuario_bloqueado',
            'entidad_id' => $creador->id,
        ]);

        // Segundo paso: confirmación explícita y cascada completa.
        $this->como($admin)
            ->deleteJson('/v1/general-users/' . $creador->id, [
                'confirmar_eliminacion' => true,
                'confirmacion_hash' => $inicial->json('confirmacion_hash'),
            ])
            ->assertOk();

        $this->assertDatabaseMissing('general_users', ['id' => $creador->id]);
        $this->assertDatabaseMissing('apprentices', ['id_usuario' => $creador->id]);
        $this->assertDatabaseMissing('projects', ['id' => $pA->id]);
        $this->assertDatabaseMissing('similarities', ['id' => $par->id]);
        // La contraparte y su propuesta pertenecen a otra persona: no se tocan.
        $this->assertDatabaseHas('general_users', ['id' => $contraparte->id]);
        $this->assertDatabaseHas('projects', ['id' => $pB->id]);
        $this->assertDatabaseHas('class_groups', ['id' => $ficha->id]);

        $log = AuditLog::where('accion', 'eliminar_usuario_definitivo')
            ->where('entidad', 'general_users')
            ->where('entidad_id', $creador->id)
            ->latest('id')
            ->firstOrFail();

        $this->assertSame((int) $admin->id, (int) $log->id_usuario);
        $this->assertSame((int) $admin->id, (int) ($log->detalle['actor_id'] ?? 0));
        $this->assertNotNull($log->created_at);
        $this->assertSame('explicita', $log->detalle['confirmacion'] ?? null);
        $this->assertTrue((bool) ($log->detalle['evidencia_compartida'] ?? false));
        $this->assertSame(1, $log->detalle['impacto']['propuestas'] ?? null);
        $this->assertSame(1, $log->detalle['impacto']['similitudes'] ?? null);
        $this->assertStringNotContainsString($correo, json_encode($log->detalle));
    }

    public function test_eliminacion_definitiva_de_instructor_conserva_aprendices_y_elimina_su_subarbol(): void
    {
        $programa = $this->programa();
        [$target, $instr] = $this->instructorDe();
        [$otroUser, $otroInstr] = $this->instructorDe();
        $ficha = $this->ficha($programa, $instr);
        $fichaExterna = $this->ficha($programa, $otroInstr);
        $aprendiz = $this->aprendizEn($ficha);
        $contraparte = $this->aprendizEn($fichaExterna);
        $pA = $this->proyecto($aprendiz, $ficha, 'aprobado');
        $pB = $this->proyecto($contraparte, $fichaExterna, 'aprobado');
        $par = $this->par($pA, $pB, true);
        $asignada = $this->proyecto($contraparte, $fichaExterna, 'aprobado');
        $asignada->update(['id_instructor_asignado' => $instr->id]);

        $this->confirmarEliminacionDefinitiva($this->usuario('admin'), $target)
            ->assertOk();

        $this->assertDatabaseMissing('general_users', ['id' => $target->id]);
        $this->assertDatabaseMissing('instructors', ['id' => $instr->id]);
        $this->assertDatabaseMissing('class_groups', ['id' => $ficha->id]);
        $this->assertDatabaseMissing('projects', ['id' => $pA->id]);
        $this->assertDatabaseMissing('similarities', ['id' => $par->id]);
        // D-10: el aprendiz de la ficha se conserva, sin estructura.
        $this->assertDatabaseHas('apprentices', [
            'id_usuario' => $aprendiz->id,
            'id_class_group' => null,
            'id_programa' => null,
        ]);
        $this->assertDatabaseHas('general_users', ['id' => $aprendiz->id]);
        // La ficha y la propuesta externas no pertenecen al subárbol eliminado.
        $this->assertDatabaseHas('class_groups', ['id' => $fichaExterna->id]);
        $this->assertDatabaseHas('projects', ['id' => $pB->id]);
        // La asignación docente se conserva como proyecto sin instructor.
        $this->assertDatabaseHas('projects', [
            'id' => $asignada->id,
            'id_instructor_asignado' => null,
        ]);
    }

    public function test_admin_objetivo_con_par_puede_eliminarse_definitivamente_con_confirmacion(): void
    {
        $solicitante = $this->usuario('admin');
        $objetivo = $this->usuario('admin');
        $programa = $this->programa();
        [$userInstr, $instr] = $this->instructorDe();
        $ficha = $this->ficha($programa, $instr);
        $contraparte = $this->aprendizEn($ficha);

        $pObjetivo = $this->proyecto($objetivo, $ficha, 'aprobado');
        $pContraparte = $this->proyecto($contraparte, $ficha, 'aprobado');
        $par = $this->par($pObjetivo, $pContraparte, true);

        $this->confirmarEliminacionDefinitiva($solicitante, $objetivo)
            ->assertOk();

        $this->assertDatabaseMissing('general_users', ['id' => $objetivo->id]);
        $this->assertDatabaseMissing('projects', ['id' => $pObjetivo->id]);
        $this->assertDatabaseMissing('similarities', ['id' => $par->id]);
        $this->assertDatabaseHas('general_users', ['id' => $solicitante->id]);
        $this->assertDatabaseHas('projects', ['id' => $pContraparte->id]);
    }

    public function test_impacto_cambiado_entre_confirmaciones_exige_nueva_confirmacion(): void
    {
        $programa = $this->programa();
        [$userInstr, $instr] = $this->instructorDe();
        $ficha = $this->ficha($programa, $instr);
        $creador = $this->aprendizEn($ficha);
        $pA = $this->proyecto($creador, $ficha, 'aprobado');
        $admin = $this->usuario('admin');

        $inicial = $this->como($admin)
            ->deleteJson('/v1/general-users/' . $creador->id)
            ->assertStatus(409);
        $hashViejo = $inicial->json('confirmacion_hash');
        $this->assertNotNull($hashViejo);

        // Nueva relación después de la primera confirmación.
        $pNueva = $this->proyecto($creador, $ficha, 'pendiente');

        $cambiada = $this->como($admin)
            ->deleteJson('/v1/general-users/' . $creador->id, [
                'confirmar_eliminacion' => true,
                'confirmacion_hash' => $hashViejo,
            ]);
        $cambiada->assertStatus(409)
            ->assertJsonPath('requiere_confirmacion', true)
            ->assertJsonPath('impacto.propuestas', 2);

        $hashNuevo = $cambiada->json('confirmacion_hash');
        $this->assertNotSame($hashViejo, $hashNuevo);
        $this->assertDatabaseHas('general_users', ['id' => $creador->id]);
        $this->assertDatabaseHas('projects', ['id' => $pA->id]);
        $this->assertDatabaseHas('projects', ['id' => $pNueva->id]);

        $bloqueo = AuditLog::where('accion', 'eliminar_usuario_bloqueado')
            ->where('entidad_id', $creador->id)
            ->latest('id')
            ->firstOrFail();
        $this->assertSame('impacto_cambiado', $bloqueo->detalle['motivo'] ?? null);

        $this->como($admin)
            ->deleteJson('/v1/general-users/' . $creador->id, [
                'confirmar_eliminacion' => true,
                'confirmacion_hash' => $hashNuevo,
            ])
            ->assertOk();

        $this->assertDatabaseMissing('general_users', ['id' => $creador->id]);
        $this->assertDatabaseMissing('projects', ['id' => $pA->id]);
        $this->assertDatabaseMissing('projects', ['id' => $pNueva->id]);
    }

    public function test_fallo_de_auditoria_revierte_la_eliminacion_definitiva(): void
    {
        $programa = $this->programa();
        [$userInstr, $instr] = $this->instructorDe();
        $ficha = $this->ficha($programa, $instr);
        $creador = $this->aprendizEn($ficha);
        $contraparte = $this->aprendizEn($ficha);
        $pA = $this->proyecto($creador, $ficha, 'aprobado');
        $pB = $this->proyecto($contraparte, $ficha, 'aprobado');
        $par = $this->par($pA, $pB, true);
        $admin = $this->usuario('admin');

        $inicial = $this->como($admin)
            ->deleteJson('/v1/general-users/' . $creador->id)
            ->assertStatus(409);
        $hash = $inicial->json('confirmacion_hash');

        AuditLog::creating(function () {
            throw new \RuntimeException('fallo de auditoría simulado');
        });

        try {
            $this->como($admin)
                ->deleteJson('/v1/general-users/' . $creador->id, [
                    'confirmar_eliminacion' => true,
                    'confirmacion_hash' => $hash,
                ])
                ->assertStatus(500);
        } finally {
            AuditLog::flushEventListeners();
        }

        $this->assertDatabaseHas('general_users', ['id' => $creador->id]);
        $this->assertDatabaseHas('projects', ['id' => $pA->id]);
        $this->assertDatabaseHas('similarities', ['id' => $par->id]);
        $this->assertSame(0, AuditLog::where('accion', 'eliminar_usuario_definitivo')->count());
    }

    public function test_usuario_no_admin_no_puede_desactivar_ni_eliminar_definitivamente_a_otro(): void
    {
        $objetivo = $this->usuario('aprendiz');
        $intruso = $this->usuario('aprendiz');

        $this->como($intruso)
            ->deleteJson('/v1/general-users/' . $objetivo->id, ['confirmar_eliminacion' => true])
            ->assertStatus(403);

        $this->assertDatabaseHas('general_users', ['id' => $objetivo->id]);

        $this->como($intruso)
            ->putJson('/v1/general-users/' . $objetivo->id, ['estado' => false])
            ->assertStatus(403);

        $this->assertTrue($objetivo->fresh()->estado);
    }

    public function test_suspender_usuario_revoca_tokens_sesiones_y_bloquea_login(): void
    {
        $admin = $this->usuario('admin');
        $objetivo = $this->usuario('aprendiz');
        $objetivo->forceFill(['remember_token' => 'cookie-recordar'])->save();

        $tokenObjetivo = $this->postJson('/v1/auth/login', [
            'correo' => $objetivo->correo,
            'password' => '123456',
        ])->assertOk()->json('token');

        DB::table('sessions')->insert([
            'id' => 'sesion-suspension-' . uniqid(),
            'user_id' => $objetivo->id,
            'ip_address' => '127.0.0.1',
            'user_agent' => 'test',
            'payload' => '',
            'last_activity' => time(),
        ]);

        $this->como($admin)
            ->putJson('/v1/general-users/' . $objetivo->id, ['estado' => false])
            ->assertOk();

        $this->assertDatabaseHas('general_users', ['id' => $objetivo->id, 'estado' => false]);
        $rememberNuevo = $objetivo->fresh()->remember_token;
        $this->assertNotSame('cookie-recordar', $rememberNuevo);
        $this->assertNotNull($rememberNuevo);
        $this->assertDatabaseMissing('sessions', ['user_id' => $objetivo->id]);
        $this->assertSame(0, DB::table('personal_access_tokens')
            ->where('tokenable_id', $objetivo->id)
            ->count());

        // El token anterior ya no autentica y el login queda bloqueado.
        $this->app['auth']->forgetGuards();
        $this->withToken($tokenObjetivo)->getJson('/v1/auth/me')->assertStatus(401);

        $this->postJson('/v1/auth/login', [
            'correo' => $objetivo->correo,
            'password' => '123456',
        ])->assertStatus(403)
          ->assertJsonPath('message', 'Cuenta suspendida. Contacta al administrador.');
    }

    public function test_recordarme_crea_version_de_sesion_sin_reabrir_sesiones_antiguas(): void
    {
        $user = $this->usuario('aprendiz');
        $user->forceFill(['remember_token' => 'recordar-' . uniqid()])->save();

        $recaller = $user->id . '|' . $user->remember_token . '|' . $user->password;
        $cookieName = auth('web')->getRecallerName();

        $this->app['auth']->forgetGuards();
        $this->withoutToken()
            ->withCredentials()
            ->withCookie($cookieName, $recaller)
            ->withHeader('Origin', $this->origenStateful())
            ->getJson('/v1/auth/me')
            ->assertOk()
            ->assertJsonPath('id', $user->id);
    }

    public function test_sesion_de_archivo_no_revive_tras_reactivacion(): void
    {
        $user = $this->usuario('aprendiz');

        $login1 = $this->loginConSesion($user)->assertOk();
        $this->assertNull($login1->json('token'));
        $cookie1 = $login1->getCookie(config('session.cookie'), false);
        $this->assertNotNull($cookie1);
        $cookie1 = $cookie1->getValue();

        $login2 = $this->loginConSesion($user)->assertOk();
        $cookie2 = $login2->getCookie(config('session.cookie'), false);
        $this->assertNotNull($cookie2);
        $cookie2 = $cookie2->getValue();

        // Ambas sesiones son válidas antes de desactivar.
        $this->app['auth']->forgetGuards();
        $this->withoutToken()
            ->withCredentials()
            ->withCookie(config('session.cookie'), $cookie1)
            ->withHeader('Origin', $this->origenStateful())
            ->getJson('/v1/auth/me')
            ->assertOk();

        // Se usa el mismo mecanismo del endpoint de suspensión, sin mezclar
        // el token del admin con la cookie de sesión bajo prueba.
        $user->update(['estado' => false]);
        SesionesUsuario::revocar($user->fresh());

        // La sesión 1 no puede acceder mientras la cuenta está desactivada.
        $this->app['auth']->forgetGuards();
        $this->withoutToken()
            ->withCredentials()
            ->withCookie(config('session.cookie'), $cookie1)
            ->withHeader('Origin', $this->origenStateful())
            ->getJson('/v1/auth/me')
            ->assertStatus(403);

        $user->update(['estado' => true]);

        // La sesión 2 no se usó durante la suspensión, pero la versión cambió:
        // reactivar no debe resucitarla.
        $this->app['auth']->forgetGuards();
        $this->withoutToken()
            ->withCredentials()
            ->withCookie(config('session.cookie'), $cookie2)
            ->withHeader('Origin', $this->origenStateful())
            ->getJson('/v1/auth/me')
            ->assertStatus(401);

        // Un login nuevo sí funciona tras reactivar.
        $login3 = $this->loginConSesion($user)->assertOk();
        $cookie3 = $login3->getCookie(config('session.cookie'), false);
        $this->assertNotNull($cookie3);
        $cookie3 = $cookie3->getValue();

        $this->app['auth']->forgetGuards();
        $this->withoutToken()
            ->withCredentials()
            ->withCookie(config('session.cookie'), $cookie3)
            ->withHeader('Origin', $this->origenStateful())
            ->getJson('/v1/auth/me')
            ->assertOk();
    }
}
