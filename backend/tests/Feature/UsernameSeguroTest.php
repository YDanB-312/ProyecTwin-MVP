<?php

namespace Tests\Feature;

use App\Models\GeneralUser;
use App\Support\Credenciales;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Generador de usernames para cuentas nuevas: fragmento legible del nombre y
// los apellidos + parte aleatoria segura (sin sufijos secuenciales).
class UsernameSeguroTest extends TestCase
{
    use DatabaseTransactions;

    private const FORMATO = '/^[A-Za-z0-9]{2}[A-Za-z0-9]{2}_[A-Za-z0-9]{5}$/';

    public function test_formato_longitud_y_caracteres_permitidos(): void
    {
        $username = Credenciales::usernameSeguro('María', 'González Pérez');

        $this->assertSame(10, mb_strlen($username));
        $this->assertMatchesRegularExpression(self::FORMATO, $username);
        $this->assertStringContainsString('_', $username);
    }

    public function test_incluye_fragmentos_del_nombre_y_los_apellidos(): void
    {
        $this->assertStringStartsWith('MaGz_', Credenciales::usernameSeguro('María', 'González Pérez'));
        $this->assertStringStartsWith('CaRz_', Credenciales::usernameSeguro('Carlos Andrés', 'Rodríguez Díaz'));
        $this->assertStringStartsWith('AnMz_', Credenciales::usernameSeguro('Ana', 'Martínez'));
        $this->assertStringStartsWith('LuGa_', Credenciales::usernameSeguro('Luis Fernando', 'García'));
    }

    public function test_normaliza_tildes_y_caracteres_especiales(): void
    {
        $this->assertStringStartsWith('JoNo_', Credenciales::usernameSeguro('José', 'Ñáñez Ñuño'));
    }

    public function test_la_parte_aleatoria_varia_y_tiene_letras_y_digitos(): void
    {
        $vistos = [];
        for ($i = 0; $i < 20; $i++) {
            $parte = substr(Credenciales::usernameSeguro('Ana', 'Martínez'), 5);
            $this->assertMatchesRegularExpression('/[A-Za-z]/', $parte);
            $this->assertMatchesRegularExpression('/[0-9]/', $parte);
            $vistos[] = $parte;
        }
        $this->assertGreaterThan(15, count(array_unique($vistos)));
    }

    public function test_sin_apellido_genera_username_valido(): void
    {
        $username = Credenciales::usernameSeguro('Administrador', '');

        $this->assertSame(10, mb_strlen($username));
        $this->assertMatchesRegularExpression(self::FORMATO, $username);
        $this->assertStringStartsWith('Ad', $username);
    }

    public function test_nombres_iguales_generan_usernames_distintos_y_unicos(): void
    {
        $usernames = [];
        for ($i = 0; $i < 5; $i++) {
            $usernames[] = GeneralUser::create([
                'nombre' => 'Carlos',
                'apellido' => 'Martínez',
                'correo' => 'carlos.' . $i . '.' . uniqid() . '@test.local',
                'password' => Hash::make('123456'),
                'rol' => 'aprendiz',
                'estado' => true,
            ])->username;
        }

        $this->assertCount(5, array_unique($usernames));
        foreach ($usernames as $username) {
            $this->assertMatchesRegularExpression(self::FORMATO, $username);
            $this->assertStringStartsWith('CaMz_', $username);
        }
    }

    public function test_el_generador_historico_conserva_su_formato(): void
    {
        // Lo usa el backfill de la migración de credenciales: no debe cambiar.
        $this->assertSame('cmartinez', Credenciales::username('Carlos', 'Martínez'));

        GeneralUser::create([
            'nombre' => 'Otro',
            'apellido' => 'Usuario',
            'correo' => 'otro.' . uniqid() . '@test.local',
            'username' => 'cmartinez',
            'password' => Hash::make('123456'),
            'rol' => 'aprendiz',
            'estado' => true,
        ]);

        $this->assertSame('cmartinez1', Credenciales::username('Carlos', 'Martínez'));
    }
}
