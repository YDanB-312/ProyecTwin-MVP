<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Apprentice;
use App\Models\AuditLog;
use App\Models\ClassGroup;
use App\Models\Comment;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Project;
use App\Models\ProjectHistory;
use App\Models\Similarity;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;

/**
 * Métricas reales de los paneles, calculadas en el backend y acotadas al rol
 * autenticado (el cliente no elige el rol).
 *
 * `periodo` (3m|30d|7d) cambia realmente el rango de la serie temporal:
 *  - 3m: últimos 3 meses (buckets mensuales).
 *  - 30d: últimos 30 días (buckets diarios).
 *  - 7d: últimos 7 días (buckets diarios).
 * Cada bucket incluye los títulos de las propuestas del periodo (máx. 6) para
 * el tooltip del dashboard; el resto se informa en `restantes`.
 */
class DashboardMetricsController extends Controller
{
    private const PERIODOS = ['3m', '30d', '7d'];
    private const MAX_TITULOS = 6;
    private const MAX_ACTIVIDAD = 12;
    private const MAX_PROPUESTAS_SIMILITUD = 6;
    private const RANGOS_ANTIGUEDAD = ['0–2 días', '3–7 días', '8–15 días', 'Más de 15 días'];

    public function show(Request $request)
    {
        $user = $request->user();
        $periodo = $request->query('periodo', '3m');
        if (!in_array($periodo, self::PERIODOS, true)) {
            $periodo = '3m';
        }

        return match ($user->rol) {
            'admin' => $this->admin($periodo),
            'instructor' => $this->instructor($user, $periodo),
            default => $this->aprendiz($user, $periodo),
        };
    }

    private function admin(string $periodo): array
    {
        $usuarios = GeneralUser::selectRaw('rol, count(*) as total')
            ->groupBy('rol')
            ->pluck('total', 'rol');

        return [
            'rol' => 'admin',
            'periodo' => $periodo,
            'usuarios' => [
                'total' => (int) $usuarios->sum(),
                'aprendices' => (int) ($usuarios['aprendiz'] ?? 0),
                'instructores' => (int) ($usuarios['instructor'] ?? 0),
                'admins' => (int) ($usuarios['admin'] ?? 0),
            ],
            'fichas' => ['total' => ClassGroup::count()],
            'aprendices' => ['total' => Apprentice::count()],
            'propuestas' => $this->porEstado(Project::query()),
            'evolucion' => $this->evolucionProyectos(Project::query(), $periodo),
            'actividad' => $this->actividadAuditoria(),
        ];
    }

