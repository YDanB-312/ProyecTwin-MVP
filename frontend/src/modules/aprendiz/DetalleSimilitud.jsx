import { useLocation, Link } from 'react-router-dom'
import DetalleSimilitudBase from '../../components/DetalleSimilitudBase/DetalleSimilitudBase'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'

export default function DetalleSimilitud() {
  const location = useLocation()
  const origen = location.state?.desde
  const proyectoActual = location.state?.proyecto

  if (!proyectoActual) {
    return (
      <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
        <div className="contenedor-pagina fade-in">
          <PageHeader title="Similitud no encontrada" icon="exclamation-circle" breadcrumb={[{ to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' }, { label: 'Detalle Similitud' }]} />
          <div className="estado-vacio-moderno">
            <div className="estado-vacio-icono"><i className="fas fa-search"></i></div>
            <h3 className="estado-vacio-titulo">Similitud no encontrada</h3>
            <p className="estado-vacio-descripcion">No se encontró información de la similitud.</p>
            <Link to="/aprendiz/alertas" className="btn-primario"><i className="fas fa-arrow-left"></i> Volver a Notificaciones</Link>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  const rutaVolver = origen === 'resultado-analisis' ? '/aprendiz/mis-proyectos' : '/aprendiz/alertas'
  const textoVolver = origen === 'resultado-analisis' ? 'Volver a Mis proyectos' : 'Volver a Notificaciones'

  return (
    <DetalleSimilitudBase
      role="aprendiz"
      dashboardTitulo="ProyecTwin - Panel del Aprendiz"
      dashboardUsuario="Maria Gonzalez | ADSO"
      notificaciones={5}
      proyectoActual={proyectoActual}
      bannerPrefix="Tu"
      breadcrumbItems={[
        { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
        { label: 'Detalle Similitud' },
      ]}
      volverLink={{ path: rutaVolver, label: textoVolver }}
      card1={{ titulo: proyectoActual, aprendiz: 'Maria Gonzalez', programa: 'ADSO', fecha: '15/03/2026' }}
      acciones={null}
    />
  )
}
