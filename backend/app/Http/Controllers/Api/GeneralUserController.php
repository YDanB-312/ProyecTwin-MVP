<?php

namespace App\Http\Controllers\Api;

use App\Models\GeneralUser;
use App\Models\AccountActivation;
use App\Models\Admin;
use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\Comment;
use App\Models\Instructor;
use App\Models\Project;
use App\Models\ProjectHistory;
use App\Models\Similarity;
use App\Support\Auditoria;
use App\Support\BorradoCascada;
use App\Services\ActivacionCuenta;
use App\Services\AltaUsuario;
use App\Services\NotificacionesService;
use App\Services\RecuperacionPassword;
use App\Notifications\CorreoCambiadoNotification;
use App\Support\Pii;
use App\Support\SesionesUsuario;
use App\Http\Controllers\Controller;
use Illuminate\Http\Exceptions\HttpResponseException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Notification;

class GeneralUserController extends Controller
{
    // Campos institucionales que solo revela la administración (el modelo los
    // oculta por defecto para el resto de la API).
    private const CAMPOS_ADMIN = [
        'tipo_documento', 'numero_documento', 'email_verified_at',
    ];

    // Proyección opt-in para selectores que solo necesitan identificar al
    // usuario (p. ej. el filtro de la bitácora). Evita enviar correo y la foto
    // en base64 de toda la plantilla. Los contratos actuales no cambian: solo
    // se activa con `resumen=1`.
    private const CAMPOS_RESUMEN = ['id', 'nombre', 'apellido', 'rol'];

    // La foto de perfil viaja como data URL de imagen generada por los
    // clientes (web y Android recortan a 256×256 JPEG). Se rechazan URLs
    // externas, MIME que no sea de imagen y payloads fuera de la columna TEXT
    // (65.535 bytes). El tope de 65.000 caracteres es seguro para la columna
    // (ASCII = 1 byte), cubre el peor caso local con ruido del JPEG 256×256
    // q0.82 (≈60.600 caracteres) y deja que Android mantenga su autocapa de
    // 60.000. Así ningún flujo actual queda rechazado por el límite.
    private const MAX_FOTO_URL = 65000;

    private const REGEX_FOTO_URL = '/\Adata:image\/(?:png|jpe?g|webp|gif);base64,[A-Za-z0-9+\/]+={0,2}\z/';

    // Reglas en arreglo (no en cadena) porque el regex contiene `|` y el
    // parser de reglas de Laravel dividiría la expresión por ese carácter.
    private const REGLAS_FOTO_URL = [
        'nullable',
        'string',
        'max:' . self::MAX_FOTO_URL,
        'regex:' . self::REGEX_FOTO_URL,
    ];

    public function index(Request $request)
    {
        // Params inválidos (p. ej. arrays) se rechazan con 422, nunca 500.
        $request->validate([
            'search' => 'nullable|string',
            'role' => 'nullable|string',
            'estado' => 'nullable|string',
            'ficha_id' => 'nullable|string',
            'programa' => 'nullable|string',
            'resumen' => 'nullable|boolean',
            'paginado' => 'nullable|boolean',
            'por_pagina' => 'bail|nullable|integer',
        ]);

        $query = GeneralUser::included()
            ->search($request->query('search'))
            ->byRol($request->query('role'))
            ->byEstado($request->query('estado'))
            ->byFicha($request->query('ficha_id'))
            ->byPrograma($request->query('programa'));

        // Proyección ligera opt-in (compatible: sin el parámetro todo sigue igual).
        $resumen = $request->boolean('resumen');

        // Paginación opt-in para el panel de administración (miles de filas).
        if ($request->boolean('paginado')) {
            $porPagina = max(1, min((int) $request->query('por_pagina', 10), 100));
            $query->orderBy('id');
            if ($resumen) {
                $query->select(self::CAMPOS_RESUMEN);
            }
            $pagina = $query->paginate($porPagina);
            if (!$resumen) {
                $pagina->getCollection()->each->makeVisible(self::CAMPOS_ADMIN);
            }
            return $pagina;
        }

        $query->orderBy('id');
        if ($resumen) {
            return $query->get(self::CAMPOS_RESUMEN);
        }

        return $query->get()->each->makeVisible(self::CAMPOS_ADMIN);
    }

