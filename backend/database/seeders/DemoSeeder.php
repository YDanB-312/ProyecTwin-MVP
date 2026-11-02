<?php

namespace Database\Seeders;

use App\Models\Admin;
use App\Models\Apprentice;
use App\Models\ApprenticeProject;
use App\Models\AuditLog;
use App\Models\BugReport;
use App\Models\ClassGroup;
use App\Models\Comment;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\KnowledgeNetwork;
use App\Models\MotorConfig;
use App\Models\Notification;
use App\Models\Project;
use App\Models\ProjectHistory;
use App\Models\TrainingProgram;
use App\Similarity\Recomputador;
use App\Support\CuentasDemo;
use App\Support\DemoRegistry;
use App\Support\Pii;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Schema;

// Escenario demo completo, idempotente y sin datos institucionales.
//
// La propiedad de los registros se guarda en `demo_seed_records`. El seeder
// solo crea/adopta registros que identifica explícitamente; si un registro ya
// existe no lo sobrescribe. Las similitudes se generan con el motor real.
class DemoSeeder extends Seeder
{
    public function run(): void
    {
        if (app()->environment('production')) {
            throw new \RuntimeException(
                'DemoSeeder es de demostración y está bloqueado en producción. Usa admin:inicial.'
            );
        }

        if (! Schema::hasTable('demo_seed_records')) {
            throw new \RuntimeException(
                'Falta la tabla demo_seed_records. Ejecuta las migraciones antes de sembrar la demo.'
            );
        }

        DB::transaction(function () {
            $this->sembrarUsuarios();
            $this->sembrarCatalogos();
            $this->sembrarFichas();
            $this->sembrarAprendices();
            $this->sembrarPropuestas();
            $this->sembrarSimilitudes();
            $this->sembrarInteracciones();
        });
    }

    // ---------------------------------------------------------------- Usuarios

    private function sembrarUsuarios(): void
    {
        $numero = 2000000000;

        foreach (CuentasDemo::CUENTAS as $username => $cuenta) {
            $numero++;
            $usuario = $this->registroId('usuario', $username)
                ? GeneralUser::find($this->registroId('usuario', $username))
                : null;

            if (! $usuario) {
                $usuario = CuentasDemo::buscaUsuario($username);
            }

            if (! $usuario) {
                $usuario = GeneralUser::create([
                    'nombre' => $cuenta['nombre'],
                    'apellido' => $cuenta['apellido'],
                    'correo' => $cuenta['correo'],
                    'tipo_documento' => 'CC',
                    'numero_documento' => (string) $numero,
                    'username' => $username,
                    'password' => Hash::make(CuentasDemo::password()),
                    'rol' => $cuenta['rol'],
                    'estado' => true,
                    'email_verified_at' => now(),
                ]);
            }

            $this->recordar('usuario', $username, $usuario->id);

            if ($usuario->rol === 'instructor') {
                $instructor = $this->registroId('instructor', $username)
                    ? Instructor::find($this->registroId('instructor', $username))
                    : null;
                $instructor = $instructor ?: Instructor::firstOrCreate(
                    ['id_usuario' => $usuario->id],
                    ['fecha_ingreso' => '2024-01-15']
                );
                $this->recordar('instructor', $username, $instructor->id);
            }

            if ($usuario->rol === 'admin') {
                $admin = $this->registroId('admin', $username)
                    ? Admin::find($this->registroId('admin', $username))
                    : null;
                $admin = $admin ?: Admin::firstOrCreate(['id_usuario' => $usuario->id]);
                $this->recordar('admin', $username, $admin->id);
            }
        }
    }

    // ---------------------------------------------------------------- Catálogos

    private function sembrarCatalogos(): void
    {
        $redes = [
            'informatica' => 'Informática, Diseño y Desarrollo de Software',
            'artes' => 'Artes Gráficas',
        ];

        foreach ($redes as $clave => $nombre) {
            $red = $this->registroId('red', $clave)
                ? KnowledgeNetwork::find($this->registroId('red', $clave))
                : null;
            $red = $red ?: KnowledgeNetwork::firstOrCreate(['nombre' => $nombre]);
            $this->recordar('red', $clave, $red->id);
        }

        $programas = [
            'adso' => ['ADSO', 'Tecnologo', 'informatica'],
            'multimedia' => ['Produccion Multimedia', 'Tecnologo', 'artes'],
            'redes' => ['Infraestructura Redes', 'Tecnologo', 'informatica'],
        ];

        foreach ($programas as $clave => [$nombre, $nivel, $redClave]) {
            $redId = $this->registroId('red', $redClave);

            if (! $redId) {
                continue;
            }

            $programa = $this->registroId('programa', $clave)
                ? TrainingProgram::find($this->registroId('programa', $clave))
                : null;
            $programa = $programa ?: TrainingProgram::firstOrCreate(
                ['nombre' => $nombre, 'knowledge_network_id' => $redId],
                ['nivel' => $nivel]
            );
            $this->recordar('programa', $clave, $programa->id);
        }
    }

    // ---------------------------------------------------------------- Fichas

