import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import { useAuth } from '../../contexts/AuthContext'
import PageHeader from '../../components/PageHeader/PageHeader'
import FilterBar from '../../components/FilterBar/FilterBar'
import '../../assets/styles/pages/mis-proyectos.css'
import Pagination from '../../components/Pagination/Pagination'

const ITEMS_PER_PAGE = 3
const meses = { ene: 0, feb: 1, mar: 2, abr: 3, may: 4, jun: 5, jul: 6, ago: 7, sep: 8, oct: 9, nov: 10, dic: 11 }

function parseFecha(fecha) {
  const [d, m, y] = fecha.split(' ')
  return new Date(Number(y), meses[m] ?? 0, Number(d))
}

function filtroPorFecha(fecha, filtro) {
  if (!filtro) return true
  const fechaProyecto = parseFecha(fecha)
  const ahora = new Date()
  if (filtro === 'ultima-semana') return (ahora - fechaProyecto) <= 7 * 24 * 60 * 60 * 1000
  if (filtro === 'ultimo-mes') return (ahora - fechaProyecto) <= 30 * 24 * 60 * 60 * 1000
  return true
}

const proyectosData = [
  { titulo: 'Sistema de Gestion Academica', instructor: 'Carlos Ruiz', estado: 'en_revision', fecha: '15 mar 2026', integrantes: 3, id: 0 },
  { titulo: 'App Movil para Inventarios', instructor: 'Ana Gómez', estado: 'aprobado', fecha: '22 abr 2026', integrantes: 2, id: 1 },
  { titulo: 'Plataforma E-Learning', instructor: 'Miguel Lopez', estado: 'borrador', fecha: '10 may 2026', integrantes: 4, id: 2 },
]

export default function MisProyectos() {
  const { user } = useAuth()
  const [filtroEstado, setFiltroEstado] = useState('')
  const [filtroFecha, setFiltroFecha] = useState('')
  const [filtroInstructor, setFiltroInstructor] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [paginaActual, setPaginaActual] = useState(1)

  const proyectosFiltrados = proyectosData.filter(p => {
    if (filtroEstado && p.estado !== filtroEstado) return false
    if (filtroInstructor && !p.instructor.toLowerCase().includes(filtroInstructor.toLowerCase())) return false
    if (busqueda && !p.titulo.toLowerCase().includes(busqueda.toLowerCase())) return false
    if (!filtroPorFecha(p.fecha, filtroFecha)) return false
    return true
  })

  const proyectosPagina = proyectosFiltrados.slice((paginaActual - 1) * ITEMS_PER_PAGE, paginaActual * ITEMS_PER_PAGE)

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
      <div className="contenedor-proyectos fade-in">

        <PageHeader
          title="Mis proyectos"
          icon="folder-open"
          breadcrumb={[
            { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
            { label: 'Mis proyectos' }
          ]}
          actions={<Link to="/aprendiz/nuevo-proyecto" className="btn-primario"><i className="fas fa-plus"></i> Nuevo Proyecto</Link>}
        />

        <FilterBar title="Filtros de Búsqueda">
          <div className="grupo-filtro">
            <label htmlFor="estado">Estado del proyecto</label>
            <select id="estado" className="campo-select" value={filtroEstado} onChange={(e) => { setFiltroEstado(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los estados</option>
              <option value="borrador">Borrador</option>
              <option value="pendiente">Pendiente</option>
              <option value="en_revision">En revisión</option>
              <option value="aprobado">Aprobado</option>
              <option value="rechazado">Rechazado</option>
              <option value="requiere_ajustes">Requiere Ajustes</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="fecha">Fecha de Creación</label>
            <select id="fecha" className="campo-select" value={filtroFecha} onChange={(e) => { setFiltroFecha(e.target.value); setPaginaActual(1) }}>
              <option value="">Cualquier fecha</option>
              <option value="ultima-semana">Última semana</option>
              <option value="ultimo-mes">Último mes</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="instructor">Instructor</label>
            <select id="instructor" className="campo-select" value={filtroInstructor} onChange={(e) => { setFiltroInstructor(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los instructores</option>
              <option value="ruiz">Carlos Ruiz</option>
              <option value="gomez">Ana Gómez</option>
              <option value="lopez">Miguel Lopez</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="busqueda">Buscar por nombre</label>
            <input type="text" id="busqueda" className="input-filtro" placeholder="Nombre del proyecto..." value={busqueda} onChange={(e) => { setBusqueda(e.target.value); setPaginaActual(1) }} />
          </div>
        </FilterBar>

        <div className="proyectos-grid">
          {proyectosPagina.length === 0 ? (
            <div className="estado-vacio-moderno"><div className="estado-vacio-icono"><i className="fas fa-folder-open"></i></div><h3 className="estado-vacio-titulo">No hay proyectos</h3><p className="estado-vacio-descripcion">No se encontraron proyectos que coincidan con los filtros.</p></div>
          ) : proyectosPagina.map((p) => (
          <div className="proyecto-card" key={p.id}>
            <div className="proyecto-card-accent"></div>
            <div className="proyecto-card-header">
              <div className="proyecto-card-icono"><i className="fas fa-file-alt"></i></div>
              <div className="proyecto-card-info">
                <h3 className="proyecto-card-nombre">{p.titulo}</h3>
                <div className="proyecto-card-instructor">
                  <div className="instructor-avatar">{p.instructor.split(' ').map(n => n[0]).join('')}</div>
                  <span>{p.instructor}</span>
                </div>
              </div>
            </div>
            <div className="proyecto-card-body">
              <div className="proyecto-card-meta"><i className="fas fa-calendar"></i><span>{p.fecha}</span></div>
              <div className="proyecto-card-meta"><i className="fas fa-users"></i><span>{p.integrantes} integrantes</span></div>
            </div>
            <div className="proyecto-card-footer">
              <span className={`estado-badge estado-${p.estado === 'en_revision' ? 'revision' : p.estado}`}>{p.estado === 'en_revision' ? 'En revisión' : p.estado === 'aprobado' ? 'Aprobado' : 'Borrador'}</span>
              <div className="proyecto-card-acciones">
                <Link to={`/aprendiz/detalle-proyecto/${p.id}`} className="btn-icono" title="Ver detalles" aria-label="Ver detalles"><i className="fas fa-eye"></i></Link>
                {p.estado !== 'aprobado' && <Link to={{ pathname: '/aprendiz/nuevo-proyecto', state: { editProyecto: { id: p.id, titulo: p.titulo } } }} className="btn-icono" title="Editar proyecto" aria-label="Editar proyecto"><i className="fas fa-edit"></i></Link>}
              </div>
            </div>
          </div>
          ))}
        </div>

        <Pagination totalItems={proyectosFiltrados.length} itemsPerPage={ITEMS_PER_PAGE} paginaActual={paginaActual} setPaginaActual={setPaginaActual} />
      </div>
    </DashboardLayout>
  )
}


