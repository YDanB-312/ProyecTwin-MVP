import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import FilterBar from '../../components/FilterBar/FilterBar'
import '../../assets/styles/pages/reportes-fallas.css'

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

export default function ReportesFallas() {
  const [filtroEstado, setFiltroEstado] = useState('')
  const [filtroFecha, setFiltroFecha] = useState('')
  const [filtroUsuario, setFiltroUsuario] = useState('')
  const [paginaActual, setPaginaActual] = useState(1)

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
      <div className="contenedor-gestion">
        <PageHeader title="Reportes de Fallas" icon="bug" />

        <FilterBar title="Filtros de Búsqueda">
          <div className="grupo-filtro">
            <label htmlFor="filtroEstado">Estado</label>
            <select id="filtroEstado" className="select-filtro" name="filtro_estado" value={filtroEstado} onChange={(e) => { setFiltroEstado(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los estados</option>
              <option value="pendiente">Pendiente</option>
              <option value="en_revision">En Revisión</option>
              <option value="resuelto">Resuelto</option>
              <option value="rechazado">Rechazado</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtroFecha">Fecha</label>
            <input type="date" id="filtroFecha" className="input-filtro" name="filtro_fecha" value={filtroFecha} onChange={(e) => { setFiltroFecha(e.target.value); setPaginaActual(1) }} />
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtroUsuario">Buscar por usuario</label>
            <input type="text" id="filtroUsuario" className="input-filtro" placeholder="Nombre o correo del usuario" name="filtro_usuario" value={filtroUsuario} onChange={(e) => { setFiltroUsuario(e.target.value); setPaginaActual(1) }} />
          </div>
          <div className="filter-bar-acciones" slot="actions">
            <button className="btn-primario" type="button" onClick={() => {}}><i className="fas fa-search"></i> Buscar</button>
            <button className="btn-secundario" type="button" onClick={() => { setFiltroEstado(''); setFiltroFecha(''); setFiltroUsuario(''); setPaginaActual(1) }}><i className="fas fa-eraser"></i> Limpiar</button>
          </div>
        </FilterBar>

        <DataPanel
          title="Listado de reportes"
          icon="list-alt"
          action={<span className="total-registros">Mostrando {reportes.length} de {reportes.length} reportes</span>}
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
              {reportes.map((r, i) => (
                <tr key={i}>
                  <td>{r.id}</td>
                  <td>{r.usuario}</td>
                  <td className="descripcion-corta" title={r.descripcion}>{r.descripcion}</td>
                  <td><span className={`badge badge-${badgeReporte[r.estado].clase}`}><i className={`fas fa-${badgeReporte[r.estado].icono}`}></i> {etiquetaReporte[r.estado]}</span></td>
                  <td>{r.fecha}</td>
                  <td>
                    <div className="acciones-tabla">
                      <Link to="/admin/detalle-reporte" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      <button className="btn-accion-tabla btn-ver" title="Cambiar estado" type="button" onClick={() => {}}><i className="fas fa-exchange-alt"></i></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="contenedor-paginacion">
            <span className="info-paginacion">Mostrando 1 - 6 de {reportes.length} reportes</span>
            <div className="paginacion">
              <button className="btn-paginacion" disabled={paginaActual === 1} title="Anterior" type="button" onClick={() => setPaginaActual(prev => Math.max(1, prev - 1))}><i className="fas fa-chevron-left"></i></button>
              <button className={`btn-paginacion${paginaActual === 1 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(1)}>1</button>
              <button className={`btn-paginacion${paginaActual === 2 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(2)}>2</button>
              <button className={`btn-paginacion${paginaActual === 3 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(3)}>3</button>
              <button className={`btn-paginacion${paginaActual === 4 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(4)}>4</button>
              <button className={`btn-paginacion${paginaActual === 5 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(5)}>5</button>
              <button className={`btn-paginacion${paginaActual === 6 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(6)}>6</button>
              <button className={`btn-paginacion${paginaActual === 7 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(7)}>7</button>
              <button className="btn-paginacion" title="Siguiente" type="button" onClick={() => setPaginaActual(prev => Math.min(7, prev + 1))}><i className="fas fa-chevron-right"></i></button>
            </div>
          </div>
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
