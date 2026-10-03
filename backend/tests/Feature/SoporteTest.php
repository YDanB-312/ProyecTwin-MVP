<?php

namespace Tests\Feature;

use App\Models\BugReport;
use App\Models\GeneralUser;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Soporte: la bandeja reutiliza bug_reports. Una solicitud puede indicar el
// número de ficha y el motivo; el admin la cierra con respuesta, deja bitácora
// y avisa al solicitante.
class SoporteTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Soporte',
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

    public function test_la_solicitud_guarda_numero_motivo_y_avisa_al_admin(): void
    {
        $admin = $this->usuario('admin');
        $solicitante = $this->usuario('instructor');

        $respuesta = $this->como($solicitante)->postJson('/v1/bug-reports', [
            'numero_ficha' => '3142101',
            'motivo' => 'El número ya está registrado y creo que es un error.',
            'descripcion' => 'Intenté crear la ficha 3142101 y el sistema dice que ya existe.',
            'tipo' => 'otro',
            'fecha' => now()->toDateString(),
        ])->assertCreated();

        // El título se genera con el número para identificarla en la bandeja.
        $this->assertSame('Conflicto con la ficha 3142101', $respuesta->json('titulo'));
        $this->assertSame('3142101', $respuesta->json('numero_ficha'));

        // El admin recibe el aviso interno.
        $this->assertDatabaseHas('notifications', [
            'id_usuario' => $admin->id,
            'tipo' => 'sistema',
        ]);
    }

    public function test_el_admin_cierra_la_solicitud_con_respuesta_y_avisa_al_solicitante(): void
    {
        $admin = $this->usuario('admin');
        $solicitante = $this->usuario('instructor');

        $reporte = BugReport::create([
            'titulo' => 'Conflicto con la ficha 3142101',
            'numero_ficha' => '3142101',
            'motivo' => 'Número ocupado por error.',
            'descripcion' => 'Necesito crear la ficha 3142101.',
            'tipo' => 'otro',
            'estado' => 'pendiente',
            'fecha' => now()->toDateString(),
            'id_usuario' => $solicitante->id,
        ]);

        $this->como($admin)
            ->putJson('/v1/bug-reports/' . $reporte->id, [
                'titulo' => $reporte->titulo,
                'numero_ficha' => '3142101',
                'motivo' => $reporte->motivo,
                'descripcion' => $reporte->descripcion,
                'tipo' => 'otro',
                'estado' => 'resuelto',
                'respuesta' => 'Se anuló la ficha duplicada; el número quedó libre.',
                'fecha' => now()->toDateString(),
                'id_usuario' => $solicitante->id,
            ])
            ->assertOk()
            ->assertJsonPath('estado', 'resuelto');

        $this->assertDatabaseHas('audit_logs', [
            'accion' => 'resolver_soporte',
            'entidad_id' => $reporte->id,
        ]);
        $this->assertDatabaseHas('notifications', [
            'id_usuario' => $solicitante->id,
            'tipo' => 'sistema',
        ]);
    }

    public function test_el_cierre_no_genera_avisos_repetidos(): void
    {
        $admin = $this->usuario('admin');
        $solicitante = $this->usuario('instructor');

        $reporte = BugReport::create([
            'titulo' => 'Conflicto con la ficha 3142102',
            'numero_ficha' => '3142102',
            'descripcion' => 'Solicitud para probar el cierre único.',
            'tipo' => 'otro',
            'estado' => 'resuelto',
            'respuesta' => 'Ya resuelta.',
            'fecha' => now()->toDateString(),
            'id_usuario' => $solicitante->id,
        ]);

        $avisosAntes = \App\Models\Notification::where('id_usuario', $solicitante->id)->count();

        // Ya estaba cerrada: otra edición no vuelve a avisar ni auditar.
        $this->como($admin)
            ->putJson('/v1/bug-reports/' . $reporte->id, [
                'titulo' => $reporte->titulo,
                'descripcion' => $reporte->descripcion,
                'tipo' => 'otro',
                'estado' => 'resuelto',
                'respuesta' => 'Nota adicional.',
                'fecha' => now()->toDateString(),
                'id_usuario' => $solicitante->id,
            ])
            ->assertOk();

        $this->assertSame(
            $avisosAntes,
            \App\Models\Notification::where('id_usuario', $solicitante->id)->count()
        );
    }
}
