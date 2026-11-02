<?php

namespace Tests\Core\Auth;

use Illuminate\Support\Facades\DB;
use Tests\TestCase;

// `/up` debe ser liveness puro: sin sesión, sin cookies y sin base de datos.
// `/ready` es la comprobación de dependencias para diagnóstico.
class HealthcheckTest extends TestCase
{
    public function test_up_no_abre_sesion_ni_emite_cookies(): void
    {
        $response = $this->get('/up');

        $response->assertOk()->assertExactJson(['status' => 'ok']);
        $this->assertFalse($response->headers->has('Set-Cookie'));
    }

    public function test_ready_comprueba_la_base_de_datos(): void
    {
        $this->get('/ready')
            ->assertOk()
            ->assertExactJson(['status' => 'ready']);
    }

    public function test_ready_devuelve_503_si_la_base_no_responde(): void
    {
        DB::shouldReceive('select')
            ->once()
            ->with('select 1')
            ->andThrow(new \RuntimeException('base no disponible'));

        $this->get('/ready')
            ->assertStatus(503)
            ->assertExactJson(['status' => 'unavailable']);
    }
}
