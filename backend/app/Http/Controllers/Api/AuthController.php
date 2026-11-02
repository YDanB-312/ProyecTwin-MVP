<?php

namespace App\Http\Controllers\Api;

use App\Models\GeneralUser;
use App\Notifications\CorreoCambiadoNotification;
use App\Services\ActivacionCuenta;
use App\Services\RecuperacionPassword;
use App\Support\Auditoria;
use App\Support\PasswordPolicy;
use App\Support\SesionesUsuario;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Notification;
use Illuminate\Support\Facades\Password;
use Illuminate\Validation\Rule;
use Illuminate\Validation\ValidationException;
use Laravel\Sanctum\PersonalAccessToken;

class AuthController extends Controller
{
    public function login(Request $request)
    {
        $request->validate([
            // La web usa el correo. Se conserva `username` para Android y
            // cuentas legacy mientras dure la transición.
            'username' => 'required_without:correo|nullable|string|max:255',
            'correo' => 'required_without:username|nullable|email|max:255',
            'password' => 'required|string|max:255',
            'recordarme' => 'nullable|boolean',
        ]);

        if ($request->filled('correo')) {
            $user = GeneralUser::where('correo', strtolower(trim((string) $request->correo)))->first();
        } else {
            // LOWER evita depender de la colación de la base: los usernames
            // históricos tienen mayúsculas y el cliente los envía en minúsculas.
            $user = GeneralUser::whereRaw('LOWER(username) = ?', [
                strtolower(trim((string) $request->username)),
            ])->first();
        }

        if (! $user || ! Hash::check($request->password, $user->password)) {
            return response()->json(['message' => 'Credenciales incorrectas. Verifica tus datos.'], 422);
        }

        if (! $user->estado) {
            return response()->json(['message' => 'Cuenta suspendida. Contacta al administrador.'], 403);
        }

        if (! $user->email_verified_at) {
            return response()->json([
                'message' => 'Tu cuenta está pendiente de activación. Revisa el correo de activación.',
                'code' => 'cuenta_no_activada',
            ], 403);
        }

        // Sesión por cookie (SPA del mismo origen). Solo si la petición es
        // "stateful"; en móvil/tests no hay sesión y se usa el token Bearer.
        // "Recordarme" usa el remember_token (sesión persistente).
        if ($request->hasSession()) {
            auth('web')->login($user, $request->boolean('recordarme'));
            $request->session()->regenerate();
            // D-01: versión de sesión para invalidar también las sesiones de
            // archivo al desactivar/reactivar una cuenta.
            $request->session()->put(
                'auth_session_version',
                (string) $user->remember_token
            );
        }

        // El token Bearer solo se emite a clientes sin sesión (móvil/tests).
        // El SPA usa la cookie httpOnly: no necesita un token que quedaría sin
        // usar y sin revocar.
        $token = null;
        if (! $request->hasSession()) {
            $token = $user->createToken('proyectwin')->plainTextToken;
        }

        return response()->json([
            'user' => $user->makeVisible(['username', 'email_verified_at']),
            'token' => $token,
            'rol' => $user->rol,
        ]);
    }

    public function logout(Request $request)
    {
        // Token Bearer (móvil) si lo hay. Con sesión (cookie), Sanctum devuelve
        // un TransientToken que no se puede borrar (no hay token que revocar).
        $token = $request->user()?->currentAccessToken();
        if ($token instanceof PersonalAccessToken) {
            $token->delete();
        }

        // Sesión por cookie (SPA): cierra la sesión de este dispositivo e
        // invalida el remember cookie local sin rotar el token global de otras
        // sesiones.
        if ($request->hasSession()) {
            auth('web')->logoutCurrentDevice();
            $request->session()->invalidate();
            $request->session()->regenerateToken();
        }

        return response()->json(['message' => 'Sesión cerrada.']);
    }

    public function me(Request $request)
    {
        return $request->user()->makeVisible(['username', 'email_verified_at']);
    }

