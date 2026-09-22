<?php

namespace App\Http\Controllers\Api;

use App\Models\MotorConfig;
use App\Models\Project;
use App\Models\TrainingProgram;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class MotorConfigController extends Controller
{
    // Config vigente: el admin de centro ve la de su centro (con herencia del
    // valor por defecto); el público y el superadmin ven la global.
    public function show(Request $request)
    {
        $user = $request->user();
        if ($user && $user->esAdminDeCentro()) {
            $centroId = $user->centroId();
            $propia = $centroId ? MotorConfig::where('training_center_id', $centroId)->first() : null;
            $item = $propia ?: MotorConfig::global();
            return response()->json(array_merge($item->toArray(), [
                'hereda' => !$propia,
                'centro_id' => $centroId,
            ]));
        }

        return MotorConfig::global();
    }

    // Resumen público para la landing: configuración del motor + conteos
    // agregados (sin datos personales).
    public function resumen()
    {
        $item = MotorConfig::global();
        return response()->json([
            'umbral' => $item->umbral,
            'meses' => $item->meses,
            'total_propuestas' => Project::where('estado', '!=', 'rechazado')->count(),
            'total_programas' => TrainingProgram::count(),
        ]);
    }

    // El superadmin edita el valor por defecto (global); el admin de centro,
    // el de su propio centro.
    public function update(Request $request)
    {
        $request->validate([
            'umbral' => 'required|numeric|min:0.05|max:0.95',
            'meses' => 'required|integer|min:1|max:60',
        ]);

        $user = $request->user();
        if ($user && $user->esAdminDeCentro()) {
            $centroId = $user->centroId();
            if (!$centroId) {
                return response()->json(['message' => 'Tu cuenta no tiene un centro asignado.'], 422);
            }
            $item = MotorConfig::firstOrCreate(
                ['training_center_id' => $centroId],
                ['umbral' => 0.2, 'meses' => 12]
            );
        } else {
            $item = MotorConfig::global();
        }

        $item->update($request->only(['umbral', 'meses']));

        \App\Support\Auditoria::registrar('config_motor', 'motor_configs', $item->id, [
            'umbral' => (float) $item->umbral,
            'meses' => (int) $item->meses,
            'centro' => $item->training_center_id,
        ]);

        return response()->json($item);
    }

    // Restaura el valor por defecto del centro (borra su fila de override).
    public function reset(Request $request)
    {
        $user = $request->user();
        if (!$user || !$user->esAdminDeCentro()) {
            return response()->json(['message' => 'Solo un admin de centro puede restaurar el valor por defecto.'], 403);
        }

        MotorConfig::where('training_center_id', $user->centroId())->delete();
        return response()->json(['message' => 'El centro usa ahora el valor por defecto.', 'restaurado' => true]);
    }
}
