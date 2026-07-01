import { useForm } from 'react-hook-form'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/mi-perfil.css'

function MiPerfil() {
  const { register: regInfo, handleSubmit: submitInfo, formState: { errors: errInfo }, reset: resetInfo } = useForm({
    defaultValues: { nombre: 'Maria', apellido: 'Gonzalez', correo: 'maria.gonzalez@soy.sena.edu.co', id_programa: '1' }
  })
  const { register: regSeg, handleSubmit: submitSeg, formState: { errors: errSeg }, reset: resetSeg, watch: watchSeg } = useForm()
  const { register: regPref, handleSubmit: submitPref, formState: { errors: errPref }, reset: resetPref } = useForm({
    defaultValues: { notif_similitud: true, notif_comentarios_instructor: true }
  })

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-perfil">

        <PageHeader
          title="Mi Perfil"
          icon="user-cog"
          breadcrumb={[
            { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
            { label: 'Mi Perfil' }
          ]}
        />

        <div className="perfil-card cabecera-card">
          <div className="cabecera-izquierda">
            <div className="perfil-avatar">MG</div>
            <div className="perfil-info">
              <h2 className="perfil-nombre">Maria Gonzalez</h2>
              <span className="perfil-rol">Aprendiz - Análisis y desarrollo de Software</span>
              <span className="badge-activo"><i className="fas fa-circle"></i> Activo</span>
            </div>
          </div>
          <div className="cabecera-derecha">
            <div className="perfil-metricas">
              <div className="metrica-item">
                <i className="fas fa-folder metrica-icono"></i>
                <span className="metrica-valor">3</span>
                <span className="metrica-label">Proyectos</span>
              </div>
              <div className="metrica-item">
                <i className="fas fa-calendar-alt metrica-icono"></i>
                <span className="metrica-valor">12</span>
                <span className="metrica-label">Meses</span>
              </div>
            </div>
          </div>
        </div>

        <div className="perfil-card">
          <h3 className="card-titulo"><i className="fas fa-user"></i> Información Personal</h3>

          <div className="mensaje-feedback mensaje-exito oculto">
            <i className="fas fa-check-circle"></i><span>Operación realizada exitosamente.</span>
          </div>
          <div className="mensaje-feedback mensaje-error oculto">
            <i className="fas fa-exclamation-circle"></i><span>Ha ocurrido un error. Intenta nuevamente.</span>
          </div>

          <p className="campos-obligatorios">Los campos marcados con <span className="obligatorio">*</span> son obligatorios.</p>

          <form onSubmit={submitInfo((data) => { console.log(data); resetInfo() })}>
            <div className="form-grid">
              <div className="campo-grupo">
                <label htmlFor="nombre" className="campo-label">Nombre <span className="obligatorio">*</span></label>
                <input type="text" id="nombre" className="campo-input" {...regInfo("nombre", { required: true })} />
                {errInfo.nombre && <span className="campo-error">El nombre es obligatorio</span>}
              </div>
              <div className="campo-grupo">
                <label htmlFor="apellido" className="campo-label">Apellido <span className="obligatorio">*</span></label>
                <input type="text" id="apellido" className="campo-input" {...regInfo("apellido", { required: true })} />
                {errInfo.apellido && <span className="campo-error">El apellido es obligatorio</span>}
              </div>
              <div className="campo-grupo">
                <label htmlFor="correo" className="campo-label">Correo Electrónico <span className="obligatorio">*</span></label>
                <input type="email" id="correo" className="campo-input" {...regInfo("correo", { required: true, pattern: /^[^@\s]+@[^@\s]+\.[^@\s]+$/ })} />
                {errInfo.correo && <span className="campo-error">Correo inválido</span>}
                <span className="campo-info">Usa tu correo institucional del SENA</span>
              </div>
              <div className="campo-grupo campo-completo">
                <label htmlFor="id_programa" className="campo-label">Programa de Formación <span className="obligatorio">*</span></label>
                <select id="id_programa" className="campo-select" {...regInfo("id_programa", { required: true })}>
                  <option value="1">Análisis y desarrollo de Software</option>
                  <option value="2">Tecnología en Sistemas</option>
                  <option value="3">diseño y desarrollo Multimedia</option>
                  <option value="4">Tecnología en Redes</option>
                </select>
                {errInfo.id_programa && <span className="campo-error">Seleccione un programa</span>}
              </div>

            </div>
            <div className="form-botones">
              <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Cambios</button>
            </div>
          </form>
        </div>

        <div className="perfil-card">
          <h3 className="card-titulo"><i className="fas fa-shield-alt"></i> Seguridad y Acceso</h3>

          <div className="banner-advertencia">
            <i className="fas fa-exclamation-triangle banner-icono"></i>
            <div className="banner-texto">
              <strong>Tu contraseña tiene más de 6 meses.</strong>
              <p>Te recomendamos cambiarla por seguridad.</p>
            </div>
            <button className="btn-cambiar-ahora" type="button" onClick={() => {}}>Cambiar ahora</button>
          </div>

          <form onSubmit={submitSeg((data) => { console.log(data); resetSeg() })}>
            <div className="cambio-password">
              <div className="campo-grupo">
                <label htmlFor="contrasena-actual" className="campo-label">Contraseña Actual <span className="obligatorio">*</span></label>
                <input type="password" id="contrasena-actual" className="campo-input" {...regSeg("passActual", { required: true })} />
                {errSeg.passActual && <span className="campo-error">Campo obligatorio</span>}
              </div>
              <div className="campo-grupo">
                <label htmlFor="nueva-contrasena" className="campo-label">Nueva Contraseña <span className="obligatorio">*</span></label>
                <input type="password" id="nueva-contrasena" className="campo-input" {...regSeg("passNueva", { required: true, minLength: 8 })} />
                {errSeg.passNueva && <span className="campo-error">Mínimo 8 caracteres</span>}
                <span className="campo-ayuda">Mínimo 8 caracteres, incluyendo mayúsculas, minúsculas y números</span>
              </div>
              <div className="campo-grupo">
                <label htmlFor="confirmar-contrasena" className="campo-label">Confirmar Nueva Contraseña <span className="obligatorio">*</span></label>
                <input type="password" id="confirmar-contrasena" className="campo-input" {...regSeg("passConfirmar", { required: true, validate: value => value === watchSeg("passNueva") || "Las contraseñas no coinciden" })} />
                {errSeg.passConfirmar && <span className="campo-error">{errSeg.passConfirmar.message || "Campo obligatorio"}</span>}
              </div>
            </div>
            <div className="form-botones">
              <button type="submit" className="btn-primario"><i className="fas fa-key"></i> Actualizar contraseña</button>
            </div>
          </form>
        </div>

        <div className="perfil-card">
          <h3 className="card-titulo"><i className="fas fa-cog"></i> Preferencias</h3>
          <form onSubmit={submitPref((data) => { console.log(data); resetPref() })}>
            <div className="lista-checkboxes">
              <label className="checkbox-item">
                <input type="checkbox" {...regPref("notif_similitud")} />
                <span className="checkmark"></span>
                <div className="checkbox-info">
                  <span className="checkbox-titulo">Notificaciones de similitud</span>
                  <span className="checkbox-desc">Recibe alertas cuando se detecten similitudes en tus proyectos.</span>
                </div>
              </label>
              <label className="checkbox-item">
                <input type="checkbox" {...regPref("notif_comentarios_instructor")} />
                <span className="checkmark"></span>
                <div className="checkbox-info">
                  <span className="checkbox-titulo">Observaciones de instructores</span>
                  <span className="checkbox-desc">Recibe notificaciones cuando un instructor comente en tus proyectos.</span>
                </div>
              </label>
            </div>
            <div className="form-botones">
              <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Preferencias</button>
            </div>
          </form>
        </div>

      </div>
    </DashboardLayout>
  )
}

export default MiPerfil
