import { useState } from 'react'
import { useLocation, useParams, Link } from 'react-router-dom'
import DetalleSimilitudBase from '../../components/DetalleSimilitudBase/DetalleSimilitudBase'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import { useAuth } from '../../contexts/AuthContext'
import PageHeader from '../../components/PageHeader/PageHeader'

function buildBreadcrumb(desde) {
  const items = [
    { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  ]
  if (desde !== 'dashboard') {
    items.push({ to: '/instructor/alertas', label: 'Notificaciones' })
  }
  items.push({ label: 'Detalle Similitud' })
  return items
}

export default function DetalleSimilitudInstructor() {
  const { user } = useAuth()
  const [revisada, setRevisada] = useState(false)
  const [contactado, setContactado] = useState(false)
  const { id } = useParams()
  const location = useLocation()
  const proyectoActual = location.state?.proyecto || id
  const desde = location.state?.desde

  if (!proyectoActual) {
    return (
      <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
        <div className="contenedor-revision fade-in">
          <PageHeader title="Similitud no encontrada" icon="exclamation-circle" breadcrumb={[{ to: '/instructor/dashboard', icon: 'home', label: 'Inicio' }, { label: 'Detalle Similitud' }]} />
          <div className="estado-vacio-moderno">
            <div className="estado-vacio-icono"><i className="fas fa-search"></i></div>
            <h3 className="estado-vacio-titulo">Similitud no encontrada</h3>
            <p className="estado-vacio-descripcion">No se encontró información de la similitud.</p>
            <Link to="/instructor/alertas" className="btn-primario"><i className="fas fa-arrow-left"></i> Volver a Notificaciones</Link>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  const volver = desde === 'dashboard' ? '/instructor/dashboard' : '/instructor/alertas'
  const volverLabel = desde === 'dashboard' ? 'Volver al Panel' : 'Volver a Notificaciones'

  return (
    <DetalleSimilitudBase
      role="instructor"
      dashboardTitulo="ProyecTwin - Panel del Instructor"
      dashboardUsuario={user?.nombre || 'Usuario'}
      notificaciones={0}
      proyectoActual={proyectoActual}
      bannerPrefix="El"
      breadcrumbItems={buildBreadcrumb(desde)}
      volverLink={{ path: volver, label: volverLabel }}
      nombreCard1={proyectoActual}
      card1={{ titulo: proyectoActual, aprendiz: 'Juan Pérez', programa: 'ADSO', fecha: '15/03/2026' }}
      coincidenciasClasses={{ root: 'alerta-roja similitud-coincidencias', list: 'lista-coincidencias coincidencias-grid' }}
      volverClasses="acciones-finales"
      acciones={
        <div className="comentarios-revision similitud-coincidencias">
          <h4><i className="fas fa-clipboard-check"></i> Acciones</h4>
          <p className="texto-ayuda">Revisa los detalles de similitud y toma una decision sobre este caso.</p>
          {revisada && <div className="mensaje-feedback mensaje-exito mb-md"><i className="fas fa-check-circle"></i> Marcada como revisada</div>}
          {contactado && <div className="mensaje-feedback mensaje-exito mb-md"><i className="fas fa-envelope"></i> Notificación enviada al aprendiz</div>}
          <div className="flex-row flex-wrap mt-12">
            <button type="button" className="btn-primario" disabled={revisada} onClick={() => setRevisada(true)}><i className="fas fa-check"></i> {revisada ? 'Ya revisada' : 'Marcar como Revisada'}</button>
            <button type="button" className="btn-secundario" disabled={contactado} onClick={() => setContactado(true)}><i className="fas fa-envelope"></i> {contactado ? 'Aprendiz contactado' : 'Contactar Aprendiz'}</button>
          </div>
        </div>
      }
    />
  )
}
