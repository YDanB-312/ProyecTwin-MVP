<?php

namespace Tests\Core\Auth;

use Illuminate\Foundation\Testing\DatabaseTransactions;
use Tests\TestCase;

// La API es consumida por el SPA y por clientes que no siempre envían
// `Accept: application/json`. Una petición sin sesión debe recibir 401 JSON,
// no un redirect a una ruta web `login` inexistente (que terminaba en 500).
class ApiNoAutenticadoTest extends TestCase
{
    use DatabaseTransactions;

    public function test_sin_sesion_responde_401_json_aunque_no_pida_json(): void
    {
        $this->get('/v1/projects')
            ->assertStatus(401)
            ->assertJsonPath('message', 'Unauthenticated.');
    }
}
