import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/nuevo-proyecto.css'

function NuevoProyecto() {
  const [palabrasClave, setPalabrasClave] = useState('')
  const [resumenTexto, setResumenTexto] = useState('')
  const [objetivosTexto, setObjetivosTexto] = useState('')
  const [entregablesTexto, setEntregablesTexto] = useState('')
  const [observacionesTexto, setObservacionesTexto] = useState('')
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-formulario">

        <Link to="/aprendiz/mis-proyectos" className="volver-link"><i className="fas fa-arrow-left"></i> Volver a Mis proyectos</Link>

        <div className="encabezado-con-boton">
          <div>
            <span className="encabezado-subtitulo">Panel del Aprendiz</span>
            <h2 className="titulo-seccion-dashboard">Nuevo proyecto</h2>
          </div>
        </div>

        <div className="mensaje-feedback mensaje-exito oculto">
          <i className="fas fa-check-circle"></i><span>Operacion realizada exitosamente.</span>
        </div>
        <div className="mensaje-feedback mensaje-error oculto">
          <i className="fas fa-exclamation-circle"></i><span>Ha ocurrido un error. Intenta nuevamente.</span>
        </div>

        <p className="campos-obligatorios">Los campos marcados con <span className="obligatorio">*</span> son obligatorios.</p>

        <form className="formulario-card" action="#">

          <div className="seccion-formulario">
            <div className="seccion-formulario-header">
              <i className="fas fa-info-circle"></i>
              <h3>Informacion Basica</h3>
            </div>
            <div className="seccion-formulario-body">
              <div className="grupo-formulario">
                <label htmlFor="titulo" className="etiqueta requerido">Titulo del proyecto</label>
                <input type="text" id="titulo" className="input-text" placeholder="Ingresa un titulo descriptivo para tu proyecto" required name="titulo" />
                <div className="campo-informacion">Maximo 100 caracteres. Se claro y especifico.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="resumen" className="etiqueta requerido">Resumen del proyecto</label>
                <textarea id="resumen" className="textarea" placeholder="Describe brevemente tu proyecto, incluyendo objetivos principales y metodologia..." maxLength="2000" required name="resumen" value={resumenTexto} onChange={e => setResumenTexto(e.target.value)}></textarea>
                <div className="contador-caracteres">{resumenTexto.length}/2000 caracteres</div>
                <div className="campo-informacion">Maximo 300 palabras. Este resumen sera usado para detectar Similitudes con otros proyectos.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="palabras-clave" className="etiqueta requerido">Palabras Clave</label>
                <input type="text" id="palabras-clave" className="input-text" placeholder="Ej: desarrollo web, aplicacion movil, base de datos" required name="palabras_clave" value={palabrasClave} onChange={e => setPalabrasClave(e.target.value)} />
                <div className="contador-palabras">{palabrasClave ? palabrasClave.split(',').filter(p => p.trim()).length : 0} de 10 palabras clave (mínimo 3)</div>
                <div className="campo-informacion">Separa cada palabra clave con comas. Minimo 3, maximo 10.</div>
              </div>
            </div>
          </div>

          <div className="seccion-formulario">
            <div className="seccion-formulario-header">
              <i className="fas fa-cogs"></i>
              <h3>Detalles Tecnicos</h3>
            </div>
            <div className="seccion-formulario-body">
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
                <textarea id="objetivos" className="textarea" placeholder="Describe los objetivos especificos de tu proyecto..." required name="objetivos" value={objetivosTexto} onChange={e => setObjetivosTexto(e.target.value)}></textarea>
                <div className="contador-caracteres">{objetivosTexto.length} caracteres</div>
                <div className="campo-informacion">Enumera los objetivos de manera clara y medible.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="entregables" className="etiqueta requerido">Entregables Esperados</label>
                <textarea id="entregables" className="textarea" placeholder="Describe los productos o resultados que entregaras al finalizar el proyecto..." required name="entregables" value={entregablesTexto} onChange={e => setEntregablesTexto(e.target.value)}></textarea>
                <div className="contador-caracteres">{entregablesTexto.length} caracteres</div>
                <div className="campo-informacion">Especifica los entregables tangibles de tu proyecto.</div>
              </div>
            </div>
          </div>

          <div className="seccion-formulario">
            <div className="seccion-formulario-header">
              <i className="fas fa-users"></i>
              <h3>Integrantes del Equipo</h3>
            </div>
            <div className="seccion-formulario-body">
              <div className="grupo-formulario">
                <div className="integrantes-encabezado">
                  <label className="etiqueta">Selecciona los integrantes de tu ficha</label>
                  <div className="integrantes-controles">
                    <label className="seleccionar-todos">
                      <input type="checkbox" className="checkbox-personalizado-input" />
                      <span className="checkbox-personalizado"></span>
                      <span>Seleccionar todos</span>
                    </label>
                    <span className="contador-seleccionados">1 seleccionado</span>
                  </div>
                </div>
                <div className="grid-miembros">
                  <label className="miembro-card seleccionado">
                    <input type="checkbox" name="integrantes" value="1" className="checkbox-personalizado-input" defaultChecked />
                    <span className="checkbox-personalizado"></span>
                    <div className="miembro-avatar">MG</div>
                    <div className="miembro-info">
                      <span className="miembro-nombre">Maria Gonzalez</span>
                      <span className="miembro-rol">(tu)</span>
                    </div>
                  </label>
                  <label className="miembro-card">
                    <input type="checkbox" name="integrantes" value="2" className="checkbox-personalizado-input" />
                    <span className="checkbox-personalizado"></span>
                    <div className="miembro-avatar">JP</div>
                    <div className="miembro-info">
                      <span className="miembro-nombre">Juan Perez</span>
                    </div>
                  </label>
                  <label className="miembro-card">
                    <input type="checkbox" name="integrantes" value="3" className="checkbox-personalizado-input" />
                    <span className="checkbox-personalizado"></span>
                    <div className="miembro-avatar">LG</div>
                    <div className="miembro-info">
                      <span className="miembro-nombre">Laura Gomez</span>
                    </div>
                  </label>
                  <label className="miembro-card">
                    <input type="checkbox" name="integrantes" value="4" className="checkbox-personalizado-input" />
                    <span className="checkbox-personalizado"></span>
                    <div className="miembro-avatar">AM</div>
                    <div className="miembro-info">
                      <span className="miembro-nombre">Ana Martinez</span>
                    </div>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div className="seccion-formulario">
            <div className="seccion-formulario-header">
              <i className="fas fa-clipboard-list"></i>
              <h3>Informacion Adicional</h3>
            </div>
            <div className="seccion-formulario-body">
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
                <div className="radio-grupo">
                  <label className="radio-item">
                    <input type="radio" name="tipo_proyecto" value="aplicacion" className="radio-personalizado-input" defaultChecked />
                    <span className="radio-personalizado"></span>
                    <span>Aplicacion/Software</span>
                  </label>
                </div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="observaciones" className="etiqueta">Observaciones Adicionales</label>
                <textarea id="observaciones" className="textarea" placeholder="Agrega cualquier informacion adicional que consideres relevante..." name="observaciones" value={observacionesTexto} onChange={e => setObservacionesTexto(e.target.value)}></textarea>
                <div className="contador-caracteres">{observacionesTexto.length} caracteres</div>
              </div>
            </div>
          </div>

          <div className="acciones-formulario">
            <div className="acciones-izquierda">
              <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar proyecto</button>
              <button type="button" className="btn-outline"><i className="fas fa-file-alt"></i> Guardar como Borrador</button>
            </div>
            <Link to="/aprendiz/mis-proyectos" className="btn-enlace"><i className="fas fa-times"></i> Cancelar</Link>
          </div>

        </form>
      </div>
    </DashboardLayout>
  );
}

export default NuevoProyecto;
