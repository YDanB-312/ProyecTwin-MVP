import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/dashboard-instructor.css'

function DashboardInstructor() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-dashboard">

        <div className="dashboard-header">
          <div className="dashboard-header-left">
            <h1 className="dashboard-titulo">Bienvenido, Carlos</h1>
            <p className="dashboard-subtitulo">Gestiona las propuestas de proyectos y mantén el control de calidad de tus aprendices.</p>
          </div>
          <span className="dashboard-fecha">28 may. 2026</span>
        </div>

        <div className="tarjeta-bienvenida-moderna">
          <div className="bienvenida-contenido">
            <span className="saludo-personal">Hola, Carlos</span>
            <h1>Bienvenido al Sistema ProyecTwin!</h1>
            <p>Gestiona las propuestas de proyectos y mantén el control de calidad de los trabajos de tus aprendices. Revisa las propuestas pendientes y las alertas de similitud desde un solo lugar.</p>
          </div>
          <div className="bienvenida-ilustracion">
            <i className="fas fa-chalkboard-teacher"></i>
          </div>
        </div>

        <section className="dashboard-section">
          <h2 className="titulo-seccion-dashboard">Acciones rápidas</h2>
          <div className="acciones-grid">
            <Link to="/instructor/revision-propuestas" className="tarjeta-accion-moderna">
              <div className="accion-icono"><i className="fas fa-tasks"></i></div>
              <h3>Revisar Propuestas</h3>
              <p>Evalúa las propuestas de proyectos enviadas por los aprendices.</p>
            </Link>
            <Link to="/instructor/alertas" className="tarjeta-accion-moderna">
              <div className="accion-icono"><i className="fas fa-bell"></i></div>
              <h3>Notificaciones de Similitud</h3>
              <p>Revisa las alertas de similitud entre proyectos.</p>
            </Link>
          </div>
        </section>

        <section className="dashboard-section">
          <div className="seccion-header">
            <h2 className="seccion-titulo">Propuestas Pendientes</h2>
            <Link to="/instructor/revision-propuestas" className="ver-todas">Ver todas <i className="fas fa-arrow-right"></i></Link>
          </div>
          <div className="propuestas-grid">

            <div className="propuesta-card">
              <div className="propuesta-info">
                <h3>Sistema IoT para Agricultura</h3>
                <span className="propuesta-meta"><i className="fas fa-user"></i> Ana Martinez &middot; <i className="fas fa-calendar-alt"></i> 15 Nov 2023</span>
              </div>
              <div className="propuesta-estado">
                <span className="badge-propuesta badge-pendiente"><i className="fas fa-clock"></i> Pendiente</span>
                <Link to="/instructor/detalle-proyecto" className="btn-revisar"><i className="fas fa-eye"></i> Revisar</Link>
              </div>
            </div>

            <div className="propuesta-card">
              <div className="propuesta-info">
                <h3>App Movil para Turismo Local</h3>
                <span className="propuesta-meta"><i className="fas fa-user"></i> Juan Perez &middot; <i className="fas fa-calendar-alt"></i> 14 Nov 2023</span>
              </div>
              <div className="propuesta-estado">
                <span className="badge-propuesta badge-pendiente"><i className="fas fa-clock"></i> Pendiente</span>
                <Link to="/instructor/detalle-proyecto" className="btn-revisar"><i className="fas fa-eye"></i> Revisar</Link>
              </div>
            </div>

          </div>
        </section>

        <section className="dashboard-section">
          <div className="seccion-header">
            <h2 className="seccion-titulo">Notificaciones de Similitud</h2>
            <Link to="/instructor/alertas" className="ver-todas">Ver todas <i className="fas fa-arrow-right"></i></Link>
          </div>
          <div className="propuestas-grid">

            <div className="notificacion-card">
              <div className="notificacion-icono"><i className="fas fa-exclamation-triangle"></i></div>
              <div className="notificacion-info">
                <h3>Alta Similitud Detectada</h3>
                <span className="notificacion-meta">Proyecto "Sistema de Gestion Academica" tiene <strong>65% de similitud</strong> &middot; <i className="fas fa-clock"></i> Hace 2 horas</span>
              </div>
              <Link to="/instructor/detalle-similitud" className="btn-revisar"><i className="fas fa-search"></i> Revisar</Link>
            </div>

          </div>
        </section>

      </div>
    </DashboardLayout>
  )
}

export default DashboardInstructor
