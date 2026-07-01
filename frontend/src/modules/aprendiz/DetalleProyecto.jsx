import { Link, useParams } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'
import '../../assets/styles/pages/mis-proyectos.css'

const proyectos = [
  {
    id: 0, nombre: 'Sistema de Gestión Académica',
    programa: 'ADSO - Análisis y Desarrollo de Software',
    fecha: '15/03/2026', estado: 'revision', estadoTexto: 'En revisión', badgeClase: 'badge-advertencia',
    descripcion: 'Desarrollo de un sistema integral para la gestión académica que permita administrar notas, horarios, asistencia y reportes académicos en tiempo real.',
    instructor: 'Carlos Ruiz',
    miembros: [
      { iniciales: 'MG', nombre: 'Maria Gonzalez', rol: 'Creador / Lider', clase: '' },
      { iniciales: 'JP', nombre: 'Juan Pérez', rol: 'Integrante', clase: 'avatar-secundario' },
      { iniciales: 'LG', nombre: 'Laura Gómez', rol: 'Integrante', clase: 'avatar-secundario' },
    ]
  },
  {
    id: 1, nombre: 'App Móvil para Inventarios',
    programa: 'ADSO - Análisis y Desarrollo de Software',
    fecha: '22/04/2026', estado: 'aprobado', estadoTexto: 'Aprobado', badgeClase: 'badge-exito',
    descripcion: 'Aplicación móvil para la gestión de inventarios que permite registrar, consultar y actualizar el stock de productos en tiempo real desde dispositivos móviles.',
    instructor: 'Ana Gómez',
    miembros: [
      { iniciales: 'MG', nombre: 'Maria Gonzalez', rol: 'Creador / Lider', clase: '' },
      { iniciales: 'JP', nombre: 'Juan Pérez', rol: 'Integrante', clase: 'avatar-secundario' },
    ]
  },
  {
    id: 2, nombre: 'Plataforma E-Learning',
    programa: 'ADSO - Análisis y Desarrollo de Software',
    fecha: '10/05/2026', estado: 'borrador', estadoTexto: 'Borrador', badgeClase: 'badge-neutral',
    descripcion: 'Plataforma de aprendizaje en línea con soporte para cursos virtuales, evaluaciones interactivas, foros de discusión y seguimiento del progreso del estudiante.',
    instructor: 'Miguel Lopez',
    miembros: [
      { iniciales: 'MG', nombre: 'Maria Gonzalez', rol: 'Creador / Lider', clase: '' },
      { iniciales: 'JP', nombre: 'Juan Pérez', rol: 'Integrante', clase: 'avatar-secundario' },
      { iniciales: 'LG', nombre: 'Laura Gómez', rol: 'Integrante', clase: 'avatar-secundario' },
      { iniciales: 'AM', nombre: 'Ana Martínez', rol: 'Integrante', clase: 'avatar-secundario' },
    ]
  },
]

function DetalleProyecto() {
  const { id } = useParams()
  const proyecto = proyectos[id] || proyectos[0]

  const breadcrumb = [
    { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
    { to: '/aprendiz/mis-proyectos', label: 'Mis Proyectos' },
    { label: proyecto.nombre },
  ]

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-proyectos">
        <PageHeader
          title={proyecto.nombre}
          icon="folder-open"
          breadcrumb={breadcrumb}
          actions={<Link to="/aprendiz/mis-proyectos" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Información General" icon="info-circle">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Nombre del Proyecto</div>
              <div className="detalle-valor">{proyecto.nombre}</div>
            </div>
            <div>
              <div className="detalle-label">Programa de Formación</div>
              <div className="detalle-valor">{proyecto.programa}</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de Creación</div>
              <div className="detalle-valor">{proyecto.fecha}</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className={`badge ${proyecto.badgeClase}`}>{proyecto.estadoTexto}</span></p>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Descripción</div>
              <div className="detalle-valor-texto">{proyecto.descripcion}</div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Integrantes del Equipo" icon="users">
          <div className="detalle-grid-moderno">
            {proyecto.miembros.map((m, i) => (
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

        <DataPanel title="Observaciones del Instructor" icon="comments">
          <div className="lista-observaciones">
            <div className="observacion-item">
              <div className="observacion-header">
                <span className="observacion-autor"><i className="fas fa-user-tie"></i> Carlos Ruiz | Instructor</span>
                <span className="observacion-fecha">10 may 2026</span>
              </div>
              <div className="observacion-contenido">
                <p>El proyecto necesita mejorar la sección de análisis de requisitos. Se recomienda ampliar la documentación técnica antes de continuar con el desarrollo.</p>
              </div>
            </div>
          </div>
        </DataPanel>

        <div className="margen-superior">
          <Link to="/aprendiz/mis-proyectos" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
        </div>
      </div>
    </DashboardLayout>
  );
}

export default DetalleProyecto;
