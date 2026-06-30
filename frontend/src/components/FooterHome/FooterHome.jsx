/*
Componente: FooterHome
Función: Pie de página para la página de inicio (Home)
Propósito: Mostrar información institucional, enlaces rápidos y contacto
*/

import { Link } from 'react-router-dom'
import '../../assets/styles/pages/Footer.css'

export default function FooterHome() {
  return (
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
            <li><Link to="/login"><i className="fas fa-sign-in-alt"></i> Ingresar</Link></li>
            <li><Link to="/register"><i className="fas fa-user-plus"></i> Registrarse</Link></li>
          </ul>
        </div>
        <div className="seccion-pie">
          <h3>Contacto</h3>
          <div className="info-contacto">
            <p><i className="fas fa-phone"></i> 3235421165</p>
            <p><i className="fas fa-envelope"></i> sena@correo.edu.co</p>
            <p><i className="fas fa-map-marker-alt"></i> Centro de Formación SENA</p>
          </div>
        </div>
      </div>
      <div className="derechos-autor">
        <p>&copy; 2026 ProyecTwin SENA. Todos los derechos reservados.</p>
      </div>
    </footer>
  )
}
