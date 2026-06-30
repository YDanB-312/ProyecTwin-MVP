import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/alertas.css'

const etiquetaTipo = {
  similitud: 'Similitud',
  revision: 'Revisión',
  mensaje: 'Mensaje',
  sistema: 'Sistema',
}

const badgeClaseTipo = {
  similitud: 'badge-similitud',
  revision: 'badge-revision',
  mensaje: 'badge-mensaje',
  sistema: 'badge-sistema',
}

const notificaciones = [
  {
    icono: 'user-plus',
    titulo: 'Nuevo Usuario Registrado',
    descripcion: 'Se ha registrado un nuevo aprendiz en el programa ADSO. Revisa los detalles y verifica que la información sea correcta antes de activar la cuenta.',
    tiempo: 'Hace 1 hora',
    modulo: 'Usuarios',
    tipo: 'sistema',
    leida: false,
    enlace: '/admin/gestion-usuarios',
    textoEnlace: 'Ver Usuario',
    iconoEnlace: 'eye',
  },
  {
    icono: 'bug',
    titulo: 'Reporte de Falla Recibido',
    descripcion: 'Carlos Rodriguez ha reportado una falla en el módulo de similitudes. Se requiere gestionar el reporte y asignarlo al equipo técnico correspondiente.',
    tiempo: 'Hace 2 horas',
    modulo: 'Reportes',
    tipo: 'revision',
    leida: false,
    enlace: '/admin/detalle-reporte',
    textoEnlace: 'Gestionar Reporte',
    iconoEnlace: 'wrench',
  },
  {
    icono: 'shield-alt',
    titulo: 'Alerta de Seguridad',
    descripcion: 'Se ha detectado un intento de acceso no autorizado desde una dirección IP desconocida. Revisa los registros de actividad del sistema para identificar la fuente.',
    tiempo: 'Ayer',
    modulo: 'Sistema',
    tipo: 'sistema',
    leida: false,
    enlace: '/admin/dashboard',
    textoEnlace: 'Revisar Actividad',
    iconoEnlace: 'search',
  },
  {
    icono: 'chart-line',
    titulo: 'Similitud Crítica Detectada',
    descripcion: 'El proyecto "Sistema de Gestión Académica" presenta un 65% de similitud con otro proyecto. Se requiere notificar al instructor asignado para revisión manual.',
    tiempo: 'Hace 3 dias',
    modulo: 'Similitudes',
    tipo: 'similitud',
    leida: true,
    enlace: '/admin/similitudes',
    textoEnlace: 'Revisar Similitud',
    iconoEnlace: 'search',
  },
]

export default function NotificacionesAdmin() {
  const [filtroTipo, setFiltroTipo] = useState('')
  const [filtroEstado, setFiltroEstado] = useState('')
  const [filtroModulo, setFiltroModulo] = useState('')
  const [leidas, setLeidas] = useState(notificaciones.map(n => n.leida))
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
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={4}>
      <div className="contenedor-alertas">

        <PageHeader
          title="Notificaciones"
          icon="bell"
          breadcrumb={[
            { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
            { label: 'Notificaciones' }
          ]}
          actions={<><Link to="/admin/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link><button className="btn-marcar-todas" type="button" onClick={marcarTodasLeidas}><i className="fas fa-check-double"></i> Marcar todas como leídas</button></>}
        />

        <div className="filtros-card">
          <div className="grupo-filtro">
            <label htmlFor="tipo-alerta">Tipo de Alerta</label>
            <select id="tipo-alerta" className="filtro-select" name="tipo_alerta" value={filtroTipo} onChange={(e) => { setFiltroTipo(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los tipos</option>
              <option value="similitud">Similitud</option>
              <option value="revision">Revisión</option>
              <option value="mensaje">Mensaje</option>
              <option value="sistema">Sistema</option>
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
            <label htmlFor="modulo-alerta">Módulo</label>
            <select id="modulo-alerta" className="filtro-select" name="modulo_alerta" value={filtroModulo} onChange={(e) => { setFiltroModulo(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los módulos</option>
              <option value="usuarios">Usuarios</option>
              <option value="reportes">Reportes de falla</option>
              <option value="similitudes">Similitudes</option>
              <option value="sistema">Sistema</option>
            </select>
          </div>
        </div>

        <div className="lista-notificaciones">
          {notificaciones.map((n, i) => (
            <div className="notificacion-card" key={i}>
              <div className="notificacion-icono"><i className={`fas fa-${n.icono}`}></i></div>
              <div className="notificacion-cuerpo">
                <div className="notificacion-fila-superior">
                  <h3 className="notificacion-titulo">{n.titulo}</h3>
                  <span className={leidas[i] ? 'badge-leida' : 'badge-no-leida'}>{leidas[i] ? 'Leída' : 'No leída'}</span>
                </div>
                <p className="notificacion-descripcion">{n.descripcion}</p>
                <div className="notificacion-fila-inferior">
                  <div className="notificacion-metas">
                    <span className="notificacion-tiempo">{n.tiempo}</span>
                    <span className="notificacion-proyecto">Módulo {n.modulo}</span>
                    <span className={`badge-estado ${badgeClaseTipo[n.tipo]}`}>{etiquetaTipo[n.tipo]}</span>
                  </div>
                  <div className="notificacion-acciones">
                    <Link to={n.enlace} className="btn-accion"><i className={`fas fa-${n.iconoEnlace}`}></i> {n.textoEnlace}</Link>
                    <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(i)} disabled={leidas[i]}><i className="fas fa-check"></i> Marcar como leída</button>
                  </div>
                </div>
              </div>
            </div>
          ))}
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
  )
}
