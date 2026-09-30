<?php

namespace App\Similarity;

use App\Models\MotorConfig;
use App\Models\Project;
use App\Models\Similarity;
use App\Services\NotificacionesService;
use App\Services\SimilitudService;

// Recompone la tabla `similarities` con el motor actual: purga los pares que ya
// no cumplen (umbral/programa/ventana/contraparte aprobada) y crea los que
// faltan. Lo usan el comando `similitud:recalcular` y el seeder (para que el
// seed quede consistente de una sola pasada).
class Recomputador
{
    public function recalcular(bool $notificar = true): array
    {
        $config = MotorConfig::firstOrCreate([], ['umbral' => 0.30, 'meses' => 12]);
        $umbral = (float) $config->umbral;
        $meses = (int) $config->meses;

        $antes = Similarity::count();

        // 1) Purga de pares que ya no cumplen las reglas.
        Similarity::all()->each(function (Similarity $s) use ($umbral, $meses) {
            if (!$this->parVigente($s, $umbral, $meses)) $s->delete();
        });
        $eliminadas = $antes - Similarity::count();

        // 2) Recomputa y crea los pares faltantes.
        $proyectos = Project::with('classGroup')->where('estado', '!=', 'rechazado')->get();
        $creadas = 0;
        foreach ($proyectos as $p) {
            foreach (SimilitudService::puntaje($p, $this->corpus($p, $meses)) as $par) {
                if ($par['score'] < $umbral) continue;

                // Si el par ya existe, se REFRESCA su porcentaje/detalles con el
                // motor actual (así "recalcular" refleja el umbral vigente).
                $existente = $this->buscarPar($p->id, $par['project_id']);
                if ($existente) {
                    $existente->update([
                        'porcentaje' => $par['porcentaje'],
                        'detalles' => $par['detalles'],
                    ]);
                    continue;
                }

                $this->guardarPar($p->id, $par['project_id'], $par['porcentaje'], $par['detalles']);
                $creadas++;
                if ($notificar) app(NotificacionesService::class)->similitud($p, $par['project_id'], $par['porcentaje']);
            }
        }

        return ['eliminadas' => $eliminadas, 'creadas' => $creadas, 'total' => Similarity::count()];
    }

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

    private function parVigente(Similarity $s, float $umbral, int $meses): bool
    {
        if (($s->porcentaje / 100) < $umbral) return false;

        $p1 = Project::with('classGroup')->find($s->id_proyecto_1);
        $p2 = Project::with('classGroup')->find($s->id_proyecto_2);
        if (!$p1 || !$p2) return false;
        if ($p1->estado === 'rechazado' || $p2->estado === 'rechazado') return false;
        if ($p1->estado !== 'aprobado' && $p2->estado !== 'aprobado') return false;
        if (optional($p1->classGroup)->id_programa !== optional($p2->classGroup)->id_programa) return false;

        $desde = now()->subMonths($meses);
        return $p1->created_at >= $desde || $p2->created_at >= $desde;
    }

    private function buscarPar(int $a, int $b): ?Similarity
    {
        [$p1, $p2] = $a < $b ? [$a, $b] : [$b, $a];
        return Similarity::where('id_proyecto_1', $p1)->where('id_proyecto_2', $p2)->first();
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

}
