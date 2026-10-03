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
use App\Notifications\VerificacionResuelta;
use App\Services\NotificacionesService;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
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
        // Registro público: solo aprendiz/instructor. Crear cuentas admin exige
        // token de administrador (la ruta es pública: se resuelve explícito).
        $yo = $request->user() ?? auth('sanctum')->user();
        if ($request->rol === 'admin' && optional($yo)->rol !== 'admin') {
            return response()->json(['message' => 'Solo un administrador puede crear cuentas admin.'], 403);
        }
        // El alta hecha por un admin ya nace verificada; el autorregistro queda
        // pendiente y sin acceso hasta que un admin revise el documento.
        $creadoPorAdmin = optional($yo)->rol === 'admin';

        $request->validate([
            'nombre' => 'required|max:255',
            'apellido' => 'required|max:255',
            'correo' => 'required|email|unique:general_users,correo',
            'password' => 'required|min:6|max:255',
            'foto_url' => 'nullable',
            'rol' => 'required|in:aprendiz,instructor,admin',
            'estado' => 'nullable|boolean',
            // El autorregistro adjunta el PDF que soporta el rol (aprendiz o
            // instructor); el alta del admin no lo exige.
            'soporte' => ($creadoPorAdmin ? 'nullable' : 'required') . '|file|mimes:pdf|max:10240',
        ]);

        $data = $request->all();
        unset($data['soporte']);
        $data['password'] = Hash::make($request->password);
        $data['estado'] = $request->has('estado') ? $request->boolean('estado') : true;
        $data['estado_verificacion'] = $creadoPorAdmin ? 'verificado' : 'pendiente';
        // El PDF se guarda en disco local privado; en la BD solo queda la ruta.
        $data['soporte_path'] = $request->hasFile('soporte')
            ? $request->file('soporte')->store('verificaciones')
            : null;

        // Alta atómica: cuenta + perfil + aviso + auditoría (todo o nada).
        $item = DB::transaction(function () use ($data, $creadoPorAdmin) {
            $item = GeneralUser::create($data);

            // El perfil (instructor/admin) nace junto con la cuenta.
            $this->sincronizarPerfiles($item);

            // Autoregistro → los admins deben revisar la solicitud.
            if (!$creadoPorAdmin && $item->rol !== 'admin') {
                app(NotificacionesService::class)->usuarioPendiente($item);
            }

            Auditoria::registrar('crear_usuario', 'general_users', $item->id, [
                'correo' => $item->correo,
                'rol' => $item->rol,
                'soporte' => (bool) $item->soporte_path,
            ]);

            return $item;
        });

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
        // Actualización parcial: los campos solo se validan si vienen. Así se
        // puede guardar SOLO la foto (foto_url) sin exigir el resto.
        $request->validate([
            'nombre' => 'sometimes|required|max:255',
            'apellido' => 'sometimes|required|max:255',
            'correo' => 'sometimes|required|email|unique:general_users,correo,' . $general_user->id,
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

        // Cambio de rol hecho por un admin: la cuenta queda verificada de una.
        if ($cambiaRol) {
            $data['estado_verificacion'] = 'verificado';
            $data['motivo_rechazo'] = null;
        }

        $cambiaNombre = $request->filled('nombre') && $request->nombre !== $general_user->nombre;

        // Actualización atómica: cuenta + perfiles + auditoría (todo o nada).
        DB::transaction(function () use ($general_user, $data, $cambiaRol, $cambiaEstado, $cambiaCorreo, $cambiaNombre, $huboPassword) {
            $general_user->update($data);

            Auditoria::registrar('actualizar_usuario', 'general_users', $general_user->id, array_filter([
                'rol' => $cambiaRol ? $general_user->rol : null,
                'estado' => $cambiaEstado ? (bool) $general_user->estado : null,
                'correo' => $cambiaCorreo ? $general_user->correo : null,
                'nombre' => $cambiaNombre ? $general_user->nombre : null,
                'password_reset' => $huboPassword ?: null,
            ]));

            // Mantiene coherentes los perfiles (admin/instructor/aprendiz) al cambio de rol.
            if ($cambiaRol) {
                $this->sincronizarPerfiles($general_user);
                $this->limpiarPerfilesObsoletos($general_user);
            }
        });

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

    // ---------------------------------------------------------------- Verificación

    // Resuelve la verificación de una cuenta autoregistrada (solo admin):
    // aprobar habilita el acceso; rechazar exige motivo. Deja rastro en la
    // bitácora y avisa por notificación interna y por correo.
    public function verificar(Request $request, GeneralUser $general_user)
    {
        if ($general_user->rol === 'admin') {
            return response()->json(['message' => 'Los administradores no requieren verificación.'], 422);
        }

        $request->validate([
            'accion' => 'required|in:verificar,rechazar',
            'motivo' => 'required_if:accion,rechazar|nullable|string|max:500',
        ]);

        $antes = $general_user->estado_verificacion;
        $aprobado = $request->accion === 'verificar';

        $general_user->update([
            'estado_verificacion' => $aprobado ? 'verificado' : 'rechazado',
            'motivo_rechazo' => $aprobado ? null : $request->motivo,
        ]);

        Auditoria::registrar('verificar_usuario', 'general_users', $general_user->id, [
            'de' => $antes,
            'a' => $general_user->estado_verificacion,
            'rol' => $general_user->rol,
            'motivo' => $aprobado ? null : $request->motivo,
            'soporte' => (bool) $general_user->soporte_path,
        ]);

        // Aviso interno (campanita) al usuario.
        app(NotificacionesService::class)->verificacionResuelta($general_user, $aprobado, $request->motivo);

        // Correo de resolución. Si el envío falla, la decisión ya quedó guardada.
        try {
            $general_user->notify(new VerificacionResuelta($aprobado, $request->motivo));
        } catch (\Throwable $e) {
            // Silencio deliberado: el correo no debe romper la verificación.
        }

        return $general_user->fresh();
    }

    // Sirve el PDF de soporte (solo admin). Vive en disco local privado: nunca
    // hay URL pública del documento.
    public function soporte(Request $request, GeneralUser $general_user)
    {
        if (optional($request->user())->rol !== 'admin') {
            return response()->json(['message' => 'Solo un administrador puede ver el documento.'], 403);
        }

        $disco = \Illuminate\Support\Facades\Storage::disk('local');
        if (!$general_user->soporte_path || !$disco->exists($general_user->soporte_path)) {
            return response()->json(['message' => 'Esta cuenta no tiene documento de soporte.'], 404);
        }

        return response()->file($disco->path($general_user->soporte_path));
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
