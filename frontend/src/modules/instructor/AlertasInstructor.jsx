import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarInstructor from '../../components/SidebarInstructor/SidebarInstructor';
import FooterInstructor from '../../components/FooterInstructor/FooterInstructor';
import '../../assets/styles/pages/alertas.css';

function AlertasInstructor() {
  return (
    <div className="modulo-instructor">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8} />
      <SidebarInstructor />
      <main className="contenido-principal">
        <div className="contenedor-alertas">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-bell"></i> Notificaciones <span className="contador-alertas">6</span></h1>
            <div>
              <button className="btn-secundario" type="button"><i className="fas fa-check-double"></i> Marcar todas como leídas</button>
            </div>
          </div>
          <section className="seccion-filtros">
            <h3 className="filtros-titulo"><i className="fas fa-filter"></i> Filtrar Notificaciones</h3>
            <div className="contenedor-filtros">
              <div className="grupo-filtro">
                <label htmlFor="tipo-Alerta">Tipo de Alerta</label>
                <select id="tipo-Alerta" className="select-filtro" name="tipo_alerta">
                  <option value="">Todos los tipos</option>
                  <option value="similitud">Similitud de proyectos</option>
                  <option value="revision">Comentarios y revisiones</option>
                  <option value="mensaje">Mensajes</option>
                  <option value="sistema">Notificaciones del sistema</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="estado-Alerta">Estado</label>
                <select id="estado-Alerta" className="select-filtro" name="estado_alerta">
                  <option value="">Todos los estados</option>
                  <option value="no-leida">No leídas</option>
                  <option value="leida">Leídas</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="proyecto-Alerta">Proyecto</label>
                <select id="proyecto-Alerta" className="select-filtro" name="proyecto_alerta">
                  <option value="">Todos los proyectos</option>
                  <option value="sistema-Gestión">Sistema de Gestión Académica</option>
                  <option value="app-inventarios">App Móvil para Inventarios</option>
                  <option value="iot-agricultura">Sistema IoT para Agricultura</option>
                  <option value="turismo-local">App Móvil para Turismo Local</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="fecha-Alerta">Fecha</label>
                <select id="fecha-Alerta" className="select-filtro" name="fecha_alerta">
                  <option value="">Cualquier fecha</option>
                  <option value="hoy">Hoy</option>
                  <option value="semana">Última semana</option>
                  <option value="mes">Último mes</option>
                </select>
              </div>
            </div>
          </section>
          <div className="lista-alertas">
            <div className="tarjeta-alerta alerta-urgente">
              <div className="contenido-alerta">
                <div className="icono-alerta"><i className="fas fa-exclamation-triangle"></i></div>
                <div className="info-alerta">
                  <h3>Similitud Urgente Detectada</h3>
                  <p>El proyecto "Sistema de Gestión Académica" de Juan Pérez tiene un <strong>65% de similitud</strong> con el proyecto "Plataforma Administrativa Escolar" registrado en el trimestre anterior. Se requiere revisión inmediata.</p>
                  <div className="meta-alerta">
                    <div>
                      <span className="fecha-alerta">Hace 1 hora</span>
                      <span className="proyecto-alerta">Sistema de Gestión Académica</span>
                      <span className="badge badge-peligro">Urgente</span>
                    </div>
                  </div>
                  <div className="acciones-alerta">
                    <Link to="/instructor/detalle-similitud" className="btn-primario"><i className="fas fa-search"></i> Revisar Similitud</Link>
                    <button className="btn-primario" type="button"><i className="fas fa-check"></i> Marcar como leída</button>
                  </div>
                </div>
              </div>
            </div>
            <div className="tarjeta-alerta alerta-urgente">
              <div className="contenido-alerta">
                <div className="icono-alerta"><i className="fas fa-exclamation-triangle"></i></div>
                <div className="info-alerta">
                  <h3>Alta Similitud en App Móvil</h3>
                  <p>El proyecto "App Móvil para Inventarios" de Carlos Ruiz presenta un <strong>58% de similitud</strong> con "Control de Stock Digital". Verificar originalidad de la Propuesta.</p>
                  <div className="meta-alerta">
                    <div>
                      <span className="fecha-alerta">Hace 3 horas</span>
                      <span className="proyecto-alerta">App Móvil para Inventarios</span>
                      <span className="badge badge-peligro">Urgente</span>
                    </div>
                  </div>
                  <div className="acciones-alerta">
                    <Link to="/instructor/detalle-similitud" className="btn-primario"><i className="fas fa-search"></i> Revisar Similitud</Link>
                    <button className="btn-primario" type="button"><i className="fas fa-check"></i> Marcar como leída</button>
                  </div>
                </div>
              </div>
            </div>
            <div className="tarjeta-alerta alerta-advertencia">
              <div className="contenido-alerta">
                <div className="icono-alerta"><i className="fas fa-exchange-alt"></i></div>
                <div className="info-alerta">
                  <h3>Cambio de Estado del proyecto</h3>
                  <p>El proyecto "Sistema IoT para Agricultura" de Ana Martínez fue <strong>aprobado por el instructor</strong> y pasa a estado "En desarrollo". El aprendiz ha sido notificado.</p>
                  <div className="meta-alerta">
                    <div>
                      <span className="fecha-alerta">Hace 5 horas</span>
                      <span className="proyecto-alerta">Sistema IoT para Agricultura</span>
                      <span className="badge badge-advertencia">Advertencia</span>
                    </div>
                  </div>
                  <div className="acciones-alerta">
                    <Link to="/instructor/detalle-proyecto" className="btn-primario"><i className="fas fa-eye"></i> Ver proyecto</Link>
                    <button className="btn-primario" type="button"><i className="fas fa-check"></i> Marcar como leída</button>
                  </div>
                </div>
              </div>
            </div>
            <div className="tarjeta-alerta alerta-informativa">
              <div className="contenido-alerta">
                <div className="icono-alerta"><i className="fas fa-bullhorn"></i></div>
                <div className="info-alerta">
                  <h3>Recordatorio: Revisiones Pendientes</h3>
                  <p>Tienes <strong>4 proyectos</strong> pendientes por revisar. Recuerda proporcionar retroalimentación detallada a los aprendices.</p>
                  <div className="meta-alerta">
                    <div>
                      <span className="fecha-alerta">Hace 2 días</span>
                      <span className="proyecto-alerta">Todos los proyectos</span>
                      <span className="badge badge-primario">Informativa</span>
                    </div>
                  </div>
                  <div className="acciones-alerta">
                    <Link to="/instructor/revision-propuestas" className="btn-primario"><i className="fas fa-clipboard-list"></i> Ver Revisiones</Link>
                    <button className="btn-primario" type="button"><i className="fas fa-check"></i> Marcar como leída</button>
                  </div>
                </div>
              </div>
            </div>
            <div className="tarjeta-alerta alerta-informativa">
              <div className="contenido-alerta">
                <div className="icono-alerta"><i className="fas fa-info-circle"></i></div>
                <div className="info-alerta">
                  <h3>Nuevo proyecto Registrado</h3>
                  <p><strong>Juan Pérez</strong> ha registrado un nuevo proyecto titulado "Plataforma de Gestión de Prácticas". El proyecto está en estado "Pendiente" y requiere revisión para asignación.</p>
                  <div className="meta-alerta">
                    <div>
                      <span className="fecha-alerta">Ayer</span>
                      <span className="proyecto-alerta">Plataforma de Gestión de Prácticas</span>
                      <span className="badge badge-primario">Informativa</span>
                    </div>
                  </div>
                  <div className="acciones-alerta">
                    <Link to="/instructor/revision-propuestas" className="btn-primario"><i className="fas fa-tasks"></i> Revisar Propuesta</Link>
                    <button className="btn-primario" type="button"><i className="fas fa-check"></i> Marcar como leída</button>
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
      </main>
      <FooterInstructor />
    </div>
  );
}

export default AlertasInstructor;