    private function instructor(GeneralUser $user, string $periodo): array
    {
        $instructorId = Instructor::where('id_usuario', $user->id)->value('id');

        $fichas = ClassGroup::query()
            ->where('id_instructor', $instructorId ?: 0)
            ->orderBy('codigo')
            ->get(['id', 'codigo', 'nombre']);
        $fichasIds = $fichas->pluck('id');

        $proyectos = Project::query()->where(function ($q) use ($instructorId, $fichasIds) {
            $q->where('id_instructor_asignado', $instructorId ?: 0);
            if ($fichasIds->isNotEmpty()) {
                $q->orWhereIn('id_class_group', $fichasIds);
            }
        });

        $idsProyectos = (clone $proyectos)->pluck('id');

        $proyectosPorFicha = Project::query()
            ->whereIn('id_class_group', $fichasIds)
            ->selectRaw(
                'id_class_group, count(*) as total, '
                . 'sum(case when estado = ? then 1 else 0 end) as pendientes, '
                . 'sum(case when estado = ? then 1 else 0 end) as aprobadas, '
                . 'sum(case when estado = ? then 1 else 0 end) as rechazadas',
                ['pendiente', 'aprobado', 'rechazado']
            )
            ->groupBy('id_class_group')
            ->get()
            ->keyBy('id_class_group');

        $aprendicesPorFicha = Apprentice::query()
            ->whereIn('id_class_group', $fichasIds)
            ->selectRaw('id_class_group, count(*) as total')
            ->groupBy('id_class_group')
            ->pluck('total', 'id_class_group');

        $detalleFichas = $fichas->map(function (ClassGroup $ficha) use ($proyectosPorFicha, $aprendicesPorFicha) {
            $conteo = $proyectosPorFicha->get($ficha->id);
            $total = (int) optional($conteo)->total;
            $pendientes = (int) optional($conteo)->pendientes;
            $aprobadas = (int) optional($conteo)->aprobadas;
            $rechazadas = (int) optional($conteo)->rechazadas;
            $borradores = max(0, $total - $pendientes - $aprobadas - $rechazadas);
            $revisadas = $aprobadas + $rechazadas;
            $revisables = $revisadas + $pendientes;

            return [
                'id' => (int) $ficha->id,
                'codigo' => (string) $ficha->codigo,
                'nombre' => (string) $ficha->nombre,
                'aprendices' => (int) ($aprendicesPorFicha[$ficha->id] ?? 0),
                'propuestas' => $total,
                'borradores' => $borradores,
                'pendientes' => $pendientes,
                'aprobadas' => $aprobadas,
                'rechazadas' => $rechazadas,
                'revisables' => $revisables,
                'revisadas' => $revisadas,
                'cobertura' => $revisables > 0
                    ? (int) round(($revisadas / $revisables) * 100)
                    : null,
            ];
        })->values()->all();

        return [
            'rol' => 'instructor',
            'periodo' => $periodo,
            'fichas' => [
                'total' => $fichas->count(),
                'detalle' => $detalleFichas,
            ],
            'aprendices' => [
                'total' => $fichasIds->isEmpty()
                    ? 0
                    : Apprentice::whereIn('id_class_group', $fichasIds)->count(),
            ],
            'propuestas' => $this->porEstado($proyectos),
            'antiguedadPendientes' => $this->antiguedadPendientes($proyectos),
            'evolucion' => $this->evolucionRevisiones($idsProyectos, $periodo, $user->id),
            'actividad' => $this->actividadProyectos($idsProyectos),
        ];
    }

