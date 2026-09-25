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
use Illuminate\Support\Facades\DB;

// Borrado en cascada para el administrador (sin restricciones): elimina la
// entidad y todo lo dependiente dentro de una transacción, respetando el orden
// que exigen las claves foráneas. La responsabilidad del impacto la asume el
// admin en la confirmación del cliente.
class BorradoCascada
{
    // Ficha: borra sus propuestas (arrastran similitudes, observaciones y equipo)
    // y luego la ficha (la FK de aprendices es cascade).
    public static function ficha(ClassGroup $ficha): void
    {
        DB::transaction(function () use ($ficha) {
            Project::where('id_class_group', $ficha->id)->get()->each->delete();
            Notification::where('enlace', 'ficha:' . $ficha->id)->delete();
            $ficha->delete();
        });
    }

    // Programa: borra sus fichas (con su cascada) y los aprendices del programa,
    // y luego el programa.
    public static function programa(TrainingProgram $programa): void
    {
        DB::transaction(function () use ($programa) {
            ClassGroup::where('id_programa', $programa->id)->get()->each(fn ($f) => self::ficha($f));
            Apprentice::where('id_programa', $programa->id)->delete();
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
            $usuario->delete();
        });
    }
}
