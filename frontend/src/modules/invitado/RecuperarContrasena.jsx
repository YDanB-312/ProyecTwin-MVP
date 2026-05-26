/*
Pagina: RecuperarContrasena
Funcion: Pagina para solicitar recuperacion de contrasena
Proposito: Enviar enlace de restablecimiento al correo del usuario
*/

import { Link } from 'react-router-dom'
import '../../assets/styles/pages/login.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterSimple from '../../components/FooterSimple/FooterSimple'

export default function RecuperarContrasena() {
  return (
    <div className="modulo-invitado">
      <GovernmentBar />

      <main className="contenedor-login">
        <div className="tarjeta-login">
          <div className="formulario-login">
            <div className="encabezado-login">
              <img src="/images/Logo-ProyecTwin.png" alt="Logo ProyecTwin" />
              <h1>Proyec<span>Twin</span></h1>
              <p>Recupera tu contrasena</p>
            </div>

            <p className="texto-instruccion">
              Ingresa tu correo electronico y te enviaremos un enlace para restablecer tu contrasena.
            </p>

            <form action="#">
              <div className="grupo-campo">
                <label><i className="fas fa-envelope"></i> Correo Electronico</label>
                <input type="email" placeholder="tu@correo.com" required name="correo" />
              </div>

              <button type="submit" className="btn-primario">
                <i className="fas fa-paper-plane"></i> Enviar enlace
              </button>

              <div className="contenedor-enlace-volver">
                <Link to="/login">
                  <i className="fas fa-arrow-left"></i> Volver al inicio de sesion
                </Link>
              </div>
            </form>
          </div>

          <div className="panel-info-login">
            <div>
              <h2>Olvidaste tu contrasena?</h2>
              <p>No te preocupes, te ayudaremos a recuperar el acceso a tu cuenta de ProyecTwin.</p>
              <ul>
                <li><i className="fas fa-check-circle"></i> Ingresa tu correo registrado</li>
                <li><i className="fas fa-check-circle"></i> Recibiras un enlace magico</li>
                <li><i className="fas fa-check-circle"></i> Crea una nueva contrasena segura</li>
                <li><i className="fas fa-check-circle"></i> Accede nuevamente a la plataforma</li>
              </ul>
            </div>
          </div>
        </div>
      </main>

      <FooterSimple />
    </div>
  )
}
