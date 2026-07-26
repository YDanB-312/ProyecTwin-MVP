import { useState } from 'react'
import { useForm } from 'react-hook-form'
import DashboardLayout from '../DashboardLayout/DashboardLayout'
import PageHeader from '../PageHeader/PageHeader'
import '../../assets/styles/pages/mi-perfil.css'
import FormField from '../FormField/FormField'

export default function PerfilBase({
  role, dashboardTitulo, dashboardUsuario, notificaciones,
  breadcrumb,
  avatarContent,
  infoDefaultValues, extraInfoFields,
  securityBannerContent,
  prefsDefaultValues, prefsSectionTitle, prefsContent,
}) {
  const [enviado, setEnviado] = useState(false)
  const [error, setError] = useState(null)
  const [showBanner, setShowBanner] = useState(true)
  const { register: regInfo, handleSubmit: submitInfo, formState: { errors: errInfo }, reset: resetInfo } = useForm({ defaultValues: infoDefaultValues })
  const { register: regSeg, handleSubmit: submitSeg, formState: { errors: errSeg }, reset: resetSeg, watch: watchSeg } = useForm()
  const { register: regPref, handleSubmit: submitPref, formState: { errors: errPref }, reset: resetPref } = useForm({ defaultValues: prefsDefaultValues })

  return (
    <DashboardLayout role={role} titulo={dashboardTitulo} usuario={dashboardUsuario} notificaciones={notificaciones}>
      <div className="contenedor-perfil fade-in">
        <PageHeader title="Mi Perfil" icon="user-cog" breadcrumb={breadcrumb} />

        <div className="perfil-card cabecera-card">
          {avatarContent}
        </div>

        <div className="perfil-card">
          <h3 className="card-titulo"><i className="fas fa-user"></i> Información Personal</h3>

          <div className={`mensaje-feedback mensaje-exito ${enviado ? '' : 'oculto'}`}>
            <i className="fas fa-check-circle"></i><span>Perfil actualizado correctamente.</span>
          </div>
          <div className={`mensaje-feedback mensaje-error ${error ? '' : 'oculto'}`}>
            <i className="fas fa-exclamation-circle"></i><span>No se pudo actualizar el perfil. Intenta de nuevo.</span>
          </div>

          <p className="campos-obligatorios">Los campos marcados con <span className="obligatorio">*</span> son obligatorios.</p>

          <form onSubmit={submitInfo(() => { setEnviado(true); setError(null); resetInfo() })}>
            <div className="form-grid">
              <FormField label="Nombre" htmlFor="nombre" required error={errInfo.nombre && 'El nombre es obligatorio'}>
                <input type="text" id="nombre" className="campo-input" {...regInfo("nombre", { required: true })} />
              </FormField>
              <FormField label="Apellido" htmlFor="apellido" required error={errInfo.apellido && 'El apellido es obligatorio'}>
                <input type="text" id="apellido" className="campo-input" {...regInfo("apellido", { required: true })} />
              </FormField>
              <FormField label="Correo Electrónico" htmlFor="correo" required error={errInfo.correo && 'Correo inválido'} helpText="Usa tu correo institucional del SENA">
                <input type="email" id="correo" className="campo-input" {...regInfo("correo", { required: true, pattern: /^[^@\s]+@[^@\s]+\.[^@\s]+$/ })} />
              </FormField>
              {typeof extraInfoFields === 'function' ? extraInfoFields(regInfo, errInfo) : extraInfoFields}
            </div>
            <div className="form-botones">
              <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Cambios</button>
            </div>
          </form>
        </div>

        <div className="perfil-card">
          <h3 className="card-titulo"><i className="fas fa-shield-alt"></i> Seguridad y Acceso</h3>

          {showBanner && (
            <div className="banner-advertencia banner-dismissible">
              {typeof securityBannerContent === 'function' ? securityBannerContent() : (
                <>
                  <i className="fas fa-exclamation-triangle banner-icono"></i>
                  <div className="banner-texto">
                    {securityBannerContent}
                  </div>
                </>
              )}
              <button className="btn-cerrar-banner" type="button" onClick={() => setShowBanner(false)} aria-label="Cerrar aviso">
                <i className="fas fa-times"></i>
              </button>
            </div>
          )}

          <form onSubmit={submitSeg(() => { resetSeg() })}>
            <div className="cambio-password">
              <FormField label="Contraseña Actual" htmlFor="contrasena-actual" required error={errSeg.passActual && 'Campo obligatorio'}>
                <input type="password" id="contrasena-actual" className="campo-input" {...regSeg("passActual", { required: true })} />
              </FormField>
              <FormField label="Nueva Contraseña" htmlFor="nueva-contrasena" required error={errSeg.passNueva && 'Mínimo 8 caracteres'} helpText="Mínimo 8 caracteres, incluyendo mayúsculas, minúsculas y números">
                <input type="password" id="nueva-contrasena" className="campo-input" {...regSeg("passNueva", { required: true, minLength: 8 })} />
              </FormField>
              <FormField label="Confirmar Nueva Contraseña" htmlFor="confirmar-contrasena" required error={errSeg.passConfirmar && (errSeg.passConfirmar.message || "Campo obligatorio")}>
                <input type="password" id="confirmar-contrasena" className="campo-input" {...regSeg("passConfirmar", { required: true, validate: value => value === watchSeg("passNueva") || "Las contraseñas no coinciden" })} />
              </FormField>
            </div>
            <div className="form-botones">
              <button type="submit" className="btn-primario"><i className="fas fa-key"></i> Actualizar contraseña</button>
            </div>
          </form>
        </div>

        <div className="perfil-card">
          <h3 className="card-titulo"><i className="fas fa-cog"></i> {prefsSectionTitle}</h3>
          <form onSubmit={submitPref(() => { resetPref() })}>
            {typeof prefsContent === 'function' ? prefsContent(regPref, errPref) : prefsContent}
            <div className="form-botones">
              <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Preferencias</button>
            </div>
          </form>
        </div>

      </div>
    </DashboardLayout>
  )
}