    // Alta exclusiva del administrador (no hay registro público): la cuenta
    // nace pendiente y recibe un enlace de activación.
    public function store(Request $request)
    {
        // Normaliza antes de validar para que la unicidad del correo no dependa
        // de mayúsculas, espacios ni de la colación del motor.
        if (is_string($request->input('correo'))) {
            $request->merge([
                'correo' => strtolower(trim((string) $request->input('correo'))),
            ]);
        }

        $request->validate([
            'nombre' => 'required|string|max:255',
            'apellido' => 'required|string|max:255',
            'tipo_documento' => 'required|in:CC,TI,CE,PPT',
            'numero_documento' => 'bail|required|string|max:40|unique:general_users,numero_documento',
            'correo' => 'bail|required|string|email|unique:general_users,correo',
            'foto_url' => self::REGLAS_FOTO_URL,
            'rol' => 'required|in:aprendiz,instructor',
            'estado' => 'nullable|boolean',
        ]);

        // Alta compartida (cuenta + perfil + auditoría + correo de activación).
        // Whitelist: los campos internos de credenciales no se aceptan del cliente.
        $alta = app(AltaUsuario::class)->crear($request->only([
            'nombre', 'apellido', 'tipo_documento', 'numero_documento',
            'correo', 'foto_url', 'rol', 'estado',
        ]));

        $respuesta = [
            'usuario' => $alta['usuario']->makeVisible(self::CAMPOS_ADMIN),
            'activacion' => [
                'enviada' => (bool) $alta['enviadas'],
                'correo' => $alta['usuario']->correo,
                'mensaje' => $alta['enviadas']
                    ? 'Enviamos el enlace de activación al correo registrado.'
                    : 'La cuenta quedó pendiente, pero el correo no pudo enviarse. Reenvía la activación desde el detalle del usuario.',
            ],
        ];

        return response()->json($respuesta, 201);
    }

    // Reenvía la activación de una cuenta pendiente. Reemplaza el token
    // anterior, por lo que el enlace viejo deja de servir.
    public function reenviarActivacion(GeneralUser $general_user)
    {
        if ($general_user->email_verified_at) {
            return response()->json(['message' => 'La cuenta ya está activada.'], 422);
        }

        if (! $general_user->estado) {
            return response()->json(['message' => 'No se puede activar una cuenta suspendida.'], 422);
        }

        // Reenvío administrativo: cualquier token/sesión previo de la cuenta
        // pendiente deja de ser válido; el enlace nuevo reemplaza al anterior.
        SesionesUsuario::revocar($general_user);

        try {
            $resultado = app(ActivacionCuenta::class)->reenviar($general_user);
        } catch (\Throwable $e) {
            if ($e instanceof \Illuminate\Validation\ValidationException) {
                throw $e;
            }

            report($e);

            return response()->json([
                'message' => 'No se pudo generar el enlace de activación. Intenta de nuevo.',
            ], 502);
        }

        Auditoria::registrar('reenviar_activacion', 'general_users', $general_user->id, [
            'correo' => $general_user->correo,
            'enviado' => $resultado['enviado'],
        ]);

        if (! $resultado['enviado']) {
            return response()->json([
                'message' => 'El enlace se generó, pero el correo no pudo enviarse. Intenta reenviarlo.',
                'activacion' => ['enviada' => false],
            ], 502);
        }

        return response()->json([
            'message' => 'Enlace de activación reenviado al correo registrado.',
            'activacion' => ['enviada' => true],
        ]);
    }

