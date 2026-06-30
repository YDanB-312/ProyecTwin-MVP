import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import FilterBar from '../../components/FilterBar/FilterBar'
import '../../assets/styles/pages/reportes-fallas.css'

const usuarios = [
  { nombre: 'Maria Gonzalez', correo: 'maria.gonzalez@soy.sena.edu.co', rol: 'Aprendiz', rolBadge: 'exito', rolIcon: 'user-graduate', estado: true },
  { nombre: 'Carlos Ruiz', correo: 'carlos.ruiz@sena.edu.co', rol: 'Instructor', rolBadge: 'advertencia', rolIcon: 'chalkboard-teacher', estado: true },
  { nombre: 'Admin Sistema', correo: 'admin@proyectwin.sena.edu.co', rol: 'Admin', rolBadge: 'peligro', rolIcon: 'user-shield', estado: true },
  { nombre: 'Juan Perez', correo: 'juan.perez@soy.sena.edu.co', rol: 'Aprendiz', rolBadge: 'exito', rolIcon: 'user-graduate', estado: false },
  { nombre: 'Ana Martinez', correo: 'ana.martinez@soy.sena.edu.co', rol: 'Aprendiz', rolBadge: 'exito', rolIcon: 'user-graduate', estado: true },
  { nombre: 'Laura Gomez', correo: 'laura.gomez@soy.sena.edu.co', rol: 'Aprendiz', rolBadge: 'exito', rolIcon: 'user-graduate', estado: true },
]

export default function GestionUsuarios() {
  const [filtroRol, setFiltroRol] = useState('')
  const [filtroEstado, setFiltroEstado] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [paginaActual, setPaginaActual] = useState(1)

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
      <div className="contenedor-gestion">
        <PageHeader
          title="Gestión de Usuarios"
          subtitle="Administra los usuarios registrados en la plataforma"
          icon="users-cog"
          actions={<Link to="/admin/nuevo-usuario" className="btn-primario"><i className="fas fa-user-plus"></i> Nuevo Usuario</Link>}
        />

        <FilterBar title="Filtrar Usuarios">
          <div className="grupo-filtro">
            <label htmlFor="filtro-rol">Rol</label>
            <select id="filtro-rol" className="select-filtro" value={filtroRol} onChange={(e) => { setFiltroRol(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los roles</option>
              <option value="aprendiz">Aprendiz</option>
              <option value="instructor">Instructor</option>
              <option value="admin">Admin</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtro-estado">Estado</label>
            <select id="filtro-estado" className="select-filtro" value={filtroEstado} onChange={(e) => { setFiltroEstado(e.target.value); setPaginaActual(1) }}>
              <option value="">Todos los estados</option>
              <option value="activo">Activo</option>
              <option value="inactivo">Inactivo</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtro-busqueda">Buscar por nombre</label>
            <input type="text" id="filtro-busqueda" className="input-filtro" placeholder="Ej: Juan Perez" value={busqueda} onChange={(e) => { setBusqueda(e.target.value); setPaginaActual(1) }} />
          </div>
          <div className="filter-bar-acciones" slot="actions">
            <button className="btn-primario" type="button" onClick={() => {}}><i className="fas fa-search"></i> Buscar</button>
            <button className="btn-secundario" type="button" onClick={() => { setFiltroRol(''); setFiltroEstado(''); setBusqueda(''); setPaginaActual(1) }}><i className="fas fa-eraser"></i> Limpiar</button>
          </div>
        </FilterBar>

        <DataPanel
          title="Lista de Usuarios"
          icon="users"
          action={<span className="total-registros">Mostrando {usuarios.length} de {usuarios.length} usuarios</span>}
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
                {usuarios.map((u, i) => (
                  <tr key={i}>
                    <td>
                      <div className="info-usuario">
                        <div className="avatar-tabla"><i className="fas fa-user"></i></div>
                        <strong>{u.nombre}</strong>
                      </div>
                    </td>
                    <td>{u.correo}</td>
                    <td><span className={`badge badge-${u.rolBadge}`}><i className={`fas fa-${u.rolIcon}`}></i> {u.rol}</span></td>
                    <td><span className={`badge badge-${u.estado ? 'exito' : 'neutral'}`}><i className="fas fa-circle"></i> {u.estado ? 'Activo' : 'Inactivo'}</span></td>
                    <td>
                      <div className="acciones-tabla">
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Editar"><i className="fas fa-edit"></i></Link>
                        <button className="btn-accion-tabla btn-ver" title={u.estado ? 'Desactivar' : 'Activar'} type="button" onClick={() => {}}><i className={`fas fa-${u.estado ? 'ban' : 'check-circle'}`}></i></button>
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="contenedor-paginacion">
            <span className="info-paginacion">Mostrando 1 - 6 de {usuarios.length} usuarios</span>
            <div className="paginacion">
              <button className="btn-paginacion" disabled={paginaActual === 1} type="button" onClick={() => setPaginaActual(prev => Math.max(1, prev - 1))}><i className="fas fa-chevron-left"></i></button>
              <button className={`btn-paginacion${paginaActual === 1 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(1)}>1</button>
              <button className={`btn-paginacion${paginaActual === 2 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(2)}>2</button>
              <button className={`btn-paginacion${paginaActual === 3 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(3)}>3</button>
              <button className="btn-paginacion" type="button" onClick={() => setPaginaActual(4)}>...</button>
              <button className={`btn-paginacion${paginaActual === 15 ? ' activo' : ''}`} type="button" onClick={() => setPaginaActual(15)}>15</button>
              <button className="btn-paginacion" type="button" onClick={() => setPaginaActual(prev => Math.min(15, prev + 1))}><i className="fas fa-chevron-right"></i></button>
            </div>
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
