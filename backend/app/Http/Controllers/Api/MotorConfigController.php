<?php

namespace App\Http\Controllers\Api;

use App\Models\MotorConfig;
use App\Models\Project;
use App\Models\TrainingProgram;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class MotorConfigController extends Controller
{
    // Configuración del motor (fila única global). La lectura es pública.
    public function show()
    {
        return MotorConfig::actual();
    }

    // Resumen público para la landing: configuración del motor + conteos
    // agregados (sin datos personales).
    public function resumen()
    {
        $item = MotorConfig::actual();
        return response()->json([
            'umbral' => $item->umbral,
            'meses' => $item->meses,
            'total_propuestas' => Project::where('estado', '!=', 'rechazado')->count(),
            'total_programas' => TrainingProgram::count(),
        ]);
    }

    public function update(Request $request)
    {
        $request->validate([
            'umbral' => 'required|numeric|min:0.05|max:0.95',
            'meses' => 'required|integer|min:1|max:60',
        ]);

        $item = MotorConfig::actual();
        $item->update($request->only(['umbral', 'meses']));

        \App\Support\Auditoria::registrar('config_motor', 'motor_configs', $item->id, [
            'umbral' => (float) $item->umbral,
            'meses' => (int) $item->meses,
        ]);

        return response()->json($item);
    }
}
