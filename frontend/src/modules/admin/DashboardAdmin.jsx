import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/dashboard-admin.css'

export default function DashboardAdmin() {
  const hoy = new Date().toLocaleDateString('es-CO', { day: 'numeric', month: 'long', year: 'numeric' })

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
      <div className="contenedor-dashboard">

        <div className="dashboard-header">
          <div>
            <h1 className="dashboard-titulo">Panel de Administración</h1>
            <p className="dashboard-subtitulo">Supervisa el estado del sistema, gestiona usuarios y revisa reportes de calidad.</p>
          </div>
          <span className="dashboard-fecha">{hoy}</span>
        </div>

        <div className="tarjeta-bienvenida-moderna">
          <div className="bienvenida-contenido">
            <h1>Bienvenido al Sistema ProyecTwin!</h1>
            <p>Supervisa el estado general de la plataforma, administra usuarios y asegura la calidad de los proyectos educativos.</p>
          </div>
          <div className="bienvenida-ilustracion">
            <i className="fas fa-shield-alt"></i>
          </div>
        </div>

        <div className="grid-estadisticas">
          <div className="tarjeta-estadistica exito">
            <div className="icono-estadistica icono-usuarios"><i className="fas fa-users"></i></div>
            <div className="info-estadistica">
              <h3>Usuarios Registrados</h3>
              <span className="valor-estadistica">156</span>
              <p className="detalle-estadistica">Total de cuentas activas</p>
            </div>
          </div>
          <div className="tarjeta-estadistica exito">
            <div className="icono-estadistica icono-aprendices"><i className="fas fa-user-graduate"></i></div>
            <div className="info-estadistica">
              <h3>Aprendices Activos</h3>
              <span className="valor-estadistica">98</span>
              <p className="detalle-estadistica">Inscritos en programas</p>
            </div>
          </div>
          <div className="tarjeta-estadistica informativa">
            <div className="icono-estadistica icono-instructores"><i className="fas fa-chalkboard-teacher"></i></div>
            <div className="info-estadistica">
              <h3>Instructores</h3>
              <span className="valor-estadistica">12</span>
              <p className="detalle-estadistica">Activos en plataforma</p>
            </div>
          </div>
          <div className="tarjeta-estadistica advertencia">
            <div className="icono-estadistica icono-programas"><i className="fas fa-book"></i></div>
            <div className="info-estadistica">
              <h3>Programas</h3>
              <span className="valor-estadistica">5</span>
              <p className="detalle-estadistica">En ejecución</p>
            </div>
          </div>
          <div className="tarjeta-estadistica peligro">
            <div className="icono-estadistica icono-similitudes"><i className="fas fa-bug"></i></div>
            <div className="info-estadistica">
              <h3>Reportes de Falla Pendientes</h3>
              <span className="valor-estadistica">8</span>
              <p className="detalle-estadistica">Esperan revisión</p>
            </div>
          </div>
          <div className="tarjeta-estadistica exito">
            <div className="icono-estadistica icono-usuarios"><i className="fas fa-check-circle"></i></div>
            <div className="info-estadistica">
              <h3>Reportes Resueltos</h3>
              <span className="valor-estadistica">5</span>
              <p className="detalle-estadistica">Cerrados este mes</p>
            </div>
          </div>
        </div>

        <section className="dashboard-section">
          <h2 className="titulo-seccion-dashboard">Acciones Rápidas</h2>
          <div className="acciones-grid-moderna">
            <Link to="/admin/gestion-usuarios" className="tarjeta-accion-moderna">
              <div className="accion-icono"><i className="fas fa-users"></i></div>
              <h3>Usuarios Registrados</h3>
              <p>Consulta y administra aprendices, instructores y administradores</p>
            </Link>
            <Link to="/admin/reportes-fallas" className="tarjeta-accion-moderna">
              <div className="accion-icono"><i className="fas fa-bug"></i></div>
              <h3>Reportes de Fallas</h3>
              <p>Revisa y gestiona los reportes de errores del sistema</p>
            </Link>
          </div>
        </section>

        <section className="dashboard-section">
          <div className="seccion-header">
            <h2 className="seccion-titulo">Actividad Reciente</h2>
            <Link to="/admin/notificaciones" className="ver-todas">Ver todas <i className="fas fa-arrow-right"></i></Link>
          </div>
          <div className="lista-alertas-moderna">
            <div className="alerta-card informativa">
              <div className="alerta-card-icono"><i className="fas fa-file-signature"></i></div>
              <div className="alerta-card-contenido">
                <div className="alerta-card-titulo">Nuevo Proyecto Registrado</div>
                <div className="alerta-card-descripcion"><p>Ana Martinez ha registrado un nuevo proyecto titulado "Sistema IoT para Agricultura". Pendiente de revisión de similitud.</p></div>
                <div className="alerta-card-meta">
                  <span className="alerta-card-fecha"><i className="fas fa-clock"></i> Hace 1 hora</span>
                  <Link to="/admin/proyectos" className="btn-alerta-accion"><i className="fas fa-eye"></i> Ver</Link>
                </div>
              </div>
            </div>
            <div className="alerta-card urgente">
              <div className="alerta-card-icono"><i className="fas fa-exclamation-triangle"></i></div>
              <div className="alerta-card-contenido">
                <div className="alerta-card-titulo">Alerta de Similitud Generada</div>
                <div className="alerta-card-descripcion"><p>Se detectó un 65% de similitud entre "Sistema de Gestión Académica" y "Plataforma Educativa SENA". Notificación enviada al instructor.</p></div>
                <div className="alerta-card-meta">
                  <span className="alerta-card-fecha"><i className="fas fa-clock"></i> Hace 3 horas</span>
                  <Link to="/admin/similitudes" className="btn-alerta-accion"><i className="fas fa-search"></i> Revisar</Link>
                </div>
              </div>
            </div>
            <div className="alerta-card advertencia">
              <div className="alerta-card-icono"><i className="fas fa-bug"></i></div>
              <div className="alerta-card-contenido">
                <div className="alerta-card-titulo">Reporte de Falla Recibido</div>
                <div className="alerta-card-descripcion"><p>El instructor Carlos Ruiz reportó un error en el módulo de revisión de propuestas al intentar cargar documentos adjuntos.</p></div>
                <div className="alerta-card-meta">
                  <span className="alerta-card-fecha"><i className="fas fa-clock"></i> Hace 5 horas</span>
                  <Link to="/admin/reportes-fallas" className="btn-alerta-accion"><i className="fas fa-wrench"></i> Gestionar</Link>
                </div>
              </div>
            </div>
          </div>
        </section>

      </div>
    </DashboardLayout>
  )
}
