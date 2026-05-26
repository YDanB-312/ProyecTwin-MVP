import { Link } from 'react-router-dom'
import '.././../assets/styles/pages/register.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterSimple from '../../components/FooterSimple/FooterSimple'

export default function Register() {
  return (
    <div className="modulo-invitado">
      <GovernmentBar />

      <main className="contenedor-register">
        <div className="tarjeta-register">
          <div className="encabezado-register">
            <Link to="/login" className="btn-secundario">
              <i className="fas fa-arrow-left"></i> Volver al login
            </Link>
            <img src="/images/Logo-ProyecTwin.png" alt="Logo ProyecTwin" />
            <h1>Proyec<span>Twin</span></h1>
            <p>Crea tu cuenta para acceder a la plataforma</p>
          </div>

          <form action="#">
            <div className="seccion-register">
              <h2 className="titulo-seccion-register">
                <i className="fas fa-user"></i> Informacion Personal
              </h2>
              <div className="formulario-grid">
                <div className="grupo-campo">
                  <label>Nombre <span className="requerido">*</span></label>
                  <input type="text" placeholder="Ej: Maria" required name="nombre" />
                </div>
                <div className="grupo-campo">
                  <label>Apellido <span className="requerido">*</span></label>
                  <input type="text" placeholder="Ej: Gonzalez" required name="apellido" />
                </div>
                <div className="grupo-campo">
                  <label>Correo Electronico <span className="requerido">*</span></label>
                  <input type="email" placeholder="tu@correo.com" required name="correo" />
                </div>
                <div className="grupo-campo">
                  <label>Telefono</label>
                  <input type="tel" placeholder="Ej: 3235421165" name="telefono" />
                </div>
                <div className="grupo-campo">
                  <label>Fecha de Nacimiento</label>
                  <input type="date" name="fecha_nacimiento" />
                </div>
                <div className="grupo-campo">
                  <label>Contrasena <span className="requerido">*</span></label>
                  <input type="password" placeholder="Minimo 6 caracteres" minLength="6" required name="password" />
                </div>
              </div>
            </div>

            <div className="seccion-register">
              <h2 className="titulo-seccion-register">
                <i className="fas fa-id-badge"></i> Selecciona tu Rol
              </h2>
              <div className="selector-rol">
                <label className="tarjeta-rol">
                  <input type="radio" name="rol" value="aprendiz" />
                  <div className="icono-rol"><i className="fas fa-user-graduate"></i></div>
                  <h3>Aprendiz</h3>
                  <p>Registra y Gestiona tus proyectos</p>
                </label>
                <label className="tarjeta-rol">
                  <input type="radio" name="rol" value="instructor" />
                  <div className="icono-rol"><i className="fas fa-chalkboard-teacher"></i></div>
                  <h3>Instructor</h3>
                  <p>Evalua proyectos y da retroalimentacion</p>
                </label>
              </div>
            </div>

            <button type="submit" className="btn-primario">
              <i className="fas fa-user-check"></i> Registrarse
            </button>
          </form>

          <div className="login-link">
            Ya tienes cuenta? <Link to="/login">Inicia sesion</Link>
          </div>
        </div>
      </main>

      <FooterSimple />
    </div>
  )
}
