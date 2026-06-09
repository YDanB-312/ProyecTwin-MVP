import { Link, useParams } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'
import '../../assets/styles/pages/mis-proyectos.css'

const propuestas = [
  {
    id: 1,
    titulo: 'Sistema IoT para Agricultura de Precisión',
    aprendiz: 'Ana Martínez',
    programa: 'ADSO - Análisis y Desarrollo de Sistemas',
    estado: 'pendiente',
    fecha_creacion: '15/11/2023',
    instructor: 'Carlos Ruiz',
    linea_tecnologica: 'Tecnologías de la Información',
    duracion_estimada: 6,
    fecha_inicio_estimada: 'Enero 2024',
    tipo_proyecto: 'Aplicación/Software',
    resumen: 'Sistema de monitoreo inteligente para cultivos utilizando sensores IoT que miden humedad, temperatura y nutrientes del suelo, permitiendo la toma de decisiones en tiempo real para optimizar el riego y la fertilización.',
    palabras_clave: 'IoT, sensores, agricultura, monitoreo, automatización',
    tecnologias: 'Arduino, Python, Firebase',
    objetivos: [
      'Diseñar e implementar una red de sensores IoT para monitoreo de variables ambientales en cultivos.',
      'Desarrollar una plataforma web para visualización de datos en tiempo real.',
      'Implementar algoritmos de alerta temprana para condiciones críticas en los cultivos.',
      'Generar reportes automáticos de rendimiento y predicciones basadas en datos históricos.',
    ],
    entregables: [
      'Prototipo funcional del sistema de sensores IoT.',
      'Plataforma web con dashboard de monitoreo en tiempo real.',
      'Documentación técnica completa del sistema.',
      'Manual de usuario para la plataforma.',
    ],
    miembros: [
      { iniciales: 'AM', nombre: 'Ana Martínez', rol: 'Creador / Líder', clase: '' },
      { iniciales: 'JP', nombre: 'Juan Pérez', rol: 'Integrante', clase: 'avatar-secundario' },
      { iniciales: 'LG', nombre: 'Laura Gómez', rol: 'Integrante', clase: 'avatar-secundario' },
    ],
    similitud: 45,
    observaciones_adicionales: '',
  },
  {
    id: 2,
    titulo: 'App Móvil para Turismo Local',
    aprendiz: 'Juan Pérez',
    programa: 'Multimedia',
    estado: 'pendiente',
    fecha_creacion: '14/11/2023',
    instructor: 'Carlos Ruiz',
    linea_tecnologica: 'Diseño Gráfico',
    duracion_estimada: 4,
    fecha_inicio_estimada: 'Febrero 2024',
    tipo_proyecto: 'Aplicación/Software',
    resumen: 'Aplicación móvil que promueve el turismo local mostrando sitios de interés, rutas y eventos culturales, facilitando la exploración de destinos y la planificación de visitas.',
    palabras_clave: 'turismo, app móvil, cultura, rutas turísticas, geolocalización',
    tecnologias: 'Flutter, Node.js, MongoDB',
    objetivos: [
      'Desarrollar una aplicación móvil multiplataforma para promoción turística local.',
      'Implementar sistema de geolocalización para rutas turísticas interactivas.',
      'Crear un catálogo interactivo de sitios de interés cultural y natural.',
      'Integrar calendario de eventos culturales y notificaciones personalizadas.',
    ],
    entregables: [
      'App móvil funcional para Android e iOS.',
      'Panel de administración web.',
      'Base de datos de sitios turísticos y eventos.',
      'Manual de usuario y guía de administración.',
    ],
    miembros: [
      { iniciales: 'JP', nombre: 'Juan Pérez', rol: 'Creador / Líder', clase: '' },
    ],
    similitud: 0,
    observaciones_adicionales: '',
  },
  {
    id: 3,
    titulo: 'Plataforma E-learning para Música',
    aprendiz: 'Laura Gómez',
    programa: 'ADSO - Análisis y Desarrollo de Sistemas',
    estado: 'requiere_ajustes',
    fecha_creacion: '12/11/2023',
    instructor: 'Carlos Ruiz',
    linea_tecnologica: 'Tecnologías de la Información',
    duracion_estimada: 8,
    fecha_inicio_estimada: 'Marzo 2024',
    tipo_proyecto: 'Aplicación/Software',
    resumen: 'Plataforma web para aprendizaje de instrumentos musicales con lecciones interactivas, seguimiento de progreso y recursos multimedia para estudiantes de todos los niveles.',
    palabras_clave: 'e-learning, música, educación, instrumentos, plataforma',
    tecnologias: 'React, Django, PostgreSQL',
    objetivos: [
      'Crear una plataforma de aprendizaje musical con lecciones interactivas y multimedia.',
      'Implementar sistema de seguimiento de progreso del estudiante.',
      'Desarrollar reproductor de audio con control de velocidad y repetición.',
      'Diseñar un sistema de evaluación y retroalimentación automática.',
    ],
    entregables: [
      'Plataforma web completa con todos los módulos funcionales.',
      'Módulo de lecciones interactivas con reproductor integrado.',
      'Sistema de evaluación y seguimiento de progreso.',
      'Biblioteca de recursos musicales y ejercicios prácticos.',
    ],
    miembros: [
      { iniciales: 'LG', nombre: 'Laura Gómez', rol: 'Creador / Líder', clase: '' },
      { iniciales: 'AM', nombre: 'Ana Martínez', rol: 'Integrante', clase: 'avatar-secundario' },
    ],
    similitud: 0,
    observaciones_adicionales: 'Se requiere definir mejor el alcance del proyecto y especificar las tecnologías para la reproducción de audio/video.',
  },
]

