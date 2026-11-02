<?php

namespace App\Providers;

use Illuminate\Cache\RateLimiting\Limit;
use Illuminate\Foundation\Support\Providers\RouteServiceProvider as ServiceProvider;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\Facades\Route;

class RouteServiceProvider extends ServiceProvider
{
    /**
     * The path to the "home" route for your application.
     *
     * Typically, users are redirected here after authentication.
     *
     * @var string
     */
    public const HOME = '/home';

    /**
     * Define your route model bindings, pattern filters, and other route configuration.
     */
    public function boot(): void
    {
        $this->configureRateLimiting();

        $this->routes(function () {
            Route::middleware('api')
                ->prefix('v1')
                ->group(base_path('routes/api-v1.php'));

            Route::middleware('web')
                ->group(base_path('routes/web.php'));
        });
    }

    /**
     * Configure the rate limiters for the application.
     */
    protected function configureRateLimiting(): void
    {
        RateLimiter::for('api', function (Request $request) {
            return Limit::perMinute((int) config('app.api_rate_limit', 60))
                ->by($request->user()?->id ?: $request->ip());
        });

        // Login: además del límite global, se limita por identificador + IP y
        // por IP. El identificador se normaliza y solo se usa en la clave
        // efímera del limitador; no se persiste en la base de datos.
        RateLimiter::for('login', function (Request $request) {
            $raw = $request->input('correo') ?: $request->input('username');
            $identificador = is_string($raw) ? strtolower(trim($raw)) : '';

            return [
                Limit::perMinute(10)->by('login:'.($identificador !== '' ? $identificador : 'anon').'|'.$request->ip()),
                Limit::perMinute(120)->by('login-ip:'.$request->ip()),
            ];
        });

        RateLimiter::for('forgot-password', function (Request $request) {
            $raw = $request->input('correo');
            $correo = is_string($raw) ? strtolower(trim($raw)) : '';

            return [
                Limit::perMinute(3)->by('forgot:'.($correo !== '' ? $correo : 'anon').'|'.$request->ip()),
                Limit::perMinute(10)->by('forgot-ip:'.$request->ip()),
            ];
        });

        RateLimiter::for('reset-password', function (Request $request) {
            $raw = $request->input('correo');
            $correo = is_string($raw) ? strtolower(trim($raw)) : '';

            return [
                Limit::perMinute(5)->by('reset:'.($correo !== '' ? $correo : 'anon').'|'.$request->ip()),
                Limit::perMinute(20)->by('reset-ip:'.$request->ip()),
            ];
        });

        // El token de activación no se usa como clave para no persistirlo en
        // caché; la protección es por IP y por la entropía del token.
        RateLimiter::for('activation', function (Request $request) {
            return [
                Limit::perMinute(5)->by('activation:'.$request->ip()),
                Limit::perHour(30)->by('activation-hour:'.$request->ip()),
            ];
        });

        RateLimiter::for('activation-resend', function (Request $request) {
            return Limit::perMinute(5)
                ->by('activation-resend:'.($request->user()?->id ?: $request->ip()));
        });

        // La demo pública ejecuta el motor completo: se limita por IP para
        // evitar ráfagas anónimas que consuman CPU.
        RateLimiter::for('demo-similitud', function (Request $request) {
            return [
                Limit::perMinute(10)->by('demo-similitud:'.$request->ip()),
                Limit::perHour(60)->by('demo-similitud-hour:'.$request->ip()),
            ];
        });

        // La detección autenticada se limita por usuario; es una operación
        // costosa y no debe convertirse en un amplificador de CPU.
        RateLimiter::for('similarity-detect', function (Request $request) {
            $actor = $request->user()?->id ?: $request->ip();

            return [
                Limit::perMinute(5)->by('similarity-detect:'.$actor),
                Limit::perHour(20)->by('similarity-detect-hour:'.$actor),
            ];
        });
    }
}
