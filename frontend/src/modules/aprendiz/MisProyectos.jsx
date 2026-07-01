import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import FilterBar from '../../components/FilterBar/FilterBar'
import '../../assets/styles/pages/mis-proyectos.css'

function MisProyectos() {
  const [filtroEstado, setFiltroEstado] = useState('')
  const [filtroFecha, setFiltroFecha] = useState('')
  const [filtroInstructor, setFiltroInstructor] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [paginaActual, setPaginaActual] = useState(1)

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-proyectos">

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
            <select id="estado" className="select-filtro" value={filtroEstado} onChange={(e) => { setFiltroEstado(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los estados</option>
              <option value="borrador">Borrador</option>
              <option value="en_revision">En revisión</option>
              <option value="aprobado">Aprobado</option>
              <option value="rechazado">Rechazado</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="fecha">Fecha de Creación</label>
            <select id="fecha" className="select-filtro" value={filtroFecha} onChange={(e) => { setFiltroFecha(e.target.value); setPaginaActual(1) }}>
              <option value="">Cualquier fecha</option>
              <option value="ultima-semana">Última semana</option>
              <option value="ultimo-mes">Último mes</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="instructor">Instructor</label>
            <select id="instructor" className="select-filtro" value={filtroInstructor} onChange={(e) => { setFiltroInstructor(e.target.value); setPaginaActual(1) }}>
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
          <div className="proyecto-card">
            <div className="proyecto-card-accent"></div>
            <div className="proyecto-card-header">
              <div className="proyecto-card-icono"><i className="fas fa-file-alt"></i></div>
              <div className="proyecto-card-info">
                <h3 className="proyecto-card-nombre">Sistema de Gestion Academica</h3>
                <div className="proyecto-card-instructor">
                  <div className="instructor-avatar">CR</div>
                  <span>Carlos Ruiz</span>
                </div>
              </div>
            </div>
            <div className="proyecto-card-body">
              <div className="proyecto-card-meta">
                <i className="fas fa-calendar"></i>
                <span>15 mar 2026</span>
              </div>
              <div className="proyecto-card-meta">
                <i className="fas fa-users"></i>
                <span>3 integrantes</span>
              </div>
            </div>
            <div className="proyecto-card-footer">
              <span className="estado-badge estado-revision">En revisión</span>
              <div className="proyecto-card-acciones">
                <Link to="/aprendiz/detalle-proyecto/0" className="btn-icono" title="Ver detalles"><i className="fas fa-eye"></i></Link>
                <Link to={{ pathname: '/aprendiz/nuevo-proyecto', state: { editProyecto: { id: 0, nombre: 'Sistema de Gestion Academica' } } }} className="btn-icono" title="Editar proyecto"><i className="fas fa-edit"></i></Link>
              </div>
            </div>
          </div>

          <div className="proyecto-card">
            <div className="proyecto-card-accent"></div>
            <div className="proyecto-card-header">
              <div className="proyecto-card-icono"><i className="fas fa-file-alt"></i></div>
              <div className="proyecto-card-info">
                <h3 className="proyecto-card-nombre">App Movil para Inventarios</h3>
                <div className="proyecto-card-instructor">
                  <div className="instructor-avatar">AG</div>
                  <span>Ana Gómez</span>
                </div>
              </div>
            </div>
            <div className="proyecto-card-body">
              <div className="proyecto-card-meta">
                <i className="fas fa-calendar"></i>
                <span>22 abr 2026</span>
              </div>
              <div className="proyecto-card-meta">
                <i className="fas fa-users"></i>
                <span>2 integrantes</span>
              </div>
            </div>
            <div className="proyecto-card-footer">
              <span className="estado-badge estado-aprobado">Aprobado</span>
              <div className="proyecto-card-acciones">
                <Link to="/aprendiz/detalle-proyecto/1" className="btn-icono" title="Ver detalles"><i className="fas fa-eye"></i></Link>
              </div>
            </div>
          </div>

          <div className="proyecto-card">
            <div className="proyecto-card-accent"></div>
            <div className="proyecto-card-header">
              <div className="proyecto-card-icono"><i className="fas fa-file-alt"></i></div>
              <div className="proyecto-card-info">
                <h3 className="proyecto-card-nombre">Plataforma E-Learning</h3>
                <div className="proyecto-card-instructor">
                  <div className="instructor-avatar">ML</div>
                  <span>Miguel Lopez</span>
                </div>
              </div>
            </div>
            <div className="proyecto-card-body">
              <div className="proyecto-card-meta">
                <i className="fas fa-calendar"></i>
                <span>10 may 2026</span>
              </div>
              <div className="proyecto-card-meta">
                <i className="fas fa-users"></i>
                <span>4 integrantes</span>
              </div>
            </div>
            <div className="proyecto-card-footer">
              <span className="estado-badge estado-borrador">Borrador</span>
              <div className="proyecto-card-acciones">
                <Link to="/aprendiz/detalle-proyecto/2" className="btn-icono" title="Ver detalles"><i className="fas fa-eye"></i></Link>
                <Link to={{ pathname: '/aprendiz/nuevo-proyecto', state: { editProyecto: { id: 2, nombre: 'Plataforma E-Learning' } } }} className="btn-icono" title="Editar proyecto"><i className="fas fa-edit"></i></Link>
              </div>
            </div>
          </div>
        </div>

        <div className="paginacion">
          <button className="btn-paginacion" disabled={paginaActual === 1} type="button" onClick={() => setPaginaActual(prev => Math.max(1, prev - 1))}><i className="fas fa-chevron-left"></i></button>
          <button className={`btn-paginacion${paginaActual === 1 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(1)}>1</button>
          <button className={`btn-paginacion${paginaActual === 2 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(2)}>2</button>
          <button className={`btn-paginacion${paginaActual === 3 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(3)}>3</button>
          <button className="btn-paginacion" type="button" onClick={() => setPaginaActual(prev => Math.min(3, prev + 1))}><i className="fas fa-chevron-right"></i></button>
        </div>
      </div>
    </DashboardLayout>
  )
}

export default MisProyectos
