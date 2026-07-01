import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import '../../assets/styles/pages/login.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterSimple from '../../components/FooterSimple/FooterSimple'

export default function Login() {
  const { register, handleSubmit, formState: { errors }, reset } = useForm()
  const navigate = useNavigate()
  const [errorMsg, setErrorMsg] = useState('')

  const onSubmit = (data) => {
    if (data.correo === 'maria.gonzalez@soy.sena.edu.co' && data.password === '123456') { navigate('/aprendiz/dashboard'); reset() }
    else if (data.correo === 'carlos.ruiz@sena.edu.co' && data.password === '123456') { navigate('/instructor/dashboard'); reset() }
    else setErrorMsg('Credenciales inválidas. Verifica tu correo y contraseña.')
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
              <p>Plataforma de Gestión de proyectos - SENA</p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)}>
              <div className="grupo-campo">
                <label><i className="fas fa-envelope"></i> Correo Electrónico</label>
                <input type="email" placeholder="tu@correo.com" {...register("correo", { required: true, pattern: /^[^@\s]+@[^@\s]+\.[^@\s]+$/ })} />
                {errors.correo && <span className="campo-error">Correo inválido</span>}
                {errorMsg && <span className="campo-error">{errorMsg}</span>}
              </div>

              <div className="grupo-campo">
                <label><i className="fas fa-lock"></i> Contraseña</label>
                <div className="contenedor-password">
                  <input type="password" placeholder="Ingresa tu contraseña" {...register("password", { required: true })} />
                </div>
                {errors.password && <span className="campo-error">La contraseña es obligatoria</span>}
              </div>

              <div className="opciones-login">
                <label><input type="checkbox" {...register("recordarme")} /> Recordarme</label>
                <Link to="/recuperar-contrasena">¿Olvidaste tu contraseña?</Link>
              </div>

              <button type="submit" className="btn-primario">
                <i className="fas fa-sign-in-alt"></i> Iniciar Sesión
              </button>

              <div className="separador"><span>O</span></div>

              <Link to="/register" className="btn-secundario">
                <i className="fas fa-user-plus"></i> Crear una cuenta nueva
              </Link>
            </form>

            <div className="info-prueba">
              <h4><i className="fas fa-info-circle"></i> Usuarios de prueba</h4>
              <div className="item-prueba">
                <span className="badge badge-exito">Aprendiz</span>
                <span className="texto-prueba">maria.gonzalez@soy.sena.edu.co / 123456</span>
              </div>
              <div className="item-prueba">
                <span className="badge badge-advertencia">Instructor</span>
                <span className="texto-prueba">carlos.ruiz@sena.edu.co / 123456</span>
              </div>
            </div>
          </div>

          <div className="panel-info-login">
            <div>
              <h2>Bienvenido a ProyecTwin</h2>
              <p>El sistema de gestión y detección de Similitudes para proyectos de formación del SENA.</p>
              <ul>
                <li><i className="fas fa-check-circle"></i> Gestiona tus proyectos de formación</li>
                <li><i className="fas fa-check-circle"></i> Detecta Similitudes con otros proyectos</li>
                <li><i className="fas fa-check-circle"></i> Recibe retroalimentación de instructores</li>
                <li><i className="fas fa-check-circle"></i> Evalúa Propuestas en tiempo real</li>
              </ul>
            </div>
          </div>
        </div>
      </main>

      <FooterSimple />
    </div>
  )
}
