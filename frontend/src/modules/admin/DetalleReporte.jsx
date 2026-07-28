import { useState, useEffect, useRef } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import { useAuth } from '../../contexts/AuthContext'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/admin/reportes-fallas', label: 'Reportes de Fallas' },
  { label: 'Detalle Reporte' },
]

const badgeReporte = {
  pendiente: { clase: 'advertencia', icono: 'clock' },
  en_revision: { clase: 'primario', icono: 'cog' },
  resuelto: { clase: 'exito', icono: 'check' },
  rechazado: { clase: 'neutral', icono: 'lock' },
}

const etiquetaReporte = {
  pendiente: 'Pendiente',
  en_revision: 'En Revisión',
  resuelto: 'Resuelto',
  rechazado: 'Rechazado',
}

export default function DetalleReporte() {
  const { user } = useAuth()
  const { id } = useParams()
  const location = useLocation()
  const reporteData = location.state?.reporte

  const [estado, setEstado] = useState(reporteData?.estado || 'pendiente')
  const [mensaje, setMensaje] = useState(null)
  const timerRef = useRef(null)

  useEffect(() => {
    return () => { if (timerRef.current) clearTimeout(timerRef.current) }
  }, [])

  if (!reporteData) {
    return (
      <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
        <div className="contenedor-gestion fade-in">
          <PageHeader title="Reporte no encontrado" icon="exclamation-circle" breadcrumb={breadcrumb} actions={<Link to="/admin/reportes-fallas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>} />
          <div className="estado-vacio-moderno">
            <div className="estado-vacio-icono"><i className="fas fa-bug"></i></div>
            <h3 className="estado-vacio-titulo">Reporte no encontrado</h3>
            <p className="estado-vacio-descripcion">No se encontró información del reporte de falla{ id ? ` (ID: ${id})` : '' }.</p>
            <Link to="/admin/reportes-fallas" className="btn-primario"><i className="fas fa-arrow-left"></i> Volver a Reportes</Link>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  const cambiarEstado = (nuevoEstado) => {
    if (!window.confirm('¿Estás seguro de cambiar el estado del reporte?')) return
    setEstado(nuevoEstado)
    const textos = { en_revision: 'Marcado en revisión', resuelto: 'Marcado como resuelto', rechazado: 'Rechazado' }
    setMensaje({ tipo: 'exito', texto: textos[nuevoEstado] || 'Estado actualizado' })
    if (timerRef.current) clearTimeout(timerRef.current)
    timerRef.current = setTimeout(() => setMensaje(null), 3000)
  }

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
      <div className="contenedor-gestion fade-in">
        <PageHeader
          title="Detalle del Reporte de Falla"
          icon="bug"
          breadcrumb={breadcrumb}
          actions={<Link to="/admin/reportes-fallas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Información del Reporte" icon="info-circle">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Titulo</div>
              <div className="detalle-valor">{reporteData.titulo || 'Sin título'}</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className={`badge badge-${badgeReporte[estado]?.clase || 'primario'}`}><i className={`fas fa-${badgeReporte[estado]?.icono || 'cog'}`}></i> {etiquetaReporte[estado] || estado}</span></p>
            </div>
            <div>
              <div className="detalle-label">Reportado por</div>
              <div className="detalle-valor">{reporteData.usuario}</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de reporte</div>
              <div className="detalle-valor">{reporteData.fecha}</div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Descripción</div>
              <div className="detalle-valor-texto">{reporteData.descripcion || 'Sin descripción'}</div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Pasos para reproducir</div>
              <div className="detalle-valor-texto">{reporteData.pasos || 'No especificados'}</div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Historial de Actualizaciones" icon="comments">
          <div className="lista-actividad">
            <div className="actividad-item borde-primario">
              <div className="flex-between">
                <strong>Admin Principal</strong>
                <span className="actividad-fecha">09/04/2026 10:30</span>
              </div>
              <p className="detalle-linea">He asignado el reporte al equipo de desarrollo. Se está investigando la causa raíz del error en el módulo de similitudes.</p>
            </div>
            <div className="actividad-item borde-advertencia">
              <div className="flex-between">
                <strong>Técnico de Soporte</strong>
                <span className="actividad-fecha">09/04/2026 14:15</span>
              </div>
              <p className="detalle-linea">Se identificó un problema de compatibilidad con la versión de PHP en el servidor. Se está preparando un parche correctivo.</p>
            </div>
          </div>
        </DataPanel>

        <div className={`mensaje-feedback mensaje-exito ${mensaje ? '' : 'oculto'} mb-md`}>
          <i className="fas fa-check-circle"></i><span>{mensaje?.texto || ''}</span>
        </div>

        <div className="acciones-finales">
          <button type="button" className="btn-primario" onClick={() => cambiarEstado('en_revision')}><i className="fas fa-cog"></i> Marcar en Revisión</button>
          <button type="button" className="btn-primario" onClick={() => cambiarEstado('resuelto')}><i className="fas fa-check"></i> Marcar como Resuelto</button>
          <button type="button" className="btn-rechazar" onClick={() => cambiarEstado('rechazado')}><i className="fas fa-times"></i> Rechazar</button>
        </div>
      </div>
    </DashboardLayout>
  )
}
