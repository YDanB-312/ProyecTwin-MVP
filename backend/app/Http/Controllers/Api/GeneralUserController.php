<?php

namespace App\Http\Controllers\Api;

use App\Models\GeneralUser;
use App\Models\Admin;
use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\ClassGroup;
use App\Models\Instructor;
use App\Models\PadronUsuario;
use App\Models\Project;
use App\Rules\FotoUrl;
use App\Support\Auditoria;
use App\Support\BorradoCascada;
use App\Support\CodigoActivacion;
use App\Support\CodigoAprendiz;
use App\Support\Pagina;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

class GeneralUserController extends Controller
{
    public function index(Request $request)
    {
        $query = GeneralUser::included()
            ->search($request->query('search'))
            ->byRol($request->query('role'))
            ->byEstado($request->query('estado'))
            ->byFicha($request->query('ficha_id'))
            ->byPrograma($request->query('programa'));

        // Con `?page=` responde paginado; sin él, la lista completa.
        return Pagina::aplicar($query, $request, 15);
    }

    public function store(Request $request)
    {
        // Alta directa del administrador (clave temporal, sin padrón) o registro
        // público validado contra el padrón institucional.
        $yo = $request->user() ?? auth('sanctum')->user();

        return optional($yo)->rol === 'admin'
            ? $this->storeComoAdmin($request)
            : $this->storeDesdePadron($request);
    }

