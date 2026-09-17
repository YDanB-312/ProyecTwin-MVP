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

    public function index()
    {
        return BugReport::included()->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'titulo' => 'nullable|max:255',
            'descripcion' => 'required',
            'tipo' => 'required|in:' . self::TIPOS,
            'estado' => 'nullable|in:pendiente,en_revision,resuelto,cerrado,rechazado',
            'fecha' => 'required|date',
            'id_usuario' => 'required|exists:general_users,id',
        ]);

        $item = BugReport::create($request->all());

        // Avisa a cada administrador activo que entró un reporte nuevo.
        $this->notificarAdmins($item);

        return response()->json($item, 201);
    }

    public function show($id)
    {
        return BugReport::included()->findOrFail($id);
    }

    public function update(Request $request, BugReport $bug_report)
    {
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

    public function destroy(BugReport $bug_report)
    {
        $bug_report->delete();
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