    private function sembrarFichas(): void
    {
        $fichas = [
            '2568' => ['xkp-mqwr', 'Analisis y Desarrollo 2568', 'activo', 'adso', 'cruiz'],
            '2634' => ['bnt-jhsa', 'Analisis y Desarrollo 2634', 'activo', 'adso', 'crodriguez'],
            '3102' => ['qwe-rtzu', 'Produccion Multimedia 3102', 'activo', 'multimedia', 'crodriguez'],
            '2801' => ['mno-pqrs', 'Infraestructura Redes 2801', 'finalizado', 'redes', 'amartinez1'],
        ];

        foreach ($fichas as $numero => [$codigo, $nombre, $estado, $programaClave, $instructorClave]) {
            $programaId = $this->registroId('programa', $programaClave);
            $instructorId = $this->registroId('instructor', $instructorClave);

            if (! $programaId || ! $instructorId) {
                continue;
            }

            $ficha = $this->registroId('ficha', $numero)
                ? ClassGroup::find($this->registroId('ficha', $numero))
                : null;
            $ficha = $ficha ?: ClassGroup::firstOrCreate(
                ['numero' => $numero],
                [
                    'codigo' => $codigo,
                    'nombre' => $nombre,
                    'estado' => $estado,
                    'id_programa' => $programaId,
                    'id_instructor' => $instructorId,
                ]
            );

            $this->recordar('ficha', $numero, $ficha->id);
        }
    }

    // ---------------------------------------------------------------- Aprendices

    private function sembrarAprendices(): void
    {
        $aprendices = [
            'mgonzalez' => ['AP-001', '2568', 'adso'],
            'amartinez' => ['AP-004', '2568', 'adso'],
            'jperez' => ['AP-005', '2568', 'adso'],
            'lgomez' => ['AP-006', '2634', 'adso'],
            'lsanchez' => ['AP-009', '3102', 'multimedia'],
            'dramirez' => ['AP-010', '2801', 'redes'],
            'pmorales' => ['AP-011', '2634', 'adso'],
            'acifuentes' => ['AP-100', '2568', 'adso'],
        ];

        foreach ($aprendices as $username => [$codigo, $fichaNumero, $programaClave]) {
            $usuarioId = $this->registroId('usuario', $username);
            $fichaId = $this->registroId('ficha', $fichaNumero);
            $programaId = $this->registroId('programa', $programaClave);

            if (! $usuarioId || ! $fichaId || ! $programaId) {
                continue;
            }

            $aprendiz = $this->registroId('aprendiz', $username)
                ? Apprentice::find($this->registroId('aprendiz', $username))
                : null;
            $aprendiz = $aprendiz ?: Apprentice::firstOrCreate(
                ['id_usuario' => $usuarioId],
                [
                    'codigo' => $codigo,
                    'id_class_group' => $fichaId,
                    'id_programa' => $programaId,
                ]
            );

            $this->recordar('aprendiz', $username, $aprendiz->id);
        }
    }

    // ---------------------------------------------------------------- Propuestas

    private function sembrarPropuestas(): void
    {
        foreach ($this->datosProyectos() as $datos) {
            $creadorId = $this->registroId('usuario', $datos['creador']);
            $instructorId = $this->registroId('instructor', $datos['instructor']);
            $fichaId = $this->registroId('ficha', $datos['ficha']);

            if (! $creadorId || ! $instructorId || ! $fichaId) {
                continue;
            }

            $proyecto = $this->registroId('proyecto', $datos['clave'])
                ? Project::find($this->registroId('proyecto', $datos['clave']))
                : null;

            $proyecto = $proyecto ?: Project::firstOrCreate(
                ['titulo' => $datos['titulo'], 'id_creador' => $creadorId],
                [
                    'resumen' => $datos['resumen'],
                    'palabras_clave' => $datos['claves'],
                    'area_aplicacion' => $datos['area'],
                    'objetivo_general' => $datos['general'],
                    'objetivos_especificos' => $datos['especificos'],
                    'estado' => $datos['estado'] === 'rechazado' && $datos['clave'] === 'copia-transparencia'
                        ? 'pendiente'
                        : $datos['estado'],
                    'id_creador' => $creadorId,
                    'id_instructor_asignado' => $instructorId,
                    'id_class_group' => $fichaId,
                ]
            );

            $this->recordar('proyecto', $datos['clave'], $proyecto->id);

            if ($proyecto->wasRecentlyCreated) {
                $fecha = now()->subDays($datos['dias'])->setTime(9, 0);
                $proyecto->forceFill([
                    'created_at' => $fecha,
                    'updated_at' => $fecha,
                    'huella_envio' => $datos['estado'] === 'borrador' ? null : $this->huella($proyecto),
                ])->saveQuietly();

                $this->equipo($proyecto, $datos['equipo']);
            }

            if ($datos['clave'] === 'copia-transparencia'
                && $proyecto->wasRecentlyCreated
                && ! DemoRegistry::existe('escenario', 'copia-rechazada')) {
                app(Recomputador::class)->detectar($proyecto, false);
                $proyecto->update(['estado' => 'rechazado']);
                app(Recomputador::class)->archivar($proyecto);
                DemoRegistry::registrar('escenario', 'copia-rechazada', $proyecto->id);
            }
        }
    }

    private function equipo(Project $proyecto, array $usuarios): void
    {
        foreach ($usuarios as $username) {
            $usuarioId = $this->registroId('usuario', $username);
            if (! $usuarioId) {
                continue;
            }

            $aprendiz = Apprentice::where('id_usuario', $usuarioId)->first();
            if (! $aprendiz) {
                continue;
            }

            ApprenticeProject::firstOrCreate([
                'id_aprendiz' => $aprendiz->id,
                'id_proyecto' => $proyecto->id,
            ]);
        }
    }

    private function huella(Project $proyecto): string
    {
        return sha1(json_encode([
            $proyecto->titulo,
            $proyecto->resumen,
            $proyecto->palabras_clave,
            $proyecto->area_aplicacion,
            $proyecto->objetivo_general,
            $proyecto->objetivos_especificos,
        ]));
    }

