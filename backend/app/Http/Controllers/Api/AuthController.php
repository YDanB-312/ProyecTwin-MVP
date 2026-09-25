<?php

namespace App\Http\Controllers\Api;

use App\Models\GeneralUser;
use App\Support\Auditoria;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Password;
use Illuminate\Validation\Rule;

class AuthController extends Controller
{
    public function login(Request $request)
    {
        $request->validate([
            'correo' => 'required|email',
            'password' => 'required',
        ]);

        $user = GeneralUser::where('correo', $request->correo)->first();

        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json(['message' => 'Credenciales incorrectas. Verifica tus datos.'], 422);
        }

        if (!$user->estado) {
            return response()->json(['message' => 'Cuenta suspendida. Contacta al administrador.'], 403);
        }

        $token = $user->createToken('proyectwin')->plainTextToken;

        return response()->json([
            'user' => $user,
            'token' => $token,
            'rol' => $user->rol,
        ]);
    }

    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();
        return response()->json(['message' => 'Sesión cerrada.']);
    }

    public function me(Request $request)
    {
        return $request->user();
    }

    // Cambio de correo del propio usuario: exige la contraseña actual antes de
    // autorizar (el correo es el identificador de acceso). Al cambiarlo se
    // revocan las demás sesiones y se conserva la actual.
    public function changeEmail(Request $request)
    {
        $request->validate([
            'correo' => [
                'required', 'email',
                Rule::unique('general_users', 'correo')->ignore($request->user()->id),
            ],
            'password_actual' => 'required|string',
        ]);

        $user = $request->user();

        if (!Hash::check($request->password_actual, $user->password)) {
            return response()->json(['message' => 'La contraseña actual no es correcta.'], 422);
        }

        $nuevo = strtolower(trim($request->correo));
        if ($nuevo === strtolower((string) $user->correo)) {
            return response()->json(['message' => 'Ese ya es tu correo actual.'], 422);
        }

        $user->update(['correo' => $nuevo]);

        Auditoria::registrar('cambiar_correo', 'general_users', $user->id, ['correo' => $nuevo]);

        $actual = $user->currentAccessToken();
        if ($actual) {
            $user->tokens()->where('id', '!=', $actual->id)->delete();
        }

        return response()->json(['message' => 'Correo actualizado.', 'correo' => $user->correo]);
    }

    // Cambio de contraseña del propio usuario autenticado: valida la actual y
    // actualiza sin emitir un token nuevo (no rompe la sesión ni "Recordarme").
    public function changePassword(Request $request)
    {
        $request->validate([
            'password_actual' => 'required|string',
            'password' => 'required|min:6|max:255|confirmed',
        ]);

        $user = $request->user();

        if (!Hash::check($request->password_actual, $user->password)) {
            return response()->json(['message' => 'La contraseña actual no es correcta.'], 422);
        }

        $user->update(['password' => Hash::make($request->password)]);

        Auditoria::registrar('cambiar_clave', 'general_users', $user->id);

        return response()->json(['message' => 'Contraseña actualizada.']);
    }

    // Solicita el enlace de restablecimiento (público). Respuesta genérica para
    // no revelar qué correos existen. En local se devuelve el enlace para poder
    // probar el flujo sin abrir el correo.
    public function forgotPassword(Request $request)
    {
        $request->validate(['correo' => 'required|email']);
        $correo = strtolower(trim($request->correo));

        $tokenPlano = null;
        Password::sendResetLink(['correo' => $correo], function ($user, $token) use (&$tokenPlano) {
            // Al pasar un callback, el broker NO envía la notificación: se envía aquí.
            $tokenPlano = $token;
            $user->sendPasswordResetNotification($token);
        });

        Auditoria::registrar('solicitar_reset', 'general_users', null, ['correo' => $correo]);

        $respuesta = [
            'message' => 'Si el correo está registrado, te enviamos un enlace para restablecer tu contraseña.',
        ];

        if (app()->environment('local') && $tokenPlano) {
            $respuesta['reset_url'] = rtrim((string) config('app.frontend_url'), '/')
                . '/restablecer-contrasena?' . http_build_query([
                    'token' => $tokenPlano,
                    'correo' => $correo,
                ]);
        }

        return response()->json($respuesta);
    }

    // Restablece la contraseña con un token válido (público).
    public function resetPassword(Request $request)
    {
        $request->validate([
            'token' => 'required',
            'correo' => 'required|email',
            'password' => 'required|min:6|max:255|confirmed',
        ]);

        $correo = strtolower(trim($request->correo));

        $status = Password::reset(
            [
                'correo' => $correo,
                'token' => $request->token,
                'password' => $request->password,
                'password_confirmation' => $request->password_confirmation,
            ],
            function ($user, $password) {
                $user->forceFill(['password' => Hash::make($password)])->save();
                // Por seguridad, se cierran todas las sesiones tras el cambio.
                $user->tokens()->delete();
            }
        );

        if ($status === Password::PASSWORD_RESET) {
            Auditoria::registrar('restablecer_clave', 'general_users', null, ['correo' => $correo]);

            return response()->json(['message' => 'Contraseña restablecida. Ya puedes iniciar sesión.']);
        }

        return response()->json([
            'message' => 'El enlace es inválido o venció. Solicita uno nuevo.',
        ], 422);
    }
}
