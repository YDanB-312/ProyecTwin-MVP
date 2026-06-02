import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/gestion-usuarios.css'

const proyectos = [
  { nombre: 'Sistema IoT para Agricultura de Precision', estado: 'Activo', badge: 'exito', fecha: '15/03/2026' },
  { nombre: 'App Movil para Inventarios', estado: 'Pendiente', badge: 'advertencia', fecha: '20/02/2026' },
]

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/admin/gestion-usuarios', label: 'Gestion Usuarios' },
  { label: 'Detalle de Usuario' },
]

export default function DetalleUsuario() {
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12}>
      <div className="contenedor-gestion">
        <PageHeader
          title="Detalle de Usuario"
          icon="user"
          breadcrumb={breadcrumb}
          actions={<Link to="/admin/gestion-usuarios" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Informacion Personal" icon="id-card">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Nombre Completo</div>
              <div className="detalle-valor">Ana Martinez Lopez</div>
            </div>
            <div>
              <div className="detalle-label">Rol</div>
              <p><span className="badge badge-exito"><i className="fas fa-user-graduate"></i> Aprendiz</span></p>
            </div>
            <div>
              <div className="detalle-label">Documento</div>
              <div className="detalle-valor">1023456789</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className="badge badge-exito"><i className="fas fa-circle"></i> Activo</span></p>
            </div>
            <div>
              <div className="detalle-label">Correo Electronico</div>
              <div className="detalle-valor">ana.martinez@soy.sena.edu.co</div>
            </div>
            <div>
              <div className="detalle-label">Telefono</div>
              <div className="detalle-valor">3235421165</div>
            </div>
            <div>
              <div className="detalle-label">Programa de Formacion</div>
              <div className="detalle-valor">ADSO - Analisis y Desarrollo de Sistemas</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de Registro</div>
              <div className="detalle-valor">10/01/2026</div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Proyectos Asociados" icon="folder-open">
          <div className="tabla-scroll">
            <table className="tabla-usuarios">
              <thead>
                <tr>
                  <th>Proyecto</th>
                  <th>Estado</th>
                  <th>Fecha Creacion</th>
                </tr>
              </thead>
              <tbody>
                {proyectos.map((p, i) => (
                  <tr key={i}>
                    <td>{p.nombre}</td>
                    <td><span className={`badge badge-${p.badge}`}>{p.estado}</span></td>
                    <td>{p.fecha}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </DataPanel>

        <div className="acciones-finales" style={{ marginTop: 'var(--space-lg)', display: 'flex', gap: '10px' }}>
          <Link to="/admin/gestion-usuarios" className="btn-primario"><i className="fas fa-edit"></i> Editar Usuario</Link>
          <Link to="/admin/gestion-usuarios" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
        </div>
      </div>
    </DashboardLayout>
  )
}