    private function datosProyectos(): array
    {
        return [
            [
                'clave' => 'iot-agricultura',
                'titulo' => 'Sistema IoT para Agricultura',
                'resumen' => 'Sistema de monitoreo inteligente para cultivos utilizando sensores IoT que miden humedad, temperatura y nutrientes del suelo, permitiendo la toma de decisiones en tiempo real para optimizar el riego y la fertilización.',
                'claves' => 'IoT, sensores, agricultura, monitoreo, automatización, datos, plataforma, transparencia',
                'area' => 'Tecnología e Informática',
                'estado' => 'pendiente', 'creador' => 'amartinez', 'instructor' => 'cruiz', 'ficha' => '2568',
                'general' => 'Optimizar el uso del agua y los nutrientes en cultivos mediante el monitoreo continuo de variables ambientales con una red de sensores IoT y visualización de datos en plataforma.',
                'especificos' => [
                    'Diseñar una red de sensores IoT para medir humedad, temperatura y nutrientes del suelo.',
                    'Desarrollar una plataforma web que visualice los datos en tiempo real y datos abiertos.',
                    'Implementar alertas tempranas y transparencia de datos ante condiciones críticas en el cultivo.',
                ],
                'equipo' => ['amartinez', 'jperez', 'lgomez'], 'dias' => 80,
            ],
            [
                'clave' => 'turismo-local',
                'titulo' => 'App Móvil para Turismo Local',
                'resumen' => 'Aplicación móvil que promueve el turismo local mostrando sitios de interés, rutas y eventos culturales, facilitando la exploración de destinos y la planificación de visitas.',
                'claves' => 'turismo, app móvil, cultura, rutas turísticas, geolocalización',
                'area' => 'Cultura y Entretenimiento',
                'estado' => 'pendiente', 'creador' => 'jperez', 'instructor' => 'cruiz', 'ficha' => '2568',
                'general' => 'Facilitar la exploración de destinos turísticos locales mediante una aplicación móvil con rutas y eventos culturales.',
                'especificos' => [
                    'Implementar sistema de geolocalización para rutas turísticas interactivas.',
                    'Crear un catálogo interactivo de sitios de interés cultural y natural.',
                ],
                'equipo' => ['jperez'], 'dias' => 70,
            ],
            [
                'clave' => 'elearning-musica',
                'titulo' => 'Plataforma E-learning para Música',
                'resumen' => 'Plataforma web para aprendizaje de instrumentos musicales con lecciones interactivas, catálogo de instrumentos, control de inventarios y stock, seguimiento de progreso y recursos multimedia para estudiantes de todos los niveles.',
                'claves' => 'e-learning, música, educación, instrumentos, plataforma, inventarios, stock, control, catálogo',
                'area' => 'Tecnología e Informática',
                'estado' => 'pendiente', 'creador' => 'lgomez', 'instructor' => 'crodriguez', 'ficha' => '2634',
                'general' => 'Crear una plataforma de aprendizaje musical con lecciones interactivas y seguimiento del progreso del estudiante.',
                'especificos' => [
                    'Implementar control de inventarios de instrumentos.',
                    'Desarrollar reproductor de audio con control de velocidad y repetición.',
                ],
                'equipo' => ['lgomez', 'amartinez'], 'dias' => 65,
            ],
            [
                'clave' => 'ventas-online',
                'titulo' => 'Plataforma de Ventas Online',
                'resumen' => 'Aplicación web de comercio electrónico para pequeños comercios con catálogo de productos, carrito de compras, control de inventarios y stock, y pasarela de pagos.',
                'claves' => 'e-commerce, ventas, pagos, catálogo, comercio, inventarios, stock, control',
                'area' => 'Tecnología e Informática',
                'estado' => 'pendiente', 'creador' => 'mgonzalez', 'instructor' => 'cruiz', 'ficha' => '2568',
                'general' => 'Facilitar la digitalización de pequeños comercios mediante una plataforma de ventas en línea con catálogo, carrito y pagos, integrada al control de inventarios y stock.',
                'especificos' => [
                    'Implementar un catálogo de productos con búsqueda y filtros.',
                    'Desarrollar un carrito de compras con proceso de pago seguro e inventario.',
                    'Crear un panel de administración para la gestión de pedidos y stock.',
                ],
                'equipo' => ['mgonzalez', 'jperez'], 'dias' => 60,
            ],
            [
                'clave' => 'inventarios',
                'titulo' => 'Sistema de Gestión de Inventarios',
                'resumen' => 'Herramienta para control de inventarios y ventas de almacén con catálogo de productos, comercio electrónico, alertas de stock y reportes de trazabilidad.',
                'claves' => 'inventarios, stock, almacén, control, reportes, ventas, comercio, catálogo, plataforma',
                'area' => 'Logística y Operaciones',
                'estado' => 'aprobado', 'creador' => 'mgonzalez', 'instructor' => 'cruiz', 'ficha' => '2568',
                'general' => 'Controlar el inventario y las ventas del almacén con reportes de trazabilidad y alertas de stock.',
                'especificos' => [
                    'Registrar entradas y salidas de mercancía.',
                    'Configurar alertas de stock mínimo.',
                    'Generar reportes de trazabilidad por lote.',
                ],
                'equipo' => ['mgonzalez'], 'dias' => 50,
            ],
            [
                'clave' => 'bienestar-deportivo',
                'titulo' => 'App de Bienestar Deportivo',
                'resumen' => 'Aplicación móvil de seguimiento de rutinas de ejercicio, registro de actividad y metas de bienestar para la comunidad SENA.',
                'claves' => 'deporte, bienestar, rutinas, actividad, salud',
                'area' => 'Deporte y Recreación',
                'estado' => 'pendiente', 'creador' => 'jperez', 'instructor' => 'cruiz', 'ficha' => '2568',
                'general' => 'Acompañar a la comunidad SENA en el seguimiento de rutinas de ejercicio y metas de bienestar.',
                'especificos' => [
                    'Registrar rutinas y progreso de ejercicio.',
                    'Implementar recordatorios de actividad física.',
                    'Visualizar estadísticas de rendimiento.',
                ],
                'equipo' => ['jperez'], 'dias' => 45,
            ],
            [
                'clave' => 'transparencia',
                'titulo' => 'Portal de Transparencia SENA',
                'resumen' => 'Portal web de datos abiertos que publica información sobre presupuesto, contratación e indicadores institucionales del SENA, con monitoreo de sensores IoT, visualización de datos y plataforma en tiempo real.',
                'claves' => 'transparencia, datos abiertos, presupuesto, contratación, IoT, sensores, monitoreo, plataforma, datos',
                'area' => 'Gobierno y Ciudadanía',
                'estado' => 'aprobado', 'creador' => 'lgomez', 'instructor' => 'crodriguez', 'ficha' => '2634',
                'general' => 'Publicar información institucional del SENA en un portal de datos abiertos con monitoreo en tiempo real.',
                'especificos' => [
                    'Implementar visualizaciones de datos interactivas.',
                    'Integrar fuentes de datos institucionales e IoT.',
                ],
                'equipo' => ['lgomez'], 'dias' => 40,
            ],
            [
                'clave' => 'chatbot-academico',
                'titulo' => 'Chatbot de Atención Académica',
                'resumen' => 'Asistente conversacional que responde dudas sobre programas de formación, requisitos de matrícula y trámites académicos.',
                'claves' => 'chatbot, atención, académico, asistente, IA',
                'area' => 'Tecnología e Informática',
                'estado' => 'rechazado', 'creador' => 'amartinez', 'instructor' => 'cruiz', 'ficha' => '2568',
                'general' => 'Resolver dudas académicas frecuentes mediante un asistente conversacional.',
                'especificos' => [
                    'Implementar respuestas automáticas a preguntas frecuentes.',
                    'Integrar con la base de datos de programas.',
                ],
                'equipo' => ['amartinez', 'lgomez'], 'dias' => 35,
            ],
            [
                'clave' => 'copia-inventarios',
                'titulo' => 'Sistema de Gestión de Inventarios',
                'resumen' => 'Herramienta para control de inventarios y ventas de almacén con catálogo de productos, comercio electrónico, alertas de stock y reportes de trazabilidad.',
                'claves' => 'inventarios, stock, almacén, control, reportes, ventas, comercio, catálogo, plataforma',
                'area' => 'Logística y Operaciones',
                'estado' => 'pendiente', 'creador' => 'amartinez', 'instructor' => 'cruiz', 'ficha' => '2568',
                'general' => 'Controlar el inventario y las ventas del almacén con reportes de trazabilidad y alertas de stock.',
                'especificos' => [
                    'Registrar entradas y salidas de mercancía.',
                    'Configurar alertas de stock mínimo.',
                    'Generar reportes de trazabilidad por lote.',
                ],
                'equipo' => ['amartinez', 'jperez'], 'dias' => 25,
            ],
            [
                'clave' => 'parafrasis-inventarios',
                'titulo' => 'Plataforma de Existencias del Almacén',
                'resumen' => 'Herramienta para la administración de las existencias y la comercialización del almacén, con catálogo de productos, alertas de stock e informes de trazabilidad.',
                'claves' => 'existencias, stock, almacén, administración, informes, comercialización, catálogo',
                'area' => 'Logística y Operaciones',
                'estado' => 'aprobado', 'creador' => 'jperez', 'instructor' => 'cruiz', 'ficha' => '2568',
                'general' => 'Administrar las existencias y las ventas del almacén con informes de trazabilidad y alertas de stock.',
                'especificos' => [
                    'Anotar entradas y salidas de mercancía.',
                    'Configurar avisos de stock mínimo.',
                    'Generar informes de trazabilidad por lote.',
                ],
                'equipo' => ['jperez'], 'dias' => 20,
            ],
            [
                'clave' => 'copia-transparencia',
                'titulo' => 'Portal de Transparencia SENA',
                'resumen' => 'Portal web de datos abiertos que publica información sobre presupuesto, contratación e indicadores institucionales del SENA, con monitoreo de sensores IoT, visualización de datos y plataforma en tiempo real.',
                'claves' => 'transparencia, datos abiertos, presupuesto, contratación, IoT, sensores, monitoreo, plataforma, datos',
                'area' => 'Gobierno y Ciudadanía',
                'estado' => 'rechazado', 'creador' => 'pmorales', 'instructor' => 'crodriguez', 'ficha' => '2634',
                'general' => 'Publicar información institucional del SENA en un portal de datos abiertos con monitoreo en tiempo real.',
                'especificos' => [
                    'Implementar visualizaciones de datos interactivas.',
                    'Integrar fuentes de datos institucionales e IoT.',
                ],
                'equipo' => ['pmorales', 'lgomez'], 'dias' => 15,
            ],
            [
                'clave' => 'citas-odontologicas',
                'titulo' => 'Gestión de Citas Odontológicas',
                'resumen' => 'Aplicación web para agendar y administrar citas odontológicas, con recordatorios a los pacientes y control de la agenda de los odontólogos.',
                'claves' => 'citas, odontología, pacientes, agenda, salud',
                'area' => 'Salud',
                'estado' => 'pendiente', 'creador' => 'mgonzalez', 'instructor' => 'cruiz', 'ficha' => '2568',
                'general' => 'Facilitar la programación de citas odontológicas y reducir las inasistencias.',
                'especificos' => [
                    'Registrar pacientes y tratamientos.',
                    'Agendar y recordar citas.',
                    'Reportar la ocupación de la agenda.',
                ],
                'equipo' => ['mgonzalez', 'amartinez', 'jperez'], 'dias' => 10,
            ],
            [
                'clave' => 'gastos-personales',
                'titulo' => 'Control de Gastos Personales',
                'resumen' => 'Aplicación para registrar ingresos y gastos personales, clasificar las transacciones y visualizar el estado de las finanzas con gráficos.',
                'claves' => 'gastos, presupuesto, finanzas, ahorro, ingresos',
                'area' => 'Finanzas',
                'estado' => 'pendiente', 'creador' => 'lgomez', 'instructor' => 'crodriguez', 'ficha' => '2634',
                'general' => 'Ayudar a las personas a controlar su presupuesto y sus gastos.',
                'especificos' => [
                    'Registrar ingresos y gastos.',
                    'Clasificar por categoría.',
                    'Visualizar reportes mensuales.',
                ],
                'equipo' => ['lgomez', 'pmorales'], 'dias' => 7,
            ],
            [
                'clave' => 'reservas-laboratorios',
                'titulo' => 'Sistema de Reservas de Laboratorios',
                'resumen' => 'Aplicación para reservar laboratorios y equipos con disponibilidad en tiempo real.',
                'claves' => 'reservas, laboratorios, disponibilidad',
                'area' => 'Tecnología e Informática',
                'estado' => 'borrador', 'creador' => 'amartinez', 'instructor' => 'cruiz', 'ficha' => '2568',
                'general' => 'Organizar la reserva de laboratorios y equipos de formación.',
                'especificos' => [
                    'Registrar disponibilidad de laboratorios.',
                    'Gestionar solicitudes de reserva.',
                ],
                'equipo' => ['amartinez'], 'dias' => 5,
            ],
            [
                'clave' => 'ciclo-rechazo',
                'titulo' => 'Sistema de Gestión de Inventarios con trazabilidad',
                'resumen' => 'Herramienta para controlar inventarios y ventas del almacén con trazabilidad por lote y alertas de stock.',
                'claves' => 'inventarios, stock, almacén, control, trazabilidad, ventas',
                'area' => 'Logística y Operaciones',
                'estado' => 'pendiente', 'creador' => 'lgomez', 'instructor' => 'crodriguez', 'ficha' => '2634',
                'general' => 'Controlar los inventarios y las ventas del almacén con trazabilidad por lote.',
                'especificos' => [
                    'Registrar entradas y salidas por lote.',
                    'Configurar alertas de stock mínimo.',
                ],
                'equipo' => ['lgomez'], 'dias' => 3,
            ],
            [
                'clave' => 'intervencion-biomedicos',
                'titulo' => 'Control de Inventario de Equipos Biomédicos',
                'resumen' => 'Sistema para registrar, mantener y auditar el inventario de equipos biomédicos de una clínica.',
                'claves' => 'inventario, equipos, biomédicos, mantenimiento',
                'area' => 'Salud',
                'estado' => 'pendiente', 'creador' => 'lsanchez', 'instructor' => 'crodriguez', 'ficha' => '3102',
                'general' => 'Controlar el ciclo de vida de los equipos biomédicos.',
                'especificos' => [
                    'Registrar equipos y mantenimientos.',
                    'Auditar el inventario por sede.',
                ],
                'equipo' => ['lsanchez'], 'dias' => 1,
            ],
        ];
    }

