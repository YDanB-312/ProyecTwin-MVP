<?php

namespace App\Similarity;

use App\Models\MotorConfig;
use App\Models\Project;
use App\Models\Similarity;
use App\Services\NotificacionesService;
use App\Services\SimilitudService;
use App\Support\Auditoria;
use Illuminate\Support\Facades\DB;

// Recompone la tabla `similarities` con el motor actual: purga los pares que ya
// no cumplen (umbral/programa/ventana/contraparte aprobada) y crea los que
// faltan. Lo usan el comando `similitud:recalcular`, el seeder y la API (envío
// de propuestas y recalibración): punto único de la orquestación del motor.
class Recomputador
{
    // Detección de UNA propuesta contra el corpus vigente: refresca los pares
    // existentes, crea los nuevos (notificando) y purga los obsoletos.
    public function detectar(Project $propio, bool $notificar = true): int
    {
        $config = MotorConfig::firstOrCreate([], ['umbral' => 0.30, 'meses' => 12]);
        $umbral = (float) $config->umbral;
        $meses = (int) $config->meses;

        // Detección atómica y serializada: el lock de la fila única del motor
        // evita que dos detecciones concurrentes dupliquen el mismo par.
        return DB::transaction(function () use ($propio, $umbral, $meses, $notificar, $config) {
            MotorConfig::whereKey($config->id)->lockForUpdate()->first();

            // Rechazada o borrador no participan: sus pares dejan de estar
            // vigentes, pero se conservan como evidencia de la versión analizada.
            if (in_array($propio->estado, ['rechazado', 'borrador'], true)) {
                $this->archivarPares($propio->id);
                return 0;
            }

            $pares = SimilitudService::puntaje($propio, $this->corpus($propio, $meses));

            // Ids que quedan vigentes para esta propuesta.
            $vigentes = [];
            $creadas = 0;
            foreach ($pares as $par) {
                if ($par['score'] < $umbral) continue;
                $vigentes[] = $par['project_id'];
                $existente = $this->buscarParVigente($propio->id, $par['project_id']);
                if ($existente) {
                    $existente->update([
                        'porcentaje' => $par['porcentaje'],
                        'detalles' => $par['detalles'],
                    ]);
                    continue;
                }

                $this->guardarPar($propio->id, $par['project_id'], $par['porcentaje'], $par['detalles']);
                $creadas++;
                if ($notificar) {
                    app(NotificacionesService::class)->similitud($propio, $par['project_id'], $par['porcentaje']);
                }
            }

            // Los pares que ya no aplican dejan de estar vigentes (no se borran).
            $this->archivarParesObsoletos($propio->id, $vigentes);

            return $creadas;
        });
    }

    // Deja de considerar vigentes los pares de una propuesta (rechazo/reenvío):
    // se conservan como evidencia histórica de la versión analizada.
    public function archivar(Project $propio): void
    {
        $this->archivarPares($propio->id);
    }

    public function recalcular(bool $notificar = true, bool $auditar = false): array
    {
        $config = MotorConfig::firstOrCreate([], ['umbral' => 0.30, 'meses' => 12]);
        $umbral = (float) $config->umbral;
        $meses = (int) $config->meses;

        // Recalibración atómica y serializada: purga + creación + notificaciones
        // (todo o nada). El lock del motor evita duplicados concurrentes.
        return DB::transaction(function () use ($umbral, $meses, $notificar, $auditar, $config) {
            MotorConfig::whereKey($config->id)->lockForUpdate()->first();
            $antes = Similarity::where('vigente', true)->count();

            // 1) Los pares vigentes que ya no cumplen dejan de estarlo (se
            //    conservan como evidencia histórica).
            Similarity::where('vigente', true)->get()->each(function (Similarity $s) use ($umbral, $meses) {
                if (!$this->parVigente($s, $umbral, $meses)) $s->update(['vigente' => false]);
            });
            $eliminadas = $antes - Similarity::where('vigente', true)->count();

            // 2) Recomputa y crea los pares faltantes.
            // Solo propuestas enviadas participan del corpus (los borradores no
            // generan coincidencias ni notificaciones; una rechazada tampoco).
            $proyectos = Project::with('classGroup')->whereIn('estado', ['pendiente', 'aprobado'])->get();
            $creadas = 0;
            foreach ($proyectos as $p) {
                foreach (SimilitudService::puntaje($p, $this->corpus($p, $meses)) as $par) {
                    if ($par['score'] < $umbral) continue;

                    // Si el par ya existe, se REFRESCA su porcentaje/detalles con
                    // el motor actual (así "recalcular" refleja el umbral vigente).
                    $existente = $this->buscarParVigente($p->id, $par['project_id']);
                    if ($existente) {
                        $existente->update([
                            'porcentaje' => $par['porcentaje'],
                            'detalles' => $par['detalles'],
                        ]);
                        continue;
                    }

                    $this->guardarPar($p->id, $par['project_id'], $par['porcentaje'], $par['detalles']);
                    $creadas++;
                    if ($notificar) {
                        app(NotificacionesService::class)->similitud($p, $par['project_id'], $par['porcentaje']);
                    }
                }
            }

            if ($auditar) {
                Auditoria::registrar('recalibrar_motor', 'similarities', null, [
                    'eliminadas' => $eliminadas,
                    'creadas' => $creadas,
                    'umbral' => $umbral,
                    'meses' => $meses,
                ]);
            }

            return ['eliminadas' => $eliminadas, 'creadas' => $creadas, 'total' => Similarity::where('vigente', true)->count()];
        });
    }

