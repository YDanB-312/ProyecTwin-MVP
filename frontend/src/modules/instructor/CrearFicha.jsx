import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarInstructor from '../../components/SidebarInstructor/SidebarInstructor';
import FooterInstructor from '../../components/FooterInstructor/FooterInstructor';
import '../../assets/styles/pages/fichas.css';

function CrearFicha() {
  return (
    <div className="modulo-instructor">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8} />
      <SidebarInstructor />
      <main className="contenido-principal">
        <div className="contenedor-pagina">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-plus-circle"></i> Crear Nueva Ficha</h1>
            <Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>
          <div className="mensaje-feedback mensaje-exito oculto">
            <i className="fas fa-check-circle"></i>
            <span>Operación realizada exitosamente.</span>
          </div>
          <div className="mensaje-feedback mensaje-error oculto">
            <i className="fas fa-exclamation-circle"></i>
            <span>Ha ocurrido un error. Intenta nuevamente.</span>
          </div>
          <form action="#" className="formulario-proyecto">
            <section className="seccion-formulario">
              <h2 className="titulo-seccion"><i className="fas fa-info-circle"></i> Información de la Ficha</h2>
              <div className="grupo-formulario">
                <label htmlFor="codigo" className="etiqueta requerido">Código de Ficha</label>
                <input type="text" id="codigo" className="input-text" placeholder="Ej: ADSO-2568" required name="codigo" />
                <div className="campo-informacion">Código único que identificará al grupo (ej: PROGRAMA-NÚMERO).</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="id_programa" className="etiqueta requerido">Programa de Formación</label>
                <select id="id_programa" className="select-filtro" required name="id_programa">
                  <option value="">Seleccione un programa</option>
                  <option value="1">ADSO - Análisis y Desarrollo de Sistemas</option>
                  <option value="2">Multimedia</option>
                  <option value="3">IoT</option>
                </select>
              </div>
            </section>
            <div className="acciones-finales margen-superior">
              <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Crear Ficha</button>
              <Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
            </div>
          </form>
        </div>
      </main>
      <FooterInstructor />
    </div>
  );
}

export default CrearFicha;
