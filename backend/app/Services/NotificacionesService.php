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
        $idUsuario = null;
        if ($project->id_instructor_asignado) {
            $idUsuario = optional(Instructor::find($project->id_instructor_asignado))->id_usuario;
        }
        if (!$idUsuario && $project->id_class_group) {
            $idUsuario = optional(optional(ClassGroup::find($project->id_class_group))->instructor)->id_usuario;
        }
        if (!$idUsuario) return;

        $this->crear($idUsuario, 'Nueva propuesta pendiente de revisión: "' . $project->titulo . '"', 'revision', 'proyecto:' . $project->id);
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

    // Nuevo autoregistro (aprendiz/instructor) → avisa a los admins activos
    // para que revisen el documento y aprueben/rechacen la cuenta.
    public function usuarioPendiente(GeneralUser $usuario): void
    {
        $nombre = trim(($usuario->nombre ?? '') . ' ' . ($usuario->apellido ?? '')) ?: ($usuario->correo ?? 'Un usuario');
        $rol = $usuario->rol === 'instructor' ? 'instructor' : 'aprendiz';

        GeneralUser::where('rol', 'admin')->where('estado', true)->get()->each(
            fn (GeneralUser $admin) => $this->crear(
                $admin->id,
                'Nuevo ' . $rol . ' pendiente de verificación: ' . $nombre,
                'sistema',
                'usuario:' . $usuario->id
            )
        );
    }

    // Verificación resuelta → avisa al usuario (aprendiz o instructor).
    public function verificacionResuelta(GeneralUser $usuario, bool $aprobado, ?string $motivo = null): void
    {
        $titulo = $aprobado
            ? 'Tu cuenta fue verificada. Ya puedes ingresar a ProyecTwin.'
            : 'Tu solicitud fue rechazada.' . ($motivo ? ' Motivo: ' . $motivo : '');

        $this->crear($usuario->id, $titulo, 'sistema', 'perfil');
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
