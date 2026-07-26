import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../DashboardLayout/DashboardLayout'
import PageHeader from '../PageHeader/PageHeader'
import DataPanel from '../DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'

const coincidencias = [
  { seccion: 'Descripción del proyecto', pct: 72 },
  { seccion: 'Tecnologías propuestas', pct: 60 },
  { seccion: 'Objetivos generales', pct: 55 },
  { seccion: 'Metodología', pct: 45 },
]

const similitudesProyecto = [
  { proyecto: 'Plataforma Educativa SENA', porcentaje: 65, aprendiz: 'Ana Martínez', programa: 'ADSO', fecha: '02/02/2026' },
  { proyecto: 'Plataforma de Cursos Online', porcentaje: 55, aprendiz: 'Pedro Lopez', programa: 'ADSO', fecha: '10/01/2026' },
]

export default function DetalleSimilitudBase({
  role, dashboardTitulo, dashboardUsuario, notificaciones,
  bannerPrefix = 'Tu',
  breadcrumbItems, volverLink,
  proyectoActual,
  nombreCard1 = 'Mi Proyecto',
  card1,
  acciones,
  coincidenciasClasses = { root: 'similitud-coincidencias', list: 'coincidencias-grid' },
  volverClasses = 'margen-superior',
}) {
  const [actual, setActual] = useState(0)
  const sim = similitudesProyecto[actual] || similitudesProyecto[0]

  return (
    <DashboardLayout role={role} titulo={dashboardTitulo} usuario={dashboardUsuario} notificaciones={notificaciones}>
      <div className="contenedor-pagina fade-in">
        <PageHeader
          title="Detalle de Similitud"
          icon="exclamation-triangle"
          breadcrumb={breadcrumbItems}
          actions={<Link to={volverLink.path} className="btn-secundario"><i className="fas fa-arrow-left"></i> {volverLink.label}</Link>}
        />

        <div className="tarjeta-bienvenida-moderna banner-similitud">
          <div className="banner-similitud-content">
            <div className="banner-similitud-text">
              {bannerPrefix} proyecto <strong>{proyectoActual}</strong> tiene <strong>{similitudesProyecto.length} similitudes</strong> detectadas.
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
              <h4 className="card-titulo-verde"><i className="fas fa-file-alt"></i> {nombreCard1}</h4>
              <p className="info-linea">{card1.titulo}</p>
              <p className="detalle-linea"><strong>Aprendiz:</strong> {card1.aprendiz}</p>
              <p className="detalle-linea"><strong>Programa:</strong> {card1.programa}</p>
              <p className="detalle-linea"><strong>Fecha:</strong> {card1.fecha}</p>
            </div>
            <div className="card-proyecto-compacto">
              <h4 className="card-titulo-amarillo"><i className="fas fa-file-alt"></i> {sim.proyecto}</h4>
              <p className="info-linea">{sim.proyecto}</p>
              <p className="detalle-linea"><strong>Aprendiz:</strong> {sim.aprendiz}</p>
              <p className="detalle-linea"><strong>Programa:</strong> {sim.programa}</p>
              <p className="detalle-linea"><strong>Fecha:</strong> {sim.fecha}</p>
            </div>
          </div>
          <div className={coincidenciasClasses.root}>
            <h4><i className="fas fa-align-left"></i> Secciones Coincidentes</h4>
            <ul className={coincidenciasClasses.list}>
              {coincidencias.map((c, i) => (
                <li key={i} className="coincidencia-item">
                  {c.seccion}: <strong>{c.pct}% de similitud</strong>
                </li>
              ))}
            </ul>
          </div>
          {acciones}
        </DataPanel>

      </div>
    </DashboardLayout>
  )
}
