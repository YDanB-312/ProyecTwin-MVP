/*
Página: Confirmacion
Función: Página de confirmación de operación exitosa
Propósito: Mostrar que una operación se realizó correctamente
*/

import { Link } from 'react-router-dom'
import '../../assets/styles/pages/login.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterSimple from '../../components/FooterSimple/FooterSimple'

export default function Confirmacion() {
  return (
    <div className="modulo-invitado">
      <GovernmentBar />

      <main className="contenedor-login">
        <div className="tarjeta-login tarjeta-confirmacion">
          <div className="formulario-login formulario-confirmacion">
            <div className="icono-exito-confirmacion">
              <i className="fas fa-check-circle"></i>
            </div>
            <h1 className="titulo-confirmacion">Operación Exitosa</h1>
            <p className="texto-confirmacion">
              La operación se ha realizado correctamente en el sistema ProyecTwin.
            </p>
            <Link to="/login" className="btn-primario">
              <i className="fas fa-home"></i> Volver al inicio
            </Link>
          </div>
        </div>
      </main>

      <FooterSimple />
    </div>
  )
}
