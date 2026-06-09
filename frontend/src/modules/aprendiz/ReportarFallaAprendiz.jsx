import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/reportar-falla.css'

function ReportarFallaAprendiz() {
  const [descTexto, setDescTexto] = useState('')
  const [archivoNombre, setArchivoNombre] = useState('No se ha seleccionado ningun archivo')

  function manejarArchivo(e) {
    const file = e.target.files[0]
    if (file) {
      setArchivoNombre(file.name)
    } else {
      setArchivoNombre('No se ha seleccionado ningun archivo')
    }
  }

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-gestion">

        <PageHeader
          title="Reportar Falla"
          icon="bug"
          breadcrumb={[
            { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
            { label: 'Reportar Falla' }
          ]}
        />

        <div className="seccion-card">
          <div className="seccion-card-header">
            <i className="fas fa-exclamation-triangle"></i>
            <h3>Nuevo reporte de Falla</h3>
          </div>

          <div className="mensaje-feedback mensaje-exito oculto">
            <i className="fas fa-check-circle"></i><span>Operacion realizada exitosamente.</span>
          </div>
          <div className="mensaje-feedback mensaje-error oculto">
            <i className="fas fa-exclamation-circle"></i><span>Ha ocurrido un error. Intenta nuevamente.</span>
          </div>

          <form id="formularioFalla" action="#">
            <div className="form-body">
              <div className="grupo-campo">
                <label htmlFor="tipo-falla" className="campo-label">Tipo de Falla <span className="obligatorio">*</span></label>
                <select id="tipo-falla" className="campo-select" required name="tipo">
                  <option value="" disabled selected>-- Selecciona una opcion --</option>
                  <option value="sistema">Error del sistema</option>
                  <option value="proyecto">Problema con mi proyecto</option>
                  <option value="datos">Error de datos</option>
                  <option value="otro">Otro</option>
                </select>
              </div>
              <div className="grupo-campo">
                <label htmlFor="descripcion" className="campo-label">Descripcion Detallada <span className="obligatorio">*</span></label>
                <textarea id="descripcion" className="campo-textarea" placeholder="Describe con detalle el problema encontrado, los pasos que seguiste y el resultado esperado..." required name="descripcion" maxLength="500" value={descTexto} onChange={e => setDescTexto(e.target.value)}></textarea>
                <div className="desc-contador">{descTexto.length}/500 caracteres</div>
              </div>
              <div className="grupo-campo">
                <label htmlFor="evidencia" className="campo-label">Adjuntar Evidencia (Opcional)</label>
                <div className="file-input-wrapper">
                  <button type="button" className="btn-file" onClick={() => document.getElementById('evidencia').click()}><i className="fas fa-paperclip"></i> Elegir archivo</button>
                  <span className="file-name">{archivoNombre}</span>
                  <input type="file" id="evidencia" className="file-input-real" accept=".jpg,.jpeg,.png,.pdf,.doc,.docx" name="url_evidencia" onChange={manejarArchivo} />
                </div>
                <span className="campo-info">Formatos aceptados: JPG, PNG, PDF, Word (Max. 10MB por archivo)</span>
              </div>
            </div>
            <div className="form-footer">
              <Link to="/aprendiz/dashboard" className="btn-cancelar"><i className="fas fa-times"></i> Cancelar</Link>
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
              <p className="reporte-descripcion">El sistema no permite subir archivos al crear un proyecto</p>
              <div className="reporte-card-footer">
                <span className="badge-estado-reporte badge-resuelto"><i className="fas fa-check-circle"></i> Resuelto</span>
                <span className="reporte-fecha">10 Oct 2023</span>
              </div>
            </div>

            <div className="reporte-card">
              <div className="reporte-card-header">
                <span className="reporte-id">#002</span>
                <span className="badge-tipo badge-tipo-datos">Error de datos</span>
              </div>
              <p className="reporte-descripcion">Mi perfil muestra informacion de programa desactualizada</p>
              <div className="reporte-card-footer">
                <span className="badge-estado-reporte badge-en-proceso"><i className="fas fa-spinner"></i> En proceso</span>
                <span className="reporte-fecha">25 Oct 2023</span>
              </div>
            </div>

            <div className="reporte-card">
              <div className="reporte-card-header">
                <span className="reporte-id">#003</span>
                <span className="badge-tipo badge-tipo-proyecto">Problema con mi proyecto</span>
              </div>
              <p className="reporte-descripcion">No puedo editar los entregables de mi proyecto registrado</p>
              <div className="reporte-card-footer">
                <span className="badge-estado-reporte badge-pendiente"><i className="fas fa-clock"></i> Pendiente</span>
                <span className="reporte-fecha">02 Nov 2023</span>
              </div>
            </div>

          </div>
        </div>

      </div>
    </DashboardLayout>
  );
}

export default ReportarFallaAprendiz;
