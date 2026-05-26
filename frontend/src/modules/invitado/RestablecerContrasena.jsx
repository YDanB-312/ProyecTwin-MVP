/*
Pagina: RestablecerContrasena
Funcion: Pagina para crear una nueva contrasena
Proposito: Permite al usuario establecer una nueva contrasena segura
*/

import { Link } from 'react-router-dom'
import '../../assets/styles/pages/login.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterSimple from '../../components/FooterSimple/FooterSimple'

export default function RestablecerContrasena() {
  return (
    <div className="modulo-invitado">
      <GovernmentBar />

      <main className="contenedor-login">
        <div className="tarjeta-login">
          <div className="formulario-login">
            <div className="encabezado-login">
              <img src="/images/Logo-ProyecTwin.png" alt="Logo ProyecTwin" />
              <h1>Proyec<span>Twin</span></h1>
              <p>Nueva contrasena</p>
            </div>

            <p className="texto-instruccion">Ingresa tu nueva contrasena para restablecer el acceso a tu cuenta.</p>

            <form action="#">
              <div className="grupo-campo">
                <label><i className="fas fa-lock"></i> Nueva Contrasena</label>
                <input type="password" placeholder="Minimo 6 caracteres" minLength="6" required name="password" />
              </div>
              <div className="grupo-campo">
                <label><i className="fas fa-check-circle"></i> Confirmar Contrasena</label>
                <input type="password" placeholder="Repite la contrasena" minLength="6" required name="password_confirmation" />
              </div>

              <button type="submit" className="btn-primario">
                <i className="fas fa-save"></i> Restablecer contrasena
              </button>

              <div className="contenedor-enlace-volver">
                <Link to="/login"><i className="fas fa-arrow-left"></i> Volver al inicio de sesion</Link>
              </div>
            </form>
          </div>

          <div className="panel-info-login">
            <div>
              <h2>Restablecimiento seguro</h2>
              <p>Elige una contrasena segura que no hayas usado antes en ProyecTwin.</p>
              <ul>
                <li><i className="fas fa-check-circle"></i> Minimo 6 caracteres</li>
                <li><i className="fas fa-check-circle"></i> Incluye letras y numeros</li>
                <li><i className="fas fa-check-circle"></i> No compartas tu contrasena</li>
              </ul>
            </div>
          </div>
        </div>
      </main>

      <FooterSimple />
    </div>
  )
}