    // ---------------------------------------------------------------- Similitudes

    private function sembrarSimilitudes(): void
    {
        $config = MotorConfig::actual();

        if (! DemoRegistry::existe('motor', 'config')) {
            DemoRegistry::registrar('motor', 'config', $config->id);
        }

        app(Recomputador::class)->recalcular(true);
        DemoRegistry::registrar('motor', 'recalculo', $config->id);
    }

    // ---------------------------------------------------------------- Interacciones

    private function sembrarInteracciones(): void
    {
        $this->sembrarComentarios();
        $this->sembrarReportes();
        $this->sembrarHistoriales();
        $this->sembrarNotificaciones();
        $this->sembrarAuditorias();
    }

    private function sembrarComentarios(): void
    {
        $datos = [
            ['clave' => 'ventas-1', 'proyecto' => 'ventas-online', 'usuario' => 'cruiz', 'texto' => 'El proyecto necesita mejorar la sección de análisis de requisitos. Se recomienda ampliar la documentación técnica antes de continuar con el desarrollo.'],
            ['clave' => 'ventas-2', 'proyecto' => 'ventas-online', 'usuario' => 'mgonzalez', 'texto' => 'He realizado los ajustes sugeridos en la documentación. La nueva versión incluye diagramas de flujo y casos de uso detallados. Quedo atento a más retroalimentación.', 'respondeA' => 'ventas-1'],
            ['clave' => 'ventas-3', 'proyecto' => 'ventas-online', 'usuario' => 'cruiz', 'texto' => 'La propuesta inicial tiene buen enfoque, pero falta definir mejor los entregables del primer sprint. Recomiendo revisar la guía de proyectos para alinear expectativas.'],
            ['clave' => 'turismo-1', 'proyecto' => 'turismo-local', 'usuario' => 'cruiz', 'texto' => 'Buen avance, revisar documentación.'],
            ['clave' => 'copia-transparencia-1', 'proyecto' => 'copia-transparencia', 'usuario' => 'crodriguez', 'texto' => 'Propuesta rechazada: el motor detectó una alta similitud con una propuesta ya aprobada. Se recomienda replantear el enfoque y diferenciar los objetivos.'],
            ['clave' => 'ciclo-1', 'proyecto' => 'ciclo-rechazo', 'usuario' => 'crodriguez', 'texto' => 'Propuesta rechazada: replantea el enfoque para diferenciarla de la propuesta aprobada.'],
        ];

        foreach ($datos as $fila) {
            $id = $this->registroId('comentario', $fila['clave']);

            if ($id && Comment::find($id)) {
                continue;
            }

            $proyecto = $this->proyectoDemo($fila['proyecto']);
            $usuarioId = $this->usuarioId($fila['usuario']);

            if (! $proyecto || ! $usuarioId) {
                continue;
            }

            $respuestaId = isset($fila['respondeA'])
                ? $this->registroId('comentario', $fila['respondeA'])
                : null;

            $comentario = Comment::firstOrCreate(
                [
                    'id_proyecto' => $proyecto->id,
                    'id_usuario' => $usuarioId,
                    'texto' => $fila['texto'],
                ],
                ['respuesta_a' => $respuestaId]
            );

            if ($comentario->wasRecentlyCreated) {
                $fecha = $proyecto->created_at->copy()->addDays(2);
                $comentario->forceFill(['created_at' => $fecha, 'updated_at' => $fecha])->saveQuietly();
            }

            $this->recordar('comentario', $fila['clave'], $comentario->id);
        }
    }

