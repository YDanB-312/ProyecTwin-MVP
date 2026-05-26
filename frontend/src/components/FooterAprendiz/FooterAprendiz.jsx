import { Link } from 'react-router-dom'
import '../../assets/styles/pages/Footer.css'

export default function FooterAprendiz() {
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
            <li><Link to="/aprendiz/dashboard"><i className="fas fa-chevron-right"></i> Inicio</Link></li>
            <li><Link to="/aprendiz/mis-proyectos"><i className="fas fa-chevron-right"></i> Mis proyectos</Link></li>
            <li><Link to="/aprendiz/alertas"><i className="fas fa-chevron-right"></i> Notificaciones</Link></li>
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
