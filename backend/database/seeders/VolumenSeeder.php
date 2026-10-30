<?php

namespace Database\Seeders;

use App\Models\Apprentice;
use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

// Datos de VOLUMEN para probar listados, paginación y filtros con muchas filas.
// Se aplica SOBRE el seed base (no lo reemplaza). Usa una marca única por
// ejecución; no es determinista, pero sí re-ejecutable.
class VolumenSeeder extends Seeder
{
    public function run(): void
    {
        DB::transaction(fn () => $this->sembrar());
    }

    private function sembrar(): void
    {
        $marca = uniqid();
        $programaAdso = TrainingProgram::where('nombre', 'ADSO')->first();

        if (!$programaAdso) {
            throw new \RuntimeException('VolumenSeeder requiere el programa base ADSO.');
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

        // Una ficha por instructor.
        $fichas = [];
        foreach ($instructores as $idx => $ins) {
            // Número libre y re-ejecutable: la primera corrida usa 9000+ y las
            // siguientes avanzan de diez en diez (la columna numero es unique).
            $numero = 9000 + $idx;
            while (ClassGroup::where('numero', (string) $numero)->exists()) {
                $numero += 10;
            }
            $fichas[] = ClassGroup::create([
                'codigo' => "vol-{$idx}-{$marca}",
                'numero' => (string) $numero,
                'nombre' => "Ficha Volumen {$idx} {$marca}",
                'estado' => 'activo',
                'id_programa' => $programaAdso->id,
                'id_instructor' => $ins->id,
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
                'codigo' => 'VOL-' . str_pad((string) $i, 3, '0', STR_PAD_LEFT) . '-' . $marca,
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

        // 12 reportes de falla. El autor se asigna mediante round-robin por id
        // para repartir los reportes entre los aprendices existentes.
        $autores = GeneralUser::where('rol', 'aprendiz')->orderBy('id')->pluck('id')->all();
        $totalAutores = max(count($autores), 1);
        for ($i = 1; $i <= 12; $i++) {
            BugReport::create([
                'titulo' => "Reporte volumen {$i} {$marca}",
                'descripcion' => 'Reporte generado para pruebas de volumen en el listado de reportes de falla.',
                'tipo' => 'sistema',
                'estado' => 'pendiente',
                'fecha' => now()->subDays($i)->toDateString(),
                'id_usuario' => $autores[($i - 1) % $totalAutores] ?? 1,
            ]);
        }
    }
}
