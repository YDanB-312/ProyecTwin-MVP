<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\Instructor;
use App\Models\Project;
use App\Models\Similarity;
use App\Models\MotorConfig;
use App\Models\Notification;
use App\Services\SimilitudService;
use App\Http\Controllers\Controller;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Http\Request;

class SimilarityController extends Controller
{
    // ---------------------------------------------------------------- CRUD

    public function index(Request $request)
    {
        $user = $request->user();

        // Vista de UNA propuesta (revisión, análisis, detalle): pares de esa
        // propuesta donde la CONTRAPARTE está aprobada (regla de perspectiva).
        $proyectoId = $request->query('proyecto_id');
        if ($proyectoId) {
            $visible = Project::where('id', $proyectoId)->paraDetalle($user)->exists();
            if (!$visible) {
                return response()->json(['message' => 'No tienes acceso a esa propuesta.'], 403);
            }

            return Similarity::included()
                ->where(function (Builder $q) use ($proyectoId) {
                    $q->where('id_proyecto_1', $proyectoId)->orWhere('id_proyecto_2', $proyectoId);
                })
                ->contraparteAprobada((int) $proyectoId)
                ->get();
        }

        return Similarity::included()
            ->paraUsuario($user)
            ->relatedTo($request->query('related_to'))
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

    public function show(Request $request, $id)
    {
        $item = Similarity::included()->findOrFail($id);

        // Mismo criterio de alcance (perspectiva del rol).
        $visible = Similarity::where('id', $item->id)
            ->visibleDetalle($request->user())
            ->exists();
        if (!$visible) {
            return response()->json(['message' => 'No tienes acceso a esta similitud.'], 403);
        }

        return $item;
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

    // Detecta coincidencias de UNA propuesta contra el corpus de APROBADAS del
    // mismo programa (dentro de la ventana) y sincroniza la tabla
    // `similarities`. Devuelve cuántos pares nuevos quedaron.
    public function detect(Request $request)
    {
        $request->validate(['id_proyecto' => 'required|exists:projects,id']);
        $propio = Project::with('classGroup')->findOrFail($request->id_proyecto);

        // Solo el dueño (aprendiz/equipo), su instructor o un admin disparan el motor.
        if (!$this->puedeDetectar($request->user(), $propio)) {
            return response()->json(['message' => 'No puedes analizar una propuesta que no es tuya.'], 403);
        }

        $umbral = (float) ($this->config()->umbral ?? 0.2);
        $meses = (int) ($this->config()->meses ?? 12);

        // Una rechazada no participa: se eliminan sus pares y no se compara.
        if ($propio->estado === 'rechazado') {
            $this->purgarPares($propio->id);
            return response()->json(['detectadas' => 0]);
        }

        $corpus = $this->corpus($propio, $meses);
        $pares = SimilitudService::puntaje($propio, $corpus);

        // Ids que quedan vigentes para esta propuesta.
        $vigentes = [];
        $creadas = 0;
        foreach ($pares as $par) {
            if ($par['score'] < $umbral) continue;
            $vigentes[] = $par['project_id'];
            $existente = $this->buscarPar($propio->id, $par['project_id']);
            if ($existente) {
                $existente->update(['porcentaje' => $par['porcentaje'], 'detalles' => $par['detalles']]);
            } else {
                $this->guardarPar($propio->id, $par['project_id'], $par['porcentaje'], $par['detalles']);
                $creadas++;
                $this->notificar($propio, $par['project_id'], $par['porcentaje']);
            }
        }

        // Limpia pares obsoletos (bajaron del umbral o ya no aplican reglas).
        $this->purgarParesObsoletos($propio->id, $vigentes);

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

        \App\Support\Auditoria::registrar('recalibrar_motor', 'similarities', null, [
            'eliminadas' => $eliminadas,
            'creadas' => $creadas,
            'umbral' => $umbral,
            'meses' => $meses,
        ]);

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

        $corpus = Project::where('estado', 'aprobado')
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

    // Admin cualquiera; aprendiz su propuesta (creador o equipo); instructor su
    // ficha o la asignada. Evita que se dispare el motor sobre propuestas ajenas.
    private function puedeDetectar($user, Project $project): bool
    {
        if (!$user) return false;
        if ($user->rol === 'admin') return true;
        if ((int) $project->id_creador === (int) $user->id) return true;

        $aprendiz = Apprentice::where('id_usuario', $user->id)->first();
        if ($aprendiz && ApprenticeProject::where('id_proyecto', $project->id)
            ->where('id_aprendiz', $aprendiz->id)->exists()) {
            return true;
        }

        $instructor = Instructor::where('id_usuario', $user->id)->first();
        if ($instructor) {
            return (int) $project->id_instructor_asignado === (int) $instructor->id
                || (int) optional($project->classGroup)->id_instructor === (int) $instructor->id;
        }

        return false;
    }

    private function config(): MotorConfig
    {
        return MotorConfig::firstOrCreate([], ['umbral' => 0.2, 'meses' => 12]);
    }

    // Corpus de comparación: propuestas APROBADAS del mismo programa dentro de
    // la ventana. La propia se incluye solo para que exista en los vectores.
    private function corpus(Project $propio, int $meses): array
    {
        $programaId = optional($propio->classGroup)->id_programa;
        $desde = now()->subMonths($meses);

        return Project::with('classGroup')
            ->where('id', '!=', $propio->id)
            ->where('estado', 'aprobado')
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

    // Un par es válido si respeta umbral, mismo programa y ventana, y además
    // la referencia es una propuesta APROBADA (al menos uno de los dos).
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

    // Elimina todos los pares de una propuesta (p. ej. al rechazarla).
    private function purgarPares(int $projectId): void
    {
        Similarity::where('id_proyecto_1', $projectId)->orWhere('id_proyecto_2', $projectId)->delete();
    }

    // Elimina los pares de la propuesta que no estén en la lista de vigentes.
    private function purgarParesObsoletos(int $projectId, array $vigentes): void
    {
        $vigentes = array_map('intval', $vigentes);
        Similarity::where('id_proyecto_1', $projectId)->orWhere('id_proyecto_2', $projectId)
            ->get()
            ->each(function (Similarity $s) use ($projectId, $vigentes) {
                $otro = (int) $s->id_proyecto_1 === $projectId ? (int) $s->id_proyecto_2 : (int) $s->id_proyecto_1;
                if (!in_array($otro, $vigentes, true)) $s->delete();
            });
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
