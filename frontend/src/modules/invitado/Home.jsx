import { Link } from 'react-router-dom'
import '../../assets/styles/pages/index.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterHome from '../../components/FooterHome/FooterHome'

export default function Home() {
  return (
    <div className="modulo-invitado modulo-pagina-completa">
      <GovernmentBar />

      <header className="header-principal">
        <div className="contenedor-header">
          <div className="grupo-izquierdo">
            <img src="/images/Logo-ProyecTwin.png" alt="Logo" className="logo-header-img" />
            <span className="titulo-header">ProyecTwin</span>
          </div>

          <div className="grupo-derecho">
            <img src="/images/logo-sena-blanco.png" alt="SENA" className="logo-sena-header" />

            <div className="buttons">
              <Link to="/login" className="btn-primario">Ingresar</Link>
              <Link to="/register" className="btn-secundario">Registrarme</Link>
            </div>
          </div>
        </div>
      </header>

      <main className="contenido-principal">
        <div className="home-container">

          <div className="hero-section">
            <div className="hero-decoration">
              <i className="fas fa-code"></i>
              <i className="fas fa-graduation-cap"></i>
              <i className="fas fa-brain"></i>
            </div>
            <h1 className="hero-titulo">
              Bienvenido a <span>ProyecTwin</span>
            </h1>
            <p className="hero-descripcion">
              Plataforma inteligente de gestión y detección de Similitudes para proyectos de formación del SENA.
              Optimiza tus proyectos, evita duplicidades y fomenta la originalidad académica.
            </p>
            <div className="hero-botones">
              <Link to="/login" className="btn-primario">
                <i className="fas fa-sign-in-alt"></i> Ingresar Ahora
              </Link>
              <Link to="/register" className="btn-secundario">
                <i className="fas fa-user-plus"></i> Crear Cuenta
              </Link>
            </div>
          </div>

          <div className="usuarios-section">
            <h3 className="usuarios-titulo">Acceso por Rol</h3>
            <p className="usuarios-subtitulo">ProyecTwin se adapta a las necesidades de cada miembro de la comunidad SENA</p>

            <div className="usuarios-grid">
              <div className="tarjeta-usuario aprendiz">
                <div className="tarjeta-usuario-icono">
                  <i className="fas fa-user-graduate"></i>
                </div>
                <h3>Aprendiz</h3>
                <p>Gestiona tus proyectos de formación y recibe retroalimentación de instructores.</p>
                <ul>
                  <li><i className="fas fa-check"></i> Crear nuevos proyectos</li>
                  <li><i className="fas fa-check"></i> Recibir Notificaciones</li>
                  <li><i className="fas fa-check"></i> Gestiona tu perfil</li>
                </ul>
              </div>

              <div className="tarjeta-usuario instructor">
                <div className="tarjeta-usuario-icono">
                  <i className="fas fa-chalkboard-teacher"></i>
                </div>
                <h3>Instructor</h3>
                <p>Revisa Propuestas, evalúa proyectos y gestiona el progreso de tus aprendices.</p>
                <ul>
                  <li><i className="fas fa-check"></i> Revisar Propuestas</li>
                  <li><i className="fas fa-check"></i> Evaluar proyectos</li>
                  <li><i className="fas fa-check"></i> Generar reportes</li>
                </ul>
              </div>
            </div>
          </div>

          <div className="como-funciona">
            <h3 className="como-funciona-titulo">¿Cómo Funciona ProyecTwin?</h3>

            <div className="pasos-grid">
              <div className="paso">
                <div className="paso-numero">1</div>
                <h4>Registrate</h4>
                <p>Crea tu cuenta como aprendiz o instructor segun tu rol en el SENA.</p>
              </div>
              <div className="paso">
                <div className="paso-numero">2</div>
                <h4>Sube tu Proyecto</h4>
                <p>Registra tu Propuesta y el sistema analizará automáticamente Similitudes.</p>
              </div>
              <div className="paso">
                <div className="paso-numero">3</div>
                <h4>Recibe Evaluación</h4>
                <p>Instructores revisan y aprueban Propuestas con herramientas inteligentes.</p>
              </div>
              <div className="paso">
                <div className="paso-numero">4</div>
                <h4>Mejora tu Proyecto</h4>
                <p>Recibe retroalimentación de instructores y mejora tu proyecto continuamente.</p>
              </div>
            </div>
          </div>

          <div className="tarjetas-container">
            <div className="tarjeta">
              <div className="tarjeta-icono tarjeta-icono-buscar">
                <i className="fas fa-search"></i>
              </div>
              <h3 className="tarjeta-titulo">Detección de Similitudes</h3>
              <p className="tarjeta-texto">
                Analizamos automáticamente tus Propuestas para identificar Similitudes con otros proyectos.
              </p>
            </div>

            <div className="tarjeta">
              <div className="tarjeta-icono tarjeta-icono-gestionar">
                <i className="fas fa-tasks"></i>
              </div>
              <h3 className="tarjeta-titulo">Gestión de Proyectos</h3>
              <p className="tarjeta-texto">
                Administra Propuestas y mantente organizado durante todo el ciclo.
              </p>
            </div>

            <div className="tarjeta">
              <div className="tarjeta-icono tarjeta-icono-seguimiento">
                <i className="fas fa-chart-line"></i>
              </div>
              <h3 className="tarjeta-titulo">Retroalimentación Continua</h3>
              <p className="tarjeta-texto">
                Recibe comentarios de instructores para mejorar tus proyectos.
              </p>
            </div>
          </div>

          <div className="cta-section">
            <h3 className="cta-titulo">
              <i className="fas fa-rocket"></i>
              Listo para transformar tus proyectos?
            </h3>
            <p className="cta-texto">
              Únete a la comunidad académica del SENA y lleva tus proyectos de formación al siguiente nivel.
            </p>
            <Link to="/register" className="btn-primario">
              <i className="fas fa-user-plus"></i> Comenzar Ahora
            </Link>
          </div>

        </div>
      </main>

      <FooterHome />
    </div>
  )
}
