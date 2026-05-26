import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import '../../assets/styles/pages/login.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterSimple from '../../components/FooterSimple/FooterSimple'

export default function Login() {
  const [correo, setCorreo] = useState('')
  const [password, setPassword] = useState('')
  const navigate = useNavigate()

  const handleSubmit = (e) => {
    e.preventDefault()
    if (correo === 'maria@correo.com' && password === '123456') navigate('/aprendiz/dashboard')
    else if (correo === 'carlos@correo.com' && password === '123456') navigate('/instructor/dashboard')
    else navigate('/admin/dashboard')
  }

  return (
    <div className="modulo-invitado">
      <GovernmentBar />

      <main className="contenedor-login">
        <div className="tarjeta-login">
          <div className="formulario-login">
            <div className="encabezado-login">
              <img src="/images/Logo-ProyecTwin.png" alt="Logo ProyecTwin" />
              <h1>Proyec<span>Twin</span></h1>
              <p>Plataforma de Gestión de proyectos - SENA</p>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="grupo-campo">
                <label><i className="fas fa-envelope"></i> Correo Electrónico</label>
                <input type="email" placeholder="tu@correo.com" required name="correo" value={correo} onChange={(e) => setCorreo(e.target.value)} />
              </div>

              <div className="grupo-campo">
                <label><i className="fas fa-lock"></i> Contraseña</label>
                <div className="contenedor-password">
                  <input type="password" placeholder="????????????????????????" required name="password" value={password} onChange={(e) => setPassword(e.target.value)} />
                </div>
              </div>

              <div className="opciones-login">
                <label><input type="checkbox" name="recordarme" /> Recordarme</label>
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
                <span className="texto-prueba">maria@correo.com / 123456</span>
              </div>
              <div className="item-prueba">
                <span className="badge badge-advertencia">Instructor</span>
                <span className="texto-prueba">carlos@correo.com / 123456</span>
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
