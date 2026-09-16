<?php

namespace App\Http\Controllers\Api;

use App\Models\MotorConfig;
use App\Models\Project;
use App\Models\TrainingProgram;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class MotorConfigController extends Controller
{
    public function show()
    {
        $item = MotorConfig::firstOrCreate([], ['umbral' => 0.2, 'meses' => 12]);
        return $item;
    }

    // Resumen público para la landing: configuración del motor + conteos
    // agregados (sin datos personales).
    public function resumen()
    {
        $item = MotorConfig::firstOrCreate([], ['umbral' => 0.2, 'meses' => 12]);
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

        $item = MotorConfig::firstOrCreate([], ['umbral' => 0.2, 'meses' => 12]);
        $item->update($request->only(['umbral', 'meses']));
        return $item;
    }
}
