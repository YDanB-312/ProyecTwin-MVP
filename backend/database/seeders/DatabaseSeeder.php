<?php

namespace Database\Seeders;

use App\Models\Admin;
use App\Models\Apprentice;
use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\Comment;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\Notification;
use App\Models\Project;
use App\Models\TrainingProgram;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        // Seed alineado con frontend/src/data/mockData.js (13 usuarios + IDs/pw oficiales).
        $aprendiz = GeneralUser::create([
            'id' => 1,
            'nombre' => 'María',
            'apellido' => 'González',
            'correo' => 'maria.gonzalez@soy.sena.edu.co',
            'password' => Hash::make('123456'),
            'rol' => 'aprendiz',
            'estado' => true,
        ]);
        GeneralUser::create(['id' => 4, 'nombre' => 'Ana', 'apellido' => 'Martínez', 'correo' => 'ana.martinez@soy.sena.edu.co', 'password' => Hash::make('123456'), 'rol' => 'aprendiz', 'estado' => true]);
        GeneralUser::create(['id' => 5, 'nombre' => 'Juan', 'apellido' => 'Pérez', 'correo' => 'juan.perez@soy.sena.edu.co', 'password' => Hash::make('123456'), 'rol' => 'aprendiz', 'estado' => true]);
        GeneralUser::create(['id' => 6, 'nombre' => 'Laura', 'apellido' => 'Gómez', 'correo' => 'laura.gomez@soy.sena.edu.co', 'password' => Hash::make('123456'), 'rol' => 'aprendiz', 'estado' => true]);
        GeneralUser::create(['id' => 9, 'nombre' => 'Laura', 'apellido' => 'Sánchez Pérez', 'correo' => 'laura.sanchez@soy.sena.edu.co', 'password' => Hash::make('123456'), 'rol' => 'aprendiz', 'estado' => true]);
        GeneralUser::create(['id' => 10, 'nombre' => 'Diego', 'apellido' => 'Ramírez Castro', 'correo' => 'diego.ramirez@soy.sena.edu.co', 'password' => Hash::make('123456'), 'rol' => 'aprendiz', 'estado' => true]);
        GeneralUser::create(['id' => 11, 'nombre' => 'Patricia', 'apellido' => 'Morales Vega', 'correo' => 'patricia.morales@soy.sena.edu.co', 'password' => Hash::make('123456'), 'rol' => 'aprendiz', 'estado' => true]);

        $instructorUser = GeneralUser::create([
            'id' => 2,
            'nombre' => 'Carlos',
            'apellido' => 'Ruiz',
            'correo' => 'carlos.ruiz@sena.edu.co',
            'password' => Hash::make('123456'),
            'rol' => 'instructor',
            'estado' => true,
        ]);
        GeneralUser::create(['id' => 7, 'nombre' => 'Carlos', 'apellido' => 'Rodríguez Díaz', 'correo' => 'carlos.rodriguez@sena.edu.co', 'password' => Hash::make('123456'), 'rol' => 'instructor', 'estado' => true]);
        GeneralUser::create(['id' => 8, 'nombre' => 'Andrés', 'apellido' => 'Martínez López', 'correo' => 'andres.martinez@sena.edu.co', 'password' => Hash::make('123456'), 'rol' => 'instructor', 'estado' => true]);
        GeneralUser::create(['id' => 13, 'nombre' => 'Luis', 'apellido' => 'Fernando García', 'correo' => 'luis.garcia@sena.edu.co', 'password' => Hash::make('123456'), 'rol' => 'instructor', 'estado' => true]);

        $adminUser = GeneralUser::create([
            'id' => 3,
            'nombre' => 'Administrador',
            'apellido' => '',
            'correo' => 'admin@sena.edu.co',
            'password' => Hash::make('admin123'),
            'rol' => 'admin',
            'estado' => true,
        ]);
        GeneralUser::create(['id' => 12, 'nombre' => 'María', 'apellido' => 'Fernanda Torres', 'correo' => 'maria.torres@sena.edu.co', 'password' => Hash::make('123456'), 'rol' => 'admin', 'estado' => true]);

        // Compat: los tests legados usaban estos alias extra (las credenciales que esperan son las de las cuentas mock)
        GeneralUser::firstOrCreate(['correo' => 'juan@sena.edu.co'], ['nombre' => 'Juan', 'apellido' => 'Pérez', 'password' => Hash::make('123456'), 'rol' => 'aprendiz', 'estado' => true]);
        GeneralUser::firstOrCreate(['correo' => 'carlos@sena.edu.co'], ['nombre' => 'Carlos', 'apellido' => 'Ruiz', 'password' => Hash::make('123456'), 'rol' => 'instructor', 'estado' => true]);
        GeneralUser::firstOrCreate(['correo' => 'admin@proyectwin.com'], ['nombre' => 'Admin', 'apellido' => 'General', 'password' => Hash::make('admin123'), 'rol' => 'admin', 'estado' => true]);

        // Redes de Conocimiento + Programa
        $redInformatica = KnowledgeNetwork::create([
            'nombre' => 'Informática, Diseño y Desarrollo de Software',
        ]);
        $redArtes = KnowledgeNetwork::create([
            'nombre' => 'Artes Gráficas',
        ]);

        $programa = TrainingProgram::create([
            'nombre' => 'ADSO',
            'red' => 'Informática, Diseño y Desarrollo de Software',
            'knowledge_network_id' => $redInformatica->id,
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
        ]);
        // Programa adicional para la segunda red (equivalente a Produccion Multimedia en el frontend)
        TrainingProgram::create([
            'nombre' => 'Produccion Multimedia',
            'red' => 'Artes Gráficas',
            'knowledge_network_id' => $redArtes->id,
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
        ]);

        // Instructor y Admin
        $instructor = Instructor::create([
            'fecha_ingreso' => '2024-01-15',
            'id_usuario' => $instructorUser->id,
        ]);

        Admin::create([
            'area_encargada' => 'General',
            'id_usuario' => $adminUser->id,
        ]);

        // Ficha
        $ficha = ClassGroup::create([
            'codigo' => 'abc-defg',
            'numero' => '001',
            'nombre' => 'Grupo 001',
            'estado' => 'activo',
            'id_programa' => $programa->id,
            'id_instructor' => $instructor->id,
        ]);

        // Aprendiz
        Apprentice::create([
            'codigo' => 'AP-001',
            'id_class_group' => $ficha->id,
            'id_usuario' => $aprendiz->id,
            'id_programa' => $programa->id,
        ]);

        // Proyectos de ejemplo
        Project::create([
            'titulo' => 'Sistema de Inventario',
            'tipo_proyecto' => 'aplicacion',
            'resumen' => 'Sistema para gestionar inventario de una tienda.',
            'palabras_clave' => 'inventario, gestion, tienda',
            'area_aplicacion' => 'Tecnologia',
            'tecnologias' => 'Laravel, MySQL',
            'objetivos' => ['Controlar stock', 'Generar reportes'],
            'entregables' => ['Sistema web', 'Manual de usuario'],
            'estado' => 'pendiente',
            'id_creador' => $aprendiz->id,
            'id_instructor_asignado' => $instructor->id,
            'id_class_group' => $ficha->id,
        ]);

        $proyectoAprobado = Project::create([
            'titulo' => 'App de Turismo',
            'tipo_proyecto' => 'aplicacion',
            'resumen' => 'App para mostrar sitios turisticos.',
            'palabras_clave' => 'turismo, app, geolocalizacion',
            'area_aplicacion' => 'Cultura',
            'tecnologias' => 'React Native, Node.js',
            'objetivos' => ['Mostrar sitios', 'Rutas turisticas'],
            'entregables' => ['App movil', 'Panel admin'],
            'estado' => 'aprobado',
            'id_creador' => $aprendiz->id,
            'id_instructor_asignado' => $instructor->id,
            'id_class_group' => $ficha->id,
        ]);

        // Datos opcionales basicos
        Notification::create([
            'titulo' => 'Bienvenido',
            'descripcion' => 'Bienvenido a ProyecTwin',
            'tipo' => 'mensaje',
            'leida' => false,
            'fecha' => now()->toDateString(),
            'id_usuario' => $aprendiz->id,
        ]);

        BugReport::create([
            'titulo' => 'Error de prueba',
            'descripcion' => 'Descripcion del error',
            'tipo' => 'sistema',
            'estado' => 'pendiente',
            'fecha' => now()->toDateString(),
            'id_usuario' => $aprendiz->id,
        ]);

        Comment::create([
            'texto' => 'Buen avance, revisar documentacion.',
            'id_proyecto' => $proyectoAprobado->id,
            'id_usuario' => $instructorUser->id,
        ]);
    }
}
