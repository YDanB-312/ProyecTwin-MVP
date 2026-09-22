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
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class GeneralUserController extends Controller
{
    public function index(Request $request)
    {
        $query = GeneralUser::included()
            ->search($request->query('search'))
            ->byRol($request->query('role'))
            ->byEstado($request->query('estado'))
            ->byTrainingCenter($request->query('training_center_id'))
            ->byFicha($request->query('ficha_id'))
            ->byPrograma($request->query('programa'));

        // El admin de centro solo lista usuarios de su centro (aprendices de
        // sus fichas, instructores con ficha allí y su propio perfil admin).
        $yo = $request->user();
        if ($yo && $yo->esAdminDeCentro()) {
            $centroId = $yo->centroId();
            $query->where(function ($w) use ($centroId) {
                $w->whereHas('apprentice.classGroup', fn ($x) => $x->where('training_center_id', $centroId))
                  ->orWhereHas('instructor.classGroups', fn ($x) => $x->where('training_center_id', $centroId))
                  ->orWhereHas('admin', fn ($x) => $x->where('training_center_id', $centroId));
            });
        }

        return $query->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'apellido' => 'required|max:255',
            'correo' => 'required|email|unique:general_users,correo',
            'password' => 'required|min:6|max:255',
            'foto_url' => 'nullable',
            'rol' => 'required|in:aprendiz,instructor,admin,superadmin',
            'estado' => 'nullable|boolean',
            'training_center_id' => 'nullable|exists:training_centers,id',
        ]);

        // Las cuentas de gobierno (admin/superadmin) solo las crea un superadmin.
        // La ruta es pública: el token se resuelve aquí de forma explícita.
        $yo = $request->user() ?? auth('sanctum')->user();
        if (in_array($request->rol, ['admin', 'superadmin'], true) && !optional($yo)->esSuperadmin()) {
            return response()->json(['message' => 'Solo un superadministrador puede crear cuentas de administración.'], 403);
        }

        // Un centro solo admite un administrador.
        $centroId = $request->input('training_center_id');
        if ($request->rol === 'admin' && $centroId && Admin::where('training_center_id', $centroId)->exists()) {
            return response()->json(['message' => 'Ese centro ya tiene un administrador asignado.'], 422);
        }

        $data = $request->all();
        $data['password'] = Hash::make($request->password);
        $data['estado'] = $request->has('estado') ? $request->boolean('estado') : true;
        unset($data['training_center_id']); // no es columna de general_users
        $item = GeneralUser::create($data);

        // El perfil (instructor/admin) nace junto con la cuenta para que el
        // usuario pueda operar de inmediato (estilo "docente crea la clase").
        $this->sincronizarPerfiles($item, $centroId ? (int) $centroId : null);

        Auditoria::registrar('crear_usuario', 'general_users', $item->id, [
            'correo' => $item->correo,
            'rol' => $item->rol,
        ]);

        return response()->json($item, 201);
    }

    public function show(Request $request, $id)
    {
        $yo = $request->user();
        $objetivo = GeneralUser::included()->findOrFail($id);

        // El detalle completo es del propio usuario, de un superadmin, o de un
        // admin de centro sobre usuarios de su centro.
        $esPropia = $yo && (int) $yo->id === (int) $objetivo->id;
        $esSuperadmin = $yo && $yo->esSuperadmin();
        $esAdminCentro = $yo && $yo->esAdminDeCentro() && $objetivo->perteneceAlCentro($yo->centroId());
        if (!$esPropia && !$esSuperadmin && !$esAdminCentro) {
            return response()->json(['message' => 'Solo puedes ver tu propia cuenta.'], 403);
        }

        return $objetivo;
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

        // Autorización: la propia cuenta, o cualquier cuenta si eres admin. El
        // admin de centro solo gestiona usuarios adscritos a su centro.
        $yo = $request->user();
        $esPropiaCuenta = $yo && (int) $yo->id === (int) $general_user->id;
        $esSuperadmin = optional($yo)->esSuperadmin();
        $esAdminDeCentro = optional($yo)->esAdminDeCentro() && $general_user->perteneceAlCentro(optional($yo)->centroId());
        if (!$esPropiaCuenta && !$esSuperadmin && !$esAdminDeCentro) {
            return response()->json(['message' => 'No puedes modificar los datos de otro usuario.'], 403);
        }

        // Cambios de rol o estado exigen admin; nadie se auto-eleva.
        $cambiaRol = $request->has('rol') && $request->rol !== $general_user->rol;
        $cambiaEstado = $request->has('estado') && (bool) $request->estado !== (bool) $general_user->estado;
        if (($cambiaRol || $cambiaEstado) && !$esSuperadmin && !optional($yo)->esAdminDeCentro()) {
            return response()->json(['message' => 'Solo un administrador puede cambiar rol o estado.'], 403);
        }

        // Conceder o retirar roles de gobierno es exclusivo del superadmin
        // (salvo sobre la propia cuenta, que cae en las auto-protecciones).
        $rolObjetivoEsGobierno = !$esPropiaCuenta && (
            in_array($general_user->rol, ['admin', 'superadmin'], true)
            || ($cambiaRol && in_array($request->rol, ['admin', 'superadmin'], true))
        );
        if ($rolObjetivoEsGobierno && !$esSuperadmin) {
            return response()->json(['message' => 'Solo un superadministrador puede administrar cuentas de gobierno.'], 403);
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
        // Nadie puede quitarse a sí mismo el rol de gobierno.
        if ($cambiaRol && $yo && (int) $yo->id === (int) $general_user->id
            && in_array($general_user->rol, ['admin', 'superadmin'], true)) {
            return response()->json(['message' => 'No puedes quitarte tu propio rol de administrador.'], 422);
        }
        // Tampoco se puede suspender/degradar al último administrador activo.
        $dejaDeSerAdminActivo = in_array($general_user->rol, ['admin', 'superadmin'], true) && $general_user->estado
            && (($cambiaRol && !in_array($request->rol, ['admin', 'superadmin'], true))
                || ($cambiaEstado && !$request->boolean('estado')));
        if ($dejaDeSerAdminActivo) {
            $activos = GeneralUser::whereIn('rol', ['admin', 'superadmin'])->where('estado', true)->count();
            if ($activos <= 1) {
                return response()->json([
                    'message' => 'No puedes dejar el sistema sin administradores activos.',
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

        // El admin de centro solo borra usuarios de su centro; las cuentas de
        // gobierno (admin/superadmin) solo las gestiona un superadmin.
        if (!optional($yo)->esSuperadmin()) {
            $enSuCentro = optional($yo)->esAdminDeCentro() && $general_user->perteneceAlCentro(optional($yo)->centroId());
            if (!$enSuCentro) {
                return response()->json(['message' => 'No puedes eliminar usuarios de otro centro.'], 403);
            }
            if (in_array($general_user->rol, ['admin', 'superadmin'], true)) {
                return response()->json(['message' => 'Solo un superadministrador puede eliminar cuentas de administración.'], 403);
            }
        }

        // No se puede dejar el sistema sin administradores activos.
        if (in_array($general_user->rol, ['admin', 'superadmin'], true) && $general_user->estado) {
            $activos = GeneralUser::whereIn('rol', ['admin', 'superadmin'])->where('estado', true)->count();
            if ($activos <= 1) {
                return response()->json([
                    'message' => 'No puedes eliminar al último administrador activo.',
                ], 409);
            }
        }

        $instructor = $general_user->instructor;

        // Un instructor con fichas a cargo no se puede borrar: la FK lo impide
        // (antes reventaba con 500). Se responde 409 con un motivo claro.
        if ($instructor) {
            $fichas = ClassGroup::where('id_instructor', $instructor->id)->count();
            if ($fichas > 0) {
                return response()->json([
                    'message' => "No se puede eliminar: tiene {$fichas} ficha(s) a cargo. Reasígnalas primero.",
                ], 409);
            }
        }

        // Nadie con historial académico se borra: se suspende la cuenta. Borrar
        // arrastraría sus propuestas (y con ellas similitudes y equipos).
        $creadas = Project::where('id_creador', $general_user->id)->count();
        if ($creadas > 0) {
            return response()->json([
                'message' => "No se puede eliminar: es autor de {$creadas} propuesta(s). Suspende la cuenta para conservar el historial.",
            ], 409);
        }

        if ($instructor) {
            $asignadas = Project::where('id_instructor_asignado', $instructor->id)->count();
            if ($asignadas > 0) {
                return response()->json([
                    'message' => "No se puede eliminar: tiene {$asignadas} propuesta(s) asignada(s). Reasígnalas primero.",
                ], 409);
            }
        }

        $aprendiz = $general_user->apprentice;
        if ($aprendiz) {
            $enEquipo = ApprenticeProject::where('id_aprendiz', $aprendiz->id)->count();
            if ($enEquipo > 0) {
                return response()->json([
                    'message' => "No se puede eliminar: participa en {$enEquipo} propuesta(s). Suspende la cuenta para conservar el historial.",
                ], 409);
            }
        }

        try {
            $general_user->delete();
        } catch (\Illuminate\Database\QueryException $e) {
            return response()->json([
                'message' => 'No se puede eliminar: el usuario tiene registros asociados.',
            ], 409);
        }

        Auditoria::registrar('eliminar_usuario', 'general_users', $general_user->id, [
            'correo' => $general_user->correo,
            'rol' => $general_user->rol,
        ]);

        return $general_user;
    }

    // ---------------------------------------------------------------- Perfiles

    // Crea el perfil que corresponde al rol. El de aprendiz no se crea aquí
    // porque `apprentices.id_programa` es obligatorio: nace al unirse a una ficha.
    private function sincronizarPerfiles(GeneralUser $usuario, ?int $centroId = null): void
    {
        if ($usuario->rol === 'instructor') {
            Instructor::firstOrCreate(
                ['id_usuario' => $usuario->id],
                ['fecha_ingreso' => now()->toDateString()]
            );
        }
        if ($usuario->rol === 'admin') {
            // El perfil nace con el centro indicado (si lo hay). El superadmin
            // NO tiene perfil de admin: su alcance es global.
            Admin::firstOrCreate(
                ['id_usuario' => $usuario->id],
                ['training_center_id' => $centroId]
            );
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
                $enEquipo = ApprenticeProject::where('id_aprendiz', $aprendiz->id)->exists();
                $esAutor = Project::where('id_creador', $usuario->id)->exists();
                if (!$enEquipo && !$esAutor) {
                    $aprendiz->delete();
                }
            }
        }
    }
}
