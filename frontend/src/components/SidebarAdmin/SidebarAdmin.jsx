import { NavLink } from 'react-router-dom'
import '../SidebarAprendiz/SidebarAprendiz.css'

export default function SidebarAdmin() {
  return (
    <nav className="sidebar" id="sidebar">
      <button type="button" className="cerrar-sidebar" aria-label="Cerrar menú">&times;</button>
      <ul className="menu-principal">
        <li><NavLink to="/admin/dashboard" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-home"></i></div> Dashboard</NavLink></li>
        <li><NavLink to="/admin/gestion-usuarios" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-users-cog"></i></div> Gestión Usuarios</NavLink></li>
        <li><NavLink to="/admin/reportes" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-chart-bar"></i></div> Reportes</NavLink></li>
        <li><NavLink to="/admin/reportes-fallas" className={({ isActive }) => 'enlace-nav' + (isActive ? ' activo' : '')}><div className="icono-nav"><i className="fas fa-bug"></i></div> Reportes de Fallas</NavLink></li>
      </ul>
    </nav>
  )
}
