<?php

namespace Database\Seeders;

use App\Models\Apprentice;
use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Project;
use App\Models\TrainingCenter;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

// Datos de VOLUMEN para probar listados, paginación y filtros con muchas filas.
// Se aplica SOBRE el seed base (no lo reemplaza). Es determinista.
class VolumenSeeder extends Seeder
{
    public function run(): void
    {
        $marca = uniqid();

        // 2 centros nuevos.
        $centros = [];
        for ($i = 1; $i <= 2; $i++) {
            $centros[] = TrainingCenter::create([
                'name' => "Centro Volumen {$i} {$marca}",
                'city' => 'Popayán',
            ]);
        }

        // 5 instructores.
        $instructores = [];
        for ($i = 1; $i <= 5; $i++) {
            $u = GeneralUser::create([
                'nombre' => "Instructor{$i}",
                'apellido' => "Volumen {$marca}",
                'correo' => "instructor.volumen.{$i}.{$marca}@sena.edu.co",
                'password' => Hash::make('123456'),
                'rol' => 'instructor',
                'estado' => true,
            ]);
            $instructores[] = Instructor::create([
                'fecha_ingreso' => '2024-01-01',
                'id_usuario' => $u->id,
            ]);
        }

        // Una ficha por instructor, repartidas entre los centros nuevos.
        $fichas = [];
        foreach ($instructores as $idx => $ins) {
            $fichas[] = ClassGroup::create([
                'codigo' => "vol-{$idx}-{$marca}",
                'numero' => (string) (9000 + $idx),
                'nombre' => "Ficha Volumen {$idx} {$marca}",
                'estado' => 'activo',
                'id_programa' => 1, // ADSO
                'id_instructor' => $ins->id,
                'training_center_id' => $centros[$idx % count($centros)]->id,
            ]);
        }

        // 40 aprendices, cada uno con 2 propuestas.
        for ($i = 1; $i <= 40; $i++) {
            $u = GeneralUser::create([
                'nombre' => "Aprendiz{$i}",
                'apellido' => "Volumen {$marca}",
                'correo' => "aprendiz.volumen.{$i}.{$marca}@soy.sena.edu.co",
                'password' => Hash::make('123456'),
                'rol' => 'aprendiz',
                'estado' => true,
            ]);

            $ficha = $fichas[$i % count($fichas)];
            Apprentice::create([
                'codigo' => 'VOL-' . str_pad((string) $i, 3, '0', STR_PAD_LEFT) . '-' . substr($marca, -3),
                'id_usuario' => $u->id,
                'id_class_group' => $ficha->id,
                'id_programa' => $ficha->id_programa,
            ]);

            for ($j = 0; $j < 2; $j++) {
                Project::create([
                    'titulo' => "Propuesta Volumen {$i}-{$j} {$marca}",
                    'resumen' => 'Propuesta generada para pruebas de volumen y paginación en los listados institucionales del sistema.',
                    'palabras_clave' => 'volumen, prueba, paginación, listados',
                    'area_aplicacion' => 'Tecnología e Informática',
                    'objetivo_general' => 'Validar el comportamiento de los listados con muchas filas.',
                    'objetivos_especificos' => ['Generar datos masivos.', 'Verificar paginación y filtros.'],
                    'estado' => $j === 0 ? 'pendiente' : 'aprobado',
                    'id_creador' => $u->id,
                    'id_instructor_asignado' => $ficha->id_instructor,
                    'id_class_group' => $ficha->id,
                ]);
            }
        }

        // 12 reportes de falla.
        for ($i = 1; $i <= 12; $i++) {
            BugReport::create([
                'titulo' => "Reporte volumen {$i} {$marca}",
                'descripcion' => 'Reporte generado para pruebas de volumen en el listado de reportes de falla.',
                'tipo' => 'sistema',
                'estado' => 'pendiente',
                'fecha' => now()->subDays($i)->toDateString(),
                'id_usuario' => GeneralUser::where('rol', 'aprendiz')->inRandomOrder()->value('id') ?? 1,
            ]);
        }
    }
}
