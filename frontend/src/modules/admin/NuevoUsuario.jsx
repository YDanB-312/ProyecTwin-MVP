import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/gestion-usuarios.css'

export default function NuevoUsuario() {
  const location = useLocation()
  const editUser = location.state?.editUser
  const isEditing = !!editUser
  const [rol, setRol] = useState(editUser?.rol || '')

  const breadcrumb = [
    { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
    { to: '/admin/gestion-usuarios', label: 'Gestión Usuarios' },
    { label: isEditing ? 'Editar Usuario' : 'Nuevo Usuario' },
  ]

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
      <div className="contenedor-pagina">
        <PageHeader
          title={isEditing ? 'Editar Usuario' : 'Nuevo Usuario'}
          icon={isEditing ? 'user-edit' : 'user-plus'}
          breadcrumb={breadcrumb}
          actions={<Link to={isEditing ? '/admin/detalle-usuario' : '/admin/gestion-usuarios'} className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title={isEditing ? 'Editar Datos del Usuario' : 'Datos del Usuario'} icon="id-card">
            <form className="formulario-proyecto tarjeta-padded" action="#" onSubmit={(e) => e.preventDefault()}>
              <div className="grupo-campos">
                <div className="grupo-formulario">
                  <label htmlFor="nombre" className="etiqueta requerido">Nombre</label>
                  <input type="text" id="nombre" className="input-text" placeholder="Nombres" required name="nombre" defaultValue={editUser?.nombre || ''} />
                </div>
                <div className="grupo-formulario">
                  <label htmlFor="apellido" className="etiqueta requerido">Apellido</label>
                  <input type="text" id="apellido" className="input-text" placeholder="Apellidos" required name="apellido" defaultValue={editUser?.apellido || ''} />
                </div>
              </div>
              <div className="grupo-campos">
                <div className="grupo-formulario">
                  <label htmlFor="correo" className="etiqueta requerido">Correo Electrónico</label>
                  <input type="email" id="correo" className="input-text" placeholder="correo@correo.com" required name="correo" defaultValue={editUser?.correo || ''} />
                </div>
                <div className="grupo-formulario">
                  {!isEditing && (
                    <>
                      <label htmlFor="password" className="etiqueta requerido">Contraseña</label>
                      <input type="password" id="password" className="input-text" placeholder="Mínimo 6 caracteres" minLength="6" required name="password" />
                    </>
                  )}
                </div>
              </div>
              <div className="grupo-campos">
                <div className="grupo-formulario">
                  <label htmlFor="rol" className="etiqueta requerido">Rol</label>
                  <select id="rol" className="select" required name="rol" value={rol} onChange={e => setRol(e.target.value)}>
                    <option value="">Selecciona un rol</option>
                    <option value="aprendiz">Aprendiz</option>
                    <option value="instructor">Instructor</option>
                    <option value="admin">Administrador</option>
                  </select>
                </div>
              </div>
              <div className="grupo-campos">
                <div className="grupo-formulario">
                  <label htmlFor="estado" className="etiqueta">Estado</label>
                  <select id="estado" className="select" name="estado" defaultValue={editUser?.estado === undefined ? 'true' : String(editUser.estado)}>
                    <option value="true">Activo</option>
                    <option value="false">Inactivo</option>
                  </select>
                </div>
              </div>

              {rol === 'instructor' && (
                <div className="seccion-formulario">
                  <div className="seccion-formulario-header">
                    <i className="fas fa-chalkboard-teacher"></i>
                    <h3>Datos del Instructor</h3>
                  </div>
                  <div className="seccion-formulario-body">
                    <div className="grupo-campos">
                      <div className="grupo-formulario">
                        <label htmlFor="codigo_instructor" className="etiqueta requerido">Código de Instructor</label>
                        <input type="text" id="codigo_instructor" className="input-text" placeholder="Ej: INS-001" required name="codigo_instructor" defaultValue={editUser?.codigo_instructor || ''} />
                      </div>
                      <div className="grupo-formulario">
                        <label htmlFor="especialidad" className="etiqueta requerido">Especialidad</label>
                        <input type="text" id="especialidad" className="input-text" placeholder="Ej: Desarrollo de Software" required name="especialidad" defaultValue={editUser?.especialidad || ''} />
                      </div>
                    </div>
                    <div className="grupo-campos">
                      <div className="grupo-formulario">
                        <label htmlFor="centro_formacion_instructor" className="etiqueta requerido">Centro de Formación</label>
                        <input type="text" id="centro_formacion_instructor" className="input-text" placeholder="Centro de formación al que pertenece" required name="centro_formacion" defaultValue={editUser?.centro_formacion || ''} />
                      </div>
                      <div className="grupo-formulario">
                        <label htmlFor="fecha_ingreso" className="etiqueta requerido">Fecha de Ingreso</label>
                        <input type="date" id="fecha_ingreso" className="input-text" required name="fecha_ingreso" defaultValue={editUser?.fecha_ingreso || ''} />
                      </div>
                    </div>
                    <div className="grupo-formulario">
                      <label htmlFor="biografia_profesional" className="etiqueta">Biografía Profesional</label>
                      <textarea id="biografia_profesional" className="textarea" placeholder=" formación académica y experiencia profesional..." name="biografia_profesional" defaultValue={editUser?.biografia_profesional || ''}></textarea>
                    </div>
                  </div>
                </div>
              )}

              {rol === 'aprendiz' && (
                <div className="seccion-formulario">
                  <div className="seccion-formulario-header">
                    <i className="fas fa-user-graduate"></i>
                    <h3>Datos del Aprendiz</h3>
                  </div>
                  <div className="seccion-formulario-body">
                    <div className="grupo-campos">
                      <div className="grupo-formulario">
                        <label htmlFor="ficha_aprendiz" className="etiqueta requerido">Ficha</label>
                        <input type="text" id="ficha_aprendiz" className="input-text" placeholder="Ej: ADSO-2568" required name="ficha" defaultValue={editUser?.ficha || ''} />
                      </div>
                      <div className="grupo-formulario">
                        <label htmlFor="id_programa" className="etiqueta requerido">Programa de Formación</label>
                        <select id="id_programa" className="select" required name="id_programa" defaultValue={editUser?.id_programa || ''}>
                          <option value="">Selecciona un programa</option>
                          <option value="1">ADSO</option>
                          <option value="2">Producción Multimedia</option>
                          <option value="3">Infraestructura de Redes</option>
                        </select>
                      </div>
                    </div>
                    <div className="grupo-formulario">
                      <label htmlFor="centro_formacion_aprendiz" className="etiqueta">Centro de Formación</label>
                      <input type="text" id="centro_formacion_aprendiz" className="input-text" placeholder="Centro de formación" name="centro_formacion" defaultValue={editUser?.centro_formacion || ''} />
                    </div>
                  </div>
                </div>
              )}

              {rol === 'admin' && (
                <div className="seccion-formulario">
                  <div className="seccion-formulario-header">
                    <i className="fas fa-user-shield"></i>
                    <h3>Datos del Administrador</h3>
                  </div>
                  <div className="seccion-formulario-body">
                    <div className="grupo-campos">
                      <div className="grupo-formulario">
                        <label htmlFor="area_encargada" className="etiqueta requerido">Área Encargada</label>
                        <input type="text" id="area_encargada" className="input-text" placeholder="Ej: Gestión Académica" required name="area_encargada" defaultValue={editUser?.area_encargada || ''} />
                      </div>
                    </div>
                  </div>
                </div>
              )}

              <div className="acciones-formulario mt-20">
                <button type="submit" className="btn-primario"><i className="fas fa-save"></i> {isEditing ? 'Actualizar Usuario' : 'Guardar Usuario'}</button>
                <Link to={isEditing ? '/admin/detalle-usuario' : '/admin/gestion-usuarios'} className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
              </div>
          </form>
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
