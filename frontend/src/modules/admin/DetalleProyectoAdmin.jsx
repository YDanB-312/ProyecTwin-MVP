import { Link, useLocation } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'

const miembros = [
  { nombre: 'Maria Gonzalez', rol: 'Creador' },
  { nombre: 'Juan Pérez', rol: 'Integrante' },
  { nombre: 'Laura Gómez', rol: 'Integrante' },
]

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/admin/proyectos', label: 'Proyectos' },
  { label: 'Detalle del Proyecto' },
]

export default function DetalleProyectoAdmin() {
  const location = useLocation()
  const proyecto = location.state?.proyecto || null
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
      <div className="contenedor-gestion">
        <PageHeader
          title="Detalle del Proyecto"
          icon="folder-open"
          breadcrumb={breadcrumb}
          actions={<Link to="/admin/proyectos" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Información General" icon="info-circle">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Nombre del Proyecto</div>
              <div className="detalle-valor">{proyecto ? proyecto.titulo : 'Sistema IoT para Agricultura de Precisión'}</div>
            </div>
            <div>
              <div className="detalle-label">Programa de Formación</div>
              <div className="detalle-valor">ADSO - Análisis y Desarrollo de Sistemas</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de Creación</div>
              <div className="detalle-valor">{proyecto ? proyecto.fecha : '15/03/2026'}</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className="badge badge-exito">Aprobado</span></p>
            </div>
            <div>
              <div className="detalle-label">Instructor</div>
              <div className="detalle-valor">{proyecto ? proyecto.instructor : 'Carlos Ruiz'}</div>
            </div>
            <div>
              <div className="detalle-label">Ficha</div>
              <div className="detalle-valor">ADSO-2568</div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Descripción</div>
              <div className="detalle-valor-texto">Desarrollo de un sistema basado en sensores IoT para monitorear variables ambientales en cultivos, permitiendo la toma de decisiones en tiempo real para optimizar el riego y la fertilización.</div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Integrantes del Equipo" icon="users">
          <div className="detalle-grid-moderno">
            {miembros.map((m, i) => (
              <div key={i} className="flex-row team-row">
                <div>
                  <strong>{m.nombre}</strong>
                  <br /><span className="texto-claro">{m.rol}</span>
                </div>
              </div>
            ))}
          </div>
        </DataPanel>

        <div className="acciones-finales">
          <Link to="/admin/proyectos" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
        </div>
      </div>
    </DashboardLayout>
  )
}
