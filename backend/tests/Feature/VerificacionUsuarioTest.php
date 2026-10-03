<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\AuditLog;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\TrainingProgram;
use App\Notifications\VerificacionResuelta;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Illuminate\Support\Facades\Storage;
use Tests\TestCase;

// Verificación de cuentas: el autoregistro (aprendiz o instructor) exige un PDF
// que soporta el rol y NO tiene acceso hasta que el admin apruebe. El admin
// resuelve (aprobar/rechazar con motivo) con correo, campanita y bitácora.
class VerificacionUsuarioTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol, array $extra = []): GeneralUser
    {
        return GeneralUser::create(array_merge([
            'nombre' => ucfirst($rol),
            'apellido' => 'Verif',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ], $extra));
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

    // Registro público multipart con el PDF obligatorio.
    private function registrarPublico(string $rol, ?UploadedFile $archivo = null)
    {
        return $this->post('/v1/general-users', [
            'nombre' => 'Auto',
            'apellido' => 'Registro',
            'correo' => 'auto.' . uniqid() . '@correo.com',
            'password' => '123456',
            'rol' => $rol,
            'soporte' => $archivo ?: UploadedFile::fake()->create('soporte.pdf', 200, 'application/pdf'),
        ], ['Accept' => 'application/json']);
    }

    // ---------------------------------------------------------------- Registro

    public function test_el_autoregistro_de_aprendiz_nace_pendiente_con_documento_y_sin_acceso(): void
    {
        Storage::fake('local');
        $admin = $this->usuario('admin');

        $respuesta = $this->registrarPublico('aprendiz')->assertCreated();

        $user = GeneralUser::findOrFail($respuesta->json('id'));
        $this->assertSame('pendiente', $user->estado_verificacion);
        $this->assertNotNull($user->soporte_path);
        Storage::disk('local')->assertExists($user->soporte_path);

        // El admin recibe el aviso interno para revisar la solicitud.
        $this->assertDatabaseHas('notifications', ['id_usuario' => $admin->id]);

        // Sin aprobación no hay acceso.
        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => '123456',
        ])->assertStatus(403)->assertJsonPath('message', 'Tu cuenta está pendiente de verificación. Un administrador debe aprobarla.');
    }

    public function test_el_autoregistro_de_instructor_tambien_nace_sin_acceso(): void
    {
        Storage::fake('local');

        $respuesta = $this->registrarPublico('instructor')->assertCreated();
        $user = GeneralUser::findOrFail($respuesta->json('id'));

        $this->assertSame('pendiente', $user->estado_verificacion);
        $this->assertDatabaseHas('instructors', ['id_usuario' => $user->id]);

        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => '123456',
        ])->assertStatus(403);
    }

    public function test_el_registro_con_archivo_que_no_es_pdf_falla(): void
    {
        Storage::fake('local');

        $this->registrarPublico('aprendiz', UploadedFile::fake()->create('carnet.txt', 10, 'text/plain'))
            ->assertStatus(422)
            ->assertJsonValidationErrors('soporte');
    }

    public function test_el_admin_crea_cuentas_verificadas_y_sin_documento(): void
    {
        $admin = $this->usuario('admin');

        $respuesta = $this->como($admin)->postJson('/v1/general-users', [
            'nombre' => 'Directo',
            'apellido' => 'Admin',
            'correo' => 'directo.' . uniqid() . '@test.local',
            'password' => '123456',
            'rol' => 'aprendiz',
        ])->assertCreated();

        $this->assertDatabaseHas('general_users', [
            'id' => $respuesta->json('id'),
            'estado_verificacion' => 'verificado',
            'soporte_path' => null,
        ]);

        // La cuenta creada por el admin puede ingresar de inmediato.
        $user = GeneralUser::findOrFail($respuesta->json('id'));
        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => '123456',
        ])->assertOk();
    }

    // ---------------------------------------------------------------- Resolución

    public function test_el_admin_aprueba_y_el_aprendiz_puede_ingresar(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $user = $this->usuario('aprendiz', ['estado_verificacion' => 'pendiente']);

        $this->como($admin)
            ->putJson('/v1/general-users/' . $user->id . '/verificacion', ['accion' => 'verificar'])
            ->assertOk()
            ->assertJsonPath('estado_verificacion', 'verificado');

        $this->assertDatabaseHas('audit_logs', [
            'accion' => 'verificar_usuario',
            'entidad_id' => $user->id,
        ]);
        Notification::assertSentTo($user, VerificacionResuelta::class);
        $this->assertDatabaseHas('notifications', ['id_usuario' => $user->id]);

        // Ya puede ingresar.
        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => '123456',
        ])->assertOk();
    }

    public function test_el_admin_aprueba_al_instructor_y_puede_crear_fichas(): void
    {
        $admin = $this->usuario('admin');
        $user = $this->usuario('instructor', ['estado_verificacion' => 'pendiente']);
        $instructor = Instructor::create(['fecha_ingreso' => now()->toDateString(), 'id_usuario' => $user->id]);

        $this->como($admin)
            ->putJson('/v1/general-users/' . $user->id . '/verificacion', ['accion' => 'verificar'])
            ->assertOk();

        $this->como($user->fresh())->postJson('/v1/class-groups', [
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha habilitada',
            'estado' => 'activo',
            'id_programa' => $this->programa()->id,
            'id_instructor' => $instructor->id,
        ])->assertCreated();
    }

    public function test_el_rechazo_exige_motivo_y_bloquea_el_acceso(): void
    {
        Notification::fake();
        $admin = $this->usuario('admin');
        $user = $this->usuario('aprendiz', ['estado_verificacion' => 'pendiente']);

        // Sin motivo → 422.
        $this->como($admin)
            ->putJson('/v1/general-users/' . $user->id . '/verificacion', ['accion' => 'rechazar'])
            ->assertStatus(422);

        $this->como($admin)
            ->putJson('/v1/general-users/' . $user->id . '/verificacion', [
                'accion' => 'rechazar',
                'motivo' => 'El documento no permite confirmar el rol.',
            ])
            ->assertOk()
            ->assertJsonPath('estado_verificacion', 'rechazado');

        $this->assertSame('El documento no permite confirmar el rol.', $user->fresh()->motivo_rechazo);
        Notification::assertSentTo($user, VerificacionResuelta::class);

        // El login muestra el motivo y sigue bloqueado.
        $this->postJson('/v1/auth/login', [
            'correo' => $user->correo,
            'password' => '123456',
        ])->assertStatus(403)->assertJsonPath(
            'message',
            'Tu solicitud fue rechazada. Motivo: El documento no permite confirmar el rol.'
        );
    }

    public function test_solo_un_admin_verifica(): void
    {
        $aprendiz = $this->usuario('aprendiz');
        $otro = $this->usuario('aprendiz', ['estado_verificacion' => 'pendiente']);

        $this->como($aprendiz)
            ->putJson('/v1/general-users/' . $otro->id . '/verificacion', ['accion' => 'verificar'])
            ->assertStatus(403);
    }

    public function test_el_documento_de_soporte_solo_lo_ve_un_admin(): void
    {
        Storage::fake('local');
        $admin = $this->usuario('admin');
        // Cuenta verificada (para pasar el middleware) con documento adjunto.
        $user = $this->usuario('aprendiz');
        $user->update(['soporte_path' => UploadedFile::fake()->create('soporte.pdf', 100, 'application/pdf')
            ->store('verificaciones')]);

        // El propio usuario (con token emitido) no puede verlo.
        $token = $user->createToken('test')->plainTextToken;
        $this->withToken($token)
            ->get('/v1/general-users/' . $user->id . '/soporte', ['Accept' => 'application/json'])
            ->assertStatus(403);

        // El admin sí.
        $this->como($admin)
            ->get('/v1/general-users/' . $user->id . '/soporte')
            ->assertOk();
    }

    public function test_un_aprendiz_pendiente_no_se_une_a_ficha(): void
    {
        $programa = $this->programa();
        $instructorUser = $this->usuario('instructor');
        $instructor = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $instructorUser->id,
        ]);
        $ficha = ClassGroup::create([
            'codigo' => 'vf-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha verificación',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);

        $pendiente = $this->usuario('aprendiz', ['estado_verificacion' => 'pendiente']);
        $token = $pendiente->createToken('test')->plainTextToken;

        // El middleware corta el acceso (401) antes de llegar al controlador:
        // sin verificación no hay acceso a nada.
        $this->withToken($token)
            ->postJson('/v1/apprentices/me/ficha', ['codigo' => $ficha->codigo])
            ->assertStatus(401);
    }

    // ---------------------------------------------------------------- Trazabilidad

    public function test_la_reasignacion_de_ficha_queda_en_bitacora(): void
    {
        $admin = $this->usuario('admin');
        $programa = $this->programa();

        $instA = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $this->usuario('instructor')->id,
        ]);
        $instB = Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $this->usuario('instructor')->id,
        ]);

        $ficha = ClassGroup::create([
            'codigo' => 'tr-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha trazabilidad',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instA->id,
        ]);

        $this->como($admin)
            ->putJson('/v1/class-groups/' . $ficha->id, [
                'numero' => $ficha->numero,
                'nombre' => $ficha->nombre,
                'estado' => 'activo',
                'id_programa' => $programa->id,
                'id_instructor' => $instB->id,
            ])
            ->assertOk();

        $log = AuditLog::where('accion', 'reasignar_ficha')->where('entidad_id', $ficha->id)->first();
        $this->assertNotNull($log);
        $this->assertSame($instA->id, (int) $log->detalle['de']);
        $this->assertSame($instB->id, (int) $log->detalle['a']);
    }

    public function test_la_correccion_de_correo_por_admin_queda_en_bitacora(): void
    {
        $admin = $this->usuario('admin');
        $user = $this->usuario('aprendiz');
        $nuevo = 'corregido.' . uniqid() . '@test.local';

        $this->como($admin)->putJson('/v1/general-users/' . $user->id, [
            'nombre' => $user->nombre,
            'apellido' => $user->apellido,
            'correo' => $nuevo,
            'rol' => 'aprendiz',
            'estado' => true,
        ])->assertOk();

        $log = AuditLog::where('accion', 'actualizar_usuario')
            ->where('entidad_id', $user->id)
            ->latest('id')
            ->first();
        $this->assertNotNull($log);
        $this->assertSame($nuevo, $log->detalle['correo']);
    }
}
