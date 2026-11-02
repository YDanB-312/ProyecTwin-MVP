<?php

namespace App\Services;

use App\Models\Admin;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Support\Auditoria;
use App\Support\Credenciales;
use App\Support\Pii;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;

// Alta de una cuenta institucional. Toda cuenta nueva nace pendiente de
// activación y recibe un enlace de un solo uso. No se generan ni almacenan
// contraseñas temporales.
class AltaUsuario
{
    public function crear(array $datos, bool $enviarCorreo = true): array
    {
        $username = Credenciales::usernameSeguro($datos['nombre'], $datos['apellido']);

        $data = $datos;
        $data['correo'] = strtolower(trim($datos['correo']));
        $data['username'] = $username;
        // Hash de un secreto aleatorio descartado: la cuenta no tiene una
        // contraseña utilizable hasta que el usuario activa y define la suya.
        $data['password'] = Hash::make(Str::random(64));
        $data['email_verified_at'] = null;
        $data['estado'] = $datos['estado'] ?? true;

        $usuario = DB::transaction(function () use ($data) {
            $usuario = GeneralUser::create($data);

            // El perfil correspondiente nace con la cuenta. El de aprendiz
            // nace al asociarse a una ficha (id_programa es obligatorio).
            if ($usuario->rol === 'instructor') {
                Instructor::firstOrCreate(
                    ['id_usuario' => $usuario->id],
                    ['fecha_ingreso' => now()->toDateString()]
                );
            }

            if ($usuario->rol === 'admin') {
                Admin::firstOrCreate(['id_usuario' => $usuario->id]);
            }

            Auditoria::registrar('crear_usuario', 'general_users', $usuario->id, [
                'correo' => $usuario->correo,
                'username' => $usuario->username,
                'documento' => Pii::documento($usuario->tipo_documento, $usuario->numero_documento),
                'rol' => $usuario->rol,
                'modo' => 'activacion',
            ]);

            return $usuario;
        });

        $activacionToken = app(ActivacionCuenta::class)->crearToken($usuario);
        $activacion = null;
        $enviadas = false;

        if ($enviarCorreo) {
            $activacion = app(ActivacionCuenta::class)->enviar($usuario, $activacionToken);
            $enviadas = $activacion['enviado'];
        }

        return [
            'usuario' => $usuario->fresh(),
            'enviadas' => $enviadas,
            'activacion' => $activacion,
            // Uso interno (p. ej. alta desde ficha que envía después de la
            // transacción). Nunca debe devolverse al cliente.
            'activacion_token' => $activacionToken,
        ];
    }
}
