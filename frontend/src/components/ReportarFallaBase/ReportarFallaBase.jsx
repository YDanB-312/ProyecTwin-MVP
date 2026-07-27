import { useState, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import DashboardLayout from '../DashboardLayout/DashboardLayout'
import PageHeader from '../PageHeader/PageHeader'
import '../../assets/styles/pages/reportar-falla.css'
import FormField from '../FormField/FormField'

export default function ReportarFallaBase({ role, dashboardPath, dashboardTitulo, dashboardUsuario, notificaciones, tipoOptions, reportesAnteriores, cancelPath }) {
  const navigate = useNavigate()
  const { register, handleSubmit, watch } = useForm()
  const [enviado, setEnviado] = useState(false)
  const [error, setError] = useState(null)
  const archivoRef = useRef(null)
  const descTexto = watch("descripcion", "")
  const archivo = watch("url_evidencia")
  const archivoNombre = archivo?.length > 0 ? archivo[0].name : 'No se ha seleccionado ningún archivo'

  const onSubmit = (data) => {
    try {
      setEnviado(true)
      setError(null)
      // TODO: Send data to API endpoint
      setTimeout(() => navigate(dashboardPath), 2000)
    } catch (err) {
      setError('Error al enviar el reporte. Intenta de nuevo.')
    }
  }

  return (
    <DashboardLayout role={role} titulo={dashboardTitulo} usuario={dashboardUsuario} notificaciones={notificaciones}>
      <div className="contenedor-gestion fade-in">
        <PageHeader title="Reportar Falla" icon="bug" breadcrumb={[{ to: dashboardPath, icon: 'home', label: 'Inicio' }, { label: 'Reportar Falla' }]} />

        <div className="seccion-card">
          <div className="seccion-card-header">
            <i className="fas fa-exclamation-triangle"></i>
            <h3>Nuevo reporte de Falla</h3>
          </div>

          <div className={`mensaje-feedback mensaje-exito ${enviado ? '' : 'oculto'}`}>
            <i className="fas fa-check-circle"></i><span>Reporte enviado correctamente.</span>
          </div>
          <div className={`mensaje-feedback mensaje-error ${error ? '' : 'oculto'}`}>
            <i className="fas fa-exclamation-circle"></i><span>No se pudo enviar el reporte. Intenta de nuevo.</span>
          </div>

          <form id="formularioFalla" onSubmit={handleSubmit(onSubmit)}>
            <div className="form-body">
              <FormField label="Título del Reporte" htmlFor="titulo-falla" required>
                <input type="text" id="titulo-falla" className="campo-input" placeholder="Ej: Error al cargar página de login" {...register("titulo", { required: true })} />
              </FormField>
              <FormField label="Tipo de Falla" htmlFor="tipo-falla" required>
                <select id="tipo-falla" className="campo-select" {...register("tipo", { required: true })}>
                  <option value="" disabled>-- Selecciona una opción --</option>
                  {tipoOptions.map((opt, i) => (
                    <option key={i} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </FormField>
              <FormField label="Descripción Detallada" htmlFor="descripcion" required>
                <textarea id="descripcion" className="campo-textarea" placeholder="Describe con detalle el problema encontrado..." {...register("descripcion", { required: true, maxLength: 500 })}></textarea>
                <div className="desc-contador">{descTexto.length}/500 caracteres</div>
              </FormField>
              <FormField label="Pasos para Reproducir" htmlFor="pasos" helpText="Describe los pasos exactos para que otro usuario pueda replicar el problema.">
                <textarea id="pasos" className="campo-textarea" placeholder="1. Ir a la página de login&#10;2. Ingresar credenciales incorrectas&#10;3. Hacer clic en 'Iniciar sesión'&#10;4. Observar el error..." rows="4" {...register("pasos")}></textarea>
              </FormField>
              <FormField label="Adjuntar Evidencia (Opcional)" htmlFor="evidencia" helpText="Formatos aceptados: JPG, PNG, PDF, Word (Max. 10MB por archivo)">
                <div className="file-input-wrapper">
                  <button type="button" className="btn-file" onClick={() => archivoRef.current?.click()}><i className="fas fa-paperclip"></i> Elegir archivo</button>
                  <span className="file-name">{archivoNombre}</span>
                  <input type="file" id="evidencia" ref={archivoRef} className="file-input-real" accept=".jpg,.jpeg,.png,.pdf,.doc,.docx" {...register("url_evidencia")} />
                </div>
              </FormField>
            </div>
            <div className="form-footer">
              <Link to={cancelPath} className="btn-cancelar"><i className="fas fa-times"></i> Cancelar</Link>
              <button type="submit" className="btn-enviar"><i className="fas fa-paper-plane"></i> Enviar reporte</button>
            </div>
          </form>
        </div>

        <div className="seccion-card">
          <div className="seccion-card-header">
            <i className="fas fa-history"></i>
            <h3>Mis reportes Anteriores</h3>
            <span className="reportes-count">{reportesAnteriores.length} reportes registrados</span>
          </div>

          <div className="reportes-grid">
            {reportesAnteriores.map((r, i) => (
              <div className="reporte-card" key={i}>
                <div className="reporte-card-header">
                  <span className="reporte-id">#{String(i + 1).padStart(3, '0')}</span>
                  <span className={`badge-tipo badge-tipo-${r.tipoClase}`}>{r.tipo}</span>
                </div>
                <p className="reporte-descripcion">{r.descripcion}</p>
                <div className="reporte-card-footer">
                  <span className={`badge-estado-reporte ${r.estadoClase}`}><i className={`fas ${r.estadoIcono}`}></i> {r.estado}</span>
                  <span className="reporte-fecha">{r.fecha}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

      </div>
    </DashboardLayout>
  )
}
