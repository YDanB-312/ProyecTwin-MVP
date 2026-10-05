<?php

namespace Database\Seeders;

use App\Models\Admin;
use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\Comment;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\MotorConfig;
use App\Models\Notification;
use App\Models\Project;
use App\Models\Similarity;
use App\Models\TrainingProgram;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

// Datos base del sistema. Es la ÚNICA fuente de datos: el frontend ya no
// tiene mock, así que todo lo que consumen las vistas nace aquí.
class DatabaseSeeder extends Seeder
{
    // Ids de las propuestas "parecidas" (para las reacciones: revisiones, avisos…).
    private array $idsSimilares = [];

    // Ids de los proyectos de escenarios (borrador, ciclo de reenvío, intervención).
    private array $idsEscenarios = [];

    // Usuario creado con el flujo real de alta (evita doble auditoría crear_usuario).
    private ?int $idUsuarioMovimiento = null;

    public function run(): void
    {
        // --- Datos ---
        $this->usuarios();
        $this->catalogos();
        $this->fichas();
        $this->aprendices();
        $this->proyectos();
        $this->propuestasSimilares();
        $this->observaciones();
        $this->reportes();
        $this->fichaMovimiento();
        MotorConfig::firstOrCreate([], ['umbral' => 0.30, 'meses' => 12]);

        // --- Causa → efecto: cada acción dispara sus reacciones ---
        $this->reacciones();
        $this->historiales();
        $this->escenarios();
        $this->auditorias();
    }

    // Reacciones del sistema (notificaciones + bitácora), como en el uso real.
    private function reacciones(): void
    {
        $notif = app(\App\Services\NotificacionesService::class);

        // 1) Crear propuesta → avisa a su instructor (el ciclo de reenvío tiene
        //    su propio aviso: propuestaReenviada).
        $excluir = array_values(array_filter([$this->idsEscenarios['ciclo'] ?? null]));
        foreach (Project::where('estado', 'pendiente')
            ->when($excluir, fn ($q) => $q->whereNotIn('id', $excluir))
            ->get() as $p) {
            $notif->revisionPropuesta($p);
        }

        // 2) Analizar → el motor detecta coincidencias y avisa a los creadores.
        app(\App\Similarity\Recomputador::class)->recalcular(true);

        // 3) La copia rechazada (C) había sido detectada antes de rechazarse:
        //    se conserva como evidencia histórica (vigente=false) y su aviso
        //    queda respaldado por una fila real del motor.
        $c = Project::find($this->idsSimilares['C'] ?? null);
        $base7 = Project::find(7);
        if ($c && $base7) {
            foreach (\App\Services\SimilitudService::puntaje($c, [$c, $base7]) as $par) {
                if ((int) $par['project_id'] !== (int) $base7->id) continue;

                Similarity::create([
                    'id_proyecto_1' => $c->id,
                    'id_proyecto_2' => $base7->id,
                    'porcentaje' => $par['porcentaje'],
                    'detalles' => $par['detalles'],
                    'fecha' => now()->toDateString(),
                    'vigente' => false,
                ]);
                $notif->similitud($c, $base7->id, $par['porcentaje']);
                break;
            }
        }

        // 4) El instructor revisó: todas las resueltas quedan en bitácora.
        foreach (Project::whereIn('estado', ['aprobado', 'rechazado'])->orderBy('id')->get() as $p) {
            $this->auditarRevision($p->id, 'pendiente', $p->estado);
        }

        // 5) Reportes de falla: los abiertos avisan a los admins; los ya
        //    resueltos/rechazados avisan a su autor (como en el uso real).
        BugReport::orderBy('id')->get()->each(function ($r) use ($notif) {
            if (in_array($r->estado, ['resuelto', 'rechazado'], true)) {
                $notif->soporteResuelto($r);
            } else {
                $notif->reporteFalla($r);
            }
        });

        // 6) El admin ajustó el motor → bitácora.
        $cfg = MotorConfig::first();
        \App\Support\Auditoria::registrar('config_motor', 'motor_configs', $cfg?->id, [
            'umbral' => (float) optional($cfg)->umbral,
            'meses' => (int) optional($cfg)->meses,
        ]);
    }

    private function auditarRevision(?int $id, string $de, string $a): void
    {
        if (!$id) return;
        $p = Project::find($id);
        if (!$p) return;
        \App\Support\Auditoria::registrar('revisar_propuesta', 'projects', $id, [
            'de' => $de,
            'a' => $a,
            'titulo' => $p->titulo,
        ]);
    }

