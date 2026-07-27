import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/fichas.css'

export default function DetalleFicha() {
  const [verFoto, setVerFoto] = useState(null)

  useEffect(() => {
    if (!verFoto) return
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') setVerFoto(null)
    }
    document.addEventListener('keydown', handleKeyDown)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = ''
    }
  }, [verFoto])
  const companeros = [
    { nombre: 'Maria Gonzalez', iniciales: 'MG', estado: 'Activo', foto: 'https://i.pravatar.cc/400?img=1' },
    { nombre: 'Juan Pérez', iniciales: 'JP', estado: 'Activo', foto: 'https://i.pravatar.cc/400?img=3' },
    { nombre: 'Laura Gómez', iniciales: 'LG', estado: 'Activo', foto: 'https://i.pravatar.cc/400?img=5' },
    { nombre: 'Ana Martínez', iniciales: 'AM', estado: 'Activo', foto: 'https://i.pravatar.cc/400?img=9' },
    { nombre: 'Diana Sánchez', iniciales: 'DS', estado: 'Inactivo', foto: 'https://i.pravatar.cc/400?img=16' },
  ]
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-pagina fade-in">

        <PageHeader
          title="Mi Ficha"
          icon="users"
          breadcrumb={[
            { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
            { label: 'Mi Ficha' }
          ]}
        />

        <div className="ficha-card">
          <div className="ficha-grid">
            <div className="ficha-dato">
              <span className="ficha-label">Programa</span>
              <span className="ficha-valor">Análisis y Desarrollo 2568</span>
            </div>
            <div className="ficha-dato">
              <span className="ficha-label">Codigo</span>
              <span className="ficha-valor"><span className="codigo-ficha">ADSO-2568</span></span>
            </div>
            <div className="ficha-dato">
              <span className="ficha-label">Estado</span>
              <span className="badge-activo"><i className="fas fa-circle"></i> Activa</span>
            </div>
            <div className="ficha-dato ficha-dato-full">
              <span className="ficha-label">Instructor</span>
              <div className="instructor-info">
                <div className="instructor-avatar">CR</div>
                <div>
                  <span className="ficha-valor">Carlos Ruiz</span>
                  <span className="instructor-area">Tecnologías de la Información</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="seccion-header">
          <h2 className="seccion-titulo">Companeros</h2>
          <span className="contador-pill">5</span>
        </div>

        <div className="companeros-grid">
          {companeros.map((c) => (
            <div key={c.iniciales} className="companero-card">
              {c.foto
                ? <img src={c.foto} alt={c.nombre} className="companero-avatar avatar-clickable" onClick={(e) => { e.preventDefault(); e.stopPropagation(); setVerFoto(c) }} />
                : <div className="companero-avatar">{c.iniciales}</div>
              }
              <Link to="/aprendiz/perfil-companero" state={c} className="companero-info-link">
                <div className="companero-info">
                  <span className="companero-nombre">{c.nombre}</span>
                </div>
                <span className={c.estado === 'Activo' ? 'badge-activo' : 'badge-inactivo'}>{c.estado}</span>
              </Link>
            </div>
          ))}
        </div>

        {verFoto && verFoto.foto && (
          <div className="lightbox-overlay" onClick={() => setVerFoto(null)} role="dialog" aria-modal="true" aria-label="Imagen ampliada">
            <button className="lightbox-cerrar" onClick={() => setVerFoto(null)} aria-label="Cerrar" autoFocus>
              <i className="fas fa-times"></i>
            </button>
            <img src={verFoto.foto} alt={verFoto.nombre} className="lightbox-img" onClick={(e) => e.stopPropagation()} />
          </div>
        )}
      </div>
    </DashboardLayout>
  )
}


