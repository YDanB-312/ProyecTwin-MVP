<?php

namespace Tests\Core\Unit;

use App\Models\GeneralUser;
use App\Support\CredencialesPdf;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Tests\TestCase;

// El PDF muestra el usuario y el estado de acceso. Nunca descifra ni expone
// contraseñas temporales.
class CredencialesPdfFilasTest extends TestCase
{
    use DatabaseTransactions;

    // Modelo sin guardar: las filas del PDF se resuelven sin tocar la BD.
    private function usuario(): GeneralUser
    {
        return new GeneralUser([
            'nombre' => 'Ana',
            'apellido' => 'Gómez',
            'username' => 'AnaGo_12345',
            'correo' => 'ana.credenciales@correo.local',
            'password' => 'hash-de-prueba',
            'rol' => 'aprendiz',
            'estado' => true,
        ]);
    }

    public function test_cuenta_activa_muestra_contrasena_ya_establecida(): void
    {
        $usuario = $this->usuario();
        $usuario->email_verified_at = now();

        $fila = CredencialesPdf::filas(collect([$usuario]))->first();

        $this->assertSame('Contraseña ya establecida', $fila['estado']);
        $this->assertArrayNotHasKey('password', $fila);
    }

    public function test_cuenta_pendiente_de_activacion_se_indica_en_el_pdf(): void
    {
        $usuario = $this->usuario();
        $usuario->email_verified_at = null;

        $fila = CredencialesPdf::filas(collect([$usuario]))->first();

        $this->assertSame('Pendiente de activación', $fila['estado']);
        $this->assertArrayNotHasKey('password', $fila);
    }

}
