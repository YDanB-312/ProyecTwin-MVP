import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarInstructor from '../../components/SidebarInstructor/SidebarInstructor';
import FooterInstructor from '../../components/FooterInstructor/FooterInstructor';
import '../../assets/styles/pages/detalle-compartido.css';
import '../../assets/styles/pages/mis-proyectos.css';

function DetalleProyectoInstructor() {
  return (
    <div className="modulo-instructor">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8} />
      <SidebarInstructor />
      <main className="contenido-principal">
        <div className="contenedor-revision">
          <div className="breadcrumb">
            <Link to="/instructor/dashboard"><i className="fas fa-home"></i> Inicio</Link>
            <span className="separador"><i className="fas fa-chevron-right"></i></span>
            <span className="actual">Detalle del Proyecto</span>
          </div>
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-folder-open"></i> Detalle del Proyecto</h1>
            <Link to="/instructor/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>
          <div className="seccion-filtros">
            <div className="filtros-titulo"><i className="fas fa-info-circle"></i> Información General</div>
            <div className="detalle-grid">
              <div>
                <label className="campo-label">Nombre del Proyecto</label>
                <p className="campo-valor">Sistema IoT para Agricultura de Precisión</p>
              </div>
              <div>
                <label className="campo-label">Estado</label>
                <p><span className="badge badge-exito">Aprobado</span> <span className="badge badge-primario"><i className="fas fa-robot"></i> Analizado por IA</span></p>
              </div>
              <div>
                <label className="campo-label">Aprendiz</label>
                <p className="campo-valor">Ana Martínez</p>
              </div>
              <div>
                <label className="campo-label">Programa de Formación</label>
                <p className="campo-valor">ADSO - Análisis y Desarrollo de Sistemas</p>
              </div>
              <div>
                <label className="campo-label">Fecha de Creación</label>
                <p className="campo-valor">15/03/2026</p>
              </div>
              <div>
                <label className="campo-label">Instructor Asignado</label>
                <p className="campo-valor">Carlos Ruiz</p>
              </div>
              <div className="detalle-full">
                <label className="campo-label">Descripción</label>
                <p className="texto-descripcion">Desarrollo de un sistema basado en sensores IoT para monitorear variables ambientales en cultivos, permitiendo la toma de decisiones en tiempo real para optimizar el riego y la fertilización.</p>
              </div>
            </div>
          </div>
          <div className="tarjeta tarjeta-padded mb-30">
            <h2 className="titulo-seccion"><i className="fas fa-users"></i> Integrantes del Equipo</h2>
            <div className="flex-row flex-wrap">
              <div className="avatar-miembro avatar-sm">AM</div>
              <div><strong className="texto-md">Ana Martínez</strong><br /><span className="texto-claro">Creador / Líder</span></div>
              <div className="avatar-miembro avatar-sm avatar-secundario">JP</div>
              <div><strong className="texto-md">Juan Pérez</strong><br /><span className="texto-claro">Integrante</span></div>
              <div className="avatar-miembro avatar-sm avatar-secundario">LG</div>
              <div><strong className="texto-md">Laura Gómez</strong><br /><span className="texto-claro">Integrante</span></div>
            </div>
          </div>
          <div className="seccion-reporte">
            <h2 className="titulo-seccion"><i className="fas fa-comments"></i> Observaciones del Proyecto</h2>
            <div className="lista-observaciones">
              <div className="observacion-item">
                <div className="observacion-header">
                  <span className="observacion-autor"><i className="fas fa-user-tie"></i> Carlos Ruiz | Instructor</span>
                  <span className="observacion-fecha">10 may 2026</span>
                  <div className="observacion-acciones">
                    <button type="button" className="btn-icono editar" title="Editar observación"><i className="fas fa-edit"></i></button>
                    <button type="button" className="btn-icono eliminar" title="Eliminar observación"><i className="fas fa-trash-alt"></i></button>
                  </div>
                </div>
                <div className="observacion-contenido">
                  <p>El proyecto necesita mejorar la sección de análisis de requisitos. Se recomienda ampliar la documentación técnica antes de continuar con el desarrollo.</p>
                </div>
                <div className="observacion-edit-form oculto">
                  <form action="#">
                    <textarea className="textarea" name="contenido">El proyecto necesita mejorar la sección de análisis de requisitos. Se recomienda ampliar la documentación técnica antes de continuar con el desarrollo.</textarea>
                    <div className="acciones-formulario acciones-edit">
                      <button type="submit" className="btn-primario btn-sm"><i className="fas fa-save"></i> Guardar Cambios</button>
                      <button type="button" className="btn-secundario btn-sm cancelar-edit"><i className="fas fa-times"></i> Cancelar</button>
                    </div>
                  </form>
                </div>
              </div>
              <div className="observacion-item">
                <div className="observacion-header">
                  <span className="observacion-autor"><i className="fas fa-user-graduate"></i> María González | Aprendiz</span>
                  <span className="observacion-fecha">8 may 2026</span>
                  <div className="observacion-acciones">
                    <button type="button" className="btn-icono editar" title="Editar observación"><i className="fas fa-edit"></i></button>
                    <button type="button" className="btn-icono eliminar" title="Eliminar observación"><i className="fas fa-trash-alt"></i></button>
                  </div>
                </div>
                <div className="observacion-contenido">
                  <p>He realizado los ajustes sugeridos en la documentación. La nueva versión incluye diagramas de flujo y casos de uso detallados. Quedo atento a más retroalimentación.</p>
                </div>
                <div className="observacion-edit-form oculto">
                  <form action="#">
                    <textarea className="textarea" name="contenido">He realizado los ajustes sugeridos en la documentación. La nueva versión incluye diagramas de flujo y casos de uso detallados. Quedo atento a más retroalimentación.</textarea>
                    <div className="acciones-formulario acciones-edit">
                      <button type="submit" className="btn-primario btn-sm"><i className="fas fa-save"></i> Guardar Cambios</button>
                      <button type="button" className="btn-secundario btn-sm cancelar-edit"><i className="fas fa-times"></i> Cancelar</button>
                    </div>
                  </form>
                </div>
              </div>
              <div className="observacion-item">
                <div className="observacion-header">
                  <span className="observacion-autor"><i className="fas fa-user-tie"></i> Carlos Ruiz | Instructor</span>
                  <span className="observacion-fecha">6 may 2026</span>
                  <div className="observacion-acciones">
                    <button type="button" className="btn-icono editar" title="Editar observación"><i className="fas fa-edit"></i></button>
                    <button type="button" className="btn-icono eliminar" title="Eliminar observación"><i className="fas fa-trash-alt"></i></button>
                  </div>
                </div>
                <div className="observacion-contenido">
                  <p>La propuesta inicial tiene buen enfoque, pero falta definir mejor los entregables del primer sprint. Recomiendo revisar la guía de proyectos para alinear expectativas.</p>
                </div>
                <div className="observacion-edit-form oculto">
                  <form action="#">
                    <textarea className="textarea" name="contenido">La propuesta inicial tiene buen enfoque, pero falta definir mejor los entregables del primer sprint. Recomiendo revisar la guía de proyectos para alinear expectativas.</textarea>
                    <div className="acciones-formulario acciones-edit">
                      <button type="submit" className="btn-primario btn-sm"><i className="fas fa-save"></i> Guardar Cambios</button>
                      <button type="button" className="btn-secundario btn-sm cancelar-edit"><i className="fas fa-times"></i> Cancelar</button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
            <div className="tarjeta tarjeta-padded mb-30">
              <h3><i className="fas fa-plus-circle"></i> Agregar Observación</h3>
              <form action="#">
                <div className="grupo-formulario">
                  <label htmlFor="observacion" className="etiqueta">Comentario</label>
                  <textarea id="observacion" className="textarea" placeholder="Escribe tu observación sobre el proyecto..." name="contenido"></textarea>
                </div>
                <div className="acciones-formulario">
                  <button type="submit" className="btn-primario"><i className="fas fa-paper-plane"></i> Guardar Observación</button>
                </div>
              </form>
            </div>
          </div>
          <div className="acciones-finales">
            <Link to="/instructor/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>
        </div>
      </main>
      <FooterInstructor />
    </div>
  );
}

export default DetalleProyectoInstructor;
