import { Link, useLocation } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import MetricCard from '../../components/MetricCard/MetricCard'
import '../../assets/styles/pages/gestion-usuarios.css'

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/admin/gestion-usuarios', label: 'Gestión Usuarios' },
  { label: 'Detalle de Usuario' },
]

export default function DetalleUsuario() {
  const location = useLocation()
  const userData = location.state?.usuario

  if (!userData) {
    return (
      <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
        <div className="contenedor-gestion fade-in">
          <PageHeader title="Usuario no encontrado" icon="exclamation-circle" breadcrumb={breadcrumb} actions={<Link to="/admin/gestion-usuarios" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>} />
          <div className="estado-vacio-moderno">
            <div className="estado-vacio-icono"><i className="fas fa-user"></i></div>
            <h3 className="estado-vacio-titulo">Usuario no encontrado</h3>
            <p className="estado-vacio-descripcion">No se encontró información del usuario.</p>
            <Link to="/admin/gestion-usuarios" className="btn-primario"><i className="fas fa-arrow-left"></i> Volver a Gestión de Usuarios</Link>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
      <div className="contenedor-gestion fade-in">
        <PageHeader
          title="Detalle de Usuario"
          icon="user"
          breadcrumb={breadcrumb}
          actions={<Link to="/admin/gestion-usuarios" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Información Personal" icon="id-card">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Nombre Completo</div>
              <div className="detalle-valor">{userData.nombre}</div>
            </div>
            <div>
              <div className="detalle-label">Rol</div>
              <p><span className={`badge badge-${userData.rol === 'Admin' ? 'peligro' : userData.rol === 'Instructor' ? 'advertencia' : 'exito'}`}><i className={`fas fa-${userData.rol === 'Admin' ? 'user-shield' : userData.rol === 'Instructor' ? 'chalkboard-teacher' : 'user-graduate'}`}></i> {userData.rol}</span></p>
            </div>
            <div>
              <div className="detalle-label">Programa de Formación</div>
              <div className="detalle-valor">{userData.programa || 'ADSO - Análisis y Desarrollo de Software'}</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de Registro</div>
              <div className="detalle-valor">{userData.fechaRegistro || '10/01/2026'}</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className={`badge ${userData.estado ? 'badge-exito' : 'badge-neutral'}`}><i className="fas fa-circle"></i> {userData.estado ? 'Activo' : 'Inactivo'}</span></p>
            </div>
            <div>
              <div className="detalle-label">Correo Electrónico</div>
              <div className="detalle-valor">{userData.correo}</div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Actividad en el Sistema" icon="activity">
          <div className="metricas-grid">
            <MetricCard icon="folder-open" value="2" label="Proyectos Creados" color="primario" />
            <MetricCard icon="search" value="1" label="Similitudes Detectadas" color="advertencia" />
            <MetricCard icon="check-circle" value="2" label="Revisiones Recibidas" color="exito" />
            <MetricCard icon="bug" value="0" label="Reportes de Falla" color="info" />
          </div>
        </DataPanel>

        <div className="acciones-finales">
          <Link to="/admin/nuevo-usuario" state={{ editUser: userData }} className="btn-primario"><i className="fas fa-edit"></i> Editar Usuario</Link>
        </div>
      </div>
    </DashboardLayout>
  )
}
