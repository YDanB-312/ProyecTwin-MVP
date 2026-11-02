<?php

namespace App\Console\Commands;

use App\Models\GeneralUser;
use App\Support\CuentasDemo;
use Database\Seeders\DatabaseSeeder;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

// Preparación local de ProyecTwin. Nunca usa migrate:fresh, db:wipe ni
// truncamientos. Los cambios de esquema requieren --migrar y las operaciones
// sobre datos demo requieren flags explícitos; el comando no decide por su
// cuenta cuándo sobrescribir una instalación existente.
class DemoPreparar extends Command
{
    protected $signature = 'demo:preparar
        {--check-only : diagnostica entorno, migraciones y esquema sin modificar datos}
        {--migrar : aplica migraciones pendientes después del preflight}
        {--solo-credenciales : rota únicamente las credenciales de las cuentas demo}
        {--actualizar-correos : migra los correos legacy de las cuentas demo al dominio reservado}
        {--sembrar : ejecuta el escenario demo idempotente}';

    protected $description = 'Prepara la base local de ProyecTwin con migraciones y datos demo, sin operaciones destructivas.';

    private const BASE_PERMITIDA = 'proyectwin_mvp';

    public function handle(): int
    {
        $problemas = $this->problemasDeSeguridad();

        if ($problemas !== []) {
            foreach ($problemas as $problema) {
                $this->error($problema);
            }

            $this->error('ABORTADO: no se modificó ninguna base de datos.');

            return self::FAILURE;
        }

        $pendientes = $this->migracionesPendientes();

        if ($this->option('check-only')) {
            $this->info('Entorno OK: APP_ENV=' . $this->laravel->environment() . ' · BD=' . $this->baseDeDatos() . ' · host=' . $this->host() . ' · puerto=' . $this->puerto());
            $this->line('Migraciones pendientes: ' . ($pendientes === [] ? 'ninguna' : count($pendientes)));

            foreach ($pendientes as $migracion) {
                $this->line('  - ' . $migracion);
            }

            $this->line('Esquema general_users: ' . $this->estadoEsquemaGeneralUsers() . '.');
            $this->line('Este diagnóstico no crea backups ni modifica datos.');

            return self::SUCCESS;
        }

        $hayUsuarios = Schema::hasTable('general_users') && GeneralUser::count() > 0;
        $hayFlags = $this->option('migrar') || $this->option('solo-credenciales')
            || $this->option('actualizar-correos') || $this->option('sembrar');

        if (! $hayFlags && $hayUsuarios) {
            $this->warn('La base ya contiene usuarios. No se modificó nada.');
            $this->line('Usa --check-only para diagnosticar o combina --migrar/--actualizar-correos/--solo-credenciales/--sembrar.');
            return self::FAILURE;
        }

        if ($hayFlags) {
            $this->line('Plan solicitado: se aplicarán solo las operaciones indicadas por flags.');
            $this->line('Migraciones pendientes: ' . count($pendientes) . '. Esquema general_users: ' . $this->estadoEsquemaGeneralUsers() . '.');
            $this->warn('Este comando no crea backups. Confirma que tienes un respaldo restaurable antes de continuar.');
        }

        if ($this->option('migrar') || (! $hayFlags && ! $hayUsuarios)) {
            if ($this->bloqueoEsquemaGeneralUsers()) {
                return self::FAILURE;
            }

            if (! $this->migrar()) {
                return self::FAILURE;
            }
        }

        if ($this->option('actualizar-correos')) {
            if (! Schema::hasTable('general_users')) {
                $this->error('--actualizar-correos requiere la tabla general_users. Ejecuta --migrar primero.');
                return self::FAILURE;
            }

            $resultado = CuentasDemo::actualizarCorreos();

            if ($resultado['conflictos'] !== []) {
                $this->error('Conflictos de correo; no se actualizó ninguna cuenta: ' . implode(', ', $resultado['conflictos']));
                return self::FAILURE;
            }

            $this->info('Correos demo actualizados: ' . $resultado['actualizados']);
        }

        if ($this->option('solo-credenciales')) {
            if (! Schema::hasTable('general_users') || ! Schema::hasColumn('general_users', 'email_verified_at')) {
                $this->error('--solo-credenciales requiere email_verified_at. Ejecuta --migrar primero.');
                return self::FAILURE;
            }

            $actualizadas = CuentasDemo::actualizarCredenciales();

            if ($actualizadas === 0) {
                $this->error('No se encontraron cuentas demo para actualizar.');
                return self::FAILURE;
            }

            $this->info("Credenciales demo actualizadas: {$actualizadas}");
        }

        if ($this->option('sembrar') || (! $hayFlags && ! $hayUsuarios)) {
            if (! Schema::hasTable('demo_seed_records')
                || ! Schema::hasTable('general_users')
                || ! Schema::hasColumn('general_users', 'email_verified_at')) {
                $this->error('--sembrar requiere las migraciones de verificación y demo_seed_records. Ejecuta --migrar primero.');
                return self::FAILURE;
            }

            if (! $this->sembrar()) {
                return self::FAILURE;
            }
        }

        $this->info('Listo. No se usó migrate:fresh, db:wipe ni TRUNCATE.');

        return self::SUCCESS;
    }

