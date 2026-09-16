<?php

namespace App\Http\Controllers\Api;

use App\Models\GeneralUser;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class GeneralUserController extends Controller
{
    public function index(Request $request)
    {
        return GeneralUser::included()
            ->search($request->query('search'))
            ->byRol($request->query('role'))
            ->byEstado($request->query('estado'))
            ->byTrainingCenter($request->query('training_center_id'))
            ->byFicha($request->query('ficha_id'))
            ->byPrograma($request->query('programa'))
            ->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'apellido' => 'required|max:255',
            'correo' => 'required|email|unique:general_users,correo',
            'password' => 'required|min:6|max:255',
            'foto_url' => 'nullable',
            'rol' => 'required|in:aprendiz,instructor,admin',
            'estado' => 'nullable|boolean',
        ]);

        // Registro público solo aprendiz/instructor; crear admin exige token admin.
        $esAdmin = optional($request->user())->rol === 'admin';
        if ($request->rol === 'admin' && !$esAdmin) {
            return response()->json(['message' => 'Solo un administrador puede crear cuentas admin.'], 403);
        }

        $data = $request->all();
        $data['password'] = Hash::make($request->password);
        $data['estado'] = $request->has('estado') ? $request->boolean('estado') : true;
        $item = GeneralUser::create($data);
        return response()->json($item, 201);
    }

    public function show($id)
    {
        return GeneralUser::included()->findOrFail($id);
    }

    public function update(Request $request, GeneralUser $general_user)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'apellido' => 'required|max:255',
            'correo' => 'required|email|unique:general_users,correo,' . $general_user->id,
            'password' => 'nullable|min:6|max:255',
            'foto_url' => 'nullable',
            'rol' => 'required|in:aprendiz,instructor,admin',
            'estado' => 'nullable|boolean',
        ]);

        // Cambios de rol o estado exigen admin; nadie se auto-eleva.
        $yo = $request->user();
        $cambiaRol = $request->has('rol') && $request->rol !== $general_user->rol;
        $cambiaEstado = $request->has('estado') && (bool) $request->estado !== (bool) $general_user->estado;
        if (($cambiaRol || $cambiaEstado) && optional($yo)->rol !== 'admin') {
            return response()->json(['message' => 'Solo un administrador puede cambiar rol o estado.'], 403);
        }
        // Nadie puede quitarse a sí mismo el rol de admin.
        if ($cambiaRol && $yo && (int) $yo->id === (int) $general_user->id && $general_user->rol === 'admin') {
            return response()->json(['message' => 'No puedes quitarte tu propio rol de administrador.'], 422);
        }

        $data = $request->all();
        // Solo re-hashear si viene clave nueva no vacía.
        if (empty($data['password'])) {
            unset($data['password']);
        } else {
            $data['password'] = Hash::make($data['password']);
        }
        $general_user->update($data);
        return $general_user;
    }

    public function destroy(GeneralUser $general_user)
    {
        $general_user->delete();
        return $general_user;
    }
}
