<?php

namespace Tests\Unit;

use App\Support\SeguridadSesion;
use PHPUnit\Framework\TestCase;

// P-05: la cookie de sesión debe ser Secure en despliegues HTTPS, fallar
// cerrado en producción y seguir funcionando sin Secure en local sobre HTTP.
class SeguridadSesionTest extends TestCase
{
    public function test_https_activa_secure_por_defecto(): void
    {
        $this->assertTrue(SeguridadSesion::cookieSegura('https://proyectwin.example'));
    }

    public function test_http_local_no_fuerza_secure_por_defecto(): void
    {
        $this->assertFalse(SeguridadSesion::cookieSegura('http://localhost'));
    }

    public function test_https_con_valor_explicito_invalido_activa_secure(): void
    {
        $this->assertTrue(SeguridadSesion::cookieSegura('https://proyectwin.example', 'foo'));
        $this->assertTrue(SeguridadSesion::cookieSegura('https://proyectwin.example', '2'));
    }

    public function test_http_local_con_valor_explicito_invalido_no_fuerza_secure(): void
    {
        $this->assertFalse(SeguridadSesion::cookieSegura('http://localhost', 'foo'));
    }

    public function test_app_url_ausente_fuera_de_produccion_no_activa_secure(): void
    {
        $this->assertFalse(SeguridadSesion::cookieSegura(null, null, 'local'));
        $this->assertFalse(SeguridadSesion::cookieSegura(null, null, 'testing'));
    }

    public function test_produccion_sin_app_url_activa_secure(): void
    {
        $this->assertTrue(SeguridadSesion::cookieSegura(null, null, 'production'));
        $this->assertTrue(SeguridadSesion::cookieSegura('', null, 'production'));
    }

    public function test_produccion_con_app_url_invalida_activa_secure(): void
    {
        $this->assertTrue(SeguridadSesion::cookieSegura('http://localhost', null, 'production'));
        $this->assertTrue(SeguridadSesion::cookieSegura('no-es-una-url', null, 'production'));
    }

    public function test_produccion_con_valor_explicito_invalido_activa_secure(): void
    {
        $this->assertTrue(SeguridadSesion::cookieSegura('http://localhost', 'foo', 'production'));
        $this->assertTrue(SeguridadSesion::cookieSegura('http://localhost', '2', 'production'));
    }

    public function test_el_valor_explicito_verdadero_activa_secure(): void
    {
        $this->assertTrue(SeguridadSesion::cookieSegura('http://localhost', true));
        $this->assertTrue(SeguridadSesion::cookieSegura('http://localhost', 'true'));
    }

    public function test_el_valor_explicito_falso_desactiva_secure_fuera_de_produccion(): void
    {
        $this->assertFalse(SeguridadSesion::cookieSegura('https://proyectwin.example', false));
        $this->assertFalse(SeguridadSesion::cookieSegura('http://localhost', 'false'));
    }

    public function test_produccion_ignora_el_false_explicito(): void
    {
        $this->assertTrue(SeguridadSesion::cookieSegura('https://proyectwin.example', false, 'production'));
        $this->assertTrue(SeguridadSesion::cookieSegura('https://proyectwin.example', 'false', 'production'));
    }

    public function test_un_valor_vacio_recurre_a_app_url(): void
    {
        $this->assertTrue(SeguridadSesion::cookieSegura('https://proyectwin.example', ''));
        $this->assertFalse(SeguridadSesion::cookieSegura('http://localhost', ''));
    }

    public function test_un_valor_vacio_en_produccion_sin_url_activa_secure(): void
    {
        $this->assertTrue(SeguridadSesion::cookieSegura(null, '', 'production'));
    }
}
