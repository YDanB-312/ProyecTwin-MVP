<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;

/**
 * Reseed de la base de datos E2E (sprint final, Paso 1).
 *
 * SOLO opera sobre `proyectwin_e2e` y exige:
 *   - E2E_ALLOW_RESET=1 (autorización explícita),
 *   - APP_ENV=e2e (invocar como `php artisan e2e:reset --env=e2e`),
 *   - backend/.env.e2e presente,
 *   - base de datos resuelta = proyectwin_e2e en loopback,
 *   - ausencia de configuración cacheada incompatible,
 *   - conexión efectiva verificada (base y puerto reales),
 *   - lock exclusivo (evita reseeds concurrentes de workers paralelos).
 *
 * Si algo de esto falla, la ejecución ABORTA sin tocar ninguna base de datos.
 * La opción --check-only valida entorno y conexión sin migrar ni sembrar.
 */
class E2eReset extends Command
{
    protected $signature = 'e2e:reset
        {--con-volumen : siembra también VolumenSeeder}
        {--check-only : valida entorno y conexión efectiva sin migrar ni sembrar}';

    protected $description = 'Reinicia SOLO la base E2E (proyectwin_e2e) con guardas estrictas de seguridad.';

    private const BD_PERMITIDA = '/^proyectwin_e2e$/';

    public function handle(): int
    {
        $problemas = $this->problemasDeSeguridad();

        if ($problemas !== []) {
            $this->mostrarProblemas($problemas);

            return self::FAILURE;
        }

        // Verificación sin reset: comparte las guardas de entorno y comprueba
        // la conexión efectiva, pero no adquiere el lock ni ejecuta migraciones.
        if ($this->option('check-only')) {
            $problemasConexion = $this->problemasDeConexion();

            if ($problemasConexion !== []) {
                $this->mostrarProblemas($problemasConexion);

                return self::FAILURE;
            }

            $this->info(
                'Verificación E2E OK: APP_ENV='.$this->laravel->environment().
                ' · BD='.$this->baseDeDatos().
                ' · host='.$this->host().
                ' · puerto='.$this->puerto()
            );

            return self::SUCCESS;
        }

        $rutaBloqueo = (string) config('e2e.reset_lock_path', storage_path('framework/e2e-reset.lock'));
        $bloqueo = fopen($rutaBloqueo, 'c');

        if ($bloqueo === false || ! flock($bloqueo, LOCK_EX | LOCK_NB)) {
            $this->error("ABORTADO: ya hay un reseed E2E en curso (lock: {$rutaBloqueo}).");

            if (is_resource($bloqueo)) {
                fclose($bloqueo);
            }

            return self::FAILURE;
        }

        try {
            // La configuración ya fue validada; esta consulta comprueba que la
            // conexión real selecciona exactamente la base y el puerto E2E.
            $problemasConexion = $this->problemasDeConexion();

            if ($problemasConexion !== []) {
                $this->mostrarProblemas($problemasConexion);

                return self::FAILURE;
            }

            $this->info(
                'Reseed E2E autorizado: APP_ENV=e2e · BD='.$this->baseDeDatos().
                ' · host='.$this->host().' · puerto='.$this->puerto()
            );

            $this->call('migrate:fresh', ['--seed' => true, '--force' => true]);

            if ($this->option('con-volumen')) {
                $this->call('db:seed', ['--class' => 'VolumenSeeder', '--force' => true]);
            }
        } finally {
            flock($bloqueo, LOCK_UN);
            fclose($bloqueo);
        }

        $this->info('Reseed E2E completado sobre '.$this->baseDeDatos().'.');

        return self::SUCCESS;
    }

    /**
     * @param string[] $problemas
     */
    private function mostrarProblemas(array $problemas): void
    {
        foreach ($problemas as $problema) {
            $this->error($problema);
        }

        $this->error('ABORTADO: no se ejecutó ningún reset y no se tocó ninguna base de datos.');
    }

    /**
     * @return string[]
     */
    private function problemasDeSeguridad(): array
    {
        $problemas = [];

        if (env('E2E_ALLOW_RESET') !== '1') {
            $problemas[] = 'Falta autorización explícita: define E2E_ALLOW_RESET=1.';
        }

        if (! file_exists(base_path('.env.e2e'))) {
            $problemas[] = 'No existe backend/.env.e2e (configuración E2E obligatoria).';
        }

        if (file_exists(base_path('bootstrap/cache/config.php'))) {
            $problemas[] = 'Existe bootstrap/cache/config.php; la configuración cacheada no es válida para E2E.';
        }

        if ($this->laravel->environment() !== 'e2e') {
            $problemas[] = 'APP_ENV="'.$this->laravel->environment().'"; debe ser "e2e" (usa --env=e2e).';
        }

        if (! preg_match(self::BD_PERMITIDA, $this->baseDeDatos())) {
            $problemas[] = 'Base de datos resuelta="'.$this->baseDeDatos().'"; solo se permite "proyectwin_e2e".';
        }

        if (! in_array($this->host(), ['127.0.0.1', 'localhost', '::1'], true)) {
            $problemas[] = 'Host de BD="'.$this->host().'"; solo se permite loopback.';
        }

        return $problemas;
    }

    /**
     * Comprueba la conexión real, sin modificar datos.
     *
     * La identidad fiable disponible es la base seleccionada y el puerto real
     * del servidor. No se compara @@hostname porque en instalaciones locales
     * puede ser el nombre de la máquina y no una prueba de destino.
     *
     * @return string[]
     */
    private function problemasDeConexion(): array
    {
        if ($this->driver() !== 'mysql') {
            return ['Driver de BD="'.$this->driver().'"; E2E solo admite mysql.'];
        }

        try {
            $fila = DB::connection()->selectOne('select database() as base, @@port as puerto');
        } catch (\Throwable $e) {
            return ['No se pudo verificar la conexión efectiva a la base E2E; se abortó sin operar.'];
        }

        if (! $fila) {
            return ['La comprobación de conexión no devolvió identidad de base; se abortó sin operar.'];
        }

        $base = (string) ($fila->base ?? '');
        $puerto = (string) ($fila->puerto ?? '');

        $problemas = [];

        if ($base !== 'proyectwin_e2e') {
            $problemas[] = 'La conexión efectiva selecciona una base distinta de "proyectwin_e2e"; se abortó sin operar.';
        }

        if ($puerto !== $this->puerto()) {
            $problemas[] = 'El puerto real de la conexión no coincide con el puerto configurado; se abortó sin operar.';
        }

        return $problemas;
    }

    private function conexion(): string
    {
        return (string) config('database.default');
    }

    private function baseDeDatos(): string
    {
        return (string) config("database.connections.{$this->conexion()}.database");
    }

    private function host(): string
    {
        return (string) config("database.connections.{$this->conexion()}.host");
    }

    private function puerto(): string
    {
        return (string) config("database.connections.{$this->conexion()}.port", '3306');
    }

    private function driver(): string
    {
        return (string) config("database.connections.{$this->conexion()}.driver");
    }
}