const observacionesData = [
  { autor: 'Carlos Ruiz | Instructor', icono: 'user-tie', fecha: '10 may 2026', texto: 'El proyecto necesita mejorar la sección de análisis de requisitos. Se recomienda ampliar la documentación técnica antes de continuar con el desarrollo.' },
  { autor: 'Maria Gonzalez | Aprendiz', icono: 'user-graduate', fecha: '8 may 2026', texto: 'He realizado los ajustes sugeridos en la documentación. La nueva versión incluye diagramas de flujo y casos de uso detallados. Quedo atento a más retroalimentación.' },
  { autor: 'Carlos Ruiz | Instructor', icono: 'user-tie', fecha: '6 may 2026', texto: 'La propuesta inicial tiene buen enfoque, pero falta definir mejor los entregables del primer sprint. Recomiendo revisar la guía de proyectos para alinear expectativas.' },
]

function DetalleProyectoInstructor() {
  const { id } = useParams()
  const propuesta = propuestas.find(p => p.id === Number(id)) || propuestas[0]

  const estadoBadge = {
    pendiente: { clase: 'badge-pendiente', texto: 'Pendiente' },
    aprobado: { clase: 'badge-exito', texto: 'Aprobado' },
    rechazado: { clase: 'badge-peligro', texto: 'Rechazado' },
    requiere_ajustes: { clase: 'badge-advertencia', texto: 'Requiere Ajustes' },
  }

  const badge = estadoBadge[propuesta.estado] || estadoBadge.pendiente

  const breadcrumb = [
    { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
    { to: '/instructor/revision-propuestas', label: 'Revisión de Propuestas' },
    { label: propuesta.titulo },
  ]

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-revision">
        <PageHeader
          title={propuesta.titulo}
          icon="folder-open"
          breadcrumb={breadcrumb}
          actions={<Link to="/instructor/revision-propuestas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a propuestas</Link>}
        />

        <DataPanel title="Información General" icon="info-circle">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Nombre del Proyecto</div>
              <div className="detalle-valor">{propuesta.titulo}</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <span className={`badge ${badge.clase}`}>{badge.texto}</span>
              {propuesta.similitud > 0 && (
                <span className="badge badge-peligro" style={{ marginLeft: 8 }}><i className="fas fa-robot"></i> {propuesta.similitud}% similitud detectada</span>
              )}
            </div>
            <div>
              <div className="detalle-label">Aprendiz</div>
              <div className="detalle-valor">{propuesta.aprendiz}</div>
            </div>
            <div>
              <div className="detalle-label">Programa de Formación</div>
              <div className="detalle-valor">{propuesta.programa}</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de Creación</div>
              <div className="detalle-valor">{propuesta.fecha_creacion}</div>
            </div>
            <div>
              <div className="detalle-label">Instructor Asignado</div>
              <div className="detalle-valor">{propuesta.instructor}</div>
            </div>
            <div>
              <div className="detalle-label">Línea Tecnológica</div>
              <div className="detalle-valor">{propuesta.linea_tecnologica}</div>
            </div>
            <div>
              <div className="detalle-label">Duración Estimada</div>
              <div className="detalle-valor">{propuesta.duracion_estimada} meses</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de Inicio</div>
              <div className="detalle-valor">{propuesta.fecha_inicio_estimada}</div>
            </div>
            <div>
              <div className="detalle-label">Tipo de Proyecto</div>
              <div className="detalle-valor">{propuesta.tipo_proyecto}</div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Resumen y Palabras Clave" icon="file-alt">
          <div className="detalle-grid-moderno">
            <div className="detalle-grid-full">
              <div className="detalle-label">Resumen del Proyecto</div>
              <div className="detalle-valor-texto">{propuesta.resumen}</div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Palabras Clave</div>
              <div className="detalle-valor">
                {propuesta.palabras_clave.split(', ').map((palabra, i) => (
                  <span key={i} style={{ display: 'inline-block', background: '#e5e7eb', color: '#374151', fontSize: '0.8rem', padding: '2px 10px', borderRadius: 12, fontWeight: 500, marginRight: 6, marginBottom: 4 }}>{palabra}</span>
                ))}
              </div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Detalles Técnicos" icon="cogs">
          <div className="detalle-grid-moderno">
            <div className="detalle-grid-full">
              <div className="detalle-label">Tecnologías a Utilizar</div>
              <div className="detalle-valor" style={{ marginBottom: 16 }}>
                {propuesta.tecnologias.split(', ').map((tech, i) => (
                  <span key={i} style={{ display: 'inline-block', background: '#dbeafe', color: '#1e40af', fontSize: '0.8rem', padding: '2px 10px', borderRadius: 12, fontWeight: 500, marginRight: 6 }}>{tech}</span>
                ))}
              </div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Objetivos Específicos</div>
              <ul style={{ margin: 0, paddingLeft: 20, color: '#4b5563', lineHeight: 1.8 }}>
                {propuesta.objetivos.map((obj, i) => (
                  <li key={i}>{obj}</li>
                ))}
              </ul>
            </div>
            <div className="detalle-grid-full" style={{ marginTop: 16 }}>
              <div className="detalle-label">Entregables Esperados</div>
              <ul style={{ margin: 0, paddingLeft: 20, color: '#4b5563', lineHeight: 1.8 }}>
                {propuesta.entregables.map((ent, i) => (
                  <li key={i}>{ent}</li>
                ))}
              </ul>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Integrantes del Equipo" icon="users">
          <div className="detalle-grid-moderno">
            {propuesta.miembros.map((m, i) => (
              <div key={i} className="flex-row flex-wrap" style={{ padding: '8px 0' }}>
                <div className={`avatar-miembro avatar-sm ${m.clase}`}>{m.iniciales}</div>
                <div>
                  <strong className="texto-md">{m.nombre}</strong>
                  <br /><span className="texto-claro">{m.rol}</span>
                </div>
              </div>
            ))}
          </div>
        </DataPanel>

        {propuesta.observaciones_adicionales && (
          <DataPanel title="Observaciones Adicionales" icon="clipboard">
            <div className="detalle-grid-moderno">
              <div className="detalle-grid-full">
                <div className="detalle-valor-texto">{propuesta.observaciones_adicionales}</div>
              </div>
            </div>
          </DataPanel>
        )}

        <DataPanel title="Historial de Observaciones" icon="comments">
          <div className="lista-observaciones">
            {observacionesData.map((obs, i) => (
              <div key={i} className="observacion-item">
                <div className="observacion-header">
                  <span className="observacion-autor"><i className={`fas fa-${obs.icono}`}></i> {obs.autor}</span>
                  <span className="observacion-fecha">{obs.fecha}</span>
                  <div className="observacion-acciones">
                    <button type="button" className="btn-icono editar" title="Editar observación"><i className="fas fa-edit"></i></button>
                    <button type="button" className="btn-icono eliminar" title="Eliminar observación"><i className="fas fa-trash-alt"></i></button>
                  </div>
                </div>
                <div className="observacion-contenido">
                  <p>{obs.texto}</p>
                </div>
              </div>
            ))}
          </div>
          <div style={{ padding: 'var(--space-xl)', borderTop: '1px solid var(--color-borde)' }}>
            <h3 style={{ fontSize: '1rem', marginBottom: 12, display: 'flex', alignItems: 'center', gap: 8 }}><i className="fas fa-plus-circle"></i> Agregar Observación</h3>
            <form action="#">
              <div className="grupo-formulario" style={{ marginTop: '12px' }}>
                <label htmlFor="observacion" className="etiqueta">Comentario</label>
                <textarea id="observacion" className="textarea" placeholder="Escribe tu observación sobre el proyecto..." name="contenido"></textarea>
              </div>
              <div className="acciones-formulario" style={{ marginTop: '12px' }}>
                <button type="submit" className="btn-primario"><i className="fas fa-paper-plane"></i> Guardar Observación</button>
              </div>
            </form>
          </div>
        </DataPanel>

        <div className="acciones-finales">
          <Link to="/instructor/revision-propuestas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a propuestas</Link>
        </div>
      </div>
    </DashboardLayout>
  );
}

export default DetalleProyectoInstructor
