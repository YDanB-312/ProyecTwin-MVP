<?php

namespace App\Support;

use App\Models\GeneralUser;
use Illuminate\Support\Facades\Hash;

// Fuente única de las cuentas de demostración. Los correos nuevos usan un
// dominio reservado para no coincidir con buzones reales. `correo_legacy`
// permite adoptar instalaciones locales anteriores sin duplicar usuarios.
class CuentasDemo
{
    public const CUENTAS = [
        'mgonzalez' => [
            'nombre' => 'María', 'apellido' => 'González', 'rol' => 'aprendiz',
            'correo' => 'maria.gonzalez@example.com', 'correo_legacy' => 'maria.gonzalez@soy.sena.edu.co',
        ],
        'amartinez' => [
            'nombre' => 'Ana', 'apellido' => 'Martínez', 'rol' => 'aprendiz',
            'correo' => 'ana.martinez@example.com', 'correo_legacy' => 'ana.martinez@soy.sena.edu.co',
        ],
        'jperez' => [
            'nombre' => 'Juan', 'apellido' => 'Pérez', 'rol' => 'aprendiz',
            'correo' => 'juan.perez@example.com', 'correo_legacy' => 'juan.perez@soy.sena.edu.co',
        ],
        'lgomez' => [
            'nombre' => 'Laura', 'apellido' => 'Gómez', 'rol' => 'aprendiz',
            'correo' => 'laura.gomez@example.com', 'correo_legacy' => 'laura.gomez@soy.sena.edu.co',
        ],
        'lsanchez' => [
            'nombre' => 'Laura', 'apellido' => 'Sánchez Pérez', 'rol' => 'aprendiz',
            'correo' => 'laura.sanchez@example.com', 'correo_legacy' => 'laura.sanchez@soy.sena.edu.co',
        ],
        'dramirez' => [
            'nombre' => 'Diego', 'apellido' => 'Ramírez Castro', 'rol' => 'aprendiz',
            'correo' => 'diego.ramirez@example.com', 'correo_legacy' => 'diego.ramirez@soy.sena.edu.co',
        ],
        'pmorales' => [
            'nombre' => 'Patricia', 'apellido' => 'Morales Vega', 'rol' => 'aprendiz',
            'correo' => 'patricia.morales@example.com', 'correo_legacy' => 'patricia.morales@soy.sena.edu.co',
        ],
        'acifuentes' => [
            'nombre' => 'Andrés', 'apellido' => 'Cifuentes', 'rol' => 'aprendiz',
            'correo' => 'andres.cifuentes@example.com', 'correo_legacy' => 'andres.cifuentes@soy.sena.edu.co',
        ],
        'cruiz' => [
            'nombre' => 'Carlos', 'apellido' => 'Ruiz', 'rol' => 'instructor',
            'correo' => 'carlos.ruiz@example.com', 'correo_legacy' => 'carlos.ruiz@sena.edu.co',
        ],
        'crodriguez' => [
            'nombre' => 'Carlos', 'apellido' => 'Rodríguez Díaz', 'rol' => 'instructor',
            'correo' => 'carlos.rodriguez@example.com', 'correo_legacy' => 'carlos.rodriguez@sena.edu.co',
        ],
        'amartinez1' => [
            'nombre' => 'Andrés', 'apellido' => 'Martínez López', 'rol' => 'instructor',
            'correo' => 'andres.martinez@example.com', 'correo_legacy' => 'andres.martinez@sena.edu.co',
        ],
        'lfernando' => [
            'nombre' => 'Luis Fernando', 'apellido' => 'García', 'rol' => 'instructor',
            'correo' => 'luis.garcia@example.com', 'correo_legacy' => 'luis.garcia@sena.edu.co',
        ],
        'a' => [
            'nombre' => 'Administrador', 'apellido' => '', 'rol' => 'admin',
            'correo' => 'admin.proyectwin@example.com', 'correo_legacy' => 'admin@sena.edu.co',
        ],
        'mfernanda' => [
            'nombre' => 'María Fernanda', 'apellido' => 'Torres', 'rol' => 'admin',
            'correo' => 'maria.torres@example.com', 'correo_legacy' => 'maria.torres@sena.edu.co',
        ],
    ];

