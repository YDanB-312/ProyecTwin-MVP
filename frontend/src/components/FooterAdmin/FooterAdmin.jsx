import { Link } from 'react-router-dom'
import '../../assets/styles/pages/Footer.css'

export default function FooterAdmin() {
  return (
    <footer className="pie-pagina">
      <div className="contenedor-pie">
        <div className="seccion-pie">
          <h3><i className="fas fa-cube"></i> ProyecTwin SENA</h3>
          <p>Sistema de gestión y detección de similitudes para proyectos de formación. Plataforma diseñada para fortalecer la calidad académica en el SENA.</p>
        </div>
        <div className="seccion-pie">
          <h3><i className="fas fa-link"></i> Enlaces Rápidos</h3>
          <ul className="enlaces-pie">
            <li><Link to="/admin/dashboard"><i className="fas fa-chevron-right"></i> Dashboard</Link></li>
            <li><Link to="/admin/gestion-usuarios"><i className="fas fa-chevron-right"></i> Gestión usuarios</Link></li>
            <li><Link to="/admin/reportes"><i className="fas fa-chevron-right"></i> Reportes</Link></li>
            <li><Link to="/admin/reportes-fallas"><i className="fas fa-chevron-right"></i> Reportes de fallas</Link></li>
          </ul>
        </div>
        <div className="seccion-pie">
          <h3><i className="fas fa-envelope"></i> Contacto</h3>
          <div className="info-contacto">
            <p><i className="fas fa-phone"></i> 323 542 1165</p>
            <p><i className="fas fa-envelope"></i> sena@correo.edu.co</p>
            <p><i className="fas fa-map-marker-alt"></i> Centro de Formación SENA</p>
          </div>
        </div>
      </div>
      <div className="pie-inferior">
        <p>&copy; 2026 ProyecTwin SENA &mdash; Todos los derechos reservados</p>
      </div>
    </footer>
  )
}
