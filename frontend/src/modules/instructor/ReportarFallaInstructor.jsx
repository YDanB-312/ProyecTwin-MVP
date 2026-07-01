import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/reportar-falla.css'

function ReportarFallaInstructor() {
  const { register, handleSubmit, watch } = useForm()
  const descTexto = watch("descripcion", "")
  const archivo = watch("url_evidencia")
  const archivoNombre = archivo?.length > 0 ? archivo[0].name : 'No se ha seleccionado ningún archivo'

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-gestion">

        <Link to="/instructor/dashboard" className="volver-link"><i className="fas fa-arrow-left"></i> Volver al Dashboard</Link>

        <div className="notificaciones-header mb-32">
          <h1 className="notificaciones-titulo">Reportar Falla</h1>
        </div>

        <div className="seccion-card">
          <div className="seccion-card-header">
            <i className="fas fa-exclamation-triangle"></i>
            <h3>Nuevo reporte de Falla</h3>
          </div>

          <div className="mensaje-feedback mensaje-exito oculto">
            <i className="fas fa-check-circle"></i><span>Operación realizada exitosamente.</span>
          </div>
          <div className="mensaje-feedback mensaje-error oculto">
            <i className="fas fa-exclamation-circle"></i><span>Ha ocurrido un error. Intenta nuevamente.</span>
          </div>

          <form id="formularioFalla" onSubmit={handleSubmit(() => {})}>
            <div className="form-body">
              <div className="grupo-campo">
                <label htmlFor="tipo-falla" className="campo-label">Tipo de Falla <span className="obligatorio">*</span></label>
                <select id="tipo-falla" className="campo-select" {...register("tipo", { required: true })}>
                  <option value="" disabled>-- Selecciona una opción --</option>
                  <option value="sistema">Error del sistema</option>
                  <option value="proyecto">Problema con proyectos asignados</option>
                  <option value="datos">Error de datos</option>
                  <option value="otro">Otro</option>
                </select>
              </div>
              <div className="grupo-campo">
                <label htmlFor="descripcion" className="campo-label">Descripción Detallada <span className="obligatorio">*</span></label>
                <textarea id="descripcion" className="campo-textarea" placeholder="Describe con detalle el problema encontrado, los pasos que seguiste y el resultado esperado..." {...register("descripcion", { required: true, maxLength: 500 })}></textarea>
                <div className="desc-contador">{descTexto.length}/500 caracteres</div>
              </div>
              <div className="grupo-campo">
                <label htmlFor="evidencia" className="campo-label">Adjuntar Evidencia (Opcional)</label>
                <div className="file-input-wrapper">
                  <button type="button" className="btn-file" onClick={() => document.getElementById('evidencia').click()}><i className="fas fa-paperclip"></i> Elegir archivo</button>
                  <span className="file-name">{archivoNombre}</span>
                  <input type="file" id="evidencia" className="file-input-real" accept=".jpg,.jpeg,.png,.pdf,.doc,.docx" {...register("url_evidencia")} />
                </div>
                <span className="campo-info">Formatos aceptados: JPG, PNG, PDF, Word (Max. 10MB por archivo)</span>
              </div>
            </div>
            <div className="form-footer">
              <Link to="/instructor/dashboard" className="btn-cancelar"><i className="fas fa-times"></i> Cancelar</Link>
              <button type="submit" className="btn-enviar"><i className="fas fa-paper-plane"></i> Enviar reporte</button>
            </div>
          </form>
        </div>

        <div className="seccion-card">
          <div className="seccion-card-header">
            <i className="fas fa-history"></i>
            <h3>Mis reportes Anteriores</h3>
            <span className="reportes-count">3 reportes registrados</span>
          </div>

          <div className="reportes-grid">

            <div className="reporte-card">
              <div className="reporte-card-header">
                <span className="reporte-id">#001</span>
                <span className="badge-tipo badge-tipo-sistema">Error del sistema</span>
              </div>
              <p className="reporte-descripcion">El sistema no permite calificar propuestas de los aprendices</p>
              <div className="reporte-card-footer">
                <span className="badge-estado-reporte badge-exito"><i className="fas fa-check-circle"></i> Resuelto</span>
                <span className="reporte-fecha">10 Oct 2026</span>
              </div>
            </div>

            <div className="reporte-card">
              <div className="reporte-card-header">
                <span className="reporte-id">#002</span>
                <span className="badge-tipo badge-tipo-datos">Error de datos</span>
              </div>
              <p className="reporte-descripcion">Los proyectos asignados no muestran correctamente la información</p>
              <div className="reporte-card-footer">
                <span className="badge-estado-reporte badge-advertencia"><i className="fas fa-spinner"></i> En Revisión</span>
                <span className="reporte-fecha">25 Oct 2026</span>
              </div>
            </div>

            <div className="reporte-card">
              <div className="reporte-card-header">
                <span className="reporte-id">#003</span>
                <span className="badge-tipo badge-tipo-proyecto">Problema con proyectos</span>
              </div>
              <p className="reporte-descripcion">No puedo asignar instructores a nuevos proyectos</p>
              <div className="reporte-card-footer">
                <span className="badge-estado-reporte badge-advertencia"><i className="fas fa-clock"></i> Pendiente</span>
                <span className="reporte-fecha">02 Nov 2026</span>
              </div>
            </div>

          </div>
        </div>

      </div>
    </DashboardLayout>
  );
}

export default ReportarFallaInstructor;
