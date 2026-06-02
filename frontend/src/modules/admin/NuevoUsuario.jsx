import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/gestion-usuarios.css'

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/admin/gestion-usuarios', label: 'Gestion Usuarios' },
  { label: 'Nuevo Usuario' },
]

export default function NuevoUsuario() {
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12}>
      <div className="contenedor-pagina">
        <PageHeader
          title="Nuevo Usuario"
          icon="user-plus"
          breadcrumb={breadcrumb}
          actions={<Link to="/admin/gestion-usuarios" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Datos del Usuario" icon="id-card">
          <form className="formulario-proyecto" action="#">
            <div style={{ padding: 'var(--space-xl)' }}>
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
              <div className="acciones-formulario" style={{ marginTop: 'var(--space-lg)' }}>
                <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Usuario</button>
                <Link to="/admin/gestion-usuarios" className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
              </div>
            </div>
          </form>
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
