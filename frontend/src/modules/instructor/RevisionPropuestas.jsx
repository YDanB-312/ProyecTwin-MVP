import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/revision-propuestas.css'

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Revisión de Propuestas' },
]

function RevisionPropuestas() {
  const [filtroEstado, setFiltroEstado] = useState('')
  const [filtroFecha, setFiltroFecha] = useState('')
  const [filtroPrograma, setFiltroPrograma] = useState('')
  const [paginaActual, setPaginaActual] = useState(1)

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="revision-propuestas-container">
        <div className="contenedor-revision">

          <PageHeader
            title="Revisión de Propuestas"
            icon="tasks"
            breadcrumb={breadcrumb}
            actions={
              <div className="metrica-pill">
                <i className="fas fa-clock"></i> 3 propuestas pendientes
              </div>
            }
          />

          <div className="filtros-card">
            <div className="filtro-grupo">
              <label htmlFor="estado-Propuesta" className="filtro-label">Estado</label>
              <select id="estado-Propuesta" className="filtro-select" name="estado_propuesta" value={filtroEstado} onChange={(e) => { setFiltroEstado(e.target.value); setPaginaActual(1) }}>
                <option value="">Todos los estados</option>
                <option value="pendiente">Pendientes</option>
                <option value="en_revision">En revisión</option>
                <option value="aprobado">Aprobadas</option>
                <option value="rechazado">Rechazadas</option>
                <option value="requiere_ajustes">Requiere ajustes</option>
              </select>
            </div>
            <div className="filtro-grupo">
              <label htmlFor="fecha-Propuesta" className="filtro-label">Fecha</label>
              <select id="fecha-Propuesta" className="filtro-select" name="fecha_propuesta" value={filtroFecha} onChange={(e) => { setFiltroFecha(e.target.value); setPaginaActual(1) }}>
                <option value="">Cualquier fecha</option>
                <option value="hoy">Hoy</option>
                <option value="semana">Última semana</option>
                <option value="mes">Último mes</option>
              </select>
            </div>
            <div className="filtro-grupo">
              <label htmlFor="programa" className="filtro-label">Programa</label>
              <select id="programa" className="filtro-select" name="programa" value={filtroPrograma} onChange={(e) => { setFiltroPrograma(e.target.value); setPaginaActual(1) }}>
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
                  <span className="aprendiz-avatar">AM</span>
                  <h3 className="propuesta-titulo">Sistema IoT para Agricultura</h3>
                </div>
                <span className="badge badge-pendiente">Pendiente</span>
              </div>

              <div className="propuesta-fila-tecnica">
                <div className="propuesta-meta">
                  <span><i className="fas fa-user"></i> Ana Martínez</span>
                  <span className="meta-separador">·</span>
                  <span><i className="fas fa-graduation-cap"></i> ADSO</span>
                  <span className="meta-separador">·</span>
                  <span><i className="fas fa-calendar"></i> 15 nov 2026</span>
                </div>
                <div className="propuesta-chips">
                  <span className="tech-chip">Arduino</span>
                  <span className="tech-chip">Python</span>
                  <span className="tech-chip">Firebase</span>
                </div>
              </div>

              <p className="propuesta-descripcion">
                Sistema de monitoreo inteligente para cultivos de hortalizas utilizando sensores IoT conectados a una plataforma cloud. El sistema permite medir humedad, temperatura y pH del suelo en tiempo real, enviando alertas al celular del agricultor cuando los parámetros salen del rango óptimo.
              </p>

              <div className="alerta-similitud">
                <i className="fas fa-exclamation-triangle"></i>
                <span>45% de similitud detectada con proyecto existente</span>
              </div>

              <div className="propuesta-acciones">
                <div className="acciones-izquierda">
                  <button className="btn-primario" type="button" onClick={() => {}}><i className="fas fa-check"></i> Aprobar</button>
                  <button className="btn-rechazar" type="button" onClick={() => {}}><i className="fas fa-times"></i> Rechazar</button>
                </div>
                <div className="acciones-derecha">
                  <button className="btn-ghost" type="button" onClick={() => {}}><i className="fas fa-comment"></i> Observaciones</button>
                  <Link to="/instructor/detalle-proyecto/1" className="btn-ghost"><i className="fas fa-eye"></i> Detalles</Link>
                </div>
              </div>
            </div>

            <div className="propuesta-card">
              <div className="propuesta-cabecera">
                <div className="cabecera-izquierda">
                  <span className="aprendiz-avatar">JP</span>
                  <h3 className="propuesta-titulo">App Móvil para Turismo Local</h3>
                </div>
                <span className="badge badge-pendiente">Pendiente</span>
              </div>

              <div className="propuesta-fila-tecnica">
                <div className="propuesta-meta">
                  <span><i className="fas fa-user"></i> Juan Pérez</span>
                  <span className="meta-separador">·</span>
                  <span><i className="fas fa-graduation-cap"></i> Multimedia</span>
                  <span className="meta-separador">·</span>
                  <span><i className="fas fa-calendar"></i> 14 nov 2026</span>
                </div>
                <div className="propuesta-chips">
                  <span className="tech-chip">React Native</span>
                  <span className="tech-chip">Node.js</span>
                  <span className="tech-chip">MongoDB</span>
                </div>
              </div>

              <p className="propuesta-descripcion">
                Aplicación móvil para promover el turismo local en municipios cercanos, con rutas culturales, gastronómicas y de naturaleza. Incluye mapas interactivos, reseñas de usuarios y notificaciones de eventos cercanos.
              </p>

              <div className="propuesta-acciones">
                <div className="acciones-izquierda">
                  <button className="btn-primario" type="button" onClick={() => {}}><i className="fas fa-check"></i> Aprobar</button>
                  <button className="btn-rechazar" type="button" onClick={() => {}}><i className="fas fa-times"></i> Rechazar</button>
                </div>
                <div className="acciones-derecha">
                  <button className="btn-ghost" type="button" onClick={() => {}}><i className="fas fa-comment"></i> Observaciones</button>
                  <Link to="/instructor/detalle-proyecto/2" className="btn-ghost"><i className="fas fa-eye"></i> Detalles</Link>
                </div>
              </div>
            </div>

            <div className="propuesta-card">
              <div className="propuesta-cabecera">
                <div className="cabecera-izquierda">
                  <span className="aprendiz-avatar">LG</span>
                  <h3 className="propuesta-titulo">Plataforma E-learning para Música</h3>
                </div>
                <span className="badge badge-requiere-ajustes">Requiere Ajustes</span>
              </div>

              <div className="propuesta-fila-tecnica">
                <div className="propuesta-meta">
                  <span><i className="fas fa-user"></i> Laura Gómez</span>
                  <span className="meta-separador">·</span>
                  <span><i className="fas fa-graduation-cap"></i> ADSO</span>
                  <span className="meta-separador">·</span>
                  <span><i className="fas fa-calendar"></i> 12 nov 2026</span>
                </div>
                <div className="propuesta-chips">
                  <span className="tech-chip">React</span>
                  <span className="tech-chip">Django</span>
                  <span className="tech-chip">PostgreSQL</span>
                </div>
              </div>

              <p className="propuesta-descripcion">
                Plataforma educativa interactiva para el aprendizaje de teoría musical y entrenamiento auditivo. Incluye lecciones gamificadas, ejercicios de reconocimiento de intervalos y un simulador de instrumentos virtuales.
              </p>

              <div className="propuesta-acciones">
                <div className="acciones-izquierda">
                  <button className="btn-primario" type="button" onClick={() => {}}><i className="fas fa-check"></i> Aprobar</button>
                  <button className="btn-rechazar" type="button" onClick={() => {}}><i className="fas fa-times"></i> Rechazar</button>
                </div>
                <div className="acciones-derecha">
                  <button className="btn-ghost" type="button" onClick={() => {}}><i className="fas fa-comment"></i> Observaciones</button>
                  <Link to="/instructor/detalle-proyecto/3" className="btn-ghost"><i className="fas fa-eye"></i> Detalles</Link>
                </div>
              </div>
            </div>

          </div>

          <div className="paginacion">
            <button className="btn-paginacion" disabled={paginaActual === 1} type="button" onClick={() => setPaginaActual(prev => Math.max(1, prev - 1))}><i className="fas fa-chevron-left"></i></button>
            <button className={`btn-paginacion${paginaActual === 1 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(1)}>1</button>
            <button className={`btn-paginacion${paginaActual === 2 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(2)}>2</button>
            <button className={`btn-paginacion${paginaActual === 3 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(3)}>3</button>
            <button className="btn-paginacion" type="button" onClick={() => setPaginaActual(prev => Math.min(3, prev + 1))}><i className="fas fa-chevron-right"></i></button>
          </div>

        </div>

      </div>
    </DashboardLayout>
  )
}

export default RevisionPropuestas
