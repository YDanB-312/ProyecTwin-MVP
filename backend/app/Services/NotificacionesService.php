<?php

namespace App\Services;

use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Notification;
use App\Models\Project;

// Punto único de creación de notificaciones (las "reacciones" del sistema).
//
// Lo usan los controllers, el motor de similitud y el seeder, para que una
// misma acción genere SIEMPRE la misma notificación (sin duplicar lógica).
class NotificacionesService
{
    // Helper base: crea una notificación para un usuario.
    public function crear(?int $idUsuario, string $titulo, string $tipo, ?string $enlace = null): void
    {
        if (!$idUsuario) return;

        Notification::create([
            'titulo' => mb_substr($titulo, 0, 250),
            'tipo' => $tipo,
            'enlace' => $enlace,
            'leida' => false,
            'fecha' => now()->toDateString(),
            'id_usuario' => $idUsuario,
        ]);
    }

    // Nueva propuesta → avisa al instructor asignado (o al de la ficha).
    public function revisionPropuesta(Project $project): void
    {
        $idUsuario = $this->instructorDe($project);
        if (!$idUsuario) return;

        $this->crear($idUsuario, 'Nueva propuesta pendiente de revisión: "' . $project->titulo . '"', 'revision', 'proyecto:' . $project->id);
    }

    // Propuesta reenviada (tras correcciones) → nueva revisión del instructor.
    public function propuestaReenviada(Project $project): void
    {
        $idUsuario = $this->instructorDe($project);
        if (!$idUsuario) return;

        $this->crear($idUsuario, 'Propuesta reenviada para revisión: "' . $project->titulo . '"', 'revision', 'proyecto:' . $project->id);
    }

    // Similitudes detectadas al enviar → el instructor las revisa con la propuesta.
    public function similitudesEncontradas(Project $project, int $total): void
    {
        $idUsuario = $this->instructorDe($project);
        if (!$idUsuario) return;

        $this->crear(
            $idUsuario,
            'La propuesta "' . $project->titulo . '" tiene ' . $total . ' coincidencia(s) por revisar.',
            'similitud',
            'proyecto:' . $project->id
        );
    }

    // Propuesta aprobada → avisa al creador.
    public function propuestaAprobada(Project $project): void
    {
        $this->crear($project->id_creador, 'Tu propuesta "' . $project->titulo . '" fue aprobada.', 'revision', 'proyecto:' . $project->id);
    }

    // Propuesta rechazada → avisa al creador (con la observación si existe).
    public function propuestaRechazada(Project $project, ?string $observacion = null): void
    {
        $titulo = 'Tu propuesta "' . $project->titulo . '" fue rechazada.';
        if ($observacion) {
            $titulo .= ' Observación: ' . $observacion;
        }
        $this->crear($project->id_creador, $titulo, 'revision', 'proyecto:' . $project->id);
    }

    // Resuelve el usuario instructor responsable de la propuesta (asignado o de
    // la ficha). Punto único para las notificaciones al instructor.
    private function instructorDe(Project $project): ?int
    {
        if ($project->id_instructor_asignado) {
            $id = optional(Instructor::find($project->id_instructor_asignado))->id_usuario;
            if ($id) return $id;
        }
        if ($project->id_class_group) {
            return optional(optional(ClassGroup::find($project->id_class_group))->instructor)->id_usuario;
        }
        return null;
    }

    // Similitud detectada → avisa al creador de la propuesta.
    public function similitud(Project $propio, int $otroId, int $porcentaje): void
    {
        $otro = Project::find($otroId);
        $titulo = "Similitud del {$porcentaje}% detectada entre '{$propio->titulo}' y '" . ($otro->titulo ?? 'otra propuesta') . "'";

        $this->crear($propio->id_creador, $titulo, 'similitud', 'proyecto:' . $propio->id);
    }

    // Movimiento de ficha → avisa al instructor de esa ficha (si tiene uno).
    public function fichaMovimientoInstructor(?ClassGroup $ficha, string $titulo, ?string $enlace = null): void
    {
        $this->crear(optional($ficha?->instructor)->id_usuario, $titulo, 'sistema', $enlace);
    }

    // Soporte resuelto → avisa al solicitante que el admin respondió.
    public function soporteResuelto(BugReport $reporte): void
    {
        $atendida = $reporte->estado !== 'rechazado';
        $titulo = 'Tu solicitud de soporte #' . $reporte->id . ' fue ' . ($atendida ? 'atendida' : 'rechazada') . '.';

        $this->crear($reporte->id_usuario, $titulo, 'sistema', 'reporte:' . $reporte->id);
    }

    // Nuevo reporte de falla → avisa a cada admin activo.
    public function reporteFalla(BugReport $reporte): void
    {
        GeneralUser::where('rol', 'admin')->where('estado', true)->get()->each(
            fn (GeneralUser $admin) => $this->crear(
                $admin->id,
                'Nuevo reporte de falla: "' . ($reporte->titulo ?: 'Sin título') . '"',
                'sistema',
                'reporte:' . $reporte->id
            )
        );
    }
}
