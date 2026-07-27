import { useAuth } from '../../contexts/AuthContext'
import './Header.css'

export default function Header({ titulo, usuario, notificaciones, onToggleSidebar }) {
  const { logout } = useAuth()

  return (
    <header className="header-principal">
      <div className="contenedor-header">
        <div className="grupo-izquierdo">
          <button className="btn-hamburguesa" onClick={onToggleSidebar} aria-label="Abrir menú"><i className="fas fa-bars"></i></button>
          <img src="/images/Logo-ProyecTwin.png" alt="Logo" className="logo-header-img" />
          <span className="titulo-header">{titulo}</span>
        </div>
        <div className="grupo-derecho">
          <img src="/images/logo-sena-blanco.png" alt="SENA" className="logo-sena-header" />
          <div className="notificaciones" role="status" aria-live="polite"><i className="fas fa-bell"></i> Notificaciones <span>{notificaciones}</span></div>
          <div className="usuario"><div className="avatar"><i className="fas fa-user"></i></div><span>{usuario}</span></div>
          <button className="btn-cerrar-sesion" onClick={logout} aria-label="Cerrar sesión"><i className="fas fa-sign-out-alt"></i> Cerrar sesión</button>
        </div>
      </div>
    </header>
  )
}