    // Historial funcional de la demo: cada propuesta muestra su ciclo real
    // (creada → enviada → decisión) con el actor que correspondía.
    private function historiales(): void
    {
        foreach (Project::orderBy('id')->get() as $p) {
            \App\Support\HistorialProyecto::registrar($p, 'creada', ['titulo' => $p->titulo], $p->id_creador);

            if (!in_array($p->estado, ['pendiente', 'aprobado', 'rechazado'], true)) {
                continue;
            }
            \App\Support\HistorialProyecto::registrar($p, 'enviada', [], $p->id_creador);

            $idInstructor = optional(Instructor::find($p->id_instructor_asignado))->id_usuario;
            if ($p->estado === 'aprobado') {
                \App\Support\HistorialProyecto::registrar($p, 'aprobada', [], $idInstructor);
            } elseif ($p->estado === 'rechazado') {
                $detalle = [];
                if ($p->id === ($this->idsSimilares['C'] ?? null)) {
                    $detalle['observacion'] = 'Propuesta rechazada: el motor detectó una alta similitud con una propuesta ya aprobada. Se recomienda replantear el enfoque y diferenciar los objetivos.';
                }
                \App\Support\HistorialProyecto::registrar($p, 'rechazada', $detalle, $idInstructor);
            }
        }
    }

    // Escenarios que el flujo real produce en varios pasos: reenvío tras
    // rechazo (con su versión histórica de similitud) e intervención admin.
    private function escenarios(): void
    {
        $notif = app(\App\Services\NotificacionesService::class);
        $motor = app(\App\Similarity\Recomputador::class);

        // 1) Ciclo rechazo → corrección → reenvío.
        $ciclo = Project::find($this->idsEscenarios['ciclo'] ?? null);
        if ($ciclo) {
            $instructor = optional(Instructor::find($ciclo->id_instructor_asignado))->id_usuario;

            $observacion = 'Propuesta rechazada: replantea el enfoque para diferenciarla de la propuesta aprobada.';
            $ciclo->update(['estado' => 'rechazado']);
            $motor->archivar($ciclo);
            Comment::create([
                'texto' => $observacion,
                'id_proyecto' => $ciclo->id,
                'id_usuario' => $instructor,
                'respuesta_a' => null,
            ]);
            \App\Support\Auditoria::registrar('revisar_propuesta', 'projects', $ciclo->id, [
                'de' => 'pendiente', 'a' => 'rechazado', 'titulo' => $ciclo->titulo,
            ]);
            \App\Support\HistorialProyecto::registrar($ciclo, 'rechazada', ['observacion' => $observacion], $instructor);

            // Corrección y reenvío: nueva detección sobre el contenido nuevo.
            $ciclo->update([
                'titulo' => 'Sistema de Gestión de Inventarios con trazabilidad',
                'resumen' => 'Herramienta para controlar inventarios y ventas del almacén con trazabilidad por lote y alertas de stock.',
                'estado' => 'pendiente',
            ]);

            // Mismo algoritmo que ProjectController::huellaContenido(): si cambia
            // allí, actualizar aquí. Deja la huella del contenido reenviado.
            $huella = sha1(json_encode([
                $ciclo->titulo,
                $ciclo->resumen,
                $ciclo->palabras_clave,
                $ciclo->area_aplicacion,
                $ciclo->objetivo_general,
                $ciclo->objetivos_especificos,
            ]));
            $ciclo->update(['huella_envio' => $huella]);

            \App\Support\HistorialProyecto::registrar($ciclo, 'actualizada', [], $ciclo->id_creador);
            $motor->detectar($ciclo);
            \App\Support\HistorialProyecto::registrar($ciclo, 'reenviada', [], $ciclo->id_creador);
            $notif->propuestaReenviada($ciclo);
            \App\Support\Auditoria::registrar('enviar_propuesta', 'projects', $ciclo->id, [
                'titulo' => $ciclo->titulo,
            ]);
        }

        // 2) Intervención administrativa sobre una aprobada (regla F8).
        $intervenido = Project::find($this->idsEscenarios['intervencion'] ?? null);
        if ($intervenido) {
            $admin = GeneralUser::where('rol', 'admin')->orderBy('id')->value('id');
            $intervenido->update(['estado' => 'pendiente']);
            \App\Support\Auditoria::registrar('revisar_propuesta', 'projects', $intervenido->id, [
                'de' => 'aprobado', 'a' => 'pendiente', 'titulo' => $intervenido->titulo,
                'intervencion_admin' => true,
            ]);
            \App\Support\HistorialProyecto::registrar($intervenido, 'devuelta_revision', ['intervencion_admin' => true], $admin);
        }
    }

    // Auditorías representativas de los datos demo (eventos ya existentes).
    private function auditorias(): void
    {
        foreach (GeneralUser::orderBy('id')->get() as $u) {
            // El alta de este usuario la auditó AltaUsuario (flujo real).
            if ($u->id === $this->idUsuarioMovimiento) continue;

            \App\Support\Auditoria::registrar('crear_usuario', 'general_users', $u->id, [
                'correo' => $u->correo,
                'username' => $u->username,
                'documento' => trim(($u->tipo_documento ?? '') . ' ' . ($u->numero_documento ?? '')),
                'rol' => $u->rol,
            ]);
        }

        foreach (ClassGroup::orderBy('id')->get() as $f) {
            \App\Support\Auditoria::registrar('crear_ficha', 'class_groups', $f->id, [
                'codigo' => $f->codigo,
                'numero' => $f->numero,
                'id_instructor' => $f->id_instructor,
            ]);
        }

        foreach (Project::where('estado', '!=', 'borrador')->orderBy('id')->get() as $p) {
            \App\Support\Auditoria::registrar('enviar_propuesta', 'projects', $p->id, [
                'titulo' => $p->titulo,
            ]);
        }
    }

