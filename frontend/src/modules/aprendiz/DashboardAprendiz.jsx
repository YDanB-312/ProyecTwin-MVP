import { Link, useNavigate } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import { useAuth } from '../../contexts/AuthContext'
import { obtenerFichaAprendiz } from '../../constants/fichas'
import '../../assets/styles/pages/dashboard-aprendiz.css'

export default function DashboardAprendiz() {
  const { user } = useAuth()
  const hoy = new Date().toLocaleDateString('es-CO', { day: 'numeric', month: 'short', year: 'numeric' })
  const navigate = useNavigate()
  const ficha = obtenerFichaAprendiz()

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
      <div className="contenedor-dashboard fade-in">

        <div className="dashboard-header">
          <div className="dashboard-header-left">
            <h1 className="dashboard-titulo">Bienvenido, {user?.nombre?.split(' ')[0] || 'Usuario'}</h1>
            <p className="dashboard-subtitulo">Gestiona tus proyectos de formación y evita similitudes con otras propuestas.</p>
          </div>
          <span className="dashboard-fecha">{hoy}</span>
        </div>

        <div className="tarjeta-bienvenida-moderna">
          <div className="bienvenida-contenido">
            <span className="saludo-personal">Hola, {user?.nombre?.split(' ')[0] || 'Usuario'}</span>
            <h1>Bienvenida al Sistema ProyecTwin!</h1>
            <p>Gestiona tus proyectos de formación y evita similitudes con otras propuestas.</p>
          </div>
          <div className="bienvenida-ilustracion">
            <i className="fas fa-rocket"></i>
          </div>
        </div>

        {!ficha && (
          <section className="dashboard-section">
            <div className="empty-state-card" style={{ borderLeft: '4px solid var(--color-advertencia)' }}>
              <i className="fas fa-users empty-icono" style={{ color: 'var(--color-advertencia)' }}></i>
              <h3>No perteneces a ninguna ficha</h3>
              <p>Únete a una ficha para empezar a colaborar con tu grupo de formación.</p>
              <button type="button" className="btn-primario" onClick={() => navigate('/aprendiz/unirse-ficha')}>+ Unirse a una ficha</button>
            </div>
          </section>
        )}

        {ficha && (
          <section className="dashboard-section">
            <div className="empty-state-card" style={{ borderLeft: '4px solid var(--color-exito)' }}>
              <i className="fas fa-users empty-icono" style={{ color: 'var(--color-exito)' }}></i>
              <h3>Ficha: {ficha.codigo}</h3>
              <p>{ficha.nombre} — Programa {ficha.programa}</p>
              <Link to="/aprendiz/detalle-ficha/ADSO-2568" className="btn-secundario"><i className="fas fa-eye"></i> Ver mi ficha</Link>
            </div>
          </section>
        )}

        <section className="dashboard-section">
          <h2 className="titulo-seccion-dashboard">Acciones rápidas</h2>
          <div className="acciones-grid">
            <Link to="/aprendiz/nuevo-proyecto" className="tarjeta-accion-moderna">
              <div className="accion-icono"><i className="fas fa-plus"></i></div>
              <h3>Nuevo proyecto</h3>
              <p>Inicia una idea desde cero</p>
            </Link>
            <Link to="/aprendiz/mis-proyectos" className="tarjeta-accion-moderna">
              <div className="accion-icono"><i className="fas fa-folder"></i></div>
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
          <div className="seccion-header">
            <h2 className="seccion-titulo">Mis proyectos</h2>
          </div>
          <div className="empty-state-card">
            <i className="fas fa-folder-open empty-icono"></i>
            <h3>Aún no tienes proyectos</h3>
            <p>Crea tu primer proyecto y empieza a colaborar</p>
            <button type="button" className="btn-primario" onClick={() => navigate('/aprendiz/nuevo-proyecto')}>+ Crear mi primer proyecto</button>
          </div>
        </section>

        <section className="dashboard-section">
          <div className="seccion-header">
            <h2 className="seccion-titulo">Notificaciones</h2>
          </div>
          <div className="empty-state-card">
            <i className="fas fa-bell-slash empty-icono"></i>
            <h3>No hay notificaciones nuevas</h3>
            <p>Cuando recibas alertas sobre tus proyectos, aparecerán aquí.</p>
          </div>
        </section>

      </div>
    </DashboardLayout>
  )
}


