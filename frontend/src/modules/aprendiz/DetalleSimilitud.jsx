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

const breadcrumb = [
  { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Detalle Similitud' },
]

const similitudesProyecto = [
  { proyecto: 'Plataforma Educativa SENA', porcentaje: 65, aprendiz: 'Ana Martínez', programa: 'ADSO', fecha: '02/02/2026' },
  { proyecto: 'Plataforma de Cursos Online', porcentaje: 55, aprendiz: 'Pedro Lopez', programa: 'ADSO', fecha: '10/01/2026' },
]

function DetalleSimilitud() {
  const location = useLocation()
  const origen = location.state?.desde
  const proyectoActual = location.state?.proyecto || 'Sistema de Gestion Academica'
  const rutaVolver = origen === 'resultado-analisis' ? '/aprendiz/mis-proyectos' : '/aprendiz/alertas'
  const textoVolver = origen === 'resultado-analisis' ? 'Volver a Mis proyectos' : 'Volver a Notificaciones'
  const [actual, setActual] = useState(0)
  const sim = similitudesProyecto[actual]

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-pagina">
        <PageHeader
          title="Detalle de Similitud"
          icon="exclamation-triangle"
          breadcrumb={breadcrumb}
          actions={<Link to={rutaVolver} className="btn-secundario"><i className="fas fa-arrow-left"></i> {textoVolver}</Link>}
        />

        <div className="tarjeta-bienvenida-moderna banner-similitud">
          <div className="banner-similitud-content">
            <div className="banner-similitud-text">
              Tu proyecto <strong>{proyectoActual}</strong> tiene <strong>{similitudesProyecto.length} similitudes</strong> detectadas.
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

        <DataPanel title="Comparacion de Proyectos" icon="balance-scale">
          <div className="similitud-encabezado">
            <div className="encabezado-revision">
              <div className="info-revision">
                <div className="similitud-meta">
                  <span><i className="fas fa-calendar"></i> Detectado: 25/04/2026</span>
                  <span><i className="fas fa-percent"></i> Similitud: <strong className={sim.porcentaje > 70 ? 'texto-peligro' : sim.porcentaje >= 40 ? 'texto-advertencia' : 'texto-exito'}>{sim.porcentaje}%</strong></span>
                </div>
              </div>
              <span className={`badge ${sim.porcentaje > 70 ? 'badge-peligro' : sim.porcentaje >= 40 ? 'badge-advertencia' : 'badge-primario'}`}><i className="fas fa-exclamation-circle"></i> {sim.porcentaje > 70 ? 'Crítico' : sim.porcentaje >= 40 ? 'Moderado' : 'Bajo'}</span>
            </div>
          </div>
          <div className="detalle-grid similitud-proyectos-grid">
            <div className="card-proyecto-compacto">
              <h4 className="card-titulo-verde"><i className="fas fa-file-alt"></i> Mi Proyecto</h4>
              <p className="info-linea">{proyectoActual}</p>
              <p className="detalle-linea"><strong>Aprendiz:</strong> Maria Gonzalez</p>
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
              {coincidencias.map((c, i) => (
                <li key={i} className="coincidencia-item">
                  {c.seccion}: <strong>{c.pct}% de similitud</strong>
                </li>
              ))}
            </ul>
          </div>
        </DataPanel>

        <div className="margen-superior">
          <Link to={rutaVolver} className="btn-secundario"><i className="fas fa-arrow-left"></i> {textoVolver}</Link>
        </div>
      </div>
    </DashboardLayout>
  )
}

export default DetalleSimilitud
