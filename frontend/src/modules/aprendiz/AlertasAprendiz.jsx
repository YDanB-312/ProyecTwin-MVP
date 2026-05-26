// AlertasAprendiz - Pagina de notificaciones y alertas del aprendiz con filtros y lista de alertas
import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarAprendiz from '../../components/SidebarAprendiz/SidebarAprendiz';
import FooterAprendiz from '../../components/FooterAprendiz/FooterAprendiz';
import '../../assets/styles/pages/alertas.css';

function AlertasAprendiz() {
  return (
    <div className="modulo-aprendiz">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5} />
      <SidebarAprendiz />
      <main className="contenido-principal">
        <div className="contenedor-alertas">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-bell"></i> Notificaciones <span className="contador-alertas">5</span></h1>
            <div>
              <button className="btn-secundario" type="button"><i className="fas fa-check-double"></i> Marcar todas como leidas</button>
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
                  <option value="revision">Comentarios del instructor</option>
                  <option value="mensaje">Mensajes</option>
                  <option value="sistema">Notificaciones del sistema</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="estado-Alerta">Estado</label>
                <select id="estado-Alerta" className="select-filtro" name="estado_alerta">
                  <option value="">Todos los estados</option>
                  <option value="no-leida">No leidas</option>
                  <option value="leida">Leidas</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="proyecto-Alerta">proyecto</label>
                <select id="proyecto-Alerta" className="select-filtro" name="proyecto_alerta">
                  <option value="">Todos los proyectos</option>
                  <option value="sistema-gestion">Sistema de gestion Academica</option>
                  <option value="app-inventarios">App Movil para Inventarios</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="fecha-Alerta">Fecha</label>
                <select id="fecha-Alerta" className="select-filtro" name="fecha_alerta">
                  <option value="">Cualquier fecha</option>
                  <option value="hoy">Hoy</option>
                  <option value="semana">Ultima semana</option>
                  <option value="mes">Ultimo mes</option>
                </select>
              </div>
            </div>
          </section>
          <div className="lista-alertas">
            <div className="tarjeta-alerta alerta-urgente">
              <div className="contenido-alerta">
                <div className="icono-alerta"><i className="fas fa-exclamation-triangle"></i></div>
                <div className="info-alerta">
                  <h3>Alta Similitud Detectada</h3>
                  <p>Tu proyecto "Sistema de gestion Academica" tiene un 65% de similitud con otro proyecto existente en la plataforma. Se recomienda revisar y ajustar tu Propuesta.</p>
                  <div className="meta-alerta">
                    <div>
                      <span className="fecha-alerta">Hace 2 horas</span>
                      <span className="proyecto-alerta">Sistema de gestion Academica</span>
                      <span className="badge badge-peligro">Urgente</span>
                    </div>
                  </div>
                  <div className="acciones-alerta">
                    <Link to="/aprendiz/detalle-similitud" className="btn-primario"><i className="fas fa-search"></i> Revisar Similitud</Link>
                    <button className="btn-primario" type="button"><i className="fas fa-check"></i> Marcar como leida</button>
                  </div>
                </div>
              </div>
            </div>
            <div className="tarjeta-alerta alerta-advertencia">
              <div className="contenido-alerta">
                <div className="icono-alerta"><i className="fas fa-clock"></i></div>
                <div className="info-alerta">
                  <h3>Revision Pendiente</h3>
                  <p>Tu instructor Carlos Ruiz tiene pendiente la Revision de tu proyecto "App Movil para Inventarios". Recibiras notificaciones cuando haya comentarios.</p>
                  <div className="meta-alerta">
                    <div>
                      <span className="fecha-alerta">Ayer</span>
                      <span className="proyecto-alerta">App Movil para Inventarios</span>
                      <span className="badge badge-advertencia">Pendiente</span>
                    </div>
                  </div>
                  <div className="acciones-alerta">
                    <Link to="/aprendiz/detalle-proyecto" className="btn-primario"><i className="fas fa-eye"></i> Ver proyecto</Link>
                    <button className="btn-primario" type="button"><i className="fas fa-check"></i> Marcar como leida</button>
                  </div>
                </div>
              </div>
            </div>
            <div className="tarjeta-alerta alerta-informativa">
              <div className="contenido-alerta">
                <div className="icono-alerta"><i className="fas fa-info-circle"></i></div>
                <div className="info-alerta">
                  <h3>Comentario del Instructor</h3>
                  <p>Carlos Ruiz ha agregado comentarios a tu proyecto "Sistema de gestion Academica". Revisa los comentarios y realiza los ajustes necesarios.</p>
                  <div className="meta-alerta">
                    <div>
                      <span className="fecha-alerta">Hace 3 dias</span>
                      <span className="proyecto-alerta">Sistema de gestion Academica</span>
                      <span className="badge badge-primario">Comentario</span>
                    </div>
                  </div>
                  <div className="acciones-alerta">
                    <Link to="/aprendiz/detalle-proyecto" className="btn-primario"><i className="fas fa-comments"></i> Ver Comentarios</Link>
                    <button className="btn-primario" type="button"><i className="fas fa-check"></i> Marcar como leida</button>
                  </div>
                </div>
              </div>
            </div>
            <div className="tarjeta-alerta alerta-exito">
              <div className="contenido-alerta">
                <div className="icono-alerta"><i className="fas fa-check-circle"></i></div>
                <div className="info-alerta">
                  <h3>proyecto Aprobado</h3>
                  <p>¡Felicidades! Tu proyecto "App Movil para Inventarios" ha sido aprobado por el instructor. Puedes continuar con el desarrollo.</p>
                  <div className="meta-alerta">
                    <div>
                      <span className="fecha-alerta">Hace 1 semana</span>
                      <span className="proyecto-alerta">App Movil para Inventarios</span>
                      <span className="badge badge-exito">Aprobado</span>
                    </div>
                  </div>
                  <div className="acciones-alerta">
                    <Link to="/aprendiz/detalle-proyecto" className="btn-primario"><i className="fas fa-eye"></i> Ver proyecto</Link>
                    <button className="btn-primario" type="button"><i className="fas fa-check"></i> Marcar como leida</button>
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
      <FooterAprendiz />
    </div>
  );
}

export default AlertasAprendiz;
