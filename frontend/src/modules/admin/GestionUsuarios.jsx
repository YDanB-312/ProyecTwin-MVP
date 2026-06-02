import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import FilterBar from '../../components/FilterBar/FilterBar'

const usuarios = [
  { nombre: 'Ana Martinez Lopez', telefono: '3235421165', documento: '1023456789', correo: 'ana.martinez@soy.sena.edu.co', rol: 'Aprendiz', rolBadge: 'exito', rolIcon: 'user-graduate', estado: 'Activo', estadoBadge: 'exito' },
  { nombre: 'Juan Perez Gomez', telefono: '3109876543', documento: '1045678901', correo: 'juan.perez@soy.sena.edu.co', rol: 'Aprendiz', rolBadge: 'exito', rolIcon: 'user-graduate', estado: 'Inactivo', estadoBadge: 'neutral' },
  { nombre: 'Carlos Rodriguez Diaz', telefono: '3154567890', documento: '79876543', correo: 'carlos.rodriguez@sena.edu.co', rol: 'Instructor', rolBadge: 'advertencia', rolIcon: 'chalkboard-teacher', estado: 'Activo', estadoBadge: 'exito' },
  { nombre: 'Maria Gonzalez Torres', telefono: '3187654321', documento: '52345678', correo: 'maria.gonzalez@sena.edu.co', rol: 'Instructor', rolBadge: 'advertencia', rolIcon: 'chalkboard-teacher', estado: 'Activo', estadoBadge: 'exito' },
  { nombre: 'Diego Munoz Herrera', telefono: '3201234567', documento: '80123456', correo: 'diego.munoz@sena.edu.co', rol: 'Admin', rolBadge: 'peligro', rolIcon: 'user-shield', estado: 'Activo', estadoBadge: 'exito' },
  { nombre: 'Laura Vargas Cote', telefono: '3112345678', documento: '52987654', correo: 'laura.vargas@sena.edu.co', rol: 'Admin', rolBadge: 'peligro', rolIcon: 'user-shield', estado: 'Inactivo', estadoBadge: 'neutral' },
]

export default function GestionUsuarios() {
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12}>
      <div className="contenedor-gestion">
        <PageHeader
          title="Gestion de Usuarios"
          subtitle="Administra los usuarios registrados en la plataforma"
          icon="users-cog"
          actions={<Link to="/admin/nuevo-usuario" className="btn-primario"><i className="fas fa-user-plus"></i> Nuevo Usuario</Link>}
        />

        <FilterBar title="Filtrar Usuarios">
          <div className="grupo-filtro">
            <label htmlFor="filtro-rol">Rol</label>
            <select id="filtro-rol" className="select-filtro">
              <option value="">Todos los roles</option>
              <option value="aprendiz">Aprendiz</option>
              <option value="instructor">Instructor</option>
              <option value="admin">Admin</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtro-estado">Estado</label>
            <select id="filtro-estado" className="select-filtro">
              <option value="">Todos los estados</option>
              <option value="activo">Activo</option>
              <option value="inactivo">Inactivo</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="filtro-busqueda">Buscar por nombre o documento</label>
            <input type="text" id="filtro-busqueda" className="input-filtro" placeholder="Ej: Juan Perez, 1023456789" />
          </div>
          <div className="filter-bar-acciones" slot="actions">
            <button className="btn-primario" type="button"><i className="fas fa-search"></i> Buscar</button>
            <button className="btn-secundario" type="button"><i className="fas fa-eraser"></i> Limpiar</button>
          </div>
        </FilterBar>

        <DataPanel
          title="Lista de Usuarios"
          icon="users"
          action={<span className="total-registros">Mostrando 6 de 150 usuarios</span>}
        >
          <div className="contenedor-tabla">
            <table className="tabla-usuarios">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Documento</th>
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
                        <div>
                          <strong>{u.nombre}</strong>
                          <span className="telefono-usuario">{u.telefono}</span>
                        </div>
                      </div>
                    </td>
                    <td>{u.documento}</td>
                    <td>{u.correo}</td>
                    <td><span className={`badge badge-${u.rolBadge}`}><i className={`fas fa-${u.rolIcon}`}></i> {u.rol}</span></td>
                    <td><span className={`badge badge-${u.estadoBadge}`}><i className="fas fa-circle"></i> {u.estado}</span></td>
                    <td>
                      <div className="acciones-tabla">
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Editar"><i className="fas fa-edit"></i></Link>
                        <button className="btn-accion-tabla btn-ver" title={u.estado === 'Activo' ? 'Desactivar' : 'Activar'} type="button"><i className={`fas fa-${u.estado === 'Activo' ? 'ban' : 'check-circle'}`}></i></button>
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </DataPanel>

        <div className="paginacion">
          <button className="btn-paginacion" disabled type="button"><i className="fas fa-chevron-left"></i></button>
          <button className="btn-paginacion activa" type="button">1</button>
          <button className="btn-paginacion" type="button">2</button>
          <button className="btn-paginacion" type="button">3</button>
          <button className="btn-paginacion" type="button">...</button>
          <button className="btn-paginacion" type="button">15</button>
          <button className="btn-paginacion" type="button"><i className="fas fa-chevron-right"></i></button>
        </div>
      </div>
    </DashboardLayout>
  )
}
