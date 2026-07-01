import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/alertas.css'
import '../../assets/styles/pages/alertas-instructor.css'

const notificacionesIniciales = [
  { leida: false },
  { leida: false },
  { leida: true },
  { leida: true },
  { leida: true },
]

function AlertasInstructor() {
  const [filtroTipo, setFiltroTipo] = useState('')
  const [filtroEstado, setFiltroEstado] = useState('')
  const [filtroProyecto, setFiltroProyecto] = useState('')
  const [filtroFecha, setFiltroFecha] = useState('')
  const [leidas, setLeidas] = useState(notificacionesIniciales.map(n => n.leida))
  const [paginaActual, setPaginaActual] = useState(1)

  const marcarLeida = (i) => {
    setLeidas(prev => {
      const next = [...prev]
      next[i] = true
      return next
    })
  }

  const marcarTodasLeidas = () => {
    setLeidas(prev => prev.map(() => true))
  }

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={5}>
      <div className="contenedor-alertas">

        <PageHeader
          title="Notificaciones"
          icon="bell"
          breadcrumb={[
            { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
            { label: 'Notificaciones' }
          ]}
          actions={<><Link to="/instructor/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link><button className="btn-marcar-todas" type="button" onClick={marcarTodasLeidas}><i className="fas fa-check-double"></i> Marcar todas como leidas</button></>}
        />

        <div className="filtros-card">
          <div className="grupo-filtro">
            <label htmlFor="tipo-alerta">Tipo de Alerta</label>
            <select id="tipo-alerta" className="filtro-select" name="tipo_alerta" value={filtroTipo} onChange={(e) => { setFiltroTipo(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los tipos</option>
              <option value="similitud">Similitud de proyectos</option>
              <option value="revision">Comentarios y revisiones</option>
              <option value="sistema">Notificaciones del sistema</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="estado-alerta">Estado</label>
              <select id="estado-alerta" className="filtro-select" name="estado_alerta" value={filtroEstado} onChange={(e) => { setFiltroEstado(e.target.value); setPaginaActual(1) }}>
                <option value="">Todos los estados</option>
                <option value="false">No leídas</option>
                <option value="true">Leídas</option>
              </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="proyecto-alerta">Proyecto</label>
            <select id="proyecto-alerta" className="filtro-select" name="proyecto_alerta" value={filtroProyecto} onChange={(e) => { setFiltroProyecto(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los proyectos</option>
              <option value="sistema-gestion">Sistema de Gestion Academica</option>
              <option value="app-inventarios">App Movil para Inventarios</option>
              <option value="iot-agricultura">Sistema IoT para Agricultura</option>
              <option value="turismo-local">App Movil para Turismo Local</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="fecha-alerta">Fecha</label>
            <select id="fecha-alerta" className="filtro-select" name="fecha_alerta" value={filtroFecha} onChange={(e) => { setFiltroFecha(e.target.value); setPaginaActual(1) }}>
              <option value="">Cualquier fecha</option>
              <option value="hoy">Hoy</option>
              <option value="semana">Ultima semana</option>
              <option value="mes">Ultimo mes</option>
            </select>
          </div>
        </div>

        <div className="lista-notificaciones">

          <div className="notificacion-card">
            <div className="notificacion-icono peligro"><i className="fas fa-exclamation-triangle"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Similitud Urgente Detectada</h3>
                <span className={leidas[0] ? 'badge-leida' : 'badge-no-leida'}>{leidas[0] ? 'Leída' : 'No leída'}</span>
              </div>
              <p className="notificacion-descripcion">El proyecto "Sistema de Gestion Academica" de Maria Gonzalez tiene un 65% de similitud con el proyecto "Plataforma Educativa SENA" registrado anteriormente. Se requiere revisión inmediata.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Hace 1 hora</span>
                  <span className="notificacion-proyecto">Sistema de Gestion Academica</span>
                  <span className="badge-estado badge-similitud">Similitud</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to={{ pathname: '/instructor/detalle-similitud', state: { proyecto: 'Sistema de Gestion Academica', desde: 'alertas' } }} className="btn-accion"><i className="fas fa-search"></i> Revisar Similitud</Link>
                  <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(0)} disabled={leidas[0]}><i className="fas fa-check-double"></i> Marcar como leída</button>
                </div>
              </div>
            </div>
          </div>

          <div className="notificacion-card">
            <div className="notificacion-icono peligro"><i className="fas fa-exclamation-triangle"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Alta Similitud en App Móvil</h3>
                <span className={leidas[1] ? 'badge-leida' : 'badge-no-leida'}>{leidas[1] ? 'Leída' : 'No leída'}</span>
              </div>
              <p className="notificacion-descripcion">El proyecto "App Movil para Inventarios" de Maria Gonzalez presenta un 58% de similitud con "Control de Stock Digital". Verificar originalidad de la propuesta.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Hace 3 horas</span>
                  <span className="notificacion-proyecto">App Movil para Inventarios</span>
                  <span className="badge-estado badge-similitud">Similitud</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to={{ pathname: '/instructor/detalle-similitud', state: { proyecto: 'App Movil para Inventarios', desde: 'alertas' } }} className="btn-accion"><i className="fas fa-search"></i> Revisar Similitud</Link>
                  <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(1)} disabled={leidas[1]}><i className="fas fa-check-double"></i> Marcar como leída</button>
                </div>
              </div>
            </div>
          </div>

          <div className="notificacion-card">
            <div className="notificacion-icono advertencia"><i className="fas fa-exchange-alt"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Cambio de Estado del Proyecto</h3>
                <span className={leidas[2] ? 'badge-leida' : 'badge-no-leida'}>{leidas[2] ? 'Leída' : 'No leída'}</span>
              </div>
              <p className="notificacion-descripcion">El proyecto "Sistema IoT para Agricultura" de Ana Martínez fue aprobado por el instructor y pasa a estado "En desarrollo". El aprendiz ha sido notificado.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Hace 5 horas</span>
                  <span className="notificacion-proyecto">Sistema IoT para Agricultura</span>
                  <span className="badge-estado badge-revision">Revisión</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to={{ pathname: '/instructor/detalle-proyecto/1', state: { desde: 'alertas' } }} className="btn-accion"><i className="fas fa-eye"></i> Ver proyecto</Link>
                  <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(2)} disabled={leidas[2]}><i className="fas fa-check-double"></i> Marcar como leída</button>
                </div>
              </div>
            </div>
          </div>

          <div className="notificacion-card">
            <div className="notificacion-icono informativa"><i className="fas fa-bullhorn"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Recordatorio: Revisiones Pendientes</h3>
                <span className={leidas[3] ? 'badge-leida' : 'badge-no-leida'}>{leidas[3] ? 'Leída' : 'No leída'}</span>
              </div>
              <p className="notificacion-descripcion">Tienes 4 proyectos pendientes por revisar. Recuerda proporcionar retroalimentación detallada a los aprendices.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Hace 2 días</span>
                  <span className="notificacion-proyecto">Todos los proyectos</span>
                  <span className="badge-estado badge-sistema">Sistema</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to="/instructor/revision-propuestas" className="btn-accion"><i className="fas fa-file-alt"></i> Ver Revisiones</Link>
                  <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(3)} disabled={leidas[3]}><i className="fas fa-check-double"></i> Marcar como leída</button>
                </div>
              </div>
            </div>
          </div>

          <div className="notificacion-card">
            <div className="notificacion-icono informativa"><i className="fas fa-info-circle"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Nuevo Proyecto Registrado</h3>
                <span className={leidas[4] ? 'badge-leida' : 'badge-no-leida'}>{leidas[4] ? 'Leída' : 'No leída'}</span>
              </div>
              <p className="notificacion-descripcion">Juan Pérez ha registrado un nuevo proyecto titulado "Plataforma de Gestion de Practicas". El proyecto esta en estado "Pendiente" y requiere revisión para asignación.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Ayer</span>
                  <span className="notificacion-proyecto">Plataforma de Gestion de Practicas</span>
                  <span className="badge-estado badge-sistema">Sistema</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to="/instructor/revision-propuestas" className="btn-accion"><i className="fas fa-search"></i> Revisar Propuesta</Link>
                  <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(4)} disabled={leidas[4]}><i className="fas fa-check-double"></i> Marcar como leída</button>
                </div>
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
    </DashboardLayout>
  );
}

export default AlertasInstructor