    // ---------------------------------------------------------------- Usuarios
    private function usuarios(): void
    {
        // Usernames demo explícitos: no se regeneran (compatibilidad E2E/README).
        $demo = [
            ['id' => 1, 'nombre' => 'María', 'apellido' => 'González', 'correo' => 'maria.gonzalez@soy.sena.edu.co', 'username' => 'mgonzalez', 'rol' => 'aprendiz'],
            ['id' => 4, 'nombre' => 'Ana', 'apellido' => 'Martínez', 'correo' => 'ana.martinez@soy.sena.edu.co', 'username' => 'amartinez', 'rol' => 'aprendiz'],
            ['id' => 5, 'nombre' => 'Juan', 'apellido' => 'Pérez', 'correo' => 'juan.perez@soy.sena.edu.co', 'username' => 'jperez', 'rol' => 'aprendiz'],
            ['id' => 6, 'nombre' => 'Laura', 'apellido' => 'Gómez', 'correo' => 'laura.gomez@soy.sena.edu.co', 'username' => 'lgomez', 'rol' => 'aprendiz'],
            ['id' => 9, 'nombre' => 'Laura', 'apellido' => 'Sánchez Pérez', 'correo' => 'laura.sanchez@soy.sena.edu.co', 'username' => 'lsanchez', 'rol' => 'aprendiz'],
            ['id' => 10, 'nombre' => 'Diego', 'apellido' => 'Ramírez Castro', 'correo' => 'diego.ramirez@soy.sena.edu.co', 'username' => 'dramirez', 'rol' => 'aprendiz'],
            ['id' => 11, 'nombre' => 'Patricia', 'apellido' => 'Morales Vega', 'correo' => 'patricia.morales@soy.sena.edu.co', 'username' => 'pmorales', 'rol' => 'aprendiz'],
            ['id' => 2, 'nombre' => 'Carlos', 'apellido' => 'Ruiz', 'correo' => 'carlos.ruiz@sena.edu.co', 'username' => 'cruiz', 'rol' => 'instructor'],
            ['id' => 7, 'nombre' => 'Carlos', 'apellido' => 'Rodríguez Díaz', 'correo' => 'carlos.rodriguez@sena.edu.co', 'username' => 'crodriguez', 'rol' => 'instructor'],
            ['id' => 8, 'nombre' => 'Andrés', 'apellido' => 'Martínez López', 'correo' => 'andres.martinez@sena.edu.co', 'username' => 'amartinez1', 'rol' => 'instructor'],
            ['id' => 13, 'nombre' => 'Luis Fernando', 'apellido' => 'García', 'correo' => 'luis.garcia@sena.edu.co', 'username' => 'lfernando', 'rol' => 'instructor'],
            ['id' => 3, 'nombre' => 'Administrador', 'apellido' => '', 'correo' => 'admin@sena.edu.co', 'username' => 'a', 'rol' => 'admin'],
            ['id' => 12, 'nombre' => 'María Fernanda', 'apellido' => 'Torres', 'correo' => 'maria.torres@sena.edu.co', 'username' => 'mfernanda', 'rol' => 'admin'],
        ];

        foreach ($demo as $u) {
            GeneralUser::create([
                'id' => $u['id'],
                'nombre' => $u['nombre'],
                'apellido' => $u['apellido'],
                'correo' => $u['correo'],
                'tipo_documento' => 'CC',
                'numero_documento' => (string) (1000000000 + $u['id']),
                'username' => $u['username'],
                'password' => Hash::make($u['id'] === 3 ? 'admin123' : '123456'),
                'rol' => $u['rol'],
                'estado' => true,
            ]);
        }

        // Usuario demo con credencial temporal: primer ingreso obligatorio.
        // El username se genera con el flujo real (usernameSeguro).
        GeneralUser::create([
            'id' => 14,
            'nombre' => 'Demo',
            'apellido' => 'Temporal',
            'correo' => 'demo.temporal@soy.sena.edu.co',
            'tipo_documento' => 'CC',
            'numero_documento' => '1000000014',
            'username' => \App\Support\Credenciales::usernameSeguro('Demo', 'Temporal'),
            'password' => Hash::make('K7P4-X2M8'),
            'must_change_password' => true,
            'password_temporal' => \Illuminate\Support\Facades\Crypt::encryptString('K7P4-X2M8'),
            'rol' => 'aprendiz',
            'estado' => true,
        ]);

        // Filas de perfil (instructor/admin) y aprendices
        Instructor::create(['id' => 1, 'fecha_ingreso' => '2024-01-15', 'id_usuario' => 2]);
        Instructor::create(['id' => 2, 'fecha_ingreso' => '2024-02-01', 'id_usuario' => 7]);
        Instructor::create(['id' => 3, 'fecha_ingreso' => '2024-02-01', 'id_usuario' => 8]);
        Instructor::create(['id' => 4, 'fecha_ingreso' => '2024-03-01', 'id_usuario' => 13]);

        // Perfiles de administrador (mismo patrón que sincronizarPerfiles()).
        Admin::firstOrCreate(['id_usuario' => 3]);
        Admin::firstOrCreate(['id_usuario' => 12]);
    }

