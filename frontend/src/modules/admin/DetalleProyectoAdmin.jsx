import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'
import '../../assets/styles/pages/mis-proyectos.css'

const miembros = [
  { iniciales: 'AM', nombre: 'Ana Martinez Lopez', rol: 'Creador / Lider', clase: '' },
  { iniciales: 'JP', nombre: 'Juan Perez', rol: 'Integrante', clase: 'avatar-secundario' },
  { iniciales: 'LG', nombre: 'Laura Gomez', rol: 'Integrante', clase: 'avatar-secundario' },
]

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Detalle del Proyecto' },
]

export default function DetalleProyectoAdmin() {
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12}>
      <div className="contenedor-gestion">
        <PageHeader
          title="Detalle del Proyecto"
          icon="folder-open"
          breadcrumb={breadcrumb}
          actions={<Link to="/admin/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver al Dashboard</Link>}
        />

        <DataPanel title="Informacion del Proyecto" icon="info-circle">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Nombre del Proyecto</div>
              <div className="detalle-valor">Sistema IoT para Agricultura de Precision</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className="badge badge-exito"><i className="fas fa-circle"></i> Activo</span> <span className="badge badge-primario"><i className="fas fa-robot"></i> Analizado por IA</span></p>
            </div>
            <div>
              <div className="detalle-label">Creador</div>
              <div className="detalle-valor">Ana Martinez Lopez</div>
            </div>
            <div>
              <div className="detalle-label">Instructor Asignado</div>
              <div className="detalle-valor">Carlos Rodriguez Diaz</div>
            </div>
            <div>
              <div className="detalle-label">Programa</div>
              <div className="detalle-valor">ADSO - Analisis y Desarrollo de Sistemas</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de Creacion</div>
              <div className="detalle-valor">15/03/2026</div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Descripcion</div>
              <div className="detalle-valor-texto">Desarrollo de un sistema basado en sensores IoT para monitorear variables ambientales en cultivos, permitiendo la toma de decisiones en tiempo real para optimizar el riego y la fertilizacion.</div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Integrantes del Equipo" icon="users">
          <div className="detalle-grid-moderno">
            {miembros.map((m, i) => (
              <div key={i} className="flex-row flex-wrap" style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '8px 0' }}>
                <div className={`avatar-miembro avatar-sm ${m.clase}`}>{m.iniciales}</div>
                <div>
                  <strong className="texto-md">{m.nombre}</strong>
                  <br /><span className="texto-claro">{m.rol}</span>
                </div>
              </div>
            ))}
          </div>
        </DataPanel>

        <DataPanel title="Similitudes Detectadas" icon="clone">
          <div className="contenedor-tabla">
            <table className="tabla-similitudes">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Proyecto Similar</th>
                  <th>Porcentaje</th>
                  <th>Fecha</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>1</td>
                  <td>Plataforma Educativa SENA</td>
                  <td><span className="badge badge-peligro">65%</span></td>
                  <td>10/04/2026</td>
                  <td><Link to="/admin/detalle-similitud" className="btn-ver"><i className="fas fa-eye"></i> Ver detalle</Link></td>
                </tr>
              </tbody>
            </table>
          </div>
        </DataPanel>

        <div style={{ marginTop: 'var(--space-lg)', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
          <Link to="/admin/reportes" className="btn-secundario"><i className="fas fa-chart-bar"></i> Ver en Reportes</Link>
          <Link to="/admin/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
        </div>
      </div>
    </DashboardLayout>
  )
}
