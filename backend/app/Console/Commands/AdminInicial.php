<?php

namespace App\Console\Commands;

use App\Models\GeneralUser;
use App\Services\ActivacionCuenta;
use App\Services\AltaUsuario;
use App\Support\Auditoria;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

// Bootstrap del único administrador después de la limpieza final. No genera
// ni imprime contraseñas ni tokens: crea una cuenta pendiente y envía el
// enlace de activación. Debe ejecutarse con autorización explícita.
class AdminInicial extends Command
{
    protected $signature = 'admin:inicial
        {--correo= : Correo del administrador}
        {--nombre= : Nombres}
        {--apellido= : Apellidos}
        {--tipo-documento=CC : CC|TI|CE|PPT}
        {--documento= : Número de documento (opcional)}
        {--check-only : Valida entorno y censo sin escribir}
        {--reenviar : Rota y reenvía la activación de un administrador pendiente}
        {--confirmar-base= : Nombre exacto de la base para confirmar la escritura}';

    protected $description = 'Crea el administrador inicial pendiente de activación, sin contraseña temporal.';

    public function handle(): int
    {
        $base = (string) DB::connection()->getDatabaseName();

        if (! $this->esquemaListo()) {
            $this->error('Faltan migraciones: general_users.email_verified_at, account_activations o admins.');
            return self::FAILURE;
        }

        if ($this->option('check-only')) {
            $this->line('Entorno: ' . app()->environment());
            $this->line('Base: ' . $base);
            $this->line('Administradores: ' . GeneralUser::where('rol', 'admin')->count());
            $this->line('Mailer: ' . (string) config('mail.default'));
            $this->line('Correo real: ' . ($this->entregaCorreoDisponible() ? 'sí' : 'no'));

            return self::SUCCESS;
        }

        if (env('ADMIN_BOOTSTRAP_ALLOW') !== '1') {
            $this->error('Falta autorización explícita: define ADMIN_BOOTSTRAP_ALLOW=1.');
            return self::FAILURE;
        }

        if ((string) $this->option('confirmar-base') !== $base) {
            $this->error('Confirma la base exacta con --confirmar-base="' . $base . '".');
            return self::FAILURE;
        }

        if (! $this->entregaCorreoDisponible()) {
            $this->error('El mailer configurado no entrega correos reales (log/array/failover o falta BREVO_API_KEY). Configura SMTP/Brevo antes de crear el administrador.');
            return self::FAILURE;
        }

        $frontend = (string) config('app.frontend_url');
        if (! filter_var($frontend, FILTER_VALIDATE_URL)) {
            $this->error('FRONTEND_URL debe ser una URL absoluta para generar el enlace de activación.');
            return self::FAILURE;
        }

        if (app()->environment('production') && ! str_starts_with(strtolower($frontend), 'https://')) {
            $this->error('FRONTEND_URL debe usar HTTPS en producción.');
            return self::FAILURE;
        }

        $correo = strtolower(trim((string) $this->option('correo')));

        if (! filter_var($correo, FILTER_VALIDATE_EMAIL)) {
            $this->error('El correo del administrador es obligatorio y debe ser válido.');
            return self::FAILURE;
        }

        if ($this->option('reenviar')) {
            return $this->reenviar($correo);
        }

        return $this->crear($correo, $base);
    }

