import { Link } from 'react-router-dom'
import './Header.css'

export default function Header({ titulo, usuario, notificaciones }) {
  return (
    <header className="header-principal">
      <div className="contenedor-header">
        <div className="grupo-izquierdo">
          <a href="#sidebar" className="btn-hamburguesa"><i className="fas fa-bars"></i></a>
          <img src="/images/Logo-ProyecTwin.png" alt="Logo" className="logo-header-img" />
          <span className="titulo-header">{titulo}</span>
        </div>
        <div className="grupo-derecho">
          <img src="/images/logo-sena-blanco.png" alt="SENA" className="logo-sena-header" />
          <div className="notificaciones"><i className="fas fa-bell"></i> Notificaciones <span>{notificaciones}</span></div>
          <div className="usuario"><div className="avatar"><i className="fas fa-user"></i></div><span>{usuario}</span></div>
          <Link to="/login" className="btn-cerrar-sesion"><i className="fas fa-sign-out-alt"></i> Cerrar sesión</Link>
        </div>
      </div>
    </header>
  )
}
