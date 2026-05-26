import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarInstructor from '../../components/SidebarInstructor/SidebarInstructor';
import FooterInstructor from '../../components/FooterInstructor/FooterInstructor';
import '../../assets/styles/pages/revision-propuestas.css';

function RevisionPropuestas() {
  return (
    <div className="modulo-instructor">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8} />
      <SidebarInstructor />
      <main className="contenido-principal">
        <div className="contenedor-revision">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-tasks"></i> Revisión de Propuestas</h1>
            <div className="contadores">
              <span className="contador-pendientes">8 Propuestas pendientes</span>
            </div>
          </div>
          <section className="seccion-filtros">
            <h3 className="filtros-titulo"><i className="fas fa-filter"></i> Filtrar Propuestas</h3>
            <div className="contenedor-filtros">
              <div className="grupo-filtro">
                <label htmlFor="estado-Propuesta">Estado</label>
                <select id="estado-Propuesta" className="select-filtro" name="estado_propuesta">
                  <option value="">Todos los estados</option>
                  <option value="pendiente">Pendientes</option>
                  <option value="aprobado">Aprobadas</option>
                  <option value="rechazado">Rechazadas</option>
                  <option value="observado">Con observaciones</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="fecha-Propuesta">Fecha</label>
                <select id="fecha-Propuesta" className="select-filtro" name="fecha_propuesta">
                  <option value="">Cualquier fecha</option>
                  <option value="hoy">Hoy</option>
                  <option value="semana">Última semana</option>
                  <option value="mes">Último mes</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="programa">Programa</label>
                <select id="programa" className="select-filtro" name="programa">
                  <option value="">Todos los programas</option>
                  <option value="adso">ADSO</option>
                  <option value="sistemas">Sistemas</option>
                  <option value="multimedia">Multimedia</option>
                </select>
              </div>
            </div>
          </section>
          <div className="lista-propuestas">
            <div className="tarjeta-propuesta">
              <div className="encabezado-propuesta">
                <div className="info-basica">
                  <h3>Sistema IoT para Agricultura</h3>
                  <div className="meta-info">
                    <span className="aprendiz"><i className="fas fa-user"></i> Ana Martínez</span>
                    <span className="programa"><i className="fas fa-graduation-cap"></i> ADSO</span>
                    <span className="fecha"><i className="fas fa-calendar"></i> 15 Nov 2023</span>
                  </div>
                </div>
                <span className="badge badge-advertencia">Pendiente</span>
              </div>
              <div className="contenido-propuesta">
                <p><strong>Descripción:</strong> Sistema de monitoreo inteligente para cultivos utilizando sensores IoT que miden humedad, temperatura y nutrientes del suelo.</p>
                <div className="detalles-tecnicos">
                  <div className="detalle"><strong>Tecnologías:</strong> Arduino, Python, Firebase, React Native</div>
                  <div className="detalle"><strong>Línea Tecnológica:</strong> desarrollo de Software</div>
                  <div className="detalle"><strong>Palabras clave:</strong> IoT, agricultura, sensores, monitoreo</div>
                </div>
                <div className="alerta-similitud">
                  <i className="fas fa-exclamation-triangle"></i>
                  <strong>Alerta:</strong> 45% de similitud detectada con proyecto existente
                </div>
              </div>
              <div className="acciones-propuesta">
                <button type="button" className="btn-primario"><i className="fas fa-check"></i> Aprobar</button>
                <button type="button" className="btn-secundario"><i className="fas fa-comment"></i> Agregar Observaciones</button>
                <button type="button" className="btn-ver"><i className="fas fa-times"></i> Rechazar</button>
                <Link to="/instructor/detalle-proyecto" className="btn-ver"><i className="fas fa-eye"></i> Ver Detalles</Link>
              </div>
            </div>
            <div className="tarjeta-propuesta">
              <div className="encabezado-propuesta">
                <div className="info-basica">
                  <h3>App Móvil para Turismo Local</h3>
                  <div className="meta-info">
                    <span className="aprendiz"><i className="fas fa-user"></i> Juan Pérez</span>
                    <span className="programa"><i className="fas fa-graduation-cap"></i> Multimedia</span>
                    <span className="fecha"><i className="fas fa-calendar"></i> 14 Nov 2023</span>
                  </div>
                </div>
                <span className="badge badge-advertencia">Pendiente</span>
              </div>
              <div className="contenido-propuesta">
                <p><strong>Descripción:</strong> Aplicación móvil que promueve el turismo local mostrando sitios de interés, rutas y eventos culturales.</p>
                <div className="detalles-tecnicos">
                  <div className="detalle"><strong>Tecnologías:</strong> Flutter, Node.js, MongoDB, Google Maps API</div>
                  <div className="detalle"><strong>Línea Tecnológica:</strong> desarrollo Móvil</div>
                  <div className="detalle"><strong>Palabras clave:</strong> turismo, app móvil, cultura, rutas</div>
                </div>
              </div>
              <div className="acciones-propuesta">
                <button type="button" className="btn-primario"><i className="fas fa-check"></i> Aprobar</button>
                <button type="button" className="btn-secundario"><i className="fas fa-comment"></i> Agregar Observaciones</button>
                <button type="button" className="btn-ver"><i className="fas fa-times"></i> Rechazar</button>
                <Link to="/instructor/detalle-proyecto" className="btn-ver"><i className="fas fa-eye"></i> Ver Detalles</Link>
              </div>
            </div>
            <div className="tarjeta-propuesta">
              <div className="encabezado-propuesta">
                <div className="info-basica">
                  <h3>Plataforma E-learning para Música</h3>
                  <div className="meta-info">
                    <span className="aprendiz"><i className="fas fa-user"></i> Laura Gómez</span>
                    <span className="programa"><i className="fas fa-graduation-cap"></i> ADSO</span>
                    <span className="fecha"><i className="fas fa-calendar"></i> 12 Nov 2023</span>
                  </div>
                </div>
                <span className="badge badge-primario">Con Observaciones</span>
              </div>
              <div className="contenido-propuesta">
                <p><strong>Descripción:</strong> Plataforma web para aprendizaje de instrumentos musicales con lecciones interactivas y seguimiento de progreso.</p>
                <div className="observaciones">
                  <h4><i className="fas fa-comment"></i> Observaciones del Instructor:</h4>
                  <p>Se requiere definir mejor el alcance del proyecto y especificar las tecnologías para la reproducción de audio/video. Considerar integración con APIs de pago para monetización.</p>
                </div>
              </div>
              <div className="acciones-propuesta">
                <button type="button" className="btn-primario"><i className="fas fa-check"></i> Aprobar</button>
                <button type="button" className="btn-secundario"><i className="fas fa-comment"></i> Editar Observaciones</button>
                <button type="button" className="btn-ver"><i className="fas fa-times"></i> Rechazar</button>
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

export default RevisionPropuestas;
