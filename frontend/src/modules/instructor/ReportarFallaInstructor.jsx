import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarInstructor from '../../components/SidebarInstructor/SidebarInstructor';
import FooterInstructor from '../../components/FooterInstructor/FooterInstructor';
import '../../assets/styles/pages/reportar-falla.css';

function ReportarFallaInstructor() {
  return (
    <div className="modulo-instructor">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8} />
      <SidebarInstructor />
      <main className="contenido-principal">
        <div className="contenedor-gestion">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-bug"></i> Reportar Falla</h1>
            <p className="subtitulo-pagina">Reporta cualquier problema o Falla encontrada en el sistema para mejorar la plataforma.</p>
          </div>
          <section className="seccion-formulario-card">
            <div className="formulario-header">
              <h2 className="formulario-titulo"><i className="fas fa-exclamation-triangle"></i> Nuevo reporte de Falla</h2>
            </div>
            <div className="mensaje-feedback mensaje-exito oculto">
              <i className="fas fa-check-circle"></i>
              <span>Operación realizada exitosamente.</span>
            </div>
            <div className="mensaje-feedback mensaje-error oculto">
              <i className="fas fa-exclamation-circle"></i>
              <span>Ha ocurrido un error. Intenta nuevamente.</span>
            </div>
            <form id="formularioFalla" action="#">
              <div className="formulario-cuerpo">
                <div className="grupo-campos">
                  <div className="grupo-campo">
                    <label htmlFor="tipo-Falla">Tipo de Falla <span className="requerido">*</span></label>
                    <select id="tipo-Falla" className="select" required name="tipo">
                      <option value="" disabled selected>-- Selecciona una opción --</option>
                      <option value="sistema">Error del sistema</option>
                      <option value="proyecto">Problema con proyectos asignados</option>
                      <option value="datos">Error de datos</option>
                      <option value="otro">Otro</option>
                    </select>
                  </div>
                  <div className="grupo-campo campo-completo">
                    <label htmlFor="descripcion">Descripción Detallada <span className="requerido">*</span></label>
                    <textarea id="descripcion" className="textarea" placeholder="Describe con detalle el problema encontrado, los pasos que seguiste y el resultado esperado..." required name="descripcion"></textarea>
                  </div>
                  <div className="grupo-campo campo-completo">
                    <label htmlFor="evidencia">Adjuntar Evidencia (Opcional)</label>
                    <input type="file" id="evidencia" className="input-file" accept=".jpg,.jpeg,.png,.pdf,.doc,.docx" name="url_evidencia" />
                    <span className="campo-info">Formatos aceptados: JPG, PNG, PDF, Word (Máx. 10MB por archivo)</span>
                  </div>
                </div>
              </div>
              <div className="formulario-pie">
                <Link to="/instructor/dashboard" className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
                <button type="submit" className="btn-primario"><i className="fas fa-paper-plane"></i> Enviar reporte</button>
              </div>
            </form>
          </section>
          <section className="seccion-tabla">
            <div className="encabezado-tabla">
              <h3 className="titulo-tabla"><i className="fas fa-history"></i> Mis reportes Anteriores</h3>
              <span className="total-registros">3 reportes registrados</span>
            </div>
            <div className="contenedor-tabla">
              <table className="tabla-usuarios">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Tipo</th>
                    <th>Descripción</th>
                    <th>Estado</th>
                    <th>Fecha</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td><strong>#001</strong></td>
                    <td><span className="badge badge-peligro"><i className="fas fa-bug"></i> Error del sistema</span></td>
                    <td>El sistema no permite calificar Avances de los aprendices</td>
                    <td><span className="estado estado-resuelto"><i className="fas fa-check-circle"></i> Resuelto</span></td>
                    <td>10 Oct 2023</td>
                  </tr>
                  <tr>
                    <td><strong>#002</strong></td>
                    <td><span className="badge badge-primario"><i className="fas fa-database"></i> Error de datos</span></td>
                    <td>Los proyectos asignados no muestran correctamente la información</td>
                    <td><span className="estado estado-en-proceso"><i className="fas fa-spinner"></i> En proceso</span></td>
                    <td>25 Oct 2023</td>
                  </tr>
                  <tr>
                    <td><strong>#003</strong></td>
                    <td><span className="badge badge-neutral"><i className="fas fa-folder-open"></i> Problema con proyectos</span></td>
                    <td>No puedo asignar instructores a nuevos proyectos</td>
                    <td><span className="badge badge-advertencia"><i className="fas fa-clock"></i> Pendiente</span></td>
                    <td>02 Nov 2023</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </div>
      </main>
      <FooterInstructor />
    </div>
  );
}

export default ReportarFallaInstructor;
