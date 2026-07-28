import '../../assets/styles/pages/footer.css'
import { CONTACTO } from '../../constants/contacto'

export default function FooterBase() {
  return (
    <footer className="pie-pagina">
      <div className="contenedor-pie contenedor-pie-sin-nav">
        <div className="seccion-pie">
          <h3><i className="fas fa-cube"></i> {CONTACTO.nombreApp}</h3>
          <p>{CONTACTO.descripcion}</p>
          <img src="/images/logo-sena-blanco.png" alt="SENA" className="logo-footer" />
        </div>
        <div className="seccion-pie">
          <h3><i className="fas fa-envelope"></i> Contacto</h3>
          <div className="info-contacto">
            <p><i className="fas fa-phone"></i> {CONTACTO.telefono}</p>
            <p><i className="fas fa-envelope"></i> {CONTACTO.email}</p>
          </div>
        </div>
      </div>
      <div className="pie-inferior">
        <p>&copy; {CONTACTO.copyright} &mdash; Todos los derechos reservados</p>
      </div>
    </footer>
  )
}
