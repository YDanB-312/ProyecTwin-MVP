<?php

namespace App\Http\Controllers\Api;

use App\Models\GeneralUser;
use App\Models\PadronUsuario;
use App\Support\Auditoria;
use App\Support\CodigoActivacion;
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
            'correo' => 'nullable|email',
            'identificador' => 'nullable|string|max:255',
            'password' => 'required',
            'recordarme' => 'nullable|boolean',
        ]);

        if (!$request->filled('correo') && !$request->filled('identificador')) {
            return response()->json(['message' => 'Ingresa tu correo o número de documento.'], 422);
        }

        $user = $this->buscarPorIdentificador($request);

        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json(['message' => 'Credenciales incorrectas. Verifica tus datos.'], 422);
        }

        if (!$user->estado) {
            // Distingue "pendiente de activar" de "suspendida" para guiar a la persona.
            $pendiente = CodigoActivacion::vigente($user->id);

            return response()->json([
                'message' => $pendiente
                    ? 'Tu cuenta está pendiente de activación. Ingresa el código que te entregamos.'
                    : 'Cuenta suspendida. Contacta al administrador.',
                'pendiente_activacion' => (bool) $pendiente,
                'correo' => $user->correo,
            ], 403);
        }

        // Sesión por cookie (SPA del mismo origen). Solo si la petición es
        // "stateful"; en móvil/tests no hay sesión y se usa el token Bearer.
        // "Recordarme" usa el remember_token (sesión persistente).
        if ($request->hasSession()) {
            auth('web')->login($user, $request->boolean('recordarme'));
            $request->session()->regenerate();
        }

        $token = $user->createToken('proyectwin')->plainTextToken;

        return response()->json([
            'user' => $user,
            'token' => $token,
            'rol' => $user->rol,
            'debe_cambiar_password' => (bool) $user->debe_cambiar_password,
        ]);
    }

    // Valida la identidad contra el padrón institucional (como el "Validar" de
    // SOFIA Plus). Respuesta genérica para no enumerar documentos.
    public function validarPadron(Request $request)
    {
        $request->validate([
            'tipo_documento' => 'required|in:CC,TI,CE,PA',
            'numero_documento' => 'required|string|max:11',
            'correo' => 'required|email',
        ]);

        $padron = PadronUsuario::where('tipo_documento', $request->tipo_documento)
            ->where('numero_documento', trim($request->numero_documento))
            ->whereRaw('LOWER(correo) = ?', [strtolower(trim($request->correo))])
            ->first();

        if (!$padron) {
            return response()->json([
                'message' => 'Tus datos no coinciden con la matrícula del SENA. Verifica el documento y el correo institucional.',
            ], 422);
        }
        if (!$padron->activo) {
            return response()->json(['message' => 'Tu registro no está activo en el padrón. Contacta al administrador.'], 422);
        }
        if ($padron->id_usuario) {
            return response()->json([
                'message' => 'Ese documento ya tiene una cuenta registrada. Inicia sesión o recupera tu contraseña.',
                'ya_registrado' => true,
            ], 422);
        }

        return response()->json([
            'coincide' => true,
            'nombre' => $padron->nombre,
            'apellido' => $padron->apellido,
            'rol' => $padron->rol,
            'tipo_documento' => $padron->tipo_documento,
            'numero_documento' => $padron->numero_documento,
            'correo' => $padron->correo,
        ]);
    }

    // Activa una cuenta pendiente con el código de un solo uso.
    public function activar(Request $request)
    {
        $request->validate([
            'correo' => 'required|email',
            'codigo' => 'required|digits:6',
        ]);

        $user = GeneralUser::whereRaw('LOWER(correo) = ?', [strtolower(trim($request->correo))])->first();
        if (!$user) {
            return response()->json(['message' => 'El código es inválido o venció. Solicita uno nuevo.'], 422);
        }

        if ($user->estado) {
            return response()->json(['message' => 'La cuenta ya está activa. Inicia sesión.']);
        }

        if (!CodigoActivacion::validar($user->id, $request->codigo)) {
            return response()->json(['message' => 'El código es inválido o venció. Solicita uno nuevo.'], 422);
        }

        $user->update(['estado' => true]);
        Auditoria::registrar('activar_cuenta', 'general_users', $user->id, ['correo' => $user->correo]);

        return response()->json(['message' => 'Cuenta activada. Ya puedes iniciar sesión.']);
    }

    // Acepta correo o número de documento como identificador de ingreso.
    private function buscarPorIdentificador(Request $request): ?GeneralUser
    {
        if ($request->filled('correo')) {
            return GeneralUser::whereRaw('LOWER(correo) = ?', [strtolower(trim($request->correo))])->first();
        }

        $identificador = trim((string) $request->identificador);
        if ($identificador === '') return null;

        if (str_contains($identificador, '@')) {
            return GeneralUser::whereRaw('LOWER(correo) = ?', [strtolower($identificador)])->first();
        }

        // Documento: se resuelve a través del padrón (única fuente de identidad).
        $padron = PadronUsuario::where('numero_documento', $identificador)
            ->whereNotNull('id_usuario')
            ->latest('id')
            ->first();

        return $padron ? GeneralUser::find($padron->id_usuario) : null;
    }

    public function logout(Request $request)
    {
        // Token Bearer (móvil) si lo hay. Con sesión (cookie), Sanctum devuelve
        // un TransientToken que no se puede borrar (no hay token que revocar).
        $token = $request->user()?->currentAccessToken();
        if ($token instanceof \Laravel\Sanctum\PersonalAccessToken) {
            $token->delete();
        }

        // Sesión por cookie (SPA): cierra sesión e invalida el remember cookie.
        if ($request->hasSession()) {
            auth('web')->logout();
            $request->session()->invalidate();
            $request->session()->regenerateToken();
        }

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
        if ($actual instanceof \Laravel\Sanctum\PersonalAccessToken) {
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
            'password' => 'required|min:8|max:255|confirmed',
        ]);

        $user = $request->user();

        if (!Hash::check($request->password_actual, $user->password)) {
            return response()->json(['message' => 'La contraseña actual no es correcta.'], 422);
        }

        // Al cambiar la clave se limpia la marca de "clave temporal".
        $user->update(['password' => Hash::make($request->password), 'debe_cambiar_password' => false]);

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
