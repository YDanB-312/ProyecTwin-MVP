<?php

namespace Tests\Core\Database;

use App\Models\GeneralUser;
use App\Support\CuentasDemo;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Transición de cuentas demo: mapeo de correos legacy y rotación explícita de
// credenciales. Se ejecuta en la base de testing, nunca en proyectwin_mvp.
class CuentasDemoTest extends TestCase
{
    use DatabaseTransactions;

    private function cuenta(string $correo): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Demo',
            'apellido' => 'Transicion',
            'correo' => $correo,
            'tipo_documento' => 'CC',
            'numero_documento' => (string) random_int(3000000000, 3999999999),
            'password' => Hash::make('claveAnterior#2026'),
            'rol' => 'aprendiz',
            'estado' => false,
        ]);
    }

    public function test_actualiza_el_correo_legacy_y_rota_credenciales(): void
    {
        $usuario = $this->cuenta('maria.gonzalez@soy.sena.edu.co');

        $resultado = CuentasDemo::actualizarCorreos();

        $this->assertSame(1, $resultado['actualizados']);
        $this->assertSame([], $resultado['conflictos']);
        $this->assertSame('maria.gonzalez@example.com', $usuario->fresh()->correo);

        $actualizadas = CuentasDemo::actualizarCredenciales();

        $this->assertSame(1, $actualizadas);
        $this->assertTrue(Hash::check(CuentasDemo::password(), $usuario->fresh()->password));
        $this->assertTrue((bool) $usuario->fresh()->estado);
        $this->assertNotNull($usuario->fresh()->email_verified_at);
    }

    public function test_no_actualiza_si_el_correo_nuevo_ya_esta_ocupado(): void
    {
        $legacy = $this->cuenta('maria.gonzalez@soy.sena.edu.co');
        $this->cuenta('maria.gonzalez@example.com');

        $resultado = CuentasDemo::actualizarCorreos();

        $this->assertSame(0, $resultado['actualizados']);
        $this->assertContains('maria.gonzalez@example.com', $resultado['conflictos']);
        $this->assertSame('maria.gonzalez@soy.sena.edu.co', $legacy->fresh()->correo);
    }
}
