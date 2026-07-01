import { Link } from 'react-router-dom'
import '../../assets/styles/pages/404.css'
import '../../assets/styles/pages/Footer.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'

export default function PaginaNoEncontrada() {
  return (
    <div className="modulo-invitado modulo-pagina-completa">
      <GovernmentBar />

      <header className="header-principal">
        <div className="contenedor-header">
          <div className="grupo-izquierdo">
            <img src="/images/Logo-ProyecTwin.png" alt="Logo" className="logo-header-img" />
            <span className="titulo-header">ProyecTwin SENA</span>
          </div>
          <div className="grupo-derecho">
            <img src="/images/logo-sena-blanco.png" alt="SENA" className="logo-sena-header" />
          </div>
        </div>
      </header>

      <main className="contenido-404">
        <div className="card-404">
          <div className="icono-404">
            <i className="fas fa-map-signs"></i>
          </div>
          <h1 className="titulo-404">404</h1>
          <h2 className="subtitulo-404">Pagina no encontrada</h2>
          <p className="texto-404">La página que buscas no existe o ha sido movida. Verifica la dirección o vuelve al inicio.</p>
          <div className="acciones-404">
            <Link to="/" className="btn-primario"><i className="fas fa-home"></i> Volver al inicio</Link>
            <Link to="/login" className="btn-secundario"><i className="fas fa-sign-in-alt"></i> Iniciar sesión</Link>
          </div>
        </div>
      </main>

      <footer className="pie-pagina">
        <div className="contenedor-pie">
          <div className="seccion-pie">
            <h3>ProyecTwin SENA</h3>
            <p>Sistema de gestión y detección de Similitudes para proyectos de Formación.</p>
          </div>
          <div className="seccion-pie">
            <h3>Enlaces Rápidos</h3>
            <ul className="enlaces-pie">
              <li><Link to="/"><i className="fas fa-home"></i> Inicio</Link></li>
              <li><Link to="/login"><i className="fas fa-sign-in-alt"></i> Iniciar sesión</Link></li>
            </ul>
          </div>
          <div className="seccion-pie">
            <h3>Contacto</h3>
            <div className="info-contacto">
              <p><i className="fas fa-phone"></i> 3235421165</p>
              <p><i className="fas fa-envelope"></i> sena@correo.edu.co</p>
            </div>
          </div>
        </div>
        <div className="derechos-autor">
          <p>&copy; 2026 ProyecTwin SENA. Todos los derechos reservados.</p>
        </div>
      </footer>
    </div>
  )
}
