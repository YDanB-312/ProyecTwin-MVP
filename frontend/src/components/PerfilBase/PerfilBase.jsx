import { useState, useRef, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import DashboardLayout from '../DashboardLayout/DashboardLayout'
import PageHeader from '../PageHeader/PageHeader'
import '../../assets/styles/pages/mi-perfil.css'
import FormField from '../FormField/FormField'

function getInitials(name) {
  if (!name) return '?'
  return name.split(' ').filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase()
}

function getStoredPhoto(role) {
  try { return localStorage.getItem(`profile_photo_${role}`) || null } catch { return null }
}

export default function PerfilBase({
  role, dashboardTitulo, dashboardUsuario, notificaciones,
  breadcrumb,
  userName,
  infoDefaultValues, extraInfoFields,
  securityBannerContent,
  prefsDefaultValues, prefsSectionTitle, prefsContent,
}) {
  const [enviado, setEnviado] = useState(false)
  const [error, setError] = useState(null)
  const [showBanner, setShowBanner] = useState(true)
  const [photo, setPhoto] = useState(() => getStoredPhoto(role))
  const fileInputRef = useRef(null)
  const { register: regInfo, handleSubmit: submitInfo, formState: { errors: errInfo }, reset: resetInfo } = useForm({ defaultValues: infoDefaultValues })
  const { register: regSeg, handleSubmit: submitSeg, formState: { errors: errSeg }, reset: resetSeg, watch: watchSeg } = useForm()
  const { register: regPref, handleSubmit: submitPref, formState: { errors: errPref }, reset: resetPref } = useForm({ defaultValues: prefsDefaultValues })

  useEffect(() => {
    setPhoto(getStoredPhoto(role))
  }, [role])

  const handlePhotoClick = () => fileInputRef.current?.click()

  const handlePhotoChange = (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    if (file.size > 2 * 1024 * 1024) {
      setError('La imagen no puede superar 2 MB.')
      return
    }
    const reader = new FileReader()
    reader.onload = (ev) => {
      const base64 = ev.target.result
      setPhoto(base64)
      try { localStorage.setItem(`profile_photo_${role}`, base64) } catch { /* quota */ }
    }
    reader.readAsDataURL(file)
    e.target.value = ''
  }

  const removePhoto = (e) => {
    e.stopPropagation()
    setPhoto(null)
    try { localStorage.removeItem(`profile_photo_${role}`) } catch { /* ignore */ }
  }

  const initials = getInitials(userName || dashboardUsuario)

  return (
    <DashboardLayout role={role} titulo={dashboardTitulo} usuario={dashboardUsuario} notificaciones={notificaciones}>
      <div className="contenedor-perfil fade-in">
        <PageHeader title="Mi Perfil" icon="user-cog" breadcrumb={breadcrumb} />

        <div className="perfil-card cabecera-card">
          <div className="cabecera-card-content">
            <div className="cabecera-izquierda">
              <div className="perfil-avatar perfil-avatar-editable" onClick={handlePhotoClick} title="Cambiar foto de perfil" role="button" tabIndex={0} onKeyDown={(e) => e.key === 'Enter' && handlePhotoClick()}>
                {photo ? (
                  <img src={photo} alt="Foto de perfil" className="perfil-foto" />
                ) : (
                  <span>{initials}</span>
                )}
                <div className="cambiar-avatar">
                  <i className="fas fa-camera"></i>
                </div>
                {photo && (
                  <button className="eliminar-foto" onClick={removePhoto} title="Eliminar foto" type="button" aria-label="Eliminar foto de perfil">
                    <i className="fas fa-times"></i>
                  </button>
                )}
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handlePhotoChange}
                className="oculto"
                aria-label="Subir foto de perfil"
              />
              <div className="perfil-info">
                <h2 className="perfil-nombre">{userName || dashboardUsuario || 'Usuario'}</h2>
                <span className="perfil-rol">{role === 'admin' ? 'Administrador' : role === 'instructor' ? 'Instructor' : 'Aprendiz'}</span>
                <span className="badge-activo"><i className="fas fa-circle"></i> Activo</span>
              </div>
            </div>
          </div>
        </div>

        <div className="perfil-card">
          <h3 className="card-titulo"><i className="fas fa-user"></i> Información Personal</h3>

          <div className={`mensaje-feedback mensaje-exito ${enviado ? '' : 'oculto'}`}>
            <i className="fas fa-check-circle"></i><span>Perfil actualizado correctamente.</span>
          </div>
          <div className={`mensaje-feedback mensaje-error ${error ? '' : 'oculto'}`}>
            <i className="fas fa-exclamation-circle"></i><span>{error || 'No se pudo actualizar el perfil. Intenta de nuevo.'}</span>
          </div>

          <p className="campos-obligatorios">Los campos marcados con <span className="obligatorio">*</span> son obligatorios.</p>

          <form onSubmit={submitInfo(() => {
            try {
              setError(null)
              setEnviado(true)
              setTimeout(() => setEnviado(false), 3000)
              resetInfo()
            } catch (err) {
              setError('Error al actualizar el perfil.')
            }
          })}>
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
