import { Link, useParams, useLocation } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'
import '../../assets/styles/pages/mis-proyectos.css'

const propuestas = [
  {
    id: 1,
    titulo: 'Sistema IoT para Agricultura',
    aprendiz: 'Ana Martínez',
    programa: 'ADSO',
    estado: 'pendiente',
    created_at: '15/11/2023',
    instructor: 'Carlos Ruiz',
    linea_tecnologica: 'Tecnologías de la Información',
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
    created_at: '14/11/2023',
    instructor: 'Carlos Ruiz',
    linea_tecnologica: 'Diseño Gráfico',
    tipo_proyecto: 'Aplicación/Software',
    resumen: 'Aplicación móvil que promueve el turismo local mostrando sitios de interés, rutas y eventos culturales, facilitando la exploración de destinos y la planificación de visitas.',
    palabras_clave: 'turismo, app móvil, cultura, rutas turísticas, geolocalización',
    tecnologias: 'React Native, Node.js, MongoDB',
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
    programa: 'ADSO',
    estado: 'requiere_ajustes',
    created_at: '12/11/2023',
    instructor: 'Carlos Ruiz',
    linea_tecnologica: 'Tecnologías de la Información',
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
  const location = useLocation()
  const propuesta = propuestas.find(p => p.id === Number(id)) || propuestas[0]

  const estadoBadge = {
    pendiente: { clase: 'badge-pendiente', texto: 'Pendiente' },
    aprobado: { clase: 'badge-exito', texto: 'Aprobado' },
    rechazado: { clase: 'badge-peligro', texto: 'Rechazado' },
    requiere_ajustes: { clase: 'badge-advertencia', texto: 'Requiere Ajustes' },
  }

  const badge = estadoBadge[propuesta.estado] || estadoBadge.pendiente

  const desde = location.state?.desde
  const volverMap = {
    dashboard: { path: '/instructor/dashboard', label: 'Volver al Panel' },
    alertas: { path: '/instructor/alertas', label: 'Volver a Notificaciones' },
  }
  const volver = volverMap[desde] || { path: '/instructor/revision-propuestas', label: 'Volver a propuestas' }

  const breadcrumb = [
    { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  ]
  if (desde !== 'dashboard') {
    breadcrumb.push({ to: '/instructor/revision-propuestas', label: 'Revisión de Propuestas' })
  }
  breadcrumb.push({ label: propuesta.titulo })

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-revision">
        <PageHeader
          title={propuesta.titulo}
          icon="folder-open"
          breadcrumb={breadcrumb}
          actions={<Link to={volver.path} className="btn-secundario"><i className="fas fa-arrow-left"></i> {volver.label}</Link>}
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
                <span className="badge badge-peligro ml-8"><i className="fas fa-robot"></i> {propuesta.similitud}% similitud detectada</span>
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
              <div className="detalle-valor">{propuesta.created_at}</div>
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
                  <span key={i} className="tag-pill tag-pill-gris">{palabra}</span>
                ))}
              </div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Detalles Técnicos" icon="cogs">
          <div className="detalle-grid-moderno">
            <div className="detalle-grid-full">
              <div className="detalle-label">Tecnologías a Utilizar</div>
              <div className="detalle-valor mb-16">
                {propuesta.tecnologias.split(', ').map((tech, i) => (
                  <span key={i} className="tag-pill tag-pill-azul">{tech}</span>
                ))}
              </div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Objetivos Específicos</div>
              <ul className="detalle-ul-reset">
                {propuesta.objetivos.map((obj, i) => (
                  <li key={i}>{obj}</li>
                ))}
              </ul>
            </div>
            <div className="detalle-grid-full mt-16">
              <div className="detalle-label">Entregables Esperados</div>
              <ul className="detalle-ul-reset">
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
              <div key={i} className="flex-row flex-wrap team-row">
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
                    <button type="button" className="btn-icono editar" title="Editar observación" onClick={() => {}}><i className="fas fa-edit"></i></button>
                    <button type="button" className="btn-icono eliminar" title="Eliminar observación" onClick={() => {}}><i className="fas fa-trash-alt"></i></button>
                  </div>
                </div>
                <div className="observacion-contenido">
                  <p>{obs.texto}</p>
                </div>
              </div>
            ))}
          </div>
          <div className="observaciones-section">
            <h3><i className="fas fa-plus-circle"></i> Agregar Observación</h3>
            <form action="#" onSubmit={(e) => e.preventDefault()}>
              <div className="grupo-formulario">
                <label htmlFor="observacion" className="etiqueta">Comentario</label>
                <textarea id="observacion" className="textarea" placeholder="Escribe tu observación sobre el proyecto..." name="contenido"></textarea>
              </div>
              <div className="acciones-formulario">
                <button type="submit" className="btn-primario"><i className="fas fa-paper-plane"></i> Guardar Observación</button>
              </div>
            </form>
          </div>
        </DataPanel>

        <div className="acciones-finales">
          <Link to={volver.path} className="btn-secundario"><i className="fas fa-arrow-left"></i> {volver.label}</Link>
        </div>
      </div>
    </DashboardLayout>
  );
}

export default DetalleProyectoInstructor
