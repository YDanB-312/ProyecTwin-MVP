import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/nuevo-proyecto.css'
import '../../assets/styles/pages/reportar-falla.css'

function NuevoProyecto() {
  const navigate = useNavigate()
  const [titulo, setTitulo] = useState('')
  const [palabrasClave, setPalabrasClave] = useState('')
  const [resumenTexto, setResumenTexto] = useState('')
  const [objetivosTexto, setObjetivosTexto] = useState('')
  const [entregablesTexto, setEntregablesTexto] = useState('')
  const [observacionesTexto, setObservacionesTexto] = useState('')

  const handleGuardar = (e) => {
    e.preventDefault()
    navigate('/aprendiz/analizando-proyecto', {
      state: {
        titulo: titulo || 'Sistema de Gestión Académica',
        resumen: resumenTexto,
        palabrasClave: palabrasClave,
        objetivos: objetivosTexto,
        entregables: entregablesTexto,
        observaciones: observacionesTexto,
      }
    })
  }

  const handleBorrador = () => {
    navigate('/aprendiz/analizando-proyecto', {
      state: {
        titulo: titulo || 'Sistema de Gestión Académica',
        resumen: resumenTexto,
        palabrasClave: palabrasClave,
      }
    })
  }
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-formulario">

        <PageHeader
          title="Nuevo proyecto"
          icon="plus-circle"
          breadcrumb={[
            { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
            { to: '/aprendiz/mis-proyectos', label: 'Mis proyectos' },
            { label: 'Nuevo proyecto' }
          ]}
        />

        <div className="mensaje-feedback mensaje-exito oculto">
          <i className="fas fa-check-circle"></i><span>Operación realizada exitosamente.</span>
        </div>
        <div className="mensaje-feedback mensaje-error oculto">
          <i className="fas fa-exclamation-circle"></i><span>Ha ocurrido un error. Intenta nuevamente.</span>
        </div>

        <p className="campos-obligatorios">Los campos marcados con <span className="obligatorio">*</span> son obligatorios.</p>

        <form className="formulario-card" action="#">

          <div className="seccion-formulario">
            <div className="seccion-formulario-header">
              <i className="fas fa-info-circle"></i>
              <h3>Información Básica</h3>
            </div>
            <div className="seccion-formulario-body">
              <div className="grupo-formulario">
                <label htmlFor="titulo" className="etiqueta requerido">Titulo del proyecto</label>
                <input type="text" id="titulo" className="input-text" placeholder="Ingresa un titulo descriptivo para tu proyecto" required name="titulo" value={titulo} onChange={e => setTitulo(e.target.value)} />
                <div className="campo-informacion">Máximo 100 caracteres. Sé claro y específico.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="resumen" className="etiqueta requerido">Resumen del proyecto</label>
                <textarea id="resumen" className="textarea" placeholder="Describe brevemente tu proyecto, incluyendo objetivos principales y metodología..." maxLength="2000" required name="resumen" value={resumenTexto} onChange={e => setResumenTexto(e.target.value)}></textarea>
                <div className="contador-caracteres">{resumenTexto.length}/2000 caracteres</div>
                <div className="campo-informacion">Máximo 300 palabras. Este resumen será usado para detectar Similitudes con otros proyectos.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="palabras-clave" className="etiqueta requerido">Palabras Clave</label>
                <input type="text" id="palabras-clave" className="input-text" placeholder="Ej: desarrollo web, aplicación móvil, base de datos" required name="palabras_clave" value={palabrasClave} onChange={e => setPalabrasClave(e.target.value)} />
                <div className="contador-palabras">{palabrasClave ? palabrasClave.split(',').filter(p => p.trim()).length : 0} de 10 palabras clave (mínimo 3)</div>
                <div className="campo-informacion">Separa cada palabra clave con comas. Mínimo 3, máximo 10.</div>
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
                  <label htmlFor="linea-tecnologica" className="etiqueta requerido">Línea de Aprendizaje</label>
                  <select id="linea-tecnologica" className="select" required name="linea_tecnologica">
                    <option value="">Selecciona una línea tecnológica</option>
                    <option value="ti">Tecnologías de la Información</option>
                    <option value="telecom">Telecomunicaciones</option>
                    <option value="diseno">Diseño Gráfico</option>
                    <option value="electronica">Electricidad y Electronica</option>
                    <option value="automotriz">Automotriz</option>
                    <option value="industrial">Industrial</option>
                    <option value="ambiental">Ambiental</option>
                  </select>
                </div>
                <div className="grupo-formulario">
                  <label htmlFor="tecnologias" className="etiqueta requerido">Tecnologías a Utilizar</label>
                  <input type="text" id="tecnologias" className="input-text" placeholder="Ej: React, Node.js, MongoDB, Python" required name="tecnologias" />
                  <div className="campo-informacion">Lista las principales tecnologías, frameworks y herramientas.</div>
                </div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="objetivos" className="etiqueta requerido">Objetivos Específicos</label>
                <textarea id="objetivos" className="textarea" placeholder="Describe los objetivos específicos de tu proyecto..." required name="objetivos" value={objetivosTexto} onChange={e => setObjetivosTexto(e.target.value)}></textarea>
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
              <h3>Información Adicional</h3>
            </div>
            <div className="seccion-formulario-body">
              <div className="grupo-formulario">
                <label className="etiqueta">Tipo de proyecto</label>
                <div className="radio-grupo">
                  <label className="radio-item">
                    <input type="radio" name="tipo_proyecto" value="aplicacion" className="radio-personalizado-input" defaultChecked />
                    <span className="radio-personalizado"></span>
                    <span>Aplicación/Software</span>
                  </label>
                </div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="observaciones" className="etiqueta">Observaciones Adicionales</label>
                <textarea id="observaciones" className="textarea" placeholder="Agrega cualquier información adicional que consideres relevante..." name="observaciones" value={observacionesTexto} onChange={e => setObservacionesTexto(e.target.value)}></textarea>
                <div className="contador-caracteres">{observacionesTexto.length} caracteres</div>
              </div>
            </div>
          </div>

          <div className="acciones-formulario">
            <div className="acciones-izquierda">
              <button type="submit" className="btn-primario" onClick={handleGuardar}><i className="fas fa-save"></i> Guardar proyecto</button>
              <button type="button" className="btn-outline" onClick={handleBorrador}><i className="fas fa-file-alt"></i> Guardar como Borrador</button>
            </div>
            <Link to="/aprendiz/mis-proyectos" className="btn-enlace"><i className="fas fa-times"></i> Cancelar</Link>
          </div>

        </form>
      </div>
    </DashboardLayout>
  );
}

export default NuevoProyecto;
