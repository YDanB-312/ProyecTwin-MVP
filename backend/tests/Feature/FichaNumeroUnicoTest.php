<?php

namespace Tests\Feature;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Fichas: el número es el identificador único de ProyecTwin, el código de unión
// lo genera el servidor, y borrar/anular depende de si la ficha tiene historial.
class FichaNumeroUnicoTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Ficha',
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
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);
    }

    private function instructorVerificado(): Instructor
    {
        $user = $this->usuario('instructor');
        return Instructor::create([
            'fecha_ingreso' => now()->toDateString(),
            'id_usuario' => $user->id,
        ]);
    }

    private function ficha(Instructor $instructor, ?string $numero = null): ClassGroup
    {
        return ClassGroup::create([
            'codigo' => 'fn-' . uniqid(),
            'numero' => $numero ?? (string) random_int(100000, 999999),
            'nombre' => 'Ficha número',
            'estado' => 'activo',
            'id_programa' => $this->programa()->id,
            'id_instructor' => $instructor->id,
        ]);
    }

    public function test_el_codigo_lo_genera_el_servidor_y_no_el_cliente(): void
    {
        $admin = $this->usuario('admin');
        $instructor = $this->instructorVerificado();

        $respuesta = $this->como($admin)->postJson('/v1/class-groups', [
            'codigo' => 'mio-mio', // el cliente intenta elegirlo: se ignora
            'numero' => '555001',
            'nombre' => 'Ficha con código propio',
            'estado' => 'activo',
            'id_programa' => $this->programa()->id,
            'id_instructor' => $instructor->id,
        ])->assertCreated();

        $this->assertMatchesRegularExpression('/^[a-z]{3}-[a-z]{4}$/', $respuesta->json('codigo'));
        $this->assertNotSame('mio-mio', $respuesta->json('codigo'));
    }

    public function test_el_numero_de_ficha_es_unico(): void
    {
        $admin = $this->usuario('admin');
        $instructor = $this->instructorVerificado();
        $existente = $this->ficha($instructor, '555002');

        $this->como($admin)->postJson('/v1/class-groups', [
            'numero' => '555002',
            'nombre' => 'Duplicada',
            'estado' => 'activo',
            'id_programa' => $existente->id_programa,
            'id_instructor' => $instructor->id,
        ])->assertStatus(422);
    }

    public function test_borrar_una_ficha_vacia_libera_el_numero(): void
    {
        $admin = $this->usuario('admin');
        $instructor = $this->instructorVerificado();
        $ficha = $this->ficha($instructor, '555003');

        $this->como($admin)
            ->deleteJson('/v1/class-groups/' . $ficha->id)
            ->assertOk()
            ->assertJsonPath('accion', 'eliminada');

        $this->assertDatabaseMissing('class_groups', ['id' => $ficha->id]);

        // El número vuelve a estar disponible.
        $this->como($admin)->postJson('/v1/class-groups', [
            'numero' => '555003',
            'nombre' => 'Reutiliza el número',
            'estado' => 'activo',
            'id_programa' => $ficha->id_programa,
            'id_instructor' => $instructor->id,
        ])->assertCreated();
    }

    public function test_anular_una_ficha_con_datos_libera_el_numero_y_conserva_historial(): void
    {
        $admin = $this->usuario('admin');
        $instructor = $this->instructorVerificado();
        $ficha = $this->ficha($instructor, '555004');

        $aprendizUser = $this->usuario('aprendiz');
        $aprendiz = Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $aprendizUser->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);

        $this->como($admin)
            ->deleteJson('/v1/class-groups/' . $ficha->id)
            ->assertOk()
            ->assertJsonPath('accion', 'anulada');

        $this->assertDatabaseHas('class_groups', [
            'id' => $ficha->id,
            'estado' => 'anulada',
            'numero' => null,
        ]);
        $this->assertDatabaseHas('apprentices', ['id' => $aprendiz->id]);
        $this->assertDatabaseHas('audit_logs', [
            'accion' => 'anular_ficha',
            'entidad_id' => $ficha->id,
        ]);

        // El número queda libre para una ficha nueva.
        $this->como($admin)->postJson('/v1/class-groups', [
            'numero' => '555004',
            'nombre' => 'Nueva dueña del número',
            'estado' => 'activo',
            'id_programa' => $ficha->id_programa,
            'id_instructor' => $instructor->id,
        ])->assertCreated();
    }

    public function test_un_instructor_no_modifica_una_ficha_anulada(): void
    {
        $instructor = $this->instructorVerificado();
        $ficha = $this->ficha($instructor, '555005');

        $aprendizUser = $this->usuario('aprendiz');
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $aprendizUser->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);

        // El propio instructor la anula (tiene historial).
        $this->como($instructor->generalUser)
            ->deleteJson('/v1/class-groups/' . $ficha->id)
            ->assertOk();

        // Pero ya no puede editarla: una ficha anulada solo la toca un admin.
        $this->como($instructor->generalUser)
            ->putJson('/v1/class-groups/' . $ficha->id, [
                'numero' => '555006',
                'nombre' => 'Intento de edición',
                'estado' => 'activo',
                'id_programa' => $ficha->id_programa,
                'id_instructor' => $instructor->id,
            ])
            ->assertStatus(403);
    }

    public function test_el_admin_reactiva_una_ficha_anulada_con_numero_libre(): void
    {
        $admin = $this->usuario('admin');
        $instructor = $this->instructorVerificado();
        $ficha = $this->ficha($instructor, '555007');

        $aprendizUser = $this->usuario('aprendiz');
        Apprentice::create([
            'codigo' => 'AP-' . uniqid(),
            'id_usuario' => $aprendizUser->id,
            'id_class_group' => $ficha->id,
            'id_programa' => $ficha->id_programa,
        ]);

        $this->como($admin)->deleteJson('/v1/class-groups/' . $ficha->id)->assertOk();

        $this->como($admin)
            ->putJson('/v1/class-groups/' . $ficha->id, [
                'numero' => '555008',
                'nombre' => 'Ficha reactivada',
                'estado' => 'activo',
                'id_programa' => $ficha->id_programa,
                'id_instructor' => $instructor->id,
            ])
            ->assertOk();

        $this->assertDatabaseHas('class_groups', [
            'id' => $ficha->id,
            'estado' => 'activo',
            'numero' => '555008',
        ]);
    }
}
