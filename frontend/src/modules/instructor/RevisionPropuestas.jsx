import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/revision-propuestas.css'
import Pagination from '../../components/Pagination/Pagination'
import FilterBar from '../../components/FilterBar/FilterBar'

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Revisión de Propuestas' },
]

const ITEMS_PER_PAGE = 3

const propuestasData = [
  { id: 1, titulo: 'Sistema IoT para Agricultura', aprendiz: 'Ana Martínez', iniciales: 'AM', programa: 'ADSO', estado: 'pendiente', fecha: '15 nov 2026', techs: ['Arduino', 'Python', 'Firebase'], desc: 'Sistema de monitoreo inteligente para cultivos de hortalizas utilizando sensores IoT conectados a una plataforma cloud. El sistema permite medir humedad, temperatura y pH del suelo en tiempo real, enviando alertas al celular del agricultor cuando los parámetros salen del rango óptimo.', similitud: 45 },
  { id: 2, titulo: 'App Móvil para Turismo Local', aprendiz: 'Juan Pérez', iniciales: 'JP', programa: 'Multimedia', estado: 'pendiente', fecha: '14 nov 2026', techs: ['React Native', 'Node.js', 'MongoDB'], desc: 'Aplicación móvil para promover el turismo local en municipios cercanos, con rutas culturales, gastronómicas y de naturaleza. Incluye mapas interactivos, reseñas de usuarios y notificaciones de eventos cercanos.', similitud: 0 },
  { id: 3, titulo: 'Plataforma E-learning para Música', aprendiz: 'Laura Gómez', iniciales: 'LG', programa: 'ADSO', estado: 'requiere_ajustes', fecha: '12 nov 2026', techs: ['React', 'Django', 'PostgreSQL'], desc: 'Plataforma educativa interactiva para el aprendizaje de teoría musical y entrenamiento auditivo. Incluye lecciones gamificadas, ejercicios de reconocimiento de intervalos y un simulador de instrumentos virtuales.', similitud: 0 },
]

