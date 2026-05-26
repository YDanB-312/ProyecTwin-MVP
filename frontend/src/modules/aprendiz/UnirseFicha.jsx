// UnirseFicha - Pagina para que el aprendiz se una a una ficha de formacion mediante codigo
import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarAprendiz from '../../components/SidebarAprendiz/SidebarAprendiz';
import FooterAprendiz from '../../components/FooterAprendiz/FooterAprendiz';
import '../../assets/styles/pages/fichas.css';

function UnirseFicha() {
  return (
    <div className="modulo-aprendiz">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5} />
      <SidebarAprendiz />
      <main className="contenido-principal">
        <div className="contenedor-pagina">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-user-plus"></i> Unirse a una Ficha</h1>
            <Link to="/aprendiz/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>
          <div className="unirse-ficha">
            <div className="tarjeta tarjeta-padded">
              <div><i className="fas fa-users"></i></div>
              <h2>Ingresa el codigo de tu ficha</h2>
              <p>Solicita a tu instructor el codigo de la ficha a la que perteneces e ingresalo aqui.</p>
              <div className="mensaje-feedback mensaje-exito oculto">
                <i className="fas fa-check-circle"></i>
                <span>Operacion realizada exitosamente.</span>
              </div>
              <div className="mensaje-feedback mensaje-error oculto">
                <i className="fas fa-exclamation-circle"></i>
                <span>Ha ocurrido un error. Intenta nuevamente.</span>
              </div>
              <form action="#">
                <div className="grupo-formulario">
                  <label htmlFor="codigo-ficha" className="etiqueta requerido">Codigo de Ficha</label>
                  <input type="text" id="codigo-ficha" className="input-codigo" placeholder="ADSO-2568" required name="ficha" />
                </div>
                <button type="submit" className="btn-primario"><i className="fas fa-sign-in-alt"></i> Unirse a la Ficha</button>
              </form>
            </div>
            <div className="tarjeta tarjeta-padded mt-20">
              <h3><i className="fas fa-question-circle"></i> ¿Que es una ficha?</h3>
              <p>Una ficha es tu grupo de formacion dentro del SENA. Al unirte a una ficha podras:</p>
              <ul>
                <li>Formar parte del grupo de trabajo con tus companeros</li>
                <li>Trabajar en equipo en los proyectos asignados</li>
                <li>Recibir notificaciones de tu instructor</li>
              </ul>
            </div>
          </div>
        </div>
      </main>
      <FooterAprendiz />
    </div>
  );
}

export default UnirseFicha;
