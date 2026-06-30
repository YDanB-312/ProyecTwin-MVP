/*
Componente: FooterSimple
Función: Pie de página simple para páginas de acceso público
Propósito: Mostrar copyright y datos de contacto institucionales
*/

import '../../assets/styles/pages/login.css'

export default function FooterSimple() {
  return (
    <footer className="pie-pagina-login">
      <div className="contenedor-pie">
        <span>ProyecTwin SENA &copy; 2026</span>
        <div className="info-contacto">
          <span><i className="fas fa-phone"></i> 3235421165</span>
          <span><i className="fas fa-envelope"></i> sena@correo.edu.co</span>
        </div>
      </div>
    </footer>
  )
}