    // ---------------------------------------------------------------- Aprendices
    // (después de fichas y programas: dependen de ambos)
    private function aprendices(): void
    {
        $aprendices = [
            ['id' => 1, 'codigo' => 'AP-001', 'usuario' => 1, 'ficha' => 1, 'programa' => 1],
            ['id' => 2, 'codigo' => 'AP-004', 'usuario' => 4, 'ficha' => 1, 'programa' => 1],
            ['id' => 3, 'codigo' => 'AP-005', 'usuario' => 5, 'ficha' => 1, 'programa' => 1],
            ['id' => 4, 'codigo' => 'AP-006', 'usuario' => 6, 'ficha' => 2, 'programa' => 1],
            ['id' => 5, 'codigo' => 'AP-009', 'usuario' => 9, 'ficha' => 3, 'programa' => 2],
            ['id' => 6, 'codigo' => 'AP-010', 'usuario' => 10, 'ficha' => 4, 'programa' => 3],
            ['id' => 7, 'codigo' => 'AP-011', 'usuario' => 11, 'ficha' => 2, 'programa' => 1],
            ['id' => 8, 'codigo' => 'AP-014', 'usuario' => 14, 'ficha' => 1, 'programa' => 1],
        ];
        foreach ($aprendices as $a) {
            Apprentice::create([
                'id' => $a['id'],
                'codigo' => $a['codigo'],
                'id_class_group' => $a['ficha'],
                'id_usuario' => $a['usuario'],
                'id_programa' => $a['programa'],
            ]);
        }
    }

    // ---------------------------------------------------------------- Catálogos
    private function catalogos(): void
    {
        // Redes de conocimiento + programas
        $informatica = KnowledgeNetwork::create(['id' => 1, 'nombre' => 'Informática, Diseño y Desarrollo de Software']);
        $artes = KnowledgeNetwork::create(['id' => 2, 'nombre' => 'Artes Gráficas']);

        TrainingProgram::create(['id' => 1, 'nombre' => 'ADSO', 'nivel' => 'Tecnologo', 'knowledge_network_id' => $informatica->id]);
        TrainingProgram::create(['id' => 2, 'nombre' => 'Produccion Multimedia', 'nivel' => 'Tecnologo', 'knowledge_network_id' => $artes->id]);
        TrainingProgram::create(['id' => 3, 'nombre' => 'Infraestructura Redes', 'nivel' => 'Tecnologo', 'knowledge_network_id' => $informatica->id]);
    }

    // ---------------------------------------------------------------- Fichas
    private function fichas(): void
    {
        $fichas = [
            ['id' => 1, 'codigo' => 'xkp-mqwr', 'numero' => '2568', 'nombre' => 'Analisis y Desarrollo 2568', 'estado' => 'activo', 'id_programa' => 1, 'id_instructor' => 1],
            ['id' => 2, 'codigo' => 'bnt-jhsa', 'numero' => '2634', 'nombre' => 'Analisis y Desarrollo 2634', 'estado' => 'activo', 'id_programa' => 1, 'id_instructor' => 2],
            ['id' => 3, 'codigo' => 'qwe-rtzu', 'numero' => '3102', 'nombre' => 'Produccion Multimedia 3102', 'estado' => 'activo', 'id_programa' => 2, 'id_instructor' => 2],
            ['id' => 4, 'codigo' => 'mno-pqrs', 'numero' => '2801', 'nombre' => 'Infraestructura Redes 2801', 'estado' => 'finalizado', 'id_programa' => 3, 'id_instructor' => 3],
        ];
        foreach ($fichas as $f) ClassGroup::create($f);
    }