    // Corpus de comparación: propuestas APROBADAS del mismo programa dentro de
    // la ventana. La propia se incluye solo para que exista en los vectores.
    private function corpus(Project $propio, int $meses): array
    {
        $programaId = optional($propio->classGroup)->id_programa;

        return Project::with('classGroup')
            ->where('id', '!=', $propio->id)
            ->where('estado', 'aprobado')
            ->where('created_at', '>=', now()->subMonths($meses))
            ->whereHas('classGroup', fn ($q) => $q->where('id_programa', $programaId))
            ->get()
            ->prepend($propio)
            ->all();
    }

    // Un par es válido si respeta umbral, mismo programa y ventana, y además
    // la referencia es una propuesta APROBADA (al menos uno de los dos).
    //
    // Regla temporal (intencional): el corpus solo compara contra aprobadas
    // dentro de la ventana, mientras que un par vigente se conserva si
    // CUALQUIERA de los dos está en ventana. Los proyectos antiguos se
    // reanalizan al recalibrar desde Config (no en cada envío); está
    // documentado en README y no es una inconsistencia accidental.
    private function parVigente(Similarity $s, float $umbral, int $meses): bool
    {
        if (($s->porcentaje / 100) < $umbral) return false;

        $p1 = Project::with('classGroup')->find($s->id_proyecto_1);
        $p2 = Project::with('classGroup')->find($s->id_proyecto_2);
        if (!$p1 || !$p2) return false;
        if (in_array($p1->estado, ['rechazado', 'borrador'], true)
            || in_array($p2->estado, ['rechazado', 'borrador'], true)) return false;
        if ($p1->estado !== 'aprobado' && $p2->estado !== 'aprobado') return false;
        if (optional($p1->classGroup)->id_programa !== optional($p2->classGroup)->id_programa) return false;

        $desde = now()->subMonths($meses);
        return $p1->created_at >= $desde || $p2->created_at >= $desde;
    }

    private function buscarParVigente(int $a, int $b): ?Similarity
    {
        [$p1, $p2] = $a < $b ? [$a, $b] : [$b, $a];
        return Similarity::where('id_proyecto_1', $p1)->where('id_proyecto_2', $p2)
            ->where('vigente', true)
            ->first();
    }

    private function guardarPar(int $a, int $b, int $porcentaje, array $detalles): void
    {
        [$p1, $p2] = $a < $b ? [$a, $b] : [$b, $a];
        Similarity::create([
            'id_proyecto_1' => $p1,
            'id_proyecto_2' => $p2,
            'porcentaje' => $porcentaje,
            'detalles' => $detalles,
            'fecha' => now()->toDateString(),
        ]);
    }

    // Deja como históricos todos los pares vigentes de una propuesta.
    private function archivarPares(int $projectId): void
    {
        Similarity::where('id_proyecto_1', $projectId)->orWhere('id_proyecto_2', $projectId)
            ->where('vigente', true)
            ->update(['vigente' => false]);
    }

    // Deja como históricos los pares vigentes de la propuesta que ya no
    // aparecen en la nueva detección (no se borran).
    private function archivarParesObsoletos(int $projectId, array $vigentes): void
    {
        $vigentes = array_map('intval', $vigentes);
        Similarity::where('id_proyecto_1', $projectId)->orWhere('id_proyecto_2', $projectId)
            ->where('vigente', true)
            ->get()
            ->each(function (Similarity $s) use ($projectId, $vigentes) {
                $otro = (int) $s->id_proyecto_1 === $projectId ? (int) $s->id_proyecto_2 : (int) $s->id_proyecto_1;
                if (!in_array($otro, $vigentes, true)) $s->update(['vigente' => false]);
            });
    }
}
