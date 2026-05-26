import { NavLink } from 'react-router-dom'
import './SidebarAprendiz.css'

export default function SidebarAprendiz() {
  return (
    <nav className="sidebar" id="sidebar">
      <button type="button" className="cerrar-sidebar" aria-label="Cerrar menú">&times;</button>
      <ul className="menu-principal">
        <li><NavLink to="/aprendiz/dashboard" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-home"></i></div> Dashboard</NavLink></li>
        <li><NavLink to="/aprendiz/mis-proyectos" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-folder-open"></i></div> Mis proyectos</NavLink></li>
        <li><NavLink to="/aprendiz/nuevo-proyecto" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-plus-circle"></i></div> Nuevo proyecto</NavLink></li>
        <li><NavLink to="/aprendiz/detalle-ficha" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-users"></i></div> Mi Ficha</NavLink></li>
        <li><NavLink to="/aprendiz/unirse-ficha" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-user-plus"></i></div> Unirse a Ficha</NavLink></li>
        <li><NavLink to="/aprendiz/alertas" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-bell"></i></div> Notificaciones</NavLink></li>
        <li><NavLink to="/aprendiz/reportar-falla" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-bug"></i></div> Reportar Falla</NavLink></li>
        <li><NavLink to="/aprendiz/perfil" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-user-cog"></i></div> Mi Perfil</NavLink></li>
      </ul>
    </nav>
  )
}
