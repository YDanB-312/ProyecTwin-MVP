<?php

namespace App\Http\Controllers\Api;

use App\Models\GeneralUser;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

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

    // Restablecimiento público (flujo "olvidé mi contraseña"): sin token.
    // Si el correo no existe se responde 422 para que la UI lo informe.
    public function passwordReset(Request $request)
    {
        $request->validate([
            'correo' => 'required|email',
            'password' => 'required|min:6|max:255',
        ]);

        $user = GeneralUser::where('correo', $request->correo)->first();
        if (!$user) {
            return response()->json(['message' => 'No encontramos una cuenta registrada con ese correo.'], 422);
        }
        $user->update(['password' => Hash::make($request->password)]);
        return response()->json(['message' => 'Contraseña actualizada.']);
    }
}