    private function aprendiz(GeneralUser $user, string $periodo): array
    {
        $proyectos = Project::query()->deAutor($user);
        $propias = (clone $proyectos)->get(['id', 'titulo', 'estado', 'created_at']);
        $ids = $propias->pluck('id');

        $niveles = [
            '0–29 %' => 0,
            '30–49 %' => 0,
            '50–69 %' => 0,
            '70–89 %' => 0,
            '90–100 %' => 0,
        ];

        $similitudesPorPropuesta = [];
        foreach ($propias as $proyecto) {
            $similitudesPorPropuesta[(int) $proyecto->id] = [
                'id' => (int) $proyecto->id,
                'titulo' => (string) $proyecto->titulo,
                'max' => 0,
                'count' => 0,
            ];
        }

        if ($ids->isNotEmpty()) {
            $pares = Similarity::query()
                ->where(function ($q) use ($ids) {
                    $q->whereIn('id_proyecto_1', $ids)
                        ->orWhereIn('id_proyecto_2', $ids);
                })
                ->vigentes()
                ->paraUsuario($user)
                ->get(['id_proyecto_1', 'id_proyecto_2', 'porcentaje']);

            foreach ($pares as $par) {
                $porcentaje = max(0, min(100, (int) $par->porcentaje));

                if ($porcentaje < 30) {
                    $niveles['0–29 %']++;
                } elseif ($porcentaje < 50) {
                    $niveles['30–49 %']++;
                } elseif ($porcentaje < 70) {
                    $niveles['50–69 %']++;
                } elseif ($porcentaje < 90) {
                    $niveles['70–89 %']++;
                } else {
                    $niveles['90–100 %']++;
                }

                foreach ([(int) $par->id_proyecto_1, (int) $par->id_proyecto_2] as $idPropio) {
                    if (!isset($similitudesPorPropuesta[$idPropio])) {
                        continue;
                    }

                    $similitudesPorPropuesta[$idPropio]['count']++;
                    $similitudesPorPropuesta[$idPropio]['max'] = max(
                        $similitudesPorPropuesta[$idPropio]['max'],
                        $porcentaje
                    );
                }
            }
        }

        $similitudesPropuestas = array_values($similitudesPorPropuesta);
        usort($similitudesPropuestas, function (array $a, array $b) {
            return $b['max'] <=> $a['max']
                ?: $b['count'] <=> $a['count']
                ?: $b['id'] <=> $a['id'];
        });
        $similitudesPropuestas = array_slice($similitudesPropuestas, 0, self::MAX_PROPUESTAS_SIMILITUD);

        $similitudesNiveles = [];
        foreach ($niveles as $label => $value) {
            $similitudesNiveles[] = ['label' => $label, 'value' => $value];
        }

        $observaciones = $ids->isEmpty()
            ? []
            : Comment::whereIn('id_proyecto', $ids)
                ->with('user:id,nombre,apellido,rol')
                ->orderByDesc('id')
                ->limit(self::MAX_ACTIVIDAD)
                ->get()
                ->map(function (Comment $comentario) {
                    $texto = trim((string) $comentario->texto);
                    $autor = trim((string) optional($comentario->user)->nombre . ' ' . (string) optional($comentario->user)->apellido);

                    return [
                        'id' => $comentario->id,
                        'titulo' => mb_strlen($texto) > 120 ? mb_substr($texto, 0, 120) . '…' : $texto,
                        'meta' => $autor !== '' ? 'Observación de ' . $autor : 'Observación',
                        'usuario' => $autor,
                        'proyecto_id' => $comentario->id_proyecto,
                        'fecha' => optional($comentario->created_at)->toIso8601String(),
                    ];
                })
                ->values()
                ->all();

        $fechasEnvio = $this->fechasEnvioPorProyecto($ids);
        $comentariosPorPropuesta = $ids->isEmpty()
            ? collect()
            : Comment::query()
                ->whereIn('id_proyecto', $ids)
                ->selectRaw('id_proyecto, count(*) as total')
                ->groupBy('id_proyecto')
                ->pluck('total', 'id_proyecto');

        $propuestasDetalle = $propias->map(function (Project $proyecto) use ($fechasEnvio, $comentariosPorPropuesta, $similitudesPorPropuesta) {
            $similitud = $similitudesPorPropuesta[(int) $proyecto->id] ?? ['max' => 0, 'count' => 0];
            $diasEnRevision = null;

            if ($proyecto->estado === 'pendiente') {
                $fecha = $fechasEnvio->get($proyecto->id) ?? $proyecto->created_at;
                $diasEnRevision = max(0, (int) Carbon::parse($fecha)->startOfDay()->diffInDays(now()->startOfDay()));
            }

            return [
                'id' => (int) $proyecto->id,
                'titulo' => (string) ($proyecto->titulo ?: 'Propuesta sin título'),
                'estado' => (string) $proyecto->estado,
                'diasEnRevision' => $diasEnRevision,
                'maxSimilitud' => (int) ($similitud['max'] ?? 0),
                'similitudes' => (int) ($similitud['count'] ?? 0),
                'observaciones' => (int) ($comentariosPorPropuesta[$proyecto->id] ?? 0),
            ];
        })->values()->all();

        $ordenEstado = ['pendiente' => 0, 'rechazado' => 1, 'aprobado' => 2, 'borrador' => 3];
        usort($propuestasDetalle, function (array $a, array $b) use ($ordenEstado) {
            return ($ordenEstado[$a['estado']] ?? 9) <=> ($ordenEstado[$b['estado']] ?? 9)
                ?: ($b['diasEnRevision'] ?? -1) <=> ($a['diasEnRevision'] ?? -1)
                ?: $b['id'] <=> $a['id'];
        });

        return [
            'rol' => 'aprendiz',
            'periodo' => $periodo,
            'propuestas' => $this->porEstado($proyectos),
            'similitudesNiveles' => $similitudesNiveles,
            'similitudesPropuestas' => $similitudesPropuestas,
            'propuestasDetalle' => $propuestasDetalle,
            'antiguedadPendientes' => $this->antiguedadPendientes($proyectos),
            'evolucion' => $this->evolucionProyectos($proyectos, $periodo),
            'actividad' => $observaciones,
        ];
    }

