import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'

const coincidencias = [
  { seccion: 'Descripción del proyecto', pct: 72 },
  { seccion: 'Tecnologías propuestas', pct: 60 },
  { seccion: 'Objetivos generales', pct: 55 },
  { seccion: 'Metodología', pct: 45 },
]

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

const similitudesProyecto = [
  { proyecto: 'Plataforma Educativa SENA', porcentaje: 65, aprendiz: 'Ana Martínez', programa: 'ADSO', fecha: '02/02/2026' },
  { proyecto: 'Plataforma de Cursos Online', porcentaje: 55, aprendiz: 'Pedro Lopez', programa: 'ADSO', fecha: '10/01/2026' },
]

function DetalleSimilitudInstructor() {
  const location = useLocation()
  const proyectoActual = location.state?.proyecto || 'Sistema de Gestión Académica'
  const [actual, setActual] = useState(0)
  const sim = similitudesProyecto[actual]
  const desde = location.state?.desde
  const volver = desde === 'dashboard' ? '/instructor/dashboard' : '/instructor/alertas'
  const volverLabel = desde === 'dashboard' ? 'Volver al Panel' : 'Volver a Notificaciones'

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-pagina">
        <PageHeader
          title="Detalle de Similitud"
          icon="exclamation-triangle"
          breadcrumb={buildBreadcrumb(desde)}
          actions={<Link to={volver} className="btn-secundario"><i className="fas fa-arrow-left"></i> {volverLabel}</Link>}
        />

        <div className="tarjeta-bienvenida-moderna banner-similitud">
          <div className="banner-similitud-content">
            <div className="banner-similitud-text">
              El proyecto <strong>{proyectoActual}</strong> tiene <strong>{similitudesProyecto.length} similitudes</strong> detectadas.
              Mostrando: con
              <select className="select-nav-similitud" value={actual} onChange={(e) => setActual(Number(e.target.value))}>
                {similitudesProyecto.map((s, i) => (
                  <option key={i} value={i}>{s.proyecto}</option>
                ))}
              </select>
              ({actual + 1} de {similitudesProyecto.length})
            </div>
          </div>
        </div>

        <DataPanel title="Comparación de Proyectos" icon="balance-scale">
          <div className="similitud-encabezado">
            <div className="similitud-meta">
              <span><i className="fas fa-calendar"></i> Detectado: 25/04/2026</span>
              <span><i className="fas fa-percent"></i> Similitud: <strong className={sim.porcentaje > 70 ? 'texto-peligro' : sim.porcentaje >= 40 ? 'texto-advertencia' : 'texto-exito'}>{sim.porcentaje}%</strong></span>
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
          <div className="alerta-roja similitud-coincidencias">
            <h4><i className="fas fa-align-left"></i> Secciones Coincidentes</h4>
            <ul className="lista-coincidencias coincidencias-grid">
              {coincidencias.map((c, i) => (
                <li key={i} className="coincidencia-item">
                  {c.seccion}: <strong>{c.pct}% de similitud</strong>
                </li>
              ))}
            </ul>
          </div>
          <div className="comentarios-revision similitud-coincidencias">
            <h4><i className="fas fa-clipboard-check"></i> Acciones</h4>
            <p className="texto-ayuda">Revisa los detalles de similitud y toma una decision sobre este caso.</p>
            <div className="flex-row flex-wrap mt-12">
        <button type="button" className="btn-primario" onClick={() => {}}><i className="fas fa-check"></i> Marcar como Revisada</button>
        <button type="button" className="btn-secundario" onClick={() => {}}><i className="fas fa-envelope"></i> Contactar Aprendiz</button>
            </div>
          </div>
        </DataPanel>

        <div className="acciones-finales">
          <Link to={volver} className="btn-secundario"><i className="fas fa-arrow-left"></i> {volverLabel}</Link>
        </div>
      </div>
    </DashboardLayout>
  )
}

export default DetalleSimilitudInstructor
