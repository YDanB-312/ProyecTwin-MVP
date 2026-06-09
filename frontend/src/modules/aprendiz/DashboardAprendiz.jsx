import { useNavigate } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/dashboard-aprendiz.css'

function DashboardAprendiz() {
  const navigate = useNavigate()

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-dashboard">

        <div className="dashboard-header">
          <div className="dashboard-header-left">
            <h1 className="dashboard-titulo">Bienvenido, María</h1>
            <p className="dashboard-subtitulo">Gestiona tus proyectos de formaci&oacute;n y evita similitudes con otras propuestas.</p>
          </div>
          <span className="dashboard-fecha">28 may. 2026</span>
        </div>

        <div className="tarjeta-bienvenida-moderna">
          <div className="bienvenida-contenido">
            <span className="saludo-personal">Hola, María</span>
            <h1>Bienvenida al Sistema ProyecTwin!</h1>
            <p>Gestiona tus proyectos de formaci&oacute;n y evita similitudes con otras propuestas.</p>
          </div>
          <div className="bienvenida-ilustracion">
            <i className="fas fa-rocket"></i>
          </div>
        </div>

        <section className="dashboard-section">
          <h2 className="titulo-seccion-dashboard">Acciones r&aacute;pidas</h2>
          <div className="acciones-grid">
            <div className="tarjeta-accion-moderna" onClick={() => navigate('/aprendiz/nuevo-proyecto')}>
              <div className="accion-icono"><i className="fas fa-plus"></i></div>
              <h3>Nuevo proyecto</h3>
              <p>Inicia una idea desde cero</p>
            </div>
            <div className="tarjeta-accion-moderna" onClick={() => navigate('/aprendiz/mis-proyectos')}>
              <div className="accion-icono"><i className="fas fa-folder"></i></div>
              <h3>Mis proyectos</h3>
              <p>Contin&uacute;a donde lo dejaste</p>
            </div>
            <div className="tarjeta-accion-moderna" onClick={() => navigate('/aprendiz/alertas')}>
              <div className="accion-icono"><i className="fas fa-bell"></i></div>
              <h3>Notificaciones</h3>
              <p>Mantente al d&iacute;a</p>
            </div>
          </div>
        </section>

        <section className="dashboard-section">
          <div className="seccion-header">
            <h2 className="seccion-titulo">Mis proyectos</h2>
          </div>
          <div className="empty-state-card">
            <i className="fas fa-folder-open empty-icono"></i>
            <h3>A&uacute;n no tienes proyectos</h3>
            <p>Crea tu primer proyecto y empieza a colaborar</p>
            <button className="btn-primario" onClick={() => navigate('/aprendiz/nuevo-proyecto')}>+ Crear mi primer proyecto</button>
          </div>
        </section>

        <section className="dashboard-section">
          <div className="seccion-header">
            <h2 className="seccion-titulo">Notificaciones</h2>
          </div>
          <div className="empty-state-card">
            <i className="fas fa-bell-slash empty-icono"></i>
            <h3>No hay notificaciones nuevas</h3>
            <p>Cuando recibas alertas sobre tus proyectos, aparecer&aacute;n aqu&iacute;.</p>
          </div>
        </section>

      </div>
    </DashboardLayout>
  )
}

export default DashboardAprendiz
