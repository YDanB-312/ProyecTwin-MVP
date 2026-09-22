<?php

namespace App\Console\Commands;

use App\Models\GeneralUser;
use App\Models\Instructor;
use Illuminate\Console\Command;

// Backfill: crea el perfil de instructor de toda cuenta con rol instructor que
// no lo tenga (útil para usuarios creados antes de la provisión automática).
class BackfillInstructorProfiles extends Command
{
    protected $signature = 'perfiles:docentes';

    protected $description = 'Crea el perfil de instructor para cuentas con rol instructor que no lo tengan.';

    public function handle(): int
    {
        $creados = 0;

        GeneralUser::where('rol', 'instructor')->get()->each(function (GeneralUser $usuario) use (&$creados) {
            if (Instructor::where('id_usuario', $usuario->id)->exists()) return;

            Instructor::create([
                'id_usuario' => $usuario->id,
                'fecha_ingreso' => now()->toDateString(),
            ]);
            $creados++;
        });

        $this->info("Perfiles de instructor creados: {$creados}");

        return self::SUCCESS;
    }
}
