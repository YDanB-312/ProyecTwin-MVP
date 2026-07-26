import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/gestion-usuarios.css'
import FormField from '../../components/FormField/FormField'

export default function NuevoUsuario() {
  const navigate = useNavigate()
  const location = useLocation()
  const editUser = location.state?.editUser
  const isEditing = !!editUser
  const [enviado, setEnviado] = useState(false)
  const [error, setError] = useState(null)
  const { register, handleSubmit, watch, formState: { errors } } = useForm({ defaultValues: { estado: 'true', ...editUser, estado: editUser ? String(editUser.estado) : 'true' } })
  const rol = watch('rol', '')

  const onSubmit = (data) => {
    setEnviado(true)
    setError(null)
    setTimeout(() => navigate('/admin/gestion-usuarios'), 2000)
  }

  const breadcrumb = [
    { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
    { to: '/admin/gestion-usuarios', label: 'Gestión Usuarios' },
    { label: isEditing ? 'Editar Usuario' : 'Nuevo Usuario' },
  ]

  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
      <div className="contenedor-pagina fade-in">
        <PageHeader
          title={isEditing ? 'Editar Usuario' : 'Nuevo Usuario'}
          icon={isEditing ? 'user-edit' : 'user-plus'}
          breadcrumb={breadcrumb}
          actions={<Link to={isEditing ? '/admin/detalle-usuario' : '/admin/gestion-usuarios'} state={isEditing ? { usuario: editUser } : undefined} className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title={isEditing ? 'Editar Datos del Usuario' : 'Datos del Usuario'} icon="id-card">
            <form className="formulario-proyecto tarjeta-padded" onSubmit={handleSubmit(onSubmit)}>
              <div className="grupo-campos">
                <FormField label="Nombre" htmlFor="nombre" required error={errors.nombre?.message}>
                  <input type="text" id="nombre" className="campo-input" placeholder="Nombres" {...register("nombre", { required: "El nombre es obligatorio" })} />
                </FormField>
                <FormField label="Apellido" htmlFor="apellido" required error={errors.apellido?.message}>
                  <input type="text" id="apellido" className="campo-input" placeholder="Apellidos" {...register("apellido", { required: "El apellido es obligatorio" })} />
                </FormField>
              </div>
              <div className="grupo-campos">
                <FormField label="Correo Electrónico" htmlFor="correo" required error={errors.correo?.message}>
                  <input type="email" id="correo" className="campo-input" placeholder="correo@correo.com" {...register("correo", { required: "El correo es obligatorio" })} />
                </FormField>
                {!isEditing && (
                  <FormField label="Contraseña" htmlFor="password" required error={errors.password?.message}>
                    <input type="password" id="password" className="campo-input" placeholder="Mínimo 6 caracteres" {...register("password", { required: "La contraseña es obligatoria", minLength: { value: 6, message: "Mínimo 6 caracteres" } })} />
                  </FormField>
                )}
              </div>
              <div className="grupo-campos">
                <FormField label="Rol" htmlFor="rol" required error={errors.rol?.message}>
                  <select id="rol" className="campo-select" {...register("rol", { required: "Selecciona un rol" })}>
                    <option value="">Selecciona un rol</option>
                    <option value="aprendiz">Aprendiz</option>
                    <option value="instructor">Instructor</option>
                    <option value="admin">Administrador</option>
                  </select>
                </FormField>
              </div>
              <div className="grupo-campos">
                <FormField label="Estado" htmlFor="estado">
                  <select id="estado" className="campo-select" {...register("estado")}>
                    <option value="true">Activo</option>
                    <option value="false">Inactivo</option>
                  </select>
                </FormField>
              </div>

              {rol === 'instructor' && (
                <div className="seccion-formulario">
                  <div className="seccion-formulario-header">
                    <i className="fas fa-chalkboard-teacher"></i>
                    <h3>Datos del Instructor</h3>
                  </div>
                  <div className="seccion-formulario-body">
                    <div className="grupo-campos">
                      <FormField label="Fecha de Ingreso" htmlFor="fecha_ingreso" required>
                        <input type="date" id="fecha_ingreso" className="campo-input" {...register("fecha_ingreso", { required: true })} />
                      </FormField>
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
                      <FormField label="Ficha" htmlFor="ficha_aprendiz" required>
                        <input type="text" id="ficha_aprendiz" className="campo-input" placeholder="Ej: ADSO-2568" {...register("ficha", { required: true })} />
                      </FormField>
                      <FormField label="Programa de Formación" htmlFor="id_programa" required>
                        <select id="id_programa" className="campo-select" {...register("id_programa", { required: true })}>
                          <option value="">Selecciona un programa</option>
                          <option value="1">ADSO</option>
                          <option value="2">Producción Multimedia</option>
                          <option value="3">Infraestructura de Redes</option>
                        </select>
                      </FormField>
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
                      <FormField label="Área Encargada" htmlFor="area_encargada" required>
                        <input type="text" id="area_encargada" className="campo-input" placeholder="Ej: Gestión Académica" {...register("area_encargada", { required: true })} />
                      </FormField>
                    </div>
                  </div>
                </div>
              )}

              <div className={`mensaje-feedback mensaje-exito ${enviado ? '' : 'oculto'}`}>
                <i className="fas fa-check-circle"></i>
                <span>Usuario registrado correctamente.</span>
              </div>
              <div className={`mensaje-feedback mensaje-error ${error ? '' : 'oculto'}`}>
                <i className="fas fa-exclamation-circle"></i>
                <span>No se pudo registrar el usuario. Verifica los datos e intenta de nuevo.</span>
              </div>
              <div className="acciones-formulario mt-20">
                <button type="submit" className="btn-primario"><i className="fas fa-save"></i> {isEditing ? 'Actualizar Usuario' : 'Guardar Usuario'}</button>
                <Link to={isEditing ? '/admin/detalle-usuario' : '/admin/gestion-usuarios'} state={isEditing ? { usuario: editUser } : undefined} className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
              </div>
          </form>
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
