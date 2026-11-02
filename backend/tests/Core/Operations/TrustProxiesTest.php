<?php

namespace Tests\Core\Operations;

use App\Http\Middleware\TrustProxies;
use ReflectionProperty;
use Tests\TestCase;

// C-07: el fallback histórico se conserva, pero TRUSTED_PROXIES permite
// endurecer la lista sin cambiar código ni debilitar el comportamiento actual.
class TrustProxiesTest extends TestCase
{
    private ?string $envOriginal = null;

    protected function setUp(): void
    {
        parent::setUp();

        $this->envOriginal = getenv('TRUSTED_PROXIES') === false
            ? null
            : (string) getenv('TRUSTED_PROXIES');
    }

    protected function tearDown(): void
    {
        if ($this->envOriginal === null) {
            putenv('TRUSTED_PROXIES');
            unset($_ENV['TRUSTED_PROXIES'], $_SERVER['TRUSTED_PROXIES']);
        } else {
            putenv('TRUSTED_PROXIES='.$this->envOriginal);
            $_ENV['TRUSTED_PROXIES'] = $this->envOriginal;
            $_SERVER['TRUSTED_PROXIES'] = $this->envOriginal;
        }

        parent::tearDown();
    }

    public function test_sin_configuracion_conserva_el_fallback_historico(): void
    {
        $this->setTrustedProxiesEnv(null);

        $this->assertSame('*', $this->proxies(new TrustProxies()));
    }

    public function test_acepta_lista_de_cidr_y_direcciones(): void
    {
        $this->setTrustedProxiesEnv('10.0.0.0/8, 192.168.1.10');

        $this->assertSame(
            ['10.0.0.0/8', '192.168.1.10'],
            $this->proxies(new TrustProxies())
        );
    }

    public function test_produccion_sin_lista_explicita_falla(): void
    {
        $this->app['env'] = 'production';
        $this->setTrustedProxiesEnv(null);

        $this->expectException(\RuntimeException::class);

        new TrustProxies();
    }

    private function proxies(TrustProxies $middleware): mixed
    {
        $propiedad = new ReflectionProperty(TrustProxies::class, 'proxies');
        $propiedad->setAccessible(true);

        return $propiedad->getValue($middleware);
    }

    private function setTrustedProxiesEnv(?string $valor): void
    {
        if ($valor === null) {
            putenv('TRUSTED_PROXIES');
            unset($_ENV['TRUSTED_PROXIES'], $_SERVER['TRUSTED_PROXIES']);

            return;
        }

        putenv('TRUSTED_PROXIES='.$valor);
        $_ENV['TRUSTED_PROXIES'] = $valor;
        $_SERVER['TRUSTED_PROXIES'] = $valor;
    }
}