    /** Conteo por estado (una sola consulta agregada). */
    private function porEstado($query): array
    {
        $conteo = (clone $query)
            ->selectRaw('estado, count(*) as total')
            ->groupBy('estado')
            ->pluck('total', 'estado');

        return [
            'total' => (int) $conteo->sum(),
            'borrador' => (int) ($conteo['borrador'] ?? 0),
            'pendiente' => (int) ($conteo['pendiente'] ?? 0),
            'aprobado' => (int) ($conteo['aprobado'] ?? 0),
            'rechazado' => (int) ($conteo['rechazado'] ?? 0),
        ];
    }

    /** Fecha del último envío/reenvío (o devolución a revisión) por propuesta. */
    private function fechasEnvioPorProyecto($ids)
    {
        if ($ids->isEmpty()) {
            return collect();
        }

        return ProjectHistory::query()
            ->whereIn('id_proyecto', $ids)
            ->whereIn('accion', ['enviada', 'reenviada', 'devuelta_revision'])
            ->orderByDesc('id')
            ->get(['id_proyecto', 'created_at'])
            ->groupBy('id_proyecto')
            ->map(fn ($historiales) => $historiales->first()->created_at);
    }

    /**
     * Pendientes agrupados por tiempo de espera. La referencia es el último
     * envío/reenvío (o la devolución administrativa a revisión); si no hay
     * historial, se usa la fecha de creación.
     */
    private function antiguedadPendientes($proyectos): array
    {
        $pendientes = (clone $proyectos)
            ->where('estado', 'pendiente')
            ->get(['id', 'created_at']);

        $conteo = array_fill_keys(self::RANGOS_ANTIGUEDAD, 0);

        if ($pendientes->isNotEmpty()) {
            $fechasEnvio = $this->fechasEnvioPorProyecto($pendientes->pluck('id'));

            $hoy = now()->startOfDay();

            foreach ($pendientes as $proyecto) {
                $fecha = $fechasEnvio->get($proyecto->id) ?? $proyecto->created_at;
                $dias = max(0, (int) Carbon::parse($fecha)->startOfDay()->diffInDays($hoy));

                if ($dias <= 2) {
                    $conteo[self::RANGOS_ANTIGUEDAD[0]]++;
                } elseif ($dias <= 7) {
                    $conteo[self::RANGOS_ANTIGUEDAD[1]]++;
                } elseif ($dias <= 15) {
                    $conteo[self::RANGOS_ANTIGUEDAD[2]]++;
                } else {
                    $conteo[self::RANGOS_ANTIGUEDAD[3]]++;
                }
            }
        }

        $serie = [];
        foreach (self::RANGOS_ANTIGUEDAD as $label) {
            $serie[] = ['label' => $label, 'value' => $conteo[$label]];
        }

        return $serie;
    }

    /** Rango real del periodo (inicio/fin) y claves de agrupación. */
    private function rango(string $periodo): array
    {
        return match ($periodo) {
            '7d' => [now()->subDays(6)->startOfDay(), 'Y-m-d', 7, 'dia'],
            '30d' => [now()->subDays(29)->startOfDay(), 'Y-m-d', 30, 'dia'],
            default => [now()->subMonths(2)->startOfMonth(), 'Y-m', 3, 'mes'],
        };
    }

    /** Serie de propuestas creadas en el periodo (con títulos para el tooltip). */
    private function evolucionProyectos($query, string $periodo): array
    {
        [$inicio, $formato, $cantidad, $tipo] = $this->rango($periodo);

        $proyectos = (clone $query)
            ->where('created_at', '>=', $inicio)
            ->get(['id', 'titulo', 'created_at']);

        $grupos = [];
        foreach ($proyectos as $proyecto) {
            $fecha = Carbon::parse($proyecto->created_at);
            $clave = $fecha->format($formato);
            if (!isset($grupos[$clave])) {
                $grupos[$clave] = ['total' => 0, 'titulos' => []];
            }
            $grupos[$clave]['total']++;
            if (count($grupos[$clave]['titulos']) < self::MAX_TITULOS) {
                $grupos[$clave]['titulos'][] = $proyecto->titulo;
            }
        }

        return $this->construirSerie($periodo, $tipo, $cantidad, $grupos);
    }