    private function sembrarReportes(): void
    {
        $reportes = [
            ['clave' => 'reporte-1', 'titulo' => 'Pantalla blanca en Dashboard', 'descripcion' => 'Error al cargar la página de Dashboard, muestra pantalla blanca después de iniciar sesión', 'tipo' => 'sistema', 'estado' => 'pendiente', 'autor' => 'crodriguez'],
            ['clave' => 'reporte-2', 'titulo' => 'No se suben archivos PDF', 'descripcion' => 'No se pueden subir archivos PDF en la sección de evidencias del proyecto', 'tipo' => 'proyecto', 'estado' => 'pendiente', 'autor' => 'mgonzalez'],
            ['clave' => 'reporte-3', 'titulo' => 'Faltan notificaciones de revisión', 'descripcion' => 'El sistema no envía notificaciones cuando un instructor revisa un proyecto', 'tipo' => 'sistema', 'estado' => 'en_revision', 'autor' => 'amartinez1'],
            ['clave' => 'reporte-4', 'titulo' => 'Botón cerrar sesión roto', 'descripcion' => 'El botón de cerrar sesión no funciona correctamente en navegador Chrome', 'tipo' => 'sistema', 'estado' => 'en_revision', 'autor' => 'lsanchez'],
            ['clave' => 'reporte-5', 'titulo' => 'Reporte PDF corrupto', 'descripcion' => 'Error en la generación de reportes PDF, el archivo descargado está corrupto', 'tipo' => 'datos', 'estado' => 'resuelto', 'autor' => 'dramirez'],
            ['clave' => 'reporte-6', 'titulo' => 'Logos de proyectos no se muestran', 'descripcion' => 'Las imágenes de los logos de proyectos no se muestran en la vista de lista', 'tipo' => 'proyecto', 'estado' => 'rechazado', 'autor' => 'pmorales'],
        ];

        foreach ($reportes as $indice => $fila) {
            $id = $this->registroId('reporte', $fila['clave']);

            if ($id && BugReport::find($id)) {
                continue;
            }

            $autorId = $this->usuarioId($fila['autor']);

            if (! $autorId) {
                continue;
            }

            $reporte = BugReport::firstOrCreate(
                ['titulo' => $fila['titulo'], 'id_usuario' => $autorId],
                [
                    'descripcion' => $fila['descripcion'],
                    'tipo' => $fila['tipo'],
                    'estado' => $fila['estado'],
                    'fecha' => now()->subDays($indice + 1)->toDateString(),
                ]
            );

            if ($reporte->wasRecentlyCreated) {
                $fecha = now()->subDays($indice + 1);
                $reporte->forceFill(['created_at' => $fecha, 'updated_at' => $fecha])->saveQuietly();
            }

            $this->recordar('reporte', $fila['clave'], $reporte->id);
        }
    }

