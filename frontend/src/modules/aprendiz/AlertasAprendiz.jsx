import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/alertas.css'

const notificacionesIniciales = [
  { leida: false },
  { leida: false },
  { leida: true },
  { leida: true },
]

function AlertasAprendiz() {
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
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={4}>
      <div className="contenedor-alertas">

        <PageHeader
          title="Notificaciones"
          icon="bell"
          breadcrumb={[
            { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
            { label: 'Notificaciones' }
          ]}
          actions={<><Link to="/aprendiz/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link><button className="btn-marcar-todas" type="button" onClick={marcarTodasLeidas}><i className="fas fa-check-double"></i> Marcar todas como leidas</button></>}
        />

        <div className="filtros-card">
          <div className="grupo-filtro">
            <label htmlFor="tipo-alerta">Tipo de Alerta</label>
            <select id="tipo-alerta" className="filtro-select" name="tipo_alerta" value={filtroTipo} onChange={(e) => { setFiltroTipo(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los tipos</option>
              <option value="similitud">Similitud de proyectos</option>
              <option value="revision">Comentarios del instructor</option>
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
            <div className="notificacion-icono"><i className="fas fa-chart-line"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Alta Similitud Detectada</h3>
                <span className={leidas[0] ? 'badge-leida' : 'badge-no-leida'}>{leidas[0] ? 'Leída' : 'No leída'}</span>
              </div>
              <p className="notificacion-descripcion">Tu proyecto "Sistema de Gestion Academica" tiene un 65% de similitud con otro proyecto existente en la plataforma. Se recomienda revisar y ajustar tu propuesta.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Hace 2 horas</span>
                  <span className="notificacion-proyecto">Sistema de Gestion Academica</span>
                  <span className="badge-estado badge-similitud">Similitud</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to={{ pathname: '/aprendiz/detalle-similitud', state: { desde: 'alertas' } }} className="btn-accion"><i className="fas fa-search"></i> Revisar Similitud</Link>
                  <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(0)} disabled={leidas[0]}><i className="fas fa-check"></i> Marcar como leída</button>
                </div>
              </div>
            </div>
          </div>

          <div className="notificacion-card">
            <div className="notificacion-icono"><i className="fas fa-clock"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Revisión Pendiente</h3>
                <span className={leidas[1] ? 'badge-leida' : 'badge-no-leida'}>{leidas[1] ? 'Leída' : 'No leída'}</span>
              </div>
              <p className="notificacion-descripcion">Tu instructor Carlos Ruiz tiene pendiente la revisión de tu proyecto "App Movil para Inventarios". Recibirás notificaciones cuando haya comentarios.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Ayer</span>
                  <span className="notificacion-proyecto">App Movil para Inventarios</span>
                  <span className="badge-estado badge-revision">Revisión</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to="/aprendiz/detalle-proyecto/1" className="btn-accion"><i className="fas fa-eye"></i> Ver proyecto</Link>
                  <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(1)} disabled={leidas[1]}><i className="fas fa-check"></i> Marcar como leída</button>
                </div>
              </div>
            </div>
          </div>

          <div className="notificacion-card">
            <div className="notificacion-icono"><i className="fas fa-comment"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Comentario del Instructor</h3>
                <span className={leidas[2] ? 'badge-leida' : 'badge-no-leida'}>{leidas[2] ? 'Leída' : 'No leída'}</span>
              </div>
              <p className="notificacion-descripcion">Carlos Ruiz ha agregado comentarios a tu proyecto "Sistema de Gestion Academica". Revisa los comentarios y realiza los ajustes necesarios.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Hace 3 días</span>
                  <span className="notificacion-proyecto">Sistema de Gestion Academica</span>
                  <span className="badge-estado badge-mensaje">Mensaje</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to="/aprendiz/detalle-proyecto/0" className="btn-accion"><i className="fas fa-eye"></i> Ver Comentarios</Link>
                  <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(2)} disabled={leidas[2]}><i className="fas fa-check"></i> Marcar como leída</button>
                </div>
              </div>
            </div>
          </div>

          <div className="notificacion-card">
            <div className="notificacion-icono"><i className="fas fa-check-circle"></i></div>
            <div className="notificacion-cuerpo">
              <div className="notificacion-fila-superior">
                <h3 className="notificacion-titulo">Proyecto Aprobado</h3>
                <span className={leidas[3] ? 'badge-leida' : 'badge-no-leida'}>{leidas[3] ? 'Leída' : 'No leída'}</span>
              </div>
              <p className="notificacion-descripcion">Felicidades! Tu proyecto "App Movil para Inventarios" ha sido aprobado por el instructor. Puedes continuar con el desarrollo.</p>
              <div className="notificacion-fila-inferior">
                <div className="notificacion-metas">
                  <span className="notificacion-tiempo">Hace 1 semana</span>
                  <span className="notificacion-proyecto">App Movil para Inventarios</span>
                  <span className="badge-estado badge-sistema">Sistema</span>
                </div>
                <div className="notificacion-acciones">
                  <Link to="/aprendiz/detalle-proyecto/1" className="btn-accion"><i className="fas fa-eye"></i> Ver proyecto</Link>
                  <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(3)} disabled={leidas[3]}><i className="fas fa-check"></i> Marcar como leída</button>
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

export default AlertasAprendiz
