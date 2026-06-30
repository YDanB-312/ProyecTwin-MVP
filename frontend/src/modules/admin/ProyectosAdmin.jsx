import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import FilterBar from '../../components/FilterBar/FilterBar'
import '../../assets/styles/pages/reportes-fallas.css'

const etiquetaEstado = {
  borrador: 'Borrador',
  pendiente: 'Pendiente',
  en_revision: 'En Revisión',
  aprobado: 'Aprobado',
  rechazado: 'Rechazado',
  requiere_ajustes: 'Requiere Ajustes',
}

const badgePorEstado = {
  borrador: 'primario',
  pendiente: 'advertencia',
  en_revision: 'advertencia',
  aprobado: 'exito',
  rechazado: 'peligro',
  requiere_ajustes: 'advertencia',
}

const proyectos = [
  { titulo: 'Sistema IoT para Agricultura', programa: 'ADSO', instructor: 'Carlos Ruiz', estado: 'aprobado', estudiantes: 3, fecha: '15/03/2026' },
  { titulo: 'Plataforma Educativa SENA', programa: 'ADSO', instructor: 'Maria Torres', estado: 'en_revision', estudiantes: 4, fecha: '02/02/2026' },
  { titulo: 'App de Reciclaje Inteligente', programa: 'Producción Multimedia', instructor: 'Andres Lopez', estado: 'borrador', estudiantes: 2, fecha: '20/04/2026' },
  { titulo: 'Red de Sensores Ambientales', programa: 'Infraestructura Redes', instructor: 'Pedro Jimenez', estado: 'aprobado', estudiantes: 3, fecha: '10/01/2026' },
  { titulo: 'Sistema de Gestión Académica', programa: 'ADSO', instructor: 'Carlos Ruiz', estado: 'en_revision', estudiantes: 5, fecha: '12/03/2026' },
  { titulo: 'Portal de Empleo Digital', programa: 'ADSO', instructor: 'Maria Torres', estado: 'borrador', estudiantes: 2, fecha: '05/05/2026' },
]

export default function ProyectosAdmin() {
  const [filtroPrograma, setFiltroPrograma] = useState('')
  const [filtroEstado, setFiltroEstado] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [paginaActual, setPaginaActual] = useState(1)

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
      <div className="contenedor-gestion">
        <PageHeader title="Proyectos" icon="folder-open" subtitle="Consulta el listado completo de proyectos registrados en la plataforma" />

        <FilterBar title="Filtrar Proyectos">
          <div className="grupo-filtro">
            <label htmlFor="filtro-programa">Programa</label>
            <select id="filtro-programa" className="select-filtro" value={filtroPrograma} onChange={(e) => { setFiltroPrograma(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los programas</option>
              <option value="adso">ADSO</option>
              <option value="multimedia">Producción Multimedia</option>
              <option value="redes">Infraestructura Redes</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtro-estado-p">Estado</label>
            <select id="filtro-estado-p" className="select-filtro" value={filtroEstado} onChange={(e) => { setFiltroEstado(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los estados</option>
              <option value="borrador">Borrador</option>
              <option value="pendiente">Pendiente</option>
              <option value="en_revision">En Revisión</option>
              <option value="aprobado">Aprobado</option>
              <option value="rechazado">Rechazado</option>
              <option value="requiere_ajustes">Requiere Ajustes</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtro-busqueda-p">Buscar proyecto</label>
            <input type="text" id="filtro-busqueda-p" className="input-filtro" placeholder="Nombre del proyecto" value={busqueda} onChange={(e) => { setBusqueda(e.target.value); setPaginaActual(1) }} />
          </div>
          <div className="filter-bar-acciones" slot="actions">
            <button className="btn-primario" type="button" onClick={() => {}}><i className="fas fa-search"></i> Buscar</button>
            <button className="btn-secundario" type="button" onClick={() => { setFiltroPrograma(''); setFiltroEstado(''); setBusqueda(''); setPaginaActual(1) }}><i className="fas fa-eraser"></i> Limpiar</button>
          </div>
        </FilterBar>

        <DataPanel
          title="Listado de Proyectos"
          icon="list"
          action={<span className="total-registros">Mostrando {proyectos.length} de {proyectos.length} proyectos</span>}
        >
          <div className="contenedor-tabla">
            <table className="tabla-reportes">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Programa</th>
                  <th>Instructor</th>
                  <th>Estado</th>
                  <th>Integrantes</th>
                  <th>Fecha</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {proyectos.map((p, i) => (
                  <tr key={i}>
                    <td><strong>{p.titulo}</strong></td>
                    <td>{p.programa}</td>
                    <td>{p.instructor}</td>
                    <td><span className={`badge badge-${badgePorEstado[p.estado]}`}>{etiquetaEstado[p.estado]}</span></td>
                    <td>{p.estudiantes}</td>
                    <td>{p.fecha}</td>
                    <td>
                      <div className="acciones-tabla">
                        <Link to="/admin/detalle-proyecto" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="contenedor-paginacion">
            <span className="info-paginacion">Mostrando 1 - 6 de {proyectos.length} proyectos</span>
            <div className="paginacion">
              <button className="btn-paginacion" disabled={paginaActual === 1} type="button" onClick={() => setPaginaActual(prev => Math.max(1, prev - 1))}><i className="fas fa-chevron-left"></i></button>
              <button className={`btn-paginacion${paginaActual === 1 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(1)}>1</button>
              <button className={`btn-paginacion${paginaActual === 2 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(2)}>2</button>
              <button className={`btn-paginacion${paginaActual === 3 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(3)}>3</button>
              <button className={`btn-paginacion${paginaActual === 4 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(4)}>4</button>
              <button className={`btn-paginacion${paginaActual === 5 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(5)}>5</button>
              <button className="btn-paginacion" type="button" onClick={() => setPaginaActual(6)}>...</button>
              <button className={`btn-paginacion${paginaActual === 8 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(8)}>8</button>
              <button className="btn-paginacion" type="button" onClick={() => setPaginaActual(prev => Math.min(8, prev + 1))}><i className="fas fa-chevron-right"></i></button>
            </div>
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
