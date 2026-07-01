import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/nuevo-proyecto.css'
import '../../assets/styles/pages/reportar-falla.css'

function NuevoProyecto() {
  const navigate = useNavigate()
  const { register, handleSubmit, watch, formState: { errors } } = useForm()
  const titulo = watch('titulo', '')
  const resumenTexto = watch('resumen', '')
  const palabrasClave = watch('palabras_clave', '')
  const objetivosTexto = watch('objetivos', '')
  const entregablesTexto = watch('entregables', '')
  const observacionesTexto = watch('observaciones', '')

  const onGuardar = (data) => {
    navigate('/aprendiz/analizando-proyecto', {
      state: {
        titulo: data.titulo || 'Sistema de Gestión Académica',
        resumen: data.resumen,
        palabrasClave: data.palabras_clave,
        objetivos: data.objetivos,
        entregables: data.entregables,
        observaciones: data.observaciones || '',
      }
    })
  }

  const onBorrador = () => {
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

        <form className="formulario-card" onSubmit={handleSubmit(onGuardar)}>

          <div className="seccion-formulario">
            <div className="seccion-formulario-header">
              <i className="fas fa-info-circle"></i>
              <h3>Información Básica</h3>
            </div>
            <div className="seccion-formulario-body">
              <div className="grupo-formulario">
                <label htmlFor="titulo" className="etiqueta requerido">Título del proyecto</label>
                <input type="text" id="titulo" className="input-text" placeholder="Ingresa un título descriptivo para tu proyecto" {...register("titulo", { required: true })} />
                {errors.titulo && <span className="campo-error">El título es obligatorio</span>}
                <div className="campo-informacion">Máximo 100 caracteres. Sé claro y específico.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="resumen" className="etiqueta requerido">Resumen del proyecto</label>
                <textarea id="resumen" className="textarea" placeholder="Describe brevemente tu proyecto, incluyendo objetivos principales y metodología..." maxLength="2000" {...register("resumen", { required: true })}></textarea>
                {errors.resumen && <span className="campo-error">El resumen es obligatorio</span>}
                <div className="contador-caracteres">{resumenTexto.length}/2000 caracteres</div>
                <div className="campo-informacion">Máximo 300 palabras. Este resumen será usado para detectar Similitudes con otros proyectos.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="palabras-clave" className="etiqueta requerido">Palabras Clave</label>
                <input type="text" id="palabras-clave" className="input-text" placeholder="Ej: desarrollo web, aplicación móvil, base de datos" {...register("palabras_clave", { required: true })} />
                {errors.palabras_clave && <span className="campo-error">Las palabras clave son obligatorias</span>}
                <div className="contador-palabras">{palabrasClave ? palabrasClave.split(',').filter(p => p.trim()).length : 0} de 10 palabras clave (mínimo 3)</div>
                <div className="campo-informacion">Separa cada palabra clave con comas. Mínimo 3, máximo 10.</div>
              </div>
            </div>
          </div>

          <div className="seccion-formulario">
            <div className="seccion-formulario-header">
              <i className="fas fa-cogs"></i>
              <h3>Detalles Técnicos</h3>
            </div>
            <div className="seccion-formulario-body">
              <div className="grupo-campos">
                <div className="grupo-formulario">
                  <label htmlFor="area-aplicacion" className="etiqueta requerido">Área de Aplicación</label>
                  <select id="area-aplicacion" className="select" {...register("area_aplicacion", { required: true })}>
                    <option value="">Selecciona un área</option>
                    <option value="agricultura">Agricultura y Medio Ambiente</option>
                    <option value="alimentacion">Alimentación y Gastronomía</option>
                    <option value="comercio">Comercio y Negocios</option>
                    <option value="comunicacion">Comunicación y Medios</option>
                    <option value="construccion">Construcción y Vivienda</option>
                    <option value="cultura">Cultura y Entretenimiento</option>
                    <option value="deportes">Deportes</option>
                    <option value="educacion">Educación</option>
                    <option value="energia">Energía y Recursos Naturales</option>
                    <option value="finanzas">Finanzas</option>
                    <option value="gestion-publica">Gestión Pública</option>
                    <option value="industria">Industria y Manufactura</option>
                    <option value="salud">Salud</option>
                    <option value="seguridad">Seguridad y Defensa</option>
                    <option value="servicios-sociales">Servicios Sociales</option>
                    <option value="tecnologia">Tecnología e Informática</option>
                    <option value="transporte">Transporte y Logística</option>
                    <option value="turismo">Turismo y Hotelería</option>
                    <option value="otro">Otro</option>
                  </select>
                </div>
                <div className="grupo-formulario">
                  <label htmlFor="tecnologias" className="etiqueta requerido">Tecnologías a Utilizar</label>
                  <input type="text" id="tecnologias" className="input-text" placeholder="Ej: React, Node.js, MongoDB, Python" {...register("tecnologias", { required: true })} />
                  {errors.tecnologias && <span className="campo-error">Las tecnologías son obligatorias</span>}
                  <div className="campo-informacion">Lista las principales tecnologías, frameworks y herramientas.</div>
                </div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="objetivos" className="etiqueta requerido">Objetivos Específicos</label>
                <textarea id="objetivos" className="textarea" placeholder="Describe los objetivos específicos de tu proyecto..." {...register("objetivos", { required: true })}></textarea>
                {errors.objetivos && <span className="campo-error">Los objetivos son obligatorios</span>}
                <div className="contador-caracteres">{objetivosTexto.length} caracteres</div>
                <div className="campo-informacion">Enumera los objetivos de manera clara y medible.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="entregables" className="etiqueta requerido">Entregables Esperados</label>
                <textarea id="entregables" className="textarea" placeholder="Describe los productos o resultados que entregarás al finalizar el proyecto..." {...register("entregables", { required: true })}></textarea>
                {errors.entregables && <span className="campo-error">Los entregables son obligatorios</span>}
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
                    <input type="checkbox" {...register("integrantes")} value="1" className="checkbox-personalizado-input" defaultChecked />
                    <span className="checkbox-personalizado"></span>
                    <div className="miembro-avatar">MG</div>
                    <div className="miembro-info">
                      <span className="miembro-nombre">Maria Gonzalez</span>
                      <span className="miembro-rol">(tu)</span>
                    </div>
                  </label>
                  <label className="miembro-card">
                    <input type="checkbox" {...register("integrantes")} value="2" className="checkbox-personalizado-input" />
                    <span className="checkbox-personalizado"></span>
                    <div className="miembro-avatar">JP</div>
                    <div className="miembro-info">
                      <span className="miembro-nombre">Juan Pérez</span>
                    </div>
                  </label>
                  <label className="miembro-card">
                    <input type="checkbox" {...register("integrantes")} value="3" className="checkbox-personalizado-input" />
                    <span className="checkbox-personalizado"></span>
                    <div className="miembro-avatar">LG</div>
                    <div className="miembro-info">
                      <span className="miembro-nombre">Laura Gómez</span>
                    </div>
                  </label>
                  <label className="miembro-card">
                    <input type="checkbox" {...register("integrantes")} value="4" className="checkbox-personalizado-input" />
                    <span className="checkbox-personalizado"></span>
                    <div className="miembro-avatar">AM</div>
                    <div className="miembro-info">
                      <span className="miembro-nombre">Ana Martínez</span>
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
                    <input type="radio" {...register("tipo_proyecto")} value="aplicacion" className="radio-personalizado-input" defaultChecked />
                    <span className="radio-personalizado"></span>
                    <span>Aplicación/Software</span>
                  </label>
                </div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="observaciones" className="etiqueta">Observaciones Adicionales</label>
                <textarea id="observaciones" className="textarea" placeholder="Agrega cualquier información adicional que consideres relevante..." {...register("observaciones")}></textarea>
                <div className="contador-caracteres">{observacionesTexto.length} caracteres</div>
              </div>
            </div>
          </div>

          <div className="acciones-formulario">
            <div className="acciones-izquierda">
              <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar proyecto</button>
              <button type="button" className="btn-outline" onClick={onBorrador}><i className="fas fa-file-alt"></i> Guardar como Borrador</button>
            </div>
            <Link to="/aprendiz/mis-proyectos" className="btn-enlace"><i className="fas fa-times"></i> Cancelar</Link>
          </div>

        </form>
      </div>
    </DashboardLayout>
  );
}

export default NuevoProyecto;