    // ---------------------------------------------------------------- Propuestas
    private function proyectos(): void
    {
        // id_instructor_asignado apunta a la FILA de instructors (no al usuario).
        $proyectos = [
            ['id' => 1, 'titulo' => 'Sistema IoT para Agricultura', 'resumen' => 'Sistema de monitoreo inteligente para cultivos utilizando sensores IoT que miden humedad, temperatura y nutrientes del suelo, permitiendo la toma de decisiones en tiempo real para optimizar el riego y la fertilización.', 'claves' => 'IoT, sensores, agricultura, monitoreo, automatización, datos, plataforma, transparencia', 'area' => 'Tecnología e Informática', 'estado' => 'pendiente', 'creador' => 4, 'instr' => 1, 'ficha' => 1,
             'general' => 'Optimizar el uso del agua y los nutrientes en cultivos mediante el monitoreo continuo de variables ambientales con una red de sensores IoT y visualización de datos en plataforma.',
             'especificos' => ['Diseñar una red de sensores IoT para medir humedad, temperatura y nutrientes del suelo.', 'Desarrollar una plataforma web que visualice los datos en tiempo real y datos abiertos.', 'Implementar alertas tempranas y transparencia de datos ante condiciones críticas en el cultivo.'],
             'equipo' => [4, 5, 6]],
            ['id' => 2, 'titulo' => 'App Móvil para Turismo Local', 'resumen' => 'Aplicación móvil que promueve el turismo local mostrando sitios de interés, rutas y eventos culturales, facilitando la exploración de destinos y la planificación de visitas.', 'claves' => 'turismo, app móvil, cultura, rutas turísticas, geolocalización', 'area' => 'Cultura y Entretenimiento', 'estado' => 'pendiente', 'creador' => 5, 'instr' => 1, 'ficha' => 1,
             'general' => 'Facilitar la exploración de destinos turísticos locales mediante una aplicación móvil con rutas y eventos culturales.',
             'especificos' => ['Implementar sistema de geolocalización para rutas turísticas interactivas.', 'Crear un catálogo interactivo de sitios de interés cultural y natural.'],
             'equipo' => [5]],
            ['id' => 3, 'titulo' => 'Plataforma E-learning para Música', 'resumen' => 'Plataforma web para aprendizaje de instrumentos musicales con lecciones interactivas, catálogo de instrumentos, control de inventarios y stock, seguimiento de progreso y recursos multimedia para estudiantes de todos los niveles.', 'claves' => 'e-learning, música, educación, instrumentos, plataforma, inventarios, stock, control, catálogo', 'area' => 'Tecnología e Informática', 'estado' => 'pendiente', 'creador' => 6, 'instr' => 2, 'ficha' => 2,
             'general' => 'Crear una plataforma de aprendizaje musical con lecciones interactivas y seguimiento del progreso del estudiante.',
             'especificos' => ['Implementar control de inventarios de instrumentos.', 'Desarrollar reproductor de audio con control de velocidad y repetición.'],
             'equipo' => [6, 4]],
            ['id' => 4, 'titulo' => 'Plataforma de Ventas Online', 'resumen' => 'Aplicación web de comercio electrónico para pequeños comercios con catálogo de productos, carrito de compras, control de inventarios y stock, y pasarela de pagos.', 'claves' => 'e-commerce, ventas, pagos, catálogo, comercio, inventarios, stock, control', 'area' => 'Tecnología e Informática', 'estado' => 'pendiente', 'creador' => 1, 'instr' => 1, 'ficha' => 1,
             'general' => 'Facilitar la digitalización de pequeños comercios mediante una plataforma de ventas en línea con catálogo, carrito y pagos, integrada al control de inventarios y stock.',
             'especificos' => ['Implementar un catálogo de productos con búsqueda y filtros.', 'Desarrollar un carrito de compras con proceso de pago seguro e inventario.', 'Crear un panel de administración para la gestión de pedidos y stock.'],
             'equipo' => [1, 5]],
            ['id' => 5, 'titulo' => 'Sistema de Gestión de Inventarios', 'resumen' => 'Herramienta para control de inventarios y ventas de almacén con catálogo de productos, comercio electrónico, alertas de stock y reportes de trazabilidad.', 'claves' => 'inventarios, stock, almacén, control, reportes, ventas, comercio, catálogo, plataforma', 'area' => 'Logística y Operaciones', 'estado' => 'aprobado', 'creador' => 1, 'instr' => 1, 'ficha' => 1,
             'general' => 'Controlar el inventario y las ventas del almacén con reportes de trazabilidad y alertas de stock.',
             'especificos' => ['Registrar entradas y salidas de mercancía.', 'Configurar alertas de stock mínimo.', 'Generar reportes de trazabilidad por lote.'],
             'equipo' => [1]],
            ['id' => 6, 'titulo' => 'App de Bienestar Deportivo', 'resumen' => 'Aplicación móvil de seguimiento de rutinas de ejercicio, registro de actividad y metas de bienestar para la comunidad SENA.', 'claves' => 'deporte, bienestar, rutinas, actividad, salud', 'area' => 'Deporte y Recreación', 'estado' => 'pendiente', 'creador' => 5, 'instr' => 1, 'ficha' => 1,
             'general' => 'Acompañar a la comunidad SENA en el seguimiento de rutinas de ejercicio y metas de bienestar.',
             'especificos' => ['Registrar rutinas y progreso de ejercicio.', 'Implementar recordatorios de actividad física.', 'Visualizar estadísticas de rendimiento.'],
             'equipo' => [5]],
            ['id' => 7, 'titulo' => 'Portal de Transparencia SENA', 'resumen' => 'Portal web de datos abiertos que publica información sobre presupuesto, contratación e indicadores institucionales del SENA, con monitoreo de sensores IoT, visualización de datos y plataforma en tiempo real.', 'claves' => 'transparencia, datos abiertos, presupuesto, contratación, IoT, sensores, monitoreo, plataforma, datos', 'area' => 'Gobierno y Ciudadanía', 'estado' => 'aprobado', 'creador' => 6, 'instr' => 2, 'ficha' => 2,
             'general' => 'Publicar información institucional del SENA en un portal de datos abiertos con monitoreo en tiempo real.',
             'especificos' => ['Implementar visualizaciones de datos interactivas.', 'Integrar fuentes de datos institucionales e IoT.'],
             'equipo' => [6]],
            ['id' => 8, 'titulo' => 'Chatbot de Atención Académica', 'resumen' => 'Asistente conversacional que responde dudas sobre programas de formación, requisitos de matrícula y trámites académicos.', 'claves' => 'chatbot, atención, académico, asistente, IA', 'area' => 'Tecnología e Informática', 'estado' => 'rechazado', 'creador' => 4, 'instr' => 1, 'ficha' => 1,
             'general' => 'Resolver dudas académicas frecuentes mediante un asistente conversacional.',
             'especificos' => ['Implementar respuestas automáticas a preguntas frecuentes.', 'Integrar con la base de datos de programas.'],
             'equipo' => [4, 6]],
        ];

        // Mapa usuario → id de fila de aprendiz (para el equipo/pivote)
        $aprendizPorUsuario = Apprentice::pluck('id', 'id_usuario');

        foreach ($proyectos as $p) {
            $proy = Project::create([
                'id' => $p['id'],
                'titulo' => $p['titulo'],
                'resumen' => $p['resumen'],
                'palabras_clave' => $p['claves'],
                'area_aplicacion' => $p['area'],
                'objetivo_general' => $p['general'],
                'objetivos_especificos' => $p['especificos'],
                'estado' => $p['estado'],
                'id_creador' => $p['creador'],
                'id_instructor_asignado' => $p['instr'],
                'id_class_group' => $p['ficha'],
            ]);
            foreach ($p['equipo'] as $usuario) {
                ApprenticeProject::create([
                    'id_aprendiz' => $aprendizPorUsuario[$usuario],
                    'id_proyecto' => $proy->id,
                ]);
            }
        }
    }

