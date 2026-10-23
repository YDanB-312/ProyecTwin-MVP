<?php

namespace Tests\Feature;

use App\Models\GeneralUser;
use Dotenv\Repository\RepositoryBuilder;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Carbon;
use Illuminate\Support\Env;
use Illuminate\Support\Facades\Hash;
use ReflectionProperty;
use Symfony\Component\HttpFoundation\Cookie;
use Tests\TestCase;

// P-05: atributos reales de la cookie del SPA y expiración de los tokens
// Bearer que usa el cliente móvil. El SPA no usa tokens personales.
class CookieSesionTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $sufijo): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Cookie',
            'apellido' => $sufijo,
            'correo' => 'cookie.' . $sufijo . '.' . uniqid() . '@sesion.local',
            'username' => 'cookie_' . $sufijo . '_' . uniqid(),
            'password' => Hash::make('123456'),
            'rol' => 'aprendiz',
            'estado' => true,
        ]);
    }

    private function origenStateful(): string
    {
        $dominio = trim((string) (config('sanctum.stateful')[0] ?? 'localhost'));

        return 'http://' . ltrim($dominio, '/');
    }

    private function loginConSesion(GeneralUser $user)
    {
        return $this->withHeader('Origin', $this->origenStateful())
            ->postJson('/v1/auth/login', [
                'username' => $user->username,
                'password' => '123456',
            ]);
    }

    private function cookieDeSesion($response): Cookie
    {
        $cookie = $response->getCookie(config('session.cookie'), false);

        $this->assertNotNull($cookie, 'La respuesta no incluyó la cookie de sesión.');

        return $cookie;
    }

    /**
     * Evalúa el archivo real config/session.php con variables de entorno
     * aisladas en memoria. No modifica el .env ni exige reiniciar la app.
     */
    private function configSesionConEntorno(array $variables): array
    {
        return $this->configConEntorno('config/session.php', $variables);
    }

    private function configSanctumConEntorno(array $variables): array
    {
        return $this->configConEntorno('config/sanctum.php', $variables);
    }

    /**
     * Evalúa un archivo real de configuración con variables de entorno
     * aisladas en memoria. Restaura el repositorio y los valores globales
     * incluso si falla la preparación o la lectura del archivo.
     */
    private function configConEntorno(string $archivo, array $variables): array
    {
        // El repositorio habitual de Laravel es inmutable. Para probar el
        // archivo real de configuración se usa uno mutable y se restauran el
        // repositorio y las variables globales al terminar.
        $propiedad = new ReflectionProperty(Env::class, 'repository');
        $original = Env::getRepository();
        $envOriginal = [];
        $serverOriginal = [];
        $repositorioInstalado = false;

        try {
            $repositorio = RepositoryBuilder::createWithDefaultAdapters()->make();

            foreach ($variables as $clave => $valor) {
                $envGuardado = [
                    'existe' => array_key_exists($clave, $_ENV),
                    'valor' => $_ENV[$clave] ?? null,
                ];
                $serverGuardado = [
                    'existe' => array_key_exists($clave, $_SERVER),
                    'valor' => $_SERVER[$clave] ?? null,
                ];

                // Solo se registra la variable después de capturar ambos
                // valores; así no se intenta restaurar una captura incompleta.
                $envOriginal[$clave] = $envGuardado;
                $serverOriginal[$clave] = $serverGuardado;

                unset($_ENV[$clave], $_SERVER[$clave]);

                if ($valor !== null) {
                    $repositorio->set($clave, (string) $valor);
                }
            }

            $propiedad->setValue(null, $repositorio);
            $repositorioInstalado = true;

            return require base_path($archivo);
        } finally {
            foreach ($envOriginal as $clave => $guardado) {
                unset($_ENV[$clave]);

                if ($guardado['existe']) {
                    $_ENV[$clave] = $guardado['valor'];
                }
            }

            foreach ($serverOriginal as $clave => $guardado) {
                unset($_SERVER[$clave]);

                if ($guardado['existe']) {
                    $_SERVER[$clave] = $guardado['valor'];
                }
            }

            if ($repositorioInstalado) {
                $propiedad->setValue(null, $original);
            }
        }
    }

    public function test_config_session_usa_el_resolvedor_con_valores_validos_e_invalidos(): void
    {
        $casos = [
            'produccion_sin_url_valor_invalido' => [
                'APP_ENV' => 'production',
                'APP_URL' => null,
                'SESSION_SECURE_COOKIE' => 'foo',
                'esperado' => true,
            ],
            'produccion_url_invalida_valor_invalido' => [
                'APP_ENV' => 'production',
                'APP_URL' => 'http://localhost',
                'SESSION_SECURE_COOKIE' => '2',
                'esperado' => true,
            ],
            'local_http_valor_invalido' => [
                'APP_ENV' => 'local',
                'APP_URL' => 'http://localhost',
                'SESSION_SECURE_COOKIE' => 'foo',
                'esperado' => false,
            ],
            'local_http_false_explicito' => [
                'APP_ENV' => 'local',
                'APP_URL' => 'http://localhost',
                'SESSION_SECURE_COOKIE' => 'false',
                'esperado' => false,
            ],
            'produccion_https_false_explicito' => [
                'APP_ENV' => 'production',
                'APP_URL' => 'https://proyectwin.example',
                'SESSION_SECURE_COOKIE' => 'false',
                'esperado' => true,
            ],
        ];

        foreach ($casos as $nombre => $caso) {
            $config = $this->configSesionConEntorno([
                'APP_ENV' => $caso['APP_ENV'],
                'APP_URL' => $caso['APP_URL'],
                'SESSION_SECURE_COOKIE' => $caso['SESSION_SECURE_COOKIE'],
            ]);

            $this->assertSame($caso['esperado'], $config['secure'], $nombre);
        }
    }

    public function test_cookie_de_sesion_es_secure_en_produccion_sin_url_valida(): void
    {
        foreach ([null, 'http://localhost', 'no-es-una-url'] as $indice => $appUrl) {
            $config = $this->configSesionConEntorno([
                'APP_ENV' => 'production',
                'APP_URL' => $appUrl,
                'SESSION_SECURE_COOKIE' => 'foo',
            ]);

            $this->assertTrue($config['secure']);
            config(['session.secure' => $config['secure']]);

            $response = $this->loginConSesion($this->usuario('produccion_' . $indice));

            $response->assertOk();
            $this->assertNull($response->json('token'));

            $cookie = $this->cookieDeSesion($response);

            $this->assertTrue($cookie->isSecure());
            $this->assertTrue($cookie->isHttpOnly());
            $this->assertSame('lax', $cookie->getSameSite());
        }
    }

    public function test_cookie_de_sesion_es_secure_en_produccion_con_false_explicito(): void
    {
        $config = $this->configSesionConEntorno([
            'APP_ENV' => 'production',
            'APP_URL' => 'https://proyectwin.example',
            'SESSION_SECURE_COOKIE' => 'false',
        ]);

        $this->assertTrue($config['secure']);
        config(['session.secure' => $config['secure']]);

        $response = $this->loginConSesion($this->usuario('produccion_false'));

        $response->assertOk();
        $this->assertNull($response->json('token'));

        $cookie = $this->cookieDeSesion($response);

        $this->assertTrue($cookie->isSecure());
        $this->assertTrue($cookie->isHttpOnly());
        $this->assertSame('lax', $cookie->getSameSite());
    }

    public function test_en_produccion_la_cookie_de_sesion_es_secure_httponly_y_lax(): void
    {
        config(['session.secure' => true]);

        $response = $this->loginConSesion($this->usuario('produccion'));

        $response->assertOk();
        $this->assertNull($response->json('token'));

        $cookie = $this->cookieDeSesion($response);

        $this->assertTrue($cookie->isSecure());
        $this->assertTrue($cookie->isHttpOnly());
        $this->assertSame('lax', $cookie->getSameSite());
    }

    public function test_en_local_http_la_cookie_de_sesion_no_exige_https(): void
    {
        config(['session.secure' => false]);

        $response = $this->loginConSesion($this->usuario('local'));

        $response->assertOk();

        $cookie = $this->cookieDeSesion($response);

        $this->assertFalse($cookie->isSecure());
        $this->assertTrue($cookie->isHttpOnly());
        $this->assertSame('lax', $cookie->getSameSite());
    }

    public function test_la_expiracion_por_defecto_de_sanctum_es_1440(): void
    {
        $config = $this->configSanctumConEntorno([
            'SANCTUM_EXPIRATION' => null,
        ]);

        $this->assertSame(1440, (int) $config['expiration']);
    }

    public function test_un_token_cerca_del_limite_de_1440_sigue_autenticando(): void
    {
        $inicio = Carbon::parse('2026-10-21 12:00:00');
        Carbon::setTestNow($inicio);

        try {
            $user = $this->usuario('cerca_limite');
            $token = $this->postJson('/v1/auth/login', [
                'username' => $user->username,
                'password' => '123456',
            ])->assertOk()->json('token');

            $this->assertNotEmpty($token);

            Carbon::setTestNow($inicio->copy()->addMinutes(1439));
            $this->app['auth']->forgetGuards();
            $this->withToken($token)->getJson('/v1/auth/me')->assertOk();
        } finally {
            Carbon::setTestNow();
        }
    }

    public function test_un_token_mayor_al_limite_de_1440_no_autentica(): void
    {
        $inicio = Carbon::parse('2026-10-21 12:00:00');
        Carbon::setTestNow($inicio);

        try {
            $user = $this->usuario('expirado');
            $token = $this->postJson('/v1/auth/login', [
                'username' => $user->username,
                'password' => '123456',
            ])->assertOk()->json('token');

            $this->assertNotEmpty($token);

            Carbon::setTestNow($inicio->copy()->addMinutes(1441));
            $this->app['auth']->forgetGuards();
            $this->withToken($token)->getJson('/v1/auth/me')->assertStatus(401);
        } finally {
            Carbon::setTestNow();
        }
    }
}
