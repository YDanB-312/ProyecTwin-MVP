<?php

namespace App\Http\Controllers\Api;

use App\Models\GeneralUser;
use App\Models\Admin;
use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\ClassGroup;
use App\Models\Instructor;
use App\Models\Project;
use App\Support\Auditoria;
use App\Support\BorradoCascada;
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

        // Registro público: solo aprendiz/instructor. Crear cuentas admin exige
        // token de administrador (la ruta es pública: se resuelve explícito).
        $yo = $request->user() ?? auth('sanctum')->user();
        if ($request->rol === 'admin' && optional($yo)->rol !== 'admin') {
            return response()->json(['message' => 'Solo un administrador puede crear cuentas admin.'], 403);
        }

        $data = $request->all();
        $data['password'] = Hash::make($request->password);
        $data['estado'] = $request->has('estado') ? $request->boolean('estado') : true;
        $item = GeneralUser::create($data);

        // El perfil (instructor/admin) nace junto con la cuenta para que el
        // usuario pueda operar de inmediato (estilo "docente crea la clase").
        $this->sincronizarPerfiles($item);

        Auditoria::registrar('crear_usuario', 'general_users', $item->id, [
            'correo' => $item->correo,
            'rol' => $item->rol,
        ]);

        return response()->json($item, 201);
    }

    public function show(Request $request, $id)
    {
        $user = $request->user();

        // El detalle completo es del propio usuario o de un admin.
        if ((int) $user->id !== (int) $id && $user->rol !== 'admin') {
            return response()->json(['message' => 'Solo puedes ver tu propia cuenta.'], 403);
        }

        return GeneralUser::included()->findOrFail($id);
    }

    // Perfil público (vistas entre usuarios: compañero, instructor). Expone solo
    // datos de contacto básicos; nunca estado ni información de gestión.
    public function perfil($id)
    {
        $user = GeneralUser::findOrFail($id);

        return response()->json([
            'id' => $user->id,
            'nombre' => $user->nombre,
            'apellido' => $user->apellido,
            'correo' => $user->correo,
            'foto_url' => $user->foto_url,
            'rol' => $user->rol,
        ]);
    }

    public function update(Request $request, GeneralUser $general_user)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'apellido' => 'required|max:255',
            'correo' => 'required|email|unique:general_users,correo,' . $general_user->id,
            'password' => 'nullable|min:6|max:255',
            'foto_url' => 'nullable',
            'rol' => 'sometimes|required|in:aprendiz,instructor,admin',
            'estado' => 'sometimes|nullable|boolean',
        ]);

        // Autorización: la propia cuenta, o cualquier cuenta si eres admin.
        $yo = $request->user();
        $esPropiaCuenta = $yo && (int) $yo->id === (int) $general_user->id;
        $esAdmin = optional($yo)->rol === 'admin';
        if (!$esPropiaCuenta && !$esAdmin) {
            return response()->json(['message' => 'No puedes modificar los datos de otro usuario.'], 403);
        }

        // Cambios de rol o estado exigen admin; nadie se auto-eleva.
        $cambiaRol = $request->has('rol') && $request->rol !== $general_user->rol;
        $cambiaEstado = $request->has('estado') && (bool) $request->estado !== (bool) $general_user->estado;
        if (($cambiaRol || $cambiaEstado) && !$esAdmin) {
            return response()->json(['message' => 'Solo un administrador puede cambiar rol o estado.'], 403);
        }
        // El propio correo (identificador de acceso) solo se cambia con la
        // contraseña actual: PUT /auth/email. Un admin sí puede corregir el
        // correo de OTRO usuario desde su detalle.
        $cambiaCorreo = $request->has('correo') && $request->correo !== $general_user->correo;
        if ($cambiaCorreo && $esPropiaCuenta) {
            return response()->json([
                'message' => 'Usa "Cambiar correo" e ingresa tu contraseña actual para modificar tu correo.',
            ], 422);
        }
        // Nadie puede quitarse a sí mismo el rol de admin.
        if ($cambiaRol && $yo && (int) $yo->id === (int) $general_user->id && $general_user->rol === 'admin') {
            return response()->json(['message' => 'No puedes quitarte tu propio rol de administrador.'], 422);
        }
        // Tampoco se puede suspender/degradar al último administrador activo.
        $dejaDeSerAdminActivo = $general_user->rol === 'admin' && $general_user->estado
            && (($cambiaRol && $request->rol !== 'admin') || ($cambiaEstado && !$request->boolean('estado')));
        if ($dejaDeSerAdminActivo) {
            $activos = GeneralUser::where('rol', 'admin')->where('estado', true)->count();
            if ($activos <= 1) {
                return response()->json([
                    'message' => 'No puedes dejar el sistema sin administradores activos.',
                ], 409);
            }
        }

        // Un instructor con fichas a cargo no puede dejar de ser instructor: las
        // fichas quedarían sin responsable. Se exige reasignarlas primero.
        if ($cambiaRol && $general_user->rol === 'instructor' && $request->rol !== 'instructor') {
            $instructor = Instructor::where('id_usuario', $general_user->id)->first();
            $fichas = $instructor ? ClassGroup::where('id_instructor', $instructor->id)->count() : 0;
            if ($fichas > 0) {
                return response()->json([
                    'message' => "No se puede cambiar el rol: tiene {$fichas} ficha(s) a cargo. Reasígnalas primero.",
                ], 409);
            }
        }

        $data = $request->all();
        $huboPassword = !empty($data['password']);
        // Solo re-hashear si viene clave nueva no vacía.
        if (empty($data['password'])) {
            unset($data['password']);
        } else {
            $data['password'] = Hash::make($data['password']);
        }
        $general_user->update($data);

        Auditoria::registrar('actualizar_usuario', 'general_users', $general_user->id, array_filter([
            'rol' => $cambiaRol ? $general_user->rol : null,
            'estado' => $cambiaEstado ? (bool) $general_user->estado : null,
            'password_reset' => $huboPassword ?: null,
        ]));

        // Mantiene coherentes los perfiles (admin/instructor/aprendiz) al cambio de rol.
        if ($cambiaRol) {
            $this->sincronizarPerfiles($general_user);
            $this->limpiarPerfilesObsoletos($general_user);
        }

        return $general_user;
    }

    public function destroy(Request $request, GeneralUser $general_user)
    {
        $yo = $request->user();

        // Autoprotección: nadie borra su propia cuenta.
        if ($yo && (int) $yo->id === (int) $general_user->id) {
            return response()->json(['message' => 'No puedes eliminar tu propia cuenta.'], 422);
        }

        // No se puede dejar el sistema sin administradores activos.
        if ($general_user->rol === 'admin' && $general_user->estado) {
            $activos = GeneralUser::where('rol', 'admin')->where('estado', true)->count();
            if ($activos <= 1) {
                return response()->json([
                    'message' => 'No puedes eliminar al último administrador activo.',
                ], 409);
            }
        }

        // Admin sin restricciones: se borra el usuario y todo lo dependiente en
        // cascada (el impacto se advierte en la confirmación del cliente).
        $instructor = $general_user->instructor;
        $impacto = [
            'propuestas' => Project::where('id_creador', $general_user->id)->count(),
            'fichas_a_cargo' => $instructor ? ClassGroup::where('id_instructor', $instructor->id)->count() : 0,
        ];

        BorradoCascada::usuario($general_user);

        Auditoria::registrar('eliminar_usuario', 'general_users', $general_user->id, [
            'correo' => $general_user->correo,
            'rol' => $general_user->rol,
            'impacto' => $impacto,
        ]);

        return $general_user;
    }

    // ---------------------------------------------------------------- Perfiles

    // Crea el perfil que corresponde al rol. El de aprendiz no se crea aquí
    // porque `apprentices.id_programa` es obligatorio: nace al unirse a una ficha.
    private function sincronizarPerfiles(GeneralUser $usuario): void
    {
        if ($usuario->rol === 'instructor') {
            Instructor::firstOrCreate(
                ['id_usuario' => $usuario->id],
                ['fecha_ingreso' => now()->toDateString()]
            );
        }
        if ($usuario->rol === 'admin') {
            Admin::firstOrCreate(['id_usuario' => $usuario->id]);
        }
    }

    // Elimina los perfiles que ya no corresponden al rol, salvo que tengan
    // historial asociado (fichas a cargo, propuestas o equipo).
    private function limpiarPerfilesObsoletos(GeneralUser $usuario): void
    {
        if ($usuario->rol !== 'admin') {
            Admin::where('id_usuario', $usuario->id)->delete();
        }

        if ($usuario->rol !== 'instructor') {
            $instructor = Instructor::where('id_usuario', $usuario->id)->first();
            if ($instructor && !ClassGroup::where('id_instructor', $instructor->id)->exists()) {
                $instructor->delete();
            }
        }

        if ($usuario->rol !== 'aprendiz') {
            $aprendiz = Apprentice::where('id_usuario', $usuario->id)->first();
            if ($aprendiz) {
                // Deja de ser aprendiz: sale del roster de su ficha (la fila se
                // conserva para el historial de sus propuestas). Sin ficha no hay
                // programa vigente.
                $aprendiz->update(['id_class_group' => null, 'id_programa' => null]);
                $enEquipo = ApprenticeProject::where('id_aprendiz', $aprendiz->id)->exists();
                $esAutor = Project::where('id_creador', $usuario->id)->exists();
                if (!$enEquipo && !$esAutor) {
                    $aprendiz->delete();
                }
            }
        }
    }
}