    // El admin sí puede crear cuentas de cualquier rol; nacen con clave temporal
    // (debe_cambiar_password) y sin pasar por el padrón.
    private function storeComoAdmin(Request $request)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'apellido' => 'required|max:255',
            'correo' => 'required|email|unique:general_users,correo',
            'password' => 'required|min:8|max:255',
            'foto_url' => ['nullable', new FotoUrl()],
            'rol' => 'required|in:aprendiz,instructor,admin',
            'estado' => 'nullable|boolean',
        ]);

        $data = $request->except(['tipo_documento', 'numero_documento']);
        $data['correo'] = strtolower(trim($request->correo));
        $data['password'] = Hash::make($request->password);
        $data['estado'] = $request->has('estado') ? $request->boolean('estado') : true;
        $data['debe_cambiar_password'] = true;

        // Alta atómica: cuenta + perfil + auditoría (todo o nada).
        $item = DB::transaction(function () use ($data) {
            $item = GeneralUser::create($data);
            $this->sincronizarPerfiles($item);

            Auditoria::registrar('crear_usuario', 'general_users', $item->id, [
                'correo' => $item->correo,
                'rol' => $item->rol,
                'origen' => 'admin',
            ]);

            return $item;
        });

        return response()->json($item, 201);
    }

    // Registro público: la persona debe existir en el padrón; el rol y la ficha
    // se copian del padrón (nunca del request) y la cuenta nace sin activar.
    private function storeDesdePadron(Request $request)
    {
        $request->validate([
            'tipo_documento' => 'required|in:CC,TI,CE,PA',
            'numero_documento' => 'required|string|max:11',
            'correo' => 'required|email|unique:general_users,correo',
            'password' => 'required|min:8|max:255|confirmed',
        ]);

        $correo = strtolower(trim($request->correo));

        $padron = PadronUsuario::where('tipo_documento', $request->tipo_documento)
            ->where('numero_documento', trim($request->numero_documento))
            ->whereRaw('LOWER(correo) = ?', [$correo])
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
            ], 422);
        }

        // Alta atómica: cuenta + vínculo al padrón + perfil + código (todo o nada).
        [$item, $codigo] = DB::transaction(function () use ($request, $padron, $correo) {
            $item = GeneralUser::create([
                'nombre' => $padron->nombre,
                'apellido' => $padron->apellido,
                'correo' => $correo,
                'password' => Hash::make($request->password),
                'rol' => $padron->rol,
                'estado' => false,
                'debe_cambiar_password' => false,
            ]);

            $padron->update(['id_usuario' => $item->id]);
            $this->sincronizarPerfiles($item, $padron);

            $codigo = CodigoActivacion::generar($item->id);

            Auditoria::registrar('crear_usuario', 'general_users', $item->id, [
                'correo' => $item->correo,
                'rol' => $item->rol,
                'origen' => 'padron',
            ]);

            return [$item, $codigo];
        });

        $respuesta = [
            'user' => $item,
            'activacion_requerida' => true,
            'correo' => $item->correo,
            'message' => 'Cuenta creada. Actívala con el código de verificación.',
        ];

        // En local/demo el código se muestra en pantalla; en producción viaja por correo.
        if (config('identidad.activacion.exponer_codigo')) {
            $respuesta['codigo'] = $codigo;
        }

        return response()->json($respuesta, 201);
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
    // datos de contacto básicos; el correo solo si hay vínculo real (ficha).
    public function perfil(Request $request, $id)
    {
        $user = GeneralUser::findOrFail($id);
        $yo = $request->user();

        $puedeVerCorreo = (int) optional($yo)->id === (int) $user->id
            || optional($yo)->rol === 'admin'
            || $this->comparteFicha($yo, $user);

        return response()->json([
            'id' => $user->id,
            'nombre' => $user->nombre,
            'apellido' => $user->apellido,
            'correo' => $puedeVerCorreo ? $user->correo : null,
            'foto_url' => $user->foto_url,
            'rol' => $user->rol,
        ]);
    }

    // ¿El solicitante tiene vínculo de ficha con la persona consultada?
    private function comparteFicha(?GeneralUser $yo, GeneralUser $objetivo): bool
    {
        if (!$yo) return false;

        $fichaYo = Apprentice::where('id_usuario', $yo->id)->value('id_class_group');
        $fichaObjetivo = Apprentice::where('id_usuario', $objetivo->id)->value('id_class_group');

        // Compañeros de la misma ficha.
        if ($fichaYo && $fichaObjetivo && (int) $fichaYo === (int) $fichaObjetivo) {
            return true;
        }

        // Aprendiz → instructor de su ficha.
        $instructorObjetivo = Instructor::where('id_usuario', $objetivo->id)->value('id');
        if ($fichaYo && $instructorObjetivo
            && ClassGroup::where('id', $fichaYo)->where('id_instructor', $instructorObjetivo)->exists()) {
            return true;
        }

        // Instructor → aprendiz de sus fichas.
        $instructorYo = Instructor::where('id_usuario', $yo->id)->value('id');
        if ($instructorYo && $fichaObjetivo
            && ClassGroup::where('id', $fichaObjetivo)->where('id_instructor', $instructorYo)->exists()) {
            return true;
        }

        return false;
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
            'foto_url' => ['nullable', new FotoUrl()],
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

        // Actualización atómica: cuenta + perfiles + auditoría (todo o nada).
        DB::transaction(function () use ($general_user, $data, $cambiaRol, $cambiaEstado, $huboPassword) {
            $general_user->update($data);

            // Suspender una cuenta cierra todas sus sesiones/tokens de inmediato.
            if ($cambiaEstado && !$general_user->estado) {
                $general_user->tokens()->delete();
            }

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

    // ---------------------------------------------------------------- Perfiles

    // Crea el perfil que corresponde al rol. El aprendiz matriculado nace en la
    // ficha de su padrón (si la tiene); sin padrón, su fila se crea al unirse.
    private function sincronizarPerfiles(GeneralUser $usuario, ?PadronUsuario $padron = null): void
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
        if ($usuario->rol === 'aprendiz' && $padron && $padron->id_class_group) {
            Apprentice::firstOrCreate(
                ['id_usuario' => $usuario->id],
                [
                    'codigo' => CodigoAprendiz::disponible(),
                    'id_class_group' => $padron->id_class_group,
                    'id_programa' => $padron->id_programa
                        ?? optional(ClassGroup::find($padron->id_class_group))->id_programa,
                ]
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
