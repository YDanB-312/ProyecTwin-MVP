import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import '../../assets/styles/pages/gestion-usuarios.css'
import Header from '../../components/Header/Header';
import SidebarAdmin from '../../components/SidebarAdmin/SidebarAdmin';
import FooterAdmin from '../../components/FooterAdmin/FooterAdmin';

export default function GestionUsuarios() {
  return (
    <div className="modulo-admin">
      <GovernmentBar />

      <Header titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12} />

      <SidebarAdmin />

      <main className="contenido-principal">
        <div className="contenedor-gestion">
          <div className="encabezado-pagina">
            <div>
              <h1 className="titulo-pagina"><i className="fas fa-users-cog"></i> Gestion de Usuarios</h1>
              <p className="subtitulo-pagina">Administra los usuarios registrados en la plataforma</p>
            </div>
            <Link to="/admin/nuevo-usuario" className="btn-primario"><i className="fas fa-user-plus"></i> Nuevo Usuario</Link>
          </div>

          <section className="seccion-filtros">
            <h3 className="filtros-titulo"><i className="fas fa-filter"></i> Filtrar Usuarios</h3>
            <div className="contenedor-filtros">
              <div className="grupo-filtro">
                <label htmlFor="filtro-rol">Rol</label>
                <select id="filtro-rol" className="select-filtro" name="filtro_rol">
                  <option value="">Todos los roles</option>
                  <option value="aprendiz">Aprendiz</option>
                  <option value="instructor">Instructor</option>
                  <option value="admin">Admin</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="filtro-estado">Estado</label>
                <select id="filtro-estado" className="select-filtro" name="filtro_estado">
                  <option value="">Todos los estados</option>
                  <option value="activo">Activo</option>
                  <option value="inactivo">Inactivo</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="filtro-busqueda">Buscar por nombre o documento</label>
                <input type="text" id="filtro-busqueda" className="input-filtro" placeholder="Ej: Juan Perez, 1023456789" name="busqueda" />
              </div>
              <div className="grupo-filtro grupo-filtro-botones">
                <button className="btn-primario" type="button"><i className="fas fa-search"></i> Buscar</button>
                <button className="btn-secundario" type="button"><i className="fas fa-eraser"></i> Limpiar</button>
              </div>
            </div>
          </section>

          <section className="seccion-tabla">
            <div className="encabezado-tabla">
              <h3 className="titulo-tabla"><i className="fas fa-users"></i> Lista de Usuarios</h3>
              <span className="total-registros">Mostrando 6 de 150 usuarios</span>
            </div>
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
                  <tr>
                    <td>
                      <div className="info-usuario">
                        <div className="avatar-tabla"><i className="fas fa-user"></i></div>
                        <div>
                          <strong>Ana Martinez Lopez</strong>
                          <span className="telefono-usuario">3235421165</span>
                        </div>
                      </div>
                    </td>
                    <td>1023456789</td>
                    <td>ana.martinez@soy.sena.edu.co</td>
                    <td><span className="badge badge-exito"><i className="fas fa-user-graduate"></i> Aprendiz</span></td>
                    <td><span className="badge badge-exito"><i className="fas fa-circle"></i> Activo</span></td>
                    <td>
                      <div className="acciones-tabla">
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Editar"><i className="fas fa-edit"></i></Link>
                        <button className="btn-accion-tabla btn-ver" title="Desactivar" type="button"><i className="fas fa-ban"></i></button>
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <div className="info-usuario">
                        <div className="avatar-tabla"><i className="fas fa-user"></i></div>
                        <div>
                          <strong>Juan Perez Gomez</strong>
                          <span className="telefono-usuario">3109876543</span>
                        </div>
                      </div>
                    </td>
                    <td>1045678901</td>
                    <td>juan.perez@soy.sena.edu.co</td>
                    <td><span className="badge badge-exito"><i className="fas fa-user-graduate"></i> Aprendiz</span></td>
                    <td><span className="badge badge-neutral"><i className="fas fa-circle"></i> Inactivo</span></td>
                    <td>
                      <div className="acciones-tabla">
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Editar"><i className="fas fa-edit"></i></Link>
                        <button className="btn-accion-tabla btn-ver" title="Activar" type="button"><i className="fas fa-check-circle"></i></button>
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <div className="info-usuario">
                        <div className="avatar-tabla"><i className="fas fa-user-tie"></i></div>
                        <div>
                          <strong>Carlos Rodriguez Diaz</strong>
                          <span className="telefono-usuario">3154567890</span>
                        </div>
                      </div>
                    </td>
                    <td>79876543</td>
                    <td>carlos.rodriguez@sena.edu.co</td>
                    <td><span className="badge badge-advertencia"><i className="fas fa-chalkboard-teacher"></i> Instructor</span></td>
                    <td><span className="badge badge-exito"><i className="fas fa-circle"></i> Activo</span></td>
                    <td>
                      <div className="acciones-tabla">
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Editar"><i className="fas fa-edit"></i></Link>
                        <button className="btn-accion-tabla btn-ver" title="Desactivar" type="button"><i className="fas fa-ban"></i></button>
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <div className="info-usuario">
                        <div className="avatar-tabla"><i className="fas fa-user-tie"></i></div>
                        <div>
                          <strong>Maria Gonzalez Torres</strong>
                          <span className="telefono-usuario">3187654321</span>
                        </div>
                      </div>
                    </td>
                    <td>52345678</td>
                    <td>maria.gonzalez@sena.edu.co</td>
                    <td><span className="badge badge-advertencia"><i className="fas fa-chalkboard-teacher"></i> Instructor</span></td>
                    <td><span className="badge badge-exito"><i className="fas fa-circle"></i> Activo</span></td>
                    <td>
                      <div className="acciones-tabla">
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Editar"><i className="fas fa-edit"></i></Link>
                        <button className="btn-accion-tabla btn-ver" title="Desactivar" type="button"><i className="fas fa-ban"></i></button>
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <div className="info-usuario">
                        <div className="avatar-tabla"><i className="fas fa-user-shield"></i></div>
                        <div>
                          <strong>Diego Munoz Herrera</strong>
                          <span className="telefono-usuario">3201234567</span>
                        </div>
                      </div>
                    </td>
                    <td>80123456</td>
                    <td>diego.munoz@sena.edu.co</td>
                    <td><span className="badge badge-peligro"><i className="fas fa-user-shield"></i> Admin</span></td>
                    <td><span className="badge badge-exito"><i className="fas fa-circle"></i> Activo</span></td>
                    <td>
                      <div className="acciones-tabla">
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Editar"><i className="fas fa-edit"></i></Link>
                        <button className="btn-accion-tabla btn-ver" title="Desactivar" type="button"><i className="fas fa-ban"></i></button>
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <div className="info-usuario">
                        <div className="avatar-tabla"><i className="fas fa-user-shield"></i></div>
                        <div>
                          <strong>Laura Vargas Cote</strong>
                          <span className="telefono-usuario">3112345678</span>
                        </div>
                      </div>
                    </td>
                    <td>52987654</td>
                    <td>laura.vargas@sena.edu.co</td>
                    <td><span className="badge badge-peligro"><i className="fas fa-user-shield"></i> Admin</span></td>
                    <td><span className="badge badge-neutral"><i className="fas fa-circle"></i> Inactivo</span></td>
                    <td>
                      <div className="acciones-tabla">
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Editar"><i className="fas fa-edit"></i></Link>
                        <button className="btn-accion-tabla btn-ver" title="Activar" type="button"><i className="fas fa-check-circle"></i></button>
                        <Link to="/admin/detalle-usuario" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

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
      </main>

      <FooterAdmin />
    </div>
  )
}
