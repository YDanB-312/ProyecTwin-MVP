import { NavLink } from 'react-router-dom'
import '../SidebarAprendiz/SidebarAprendiz.css'

export default function SidebarInstructor({ isOpen, onClose }) {
  return (
    <nav className={`sidebar${isOpen ? ' abierto' : ''}`} id="sidebar">
      <button type="button" className="cerrar-sidebar" aria-label="Cerrar menú" onClick={onClose}>&times;</button>
      <ul className="menu-principal">
        <li><NavLink to="/instructor/dashboard" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-home"></i></div> Dashboard</NavLink></li>
        <li><NavLink to="/instructor/revision-propuestas" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-tasks"></i></div> Revisión de Propuestas</NavLink></li>
        <li><NavLink to="/instructor/gestionar-fichas" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-users"></i></div> Gestionar Fichas</NavLink></li>
        <li><NavLink to="/instructor/alertas" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-bell"></i></div> Notificaciones</NavLink></li>
        <li><NavLink to="/instructor/reportar-falla" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-bug"></i></div> Reportar Falla</NavLink></li>
        <li><NavLink to="/instructor/perfil" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-user-cog"></i></div> Mi Perfil</NavLink></li>
      </ul>
    </nav>
  )
}
