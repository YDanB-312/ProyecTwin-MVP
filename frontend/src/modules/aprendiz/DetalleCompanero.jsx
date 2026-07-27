import { useState, useEffect } from 'react'
import { useLocation, Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/mi-perfil.css'

export default function DetalleCompanero() {
  const location = useLocation()
  const { nombre, iniciales, estado, foto } = location.state || {}
  const [verFoto, setVerFoto] = useState(false)

  useEffect(() => {
    if (!verFoto) return
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') setVerFoto(false)
    }
    document.addEventListener('keydown', handleKeyDown)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = ''
    }
  }, [verFoto])

  if (!nombre) {
    return (
      <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
        <div className="contenedor-pagina fade-in">
          <PageHeader
            title="Perfil del Compañero"
            icon="user"
            breadcrumb={[
              { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
              { to: '/aprendiz/detalle-ficha', label: 'Mi Ficha' },
              { label: 'Compañero' }
            ]}
          />
          <div className="perfil-card">
            <p>No se encontró información del compañero.</p>
            <Link to="/aprendiz/detalle-ficha" className="btn-primario">Volver a Mi Ficha</Link>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-pagina fade-in">
        <PageHeader
          title="Perfil del Compañero"
          icon="user"
          breadcrumb={[
            { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
            { to: '/aprendiz/detalle-ficha', label: 'Mi Ficha' },
            { label: nombre }
          ]}
        />

        <div className="perfil-card cabecera-card">
          {foto
            ? <img src={foto} alt={nombre} className="companero-avatar-lg avatar-clickable" onClick={() => setVerFoto(true)} />
            : <div className="companero-avatar-lg">{iniciales || nombre?.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()}</div>
          }
          <div className="companero-detalle-info">
            <h2 className="companero-detalle-nombre">{nombre}</h2>
            <span className={estado === 'Activo' ? 'badge-activo' : 'badge-inactivo'}>
              <i className="fas fa-circle"></i> {estado}
            </span>
          </div>
        </div>

        <div className="perfil-card">
          <h3 className="card-titulo"><i className="fas fa-info-circle"></i> Información</h3>
          <div className="perfil-info-grid">
            <div className="perfil-info-item">
              <span className="perfil-info-label">Nombre</span>
              <span className="perfil-info-valor">{nombre}</span>
            </div>
            <div className="perfil-info-item">
              <span className="perfil-info-label">Programa</span>
              <span className="perfil-info-valor">Análisis y Desarrollo 2568</span>
            </div>
            <div className="perfil-info-item">
              <span className="perfil-info-label">Ficha</span>
              <span className="perfil-info-valor">ADSO-2568</span>
            </div>
            <div className="perfil-info-item">
              <span className="perfil-info-label">Estado</span>
              <span className="perfil-info-valor">{estado}</span>
            </div>
          </div>
        </div>

        <Link to="/aprendiz/detalle-ficha" className="btn-primario btn-volver-ficha">
          <i className="fas fa-arrow-left"></i> Volver a Mi Ficha
        </Link>

        {verFoto && foto && (
          <div className="lightbox-overlay" onClick={() => setVerFoto(false)} role="dialog" aria-modal="true" aria-label="Imagen ampliada">
            <button className="lightbox-cerrar" onClick={() => setVerFoto(false)} aria-label="Cerrar" autoFocus>
              <i className="fas fa-times"></i>
            </button>
            <img src={foto} alt={nombre} className="lightbox-img" onClick={(e) => e.stopPropagation()} />
          </div>
        )}
      </div>
    </DashboardLayout>
  )
}