    public const CORREOS = [
        'maria.gonzalez@example.com',
        'ana.martinez@example.com',
        'juan.perez@example.com',
        'laura.gomez@example.com',
        'laura.sanchez@example.com',
        'diego.ramirez@example.com',
        'patricia.morales@example.com',
        'andres.cifuentes@example.com',
        'carlos.ruiz@example.com',
        'carlos.rodriguez@example.com',
        'andres.martinez@example.com',
        'luis.garcia@example.com',
        'admin.proyectwin@example.com',
        'maria.torres@example.com',
    ];

    // Cuentas heredadas que no forman parte del escenario demo nuevo, pero que
    // la preparación local debe administrar si existen en una base anterior.
    public const CUENTAS_LEGACY = [
        'DeTl_4TDKj' => [
            'nombre' => 'Demo', 'apellido' => 'Temporal', 'rol' => 'aprendiz',
            'correo' => 'demo.temporal@example.com',
            'correo_legacy' => 'demo.temporal@soy.sena.edu.co',
        ],
    ];

    public static function cuentasAdministradas(): array
    {
        return self::CUENTAS + self::CUENTAS_LEGACY;
    }

    public static function correosAdministrados(): array
    {
        $correos = [];

        foreach (self::cuentasAdministradas() as $cuenta) {
            $correos[] = $cuenta['correo'];
            $correos[] = $cuenta['correo_legacy'];
        }

        return array_values(array_unique($correos));
    }

    public static function password(): string
    {
        return (string) config('demo.password');
    }

    public static function buscaUsuario(string $username): ?GeneralUser
    {
        $cuenta = self::cuentasAdministradas()[$username] ?? null;

        if (! $cuenta) {
            return null;
        }

        // Solo se adopta por los correos conocidos del escenario. Nunca por
        // username: podría coincidir con una cuenta real ajena al demo.
        return GeneralUser::query()
            ->where('correo', $cuenta['correo'])
            ->orWhere('correo', $cuenta['correo_legacy'])
            ->orderByRaw('CASE WHEN correo = ? THEN 0 ELSE 1 END', [$cuenta['correo']])
            ->first();
    }

    // Rotación explícita de credenciales demo. No se ejecuta desde los seeders;
    // solo desde `demo:preparar --solo-credenciales` con autorización.
    public static function actualizarCredenciales(): int
    {
        $actualizadas = 0;

        foreach (array_keys(self::cuentasAdministradas()) as $username) {
            $usuario = self::buscaUsuario($username);

            if (! $usuario) {
                continue;
            }

            $usuario->forceFill([
                'password' => Hash::make(self::password()),
                'estado' => true,
                'email_verified_at' => now(),
            ])->save();

            $actualizadas++;
        }

        return $actualizadas;
    }

    // Transición de instalaciones anteriores. Solo actúa sobre cuentas demo
    // cuyo correo actual es exactamente el legacy conocido. Aborta por conflicto
    // si el correo nuevo ya pertenece a otra cuenta.
    public static function actualizarCorreos(): array
    {
        $pendientes = [];
        $conflictos = [];

        foreach (self::cuentasAdministradas() as $cuenta) {
            $usuario = GeneralUser::where('correo', $cuenta['correo_legacy'])->first();

            if (! $usuario) {
                continue;
            }

            $ocupado = GeneralUser::where('correo', $cuenta['correo'])
                ->where('id', '!=', $usuario->id)
                ->exists();

            if ($ocupado) {
                $conflictos[] = $cuenta['correo'];
                continue;
            }

            $pendientes[] = [$usuario, $cuenta['correo']];
        }

        // No se actualiza parcialmente si existe un conflicto.
        if ($conflictos !== []) {
            return ['actualizados' => 0, 'conflictos' => $conflictos];
        }

        foreach ($pendientes as [$usuario, $correo]) {
            $usuario->forceFill(['correo' => $correo])->save();
        }

        return ['actualizados' => count($pendientes), 'conflictos' => []];
    }
}
