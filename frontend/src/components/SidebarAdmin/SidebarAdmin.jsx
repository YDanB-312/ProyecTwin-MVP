import { NavLink } from 'react-router-dom'
import '../SidebarAprendiz/SidebarAprendiz.css'

export default function SidebarAdmin({ isOpen, onClose }) {
  return (
    <nav className={`sidebar${isOpen ? ' abierto' : ''}`} id="sidebar">
      <button type="button" className="cerrar-sidebar" aria-label="Cerrar menú" onClick={onClose}>&times;</button>
      <ul className="menu-principal">
        <li><NavLink to="/admin/dashboard" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-home"></i></div> Dashboard</NavLink></li>
        <li><NavLink to="/admin/gestion-usuarios" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-users-cog"></i></div> Gestion Usuarios</NavLink></li>
        <li><NavLink to="/admin/proyectos" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-folder-open"></i></div> Proyectos</NavLink></li>
        <li><NavLink to="/admin/similitudes" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-search"></i></div> Similitudes</NavLink></li>
        <li><NavLink to="/admin/reportes-fallas" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-bug"></i></div> Reportes de Fallas</NavLink></li>
        <li><NavLink to="/admin/notificaciones" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-bell"></i></div> Notificaciones</NavLink></li>
        <li><NavLink to="/admin/perfil" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')} onClick={onClose}><div className="icono-nav"><i className="fas fa-user"></i></div> Mi Perfil</NavLink></li>
      </ul>
    </nav>
  )
}