    // Restablecimiento administrativo: revoca el acceso actual y envía un
    // enlace de recuperación (o reenvía la activación si la cuenta está
    // pendiente). Nunca genera ni devuelve contraseñas.
    public function restablecerCredenciales(GeneralUser $general_user)
    {
        if ($general_user->rol === 'admin') {
            return response()->json([
                'message' => 'Los administradores usan la recuperación de contraseña; no este restablecimiento.',
            ], 422);
        }

        if (! $general_user->email_verified_at) {
            return $this->reenviarActivacion($general_user);
        }

        SesionesUsuario::revocar($general_user);

        $resultado = app(RecuperacionPassword::class)->enviar($general_user);

        Auditoria::registrar('restablecer_clave_administrativa', 'general_users', $general_user->id, [
            'correo' => $general_user->correo,
            'enviado' => $resultado['enviado'],
        ]);

        if (! $resultado['enviado']) {
            return response()->json([
                'message' => 'Se cerraron las sesiones, pero el correo de recuperación no pudo enviarse. Intenta de nuevo.',
            ], 502);
        }

        return response()->json([
            'message' => 'Se cerraron las sesiones del usuario y se envió un enlace para restablecer su contraseña.',
        ]);
    }

    public function show(Request $request, $id)
    {
        $user = $request->user();

        // El detalle completo es del propio usuario o de un admin.
        if ((int) $user->id !== (int) $id && $user->rol !== 'admin') {
            return response()->json(['message' => 'Solo puedes ver tu propia cuenta.'], 403);
        }

        return GeneralUser::included()->findOrFail($id)->makeVisible(self::CAMPOS_ADMIN);
    }

