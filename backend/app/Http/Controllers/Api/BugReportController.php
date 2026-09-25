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

        // El admin ve todos; cada usuario solo sus propios reportes.
        return BugReport::included()
            ->when(optional($user)->rol !== 'admin', fn ($q) => $q->where('id_usuario', $user->id))
            ->orderByDesc('id')
            ->get();
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
        if ((int) $item->id_usuario !== (int) $user->id && $user->rol !== 'admin') {
            return response()->json(['message' => 'No tienes acceso a este reporte.'], 403);
        }
        return $item;
    }

    public function update(Request $request, BugReport $bug_report)
    {
        if ($request->user()->rol !== 'admin') {
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
        if ($request->user()->rol !== 'admin') {
            return response()->json(['message' => 'Solo un administrador puede eliminar reportes.'], 403);
        }

        $bug_report->delete();
        // Evita notificaciones con enlace a un reporte ya inexistente.
        Notification::where('enlace', 'reporte:' . $bug_report->id)->delete();
        return $bug_report;
    }

    // Crea una notificación por cada admin activo (el panel filtra por usuario).
    private function notificarAdmins(BugReport $reporte): void
    {
        GeneralUser::where('rol', 'admin')->where('estado', true)->get()->each(function (GeneralUser $admin) use ($reporte) {
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