    // ------------------------------------------------- Propuestas parecidas (motor)
    // Propuestas pensadas para que el MOTOR detecte coincidencias reales: dos
    // copias casi idénticas de aprobadas, una paráfrasis y dos originales.
    // (Las similitudes NO se escriben aquí: las calcula el motor al final.)
    private function propuestasSimilares(): void
    {
        $base5 = Project::find(5); // aprobada (inventarios)
        $base7 = Project::find(7); // aprobada (transparencia)

        $nuevas = [
            // Copia casi idéntica de la 5 (mismo texto).
            ['titulo' => $base5->titulo, 'resumen' => $base5->resumen, 'claves' => $base5->palabras_clave,
             'area' => $base5->area_aplicacion, 'general' => $base5->objetivo_general, 'especificos' => $base5->objetivos_especificos,
             'estado' => 'pendiente', 'creador' => 4, 'instr' => 1, 'ficha' => 1, 'equipo' => [4, 5]],
            // Paráfrasis de la 5 (mismas ideas, otras palabras).
            ['titulo' => 'Plataforma de Existencias del Almacén',
             'resumen' => 'Herramienta para la administración de las existencias y la comercialización del almacén, con catálogo de productos, alertas de stock e informes de trazabilidad.',
             'claves' => 'existencias, stock, almacén, administración, informes, comercialización, catálogo',
             'area' => 'Logística y Operaciones',
             'general' => 'Administrar las existencias y las ventas del almacén con informes de trazabilidad y alertas de stock.',
             'especificos' => ['Anotar entradas y salidas de mercancía.', 'Configurar avisos de stock mínimo.', 'Generar informes de trazabilidad por lote.'],
             'estado' => 'aprobado', 'creador' => 5, 'instr' => 1, 'ficha' => 1, 'equipo' => [5]],
            // Copia casi idéntica de la 7 (mismo texto).
            ['titulo' => $base7->titulo, 'resumen' => $base7->resumen, 'claves' => $base7->palabras_clave,
             'area' => $base7->area_aplicacion, 'general' => $base7->objetivo_general, 'especificos' => $base7->objetivos_especificos,
             'estado' => 'rechazado', 'creador' => 11, 'instr' => 2, 'ficha' => 2, 'equipo' => [11, 6]],
            // Original 1 (sin parecido).
            ['titulo' => 'Gestión de Citas Odontológicas',
             'resumen' => 'Aplicación web para agendar y administrar citas odontológicas, con recordatorios a los pacientes y control de la agenda de los odontólogos.',
             'claves' => 'citas, odontología, pacientes, agenda, salud',
             'area' => 'Salud',
             'general' => 'Facilitar la programación de citas odontológicas y reducir las inasistencias.',
             'especificos' => ['Registrar pacientes y tratamientos.', 'Agendar y recordar citas.', 'Reportar la ocupación de la agenda.'],
             'estado' => 'pendiente', 'creador' => 1, 'instr' => 1, 'ficha' => 1, 'equipo' => [1, 4, 5]],
            // Original 2 (sin parecido).
            ['titulo' => 'Control de Gastos Personales',
             'resumen' => 'Aplicación para registrar ingresos y gastos personales, clasificar las transacciones y visualizar el estado de las finanzas con gráficos.',
             'claves' => 'gastos, presupuesto, finanzas, ahorro, ingresos',
             'area' => 'Finanzas',
             'general' => 'Ayudar a las personas a controlar su presupuesto y sus gastos.',
             'especificos' => ['Registrar ingresos y gastos.', 'Clasificar por categoría.', 'Visualizar reportes mensuales.'],
             'estado' => 'pendiente', 'creador' => 6, 'instr' => 2, 'ficha' => 2, 'equipo' => [6, 11]],
            // Borrador (aún sin enviar).
            ['titulo' => 'Sistema de Reservas de Laboratorios',
             'resumen' => 'Aplicación para reservar laboratorios y equipos con disponibilidad en tiempo real.',
             'claves' => 'reservas, laboratorios, disponibilidad',
             'area' => 'Tecnología e Informática',
             'general' => 'Organizar la reserva de laboratorios y equipos de formación.',
             'especificos' => ['Registrar disponibilidad de laboratorios.', 'Gestionar solicitudes de reserva.'],
             'estado' => 'borrador', 'creador' => 4, 'instr' => 1, 'ficha' => 1, 'equipo' => [4]],
            // Ciclo rechazo → corrección → reenvío (copia de la 5).
            ['titulo' => $base5->titulo, 'resumen' => $base5->resumen, 'claves' => $base5->palabras_clave,
             'area' => $base5->area_aplicacion, 'general' => $base5->objetivo_general, 'especificos' => $base5->objetivos_especificos,
             'estado' => 'pendiente', 'creador' => 6, 'instr' => 2, 'ficha' => 2, 'equipo' => [6]],
            // Intervención administrativa (original aprobada).
            ['titulo' => 'Control de Inventario de Equipos Biomédicos',
             'resumen' => 'Sistema para registrar, mantener y auditar el inventario de equipos biomédicos de una clínica.',
             'claves' => 'inventario, equipos, biomédicos, mantenimiento',
             'area' => 'Salud',
             'general' => 'Controlar el ciclo de vida de los equipos biomédicos.',
             'especificos' => ['Registrar equipos y mantenimientos.', 'Auditar el inventario por sede.'],
             'estado' => 'aprobado', 'creador' => 9, 'instr' => 2, 'ficha' => 3, 'equipo' => [9]],
        ];

        $aprendizPorUsuario = Apprentice::pluck('id', 'id_usuario');
        $creados = [];

        foreach ($nuevas as $p) {
            $proy = Project::create([
                'titulo' => $p['titulo'],
                'resumen' => $p['resumen'],
                'palabras_clave' => $p['claves'],
                'area_aplicacion' => $p['area'],
                'objetivo_general' => $p['general'],
                'objetivos_especificos' => $p['especificos'],
                'estado' => $p['estado'],
                'id_creador' => $p['creador'],
                'id_instructor_asignado' => $p['instr'],
                'id_class_group' => $p['ficha'],
            ]);
            $creados[] = $proy->id;
            foreach ($p['equipo'] as $usuario) {
                $idAprendiz = $aprendizPorUsuario[$usuario] ?? null;
                if ($idAprendiz) {
                    ApprenticeProject::create(['id_aprendiz' => $idAprendiz, 'id_proyecto' => $proy->id]);
                }
            }
        }

        // A = copia pendiente (activa), B = paráfrasis aprobada, C = copia rechazada.
        $this->idsSimilares = [
            'A' => $creados[0] ?? null,
            'B' => $creados[1] ?? null,
            'C' => $creados[2] ?? null,
        ];

        // Escenarios de demostración (ids tras los anteriores).
        $this->idsEscenarios = [
            'borrador' => $creados[5] ?? null,
            'ciclo' => $creados[6] ?? null,
            'intervencion' => $creados[7] ?? null,
        ];
    }