    // Perfil público (vistas entre usuarios: compañero, instructor). Expone solo
    // datos de contacto básicos; nunca estado ni información de gestión. El
    // alcance es por relación académica (misma ficha / instructor de la ficha /
    // aprendiz de las fichas propias); el admin ve cualquiera.
    public function perfil(Request $request, $id)
    {
        $yo = $request->user();
        $user = GeneralUser::findOrFail($id);

        if (!$this->puedeVerPerfil($yo, $user)) {
            return response()->json(['message' => 'No tienes acceso a este perfil.'], 403);
        }

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
        // Autorización antes de validar: un usuario sin permiso no debe poder
        // usar errores de unicidad para enumerar correos o documentos.
        $yo = $request->user();
        $esPropiaCuenta = $yo && (int) $yo->id === (int) $general_user->id;
        $esAdmin = optional($yo)->rol === 'admin';

        if (! $esPropiaCuenta && ! $esAdmin) {
            return response()->json(['message' => 'No puedes modificar los datos de otro usuario.'], 403);
        }

        // El documento solo lo corrige un administrador. Se rechaza antes de
        // validar para que una petición manual reciba siempre 403 y no pueda
        // usar el error de unicidad como sondeo.
        $intentaDocumento = $request->has('tipo_documento') || $request->has('numero_documento');
        if ($intentaDocumento && ! $esAdmin) {
            return response()->json([
                'message' => 'Solo un administrador puede modificar el documento de identidad.',
            ], 403);
        }

        // Normaliza el correo antes de validar: la unicidad y la comparación
        // con el valor actual deben usar siempre la misma representación.
        if ($request->has('correo') && is_string($request->input('correo'))) {
            $request->merge(['correo' => strtolower(trim($request->input('correo')))]);
        }

        // Actualización parcial: los campos solo se validan si vienen. Así se
        // puede guardar SOLO la foto (foto_url) sin exigir el resto.
        $request->validate([
            'nombre' => 'sometimes|required|string|max:255',
            'apellido' => 'sometimes|required|string|max:255',
            'correo' => 'bail|sometimes|required|string|email|unique:general_users,correo,' . $general_user->id,
            'foto_url' => self::REGLAS_FOTO_URL,
            'rol' => 'sometimes|required|in:aprendiz,instructor,admin',
            'estado' => 'sometimes|boolean',
            // Documento de identidad: el admin puede corregirlo (p. ej. un
            // número mal digitado en el alta). El propio usuario no lo edita.
            'tipo_documento' => 'sometimes|required|in:CC,TI,CE,PPT',
            'numero_documento' => 'bail|sometimes|required|string|max:40|unique:general_users,numero_documento,' . $general_user->id,
        ]);

        // Cambios de rol o estado exigen admin; nadie se auto-eleva.
        $cambiaRol = $request->has('rol') && $request->rol !== $general_user->rol;
        $cambiaEstado = $request->has('estado') && (bool) $request->estado !== (bool) $general_user->estado;
        if (($cambiaRol || $cambiaEstado) && !$esAdmin) {
            return response()->json(['message' => 'Solo un administrador puede cambiar rol o estado.'], 403);
        }
        // El propio correo (identificador de acceso) solo se cambia con la
        // contraseña actual: PUT /auth/email. Un admin sí puede corregir el
        // correo de OTRO usuario desde su detalle.
        $correoAnterior = strtolower(trim((string) $general_user->correo));
        $cambiaCorreo = $request->has('correo') && $request->input('correo') !== $correoAnterior;
        if ($cambiaCorreo && $esPropiaCuenta) {
            return response()->json([
                'message' => 'Usa "Cambiar correo" e ingresa tu contraseña actual para modificar tu correo.',
            ], 422);
        }
        // Un admin no puede cambiar el correo de OTRO admin: podría tomar la
        // cuenta y usar la recuperación de contraseña sobre el correo nuevo.
        // El titular lo cambia con /auth/email y su contraseña actual.
        if ($cambiaCorreo && $esAdmin && ! $esPropiaCuenta && $general_user->rol === 'admin') {
            return response()->json([
                'message' => 'El correo de otro administrador solo puede cambiarlo su titular con la contraseña actual.',
            ], 422);
        }
        // Nadie puede quitarse a sí mismo el rol de admin.
        if ($cambiaRol && $yo && (int) $yo->id === (int) $general_user->id && $general_user->rol === 'admin') {
            return response()->json(['message' => 'No puedes quitarte tu propio rol de administrador.'], 422);
        }
        // Tampoco se puede degradar a OTRO admin desde esta vía: tras
        // degradarlo, el atacante podría cambiar su correo y tomar la cuenta.
        if ($cambiaRol && $esAdmin && ! $esPropiaCuenta && $general_user->rol === 'admin') {
            return response()->json([
                'message' => 'El rol de otro administrador no puede cambiarse desde esta vía.',
            ], 422);
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
        // Un admin no cambia su propio estado (ni se activa ni se desactiva
        // solo): la regla vive en el backend, sin depender del cliente (D-4).
        if ($cambiaEstado && $esPropiaCuenta && $esAdmin) {
            return response()->json(['message' => 'No puedes cambiar tu propio estado.'], 422);
        }

        // Un instructor con fichas a cargo no puede dejar de ser instructor: las
        // fichas quedarían sin responsable. Se exige reasignarlas primero.
        if ($cambiaRol && $general_user->rol === 'instructor' && $request->rol !== 'instructor') {
            $instructor = $general_user->instructor;
            $fichas = $instructor ? ClassGroup::where('id_instructor', $instructor->id)->count() : 0;
            if ($fichas > 0) {
                return response()->json([
                    'message' => "No se puede cambiar el rol: tiene {$fichas} ficha(s) a cargo. Reasígnalas primero.",
                ], 409);
            }
        }

        // Whitelist: solo campos editables por esta vía. El admin también puede
        // corregir el documento de identidad; los campos internos de acceso
        // (password, email_verified_at) nunca se aceptan del request.
        $data = $esAdmin
            ? $request->only([
                'nombre', 'apellido', 'correo', 'foto_url', 'rol', 'estado',
                'tipo_documento', 'numero_documento',
            ])
            : $request->only(['nombre', 'apellido', 'correo', 'foto_url']);

        // Las contraseñas no se cambian por esta vía: la propia exige la actual
        // (PUT /auth/password) y el restablecimiento administrativo envía un
        // enlace de recuperación.
        if ($request->filled('password')) {
            return response()->json([
                'message' => $esAdmin
                    ? 'Usa "Restablecer contraseña" para enviar un enlace de recuperación.'
                    : 'Usa "Cambiar contraseña" e ingresa tu contraseña actual para modificarla.',
            ], 422);
        }

        $cambiaNombre = $request->filled('nombre') && $request->nombre !== $general_user->nombre;
        $cambiaDocumento = ($request->filled('tipo_documento') && $request->tipo_documento !== $general_user->tipo_documento)
            || ($request->filled('numero_documento') && $request->numero_documento !== $general_user->numero_documento);
        $documentoAnterior = Pii::documento($general_user->tipo_documento, $general_user->numero_documento);

        // Actualización atómica: cuenta + perfiles + auditoría (todo o nada).
        DB::transaction(function () use ($general_user, $data, $cambiaRol, $cambiaEstado, $cambiaCorreo, $cambiaNombre, $cambiaDocumento, $documentoAnterior, $dejaDeSerAdminActivo) {
            // La comprobación previa puede quedar obsoleta si otra sesión
            // cambió el rol/estado mientras tanto. Se bloquean y recuentan los
            // administradores activos dentro de la misma transacción.
            if ($dejaDeSerAdminActivo) {
                \App\Support\GuardiaAdministradores::bloquearYAfirmar();
            }

            $general_user->update($data);

            // D-01: una suspensión debe cortar el acceso existente de inmediato.
            if ($cambiaEstado && ! $general_user->estado) {
                SesionesUsuario::revocar($general_user);
                // Un enlace de activación anterior no debe sobrevivir a la
                // suspensión ni reactivarse después.
                AccountActivation::where('user_id', $general_user->id)->delete();
            }

            $documentoNuevo = $cambiaDocumento
                ? Pii::documento($general_user->tipo_documento, $general_user->numero_documento)
                : null;

            Auditoria::registrar('actualizar_usuario', 'general_users', $general_user->id, array_filter([
                'rol' => $cambiaRol ? $general_user->rol : null,
                'estado' => $cambiaEstado ? (bool) $general_user->estado : null,
                'correo' => $cambiaCorreo ? $general_user->correo : null,
                'nombre' => $cambiaNombre ? $general_user->nombre : null,
                // La bitácora no guarda el documento completo. Se conserva el
                // valor anterior y el nuevo enmascarados para trazabilidad.
                'documento_anterior' => $cambiaDocumento ? $documentoAnterior : null,
                'documento_nuevo' => $documentoNuevo,
            // Solo se descartan los nulos: `estado=false` es un cambio real y
            // debe quedar registrado (el array_filter por defecto lo perdería).
            ], fn ($v) => $v !== null));

            // Mantiene coherentes los perfiles (admin/instructor/aprendiz) al cambio de rol.
            if ($cambiaRol) {
                $this->sincronizarPerfiles($general_user);
                $this->limpiarPerfilesObsoletos($general_user);
            }
        });

        // Aviso al usuario afectado solo si el correo cambió realmente y el
        // cambio lo hizo un administrador sobre otra cuenta.
        if ($cambiaCorreo && $esAdmin && !$esPropiaCuenta) {
            $general_user->refresh();

            // Si la cuenta estaba pendiente, el enlace de activación anterior
            // ya no sirve para el correo nuevo. Al activarla, no hay fila.
            AccountActivation::where('user_id', $general_user->id)->delete();

            // Cambiar el identificador de acceso invalida todas las sesiones
            // del usuario objetivo y rota su remember_token. Se borran también
            // enlaces de recuperación del correo anterior y del nuevo.
            SesionesUsuario::revocarOtras($general_user, null, null, $correoAnterior);

            try {
                Notification::route('mail', $correoAnterior)
                    ->notify(new CorreoCambiadoNotification($correoAnterior, $general_user->correo));
            } catch (\Throwable $e) {
                report($e);
            }

            app(NotificacionesService::class)->crear(
                $general_user->id,
                'Tu correo electrónico fue actualizado por un administrador.',
                'sistema'
            );
        }

        return $general_user;
    }

    public function destroy(Request $request, GeneralUser $general_user)
    {
        $request->validate([
            'confirmar_eliminacion' => 'sometimes|boolean',
            'confirmacion_hash' => 'sometimes|string|max:64',
        ]);

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

        // Contexto del borrado: propuestas propias, propuestas de sus fichas,
        // participaciones, observaciones, reportes, historial y similitudes que
        // quedarían afectados. No incluye PII.
        $impacto = $this->impactoUsuario($general_user);
        $tieneHistorial = $this->tieneHistorial($impacto);

        // Sin confirmación explícita, cualquier historial relevante exige una
        // decisión: desactivar (conserva) o eliminar definitivamente (cascada).
        if (!$request->boolean('confirmar_eliminacion')) {
            if ($tieneHistorial) {
                return $this->respuestaEliminacionConHistorial(
                    $general_user,
                    $impacto,
                    $impacto['similitudes'] > 0
                        ? 'No puedes eliminar al usuario: sus propuestas o las de sus fichas tienen similitudes compartidas (vigentes o históricas). La evidencia pertenece también a las demás propuestas.'
                        : 'El usuario tiene propuestas u otros registros históricos. Desactivar la cuenta conserva esos registros y la autoría; la eliminación definitiva los borra en cascada.',
                    'registros_historicos'
                );
            }

            try {
                BorradoCascada::usuario($general_user);
            } catch (HttpResponseException $e) {
                // Carrera: apareció evidencia entre la lectura y el borrado.
                $mensaje = 'No puedes eliminar al usuario: sus propuestas o las de sus fichas tienen similitudes compartidas.';
                $respuesta = $e->getResponse();
                if ($respuesta instanceof JsonResponse) {
                    $mensaje = $respuesta->getData(true)['message'] ?? $mensaje;
                }

                return $this->respuestaEliminacionConHistorial(
                    $general_user,
                    $impacto,
                    $mensaje,
                    'evidencia_compartida'
                );
            }

            // Sin historial no hace falta una segunda confirmación.
            Auditoria::registrar('eliminar_usuario', 'general_users', $general_user->id, [
                'rol' => $general_user->rol,
                'impacto' => $impacto,
            ]);

            return $general_user;
        }

        // Confirmación explícita: se revalida el impacto con las filas
        // relacionadas bloqueadas y la eliminación + auditoría se confirman
        // en la misma transacción. Si la auditoría falla, se revierte todo.
        $hashEsperado = (string) $request->input('confirmacion_hash', '');
        $impactoConfirmado = $impacto;
        $impactoCambiado = false;

        try {
            DB::transaction(function () use (
                $general_user,
                $yo,
                $hashEsperado,
                &$impactoConfirmado,
                &$impactoCambiado
            ) {
                if ($general_user->rol === 'admin' && $general_user->estado) {
                    \App\Support\GuardiaAdministradores::bloquearYAfirmar();
                }

                $this->bloquearUsuarioYRelaciones($general_user);
                $impactoConfirmado = $this->impactoUsuario($general_user);
                $hashActual = $this->hashImpacto($general_user, $impactoConfirmado);

                if ($hashEsperado === '' || !hash_equals($hashActual, $hashEsperado)) {
                    $impactoCambiado = true;
                    throw new HttpResponseException(response()->json([
                        'message' => 'El impacto del borrado cambió desde la primera confirmación. Revisa los registros actuales y vuelve a confirmar.',
                        'requiere_confirmacion' => true,
                        'puede_desactivar' => true,
                        'accion_recomendada' => 'desactivar',
                        'impacto' => $impactoConfirmado,
                        'confirmacion_hash' => $hashActual,
                    ], 409));
                }

                BorradoCascada::usuario($general_user, true);

                Auditoria::registrarEstricto('eliminar_usuario_definitivo', 'general_users', $general_user->id, [
                    'rol' => $general_user->rol,
                    'impacto' => $impactoConfirmado,
                    'confirmacion' => 'explicita',
                    'tenia_historial' => $this->tieneHistorial($impactoConfirmado),
                    'evidencia_compartida' => $impactoConfirmado['similitudes'] > 0,
                    // Copia del actor en el detalle: la fila sobrevive aunque la
                    // cuenta del administrador se elimine después (FK nullOnDelete).
                    'actor_id' => $yo?->id,
                ]);
            });
        } catch (HttpResponseException $e) {
            if (!$impactoCambiado) {
                throw $e;
            }

            return $this->respuestaEliminacionConHistorial(
                $general_user,
                $impactoConfirmado,
                'El impacto del borrado cambió desde la primera confirmación. Revisa los registros actuales y vuelve a confirmar.',
                'impacto_cambiado'
            );
        }

        return $general_user;
    }

    // Censo de registros que se perderían. No incluye PII.
    private function impactoUsuario(GeneralUser $general_user): array
    {
        $instructor = Instructor::where('id_usuario', $general_user->id)->first();
        $fichas = $instructor
            ? ClassGroup::where('id_instructor', $instructor->id)->pluck('id')
            : collect();
        $proyectosPropios = Project::where('id_creador', $general_user->id)->pluck('id');
        $proyectosFichas = $fichas->isEmpty()
            ? collect()
            : Project::whereIn('id_class_group', $fichas)->pluck('id');
        $proyectosSubarbol = $proyectosPropios->merge($proyectosFichas)->unique();

        return [
            'propuestas' => $proyectosPropios->count(),
            'propuestas_fichas' => $proyectosFichas->count(),
            'fichas_a_cargo' => $fichas->count(),
            'similitudes' => $proyectosSubarbol->isEmpty()
                ? 0
                : Similarity::whereIn('id_proyecto_1', $proyectosSubarbol)
                    ->orWhereIn('id_proyecto_2', $proyectosSubarbol)
                    ->count(),
            'observaciones' => Comment::where('id_usuario', $general_user->id)->count(),
            'reportes' => BugReport::where('id_usuario', $general_user->id)->count(),
            'historial' => ProjectHistory::where('id_usuario', $general_user->id)->count(),
            'participaciones' => ApprenticeProject::whereHas(
                'apprentice',
                fn ($q) => $q->where('id_usuario', $general_user->id)
            )->count(),
            'asignaciones' => $instructor
                ? Project::where('id_instructor_asignado', $instructor->id)->count()
                : 0,
        ];
    }

    private function tieneHistorial(array $impacto): bool
    {
        return $impacto['propuestas'] > 0
            || $impacto['propuestas_fichas'] > 0
            || $impacto['fichas_a_cargo'] > 0
            || $impacto['similitudes'] > 0
            || $impacto['observaciones'] > 0
            || $impacto['reportes'] > 0
            || $impacto['historial'] > 0
            || $impacto['participaciones'] > 0
            || $impacto['asignaciones'] > 0;
    }

    // Bloquea al usuario y sus relaciones antes de recalcular el impacto. Los
    // FK de las tablas relacionadas toman locks compartidos sobre estas filas,
    // por lo que las altas concurrentes quedan esperando hasta el commit.
    private function bloquearUsuarioYRelaciones(GeneralUser $general_user): void
    {
        GeneralUser::whereKey($general_user->id)->lockForUpdate()->first();

        $instructor = Instructor::where('id_usuario', $general_user->id)->lockForUpdate()->first();
        $fichas = $instructor
            ? ClassGroup::where('id_instructor', $instructor->id)->lockForUpdate()->pluck('id')
            : collect();

        Apprentice::where('id_usuario', $general_user->id)->lockForUpdate()->first();

        $proyectosPropios = Project::where('id_creador', $general_user->id)->pluck('id');
        $proyectosFichas = $fichas->isEmpty()
            ? collect()
            : Project::whereIn('id_class_group', $fichas)->pluck('id');
        $proyectosAsignados = $instructor
            ? Project::where('id_instructor_asignado', $instructor->id)->pluck('id')
            : collect();
        $proyectos = $proyectosPropios
            ->merge($proyectosFichas)
            ->merge($proyectosAsignados)
            ->unique();

        if ($proyectos->isNotEmpty()) {
            Project::whereIn('id', $proyectos)->lockForUpdate()->pluck('id');
        }
    }

    private function hashImpacto(GeneralUser $general_user, array $impacto): string
    {
        return hash('sha256', json_encode([
            'usuario_id' => $general_user->id,
            'impacto' => $impacto,
        ]));
    }

    // Respuesta común del bloqueo D-01: registra el intento y devuelve las dos
    // alternativas con el impacto (sin datos personales del usuario).
    private function respuestaEliminacionConHistorial(
        GeneralUser $general_user,
        array $impacto,
        string $mensaje,
        string $motivo
    ): JsonResponse {
        Auditoria::registrar('eliminar_usuario_bloqueado', 'general_users', $general_user->id, [
            'rol' => $general_user->rol,
            'motivo' => $motivo,
            'impacto' => $impacto,
        ]);

        return response()->json([
            'message' => $mensaje,
            'requiere_confirmacion' => true,
            'puede_desactivar' => true,
            'accion_recomendada' => 'desactivar',
            'impacto' => $impacto,
            'confirmacion_hash' => $this->hashImpacto($general_user, $impacto),
        ], 409);
    }

    // ---------------------------------------------------------------- Perfiles

    // Alcance del perfil público según la relación académica.
    private function puedeVerPerfil(GeneralUser $yo, GeneralUser $objetivo): bool
    {
        if ($yo->rol === 'admin' || (int) $yo->id === (int) $objetivo->id) return true;

        $miAprendiz = $yo->apprentice()->first();
        $suAprendiz = $objetivo->apprentice;
        $miInstructor = $yo->instructor()->first();
        $suInstructor = $objetivo->instructor;

        // Compañeros de la misma ficha.
        if ($miAprendiz && $suAprendiz
            && $miAprendiz->id_class_group && $suAprendiz->id_class_group
            && (int) $miAprendiz->id_class_group === (int) $suAprendiz->id_class_group) {
            return true;
        }

        // Aprendiz → instructor responsable de su ficha.
        if ($miAprendiz && $miAprendiz->id_class_group && $suInstructor) {
            $ficha = ClassGroup::find($miAprendiz->id_class_group);
            if ($ficha && (int) $ficha->id_instructor === (int) $suInstructor->id) return true;
        }

        // Instructor → aprendiz de sus fichas.
        if ($miInstructor && $suAprendiz && $suAprendiz->id_class_group) {
            return ClassGroup::where('id', $suAprendiz->id_class_group)
                ->where('id_instructor', $miInstructor->id)
                ->exists();
        }

        return false;
    }

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
            $instructor = $usuario->instructor;
            if ($instructor && !ClassGroup::where('id_instructor', $instructor->id)->exists()) {
                $instructor->delete();
            }
        }

        if ($usuario->rol !== 'aprendiz') {
            $aprendiz = $usuario->apprentice;
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
