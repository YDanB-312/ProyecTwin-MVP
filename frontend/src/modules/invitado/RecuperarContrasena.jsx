import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import '../../assets/styles/pages/login.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterSimple from '../../components/FooterSimple/FooterSimple'

export default function RecuperarContrasena() {
  const { register, handleSubmit, formState: { errors }, reset } = useForm()
  const navigate = useNavigate()

  const onSubmit = () => {
    navigate('/login')
    reset()
  }

  return (
    <div className="modulo-invitado modulo-pagina-completa">
      <GovernmentBar />

      <main className="contenedor-login">
        <div className="tarjeta-login">
          <div className="formulario-login">
            <div className="encabezado-login">
              <img src="/images/Logo-ProyecTwin.png" alt="Logo ProyecTwin" />
              <h1>Proyec<span>Twin</span></h1>
              <p>Recupera tu contraseña</p>
            </div>

            <p className="texto-instruccion">
              Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.
            </p>

            <form onSubmit={handleSubmit(onSubmit)}>
              <div className="grupo-campo">
                <label>                <i className="fas fa-envelope"></i> Correo Electrónico</label>
                <input type="email" placeholder="tu@correo.com" {...register("correo", { required: true, pattern: /^[^@\s]+@[^@\s]+\.[^@\s]+$/ })} />
                {errors.correo && <span className="campo-error">Correo inválido</span>}
              </div>

              <button type="submit" className="btn-primario">
                <i className="fas fa-paper-plane"></i> Enviar enlace
              </button>

              <div className="contenedor-enlace-volver">
                <Link to="/login">
                  <i className="fas fa-arrow-left"></i> Volver al inicio de sesión
                </Link>
              </div>
            </form>
          </div>

          <div className="panel-info-login">
            <div>
              <h2>¿Olvidaste tu contraseña?</h2>
              <p>No te preocupes, te ayudaremos a recuperar el acceso a tu cuenta de ProyecTwin.</p>
              <ul>
                <li><i className="fas fa-check-circle"></i> Ingresa tu correo registrado</li>
                <li><i className="fas fa-check-circle"></i> Recibirás un enlace mágico</li>
                <li><i className="fas fa-check-circle"></i> Crea una nueva contraseña segura</li>
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
