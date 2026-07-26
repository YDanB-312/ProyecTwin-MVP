import { useLocation } from 'react-router-dom'
import DetalleSimilitudBase from '../../components/DetalleSimilitudBase/DetalleSimilitudBase'

export default function DetalleSimilitud() {
  const location = useLocation()
  const origen = location.state?.desde
  const proyectoActual = location.state?.proyecto || 'Sistema de Gestion Academica'
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
