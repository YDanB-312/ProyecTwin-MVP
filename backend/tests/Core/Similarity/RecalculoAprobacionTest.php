<?php

namespace Tests\Core\Similarity;

use App\Jobs\RecalcularSimilitudesPendientes;
use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Project;
use App\Models\Similarity;
use App\Models\TrainingProgram;
use App\Similarity\EstadoSimilitud;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Queue;
use Tests\TestCase;

/**
 * Recalcula pendientes al aprobar, respeta programa+red y conserva el
 * histórico de las aprobadas.
 */
class RecalculoAprobacionTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Similitud',
            'correo' => $rol . '.' . uniqid() . '@correo.com',
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

    private function red(?string $nombre = null): KnowledgeNetwork
    {
        return KnowledgeNetwork::create(['nombre' => $nombre ?: 'Red ' . uniqid()]);
    }

    private function programa(KnowledgeNetwork $red): TrainingProgram
    {
        return TrainingProgram::create([
            'nombre' => 'Programa ' . uniqid(),
            'nivel' => 'Tecnologo',
            'knowledge_network_id' => $red->id,
        ]);
    }

    private function ficha(TrainingProgram $programa, GeneralUser $instructorUser): ClassGroup
    {
        $instructor = Instructor::firstOrCreate(
            ['id_usuario' => $instructorUser->id],
            ['fecha_ingreso' => now()->toDateString()],
        );

        return ClassGroup::create([
            'codigo' => 'sim-' . uniqid(),
            'numero' => (string) random_int(100000, 999999),
            'nombre' => 'Ficha similitud',
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

    private function crearYEnviar(GeneralUser $aprendiz, ClassGroup $ficha, string $titulo, string $sufijo = ''): Project
    {
        $creada = $this->como($aprendiz)->postJson('/v1/projects', [
            'titulo' => $titulo,
            'resumen' => 'Herramienta web para controlar inventarios del almacén, con alertas de stock, reportes y trazabilidad por lote.',
            'palabras_clave' => 'inventario, almacén, reportes, stock',
            'area_aplicacion' => 'Desarrollo Web',
            'objetivo_general' => 'Controlar los inventarios del almacén con alertas y reportes confiables.',
            'objetivos_especificos' => [
                'Registrar entradas y salidas de productos.',
                'Generar reportes de trazabilidad por lote.',
            ],
            'id_creador' => $aprendiz->id,
            'id_class_group' => $ficha->id,
        ])->assertCreated();

        $id = (int) $creada->json('id');
        $this->como($aprendiz)->postJson("/v1/projects/$id/enviar")->assertOk();

        return Project::findOrFail($id);
    }

    private function aprobar(GeneralUser $instructor, Project $project): void
    {
        $this->como($instructor)->putJson("/v1/projects/{$project->id}", [
            'titulo' => $project->titulo,
            'resumen' => $project->resumen,
            'palabras_clave' => $project->palabras_clave,
            'area_aplicacion' => $project->area_aplicacion,
            'objetivo_general' => $project->objetivo_general,
            'objetivos_especificos' => $project->objetivos_especificos,
            'estado' => 'aprobado',
            'version' => $project->updated_at?->toJSON(),
        ])->assertOk();
    }

    public function test_sin_referencias_no_genera_pares_y_usa_estado_explicito(): void
    {
        $instructor = $this->usuario('instructor');
        $ficha = $this->ficha($this->programa($this->red()), $instructor);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);

        $pa = $this->crearYEnviar($a, $ficha, 'Sistema de inventarios');
        $pb = $this->crearYEnviar($b, $ficha, 'Sistema de inventarios');

        $this->assertSame(EstadoSimilitud::SIN_REFERENCIAS, $pa->fresh()->similitud_estado);
        $this->assertSame(EstadoSimilitud::SIN_REFERENCIAS, $pb->fresh()->similitud_estado);
        $this->assertSame(0, Similarity::where('vigente', true)->count());
    }

    public function test_primera_aprobacion_recalcula_todas_las_pendientes_del_ambito(): void
    {
        $instructor = $this->usuario('instructor');
        $programa = $this->programa($this->red());
        $ficha = $this->ficha($programa, $instructor);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);
        $c = $this->aprendizEn($ficha);

        $pa = $this->crearYEnviar($a, $ficha, 'Sistema de inventarios para tienda');
        $pb = $this->crearYEnviar($b, $ficha, 'Sistema de inventarios para tienda');
        $pc = $this->crearYEnviar($c, $ficha, 'Sistema de inventarios para tienda');

        $this->aprobar($instructor, $pa);

        $this->assertSame(EstadoSimilitud::ACTUALIZADO, $pb->fresh()->similitud_estado);
        $this->assertSame(EstadoSimilitud::ACTUALIZADO, $pc->fresh()->similitud_estado);
        $this->assertTrue(Similarity::where('vigente', true)
            ->where(function ($q) use ($pa, $pb) {
                $q->where('id_proyecto_1', min($pa->id, $pb->id))
                    ->where('id_proyecto_2', max($pa->id, $pb->id));
            })->exists());
        $this->assertTrue(Similarity::where('vigente', true)
            ->where(function ($q) use ($pa, $pc) {
                $q->where('id_proyecto_1', min($pa->id, $pc->id))
                    ->where('id_proyecto_2', max($pa->id, $pc->id));
            })->exists());
    }

    public function test_segunda_aprobacion_recalcula_solo_pendientes_y_conserva_aprobadas(): void
    {
        $instructor = $this->usuario('instructor');
        $ficha = $this->ficha($this->programa($this->red()), $instructor);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);
        $c = $this->aprendizEn($ficha);

        $pa = $this->crearYEnviar($a, $ficha, 'Sistema de inventarios');
        $pb = $this->crearYEnviar($b, $ficha, 'Sistema de inventarios');
        $pc = $this->crearYEnviar($c, $ficha, 'Sistema de inventarios');

        $this->aprobar($instructor, $pa);
        $this->aprobar($instructor, $pb->fresh());

        $this->assertSame(EstadoSimilitud::ACTUALIZADO, $pc->fresh()->similitud_estado);
        $this->assertSame(2, Similarity::where('vigente', true)
            ->where(function ($q) use ($pc) {
                $q->where('id_proyecto_1', $pc->id)->orWhere('id_proyecto_2', $pc->id);
            })->count());
        $this->assertSame(1, Similarity::where('vigente', true)
            ->where(function ($q) use ($pa, $pb) {
                $q->where('id_proyecto_1', min($pa->id, $pb->id))
                    ->where('id_proyecto_2', max($pa->id, $pb->id));
            })->count());
    }

    public function test_no_compara_programas_o_redes_distintas(): void
    {
        $instructor = $this->usuario('instructor');
        $redA = $this->red();
        $redB = $this->red();
        $fichaA = $this->ficha($this->programa($redA), $instructor);
        $fichaB = $this->ficha($this->programa($redB), $instructor);
        $a = $this->aprendizEn($fichaA);
        $b = $this->aprendizEn($fichaB);

        $pa = $this->crearYEnviar($a, $fichaA, 'Sistema de inventarios');
        $pb = $this->crearYEnviar($b, $fichaB, 'Sistema de inventarios');

        $this->aprobar($instructor, $pa);

        $this->assertSame(EstadoSimilitud::SIN_REFERENCIAS, $pb->fresh()->similitud_estado);
        $this->assertFalse(Similarity::where('vigente', true)
            ->where(function ($q) use ($pa, $pb) {
                $q->where('id_proyecto_1', min($pa->id, $pb->id))
                    ->where('id_proyecto_2', max($pa->id, $pb->id));
            })->exists());
    }

    public function test_rechazadas_no_se_recalculan_ni_son_referencia(): void
    {
        $instructor = $this->usuario('instructor');
        $ficha = $this->ficha($this->programa($this->red()), $instructor);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);
        $c = $this->aprendizEn($ficha);

        $pa = $this->crearYEnviar($a, $ficha, 'Sistema de inventarios');
        $pb = $this->crearYEnviar($b, $ficha, 'Sistema de inventarios');
        $pc = $this->crearYEnviar($c, $ficha, 'Sistema de inventarios');

        $this->como($instructor)->putJson("/v1/projects/{$pb->id}", [
            'titulo' => $pb->titulo,
            'resumen' => $pb->resumen,
            'area_aplicacion' => $pb->area_aplicacion,
            'objetivo_general' => $pb->objetivo_general,
            'objetivos_especificos' => $pb->objetivos_especificos,
            'estado' => 'rechazado',
            'version' => $pb->updated_at?->toJSON(),
        ])->assertOk();

        $this->aprobar($instructor, $pa);

        $this->assertSame('rechazado', $pb->fresh()->estado);
        $this->assertFalse(Similarity::where('vigente', true)
            ->where(function ($q) use ($pb) {
                $q->where('id_proyecto_1', $pb->id)->orWhere('id_proyecto_2', $pb->id);
            })->exists());
        $this->assertTrue(Similarity::where('vigente', true)
            ->where(function ($q) use ($pa, $pc) {
                $q->where('id_proyecto_1', min($pa->id, $pc->id))
                    ->where('id_proyecto_2', max($pa->id, $pc->id));
            })->exists());
    }

    public function test_aprobacion_desactualizada_exige_recalcular_y_no_usa_pares_viejos(): void
    {
        Queue::fake();

        $instructor = $this->usuario('instructor');
        $ficha = $this->ficha($this->programa($this->red()), $instructor);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);

        $pa = $this->crearYEnviar($a, $ficha, 'Sistema de inventarios');
        $pb = $this->crearYEnviar($b, $ficha, 'Sistema de inventarios');

        $this->aprobar($instructor, $pa);

        $this->como($instructor)->putJson("/v1/projects/{$pb->id}", [
            'titulo' => $pb->titulo,
            'resumen' => $pb->resumen,
            'palabras_clave' => $pb->palabras_clave,
            'area_aplicacion' => $pb->area_aplicacion,
            'objetivo_general' => $pb->objetivo_general,
            'objetivos_especificos' => $pb->objetivos_especificos,
            'estado' => 'aprobado',
            'version' => $pb->fresh()->updated_at?->toJSON(),
        ])->assertStatus(409)->assertJsonPath('code', 'similitud_desactualizada');

        $this->assertSame('pendiente', $pb->fresh()->estado);
        $this->assertSame(EstadoSimilitud::ACTUALIZADO, $pb->fresh()->similitud_estado);
        Queue::assertPushed(RecalcularSimilitudesPendientes::class);
    }

    public function test_actualiza_pendientes_de_otra_ficha_del_mismo_programa_y_red(): void
    {
        $instructor = $this->usuario('instructor');
        $programa = $this->programa($this->red());
        $fichaA = $this->ficha($programa, $instructor);
        $fichaB = $this->ficha($programa, $instructor);
        $a = $this->aprendizEn($fichaA);
        $b = $this->aprendizEn($fichaB);
        $c = $this->aprendizEn($fichaB);

        $pa = $this->crearYEnviar($a, $fichaA, 'Sistema de inventarios');
        $pb = $this->crearYEnviar($b, $fichaB, 'Sistema de inventarios');
        $pc = $this->crearYEnviar($c, $fichaB, 'Sistema de inventarios');

        $this->aprobar($instructor, $pa);

        $this->assertTrue(Similarity::where('vigente', true)
            ->where(function ($q) use ($pa, $pb) {
                $q->where('id_proyecto_1', min($pa->id, $pb->id))
                    ->where('id_proyecto_2', max($pa->id, $pb->id));
            })->exists());
        $this->assertTrue(Similarity::where('vigente', true)
            ->where(function ($q) use ($pa, $pc) {
                $q->where('id_proyecto_1', min($pa->id, $pc->id))
                    ->where('id_proyecto_2', max($pa->id, $pc->id));
            })->exists());
    }

    public function test_reintentar_el_job_no_duplica_pares_y_conserva_aprobadas(): void
    {
        $instructor = $this->usuario('instructor');
        $programa = $this->programa($this->red());
        $ficha = $this->ficha($programa, $instructor);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);

        $pa = $this->crearYEnviar($a, $ficha, 'Sistema de inventarios');
        $pb = $this->crearYEnviar($b, $ficha, 'Sistema de inventarios');
        $this->aprobar($instructor, $pa);

        $antes = Similarity::where('vigente', true)->count();
        $porcentajeAprobada = Similarity::where('vigente', true)
            ->where(function ($q) use ($pa, $pb) {
                $q->where('id_proyecto_1', min($pa->id, $pb->id))
                    ->where('id_proyecto_2', max($pa->id, $pb->id));
            })->value('porcentaje');

        $job = new RecalcularSimilitudesPendientes(
            (int) $programa->id,
            (int) $programa->knowledge_network_id,
            app(\App\Similarity\Recomputador::class)->hashScope((int) $programa->id, (int) $programa->knowledge_network_id),
        );
        $job->handle(app(\App\Similarity\Recomputador::class));

        $this->assertSame($antes, Similarity::where('vigente', true)->count());
        $this->assertSame(
            $porcentajeAprobada,
            Similarity::where('vigente', true)
                ->where(function ($q) use ($pa, $pb) {
                    $q->where('id_proyecto_1', min($pa->id, $pb->id))
                        ->where('id_proyecto_2', max($pa->id, $pb->id));
                })->value('porcentaje'),
        );
    }

    public function test_job_fallido_deja_estado_error_y_bloquea_aprobacion_hasta_recalcular(): void
    {
        Queue::fake();

        $instructor = $this->usuario('instructor');
        $programa = $this->programa($this->red());
        $ficha = $this->ficha($programa, $instructor);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);

        $pa = $this->crearYEnviar($a, $ficha, 'Sistema de inventarios');
        $pb = $this->crearYEnviar($b, $ficha, 'Sistema de inventarios');
        $this->aprobar($instructor, $pa);

        $job = new RecalcularSimilitudesPendientes(
            (int) $programa->id,
            (int) $programa->knowledge_network_id,
            app(\App\Similarity\Recomputador::class)->hashScope((int) $programa->id, (int) $programa->knowledge_network_id),
        );
        $job->failed(new \RuntimeException('fallo simulado'));

        $this->assertSame(EstadoSimilitud::ERROR, $pb->fresh()->similitud_estado);

        $this->como($instructor)->putJson("/v1/projects/{$pb->id}", [
            'titulo' => $pb->titulo,
            'resumen' => $pb->resumen,
            'palabras_clave' => $pb->palabras_clave,
            'area_aplicacion' => $pb->area_aplicacion,
            'objetivo_general' => $pb->objetivo_general,
            'objetivos_especificos' => $pb->objetivos_especificos,
            'estado' => 'aprobado',
            'version' => $pb->fresh()->updated_at?->toJSON(),
        ])->assertStatus(409)->assertJsonPath('code', 'similitud_desactualizada');
    }

    public function test_relacionadas_incluye_pendiente_aprobado_para_instructor(): void
    {
        $instructor = $this->usuario('instructor');
        $ficha = $this->ficha($this->programa($this->red()), $instructor);
        $a = $this->aprendizEn($ficha);
        $b = $this->aprendizEn($ficha);

        $pa = $this->crearYEnviar($a, $ficha, 'Sistema de inventarios');
        $pb = $this->crearYEnviar($b, $ficha, 'Sistema de inventarios');
        $this->aprobar($instructor, $pa);

        $respuesta = $this->como($instructor)->getJson(
            "/v1/similarities?related_to={$pa->id},{$pb->id}"
        )->assertOk()->json();

        $this->assertTrue(collect($respuesta)->contains(function ($fila) use ($pa, $pb) {
            $ids = [(int) $fila['id_proyecto_1'], (int) $fila['id_proyecto_2']];
            return in_array($pa->id, $ids, true) && in_array($pb->id, $ids, true);
        }));
    }
}
