import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/fichas.css'

function UnirseFicha() {
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-pagina">

        <Link to="/aprendiz/dashboard" className="volver-link"><i className="fas fa-arrow-left"></i> Volver al Dashboard</Link>

        <div className="unirse-card">

          <h1 className="unirse-titulo">Unirse a una Ficha</h1>
          <p className="unirse-subtitulo">Ingresa el codigo de tu ficha para unirte al grupo de formacion.</p>

          <div className="mensaje-feedback mensaje-exito oculto">
            <i className="fas fa-check-circle"></i>
            <span>Operacion realizada exitosamente.</span>
          </div>
          <div className="mensaje-feedback mensaje-error oculto">
            <i className="fas fa-exclamation-circle"></i>
            <span>Ha ocurrido un error. Intenta nuevamente.</span>
          </div>

          <form action="#">
            <div className="unirse-campo-grupo">
              <label htmlFor="codigo-ficha" className="campo-label">Codigo de ficha <span className="obligatorio">*</span></label>
              <input type="text" id="codigo-ficha" className="campo-input" placeholder="ADSO-2568" required name="ficha" />
              <p className="campo-ayuda">Solicita a tu instructor el codigo de la ficha a la que perteneces e ingresalo aqui.</p>
            </div>

            <a href="#que-es-ficha" className="ayuda-link"><i className="fas fa-question-circle"></i> ¿Que es una ficha?</a>

            <button type="submit" className="btn-unirse"><i className="fas fa-sign-in-alt"></i> Unirse a la Ficha</button>
          </form>

          <div className="beneficios">
            <div className="beneficio-item">
              <span className="beneficio-icono"><i className="fas fa-users"></i></span>
              <span className="beneficio-texto">Formar parte del grupo de trabajo con tus companeros</span>
            </div>
            <div className="beneficio-item">
              <span className="beneficio-icono"><i className="fas fa-code-branch"></i></span>
              <span className="beneficio-texto">Trabajar en equipo en los proyectos asignados</span>
            </div>
            <div className="beneficio-item">
              <span className="beneficio-icono"><i className="fas fa-bell"></i></span>
              <span className="beneficio-texto">Recibir notificaciones de tu instructor</span>
            </div>
          </div>

        </div>
      </div>
    </DashboardLayout>
  );
}

export default UnirseFicha
