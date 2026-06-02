import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/alertas.css'

function AlertasAprendiz() {
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-alertas">

        <Link to="/aprendiz/dashboard" className="volver-link"><i className="fas fa-arrow-left"></i> Volver al Dashboard</Link>

        <div className="notificaciones-header">
          <h1 className="notificaciones-titulo">Notificaciones</h1>
          <button className="btn-marcar-todas" type="button"><i className="fas fa-check-double"></i> Marcar todas como leidas</button>
        </div>

        <div className="filtros-card">
          <div className="grupo-filtro">
            <label htmlFor="tipo-alerta">Tipo de Alerta</label>
            <select id="tipo-alerta" className="filtro-select" name="tipo_alerta">
              <option value="">Todos los tipos</option>
              <option value="similitud">Similitud de proyectos</option>
              <option value="revision">Comentarios del instructor</option>
              <option value="sistema">Notificaciones del sistema</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="estado-alerta">Estado</label>
            <select id="estado-alerta" className="filtro-select" name="estado_alerta">
              <option value="">Todos los estados</option>
              <option value="no-leida">No leidas</option>
              <option value="leida">Leidas</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="proyecto-alerta">Proyecto</label>
            <select id="proyecto-alerta" className="filtro-select" name="proyecto_alerta">
              <option value="">Todos los proyectos</option>
              <option value="sistema-gestion">Sistema de Gestion Academica</option>
              <option value="app-inventarios">App Movil para Inventarios</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="fecha-alerta">Fecha</label>
            <select id="fecha-alerta" className="filtro-select" name="fecha_alerta">
              <option value="">Cualquier fecha</option>
              <option value="hoy">Hoy</option>
              <option value="semana">Ultima semana</option>
              <option value="mes">Ultimo mes</option>
            </select>
          </div>
        </div>

        <div className="lista-notificaciones">

          <div className="notificacion-card">
            <div className="notificacion-icono"><i className="fas fa-chart-line"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Alta Similitud Detectada</h3>
                <span className="badge-no-leida">No leida</span>
              </div>
              <p className="notificacion-descripcion">Tu proyecto "Sistema de Gestion Academica" tiene un 65% de similitud con otro proyecto existente en la plataforma. Se recomienda revisar y ajustar tu propuesta.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Hace 2 horas</span>
                  <span className="notificacion-proyecto">Sistema de Gestion Academica</span>
                  <span className="badge-estado badge-urgente">Urgente</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to="/aprendiz/detalle-similitud" className="btn-accion"><i className="fas fa-search"></i> Revisar Similitud</Link>
                  <button className="btn-accion-secundaria" type="button"><i className="fas fa-check"></i> Marcar como leida</button>
                </div>
              </div>
            </div>
          </div>

          <div className="notificacion-card">
            <div className="notificacion-icono"><i className="fas fa-clock"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Revision Pendiente</h3>
                <span className="badge-no-leida">No leida</span>
              </div>
              <p className="notificacion-descripcion">Tu instructor Carlos Ruiz tiene pendiente la revision de tu proyecto "App Movil para Inventarios". Recibiras notificaciones cuando haya comentarios.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Ayer</span>
                  <span className="notificacion-proyecto">App Movil para Inventarios</span>
                  <span className="badge-estado badge-pendiente">Pendiente</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to="/aprendiz/detalle-proyecto" className="btn-accion"><i className="fas fa-eye"></i> Ver proyecto</Link>
                  <button className="btn-accion-secundaria" type="button"><i className="fas fa-check"></i> Marcar como leida</button>
                </div>
              </div>
            </div>
          </div>

          <div className="notificacion-card">
            <div className="notificacion-icono"><i className="fas fa-comment"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Comentario del Instructor</h3>
                <span className="badge-leida">Leida</span>
              </div>
              <p className="notificacion-descripcion">Carlos Ruiz ha agregado comentarios a tu proyecto "Sistema de Gestion Academica". Revisa los comentarios y realiza los ajustes necesarios.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Hace 3 dias</span>
                  <span className="notificacion-proyecto">Sistema de Gestion Academica</span>
                  <span className="badge-estado badge-comentario">Comentario</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to="/aprendiz/detalle-proyecto" className="btn-accion"><i className="fas fa-eye"></i> Ver Comentarios</Link>
                  <button className="btn-accion-secundaria" type="button"><i className="fas fa-check"></i> Marcar como leida</button>
                </div>
              </div>
            </div>
          </div>

          <div className="notificacion-card">
            <div className="notificacion-icono"><i className="fas fa-check-circle"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Proyecto Aprobado</h3>
                <span className="badge-leida">Leida</span>
              </div>
              <p className="notificacion-descripcion">Felicidades! Tu proyecto "App Movil para Inventarios" ha sido aprobado por el instructor. Puedes continuar con el desarrollo.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Hace 1 semana</span>
                  <span className="notificacion-proyecto">App Movil para Inventarios</span>
                  <span className="badge-estado badge-aprobado">Aprobado</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to="/aprendiz/detalle-proyecto" className="btn-accion"><i className="fas fa-eye"></i> Ver proyecto</Link>
                  <button className="btn-accion-secundaria" type="button"><i className="fas fa-check"></i> Marcar como leida</button>
                </div>
              </div>
            </div>
          </div>

        </div>

        <div className="paginacion">
          <button className="btn-paginacion" type="button"><i className="fas fa-chevron-left"></i></button>
          <button className="btn-paginacion activa" type="button">1</button>
          <button className="btn-paginacion" type="button">2</button>
          <button className="btn-paginacion" type="button">3</button>
          <button className="btn-paginacion" type="button"><i className="fas fa-chevron-right"></i></button>
        </div>

      </div>
    </DashboardLayout>
  );
}

export default AlertasAprendiz