    private function sembrarHistoriales(): void
    {
        foreach ($this->estadoProyectos() as $clave => $estado) {
            $proyecto = $this->proyectoDemo($clave);

            if (! $proyecto) {
                continue;
            }

            $creadorId = (int) $proyecto->id_creador;
            $instructorId = $this->instructorUsuarioDe($proyecto);
            $base = $proyecto->created_at;

            $this->registrarHistorial($clave, 'creada', $creadorId, $base, ['titulo' => $proyecto->titulo]);

            if ($estado === 'borrador') {
                continue;
            }

            $this->registrarHistorial($clave, 'enviada', $creadorId, $base->copy()->addDay());

            if ($estado === 'aprobado') {
                $this->registrarHistorial($clave, 'aprobada', $instructorId, $base->copy()->addDays(2));
            }

            if ($estado === 'rechazado') {
                $this->registrarHistorial($clave, 'rechazada', $instructorId, $base->copy()->addDays(2), [
                    'observacion' => $clave === 'copia-transparencia'
                        ? 'El motor detectó una alta similitud con una propuesta aprobada.'
                        : 'La propuesta no cumple los criterios de originalidad y alcance.',
                ]);
            }
        }

        $ciclo = $this->proyectoDemo('ciclo-rechazo');
        if ($ciclo) {
            $instructorId = $this->instructorUsuarioDe($ciclo);
            $creadorId = (int) $ciclo->id_creador;
            $base = $ciclo->created_at;

            $this->registrarHistorial('ciclo-rechazo', 'rechazada', $instructorId, $base->copy()->addDays(2), [
                'observacion' => 'Propuesta rechazada: replantea el enfoque para diferenciarla de la propuesta aprobada.',
            ]);
            $this->registrarHistorial('ciclo-rechazo', 'actualizada', $creadorId, $base->copy()->addDays(3));
            $this->registrarHistorial('ciclo-rechazo', 'reenviada', $creadorId, $base->copy()->addDays(4));
        }

        $intervencion = $this->proyectoDemo('intervencion-biomedicos');
        if ($intervencion) {
            $instructorId = $this->instructorUsuarioDe($intervencion);
            $adminId = $this->usuarioId('a');
            $base = $intervencion->created_at;

            $this->registrarHistorial('intervencion-biomedicos', 'aprobada', $instructorId, $base->copy()->addDays(2));
            $this->registrarHistorial('intervencion-biomedicos', 'devuelta_revision', $adminId, $base->copy()->addDays(4), [
                'intervencion_admin' => true,
            ]);
        }
    }

