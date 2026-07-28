import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import { useAuth } from '../../contexts/AuthContext'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import FilterBar from '../../components/FilterBar/FilterBar'
import '../../assets/styles/pages/admin-tablas.css'
import Pagination from '../../components/Pagination/Pagination'

const etiquetaReporte = {
  pendiente: 'Pendiente',
  en_revision: 'En Revisión',
  resuelto: 'Resuelto',
  rechazado: 'Rechazado',
}

const badgeReporte = {
  pendiente: { clase: 'advertencia', icono: 'clock' },
  en_revision: { clase: 'primario', icono: 'cog' },
  resuelto: { clase: 'exito', icono: 'check' },
  rechazado: { clase: 'neutral', icono: 'lock' },
}

const reportes = [
  { id: 1, usuario: 'Carlos Rodríguez Díaz', descripcion: 'Error al cargar la página de Dashboard, muestra pantalla blanca después de iniciar sesión', estado: 'pendiente', fecha: '12/04/2026' },
  { id: 2, usuario: 'Maria González Torres', descripcion: 'No se pueden subir archivos PDF en la sección de evidencias del proyecto', estado: 'pendiente', fecha: '11/04/2026' },
  { id: 3, usuario: 'Andrés Martínez López', descripcion: 'El sistema no envía Notificaciones cuando un instructor revisa un proyecto', estado: 'en_revision', fecha: '10/04/2026' },
  { id: 4, usuario: 'Laura Sánchez Pérez', descripcion: 'El botón de Cerrar sesión no funciona correctamente en navegador Chrome', estado: 'en_revision', fecha: '09/04/2026' },
  { id: 5, usuario: 'Diego Ramírez Castro', descripcion: 'Error en la generación de reportes PDF, el archivo descargado está corrupto', estado: 'resuelto', fecha: '08/04/2026' },
  { id: 6, usuario: 'Patricia Morales Vega', descripcion: 'Las imágenes de los logos de proyectos no se muestran en la vista de lista', estado: 'rechazado', fecha: '05/04/2026' },
]

const ITEMS_PER_PAGE = 4
const cicloEstado = { pendiente: 'en_revision', en_revision: 'resuelto', resuelto: 'resuelto', rechazado: 'rechazado' }

export default function ReportesFallas() {
  const { user } = useAuth()
  const [reportesLocal, setReportesLocal] = useState(reportes)
  const [filtroEstado, setFiltroEstado] = useState('')
  const [filtroFecha, setFiltroFecha] = useState('')
  const [filtroUsuario, setFiltroUsuario] = useState('')
  const [paginaActual, setPaginaActual] = useState(1)

  const cambiarEstado = (id) => {
    if (!window.confirm('¿Estás seguro de cambiar el estado?')) return
    setReportesLocal(prev => prev.map(r => r.id === id ? { ...r, estado: cicloEstado[r.estado] || r.estado } : r))
  }

  const reportesFiltrados = reportesLocal.filter(r => {
    if (filtroEstado && r.estado !== filtroEstado) return false
    if (filtroUsuario && !r.usuario.toLowerCase().includes(filtroUsuario.toLowerCase())) return false
    if (filtroFecha) {
      const [d, m, y] = r.fecha.split('/')
      const reporteFecha = `${y}-${m.padStart(2, '0')}-${d.padStart(2, '0')}`
      if (reporteFecha !== filtroFecha) return false
    }
    return true
  })



  const reportesPagina = reportesFiltrados.slice((paginaActual - 1) * ITEMS_PER_PAGE, paginaActual * ITEMS_PER_PAGE)

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
      <div className="contenedor-gestion fade-in">
        <PageHeader title="Reportes de Fallas" icon="bug" breadcrumb={[{ to: '/admin/dashboard', icon: 'home', label: 'Inicio' }, { label: 'Reportes de Fallas' }]} />

        <FilterBar title="Filtros de Búsqueda" actions={<>
            <button className="btn-primario" type="button" onClick={() => setPaginaActual(1)}><i className="fas fa-search"></i> Buscar</button>
            <button className="btn-secundario" type="button" onClick={() => { setFiltroEstado(''); setFiltroFecha(''); setFiltroUsuario(''); setPaginaActual(1) }}><i className="fas fa-eraser"></i> Limpiar</button>
          </>}>
          <div className="grupo-filtro">
            <label htmlFor="filtroEstado">Estado</label>
            <select id="filtroEstado" className="campo-select" value={filtroEstado} onChange={(e) => { setFiltroEstado(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los estados</option>
              <option value="pendiente">Pendiente</option>
              <option value="en_revision">En Revisión</option>
              <option value="resuelto">Resuelto</option>
              <option value="rechazado">Rechazado</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtroFecha">Fecha</label>
            <input type="date" id="filtroFecha" className="input-filtro" value={filtroFecha} onChange={(e) => { setFiltroFecha(e.target.value); setPaginaActual(1) }} />
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtroUsuario">Buscar por usuario</label>
            <input type="text" id="filtroUsuario" className="input-filtro" placeholder="Nombre o correo del usuario" value={filtroUsuario} onChange={(e) => { setFiltroUsuario(e.target.value); setPaginaActual(1) }} />
          </div>
        </FilterBar>

        <DataPanel
          title="Listado de reportes"
          icon="list-alt"
          action={<span className="total-registros">Mostrando {reportesFiltrados.length} de {reportes.length} reportes</span>}
        >
          <div className="contenedor-tabla">
            <table className="tabla-reportes">
            <thead>
              <tr>
                <th>ID</th>
                <th>Usuario</th>
                <th>Descripción</th>
                <th>Estado</th>
                <th>Fecha</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {reportesPagina.map((r, i) => (
                <tr key={r.id}>
                  <td>{r.id}</td>
                  <td>{r.usuario}</td>
                  <td className="descripcion-corta" title={r.descripcion}>{r.descripcion}</td>
                  <td><span className={`badge badge-${badgeReporte[r.estado].clase}`}><i className={`fas fa-${badgeReporte[r.estado].icono}`}></i> {etiquetaReporte[r.estado]}</span></td>
                  <td>{r.fecha}</td>
                  <td>
                    <div className="acciones-tabla">
                      <Link to={`/admin/detalle-reporte/${r.id}`} state={{ reporte: r }} className="btn-accion-tabla btn-ver" title="Ver detalle" aria-label="Ver detalle del reporte"><i className="fas fa-eye"></i></Link>
                      <button className="btn-accion-tabla btn-ver" title="Cambiar estado" aria-label="Cambiar estado del reporte" type="button" onClick={() => cambiarEstado(r.id)}><i className="fas fa-exchange-alt"></i></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination totalItems={reportesFiltrados.length} itemsPerPage={ITEMS_PER_PAGE} paginaActual={paginaActual} setPaginaActual={setPaginaActual} itemName="reportes" showInfo={true} filteredCount={reportesPagina.length} />
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
