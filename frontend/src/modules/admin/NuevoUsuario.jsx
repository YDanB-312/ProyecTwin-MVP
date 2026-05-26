import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import Header from '../../components/Header/Header';
import SidebarAdmin from '../../components/SidebarAdmin/SidebarAdmin';
import FooterAdmin from '../../components/FooterAdmin/FooterAdmin';
import '../../assets/styles/pages/gestion-usuarios.css'

export default function NuevoUsuario() {
  return (
    <div className="modulo-admin">
      <GovernmentBar />

      <Header titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12} />

      <SidebarAdmin />

      <main className="contenido-principal">
        <div className="contenedor-pagina">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-user-plus"></i> Nuevo Usuario</h1>
            <Link to="/admin/gestion-usuarios" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>

          <div className="tarjeta tarjeta-padded">
            <form className="formulario-proyecto" action="#">
              <div className="grupo-campos">
                <div className="grupo-formulario">
                  <label htmlFor="nombre" className="etiqueta requerido">Nombre</label>
                  <input type="text" id="nombre" className="input-text" placeholder="Nombres" required name="nombre" />
                </div>
                <div className="grupo-formulario">
                  <label htmlFor="apellido" className="etiqueta requerido">Apellido</label>
                  <input type="text" id="apellido" className="input-text" placeholder="Apellidos" required name="apellido" />
                </div>
              </div>
              <div className="grupo-campos">
                <div className="grupo-formulario">
                  <label htmlFor="correo" className="etiqueta requerido">Correo Electronico</label>
                  <input type="email" id="correo" className="input-text" placeholder="correo@correo.com" required name="correo" />
                </div>
                <div className="grupo-formulario">
                  <label htmlFor="password" className="etiqueta requerido">Contrasena</label>
                  <input type="password" id="password" className="input-text" placeholder="Minimo 6 caracteres" minLength="6" required name="password" />
                </div>
              </div>
              <div className="grupo-campos">
                <div className="grupo-formulario">
                  <label htmlFor="rol" className="etiqueta requerido">Rol</label>
                  <select id="rol" className="select" required name="rol">
                    <option value="">Selecciona un rol</option>
                    <option value="aprendiz">Aprendiz</option>
                    <option value="instructor">Instructor</option>
                    <option value="admin">Administrador</option>
                  </select>
                </div>
                <div className="grupo-formulario">
                  <label htmlFor="estado" className="etiqueta">Estado</label>
                  <select id="estado" className="select" name="estado">
                    <option value="activo">Activo</option>
                    <option value="inactivo">Inactivo</option>
                  </select>
                </div>
              </div>
              <div className="acciones-formulario">
                <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Usuario</button>
                <Link to="/admin/gestion-usuarios" className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
              </div>
            </form>
          </div>
        </div>
      </main>

      <FooterAdmin />
    </div>
  )
}
