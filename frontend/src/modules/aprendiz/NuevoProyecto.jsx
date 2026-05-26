// NuevoProyecto - Pagina de formulario para registrar un nuevo proyecto formativo en el sistema
import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarAprendiz from '../../components/SidebarAprendiz/SidebarAprendiz';
import FooterAprendiz from '../../components/FooterAprendiz/FooterAprendiz';
import '../../assets/styles/pages/nuevo-proyecto.css';

function NuevoProyecto() {
  return (
    <div className="modulo-aprendiz">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5} />
      <SidebarAprendiz />
      <main className="contenido-principal">
        <div className="contenedor-formulario">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-plus-circle"></i> Nuevo proyecto</h1>
            <Link to="/aprendiz/mis-proyectos" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Mis proyectos</Link>
          </div>
          <div className="mensaje-feedback mensaje-exito oculto">
            <i className="fas fa-check-circle"></i>
            <span>Operacion realizada exitosamente.</span>
          </div>
          <div className="mensaje-feedback mensaje-error oculto">
            <i className="fas fa-exclamation-circle"></i>
            <span>Ha ocurrido un error. Intenta nuevamente.</span>
          </div>
          <form className="formulario-proyecto" action="#">
            <section className="seccion-formulario">
              <h2 className="titulo-seccion"><i className="fas fa-info-circle"></i> Informacion Basica</h2>
              <div className="grupo-formulario">
                <label htmlFor="titulo" className="etiqueta requerido">Titulo del proyecto</label>
                <input type="text" id="titulo" className="input-text" placeholder="Ingresa un titulo descriptivo para tu proyecto" required name="titulo" />
                <div className="campo-informacion">Maximo 100 caracteres. Se claro y especifico.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="resumen" className="etiqueta requerido">Resumen del proyecto</label>
                <textarea id="resumen" className="textarea" placeholder="Describe brevemente tu proyecto, incluyendo objetivos principales y metodologia..." maxLength="2000" required name="resumen"></textarea>
                <div className="contador-caracteres"><span id="contador-resumen">0</span>/2000 caracteres</div>
                <div className="campo-informacion">Maximo 300 palabras. Este resumen sera usado para detectar Similitudes con otros proyectos.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="palabras-clave" className="etiqueta requerido">Palabras Clave</label>
                <input type="text" id="palabras-clave" className="input-text" placeholder="Ej: desarrollo web, aplicacion movil, base de datos" required name="palabras_clave" />
                <div className="campo-informacion">Separa cada palabra clave con comas. Minimo 3, maximo 10.</div>
              </div>
            </section>
            <section className="seccion-formulario">
              <h2 className="titulo-seccion"><i className="fas fa-cogs"></i> Detalles Tecnicos</h2>
              <div className="grupo-campos">
                <div className="grupo-formulario">
                  <label htmlFor="linea-tecnologica" className="etiqueta requerido">Linea de Aprendizaje</label>
                  <select id="linea-tecnologica" className="select" required name="linea_tecnologica">
                    <option value="">Selecciona una linea tecnologica</option>
                    <option value="ti">Tecnologias de la Informacion</option>
                    <option value="telecom">Telecomunicaciones</option>
                    <option value="diseno">Diseno Grafico</option>
                    <option value="electronica">Electricidad y Electronica</option>
                    <option value="automotriz">Automotriz</option>
                    <option value="industrial">Industrial</option>
                    <option value="ambiental">Ambiental</option>
                  </select>
                </div>
                <div className="grupo-formulario">
                  <label htmlFor="tecnologias" className="etiqueta requerido">Tecnologias a Utilizar</label>
                  <input type="text" id="tecnologias" className="input-text" placeholder="Ej: React, Node.js, MongoDB, Python" required name="tecnologias" />
                  <div className="campo-informacion">Lista las principales tecnologias, frameworks y herramientas.</div>
                </div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="objetivos" className="etiqueta requerido">Objetivos Especificos</label>
                <textarea id="objetivos" className="textarea" placeholder="Describe los objetivos especificos de tu proyecto..." required name="objetivos"></textarea>
                <div className="campo-informacion">Enumera los objetivos de manera clara y medible.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="entregables" className="etiqueta requerido">Entregables Esperados</label>
                <textarea id="entregables" className="textarea" placeholder="Describe los productos o resultados que entregaras al finalizar el proyecto..." required name="entregables"></textarea>
                <div className="campo-informacion">Especifica los entregables tangibles de tu proyecto.</div>
              </div>
            </section>
            <section className="seccion-formulario">
              <h2 className="titulo-seccion"><i className="fas fa-users"></i> Integrantes del Equipo</h2>
              <div className="grupo-formulario">
                <label className="etiqueta">Selecciona los integrantes de tu ficha que participaran en este proyecto</label>
                <div className="grid-miembros grid-miembros-sm">
                  <label className="etiqueta-checkbox etiqueta-checkbox-card">
                    <input type="checkbox" name="integrantes" value="1" className="checkbox-proyecto" checked />
                    <div className="avatar-miembro avatar-sm">MG</div>
                    <span className="texto-md">Maria Gonzalez (tu)</span>
                  </label>
                  <label className="etiqueta-checkbox etiqueta-checkbox-card">
                    <input type="checkbox" name="integrantes" value="2" className="checkbox-proyecto" />
                    <div className="avatar-miembro avatar-sm">JP</div>
                    <span className="texto-md">Juan Perez</span>
                  </label>
                  <label className="etiqueta-checkbox etiqueta-checkbox-card">
                    <input type="checkbox" name="integrantes" value="3" className="checkbox-proyecto" />
                    <div className="avatar-miembro avatar-sm">LG</div>
                    <span className="texto-md">Laura Gomez</span>
                  </label>
                  <label className="etiqueta-checkbox etiqueta-checkbox-card">
                    <input type="checkbox" name="integrantes" value="4" className="checkbox-proyecto" />
                    <div className="avatar-miembro avatar-sm">AM</div>
                    <span className="texto-md">Ana Martinez</span>
                  </label>
                </div>
                <div className="campo-informacion">Selecciona a los companeros de tu ficha que trabajaran contigo en este proyecto.</div>
              </div>
            </section>
            <section className="seccion-formulario">
              <h2 className="titulo-seccion"><i className="fas fa-clipboard-list"></i> Informacion Adicional</h2>
              <div className="grupo-campos">
                <div className="grupo-formulario">
                  <label htmlFor="duracion" className="etiqueta">Duracion Estimada (meses)</label>
                  <input type="number" id="duracion" className="input-text" min="1" max="24" placeholder="6" name="duracion_estimada" />
                </div>
                <div className="grupo-formulario">
                  <label htmlFor="fecha-inicio" className="etiqueta">Fecha de Inicio Estimada</label>
                  <input type="date" id="fecha-inicio" className="input-text" name="fecha_inicio_estimada" />
                </div>
              </div>
              <div className="grupo-formulario">
                <label className="etiqueta">Tipo de proyecto</label>
                <div>
                  <label className="etiqueta-checkbox">
                    <input type="radio" name="tipo_proyecto" value="aplicacion" className="checkbox-proyecto" checked />
                    Aplicacion/Software
                  </label>
                </div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="observaciones" className="etiqueta">Observaciones Adicionales</label>
                <textarea id="observaciones" className="textarea" placeholder="Agrega cualquier informacion adicional que consideres relevante..." name="observaciones"></textarea>
              </div>
            </section>
            <div className="acciones-formulario">
              <div className="acciones-izquierda">
                <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar proyecto</button>
                <button type="button" className="btn-secundario"><i className="fas fa-file-alt"></i> Guardar como Borrador</button>
              </div>
              <Link to="/aprendiz/mis-proyectos" className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
            </div>
          </form>
        </div>
      </main>
      <FooterAprendiz />
    </div>
  );
}

export default NuevoProyecto;
