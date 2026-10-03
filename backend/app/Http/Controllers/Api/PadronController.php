<?php

namespace App\Http\Controllers\Api;

use App\Models\ActivacionCuenta;
use App\Models\GeneralUser;
use App\Models\PadronUsuario;
use App\Support\Auditoria;
use App\Support\CodigoActivacion;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

// Administración del padrón institucional (solo admin) y de los códigos de
// activación de cuentas pendientes.
class PadronController extends Controller
{
    public function index(Request $request)
    {
        return PadronUsuario::with(['programa', 'classGroup', 'user:id,correo,estado'])
            ->search($request->query('search'))
            ->byRol($request->query('role'))
            ->byFicha($request->query('ficha_id'))
            ->orderBy('apellido')
            ->get();
    }

    public function store(Request $request)
    {
        $datos = $request->validate([
            'tipo_documento' => 'required|in:CC,TI,CE,PA',
            'numero_documento' => [
                'required', 'string', 'max:11',
                Rule::unique('padron_usuarios')->where('tipo_documento', $request->tipo_documento),
            ],
            'nombre' => 'required|max:255',
            'apellido' => 'required|max:255',
            'correo' => 'required|email|unique:padron_usuarios,correo',
            'rol' => 'required|in:aprendiz,instructor,admin',
            'id_programa' => 'nullable|exists:training_programs,id',
            'id_class_group' => 'nullable|exists:class_groups,id',
        ]);

        $datos['correo'] = strtolower(trim($datos['correo']));

        if (!$this->dominioValido($datos['rol'], $datos['correo'])) {
            return response()->json([
                'message' => 'El correo debe ser institucional del SENA para el rol ' . $datos['rol'] . '.',
            ], 422);
        }

        $item = PadronUsuario::create($datos);

        Auditoria::registrar('crear_padron', 'padron_usuarios', $item->id, [
            'correo' => $item->correo,
            'rol' => $item->rol,
        ]);

        return response()->json($item->load(['programa', 'classGroup']), 201);
    }

    public function destroy(PadronUsuario $padron_usuario)
    {
        if ($padron_usuario->id_usuario) {
            return response()->json([
                'message' => 'Ese registro ya tiene una cuenta: no se puede retirar del padrón.',
            ], 422);
        }

        $padron_usuario->delete();
        Auditoria::registrar('eliminar_padron', 'padron_usuarios', $padron_usuario->id, [
            'correo' => $padron_usuario->correo,
        ]);

        return $padron_usuario;
    }

    // Cuentas registradas pendientes de activación (con código vigente).
    public function codigos()
    {
        return ActivacionCuenta::with('user')
            ->whereNull('usado_en')
            ->where('expira_en', '>', now())
            ->whereHas('user', fn ($q) => $q->where('estado', false))
            ->orderByDesc('id')
            ->get()
            ->map(fn ($a) => [
                'id' => $a->id_usuario,
                'nombre' => optional($a->user)->nombre,
                'apellido' => optional($a->user)->apellido,
                'correo' => optional($a->user)->correo,
                'rol' => optional($a->user)->rol,
                'expira_en' => $a->expira_en,
            ]);
    }

    // Regenera el código de una cuenta pendiente y lo entrega al admin.
    public function regenerarCodigo(GeneralUser $general_user)
    {
        if ($general_user->estado) {
            return response()->json(['message' => 'La cuenta ya está activa.'], 422);
        }

        $codigo = CodigoActivacion::generar($general_user->id);

        Auditoria::registrar('regenerar_activacion', 'general_users', $general_user->id, [
            'correo' => $general_user->correo,
        ]);

        return response()->json([
            'correo' => $general_user->correo,
            'codigo' => $codigo,
            'expira_en' => optional(CodigoActivacion::vigente($general_user->id))->expira_en,
        ]);
    }

    private function dominioValido(string $rol, string $correo): bool
    {
        $dominios = config("identidad.dominios.{$rol}", []);

        return collect($dominios)->contains(
            fn ($d) => str_ends_with($correo, '@' . strtolower($d))
        );
    }
}
