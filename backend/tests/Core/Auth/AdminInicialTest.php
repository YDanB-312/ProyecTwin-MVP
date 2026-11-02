<?php

namespace Tests\Core\Auth;

use App\Models\GeneralUser;
use App\Notifications\ActivacionCuentaNotification;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Tests\TestCase;

// Bootstrap del administrador inicial: sin contraseña temporal, sin token en
// stdout y con autorización/confirmación de base obligatorias.
class AdminInicialTest extends TestCase
{
    use DatabaseTransactions;

    private function base(): string
    {
        return (string) DB::connection()->getDatabaseName();
    }

    private function conAutorizacion(callable $callback)
    {
        $previos = [
            'env' => $_ENV['ADMIN_BOOTSTRAP_ALLOW'] ?? null,
            'server' => $_SERVER['ADMIN_BOOTSTRAP_ALLOW'] ?? null,
            'putenv' => getenv('ADMIN_BOOTSTRAP_ALLOW'),
        ];

        $_ENV['ADMIN_BOOTSTRAP_ALLOW'] = '1';
        $_SERVER['ADMIN_BOOTSTRAP_ALLOW'] = '1';
        putenv('ADMIN_BOOTSTRAP_ALLOW=1');

        try {
            return $callback();
        } finally {
            if ($previos['env'] === null) unset($_ENV['ADMIN_BOOTSTRAP_ALLOW']);
            else $_ENV['ADMIN_BOOTSTRAP_ALLOW'] = $previos['env'];

            if ($previos['server'] === null) unset($_SERVER['ADMIN_BOOTSTRAP_ALLOW']);
            else $_SERVER['ADMIN_BOOTSTRAP_ALLOW'] = $previos['server'];

            if ($previos['putenv'] === false) putenv('ADMIN_BOOTSTRAP_ALLOW');
            else putenv('ADMIN_BOOTSTRAP_ALLOW=' . $previos['putenv']);
        }
    }

    private function opciones(array $extra = []): array
    {
        return array_merge([
            '--correo' => 'admin.inicial@test.local',
            '--nombre' => 'Admin',
            '--apellido' => 'Inicial',
            '--tipo-documento' => 'CC',
            '--documento' => '90000001',
            '--confirmar-base' => $this->base(),
        ], $extra);
    }

    public function test_check_only_no_escribe_ni_exige_autorizacion(): void
    {
        $antes = GeneralUser::count();
        $this->artisan('admin:inicial', ['--check-only' => true])->assertSuccessful();
        $this->assertSame($antes, GeneralUser::count());
    }

    public function test_sin_autorizacion_aborta_sin_crear_cuenta(): void
    {
        $antes = GeneralUser::count();
        $this->artisan('admin:inicial', $this->opciones())->assertFailed();
        $this->assertSame($antes, GeneralUser::count());
    }

    public function test_confirmacion_de_base_incorrecta_aborta_sin_crear_cuenta(): void
    {
        $antes = GeneralUser::count();
        $this->conAutorizacion(function () {
            $this->artisan('admin:inicial', $this->opciones(['--confirmar-base' => 'otra_base']))
                ->assertFailed();
        });

        $this->assertSame($antes, GeneralUser::count());
    }

    public function test_crea_administrador_pendiente_sin_credenciales_temporales_ni_token_en_salida(): void
    {
        Notification::fake();

        $this->conAutorizacion(function () {
            $this->artisan('admin:inicial', $this->opciones())->assertSuccessful();
        });

        $user = GeneralUser::where('correo', 'admin.inicial@test.local')->firstOrFail();
        $this->assertSame('admin', $user->rol);
        $this->assertNull($user->email_verified_at);
        $this->assertDatabaseHas('admins', ['id_usuario' => $user->id]);
        $this->assertDatabaseHas('account_activations', ['user_id' => $user->id]);
        Notification::assertSentTo($user, ActivacionCuentaNotification::class);

        $this->assertStringNotContainsString('?token=', Artisan::output());
    }

    public function test_no_crea_un_segundo_administrador(): void
    {
        GeneralUser::create([
            'nombre' => 'Admin',
            'apellido' => 'Existente',
            'correo' => 'admin.existente@test.local',
            'password' => Hash::make('claveActual123'),
            'rol' => 'admin',
            'estado' => true,
        ]);

        $antes = GeneralUser::where('rol', 'admin')->count();

        $this->conAutorizacion(function () {
            $this->artisan('admin:inicial', $this->opciones())->assertFailed();
        });

        $this->assertSame($antes, GeneralUser::where('rol', 'admin')->count());
    }

    public function test_reenviar_rota_el_token_anterior(): void
    {
        Notification::fake();

        $this->conAutorizacion(function () {
            $this->artisan('admin:inicial', $this->opciones())->assertSuccessful();
        });

        $user = GeneralUser::where('correo', 'admin.inicial@test.local')->firstOrFail();
        $hashAnterior = $user->accountActivation()->value('token_hash');

        $this->conAutorizacion(function () {
            $this->artisan('admin:inicial', $this->opciones(['--reenviar' => true]))->assertSuccessful();
        });

        $hashNuevo = $user->fresh()->accountActivation()->value('token_hash');
        $this->assertNotSame($hashAnterior, $hashNuevo);
        $this->assertStringNotContainsString('?token=', Artisan::output());
    }
}