    // Cambio de correo del propio usuario: exige la contraseña actual antes de
    // autorizar. El correo es el identificador de acceso; al cambiarlo se
    // revocan las demás sesiones y se avisa al correo anterior.
    public function changeEmail(Request $request)
    {
        // Primero se autentica la contraseña actual; después se valida la
        // unicidad del nuevo correo. Así un usuario autenticado no puede usar
        // el error de unicidad para enumerar correos antes de probar la clave.
        $request->validate([
            'password_actual' => 'required|string|max:255',
        ]);

        $user = $request->user();

        if (! Hash::check($request->password_actual, $user->password)) {
            return response()->json([
                'message' => 'La contraseña actual no es correcta.',
                'errors' => ['password_actual' => ['La contraseña actual no es correcta.']],
            ], 422);
        }

        $request->validate([
            'correo' => [
                'required', 'email', 'max:255',
                Rule::unique('general_users', 'correo')->ignore($user->id),
            ],
        ]);

        $nuevo = strtolower(trim((string) $request->correo));
        if ($nuevo === strtolower((string) $user->correo)) {
            return response()->json(['message' => 'Ese ya es tu correo actual.'], 422);
        }

        $anterior = strtolower((string) $user->correo);
        [$sessionId, $tokenId] = $this->contextoActual($request);

        DB::transaction(function () use ($user, $nuevo, $anterior, $sessionId, $tokenId) {
            $user->update(['correo' => $nuevo]);

            // Si la cuenta estaba pendiente, el enlace anterior queda inservible:
            // el administrador debe reenviar la activación al correo nuevo.
            if (! $user->email_verified_at) {
                \App\Models\AccountActivation::where('user_id', $user->id)->delete();
            }

            Auditoria::registrar('cambiar_correo', 'general_users', $user->id, ['correo' => $nuevo]);

            SesionesUsuario::revocarOtras($user->fresh(), $sessionId, $tokenId, $anterior);
        });

        // El aviso es informativo y no debe mantener abierta la transacción.
        $this->avisarCorreoAnterior($anterior, $nuevo);

        return response()->json(['message' => 'Correo actualizado.', 'correo' => $user->correo]);
    }

    // Cambio de contraseña del propio usuario autenticado: valida la actual,
    // actualiza el hash y conserva la sesión actual, revocando las demás.
    public function changePassword(Request $request)
    {
        $request->validate([
            'password_actual' => 'required|string|max:255',
            'password' => PasswordPolicy::reglas(),
        ], PasswordPolicy::mensajes());

        $user = $request->user();

        if (! Hash::check($request->password_actual, $user->password)) {
            return response()->json([
                'message' => 'La contraseña actual no es correcta.',
                'errors' => ['password_actual' => ['La contraseña actual no es correcta.']],
            ], 422);
        }

        if (Hash::check($request->password, $user->password)) {
            return response()->json([
                'message' => 'La nueva contraseña debe ser distinta a la actual.',
                'errors' => ['password' => ['La nueva contraseña debe ser distinta a la actual.']],
            ], 422);
        }

        [$sessionId, $tokenId] = $this->contextoActual($request);

        DB::transaction(function () use ($user, $request, $sessionId, $tokenId) {
            $user->update([
                'password' => Hash::make($request->password),
            ]);

            Auditoria::registrar('cambiar_clave', 'general_users', $user->id);

            SesionesUsuario::revocarOtras($user->fresh(), $sessionId, $tokenId);
        });

        return response()->json(['message' => 'Contraseña actualizada.']);
    }

    // Solicita el enlace de restablecimiento (público). Respuesta genérica:
    // nunca revela si el correo existe, si la cuenta está suspendida o si un
    // token es válido. En local se devuelve el enlace para probar sin correo.
    public function forgotPassword(Request $request)
    {
        $request->validate(['correo' => 'required|email|max:255']);
        $correo = strtolower(trim((string) $request->correo));

        $usuario = GeneralUser::where('correo', $correo)->first();

        if ($usuario && $usuario->estado) {
            if (! $usuario->email_verified_at) {
                // Una cuenta pendiente necesita activación, no un reset.
                try {
                    app(ActivacionCuenta::class)->reenviar($usuario);
                } catch (\Throwable $e) {
                    report($e);
                }
            } else {
                app(RecuperacionPassword::class)->enviar($usuario);
            }
        }

        Auditoria::registrar('solicitar_reset', 'general_users', null, ['correo' => $correo]);

        return response()->json([
            'message' => 'Si el correo está registrado, te enviamos un enlace para activar tu cuenta o restablecer tu contraseña.',
        ]);
    }

