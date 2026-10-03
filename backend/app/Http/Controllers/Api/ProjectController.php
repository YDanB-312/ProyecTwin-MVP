<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Notification;
use App\Models\Project;
use App\Models\Similarity;
use App\Services\NotificacionesService;
use App\Support\Pagina;
use App\Http\Requests\StoreProjectRequest;
use App\Http\Requests\UpdateProjectRequest;
use App\Http\Controllers\Controller;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class ProjectController extends Controller
{
    public function index(Request $request)
    {
        $query = Project::included()
            ->paraUsuario($request->user())
            ->search($request->query('search'))
            ->byEstado($request->query('estado'))
            ->byFicha($request->query('ficha_id'))
            ->byPrograma($request->query('programa'));

        // Con `?page=` responde paginado; sin él, la lista completa.
        return Pagina::aplicar($query, $request, 15);
    }

    public function store(StoreProjectRequest $request)
    {
        $usuario = $request->user();
        $rol = optional($usuario)->rol;
        $datos = $request->all();

        if ($rol === 'aprendiz') {
            // La propuesta vive en la ficha ACTUAL del aprendiz (activa) y con
            // el instructor de esa ficha: no se confía en el request.
            $aprendiz = Apprentice::where('id_usuario', $usuario->id)->first();
            if (!$aprendiz || !$aprendiz->id_class_group) {
                return response()->json(['message' => 'Únete a una ficha para registrar propuestas.'], 422);
            }
            $ficha = ClassGroup::find($aprendiz->id_class_group);
            if (!$ficha || $ficha->estado !== 'activo') {
                return response()->json(['message' => 'Tu ficha no está activa para recibir propuestas.'], 422);
            }
            $datos['id_class_group'] = $ficha->id;
            $datos['id_instructor_asignado'] = $ficha->id_instructor;
            $datos['id_creador'] = $usuario->id;
        } elseif ($rol === 'admin') {
            // Un admin puede registrarla a nombre de otro, pero el autor debe
            // ser un aprendiz (integridad de dominio).
            if (!empty($datos['id_creador'])) {
                $creador = GeneralUser::find($datos['id_creador']);
                if (!$creador || $creador->rol !== 'aprendiz') {
                    return response()->json(['message' => 'La propuesta debe pertenecer a un aprendiz.'], 422);
                }
            } else {
                $datos['id_creador'] = $usuario->id;
            }
        } else {
            // Solo aprendices (o un admin a nombre de uno) pueden ser autores:
            // una propuesta sin autor aprendiz rompe el dominio del motor.
            return response()->json(['message' => 'Solo un aprendiz puede registrar propuestas.'], 403);
        }

        // Alta atómica: propuesta + equipo + notificación (todo o nada).
        $item = DB::transaction(function () use ($datos) {
            $item = Project::create($datos);
            // El creador forma parte del equipo: se registra en el pivote para que
            // los conteos y el listado de integrantes sean consistentes.
            $this->asegurarCreadorEnEquipo($item);
            // Avisa al instructor a cargo que hay una propuesta pendiente de revisión.
            app(NotificacionesService::class)->revisionPropuesta($item);
            return $item;
        });

        // El motor corre en el servidor al radicar (el cliente deja de ser la
        // fuente): si falla, la propuesta igual queda registrada.
        try {
            app(SimilarityController::class)->detectarProyecto($item);
        } catch (\Throwable $e) {
            // El análisis puede recalcularse después.
        }

        return response()->json($item, 201);
    }

    public function show(Request $request, $id)
    {
        $user = $request->user();

        // El detalle completo solo si la propuesta está dentro de su alcance.
        $visible = Project::where('id', $id)->paraDetalle($user)->exists();

        // Un aprendiz puede abrir (solo lectura) la CONTRAPARTE de una similitud
        // que puede ver, aunque sea de otra ficha: necesita saber cuál es la
        // propuesta con la que se le compara.
        if (!$visible && $user && $user->rol === 'aprendiz') {
            $visible = Similarity::where(function (Builder $q) use ($id) {
                $q->where('id_proyecto_1', $id)->orWhere('id_proyecto_2', $id);
            })->visibleDetalle($user)->exists();
        }

        if (!$visible) {
            return response()->json(['message' => 'No tienes acceso a esta propuesta.'], 403);
        }

        return Project::included()->findOrFail($id);
    }

    public function update(UpdateProjectRequest $request, Project $project)
    {
        $usuario = $request->user();
        $rol = optional($usuario)->rol;

        // Autorización por rol: aprendiz solo las suyas; instructor solo las de
        // su ficha o asignadas; admin cualquiera.
        if ($rol === 'aprendiz') {
            if (!$this->esPropiaDelAprendiz($project, $usuario)) {
                return response()->json(['message' => 'No puedes editar una propuesta que no es tuya.'], 403);
            }
            // Solo lectura si ya no perteneces a la ficha (o está finalizada).
            if (!$project->puedeEscribir($usuario)) {
                return response()->json([
                    'message' => 'Solo lectura: ya no perteneces a la ficha de esta propuesta.',
                ], 403);
            }
        } elseif ($rol === 'instructor') {
            if (!$this->esInstructorDeLaPropuesta($project, $usuario)) {
                return response()->json(['message' => 'No puedes editar una propuesta fuera de tus fichas.'], 403);
            }
        } elseif ($rol !== 'admin') {
            return response()->json(['message' => 'Sin permiso para editar propuestas.'], 403);
        }

        $datos = $request->all();

        // La propuesta no cambia de ficha ni de creador por esta vía
        // (trazabilidad histórica); se conservan los valores actuales.
        unset($datos['id_class_group'], $datos['id_creador']);
        $datos['id_creador'] = $project->id_creador;
        // El instructor asignado solo lo reasigna un admin (evita que el aprendiz
        // o el equipo "secuestren" la asignación).
        if ($rol !== 'admin') {
            unset($datos['id_instructor_asignado']);
        }

        // El aprendiz no aprueba/rechaza: al reenviar una rechazada vuelve a
        // pendiente para una nueva revisión del instructor.
        if ($rol === 'aprendiz') {
            $datos['estado'] = 'pendiente';
        }

        // Contenido de una propuesta APROBADA editado por staff → nueva revisión.
        if ($rol !== 'aprendiz' && $project->estado === 'aprobado') {
            $campos = ['titulo', 'resumen', 'palabras_clave', 'area_aplicacion', 'objetivo_general', 'objetivos_especificos'];
            foreach ($campos as $c) {
                if (!$request->has($c)) continue;
                if (json_encode($request->input($c)) !== json_encode($project->{$c})) {
                    $datos['estado'] = 'pendiente';
                    break;
                }
            }
        }

        $estadoAnterior = $project->estado;
        $project->update($datos);

        // Aprobar/rechazar queda en la bitácora (decisión con efecto académico).
        if ($rol !== 'aprendiz' && $estadoAnterior !== $project->estado) {
            \App\Support\Auditoria::registrar('revisar_propuesta', 'projects', $project->id, [
                'de' => $estadoAnterior,
                'a' => $project->estado,
                'titulo' => $project->titulo,
            ]);

            // La reacción al creador la genera el servidor (el cliente ya no la crea).
            app(NotificacionesService::class)->revisionResuelta($project, $project->estado);
        }

        // Al aprobar, el motor corre en el servidor (nuevos pares contra el corpus).
        if ($rol !== 'aprendiz' && $estadoAnterior !== 'aprobado' && $project->estado === 'aprobado') {
            try {
                app(SimilarityController::class)->detectarProyecto($project);
            } catch (\Throwable $e) {
                // El análisis puede recalcularse después; no impide aprobar.
            }
        }

        return $project;
    }

    public function destroy(Request $request, Project $project)
    {
        // Autorización vía Policy (misma regla: creador aprendiz en ficha activa o admin).
        if (!\Illuminate\Support\Facades\Gate::allows('delete', $project)) {
            return response()->json(['message' => 'No puedes eliminar esta propuesta.'], 403);
        }

        $project->delete();
        // Evita notificaciones con enlace a una propuesta ya inexistente.
        Notification::where('enlace', 'proyecto:' . $project->id)->delete();
        return $project;
    }

    // ---------------------------------------------------------------- Internos

    // ¿La propuesta es del aprendiz (creador o integrante del equipo)?
    private function esPropiaDelAprendiz(Project $project, $usuario): bool
    {
        if ((int) $project->id_creador === (int) $usuario->id) return true;

        $filaAprendiz = Apprentice::where('id_usuario', $usuario->id)->first();
        if (!$filaAprendiz) return false;

        return ApprenticeProject::where('id_proyecto', $project->id)
            ->where('id_aprendiz', $filaAprendiz->id)
            ->exists();
    }

    // ¿El instructor está asignado a la propuesta o es el de su ficha?
    private function esInstructorDeLaPropuesta(Project $project, $usuario): bool
    {
        $instructor = Instructor::where('id_usuario', $usuario->id)->first();
        if (!$instructor) return false;

        if ((int) $project->id_instructor_asignado === (int) $instructor->id) return true;

        return (int) optional($project->classGroup)->id_instructor === (int) $instructor->id;
    }

    // Inserta la fila del creador en el pivote si aún no está.
    private function asegurarCreadorEnEquipo(Project $project): void
    {
        $filaAprendiz = Apprentice::where('id_usuario', $project->id_creador)->first();
        if (!$filaAprendiz) return;

        ApprenticeProject::firstOrCreate([
            'id_proyecto' => $project->id,
            'id_aprendiz' => $filaAprendiz->id,
        ]);
    }

}