    /**
     * Decisiones tomadas por el instructor autenticado sobre propuestas de su
     * ámbito. Las acciones del historial son las decisiones académicas reales,
     * no el nombre de la acción de auditoría.
     */
    private function evolucionRevisiones($idsProyectos, string $periodo, int $idUsuario): array
    {
        [$inicio, $formato, $cantidad, $tipo] = $this->rango($periodo);

        if ($idsProyectos->isEmpty()) {
            return $this->construirSerie($periodo, $tipo, $cantidad, []);
        }

        $historiales = ProjectHistory::with('project:id,titulo')
            ->whereIn('id_proyecto', $idsProyectos)
            ->where('id_usuario', $idUsuario)
            ->whereIn('accion', ['aprobada', 'rechazada'])
            ->where('created_at', '>=', $inicio)
            ->get(['id', 'id_proyecto', 'created_at']);

        $grupos = [];
        foreach ($historiales as $historial) {
            $clave = Carbon::parse($historial->created_at)->format($formato);
            if (!isset($grupos[$clave])) {
                $grupos[$clave] = ['total' => 0, 'titulos' => []];
            }
            $grupos[$clave]['total']++;
            $titulo = optional($historial->project)->titulo;
            if ($titulo && !in_array($titulo, $grupos[$clave]['titulos'], true) && count($grupos[$clave]['titulos']) < self::MAX_TITULOS) {
                $grupos[$clave]['titulos'][] = $titulo;
            }
        }

        return $this->construirSerie($periodo, $tipo, $cantidad, $grupos);
    }

    /** Genera los buckets completos (incluye periodos sin registros). */
    private function construirSerie(string $periodo, string $tipo, int $cantidad, array $grupos): array
    {
        $serie = [];
        for ($i = $cantidad - 1; $i >= 0; $i--) {
            $fecha = $tipo === 'mes' ? now()->subMonths($i) : now()->subDays($i);
            $clave = $fecha->format($tipo === 'mes' ? 'Y-m' : 'Y-m-d');
            $datos = $grupos[$clave] ?? ['total' => 0, 'titulos' => []];

            $serie[] = [
                'clave' => $clave,
                'label' => $tipo === 'mes'
                    ? $fecha->locale('es')->isoFormat('MMM')
                    : $fecha->format('d/m'),
                'fecha' => $tipo === 'mes'
                    ? $fecha->locale('es')->isoFormat('MMMM [de] YYYY')
                    : $fecha->locale('es')->isoFormat('ddd D [de] MMMM'),
                'total' => $datos['total'],
                'propuestas' => $datos['titulos'],
                'restantes' => max(0, $datos['total'] - count($datos['titulos'])),
            ];
        }

        return $serie;
    }

    private function actividadAuditoria(): array
    {
        return AuditLog::with(['user:id,nombre,apellido,rol'])
            ->orderByDesc('id')
            ->limit(self::MAX_ACTIVIDAD)
            ->get()
            ->map(function (AuditLog $log) {
                $usuario = trim((string) optional($log->user)->nombre . ' ' . (string) optional($log->user)->apellido);

                return [
                    'id' => $log->id,
                    'titulo' => (string) $log->accion,
                    'meta' => trim(($log->entidad ?? '') . ($log->entidad_id ? ' #' . $log->entidad_id : '')) ?: 'Sistema',
                    'usuario' => $usuario,
                    'proyecto_id' => null,
                    'fecha' => optional($log->created_at)->toIso8601String(),
                ];
            })
            ->values()
            ->all();
    }

    private function actividadProyectos($idsProyectos): array
    {
        if ($idsProyectos->isEmpty()) {
            return [];
        }

        return ProjectHistory::whereIn('id_proyecto', $idsProyectos)
            ->with(['user:id,nombre,apellido,rol', 'project:id,titulo'])
            ->orderByDesc('id')
            ->limit(self::MAX_ACTIVIDAD)
            ->get()
            ->map(function (ProjectHistory $historial) {
                $usuario = trim((string) optional($historial->user)->nombre . ' ' . (string) optional($historial->user)->apellido);

                return [
                    'id' => $historial->id,
                    'titulo' => (string) $historial->accion,
                    'meta' => optional($historial->project)->titulo ?: 'Propuesta',
                    'usuario' => $usuario,
                    'proyecto_id' => $historial->id_proyecto,
                    'fecha' => optional($historial->created_at)->toIso8601String(),
                ];
            })
            ->values()
            ->all();
    }
}
