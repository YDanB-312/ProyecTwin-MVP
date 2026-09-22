<?php

namespace App\Http\Controllers\Api;

use App\Models\BugReport;
use App\Models\GeneralUser;
use App\Models\Notification;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class BugReportController extends Controller
{
    private const TIPOS = 'sistema,proyecto,datos,bug_ui,error_datos,rendimiento,seguridad,otro';

    public function index(Request $request)
    {
        $user = $request->user();

        // El superadmin ve todos; el admin de centro, los de su centro; cada
        // usuario solo sus propios reportes.
        $query = BugReport::included()->orderByDesc('id');

        if (optional($user)->esSuperadmin()) {
            return $query->get();
        }
        if (optional($user)->esAdminDeCentro()) {
            $centroId = $user->centroId();
            return $query->whereHas('generalUser', function ($q) use ($centroId) {
                $q->whereHas('apprentice.classGroup', fn ($x) => $x->where('training_center_id', $centroId))
                  ->orWhereHas('instructor.classGroups', fn ($x) => $x->where('training_center_id', $centroId));
            })->get();
        }

        return $query->where('id_usuario', $user->id)->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'titulo' => 'nullable|max:255',
            'descripcion' => 'required',
            'tipo' => 'required|in:' . self::TIPOS,
            'estado' => 'nullable|in:pendiente,en_revision,resuelto,cerrado,rechazado',
            'fecha' => 'required|date',
            'id_usuario' => 'nullable|exists:general_users,id',
        ]);

        // El reporte siempre se firma con el usuario del token.
        $item = BugReport::create([
            'titulo' => $request->titulo,
            'descripcion' => $request->descripcion,
            'tipo' => $request->tipo,
            'estado' => $request->estado ?? 'pendiente',
            'fecha' => $request->fecha,
            'id_usuario' => $request->user()->id,
        ]);

        // Avisa a cada administrador activo que entró un reporte nuevo.
        $this->notificarAdmins($item);

        return response()->json($item, 201);
    }

    public function show(Request $request, $id)
    {
        $item = BugReport::included()->findOrFail($id);
        $user = $request->user();
        $esSuperadmin = optional($user)->esSuperadmin();
        $esAdminCentro = optional($user)->esAdminDeCentro() && $item->generalUser
            && $item->generalUser->perteneceAlCentro($user->centroId());
        if ((int) $item->id_usuario !== (int) $user->id && !$esSuperadmin && !$esAdminCentro) {
            return response()->json(['message' => 'No tienes acceso a este reporte.'], 403);
        }
        return $item;
    }

    public function update(Request $request, BugReport $bug_report)
    {
        if (!$this->puedeGestionar($request, $bug_report)) {
            return response()->json(['message' => 'Solo un administrador puede actualizar reportes.'], 403);
        }

        $request->validate([
            'titulo' => 'nullable|max:255',
            'descripcion' => 'required',
            'tipo' => 'required|in:' . self::TIPOS,
            'estado' => 'nullable|in:pendiente,en_revision,resuelto,cerrado,rechazado',
            'fecha' => 'required|date',
            'id_usuario' => 'required|exists:general_users,id',
        ]);

        $bug_report->update($request->all());
        return $bug_report;
    }

    public function destroy(Request $request, BugReport $bug_report)
    {
        if (!$this->puedeGestionar($request, $bug_report)) {
            return response()->json(['message' => 'Solo un administrador puede eliminar reportes.'], 403);
        }

        $bug_report->delete();
        return $bug_report;
    }

    // Superadmin cualquiera; admin de centro solo los de su centro.
    private function puedeGestionar(Request $request, BugReport $reporte): bool
    {
        $user = $request->user();
        if (!$user) return false;
        if ($user->esSuperadmin()) return true;
        if (!$user->esAdminDeCentro()) return false;

        return $reporte->generalUser && $reporte->generalUser->perteneceAlCentro($user->centroId());
    }

    // Crea una notificación por cada admin activo que corresponda: el superadmin
    // siempre; el admin de centro solo si el reporte viene de su centro.
    private function notificarAdmins(BugReport $reporte): void
    {
        $reporte->loadMissing('generalUser');
        $autor = $reporte->generalUser;

        GeneralUser::whereIn('rol', ['admin', 'superadmin'])->where('estado', true)->get()
            ->each(function (GeneralUser $admin) use ($reporte, $autor) {
                if ($admin->esAdminDeCentro() && !($autor && $autor->perteneceAlCentro($admin->centroId()))) {
                    return;
                }
                Notification::create([
                    'titulo' => 'Nuevo reporte de falla: "' . ($reporte->titulo ?: 'Sin título') . '"',
                    'tipo' => 'sistema',
                    'enlace' => 'reporte:' . $reporte->id,
                    'leida' => false,
                    'fecha' => now()->toDateString(),
                    'id_usuario' => $admin->id,
                ]);
            });
    }
}