export default function RevisionPropuestas() {
  const [filtroEstado, setFiltroEstado] = useState('')
  const [filtroFecha, setFiltroFecha] = useState('')
  const [filtroPrograma, setFiltroPrograma] = useState('')
  const [paginaActual, setPaginaActual] = useState(1)
  const [mensaje, setMensaje] = useState(null)
  const [propuestasEstado, setPropuestasEstado] = useState({})
  const [obsModal, setObsModal] = useState(null)
  const [obsTexto, setObsTexto] = useState('')

  const propuestasFiltradas = propuestasData.filter(p => {
    const estadoActual = propuestasEstado[p.id] || p.estado
    if (filtroEstado && estadoActual !== filtroEstado) return false
    if (filtroPrograma && p.programa.toLowerCase() !== filtroPrograma) return false
    return true
  })



  const propuestasPagina = propuestasFiltradas.slice((paginaActual - 1) * ITEMS_PER_PAGE, paginaActual * ITEMS_PER_PAGE)

  const accionPropuesta = (id, accion) => {
    setPropuestasEstado(prev => ({ ...prev, [id]: accion }))
    const textos = { aprobar: 'Propuesta aprobada exitosamente', rechazar: 'Propuesta rechazada', observaciones: 'Observaciones registradas' }
    setMensaje({ tipo: 'exito', texto: textos[accion] })
    setTimeout(() => setMensaje(null), 3000)
  }

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
        <div className="revision-propuestas-container fade-in">
        <div className="contenedor-revision">

          <PageHeader
            title="Revisión de Propuestas"
            icon="tasks"
            breadcrumb={breadcrumb}
            actions={
              <div className="metrica-pill">
                <i className="fas fa-clock"></i> {3 - Object.keys(propuestasEstado).length} propuestas pendientes
              </div>
            }
          />

          <div className={`mensaje-feedback mensaje-exito ${mensaje && mensaje.tipo === 'exito' ? '' : 'oculto'} mb-md`}>
            <i className="fas fa-check-circle"></i><span>{mensaje?.texto || ''}</span>
          </div>

          <FilterBar title="Filtrar Propuestas">
            <div className="grupo-filtro">
              <label htmlFor="estado-Propuesta" className="filtro-label">Estado</label>
              <select id="estado-Propuesta" className="campo-select" name="estado_propuesta" value={filtroEstado} onChange={(e) => { setFiltroEstado(e.target.value); setPaginaActual(1) }}>
                <option value="">Todos los estados</option>
                <option value="pendiente">Pendientes</option>
                <option value="en_revision">En revisión</option>
                <option value="aprobado">Aprobadas</option>
                <option value="rechazado">Rechazadas</option>
                <option value="requiere_ajustes">Requiere ajustes</option>
              </select>
            </div>
            <div className="grupo-filtro">
              <label htmlFor="fecha-Propuesta" className="filtro-label">Fecha</label>
              <select id="fecha-Propuesta" className="campo-select" name="fecha_propuesta" value={filtroFecha} onChange={(e) => { setFiltroFecha(e.target.value); setPaginaActual(1) }}>
                <option value="">Cualquier fecha</option>
                <option value="hoy">Hoy</option>
                <option value="semana">Última semana</option>
                <option value="mes">Último mes</option>
              </select>
            </div>
            <div className="grupo-filtro">
              <label htmlFor="programa" className="filtro-label">Programa</label>
              <select id="programa" className="campo-select" name="programa" value={filtroPrograma} onChange={(e) => { setFiltroPrograma(e.target.value); setPaginaActual(1) }}>
                <option value="">Todos los programas</option>
                <option value="adso">ADSO</option>
                <option value="sistemas">Sistemas</option>
                <option value="multimedia">Multimedia</option>
              </select>
            </div>
          </FilterBar>

          <div className="propuestas-lista">

            {propuestasPagina.length === 0 ? (
              <div className="estado-vacio-moderno"><div className="estado-vacio-icono"><i className="fas fa-clipboard-list"></i></div><h3 className="estado-vacio-titulo">No hay propuestas</h3><p className="estado-vacio-descripcion">No se encontraron propuestas que coincidan con los filtros.</p></div>
            ) : propuestasPagina.map((p) => (
            <div className="propuesta-card" key={p.id}>
              <div className="propuesta-cabecera">
                <div className="cabecera-izquierda">
                  <span className="aprendiz-avatar">{p.iniciales}</span>
                  <h3 className="propuesta-titulo">{p.titulo}</h3>
                </div>
                <span className={`badge ${propuestasEstado[p.id] === 'aprobar' ? 'badge-exito' : propuestasEstado[p.id] === 'rechazar' ? 'badge-peligro' : 'badge-pendiente'}`}>{propuestasEstado[p.id] === 'aprobar' ? 'Aprobada' : propuestasEstado[p.id] === 'rechazar' ? 'Rechazada' : 'Pendiente'}</span>
              </div>

              <div className="propuesta-fila-tecnica">
                <div className="propuesta-meta">
                  <span><i className="fas fa-user"></i> {p.aprendiz}</span>
                  <span className="meta-separador">·</span>
                  <span><i className="fas fa-graduation-cap"></i> {p.programa}</span>
                  <span className="meta-separador">·</span>
                  <span><i className="fas fa-calendar"></i> {p.fecha}</span>
                </div>
                <div className="propuesta-chips">
                  {p.techs.map((t, i) => <span key={i} className="tech-chip">{t}</span>)}
                </div>
              </div>

              <p className="propuesta-descripcion">{p.desc}</p>

              {p.similitud > 0 && (
                <div className="alerta-similitud">
                  <i className="fas fa-exclamation-triangle"></i>
                  <span>{p.similitud}% de similitud detectada con proyecto existente</span>
                </div>
              )}

              <div className="propuesta-acciones">
                <div className="acciones-izquierda">
                  <button className={`btn-primario ${propuestasEstado[p.id] === 'aprobar' ? 'deshabilitado' : ''}`} type="button" disabled={propuestasEstado[p.id] === 'aprobar' || propuestasEstado[p.id] === 'rechazar'} onClick={() => accionPropuesta(p.id, 'aprobar')}><i className="fas fa-check"></i> Aprobar</button>
                  <button className={`btn-rechazar ${propuestasEstado[p.id] === 'rechazar' ? 'deshabilitado' : ''}`} type="button" disabled={propuestasEstado[p.id] === 'aprobar' || propuestasEstado[p.id] === 'rechazar'} onClick={() => accionPropuesta(p.id, 'rechazar')}><i className="fas fa-times"></i> Rechazar</button>
                </div>
                <div className="acciones-derecha">
                  <button className="btn-ghost" type="button" onClick={() => { setObsModal(p.id); setObsTexto('') }}><i className="fas fa-comment"></i> Observaciones</button>
                  <Link to={`/instructor/detalle-proyecto/${p.id}`} className="btn-ghost"><i className="fas fa-eye"></i> Detalles</Link>
                </div>
              </div>
            </div>
            ))}

          </div>

          <Pagination totalItems={propuestasFiltradas.length} itemsPerPage={ITEMS_PER_PAGE} paginaActual={paginaActual} setPaginaActual={setPaginaActual} />

        </div>

      </div>

      {obsModal && (
        <div className="modal-overlay" onClick={() => setObsModal(null)}>
          <div className="modal-contenido" onClick={(e) => e.stopPropagation()}>
            <h3><i className="fas fa-comment"></i> Observaciones</h3>
            <textarea className="campo-textarea" rows="5" placeholder="Escribe tus observaciones..." value={obsTexto} onChange={(e) => setObsTexto(e.target.value)} />
            <div className="modal-acciones">
              <button className="btn-secundario" type="button" onClick={() => setObsModal(null)}>Cancelar</button>
              <button className="btn-primario" type="button" onClick={() => { accionPropuesta(obsModal, 'observaciones'); setObsModal(null) }}>Guardar</button>
            </div>
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}


