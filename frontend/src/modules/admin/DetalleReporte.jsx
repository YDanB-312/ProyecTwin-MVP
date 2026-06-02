import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/reportes-fallas.css'

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/admin/reportes-fallas', label: 'Reportes de Fallas' },
  { label: 'Detalle Reporte' },
]

export default function DetalleReporte() {
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12}>
      <div className="contenedor-gestion">
        <PageHeader
          title="Detalle del Reporte de Falla"
          icon="bug"
          breadcrumb={breadcrumb}
          actions={<Link to="/admin/reportes-fallas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Informacion del Reporte" icon="info-circle">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Titulo</div>
              <div className="detalle-valor">Error al cargar el modulo de similitudes</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className="badge badge-advertencia">En proceso</span></p>
            </div>
            <div>
              <div className="detalle-label">Reportado por</div>
              <div className="detalle-valor">Maria Gonzalez (Aprendiz)</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de reporte</div>
              <div className="detalle-valor">08/04/2026</div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Descripcion</div>
              <div className="detalle-valor-texto">Al intentar acceder al modulo de deteccion de similitudes desde el panel del aprendiz, el sistema muestra un error interno y no permite completar la busqueda. El error aparece despues de seleccionar el proyecto y hacer clic en "Buscar similitudes".</div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Pasos para reproducir</div>
              <div className="detalle-valor-texto">1. Iniciar sesion como aprendiz. 2. Ir a "Mis proyectos". 3. Seleccionar un proyecto existente. 4. Hacer clic en "Buscar similitudes". 5. El sistema muestra un error 500.</div>
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
              <p className="detalle-linea">He asignado el reporte al equipo de desarrollo. Se esta investigando la causa raiz del error en el modulo de similitudes.</p>
            </div>
            <div className="actividad-item borde-advertencia">
              <div className="flex-between">
                <strong>Tecnico de Soporte</strong>
                <span className="actividad-fecha">09/04/2026 14:15</span>
              </div>
              <p className="detalle-linea">Se identifico un problema de compatibilidad con la version de PHP en el servidor. Se esta preparando un parche correctivo.</p>
            </div>
          </div>
        </DataPanel>

        <div className="margen-superior">
          <Link to="/admin/reportes-fallas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
        </div>
      </div>
    </DashboardLayout>
  )
}