    /**
     * @return string[]
     */
    private function problemasDeSeguridad(): array
    {
        $problemas = [];

        // Solo la instalación local. Las bases test/e2e tienen sus propios comandos.
        if ($this->laravel->environment() !== 'local') {
            $problemas[] = 'APP_ENV="' . $this->laravel->environment() . '"; demo:preparar solo admite local.';
        }

        if (filled(env('DATABASE_URL'))) {
            $problemas[] = 'DATABASE_URL está definido; usa DB_HOST/DB_PORT/DB_DATABASE explícitos.';
        }

        if (file_exists(base_path('bootstrap/cache/config.php'))) {
            $problemas[] = 'Existe bootstrap/cache/config.php; ejecuta `php artisan config:clear`.';
        }

        if ($this->driver() !== 'mysql') {
            $problemas[] = 'Driver de BD="' . $this->driver() . '"; solo se admite mysql.';
        }

        if ($this->baseDeDatos() !== self::BASE_PERMITIDA) {
            $problemas[] = 'Base de datos="' . $this->baseDeDatos() . '"; solo se permite ' . self::BASE_PERMITIDA . '.';
        }

        if (! in_array($this->host(), ['127.0.0.1', 'localhost', '::1'], true)) {
            $problemas[] = 'Host de BD="' . $this->host() . '"; solo se permite loopback.';
        }

        try {
            $fila = DB::connection()->selectOne('select database() as base, @@port as puerto');

            if (! $fila || (string) $fila->base !== $this->baseDeDatos()) {
                $problemas[] = 'La conexión efectiva no selecciona la base configurada; se abortó sin operar.';
            } elseif ((string) $fila->puerto !== $this->puerto()) {
                $problemas[] = 'El puerto real no coincide con el configurado; se abortó sin operar.';
            }
        } catch (\Throwable $e) {
            $problemas[] = 'No se pudo verificar la conexión; se abortó sin operar.';
        }

        return $problemas;
    }

    /**
     * @return string[]
     */
    private function migracionesPendientes(): array
    {
        $archivos = collect(glob(database_path('migrations/*.php')))
            ->map(fn ($ruta) => basename($ruta, '.php'))
            ->sort()
            ->values();

        if (! Schema::hasTable('migrations')) {
            return $archivos->all();
        }

        $aplicadas = DB::table('migrations')->pluck('migration')->all();

        return $archivos->diff($aplicadas)->values()->all();
    }

    private function estadoEsquemaGeneralUsers(): string
    {
        if (! Schema::hasTable('general_users')) {
            return 'sin tabla (instalación nueva)';
        }

        if (! Schema::hasColumn('general_users', 'email_verified_at')) {
            return 'pendiente de adaptación única (falta email_verified_at)';
        }

        return 'actualizado';
    }

    private function bloqueoEsquemaGeneralUsers(): bool
    {
        if (! Schema::hasTable('general_users') || Schema::hasColumn('general_users', 'email_verified_at')) {
            return false;
        }

        $this->error('ABORTADO antes de migrar: general_users existe pero no tiene email_verified_at.');
        $this->line('La base conserva el esquema anterior. Aplica la adaptación única documentada en README.md:');
        $this->line('  1. genera un backup restaurable');
        $this->line('  2. ejecuta el ALTER TABLE de adaptación (añade email_verified_at y retira columnas legacy)');
        $this->line('  3. vuelve a ejecutar --migrar');
        $this->line('No se modificó el esquema.');

        return true;
    }

    private function migrar(): bool
    {
        $this->info('Aplicando migraciones pendientes...');
        $codigo = $this->call('migrate', ['--force' => true]);

        if ($codigo !== 0) {
            $this->error('La migración falló. Restaura el backup y no continúes; no se ejecutarán seeders ni credenciales.');
            return false;
        }

        return true;
    }

    private function sembrar(): bool
    {
        $this->info('Ejecutando seeders demo idempotentes...');
        $codigo = $this->call('db:seed', [
            '--class' => DatabaseSeeder::class,
            '--force' => true,
        ]);

        if ($codigo !== 0) {
            $this->error('El seeder demo falló. No se continuó.');
            return false;
        }

        return true;
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
