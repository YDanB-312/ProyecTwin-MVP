<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\MotorConfig;
use App\Models\Notification;
use App\Models\Project;
use App\Models\Similarity;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

// Conteos agregados por rol. Evita que los tableros descarguen colecciones
// completas solo para pintar números.
class StatsController extends Controller
{
    public function resumen(Request $request)
    {
        $user = $request->user();
        $noLeidas = Notification::where('id_usuario', $user->id)->where('leida', false)->count();

        if ($user->rol === 'admin') {
            $proyectos = $this->porColumna(Project::class, 'estado');
            $reportes = $this->porColumna(BugReport::class, 'estado');
            $fichas = $this->porColumna(ClassGroup::class, 'estado');
            $usuarios = $this->porColumna(GeneralUser::class, 'rol');
            $config = MotorConfig::first();

            return response()->json([
                'usuarios' => [
                    'total' => (int) $usuarios->sum(),
                    'suspendidos' => GeneralUser::where('estado', false)->count(),
                    'porRol' => $usuarios,
                ],
                'proyectos' => [
                    'total' => (int) $proyectos->sum(),
                    'pendiente' => (int) ($proyectos['pendiente'] ?? 0),
                    'aprobado' => (int) ($proyectos['aprobado'] ?? 0),
                    'rechazado' => (int) ($proyectos['rechazado'] ?? 0),
                ],
                'similitudes' => Similarity::count(),
                'reportes' => [
                    'total' => (int) $reportes->sum(),
                    'abiertos' => (int) ($reportes['pendiente'] ?? 0) + (int) ($reportes['en_revision'] ?? 0),
                ],
                'fichas' => [
                    'total' => (int) $fichas->sum(),
                    'activo' => (int) ($fichas['activo'] ?? 0),
                    'finalizado' => (int) ($fichas['finalizado'] ?? 0),
                ],
                'notificaciones_no_leidas' => $noLeidas,
                'motor' => [
                    'umbral' => (float) ($config->umbral ?? 0.30),
                    'meses' => (int) ($config->meses ?? 12),
                ],
            ]);
        }

        if ($user->rol === 'instructor') {
            $instructorId = Instructor::where('id_usuario', $user->id)->value('id');
            $fichaIds = $instructorId
                ? ClassGroup::where('id_instructor', $instructorId)->pluck('id')
                : collect();

            return response()->json([
                'fichas' => $fichaIds->count(),
                'aprendices' => Apprentice::whereIn('id_class_group', $fichaIds)->count(),
                'propuestas_pendientes' => Project::whereIn('id_class_group', $fichaIds)
                    ->where('estado', 'pendiente')->count(),
                'similitudes' => Similarity::paraUsuario($user)->count(),
                'notificaciones_no_leidas' => $noLeidas,
            ]);
        }

        // Aprendiz: propuestas propias (creador o equipo) y similitudes visibles.
        $mios = Project::where('id_creador', $user->id)
            ->orWhereHas('apprentices', fn ($q) => $q->where('id_usuario', $user->id))
            ->pluck('id');

        return response()->json([
            'propuestas' => $mios->count(),
            'propuestas_aprobadas' => Project::whereIn('id', $mios)->where('estado', 'aprobado')->count(),
            'similitudes' => Similarity::paraUsuario($user)->count(),
            'notificaciones_no_leidas' => $noLeidas,
        ]);
    }

    private function porColumna(string $modelo, string $columna)
    {
        return $modelo::selectRaw("{$columna}, COUNT(*) as n")
            ->groupBy($columna)
            ->pluck('n', $columna);
    }
}
