import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/dashboard-aprendiz.css'

function DashboardAprendiz() {
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-dashboard">

        <div className="tarjeta-bienvenida-moderna">
          <div className="bienvenida-contenido">
            <span className="saludo-personal">Hola, Maria</span>
            <h1>Bienvenida al Sistema ProyecTwin!</h1>
            <p>Gestiona tus proyectos de formacion y evita similitudes con otras propuestas. Comienza registrando tu primer proyecto para aprovechar todas las funcionalidades.</p>
          </div>
          <div className="bienvenida-ilustracion">
            <i className="fas fa-rocket"></i>
          </div>
        </div>

        <section className="dashboard-section">
          <h2 className="titulo-seccion-dashboard">Acciones rápidas</h2>
          <div className="acciones-grid">
            <Link to="/aprendiz/nuevo-proyecto" className="tarjeta-accion-moderna">
              <div className="accion-icono"><i className="fas fa-plus"></i></div>
              <h3>Nuevo proyecto</h3>
              <p>Inicia una idea desde cero</p>
            </Link>
            <Link to="/aprendiz/mis-proyectos" className="tarjeta-accion-moderna">
              <div className="accion-icono"><i className="fas fa-folder-open"></i></div>
              <h3>Mis proyectos</h3>
              <p>Continúa donde lo dejaste</p>
            </Link>
            <Link to="/aprendiz/alertas" className="tarjeta-accion-moderna">
              <div className="accion-icono"><i className="fas fa-bell"></i></div>
              <h3>Notificaciones</h3>
              <p>Mantente al día</p>
            </Link>
          </div>
        </section>

        <section className="dashboard-section">
          <h2 className="titulo-seccion-dashboard">Mis proyectos</h2>
          <div className="estado-vacio-card">
            <div className="estado-vacio-icono"><i className="fas fa-folder-open"></i></div>
            <p className="estado-vacio-titulo">Aún no tienes proyectos</p>
            <p className="estado-vacio-descripcion">Crea tu primer proyecto y empieza a colaborar</p>
            <Link to="/aprendiz/nuevo-proyecto" className="btn-accion-principal"><i className="fas fa-plus"></i> Crear mi primer proyecto</Link>
          </div>
        </section>

        <section className="dashboard-section">
          <h2 className="titulo-seccion-dashboard">Notificaciones</h2>
          <div className="estado-vacio-card estado-vacio-card-sin-icono">
            <p className="estado-vacio-titulo">No tienes notificaciones nuevas</p>
            <p className="estado-vacio-descripcion">Cuando tengas notificaciones sobre tus proyectos, aparecerán aquí.</p>
          </div>
        </section>

      </div>
    </DashboardLayout>
  )
}

export default DashboardAprendiz
