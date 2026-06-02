import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/revision-propuestas.css'

function RevisionPropuestas() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-revision">

        <div className="vista-header">
          <Link to="/instructor/dashboard" className="volver-link"><i className="fas fa-arrow-left"></i> Volver al Dashboard</Link>
          <div className="vista-titulo-row">
            <h1 className="vista-titulo">Revisión de Propuestas</h1>
            <span className="metrica-pill"><i className="fas fa-clock"></i> 3 propuestas pendientes</span>
          </div>
        </div>

        <div className="filtros-card">
          <div className="filtro-grupo">
            <label htmlFor="estado-Propuesta" className="filtro-label">Estado</label>
            <select id="estado-Propuesta" className="filtro-select" name="estado_propuesta">
              <option value="">Todos los estados</option>
              <option value="pendiente">Pendientes</option>
              <option value="aprobado">Aprobadas</option>
              <option value="rechazado">Rechazadas</option>
              <option value="observado">Con observaciones</option>
            </select>
          </div>
          <div className="filtro-grupo">
            <label htmlFor="fecha-Propuesta" className="filtro-label">Fecha</label>
            <select id="fecha-Propuesta" className="filtro-select" name="fecha_propuesta">
              <option value="">Cualquier fecha</option>
              <option value="hoy">Hoy</option>
              <option value="semana">Última semana</option>
              <option value="mes">Último mes</option>
            </select>
          </div>
          <div className="filtro-grupo">
            <label htmlFor="programa" className="filtro-label">Programa</label>
            <select id="programa" className="filtro-select" name="programa">
              <option value="">Todos los programas</option>
              <option value="adso">ADSO</option>
              <option value="sistemas">Sistemas</option>
              <option value="multimedia">Multimedia</option>
            </select>
          </div>
        </div>

        <div className="propuestas-lista">

          <div className="propuesta-card">
            <div className="propuesta-cabecera">
              <div className="cabecera-izquierda">
                <div className="aprendiz-avatar">AM</div>
                <h3>Sistema IoT para Agricultura</h3>
              </div>
              <span className="badge badge-pendiente">Pendiente</span>
            </div>
            <div className="propuesta-meta">
              <span>Ana Martínez</span>
              <span>ADSO</span>
              <span>15 Nov 2023</span>
            </div>
            <p className="propuesta-descripcion">Sistema de monitoreo inteligente para cultivos utilizando sensores IoT que miden humedad, temperatura y nutrientes del suelo.</p>
            <div className="propuesta-chips">
              <span className="tech-chip">Arduino</span>
              <span className="tech-chip">Python</span>
              <span className="tech-chip">Firebase</span>
            </div>
            <div className="alerta-similitud">
              <span className="alerta-icono">⚠️</span>
              <span className="alerta-texto">45% de similitud detectada con proyecto existente</span>
            </div>
            <div className="propuesta-acciones">
              <div className="acciones-izquierda">
                <button className="btn-primario">Aprobar</button>
                <button className="btn-rechazar">Rechazar</button>
              </div>
              <div className="acciones-derecha">
                <button className="btn-ghost">💬 Observaciones</button>
                <Link to="/instructor/detalle-proyecto" className="btn-ghost">👁️ Detalles</Link>
              </div>
            </div>
          </div>

          <div className="propuesta-card">
            <div className="propuesta-cabecera">
              <div className="cabecera-izquierda">
                <div className="aprendiz-avatar">JP</div>
                <h3>App Móvil para Turismo Local</h3>
              </div>
              <span className="badge badge-pendiente">Pendiente</span>
            </div>
            <div className="propuesta-meta">
              <span>Juan Pérez</span>
              <span>Multimedia</span>
              <span>14 Nov 2023</span>
            </div>
            <p className="propuesta-descripcion">Aplicación móvil que promueve el turismo local mostrando sitios de interés, rutas y eventos culturales.</p>
            <div className="propuesta-chips">
              <span className="tech-chip">Flutter</span>
              <span className="tech-chip">Node.js</span>
              <span className="tech-chip">MongoDB</span>
            </div>
            <div className="propuesta-acciones">
              <div className="acciones-izquierda">
                <button className="btn-primario">Aprobar</button>
                <button className="btn-rechazar">Rechazar</button>
              </div>
              <div className="acciones-derecha">
                <button className="btn-ghost">💬 Observaciones</button>
                <Link to="/instructor/detalle-proyecto" className="btn-ghost">👁️ Detalles</Link>
              </div>
            </div>
          </div>

          <div className="propuesta-card">
            <div className="propuesta-cabecera">
              <div className="cabecera-izquierda">
                <div className="aprendiz-avatar">LG</div>
                <h3>Plataforma E-learning para Música</h3>
              </div>
              <span className="badge badge-revisado">Con Observaciones</span>
            </div>
            <div className="propuesta-meta">
              <span>Laura Gómez</span>
              <span>ADSO</span>
              <span>12 Nov 2023</span>
            </div>
            <p className="propuesta-descripcion">Plataforma web para aprendizaje de instrumentos musicales con lecciones interactivas y seguimiento de progreso.</p>
            <div className="propuesta-chips">
              <span className="tech-chip">React</span>
              <span className="tech-chip">Django</span>
              <span className="tech-chip">PostgreSQL</span>
            </div>
            <div className="observacion-instructor">
              <span className="obs-icono">💬</span>
              <div>
                <strong>Observación del instructor</strong>
                <p>Se requiere definir mejor el alcance del proyecto y especificar las tecnologías para la reproducción de audio/video. Considerar integración con APIs de pago para monetización.</p>
              </div>
            </div>
            <div className="propuesta-acciones">
              <div className="acciones-izquierda">
                <button className="btn-primario">Aprobar</button>
                <button className="btn-rechazar">Rechazar</button>
              </div>
              <div className="acciones-derecha">
                <button className="btn-ghost">💬 Observaciones</button>
                <Link to="/instructor/detalle-proyecto" className="btn-ghost">👁️ Detalles</Link>
              </div>
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
    </DashboardLayout>
  )
}

export default RevisionPropuestas
