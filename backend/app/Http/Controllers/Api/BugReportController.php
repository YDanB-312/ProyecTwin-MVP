<?php

namespace App\Http\Controllers\Api;

use App\Models\BugReport;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class BugReportController extends Controller
{
    public function index()
    {
        $items = BugReport::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'titulo' => 'nullable|max:255',
            'descripcion' => 'required',
            'tipo' => 'required|max:255',
            'pasos' => 'nullable',
            'url_evidencia' => 'nullable|max:255',
            'estado' => 'nullable|max:255',
            'fecha' => 'required|date',
            'id_usuario' => 'required|exists:general_users,id',
            'id_admin' => 'nullable|exists:admins,id',
        ]);

        $item = BugReport::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = BugReport::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, BugReport $bug_report)
    {
        $request->validate([
            'titulo' => 'nullable|max:255',
            'descripcion' => 'required',
            'tipo' => 'required|max:255',
            'pasos' => 'nullable',
            'url_evidencia' => 'nullable|max:255',
            'estado' => 'nullable|max:255',
            'fecha' => 'required|date',
            'id_usuario' => 'required|exists:general_users,id',
            'id_admin' => 'nullable|exists:admins,id',
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
