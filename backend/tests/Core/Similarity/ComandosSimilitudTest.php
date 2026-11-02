<?php

namespace Tests\Core\Similarity;

use App\Models\Project;
use App\Similarity\Recomputador;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Tests\TestCase;

// C-03: los comandos de similitud delegan en el motor y no fallan con corpus
// vacío. La lógica de puntaje se prueba en RecalcularMotorTest/SimilitudServiceTest.
class ComandosSimilitudTest extends TestCase
{
    use DatabaseTransactions;

    public function test_recalcular_delega_en_el_motor_y_notifica(): void
    {
        $this->mock(Recomputador::class, function ($mock) {
            $mock->shouldReceive('recalcular')
                ->once()
                ->with(true)
                ->andReturn(['eliminadas' => 2, 'creadas' => 3, 'total' => 5]);
        });

        $this->artisan('similitud:recalcular')
            ->expectsOutputToContain('Eliminadas: 2')
            ->assertExitCode(0);
    }

    public function test_recalcular_sin_notificar_pasa_el_flag_al_motor(): void
    {
        $this->mock(Recomputador::class, function ($mock) {
            $mock->shouldReceive('recalcular')
                ->once()
                ->with(false)
                ->andReturn(['eliminadas' => 0, 'creadas' => 0, 'total' => 0]);
        });

        $this->artisan('similitud:recalcular', ['--sin-notificar' => true])
            ->expectsOutputToContain('total: 0')
            ->assertExitCode(0);
    }

    public function test_calibrar_sin_propuestas_termina_sin_error(): void
    {
        Project::query()->update(['estado' => 'rechazado']);

        $this->artisan('similitud:calibrar')
            ->expectsOutputToContain('No hay propuestas para analizar')
            ->assertExitCode(0);
    }
}