    // ------------------------------------------------- Ficha: unión de un aprendiz
    // Reacciona como el sistema real: registra la unión y avisa al instructor.
    private function fichaMovimiento(): void
    {
        // Alta con el flujo real: username seguro + credencial temporal.
        // Sin envío de correo: el Seeder no depende del mailer.
        $alta = app(\App\Services\AltaUsuario::class)->crear([
            'nombre' => 'Andrés',
            'apellido' => 'Cifuentes',
            'correo' => 'andres.cifuentes@soy.sena.edu.co',
            'tipo_documento' => 'CC',
            'numero_documento' => '1000000015',
            'rol' => 'aprendiz',
        ], false);
        $u = $alta['usuario'];
        $this->idUsuarioMovimiento = $u->id;

        Apprentice::create([
            'codigo' => 'AP-100',
            'id_usuario' => $u->id,
            'id_class_group' => 1,
            'id_programa' => 1,
        ]);

        // La asociación a la ficha queda auditada como en el flujo real.
        \App\Support\Auditoria::registrar('agregar_aprendiz', 'class_groups', 1, [
            'usuario' => $u->id,
            'creada' => true,
            'credenciales_enviadas' => $alta['enviadas'],
        ]);

        app(\App\Services\NotificacionesService::class)->fichaMovimientoInstructor(
            ClassGroup::with('instructor')->find(1),
            'El aprendiz ' . $u->nombre . ' ' . $u->apellido . ' se unió a la ficha "Analisis y Desarrollo 2568".',
            'ficha:1'
        );
    }

