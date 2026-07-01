import { Link, useLocation } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
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

const defaultReporte = { estado: 'en_revision', usuario: 'Maria Gonzalez (Aprendiz)', fecha: '08/04/2026', descripcion: '', titulo: 'Error al cargar el módulo de similitudes', pasos: '' }

export default function DetalleReporte() {
  const location = useLocation()
  const reporteData = location.state?.reporte || defaultReporte
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
      <div className="contenedor-gestion">
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
              <div className="detalle-valor">Error al cargar el módulo de similitudes</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className={`badge badge-${badgeReporte[reporteData.estado]?.clase || 'primario'}`}><i className={`fas fa-${badgeReporte[reporteData.estado]?.icono || 'cog'}`}></i> {etiquetaReporte[reporteData.estado] || reporteData.estado}</span></p>
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
              <div className="detalle-valor-texto">Al intentar acceder al módulo de detección de similitudes desde el panel del aprendiz, el sistema muestra un error interno y no permite completar la búsqueda. El error aparece después de seleccionar el proyecto y hacer clic en "Buscar similitudes".</div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Pasos para reproducir</div>
              <div className="detalle-valor-texto">1. Iniciar sesión como aprendiz. 2. Ir a "Mis proyectos". 3. Seleccionar un proyecto existente. 4. Hacer clic en "Buscar similitudes". 5. El sistema muestra un error 500.</div>
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

        <div className="acciones-finales">
          <Link to="/admin/reportes-fallas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
        </div>
      </div>
    </DashboardLayout>
  )
}
