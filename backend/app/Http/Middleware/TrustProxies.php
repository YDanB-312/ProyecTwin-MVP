<?php

namespace App\Http\Middleware;

use Illuminate\Http\Middleware\TrustProxies as Middleware;
use Illuminate\Http\Request;

class TrustProxies extends Middleware
{
    /**
     * The trusted proxies for this application.
     *
     * El valor por defecto conserva el comportamiento histórico de Railway
     * (TLS termina en su proxy). Para endurecerlo, define TRUSTED_PROXIES con
     * los CIDR/IP del proxy; el valor "*" sigue siendo el fallback.
     *
     * @var array<int, string>|string|null
     */
    protected $proxies;

    public function __construct()
    {
        // Se lee del entorno del proceso, igual que la guarda del entrypoint.
        // Con `config:cache` la variable debe llegar del contenedor (Railway),
        // no solo desde el `.env` local.
        $configurados = env('TRUSTED_PROXIES');
        $valor = is_string($configurados) ? trim($configurados) : '';

        if (app()->environment('production') && ($valor === '' || $valor === '*')) {
            throw new \RuntimeException(
                'TRUSTED_PROXIES debe configurarse con los CIDR/IP del proxy en producción; "*" no está permitido.'
            );
        }

        if ($valor === '') {
            $this->proxies = '*';

            return;
        }

        $this->proxies = $valor === '*'
            ? '*'
            : array_values(array_filter(array_map('trim', explode(',', $valor))));
    }

    /**
     * The headers that should be used to detect proxies.
     *
     * @var int
     */
    protected $headers =
        Request::HEADER_X_FORWARDED_FOR |
        Request::HEADER_X_FORWARDED_HOST |
        Request::HEADER_X_FORWARDED_PORT |
        Request::HEADER_X_FORWARDED_PROTO |
        Request::HEADER_X_FORWARDED_AWS_ELB;
}
