import '../../assets/styles/pages/footer.css'

export default function FooterAprendiz() {
  return (
    <footer className="pie-pagina">
      <div className="contenedor-pie contenedor-pie-sin-nav">
        <div className="seccion-pie">
          <h3><i className="fas fa-cube"></i> ProyecTwin SENA</h3>
          <p>Sistema de gestión y detección de similitudes para proyectos de formación. Plataforma diseñada para fortalecer la calidad académica en el SENA.</p>
          <img src="/images/logo-sena-blanco.png" alt="SENA" className="logo-footer" />
        </div>
        <div className="seccion-pie">
          <h3><i className="fas fa-envelope"></i> Contacto</h3>
          <div className="info-contacto">
            <p><i className="fas fa-phone"></i> 323 542 1165</p>
            <p><i className="fas fa-envelope"></i> sena@correo.edu.co</p>
          </div>
        </div>
      </div>
      <div className="pie-inferior">
        <p>&copy; 2026 ProyecTwin SENA &mdash; Todos los derechos reservados</p>
      </div>
    </footer>
  )
}
