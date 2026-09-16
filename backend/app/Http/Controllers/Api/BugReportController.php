<?php

namespace App\Http\Controllers\Api;

use App\Models\BugReport;
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
}
