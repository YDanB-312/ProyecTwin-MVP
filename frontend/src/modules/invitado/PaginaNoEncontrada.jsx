import { Link } from 'react-router-dom'
import '../../assets/styles/pages/404.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterHome from '../../components/FooterHome/FooterHome'

export default function PaginaNoEncontrada() {
  return (
    <div className="modulo-invitado modulo-pagina-completa fade-in">
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

      <FooterHome />
    </div>
  )
}
