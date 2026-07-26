import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/reportar-falla.css'
import '../../assets/styles/pages/nuevo-proyecto.css'
import FormField from '../../components/FormField/FormField'

export default function NuevoProyecto() {
  const [enviado, setEnviado] = useState(false)
  const [error, setError] = useState(null)
  const navigate = useNavigate()
  const location = useLocation()
  const editProyecto = location.state?.editProyecto

  const { register, handleSubmit, watch, trigger, reset, formState: { errors } } = useForm({
    defaultValues: editProyecto ? { titulo: editProyecto.titulo, tipo_proyecto: 'aplicacion', integrantes: ['1'] } : { tipo_proyecto: 'aplicacion', integrantes: ['1'] }
  })

  useEffect(() => {
    if (editProyecto) {
      reset({
        titulo: editProyecto.titulo || '',
        resumen: editProyecto.resumen || '',
        palabras_clave: editProyecto.palabras_clave || '',
        area_aplicacion: editProyecto.area_aplicacion || '',
        tecnologias: editProyecto.tecnologias || '',
        objetivos: editProyecto.objetivos || '',
        entregables: editProyecto.entregables || '',
        tipo_proyecto: editProyecto.tipo_proyecto || 'aplicacion',
        observaciones: editProyecto.observaciones || '',
      })
    }
  }, [editProyecto, reset])
  const titulo = watch('titulo', '')
  const resumenTexto = watch('resumen', '')
  const palabrasClave = watch('palabras_clave', '')
  const objetivosTexto = watch('objetivos', '')
  const entregablesTexto = watch('entregables', '')
  const observacionesTexto = watch('observaciones', '')

  const onGuardar = (data) => {
    setEnviado(true)
    setError(null)
    const objetivosArray = data.objetivos ? data.objetivos.split('\n').filter(l => l.trim()) : []
    const entregablesArray = data.entregables ? data.entregables.split('\n').filter(l => l.trim()) : []
    navigate('/aprendiz/analizando-proyecto', {
      state: {
        titulo: data.titulo || 'Sistema de Gestión Académica',
        resumen: data.resumen,
        palabrasClave: data.palabras_clave,
        objetivos: objetivosArray,
        entregables: entregablesArray,
        observaciones: data.observaciones || '',
      }
    })
  }

  const onBorrador = async () => {
    const valido = await trigger(['titulo', 'resumen', 'palabras_clave'])
    if (!valido) return
    setEnviado(true)
    setError(null)
    const objetivosArray = objetivosTexto ? objetivosTexto.split('\n').filter(l => l.trim()) : []
    const entregablesArray = entregablesTexto ? entregablesTexto.split('\n').filter(l => l.trim()) : []
    navigate('/aprendiz/analizando-proyecto', {
      state: {
        titulo: titulo || 'Sistema de Gestión Académica',
        resumen: resumenTexto,
        palabrasClave: palabrasClave,
        objetivos: objetivosArray,
        entregables: entregablesArray,
      }
    })
  }
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-formulario fade-in">

        <PageHeader
          title={editProyecto ? 'Editar proyecto' : 'Nuevo proyecto'}
          icon={editProyecto ? 'edit' : 'plus-circle'}
          breadcrumb={[
            { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
            { to: '/aprendiz/mis-proyectos', label: 'Mis proyectos' },
            { label: editProyecto ? 'Editar proyecto' : 'Nuevo proyecto' }
          ]}
        />

        <div className={`mensaje-feedback mensaje-exito ${enviado ? '' : 'oculto'}`}>
          <i className="fas fa-check-circle"></i><span>Proyecto enviado a revisión correctamente.</span>
        </div>
        <div className={`mensaje-feedback mensaje-error ${error ? '' : 'oculto'}`}>
          <i className="fas fa-exclamation-circle"></i><span>No se pudo enviar el proyecto. Intenta de nuevo.</span>
        </div>

        <p className="campos-obligatorios">Los campos marcados con <span className="obligatorio">*</span> son obligatorios.</p>

        <form className="formulario-card" onSubmit={handleSubmit(onGuardar)}>

          <div className="seccion-formulario">
            <div className="seccion-formulario-header">
              <i className="fas fa-info-circle"></i>
              <h3>Información Básica</h3>
            </div>
            <div className="seccion-formulario-body">
              <FormField label="Título del proyecto" htmlFor="titulo" required error={errors.titulo && 'El título es obligatorio'} helpText="Máximo 100 caracteres. Sé claro y específico.">
                <input type="text" id="titulo" className="campo-input" placeholder="Ingresa un título descriptivo para tu proyecto" {...register("titulo", { required: true })} />
              </FormField>
              <div className="grupo-formulario">
                <label htmlFor="resumen" className="etiqueta requerido">Resumen del proyecto</label>
                <textarea id="resumen" className="textarea" placeholder="Describe brevemente tu proyecto, incluyendo objetivos principales y metodología..." maxLength="2000" {...register("resumen", { required: true })}></textarea>
                {errors.resumen && <span className="campo-error">El resumen es obligatorio</span>}
                <div className="contador-caracteres">{resumenTexto.length}/2000 caracteres</div>
                <div className="campo-informacion">Máximo 300 palabras. Este resumen será usado para detectar Similitudes con otros proyectos.</div>
              </div>
              <FormField label="Palabras Clave" htmlFor="palabras-clave" required error={errors.palabras_clave && 'Las palabras clave son obligatorias'} helpText="Separa cada palabra clave con comas. Mínimo 3, máximo 10.">
                <input type="text" id="palabras-clave" className="campo-input" placeholder="Ej: desarrollo web, aplicación móvil, base de datos" {...register("palabras_clave", { required: true })} />
                <div className="contador-palabras">{palabrasClave ? palabrasClave.split(',').filter(p => p.trim()).length : 0} de 10 palabras clave (mínimo 3)</div>
              </FormField>
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
                <FormField label="Tecnologías a Utilizar" htmlFor="tecnologias" required error={errors.tecnologias && 'Las tecnologías son obligatorias'} helpText="Lista las principales tecnologías, frameworks y herramientas.">
                  <input type="text" id="tecnologias" className="campo-input" placeholder="Ej: React, Node.js, MongoDB, Python" {...register("tecnologias", { required: true })} />
                </FormField>
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
                    <input type="checkbox" {...register("integrantes")} value="1" className="checkbox-personalizado-input" />
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
                    <input type="radio" {...register("tipo_proyecto")} value="aplicacion" className="radio-personalizado-input" />
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
            <Link to="/aprendiz/mis-proyectos" className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
          </div>

        </form>
      </div>
    </DashboardLayout>
  );
}


