<?php

namespace App\Http\Controllers\Api;

use App\Models\Project;
use App\Models\Similarity;
use App\Models\MotorConfig;
use App\Models\Notification;
use App\Services\SimilitudService;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class SimilarityController extends Controller
{
    // ---------------------------------------------------------------- CRUD

    public function index(Request $request)
    {
        return Similarity::included()
            ->search($request->query('search'))
            ->byTrainingCenter($request->query('training_center_id'))
            ->byFicha($request->query('ficha_id'))
            ->byPrograma($request->query('programa'))
            ->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'porcentaje' => 'required|numeric',
            'detalles' => 'nullable|array',
            'fecha' => 'nullable|date',
            'id_proyecto_1' => 'required|exists:projects,id',
            'id_proyecto_2' => 'required|exists:projects,id',
        ]);

        $item = Similarity::create($request->all());
        return response()->json($item, 201);
    }

    public function show($id)
    {
        return Similarity::included()->findOrFail($id);
    }

    public function update(Request $request, Similarity $similarity)
    {
        $request->validate([
            'porcentaje' => 'required|numeric',
            'detalles' => 'nullable|array',
            'fecha' => 'nullable|date',
            'id_proyecto_1' => 'required|exists:projects,id',
            'id_proyecto_2' => 'required|exists:projects,id',
        ]);

        $similarity->update($request->all());
        return $similarity;
    }

    public function destroy(Similarity $similarity)
    {
        $similarity->delete();
        return $similarity;
    }

    // ---------------------------------------------------------------- Motor

    // Detecta coincidencias de UNA propuesta contra el corpus vigente de su
    // mismo programa (pendientes + aprobadas dentro de la ventana) y
    // sincroniza la tabla `similarities`. Devuelve el total vigente del par.
    public function detect(Request $request)
    {
        $request->validate(['id_proyecto' => 'required|exists:projects,id']);
        $propio = Project::with('classGroup')->findOrFail($request->id_proyecto);
        $umbral = (float) ($this->config()->umbral ?? 0.2);
        $meses = (int) ($this->config()->meses ?? 12);

        $corpus = $this->corpus($propio, $meses);
        $pares = SimilitudService::puntaje($propio, $corpus);

        $creadas = 0;
        foreach ($pares as $par) {
            $vigente = $par['score'] >= $umbral;
            $existente = $this->buscarPar($propio->id, $par['project_id']);
            if ($vigente) {
                if ($existente) {
                    $existente->update(['porcentaje' => $par['porcentaje'], 'detalles' => $par['detalles']]);
                } else {
                    $this->guardarPar($propio->id, $par['project_id'], $par['porcentaje'], $par['detalles']);
                    $creadas++;
                    $this->notificar($propio, $par['project_id'], $par['porcentaje']);
                }
            } elseif ($existente) {
                $existente->delete();
            }
        }

        return response()->json(['detectadas' => $creadas]);
    }

    // Recalibra toda la base con el umbral/ventana vigentes: purga los pares
    // que ya no cumplen y re-ejecuta la detección sobre cada propuesta vigente.
    public function recalculate()
    {
        $umbral = (float) ($this->config()->umbral ?? 0.2);
        $meses = (int) ($this->config()->meses ?? 12);

        $antes = Similarity::count();
        Similarity::all()->each(function (Similarity $s) use ($umbral, $meses) {
            if (!$this->parVigente($s, $umbral, $meses)) $s->delete();
        });
        $eliminadas = $antes - Similarity::count();

        $creadas = 0;
        foreach (Project::with('classGroup')->where('estado', '!=', 'rechazado')->get() as $p) {
            $corpus = $this->corpus($p, $meses);
            foreach (SimilitudService::puntaje($p, $corpus) as $par) {
                if ($par['score'] < $umbral) continue;
                if ($this->buscarPar($p->id, $par['project_id'])) continue;
                $this->guardarPar($p->id, $par['project_id'], $par['porcentaje'], $par['detalles']);
                $creadas++;
                $this->notificar($p, $par['project_id'], $par['porcentaje']);
            }
        }

        return response()->json(['eliminadas' => $eliminadas, 'creadas' => $creadas]);
    }

    // ---------------------------------------------------------------- Demo pública

    // Compara un texto libre contra el corpus vigente sin persistir nada.
    // La landing lo usa para que cualquier visitante pruebe el motor.
    // Devuelve solo títulos y porcentajes: nunca autores ni resúmenes.
    public function demo(Request $request)
    {
        $request->validate(['texto' => 'required|string|min:3|max:300']);

        $config = $this->config();
        $umbral = (float) ($config->umbral ?? 0.2);
        $meses = (int) ($config->meses ?? 12);

        // Proyecto "sonda" virtual (id centinela 0; el corpus usa ids >= 1).
        $sonda = new Project([
            'titulo' => $request->texto,
            'palabras_clave' => $request->texto,
            'area_aplicacion' => '',
            'resumen' => '',
        ]);
        $sonda->id = 0;

        $corpus = Project::where('estado', '!=', 'rechazado')
            ->where('created_at', '>=', now()->subMonths($meses))
            ->get();

        $pares = SimilitudService::puntaje($sonda, array_merge([$sonda], $corpus->all()));
        usort($pares, fn ($a, $b) => $b['score'] <=> $a['score']);

        $titulo = $corpus->pluck('titulo', 'id');
        $coincidencias = array_map(fn ($p) => [
            'id' => $p['project_id'],
            'titulo' => $titulo[$p['project_id']] ?? 'Propuesta',
            'porcentaje' => $p['porcentaje'],
        ], array_slice($pares, 0, 3));

        return response()->json([
            'umbral' => $umbral,
            'meses' => $meses,
            'total' => $corpus->count(),
            'sobre' => count(array_filter($pares, fn ($p) => $p['score'] >= $umbral)),
            'coincidencias' => $coincidencias,
        ]);
    }

    // ---------------------------------------------------------------- Internos

    private function config(): MotorConfig
    {
        return MotorConfig::firstOrCreate([], ['umbral' => 0.2, 'meses' => 12]);
    }

    // Corpus del mismo programa: pendientes/aprobadas dentro de la ventana.
    private function corpus(Project $propio, int $meses): array
    {
        $programaId = optional($propio->classGroup)->id_programa;
        $desde = now()->subMonths($meses);

        return Project::with('classGroup')
            ->where('id', '!=', $propio->id)
            ->where('estado', '!=', 'rechazado')
            ->where('created_at', '>=', $desde)
            ->whereHas('classGroup', fn ($q) => $q->where('id_programa', $programaId))
            ->get()
            ->prepend($propio)   // incluir la propia para que exista en los vectores
            ->all();
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

    private function parVigente(Similarity $s, float $umbral, int $meses): bool
    {
        if (($s->porcentaje / 100) < $umbral) return false;
        $p1 = Project::with('classGroup')->find($s->id_proyecto_1);
        $p2 = Project::with('classGroup')->find($s->id_proyecto_2);
        if (!$p1 || !$p2) return false;
        if ($p1->estado === 'rechazado' || $p2->estado === 'rechazado') return false;
        if (optional($p1->classGroup)->id_programa !== optional($p2->classGroup)->id_programa) return false;
        $desde = now()->subMonths($meses);
        return $p1->created_at >= $desde || $p2->created_at >= $desde;
    }

    // Avisa al creador de la propuesta que se le detectó una coincidencia.
    private function notificar(Project $propio, int $otroId, int $porcentaje): void
    {
        $otro = Project::find($otroId);
        Notification::create([
            'titulo' => "Similitud del {$porcentaje}% detectada entre '{$propio->titulo}' y '" . ($otro->titulo ?? 'otra propuesta') . "'",
            'tipo' => 'similitud',
            'enlace' => 'proyecto:' . $propio->id,
            'leida' => false,
            'fecha' => now()->toDateString(),
            'id_usuario' => $propio->id_creador,
        ]);
    }
}