    private function registrarHistorial(string $proyectoClave, string $accion, ?int $usuarioId, $fecha, array $detalle = []): void
    {
        $proyecto = $this->proyectoDemo($proyectoClave);

        if (! $proyecto || ! $usuarioId) {
            return;
        }

        $clave = 'historial:' . $proyectoClave . ':' . $accion;
        $id = $this->registroId('historial', $clave);

        if ($id && ProjectHistory::find($id)) {
            return;
        }

        $historial = ProjectHistory::firstOrCreate(
            [
                'id_proyecto' => $proyecto->id,
                'id_usuario' => $usuarioId,
                'accion' => $accion,
            ],
            ['detalle' => $detalle ?: null]
        );

        if ($historial->wasRecentlyCreated) {
            $historial->forceFill(['created_at' => $fecha, 'updated_at' => $fecha])->saveQuietly();
        }

        $this->recordar('historial', $clave, $historial->id);
    }

    private function sembrarNotificaciones(): void
    {
        foreach ($this->estadoProyectos() as $clave => $estado) {
            $proyecto = $this->proyectoDemo($clave);

            if (! $proyecto) {
                continue;
            }

            if ($estado === 'pendiente') {
                $this->registrarNotificacion(
                    'revision:' . $clave,
                    $this->instructorUsuarioDe($proyecto),
                    'Nueva propuesta pendiente de revisión: "' . $proyecto->titulo . '"',
                    'revision',
                    'proyecto:' . $proyecto->id,
                    $proyecto->created_at->copy()->addDay()
                );
            }

            if ($estado === 'aprobado') {
                $this->registrarNotificacion(
                    'aprobada:' . $clave,
                    (int) $proyecto->id_creador,
                    'Tu propuesta "' . $proyecto->titulo . '" fue aprobada.',
                    'revision',
                    'proyecto:' . $proyecto->id,
                    $proyecto->created_at->copy()->addDays(2)
                );
            }

            if ($estado === 'rechazado') {
                $this->registrarNotificacion(
                    'rechazada:' . $clave,
                    (int) $proyecto->id_creador,
                    'Tu propuesta "' . $proyecto->titulo . '" fue rechazada. Observación: revisa la retroalimentación del instructor.',
                    'revision',
                    'proyecto:' . $proyecto->id,
                    $proyecto->created_at->copy()->addDays(2)
                );
            }
        }

        $ficha = ClassGroup::find($this->registroId('ficha', '2568'));
        $instructorFicha = $ficha ? optional(Instructor::find($ficha->id_instructor))->id_usuario : null;
        if ($ficha && $instructorFicha) {
            $this->registrarNotificacion(
                'ficha-movimiento',
                $instructorFicha,
                'El aprendiz Andrés Cifuentes se unió a la ficha "Analisis y Desarrollo 2568".',
                'sistema',
                'ficha:' . $ficha->id,
                now()->subDays(1)
            );
        }

        $admins = GeneralUser::where('rol', 'admin')->where('estado', true)->orderBy('id')->pluck('id');

        foreach (BugReport::orderBy('id')->get() as $reporte) {
            if (in_array($reporte->estado, ['resuelto', 'rechazado'], true)) {
                $titulo = 'Tu solicitud de soporte #' . $reporte->id . ' fue '
                    . ($reporte->estado === 'rechazado' ? 'rechazada.' : 'atendida.');
                $this->registrarNotificacion(
                    'soporte-autor:' . $reporte->id,
                    (int) $reporte->id_usuario,
                    $titulo,
                    'sistema',
                    'reporte:' . $reporte->id,
                    $reporte->created_at->copy()->addDay()
                );
                continue;
            }

            foreach ($admins as $adminId) {
                $this->registrarNotificacion(
                    'soporte-admin:' . $reporte->id . ':' . $adminId,
                    (int) $adminId,
                    'Nuevo reporte de falla: "' . ($reporte->titulo ?: 'Sin título') . '"',
                    'sistema',
                    'reporte:' . $reporte->id,
                    $reporte->created_at
                );
            }
        }
    }

    private function registrarNotificacion(string $clave, ?int $usuarioId, string $titulo, string $tipo, ?string $enlace, $fecha, bool $leida = false): void
    {
        if (! $usuarioId) {
            return;
        }

        $id = $this->registroId('notificacion', $clave);

        if ($id && Notification::find($id)) {
            return;
        }

        $titulo = mb_substr($titulo, 0, 250);
        $notificacion = Notification::firstOrCreate(
            [
                'id_usuario' => $usuarioId,
                'titulo' => $titulo,
                'enlace' => $enlace,
            ],
            [
                'tipo' => $tipo,
                'leida' => $leida,
                'fecha' => $fecha->toDateString(),
            ]
        );

        if ($notificacion->wasRecentlyCreated) {
            $notificacion->forceFill(['created_at' => $fecha, 'updated_at' => $fecha])->saveQuietly();
        }

        $this->recordar('notificacion', $clave, $notificacion->id);
    }