    private function crear(string $correo, string $base): int
    {
        if (GeneralUser::where('rol', 'admin')->exists()) {
            $this->error('Ya existe al menos un administrador. No se crea otro.');
            $this->line('Si el administrador existente sigue pendiente, reenvía su activación con:');
            $this->line('  php artisan admin:inicial --reenviar --correo="' . $correo . '" --confirmar-base="' . $base . '"');

            return self::FAILURE;
        }

        if (GeneralUser::where('correo', $correo)->exists()) {
            $this->error('El correo ya pertenece a una cuenta que no es administrador.');
            return self::FAILURE;
        }

        $nombre = trim((string) $this->option('nombre'));
        $apellido = trim((string) $this->option('apellido'));
        $tipoDocumento = strtoupper(trim((string) $this->option('tipo-documento')));
        $documento = trim((string) $this->option('documento'));

        if ($nombre === '' || $apellido === '') {
            $this->error('Nombre y apellido son obligatorios.');
            return self::FAILURE;
        }

        if (! in_array($tipoDocumento, ['CC', 'TI', 'CE', 'PPT'], true)) {
            $this->error('Tipo de documento inválido. Usa CC, TI, CE o PPT.');
            return self::FAILURE;
        }

        if ($documento !== '' && GeneralUser::where('numero_documento', $documento)->exists()) {
            $this->error('El número de documento ya está registrado.');
            return self::FAILURE;
        }

        $alta = app(AltaUsuario::class)->crear([
            'nombre' => $nombre,
            'apellido' => $apellido,
            'correo' => $correo,
            'tipo_documento' => $tipoDocumento,
            'numero_documento' => $documento !== '' ? $documento : null,
            'rol' => 'admin',
            'estado' => true,
        ], true);

        Auditoria::registrar('crear_admin_inicial', 'general_users', $alta['usuario']->id, [
            'correo' => $correo,
            'base' => $base,
        ]);

        if (! $alta['enviadas']) {
            $this->error('La cuenta quedó pendiente, pero el correo no pudo enviarse.');
            $this->line('Corrige el mailer y ejecuta de nuevo con --reenviar --confirmar-base="' . $base . '".');

            return self::FAILURE;
        }

        $this->info('Administrador pendiente creado. Revisa el correo de activación.');
        $this->line('No se generó ni mostró ninguna contraseña o token.');

        return self::SUCCESS;
    }

    private function reenviar(string $correo): int
    {
        $admin = GeneralUser::where('correo', $correo)->where('rol', 'admin')->first();

        if (! $admin) {
            $this->error('No existe un administrador con ese correo.');
            return self::FAILURE;
        }

        if ($admin->email_verified_at) {
            $this->error('El administrador ya está activado. Usa el flujo de recuperación de contraseña.');
            return self::FAILURE;
        }

        try {
            $resultado = app(ActivacionCuenta::class)->reenviar($admin);
        } catch (\Throwable $e) {
            report($e);
            $this->error('No se pudo reenviar la activación. Revisa el mailer y los logs; no se muestran detalles sensibles.');
            return self::FAILURE;
        }

        Auditoria::registrar('reenviar_activacion_admin', 'general_users', $admin->id, [
            'correo' => $correo,
            'enviado' => $resultado['enviado'],
        ]);

        if (! $resultado['enviado']) {
            $this->error('El enlace se rotó, pero el correo no pudo enviarse. Revisa el mailer.');
            return self::FAILURE;
        }

        $this->info('Enlace de activación reenviado. El enlace anterior quedó invalidado.');

        return self::SUCCESS;
    }

    private function esquemaListo(): bool
    {
        return Schema::hasTable('general_users')
            && Schema::hasColumn('general_users', 'email_verified_at')
            && Schema::hasTable('account_activations')
            && Schema::hasTable('admins');
    }

    private function entregaCorreoDisponible(): bool
    {
        // En testing se usa array/Notification::fake; la guarda aplica a
        // entornos reales donde un mailer log dejaría al admin sin enlace.
        if (app()->environment('testing')) {
            return true;
        }

        $mailer = strtolower((string) config('mail.default'));

        if ($mailer === 'brevo') {
            return trim((string) config('mail.mailers.brevo.key')) !== '';
        }

        if ($mailer === 'smtp') {
            $host = strtolower(trim((string) config('mail.mailers.smtp.host')));
            $port = (int) config('mail.mailers.smtp.port');

            // Rechaza el Mailpit local y los hosts de loopback: son válidos
            // para desarrollo, pero no para entregar el enlace del admin.
            return ! in_array($host, ['localhost', '127.0.0.1', '::1', 'mailpit'], true)
                && $port !== 1025;
        }

        return ! in_array($mailer, ['log', 'array', 'failover'], true);
    }
}
