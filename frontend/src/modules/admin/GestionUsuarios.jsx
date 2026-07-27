import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import FilterBar from '../../components/FilterBar/FilterBar'
import '../../assets/styles/pages/admin-tablas.css'
import Pagination from '../../components/Pagination/Pagination'

const usuarios = [
  { nombre: 'Maria', apellido: 'Gonzalez', correo: 'maria.gonzalez@soy.sena.edu.co', rol: 'Aprendiz', rolBadge: 'exito', rolIcon: 'user-graduate', estado: true },
  { nombre: 'Carlos', apellido: 'Ruiz', correo: 'carlos.ruiz@sena.edu.co', rol: 'Instructor', rolBadge: 'advertencia', rolIcon: 'chalkboard-teacher', estado: true },
  { nombre: 'Admin', apellido: 'Sistema', correo: 'admin@proyectwin.sena.edu.co', rol: 'Admin', rolBadge: 'peligro', rolIcon: 'user-shield', estado: true },
  { nombre: 'Juan', apellido: 'Pérez', correo: 'juan.perez@soy.sena.edu.co', rol: 'Aprendiz', rolBadge: 'exito', rolIcon: 'user-graduate', estado: false },
  { nombre: 'Ana', apellido: 'Martínez', correo: 'ana.martinez@soy.sena.edu.co', rol: 'Aprendiz', rolBadge: 'exito', rolIcon: 'user-graduate', estado: true },
  { nombre: 'Laura', apellido: 'Gómez', correo: 'laura.gomez@soy.sena.edu.co', rol: 'Aprendiz', rolBadge: 'exito', rolIcon: 'user-graduate', estado: true },
]

const ITEMS_PER_PAGE = 4

export default function GestionUsuarios() {
  const [filtroRol, setFiltroRol] = useState('')
  const [filtroEstado, setFiltroEstado] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [paginaActual, setPaginaActual] = useState(1)
  const [usuariosLocal, setUsuariosLocal] = useState(usuarios)

  const toggleEstado = (index) => {
    setUsuariosLocal(prev => prev.map((u, i) => i === index ? { ...u, estado: !u.estado } : u))
  }

  const usuariosFiltrados = usuariosLocal.filter(u => {
    if (filtroRol && u.rol.toLowerCase() !== filtroRol) return false
    if (filtroEstado === 'activo' && !u.estado) return false
    if (filtroEstado === 'inactivo' && u.estado) return false
    if (busqueda && !`${u.nombre} ${u.apellido}`.toLowerCase().includes(busqueda.toLowerCase())) return false
    return true
  })



  const usuariosPagina = usuariosFiltrados.slice((paginaActual - 1) * ITEMS_PER_PAGE, paginaActual * ITEMS_PER_PAGE)

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
        <div className="contenedor-gestion fade-in">
        <PageHeader
          title="Gestión de Usuarios"
          subtitle="Administra los usuarios registrados en la plataforma"
          icon="users-cog"
          breadcrumb={[{ to: '/admin/dashboard', icon: 'home', label: 'Inicio' }, { label: 'Gestión de Usuarios' }]}
          actions={<Link to="/admin/nuevo-usuario" className="btn-primario"><i className="fas fa-user-plus"></i> Nuevo Usuario</Link>}
        />

        <FilterBar title="Filtrar Usuarios" actions={<>
            <button className="btn-primario" type="button" onClick={() => setPaginaActual(1)}><i className="fas fa-search"></i> Buscar</button>
            <button className="btn-secundario" type="button" onClick={() => { setFiltroRol(''); setFiltroEstado(''); setBusqueda(''); setPaginaActual(1) }}><i className="fas fa-eraser"></i> Limpiar</button>
          </>}>
          <div className="grupo-filtro">
            <label htmlFor="filtro-rol">Rol</label>
            <select id="filtro-rol" className="campo-select" value={filtroRol} onChange={(e) => { setFiltroRol(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los roles</option>
              <option value="aprendiz">Aprendiz</option>
              <option value="instructor">Instructor</option>
              <option value="admin">Admin</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtro-estado">Estado</label>
            <select id="filtro-estado" className="campo-select" value={filtroEstado} onChange={(e) => { setFiltroEstado(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los estados</option>
              <option value="activo">Activo</option>
              <option value="inactivo">Inactivo</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtro-busqueda">Buscar por nombre</label>
            <input type="text" id="filtro-busqueda" className="input-filtro" placeholder="Ej: Juan Pérez" value={busqueda} onChange={(e) => { setBusqueda(e.target.value); setPaginaActual(1) }} />
          </div>
        </FilterBar>

        <DataPanel
          title="Lista de Usuarios"
          icon="users"
          action={<span className="total-registros">Mostrando {usuariosFiltrados.length} de {usuarios.length} usuarios</span>}
        >
          <div className="contenedor-tabla">
            <table className="tabla-reportes">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Correo</th>
                  <th>Rol</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {usuariosPagina.map((u) => (
                  <tr key={u.correo}>
                    <td>
                      <div className="info-usuario">
                        <div className="avatar-tabla"><i className="fas fa-user"></i></div>
                        <strong>{u.nombre} {u.apellido}</strong>
                      </div>
                    </td>
                    <td>{u.correo}</td>
                    <td><span className={`badge badge-${u.rolBadge}`}><i className={`fas fa-${u.rolIcon}`}></i> {u.rol}</span></td>
                    <td><span className={`badge badge-${u.estado ? 'exito' : 'neutral'}`}><i className="fas fa-circle"></i> {u.estado ? 'Activo' : 'Inactivo'}</span></td>
                    <td>
                      <div className="acciones-tabla">
                        <Link to="/admin/detalle-usuario" state={{ usuario: u }} className="btn-accion-tabla btn-ver" title="Editar" aria-label="Editar usuario"><i className="fas fa-edit"></i></Link>
                        <button className="btn-accion-tabla btn-ver" title={u.estado ? 'Desactivar' : 'Activar'} aria-label={u.estado ? 'Desactivar usuario' : 'Activar usuario'} type="button" onClick={() => toggleEstado(usuariosLocal.findIndex(x => x.nombre === u.nombre))}><i className={`fas fa-${u.estado ? 'ban' : 'check-circle'}`}></i></button>
                        <Link to="/admin/detalle-usuario" state={{ usuario: u }} className="btn-accion-tabla btn-ver" title="Ver detalle" aria-label="Ver detalle de usuario"><i className="fas fa-eye"></i></Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination totalItems={usuariosFiltrados.length} itemsPerPage={ITEMS_PER_PAGE} paginaActual={paginaActual} setPaginaActual={setPaginaActual} itemName="usuarios" showInfo={true} filteredCount={usuariosFiltrados.length} />
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
