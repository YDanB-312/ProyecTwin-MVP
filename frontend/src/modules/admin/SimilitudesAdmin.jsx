import { useState, Fragment } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import FilterBar from '../../components/FilterBar/FilterBar'
import '../../assets/styles/pages/admin-tablas.css'

function nivelSimilitud(pct) {
  if (pct > 70) return { texto: 'Crítico', badge: 'peligro' }
  if (pct >= 40) return { texto: 'Moderado', badge: 'advertencia' }
  return { texto: 'Bajo', badge: 'primario' }
}

const etiquetaEstado = {
  pendiente: 'Pendiente',
  revisada: 'Revisada',
  resuelta: 'Resuelta',
}

const badgeEstado = {
  pendiente: 'advertencia',
  revisada: 'primario',
  resuelta: 'exito',
}

const paresSimilitud = [
  { proyecto1: 'Sistema de Gestión Académica', proyecto2: 'Plataforma Educativa SENA', porcentaje: 65, estado: 'pendiente', fecha: '25/04/2026' },
  { proyecto1: 'App de Reciclaje Inteligente', proyecto2: 'EcoApp Ciudadana', porcentaje: 48, estado: 'revisada', fecha: '22/04/2026' },
  { proyecto1: 'Red de Sensores Ambientales', proyecto2: 'Monitor Clima Urbano', porcentaje: 35, estado: 'resuelta', fecha: '20/04/2026' },
  { proyecto1: 'Portal de Empleo Digital', proyecto2: 'Bolsa de Trabajo SENA', porcentaje: 72, estado: 'pendiente', fecha: '18/04/2026' },
  { proyecto1: 'Sistema de Inventarios', proyecto2: 'App de Control Stock', porcentaje: 25, estado: 'revisada', fecha: '15/04/2026' },
  { proyecto1: 'Plataforma de Cursos Online', proyecto2: 'Sistema de Gestión Académica', porcentaje: 55, estado: 'pendiente', fecha: '10/04/2026' },
]

function agruparPorProyecto(pares) {
  const mapa = {}
  for (const p of pares) {
    for (const lado of ['proyecto1', 'proyecto2']) {
      const otro = lado === 'proyecto1' ? 'proyecto2' : 'proyecto1'
      const nombre = p[lado]
      if (!mapa[nombre]) {
        mapa[nombre] = { nombre, similitudes: [] }
      }
      mapa[nombre].similitudes.push({
        proyecto: p[otro],
        porcentaje: p.porcentaje,
        estado: p.estado,
        fecha: p.fecha,
        nivel: nivelSimilitud(p.porcentaje),
      })
    }
  }
  return Object.values(mapa).map(proy => {
    const pcts = proy.similitudes.map(s => s.porcentaje)
    const mayor = Math.max(...pcts)
    const estados = proy.similitudes.map(s => s.estado)
    const prioridad = { pendiente: 0, revisada: 1, resuelta: 2 }
    const peorEstado = estados.sort((a, b) => prioridad[a] - prioridad[b])[0]
    return {
      ...proy,
      totalSimilitudes: proy.similitudes.length,
      mayorPorcentaje: mayor,
      nivelGeneral: nivelSimilitud(mayor),
      estadoGeneral: peorEstado,
    }
  })
}

