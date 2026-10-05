<?php

namespace App\Support;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Notification;
use App\Models\Project;
use App\Models\TrainingProgram;
use App\Services\NotificacionesService;
use Illuminate\Support\Facades\DB;

// Borrado en cascada para el administrador (sin restricciones): elimina la
// entidad y todo lo dependiente dentro de una transacción, respetando el orden
// que exigen las claves foráneas. La responsabilidad del impacto la asume el
// admin en la confirmación del cliente.
class BorradoCascada
{
    // Ficha: borra sus propuestas (arrastran similitudes, observaciones y equipo)
    // y luego la ficha. Los aprendices se conservan sin ficha (la FK cascade los
    // borraría y perderían sus equipos en otras propuestas).
    public static function ficha(ClassGroup $ficha): void
    {
        DB::transaction(function () use ($ficha) {
            $aprendices = Apprentice::where('id_class_group', $ficha->id)->get();
            $idUsuarios = $aprendices->pluck('id_usuario');
            $titulo = 'Tu ficha "' . $ficha->nombre . '" fue eliminada. Quedaste sin ficha.';

            // Las propuestas de la ficha se eliminan: sus notificaciones con
            // enlace al proyecto quedarían huérfanas.
            $proyectos = Project::where('id_class_group', $ficha->id)->pluck('id');
            Notification::whereIn('enlace', $proyectos->map(fn ($id) => 'proyecto:' . $id))->delete();

            Project::where('id_class_group', $ficha->id)->get()->each->delete();
            Notification::where('enlace', 'ficha:' . $ficha->id)->delete();

            // Desvincular antes de borrar la ficha (conserva fila y pivotes).
            foreach ($aprendices as $aprendiz) {
                $aprendiz->update(['id_class_group' => null, 'id_programa' => null]);
            }

            $ficha->delete();

            foreach ($idUsuarios as $idUsuario) {
                app(NotificacionesService::class)->crear($idUsuario, $titulo, 'sistema');
            }
        });
    }

    // Programa: borra sus fichas (con su cascada) y los aprendices del programa,
    // y luego el programa. Los aprendices se capturan antes porque ficha() los
    // desvincula del programa.
    public static function programa(TrainingProgram $programa): void
    {
        DB::transaction(function () use ($programa) {
            $aprendices = Apprentice::where('id_programa', $programa->id)->pluck('id');
            ClassGroup::where('id_programa', $programa->id)->get()->each(fn ($f) => self::ficha($f));
            Apprentice::whereIn('id', $aprendices)->delete();
            $programa->delete();
        });
    }

    // Red de conocimiento: borra sus programas (con su cascada) y luego la red.
    public static function red(KnowledgeNetwork $red): void
    {
        DB::transaction(function () use ($red) {
            TrainingProgram::where('knowledge_network_id', $red->id)->get()->each(fn ($p) => self::programa($p));
            $red->delete();
        });
    }

    // Usuario: si es instructor con fichas, se borran esas fichas (con su
    // cascada); luego el borrado del usuario arrastra propuestas, notificaciones,
    // comentarios, reportes y filas de perfil (FK cascade).
    public static function usuario(GeneralUser $usuario): void
    {
        DB::transaction(function () use ($usuario) {
            $instructor = Instructor::where('id_usuario', $usuario->id)->first();
            if ($instructor) {
                ClassGroup::where('id_instructor', $instructor->id)->get()->each(fn ($f) => self::ficha($f));
            }
            // Sus propias propuestas se eliminan en cascada: limpia los avisos
            // que apuntaban a ellas.
            $proyectos = Project::where('id_creador', $usuario->id)->pluck('id');
            Notification::whereIn('enlace', $proyectos->map(fn ($id) => 'proyecto:' . $id))->delete();

            // Reportes del usuario: la FK los borra; limpia los avisos a admins.
            $reportes = \App\Models\BugReport::where('id_usuario', $usuario->id)->pluck('id');
            Notification::whereIn('enlace', $reportes->map(fn ($id) => 'reporte:' . $id))->delete();

            // Tokens de sesión y de recuperación (no tienen FK al usuario).
            $usuario->tokens()->delete();
            DB::table('password_reset_tokens')
                ->where('email', strtolower((string) $usuario->correo))
                ->delete();

            $usuario->delete();
        });
    }
}
