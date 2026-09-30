<?php

namespace App\Console\Commands;

use App\Models\Project;
use App\Services\SimilitudService;
use Illuminate\Console\Command;

// Calibración del motor: calcula los pares reales del corpus y muestra las
// señales (palabras, caracteres, tema, cobertura) + el score combinado.
//
// Con un archivo de etiquetas (opcional) calcula precisión/recall/F1 para varios
// umbrales y sugiere el mejor. Sin etiquetas, muestra la distribución y los
// huecos para elegir el umbral a ojo con datos.
//
// Etiquetas: JSON { "idA-idB": 1, "idC-idD": 0 }  (1 = debería marcarse).
class SimilitudCalibrar extends Command
{
    protected $signature = 'similitud:calibrar {--labels= : Ruta a un JSON de etiquetas}';

    protected $description = 'Muestra los puntajes y señales por par y sugiere umbral.';

    public function handle(): int
    {
        $proyectos = Project::with('classGroup')->where('estado', '!=', 'rechazado')->get();
        if ($proyectos->isEmpty()) {
            $this->warn('No hay propuestas para analizar.');
            return self::SUCCESS;
        }

        $pares = [];
        foreach ($proyectos->groupBy(fn ($p) => optional($p->classGroup)->id_programa) as $grupo) {
            $corpus = $grupo->all();
            foreach ($grupo as $p) {
                foreach (SimilitudService::puntaje($p, $corpus) as $r) {
                    $a = min($p->id, $r['project_id']);
                    $b = max($p->id, $r['project_id']);
                    $key = "{$a}-{$b}";
                    if (isset($pares[$key])) continue;
                    $d = $r['detalles'];
                    $pares[$key] = [
                        'par' => $key,
                        'score' => $r['porcentaje'],
                        'palabras' => $d['palabras'] ?? 0,
                        'caracteres' => $d['caracteres'] ?? 0,
                        'tema' => $d['tema'] ?? 'n/d',
                        'cobertura' => $d['cobertura'] ?? 0,
                        'terminos' => count($d['terminos'] ?? []),
                        'pasajes' => count($d['pasajes'] ?? []),
                    ];
                }
            }
        }

        if (!$pares) {
            $this->warn('No se generaron pares (¿propuestas en distintas fichas/programas?).');
            return self::SUCCESS;
        }

        uasort($pares, fn ($x, $y) => $y['score'] <=> $x['score']);
        $this->table(
            ['Par', 'Score', 'Palabras', 'Caracteres', 'Tema', 'Cobertura', 'Términos', 'Pasajes'],
            array_map(fn ($p) => [
                $p['par'], $p['score'], $p['palabras'], $p['caracteres'],
                $p['tema'], $p['cobertura'], $p['terminos'], $p['pasajes'],
            ], array_values($pares))
        );

        $scores = array_column($pares, 'score');
        sort($scores);
        $this->line('Distribución → min: ' . $scores[0] . ' · mediana: ' . $scores[(int) floor(count($scores) / 2)]
            . ' · max: ' . end($scores));

        $labels = $this->cargarLabels();
        if ($labels) {
            $this->evaluarUmbrales($pares, $labels);
        } else {
            $this->line('Sin etiquetas: usa --labels=archivo.json para métricas y umbral sugerido.');
        }

        return self::SUCCESS;
    }

    private function cargarLabels(): array
    {
        $ruta = $this->option('labels');
        if (!$ruta || !is_file($ruta)) return [];
        $json = json_decode(file_get_contents($ruta), true);
        return is_array($json) ? $json : [];
    }

    private function evaluarUmbrales(array $pares, array $labels): void
    {
        $filas = [];
        $mejor = null;
        for ($t = 0.10; $t <= 0.65 + 1e-9; $t += 0.05) {
            $tp = $fp = $fn = 0;
            foreach ($pares as $key => $p) {
                $real = (int) ($labels[$key] ?? 0);
                $pred = $p['score'] / 100 >= $t ? 1 : 0;
                if ($pred && $real) $tp++;
                elseif ($pred && !$real) $fp++;
                elseif (!$pred && $real) $fn++;
            }
            $prec = ($tp + $fp) > 0 ? $tp / ($tp + $fp) : 0.0;
            $rec = ($tp + $fn) > 0 ? $tp / ($tp + $fn) : 0.0;
            $f1 = ($prec + $rec) > 0 ? 2 * $prec * $rec / ($prec + $rec) : 0.0;
            $filas[] = [number_format($t, 2), $tp, $fp, $fn, number_format($prec, 2), number_format($rec, 2), number_format($f1, 3)];
            if ($mejor === null || $f1 > $mejor['f1']) $mejor = ['t' => $t, 'f1' => $f1, 'prec' => $prec, 'rec' => $rec];
        }

        $this->newLine();
        $this->table(['Umbral', 'TP', 'FP', 'FN', 'Precisión', 'Recall', 'F1'], $filas);

        if ($mejor) {
            $this->info('Mejor umbral por F1: ' . number_format($mejor['t'], 2)
                . ' (F1=' . number_format($mejor['f1'], 3)
                . ', precisión=' . number_format($mejor['prec'], 2)
                . ', recall=' . number_format($mejor['rec'], 2) . ')');
        }
    }
}