export default function SimilitudesAdmin() {
  const [expandidos, setExpandidos] = useState({})
  const [filtroNivel, setFiltroNivel] = useState('')
  const [filtroEstadoS, setFiltroEstadoS] = useState('')
  const [busquedaS, setBusquedaS] = useState('')
  const todosProyectos = agruparPorProyecto(paresSimilitud)
  const proyectos = todosProyectos.filter(p => {
    if (filtroNivel && p.nivelGeneral.texto.toLowerCase() !== filtroNivel) return false
    if (filtroEstadoS && p.estadoGeneral !== filtroEstadoS) return false
    if (busquedaS && !p.nombre.toLowerCase().includes(busquedaS.toLowerCase())) return false
    return true
  })
  const totalSimilitudes = paresSimilitud.length

  function toggleExpandir(nombre) {
    setExpandidos(prev => ({ ...prev, [nombre]: !prev[nombre] }))
  }

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
        <div className="contenedor-gestion fade-in">
        <PageHeader title="Similitudes" icon="search" subtitle="Listado de proyectos con similitudes detectadas" />

        <FilterBar title="Filtrar Similitudes" actions={<>
            <button className="btn-primario" type="button" onClick={() => { setBusquedaS(busquedaS) }}><i className="fas fa-search"></i> Buscar</button>
            <button className="btn-secundario" type="button" onClick={() => { setFiltroNivel(''); setFiltroEstadoS(''); setBusquedaS('') }}><i className="fas fa-eraser"></i> Limpiar</button>
          </>}>
          <div className="grupo-filtro">
            <label htmlFor="filtro-nivel">Nivel de Similitud</label>
            <select id="filtro-nivel" className="campo-select" value={filtroNivel} onChange={(e) => setFiltroNivel(e.target.value)}>
              <option value="">Todos los niveles</option>
              <option value="crítico">Crítico (&gt;70%)</option>
              <option value="moderado">Moderado (40-70%)</option>
              <option value="bajo">Bajo (&lt;40%)</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtro-estado-s">Estado</label>
            <select id="filtro-estado-s" className="campo-select" value={filtroEstadoS} onChange={(e) => setFiltroEstadoS(e.target.value)}>
              <option value="">Todos</option>
              <option value="pendiente">Pendiente</option>
              <option value="revisada">Revisada</option>
              <option value="resuelta">Resuelta</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtro-busqueda-s">Buscar proyecto</label>
            <input type="text" id="filtro-busqueda-s" className="input-filtro" placeholder="Nombre del proyecto" value={busquedaS} onChange={(e) => setBusquedaS(e.target.value)} />
          </div>
        </FilterBar>

        <DataPanel
          title="Listado de Similitudes"
          icon="list"
          action={<span className="total-registros">{proyectos.length} proyectos con {totalSimilitudes} similitudes</span>}
        >
          <div className="contenedor-tabla">
            <table className="tabla-reportes">
              <thead>
                <tr>
                  <th></th>
                  <th>Proyecto</th>
                  <th>Similitudes</th>
                  <th>Mayor %</th>
                  <th>Nivel</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {proyectos.map((proy, i) => (
                  <Fragment key={i}>
                    <tr key={i} className="fila-proyecto" onClick={() => toggleExpandir(proy.nombre)}>
                      <td>
                        <i className={`fas fa-chevron-${expandidos[proy.nombre] ? 'down' : 'right'}`}></i>
                      </td>
                      <td><strong>{proy.nombre}</strong></td>
                      <td>{proy.totalSimilitudes} similitud{proy.totalSimilitudes !== 1 ? 'es' : ''}</td>
                      <td><strong className={`texto-${proy.nivelGeneral.badge === 'primario' ? 'exito' : proy.nivelGeneral.badge}`}>{proy.mayorPorcentaje}%</strong></td>
                      <td><span className={`badge badge-${proy.nivelGeneral.badge}`}>{proy.nivelGeneral.texto}</span></td>
                      <td><span className={`badge badge-${badgeEstado[proy.estadoGeneral]}`}>{etiquetaEstado[proy.estadoGeneral]}</span></td>
                      <td>
                        <div className="acciones-tabla">
                          <Link to={{ pathname: '/admin/detalle-similitud', state: { proyecto: proy.nombre } }} className="btn-accion-tabla btn-ver" title="Ver detalle" aria-label="Ver detalle"><i className="fas fa-eye"></i></Link>
                        </div>
                      </td>
                    </tr>
                    {expandidos[proy.nombre] && proy.similitudes.map((sim, j) => (
                      <tr key={`${i}-${j}`} className="fila-similitud">
                        <td></td>
                        <td className="celda-similitud">
                          <i className="fas fa-arrow-right icono-similitud"></i>
                          {sim.proyecto}
                        </td>
                        <td></td>
                        <td><strong className={`texto-${sim.nivel.badge === 'primario' ? 'exito' : sim.nivel.badge}`}>{sim.porcentaje}%</strong></td>
                        <td><span className={`badge badge-${sim.nivel.badge}`}>{sim.nivel.texto}</span></td>
                        <td><span className={`badge badge-${badgeEstado[sim.estado]}`}>{etiquetaEstado[sim.estado]}</span></td>
                        <td>{sim.fecha}</td>
                      </tr>
                    ))}
                  </Fragment>
                ))}
              </tbody>
            </table>
          </div>
          <div className="contenedor-paginacion">
            <span className="info-paginacion">Mostrando {proyectos.length} de {proyectos.length} proyectos</span>
            <div className="paginacion">
              <button className="btn-paginacion" type="button" disabled aria-label="Página anterior"><i className="fas fa-chevron-left"></i></button>
              <button className="btn-paginacion activo" type="button">1</button>
              <button className="btn-paginacion" type="button" disabled aria-label="Página siguiente"><i className="fas fa-chevron-right"></i></button>
            </div>
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
