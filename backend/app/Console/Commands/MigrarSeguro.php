<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;
use Throwable;

// Ejecuta las migraciones bajo un lock de MySQL para que dos réplicas no
// compitan por el esquema. Si no hay migraciones pendientes, `migrate` no
// ejecuta DDL; el lock solo serializa arranques concurrentes.
class MigrarSeguro extends Command
{
    protected $signature = 'app:migrar-seguro
        {--timeout=30 : Segundos máximos de espera por el lock}';

    protected $description = 'Ejecuta migrate --force con lock MySQL para evitar carreras entre réplicas.';

    private const LOCK = 'proyectwin:migrate';

    // Códigos para que el entrypoint distinga una base no disponible de una
    // migración rota. Solo el 2 se reintenta.
    public const DB_UNAVAILABLE = 2;

    public const LOCK_TIMEOUT = 3;

    public const MIGRATION_FAILED = 4;

    public function handle(): int
    {
        $timeout = max(1, (int) $this->option('timeout'));

        try {
            $fila = DB::selectOne('SELECT GET_LOCK(?, ?) AS adquirido', [self::LOCK, $timeout]);
        } catch (Throwable $e) {
            $this->error('No se pudo conectar para el lock de migraciones: '.$e->getMessage());

            return self::DB_UNAVAILABLE;
        }

        if (! $fila || (int) ($fila->adquirido ?? 0) !== 1) {
            $this->error('No se pudo adquirir el lock de migraciones en '.$timeout.' s.');

            return self::LOCK_TIMEOUT;
        }

        try {
            $this->info('Lock de migraciones adquirido.');

            $resultado = (int) $this->call('migrate', ['--force' => true]);

            return $resultado === 0 ? self::SUCCESS : self::MIGRATION_FAILED;
        } catch (Throwable $e) {
            $this->error('La migración falló: '.$e->getMessage());

            return self::MIGRATION_FAILED;
        } finally {
            try {
                DB::select('SELECT RELEASE_LOCK(?)', [self::LOCK]);
            } catch (Throwable) {
                // Si la conexión se perdió, el lock se libera al cerrarla.
            }
        }
    }
}