    private function sembrarAuditorias(): void
    {
        $adminId = $this->usuarioId('a');

        foreach (CuentasDemo::CUENTAS as $username => $cuenta) {
            $usuario = GeneralUser::where('username', $username)->first();

            if (! $usuario) {
                continue;
            }

            $this->registrarAuditoria('usuario:' . $username, 'crear_usuario', 'general_users', $usuario->id, [
                'correo' => $usuario->correo,
                'username' => $usuario->username,
                'documento' => Pii::documento($usuario->tipo_documento, $usuario->numero_documento),
                'rol' => $usuario->rol,
            ], $adminId, now()->subDays(30));
        }

        foreach (['2568', '2634', '3102', '2801'] as $numero) {
            $ficha = ClassGroup::find($this->registroId('ficha', $numero));

            if (! $ficha) {
                continue;
            }

            $this->registrarAuditoria('ficha:' . $numero, 'crear_ficha', 'class_groups', $ficha->id, [
                'codigo' => $ficha->codigo,
                'numero' => $ficha->numero,
                'id_instructor' => $ficha->id_instructor,
            ], $adminId, now()->subDays(20));
        }

        foreach ($this->estadoProyectos() as $clave => $estado) {
            if ($estado === 'borrador') {
                continue;
            }

            $proyecto = $this->proyectoDemo($clave);

            if (! $proyecto) {
                continue;
            }

            $this->registrarAuditoria('enviar:' . $clave, 'enviar_propuesta', 'projects', $proyecto->id, [
                'titulo' => $proyecto->titulo,
            ], (int) $proyecto->id_creador, $proyecto->created_at->copy()->addDay());

            if (in_array($estado, ['aprobado', 'rechazado'], true)) {
                $this->registrarAuditoria('revisar:' . $clave, 'revisar_propuesta', 'projects', $proyecto->id, [
                    'de' => 'pendiente',
                    'a' => $estado,
                    'titulo' => $proyecto->titulo,
                ], $this->instructorUsuarioDe($proyecto), $proyecto->created_at->copy()->addDays(2));
            }
        }

        $ficha = ClassGroup::find($this->registroId('ficha', '2568'));
        if ($ficha) {
            $this->registrarAuditoria('ficha-movimiento', 'agregar_aprendiz', 'class_groups', $ficha->id, [
                'usuario' => $this->usuarioId('acifuentes'),
                'creada' => true,
                'activacion_enviada' => false,
            ], $this->usuarioId('cruiz'), now()->subDays(1));
        }

        $config = MotorConfig::actual();
        $this->registrarAuditoria('config-motor', 'config_motor', 'motor_configs', $config->id, [
            'umbral' => (float) $config->umbral,
            'meses' => (int) $config->meses,
        ], $adminId, now()->subDays(5));

        $resuelto = BugReport::where('estado', 'resuelto')->first();
        if ($resuelto) {
            $this->registrarAuditoria('resolver-soporte:' . $resuelto->id, 'resolver_soporte', 'bug_reports', $resuelto->id, [
                'estado' => 'resuelto',
            ], $adminId, $resuelto->created_at->copy()->addDay());
        }
    }

    private function registrarAuditoria(string $clave, string $accion, string $entidad, int $entidadId, array $detalle, ?int $usuarioId, $fecha): void
    {
        $id = $this->registroId('auditoria', $clave);

        if ($id && AuditLog::find($id)) {
            return;
        }

        $existente = AuditLog::where('accion', $accion)
            ->where('entidad', $entidad)
            ->where('entidad_id', $entidadId)
            ->first();

        if ($existente) {
            $this->recordar('auditoria', $clave, $existente->id);
            return;
        }

        $log = AuditLog::create([
            'id_usuario' => $usuarioId,
            'accion' => $accion,
            'entidad' => $entidad,
            'entidad_id' => $entidadId,
            'detalle' => Pii::detalle($detalle),
            'ip' => '127.0.0.1',
        ]);

        $log->forceFill(['created_at' => $fecha, 'updated_at' => $fecha])->saveQuietly();
        $this->recordar('auditoria', $clave, $log->id);
    }

    private function estadoProyectos(): array
    {
        return [
            'iot-agricultura' => 'pendiente',
            'turismo-local' => 'pendiente',
            'elearning-musica' => 'pendiente',
            'ventas-online' => 'pendiente',
            'inventarios' => 'aprobado',
            'bienestar-deportivo' => 'pendiente',
            'transparencia' => 'aprobado',
            'chatbot-academico' => 'rechazado',
            'copia-inventarios' => 'pendiente',
            'parafrasis-inventarios' => 'aprobado',
            'copia-transparencia' => 'rechazado',
            'citas-odontologicas' => 'pendiente',
            'gastos-personales' => 'pendiente',
            'reservas-laboratorios' => 'borrador',
            'ciclo-rechazo' => 'pendiente',
            'intervencion-biomedicos' => 'pendiente',
        ];
    }

    // ---------------------------------------------------------------- Helpers

    private function registroId(string $tipo, string $clave): ?int
    {
        return DemoRegistry::id($tipo, (string) $clave);
    }

    private function recordar(string $tipo, string $clave, int $id): void
    {
        DemoRegistry::registrar($tipo, (string) $clave, $id);
    }

    private function usuarioId(string $clave): ?int
    {
        return $this->registroId('usuario', $clave);
    }

    private function proyectoDemo(string $clave): ?Project
    {
        $id = $this->registroId('proyecto', $clave);

        return $id ? Project::find($id) : null;
    }

    private function instructorUsuarioDe(Project $proyecto): ?int
    {
        $instructor = Instructor::find($proyecto->id_instructor_asignado);

        return $instructor ? (int) $instructor->id_usuario : null;
    }
}
