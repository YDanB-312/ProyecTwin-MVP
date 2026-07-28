import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import { useAuth } from '../../contexts/AuthContext'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/gestion-fichas.css'
import FilterBar from '../../components/FilterBar/FilterBar'

const fichas = [
  { codigo: 'ADSO-2568', nombre: 'Analisis y Desarrollo 2568', programa: 'ADSO', aprendices: 28, proyectos: 5, estado: true },
  { codigo: 'ADSO-2634', nombre: 'Analisis y Desarrollo 2634', programa: 'ADSO', aprendices: 25, proyectos: 3, estado: true },
  { codigo: 'MM-3102', nombre: 'Produccion Multimedia 3102', programa: 'Produccion Multimedia', aprendices: 22, proyectos: 4, estado: true },
  { codigo: 'IR-2801', nombre: 'Infraestructura Redes 2801', programa: 'Infraestructura Redes', aprendices: 20, proyectos: 0, estado: false },
]

export default function GestionarFichas() {
  const { user } = useAuth()
  const [filtroPrograma, setFiltroPrograma] = useState('')
  const [filtroEstado, setFiltroEstado] = useState('')
  const [busqueda, setBusqueda] = useState('')

  const limpiarFiltros = () => {
    setFiltroPrograma('')
    setFiltroEstado('')
    setBusqueda('')
  }

  const fichasFiltradas = fichas.filter(f => {
    if (filtroPrograma && f.programa.toLowerCase() !== filtroPrograma) return false
    if (filtroEstado === 'activo' && !f.estado) return false
    if (filtroEstado === 'inactivo' && f.estado) return false
    if (busqueda && !f.nombre.toLowerCase().includes(busqueda.toLowerCase()) && !f.codigo.toLowerCase().includes(busqueda.toLowerCase())) return false
    return true
  })

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
      <div className="gestion-fichas-container fade-in">
        <div className="contenedor-revision">

          <PageHeader title="Gestión de Fichas" icon="users" breadcrumb={[ { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' }, { label: 'Gestión de Fichas' } ]} actions={<Link to="/instructor/crear-ficha" className="btn-primario"><i className="fas fa-plus"></i> Nueva Ficha</Link>} />

          <FilterBar title="Filtrar Fichas">
            <div className="grupo-filtro">
              <label htmlFor="programa-ficha" className="filtro-label">Programa</label>
              <select id="programa-ficha" className="campo-select" name="programa_ficha" value={filtroPrograma} onChange={(e) => setFiltroPrograma(e.target.value)}>
                <option value="">Todos los programas</option>
                <option value="adso">ADSO</option>
                <option value="produccion multimedia">Producción Multimedia</option>
                <option value="infraestructura redes">Infraestructura Redes</option>
              </select>
            </div>
            <div className="grupo-filtro">
              <label htmlFor="estado-ficha" className="filtro-label">Estado</label>
              <select id="estado-ficha" className="campo-select" name="estado_ficha" value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
                <option value="">Todos</option>
                <option value="activo">Activas</option>
                <option value="inactivo">Inactivas</option>
              </select>
            </div>
            <div className="grupo-filtro">
              <label htmlFor="buscar-ficha" className="filtro-label">Buscar</label>
              <div className="input-con-icono">
                <i className="fas fa-search"></i>
                <input type="text" id="buscar-ficha" className="filtro-input" placeholder="Buscar ficha..." name="buscar" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} />
              </div>
            </div>
            <button className="btn-limpiar" type="button" onClick={limpiarFiltros}><i className="fas fa-times"></i> Limpiar</button>
          </FilterBar>

          <div className="tabla-container">
            <table className="tabla-premium">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Nombre</th>
                  <th>Programa</th>
                  <th>Aprendices</th>
                  <th>Proyectos</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {fichasFiltradas.length === 0 ? (
                  <tr><td colSpan="7" className="texto-centro texto-claro">No hay fichas que coincidan con los filtros.</td></tr>
                ) : fichasFiltradas.map((f) => (
                  <tr key={f.codigo}>
                    <td><span className="codigo-cell">{f.codigo}</span></td>
                    <td className="nombre-cell">{f.nombre}</td>
                    <td>{f.programa}</td>
                    <td>{f.aprendices}</td>
                    <td>{f.proyectos}</td>
                    <td><span className={`badge ${f.estado ? 'badge-exito' : 'badge-neutral'}`}>{f.estado ? 'Activa' : 'Inactiva'}</span></td>
                    <td>
                      <div className="acciones-celda">
                        <Link to={`/instructor/detalle-ficha/${f.codigo}`} state={{ ficha: f }} className="btn-ghost-tabla" aria-label="Ver ficha"><i className="fas fa-eye"></i></Link>
                        <Link to={`/instructor/directorio-ficha/${f.codigo}`} state={{ ficha: f }} className="btn-ghost-tabla" aria-label="Ver directorio"><i className="fas fa-users"></i></Link>
                        <Link to="/instructor/crear-ficha" state={{ editFicha: f }} className="btn-ghost-tabla" aria-label="Editar ficha"><i className="fas fa-edit"></i></Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

        </div>
      </div>
    </DashboardLayout>
  )
}


