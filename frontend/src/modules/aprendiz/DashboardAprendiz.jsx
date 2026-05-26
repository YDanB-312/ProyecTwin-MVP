import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarAprendiz from '../../components/SidebarAprendiz/SidebarAprendiz';
import FooterAprendiz from '../../components/FooterAprendiz/FooterAprendiz';
import '../../assets/styles/pages/dashboard-aprendiz.css';

function DashboardAprendiz() {
  return (
    <div className="modulo-aprendiz">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5} />
      <SidebarAprendiz />
      <main className="contenido-principal">
        <div className="contenedor-dashboard">
          <section className="seccion-bienvenida">
            <div className="tarjeta-bienvenida">
              <div className="contenido-bienvenida">
                <h1>¡Bienvenida al Sistema ProyecTwin!</h1>
                <p>Gestiona tus proyectos de Formacion y evita Similitudes con otras Propuestas. Comienza registrando tu primer proyecto para aprovechar todas las funcionalidades.</p>
              </div>
            </div>
          </section>
          <section className="seccion-acciones">
            <h2 className="titulo-seccion"><i className="fas fa-bolt"></i> Acciones Rapidas</h2>
            <div className="grid-acciones">
              <div className="tarjeta-accion">
                <div className="icono-accion"><i className="fas fa-plus-circle"></i></div>
                <h3>Nuevo proyecto</h3>
                <p>Registra una nueva Propuesta de proyecto en el sistema</p>
                <Link to="/aprendiz/nuevo-proyecto" className="btn-accion-principal"><i className="fas fa-plus"></i> Crear proyecto</Link>
              </div>
              <div className="tarjeta-accion">
                <div className="icono-accion"><i className="fas fa-folder-open"></i></div>
                <h3>Mis proyectos</h3>
                <p>Revisa y Gestiona tus proyectos existentes</p>
                <Link to="/aprendiz/mis-proyectos" className="btn-accion-principal"><i className="fas fa-eye"></i> Ver proyectos</Link>
              </div>
              <div className="tarjeta-accion">
                <div className="icono-accion"><i className="fas fa-bell"></i></div>
                <h3>Notificaciones</h3>
                <p>Revisa Notificaciones y Similitudes detectadas</p>
                <Link to="/aprendiz/alertas" className="btn-accion-principal"><i className="fas fa-bell"></i> Ver Notificaciones</Link>
              </div>
            </div>
          </section>
          <section className="seccion-proyectos">
            <div className="encabezado-seccion">
              <h2 className="titulo-seccion"><i className="fas fa-folder"></i> Mis proyectos</h2>
              <Link to="/aprendiz/mis-proyectos" className="enlace-ver-todo">Ver todos <i className="fas fa-arrow-right"></i></Link>
            </div>
            <div className="estado-vacio">
              <div className="icono-vacio"><i className="fas fa-inbox"></i></div>
              <h3>No tienes proyectos registrados</h3>
              <p>Comienza registrando tu primer proyecto para aprovechar todas las funcionalidades del sistema.</p>
              <Link to="/aprendiz/nuevo-proyecto" className="btn-accion-principal"><i className="fas fa-plus"></i> Crear mi primer proyecto</Link>
            </div>
          </section>
          <section className="seccion-alertas">
            <div className="encabezado-seccion">
              <h2 className="titulo-seccion"><i className="fas fa-bell"></i> Notificaciones Recientes</h2>
              <Link to="/aprendiz/alertas" className="enlace-ver-todo">Ver todas <i className="fas fa-arrow-right"></i></Link>
            </div>
            <div className="estado-vacio">
              <div className="icono-vacio"><i className="fas fa-check-circle"></i></div>
              <h3>No tienes Notificaciones pendientes</h3>
              <p>Cuando tengas Notificaciones sobre tus proyectos, apareceran aqui.</p>
            </div>
          </section>
        </div>
      </main>
      <FooterAprendiz />
    </div>
  );
}

export default DashboardAprendiz;