    // Restablece la contraseña con un token válido (público). El token se
    // valida antes de cualquier comprobación sobre la contraseña actual: así
    // no existe un oráculo que permita adivinarla con un token falso.
    public function resetPassword(Request $request)
    {
        $request->validate([
            'token' => 'required|string|max:255',
            'correo' => 'required|email|max:255',
            'password' => PasswordPolicy::reglas(),
        ], PasswordPolicy::mensajes());

        $correo = strtolower(trim((string) $request->correo));

        $status = Password::reset(
            [
                'correo' => $correo,
                'token' => $request->token,
                'password' => $request->password,
                'password_confirmation' => $request->password_confirmation,
            ],
            function ($user, $password) {
                if (Hash::check($password, $user->password)) {
                    throw ValidationException::withMessages([
                        'password' => 'La nueva contraseña debe ser distinta a la actual.',
                    ]);
                }

                DB::transaction(function () use ($user, $password) {
                    $user->forceFill([
                        'password' => Hash::make($password),
                    ])->save();

                    // Cierra todas las sesiones, tokens y enlaces pendientes;
                    // rota remember_token. La sesión actual no existe aquí.
                    SesionesUsuario::revocar($user->fresh());
                });
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

    // Valida un token de activación y devuelve solo datos enmascarados para
    // mostrar la pantalla. No consume el token. El token viaja en el cuerpo
    // (la SPA lo lee del fragmento del enlace) para que nunca quede en paths,
    // query strings ni logs de servidor o proxies.
    public function validateActivation(Request $request)
    {
        $request->validate([
            'token' => 'required|string|max:255',
        ]);

        $token = (string) $request->token;

        $activacion = app(ActivacionCuenta::class)->buscarVigente($token);

        if (! $activacion) {
            return response()->json(['message' => ActivacionCuenta::MENSAJE_INVALIDO], 422);
        }

        $usuario = $activacion->user;

        return response()->json([
            'nombre' => $usuario->nombre,
            'correo' => $this->enmascararCorreo((string) $usuario->correo),
        ]);
    }

    // Establece la contraseña definitiva y activa la cuenta. El consumo es
    // atómico: dos solicitudes concurrentes no pueden activar dos veces.
    public function activate(Request $request)
    {
        $request->validate([
            'token' => 'required|string|max:255',
            'password' => PasswordPolicy::reglas(),
        ], PasswordPolicy::mensajes());

        $usuario = app(ActivacionCuenta::class)->activar(
            (string) $request->token,
            (string) $request->password
        );

        return response()->json([
            'message' => 'Cuenta activada. Ya puedes iniciar sesión.',
            'correo' => $usuario->correo,
        ]);
    }

    /**
     * @return array{0: ?string, 1: ?int}
     */
    private function contextoActual(Request $request): array
    {
        $sessionId = $request->hasSession() ? $request->session()->getId() : null;
        $token = $request->user()?->currentAccessToken();
        $tokenId = $token instanceof PersonalAccessToken ? $token->id : null;

        return [$sessionId, $tokenId];
    }

    private function avisarCorreoAnterior(string $anterior, string $nuevo): void
    {
        try {
            Notification::route('mail', $anterior)
                ->notify(new CorreoCambiadoNotification($anterior, $nuevo));
        } catch (\Throwable $e) {
            // El aviso es informativo: no revierte el cambio ni rompe la
            // respuesta principal.
            report($e);
        }
    }

    private function enmascararCorreo(string $correo): string
    {
        $partes = explode('@', $correo, 2);

        if (count($partes) !== 2 || $partes[0] === '' || $partes[1] === '') {
            return '***';
        }

        [$local, $dominio] = $partes;
        $visible = mb_substr($local, 0, 1);

        return $visible . str_repeat('*', max(2, mb_strlen($local) - 1)) . '@' . $dominio;
    }
}
