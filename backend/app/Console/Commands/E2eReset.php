<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;

/**
 * Reseed de la base exclusiva de Playwright.
 *
 * Opera únicamente sobre una base desechable `ptwin_e2e_6an_<identificador>`,
 * y exige:
 *   - E2E_ALLOW_RESET=1 (autorización explícita),
 *   - APP_ENV=e2e (se pasa por variable de proceso),
 *   - E2E_DB_DATABASE igual a DB_DATABASE resuelta,
 *   - ausencia de DATABASE_URL y de configuración cacheada incompatible,
 *   - conexión efectiva verificada (base y puerto reales),
 *   - lock exclusivo (evita reseeds concurrentes).
 *
 * Si algo de esto falla, la ejecución ABORTA sin tocar ninguna base de datos.
 * La opción --check-only valida entorno y conexión sin migrar ni sembrar.
 */
class E2eReset extends Command
{
    protected $signature = 'e2e:reset
        {--check-only : valida entorno y conexión efectiva sin migrar ni sembrar}';

    protected $description = 'Reinicia una base E2E desechable ptwin_e2e_6an_*.';

    private const BD_DESECHABLE = '/^ptwin_e2e_6an_[a-z0-9_]{6,40}$/';

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
                ' · solicitada='.$this->baseSolicitada().
                ' · host='.$this->host().
                ' · puerto='.$this->puerto()
            );

            return self::SUCCESS;
        }

        $rutaBloqueo = (string) config('e2e.reset_lock_path', storage_path('framework/e2e-reset.lock'));
        $bloqueo = $this->adquirirLock($rutaBloqueo);

        if ($bloqueo === null) {
            $this->error("ABORTADO: ya hay un reseed E2E en curso (lock: {$rutaBloqueo}).");

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
                'Reseed autorizado: APP_ENV=e2e · BD='.$this->baseDeDatos().
                ' · solicitada='.$this->baseSolicitada().
                ' · host='.$this->host().' · puerto='.$this->puerto()
            );

            // Destructivo SOLO sobre la base validada arriba.
            $resultado = $this->ejecutarMigrateFresh();

            if ($resultado !== self::SUCCESS) {
                $this->error('El reseed E2E falló: migrate:fresh devolvió '.$resultado.'.');

                return self::FAILURE;
            }
        } finally {
            $this->liberarLock($bloqueo);
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
    protected function problemasDeSeguridad(): array
    {
        $problemas = [];

        if (env('E2E_ALLOW_RESET') !== '1') {
            $problemas[] = 'Falta autorización explícita: define E2E_ALLOW_RESET=1.';
        }

        if (filled(env('DATABASE_URL'))) {
            $problemas[] = 'DATABASE_URL no está permitido en E2E; usa DB_HOST/DB_PORT/DB_DATABASE explícitos.';
        }

        $configCache = $this->laravel->getCachedConfigPath();

        if (file_exists($configCache)) {
            $problemas[] = 'Existe configuración cacheada ('.$configCache.'); no es válida para E2E.';
        }

        if ($this->laravel->environment() !== 'e2e') {
            $problemas[] = 'APP_ENV="'.$this->laravel->environment().'"; debe ser "e2e" (define APP_ENV=e2e en el proceso).';
        }

        $solicitada = $this->baseSolicitada();

        if ($solicitada === '') {
            $problemas[] = 'E2E_DB_DATABASE es obligatorio y debe ser una base desechable "ptwin_e2e_6an_<identificador>".';
        } elseif (! preg_match(self::BD_DESECHABLE, $solicitada)) {
            $problemas[] = 'E2E_DB_DATABASE="'.$solicitada.'"; solo se permite una base desechable "ptwin_e2e_6an_<identificador>".';
        }

        if ($this->baseDeDatos() !== $solicitada) {
            $problemas[] = 'DB_DATABASE="'.$this->baseDeDatos().'" no coincide con E2E_DB_DATABASE="'.$solicitada.'".';
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
    protected function problemasDeConexion(): array
    {
        if ($this->driver() !== 'mysql') {
            return ['Driver de BD="'.$this->driver().'"; E2E solo admite mysql.'];
        }

        try {
            $fila = $this->identidadConexion();
        } catch (\Throwable $e) {
            return ['No se pudo verificar la conexión efectiva a la base E2E; se abortó sin operar.'];
        }

        if (! $fila) {
            return ['La comprobación de conexión no devolvió identidad de base; se abortó sin operar.'];
        }

        $base = (string) ($fila->base ?? '');
        $puerto = (string) ($fila->puerto ?? '');

        $problemas = [];

        if ($base !== $this->baseSolicitada()) {
            $problemas[] = 'La conexión efectiva selecciona una base distinta de "'.$this->baseSolicitada().'"; se abortó sin operar.';
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

    private function baseSolicitada(): string
    {
        $valor = env('E2E_DB_DATABASE');

        return is_string($valor) && trim($valor) !== '' ? trim($valor) : '';
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

    /**
     * Punto de extensión para pruebas: la implementación real consulta la
     * conexión efectiva. No se usa para saltarse la validación.
     */
    protected function identidadConexion(): ?object
    {
        return DB::connection()->selectOne('select database() as base, @@port as puerto');
    }

    /**
     * Punto de extensión para pruebas: ejecuta el reseed real. La guarda de
     * conexión ya se validó antes de llamar a este método.
     */
    protected function ejecutarMigrateFresh(): int
    {
        return $this->call('migrate:fresh', ['--seed' => true, '--force' => true]);
    }

    /**
     * @return resource|null
     */
    protected function adquirirLock(string $ruta): mixed
    {
        $bloqueo = fopen($ruta, 'c');

        if ($bloqueo === false || ! flock($bloqueo, LOCK_EX | LOCK_NB)) {
            if (is_resource($bloqueo)) {
                fclose($bloqueo);
            }

            return null;
        }

        return $bloqueo;
    }

    /**
     * @param resource|null $bloqueo
     */
    protected function liberarLock(mixed $bloqueo): void
    {
        if (is_resource($bloqueo)) {
            flock($bloqueo, LOCK_UN);
            fclose($bloqueo);
        }
    }
}
