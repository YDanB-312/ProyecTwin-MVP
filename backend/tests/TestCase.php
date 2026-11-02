<?php

namespace Tests;

use Illuminate\Foundation\Testing\TestCase as BaseTestCase;
use Illuminate\Support\Facades\Cache;

abstract class TestCase extends BaseTestCase
{
    use CreatesApplication;

    protected function setUp(): void
    {
        parent::setUp();

        // Aísla los contadores de RateLimiter entre pruebas: la suite completa
        // inicia sesión muchas veces desde 127.0.0.1 y no debe agotar el cupo
        // por IP de una prueba anterior.
        Cache::flush();
    }
}