    // ---------------------------------------------------------------- Reportes de falla
    private function reportes(): void
    {
        $reportes = [
            ['id' => 1, 'titulo' => 'Pantalla blanca en Dashboard', 'descripcion' => 'Error al cargar la página de Dashboard, muestra pantalla blanca después de iniciar sesión', 'tipo' => 'sistema', 'estado' => 'pendiente', 'correo' => 'carlos.rodriguez@sena.edu.co'],
            ['id' => 2, 'titulo' => 'No se suben archivos PDF', 'descripcion' => 'No se pueden subir archivos PDF en la sección de evidencias del proyecto', 'tipo' => 'proyecto', 'estado' => 'pendiente', 'correo' => 'maria.gonzalez@soy.sena.edu.co'],
            ['id' => 3, 'titulo' => 'Faltan notificaciones de revisión', 'descripcion' => 'El sistema no envía notificaciones cuando un instructor revisa un proyecto', 'tipo' => 'sistema', 'estado' => 'en_revision', 'correo' => 'andres.martinez@sena.edu.co'],
            ['id' => 4, 'titulo' => 'Botón cerrar sesión roto', 'descripcion' => 'El botón de cerrar sesión no funciona correctamente en navegador Chrome', 'tipo' => 'sistema', 'estado' => 'en_revision', 'correo' => 'laura.sanchez@soy.sena.edu.co'],
            ['id' => 5, 'titulo' => 'Reporte PDF corrupto', 'descripcion' => 'Error en la generación de reportes PDF, el archivo descargado está corrupto', 'tipo' => 'datos', 'estado' => 'resuelto', 'correo' => 'diego.ramirez@soy.sena.edu.co'],
            ['id' => 6, 'titulo' => 'Logos de proyectos no se muestran', 'descripcion' => 'Las imágenes de los logos de proyectos no se muestran en la vista de lista', 'tipo' => 'proyecto', 'estado' => 'rechazado', 'correo' => 'patricia.morales@soy.sena.edu.co'],
        ];
        foreach ($reportes as $r) {
            $autor = GeneralUser::where('correo', $r['correo'])->first();
            BugReport::create([
                'id' => $r['id'],
                'titulo' => $r['titulo'],
                'descripcion' => $r['descripcion'],
                'tipo' => $r['tipo'],
                'estado' => $r['estado'],
                'fecha' => now()->subDays($r['id'])->toDateString(),
                'id_usuario' => $autor?->id ?? 1,
            ]);
        }
    }

    // ---------------------------------------------------------------- Observaciones
    private function observaciones(): void
    {
        // Hilo de la propuesta 4 (dos observaciones de Carlos, una de María).
        $hilos = [
            ['id' => 1, 'texto' => 'El proyecto necesita mejorar la sección de análisis de requisitos. Se recomienda ampliar la documentación técnica antes de continuar con el desarrollo.', 'id_usuario' => 2],
            ['id' => 2, 'texto' => 'He realizado los ajustes sugeridos en la documentación. La nueva versión incluye diagramas de flujo y casos de uso detallados. Quedo atento a más retroalimentación.', 'id_usuario' => 1],
            ['id' => 3, 'texto' => 'La propuesta inicial tiene buen enfoque, pero falta definir mejor los entregables del primer sprint. Recomiendo revisar la guía de proyectos para alinear expectativas.', 'id_usuario' => 2],
        ];
        foreach ($hilos as $h) {
            Comment::create([
                'id' => $h['id'],
                'texto' => $h['texto'],
                'id_proyecto' => 4,
                'id_usuario' => $h['id_usuario'],
                'respuesta_a' => null,
            ]);
        }

        // Y una observación del instructor sobre la propuesta aprobada (2).
        Comment::create([
            'texto' => 'Buen avance, revisar documentación.',
            'id_proyecto' => 2,
            'id_usuario' => 2,
            'respuesta_a' => null,
        ]);

        // Rechazo de la copia (C): el instructor deja la observación del porqué.
        if (!empty($this->idsSimilares['C'])) {
            $c = Project::find($this->idsSimilares['C']);
            $idInstructor = $c ? optional(Instructor::find($c->id_instructor_asignado))->id_usuario : null;
            if ($idInstructor) {
                Comment::create([
                    'texto' => 'Propuesta rechazada: el motor detectó una alta similitud con una propuesta ya aprobada. Se recomienda replantear el enfoque y diferenciar los objetivos.',
                    'id_proyecto' => $this->idsSimilares['C'],
                    'id_usuario' => $idInstructor,
                    'respuesta_a' => null,
                ]);
            }
        }
    }
}
