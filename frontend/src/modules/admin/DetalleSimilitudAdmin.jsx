import { useState, useEffect, useRef } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import { useAuth } from '../../contexts/AuthContext'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'

const coincidencias = [
  { seccion: 'Descripción del proyecto', pct: 72 },
  { seccion: 'Tecnologías propuestas', pct: 60 },
  { seccion: 'Objetivos generales', pct: 55 },
  { seccion: 'Metodología', pct: 45 },
]

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/admin/similitudes', label: 'Similitudes' },
  { label: 'Detalle de Similitud' },
]

const similitudesProyecto = [
  { proyecto: 'Plataforma Educativa SENA', porcentaje: 65, aprendiz: 'Ana Martínez', programa: 'ADSO', fecha: '02/02/2026' },
  { proyecto: 'Plataforma de Cursos Online', porcentaje: 55, aprendiz: 'Pedro Lopez', programa: 'ADSO', fecha: '10/01/2026' },
]

export default function DetalleSimilitudAdmin() {
  const { user } = useAuth()
  const { id } = useParams()
  const location = useLocation()
  const proyectoActual = location.state?.proyecto || id

  const [actual, setActual] = useState(0)
  const [mensaje, setMensaje] = useState(null)
  const timerRef = useRef(null)

  useEffect(() => {
    return () => { if (timerRef.current) clearTimeout(timerRef.current) }
  }, [])

  if (!proyectoActual) {
    return (
      <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
        <div className="contenedor-gestion fade-in">
          <PageHeader title="Similitud no encontrada" icon="exclamation-circle" breadcrumb={breadcrumb} actions={<Link to="/admin/similitudes" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>} />
          <div className="estado-vacio-moderno">
            <div className="estado-vacio-icono"><i className="fas fa-search"></i></div>
            <h3 className="estado-vacio-titulo">Similitud no encontrada</h3>
            <p className="estado-vacio-descripcion">No se encontró información de la similitud.</p>
            <Link to="/admin/similitudes" className="btn-primario"><i className="fas fa-arrow-left"></i> Volver a Similitudes</Link>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  const sim = similitudesProyecto[actual] || similitudesProyecto[0]

  const marcarRevisada = () => {
    setMensaje({ tipo: 'exito', texto: 'Similitud marcada como revisada' })
    if (timerRef.current) clearTimeout(timerRef.current)
    timerRef.current = setTimeout(() => setMensaje(null), 3000)
  }

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
      <div className="contenedor-gestion fade-in">
        <PageHeader
          title="Detalle de Similitud"
          icon="exclamation-triangle"
          breadcrumb={breadcrumb}
          actions={<Link to="/admin/similitudes" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <div className="tarjeta-bienvenida-moderna banner-similitud">
          <div className="banner-similitud-content">
            <div className="banner-similitud-text">
              <strong className="proyecto-nombre">{proyectoActual}</strong> tiene <strong>{similitudesProyecto.length} similitudes</strong> detectadas.
              Mostrando: con
              <select className="select-nav-similitud" value={actual} onChange={(e) => setActual(Number(e.target.value))}>
                {similitudesProyecto.map((s) => (
                  <option key={s.proyecto} value={similitudesProyecto.indexOf(s)}>{s.proyecto}</option>
                ))}
              </select>
              ({actual + 1} de {similitudesProyecto.length})
            </div>
          </div>
        </div>

        <DataPanel title="Comparación de Proyectos" icon="balance-scale">
          <div className="similitud-encabezado">
            <div className="encabezado-revision">
              <div className="info-revision">
                <div className="similitud-meta">
                  <span><i className="fas fa-calendar"></i> Detectado: 25/04/2026</span>
                  <span><i className="fas fa-percent"></i> Similitud: <strong className={sim.porcentaje > 70 ? 'texto-peligro' : sim.porcentaje >= 40 ? 'texto-advertencia' : 'texto-exito'}>{sim.porcentaje}%</strong></span>
                </div>
              </div>
              <span className={`badge ${sim.porcentaje > 70 ? 'badge-peligro' : sim.porcentaje >= 40 ? 'badge-advertencia' : 'badge-primario'}`}><i className="fas fa-exclamation-circle"></i> Nivel {sim.porcentaje > 70 ? 'Crítico' : sim.porcentaje >= 40 ? 'Moderado' : 'Bajo'}</span>
            </div>
          </div>
          <div className="detalle-grid similitud-proyectos-grid">
            <div className="card-proyecto-compacto">
              <h4 className="card-titulo-verde"><i className="fas fa-file-alt"></i> {proyectoActual}</h4>
              <p className="info-linea">{proyectoActual}</p>
              <p className="detalle-linea"><strong>Aprendiz:</strong> Juan Pérez</p>
              <p className="detalle-linea"><strong>Programa:</strong> ADSO</p>
              <p className="detalle-linea"><strong>Fecha:</strong> 15/03/2026</p>
            </div>
            <div className="card-proyecto-compacto">
              <h4 className="card-titulo-amarillo"><i className="fas fa-file-alt"></i> {sim.proyecto}</h4>
              <p className="info-linea">{sim.proyecto}</p>
              <p className="detalle-linea"><strong>Aprendiz:</strong> {sim.aprendiz}</p>
              <p className="detalle-linea"><strong>Programa:</strong> {sim.programa}</p>
              <p className="detalle-linea"><strong>Fecha:</strong> {sim.fecha}</p>
            </div>
          </div>
          <div className="similitud-coincidencias">
            <h4><i className="fas fa-align-left"></i> Secciones Coincidentes</h4>
            <ul className="coincidencias-grid">
              {coincidencias.map((c) => (
                <li key={c.seccion} className="coincidencia-item">
                  {c.seccion}: <strong>{c.pct}% de similitud</strong>
                </li>
              ))}
            </ul>
          </div>
        </DataPanel>

        <div className={`mensaje-feedback mensaje-exito ${mensaje ? '' : 'oculto'} mb-md`}>
          <i className="fas fa-check-circle"></i><span>{mensaje?.texto || ''}</span>
        </div>

        <div className="acciones-finales">
          <button type="button" className="btn-primario" onClick={marcarRevisada}><i className="fas fa-check"></i> Marcar como Revisada</button>
        </div>
      </div>
    </DashboardLayout>
  )
}
