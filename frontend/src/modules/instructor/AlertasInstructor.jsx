import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/alertas-instructor.css'

function AlertasInstructor() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="notificaciones-instructor-container">
        <div className="contenedor-notificaciones">

          <div className="vista-header">
            <Link to="/instructor/dashboard" className="volver-link"><i className="fas fa-arrow-left"></i> Volver al Dashboard</Link>
            <div className="vista-titulo-row">
              <h1 className="vista-titulo">Notificaciones</h1>
              <span className="metrica-pill"><i className="fas fa-bell"></i> 5 notificaciones</span>
              <button className="btn-marcar-todas" type="button"><i className="fas fa-check-double"></i> Marcar todas como leídas</button>
            </div>
          </div>

          <div className="filtros-card">
            <div className="filtro-grupo">
              <label className="filtro-label" htmlFor="tipo-alerta">Tipo de Alerta</label>
              <select id="tipo-alerta" className="filtro-select" name="tipo_alerta">
                <option value="">Todos los tipos</option>
                <option value="similitud">Similitud de proyectos</option>
                <option value="revision">Comentarios y revisiones</option>
                <option value="sistema">Notificaciones del sistema</option>
              </select>
            </div>
            <div className="filtro-grupo">
              <label className="filtro-label" htmlFor="estado-alerta">Estado</label>
              <select id="estado-alerta" className="filtro-select" name="estado_alerta">
                <option value="">Todos los estados</option>
                <option value="no-leida">No leídas</option>
                <option value="leida">Leídas</option>
              </select>
            </div>
            <div className="filtro-grupo">
              <label className="filtro-label" htmlFor="proyecto-alerta">Proyecto</label>
              <select id="proyecto-alerta" className="filtro-select" name="proyecto_alerta">
                <option value="">Todos los proyectos</option>
                <option value="sistema-gestion">Sistema de Gestión Académica</option>
                <option value="app-inventarios">App Móvil para Inventarios</option>
                <option value="iot-agricultura">Sistema IoT para Agricultura</option>
                <option value="turismo-local">App Móvil para Turismo Local</option>
              </select>
            </div>
            <div className="filtro-grupo">
              <label className="filtro-label" htmlFor="fecha-alerta">Fecha</label>
              <select id="fecha-alerta" className="filtro-select" name="fecha_alerta">
                <option value="">Cualquier fecha</option>
                <option value="hoy">Hoy</option>
                <option value="semana">Última semana</option>
                <option value="mes">Último mes</option>
              </select>
            </div>
          </div>

          <div className="notificaciones-lista">

            <div className="notificacion-card">
              <div className="notif-cabecera">
                <div className="notif-titulo-grupo">
                  <span className="notif-icono-bg peligro"><i className="fas fa-exclamation-triangle"></i></span>
                  <h3>Similitud Urgente Detectada</h3>
                </div>
                <span className="badge badge-peligro">Urgente</span>
              </div>
              <hr className="propuesta-divider" />
              <p className="notif-descripcion">El proyecto "Sistema de Gestión Académica" de Juan Pérez tiene un 65% de similitud con el proyecto "Plataforma Administrativa Escolar" registrado en el trimestre anterior. Se requiere revisión inmediata.</p>
              <hr className="propuesta-divider" />
              <div className="notif-meta">
                <span className="notif-tiempo"><i className="fas fa-clock"></i> Hace 1 hora</span>
                <span className="meta-separador">·</span>
                <span className="notif-proyecto"><i className="fas fa-folder"></i> Sistema de Gestión Académica</span>
              </div>
              <div className="notif-acciones">
                <Link to="/instructor/detalle-similitud" className="btn-primario"><i className="fas fa-search"></i> Revisar Similitud</Link>
                <button className="btn-ghost" type="button"><i className="fas fa-check-double"></i> Marcar como leída</button>
              </div>
            </div>

            <div className="notificacion-card">
              <div className="notif-cabecera">
                <div className="notif-titulo-grupo">
                  <span className="notif-icono-bg peligro"><i className="fas fa-exclamation-triangle"></i></span>
                  <h3>Alta Similitud en App Móvil</h3>
                </div>
                <span className="badge badge-peligro">Urgente</span>
              </div>
              <hr className="propuesta-divider" />
              <p className="notif-descripcion">El proyecto "App Móvil para Inventarios" de Carlos Ruiz presenta un 58% de similitud con "Control de Stock Digital". Verificar originalidad de la propuesta.</p>
              <hr className="propuesta-divider" />
              <div className="notif-meta">
                <span className="notif-tiempo"><i className="fas fa-clock"></i> Hace 3 horas</span>
                <span className="meta-separador">·</span>
                <span className="notif-proyecto"><i className="fas fa-folder"></i> App Móvil para Inventarios</span>
              </div>
              <div className="notif-acciones">
                <Link to="/instructor/detalle-similitud" className="btn-primario"><i className="fas fa-search"></i> Revisar Similitud</Link>
                <button className="btn-ghost" type="button"><i className="fas fa-check-double"></i> Marcar como leída</button>
              </div>
            </div>

            <div className="notificacion-card">
              <div className="notif-cabecera">
                <div className="notif-titulo-grupo">
                  <span className="notif-icono-bg advertencia"><i className="fas fa-exchange-alt"></i></span>
                  <h3>Cambio de Estado del Proyecto</h3>
                </div>
                <span className="badge badge-advertencia">Advertencia</span>
              </div>
              <hr className="propuesta-divider" />
              <p className="notif-descripcion">El proyecto "Sistema IoT para Agricultura" de Ana Martínez fue aprobado por el instructor y pasa a estado "En desarrollo". El aprendiz ha sido notificado.</p>
              <hr className="propuesta-divider" />
              <div className="notif-meta">
                <span className="notif-tiempo"><i className="fas fa-clock"></i> Hace 5 horas</span>
                <span className="meta-separador">·</span>
                <span className="notif-proyecto"><i className="fas fa-folder"></i> Sistema IoT para Agricultura</span>
              </div>
              <div className="notif-acciones">
                <Link to="/instructor/detalle-proyecto" className="btn-primario"><i className="fas fa-eye"></i> Ver proyecto</Link>
                <button className="btn-ghost" type="button"><i className="fas fa-check-double"></i> Marcar como leída</button>
              </div>
            </div>

            <div className="notificacion-card">
              <div className="notif-cabecera">
                <div className="notif-titulo-grupo">
                  <span className="notif-icono-bg informativa"><i className="fas fa-bullhorn"></i></span>
                  <h3>Recordatorio: Revisiones Pendientes</h3>
                </div>
                <span className="badge badge-primario">Informativa</span>
              </div>
              <hr className="propuesta-divider" />
              <p className="notif-descripcion">Tienes 4 proyectos pendientes por revisar. Recuerda proporcionar retroalimentación detallada a los aprendices.</p>
              <hr className="propuesta-divider" />
              <div className="notif-meta">
                <span className="notif-tiempo"><i className="fas fa-clock"></i> Hace 2 días</span>
                <span className="meta-separador">·</span>
                <span className="notif-proyecto"><i className="fas fa-folder"></i> Todos los proyectos</span>
              </div>
              <div className="notif-acciones">
                <Link to="/instructor/revision-propuestas" className="btn-primario"><i className="fas fa-file-alt"></i> Ver Revisiones</Link>
                <button className="btn-ghost" type="button"><i className="fas fa-check-double"></i> Marcar como leída</button>
              </div>
            </div>

            <div className="notificacion-card">
              <div className="notif-cabecera">
                <div className="notif-titulo-grupo">
                  <span className="notif-icono-bg informativa"><i className="fas fa-info-circle"></i></span>
                  <h3>Nuevo Proyecto Registrado</h3>
                </div>
                <span className="badge badge-primario">Informativa</span>
              </div>
              <hr className="propuesta-divider" />
              <p className="notif-descripcion">Juan Pérez ha registrado un nuevo proyecto titulado "Plataforma de Gestión de Prácticas". El proyecto está en estado "Pendiente" y requiere revisión para asignación.</p>
              <hr className="propuesta-divider" />
              <div className="notif-meta">
                <span className="notif-tiempo"><i className="fas fa-clock"></i> Ayer</span>
                <span className="meta-separador">·</span>
                <span className="notif-proyecto"><i className="fas fa-folder"></i> Plataforma de Gestión de Prácticas</span>
              </div>
              <div className="notif-acciones">
                <Link to="/instructor/revision-propuestas" className="btn-primario"><i className="fas fa-search"></i> Revisar Propuesta</Link>
                <button className="btn-ghost" type="button"><i className="fas fa-check-double"></i> Marcar como leída</button>
              </div>
            </div>

          </div>

          <div className="paginacion">
            <button className="btn-paginacion" type="button"><i className="fas fa-chevron-left"></i></button>
            <button className="btn-paginacion activo" type="button">1</button>
            <button className="btn-paginacion" type="button">2</button>
            <button className="btn-paginacion" type="button">3</button>
            <button className="btn-paginacion" type="button"><i className="fas fa-chevron-right"></i></button>
          </div>

        </div>
      </div>
    </DashboardLayout>
  );
}

export default AlertasInstructor;
