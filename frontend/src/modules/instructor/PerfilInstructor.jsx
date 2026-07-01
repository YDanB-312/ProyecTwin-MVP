import { useForm } from 'react-hook-form'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/mi-perfil.css'
import '../../assets/styles/pages/perfil-instructor.css'

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Mi Perfil' },
]

function PerfilInstructor() {
  const { register: regInfo, handleSubmit: submitInfo, formState: { errors: errInfo }, reset: resetInfo } = useForm({
    defaultValues: { nombre: 'Carlos', apellido: 'Ruiz', correo: 'carlos.ruiz@sena.edu.co' }
  })
  const { register: regSeg, handleSubmit: submitSeg, formState: { errors: errSeg }, reset: resetSeg, watch: watchSeg } = useForm()
  const { register: regPref, handleSubmit: submitPref, formState: { errors: errPref }, reset: resetPref } = useForm({
    defaultValues: { notif_nuevos_proyectos: true, notif_revisiones_pendientes: true, notif_similitud: false, notif_noticias_sistema: true, plantilla_comentarios: 'Estimado aprendiz,\n\nHe revisado tu proyecto y tengo los siguientes comentarios:\n\nAspectos positivos:\n-\n\nAspectos a mejorar:\n-\n\nRecomendaciones:\n-\n\nQuedo atento a cualquier inquietud.\n\nSaludos cordiales,\nCarlos Ruiz\nInstructor SENA' }
  })

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-perfil">

        <PageHeader
          title="Mi Perfil"
          icon="user-cog"
          breadcrumb={breadcrumb}
        />

        <div className="perfil-card cabecera-card">
          <div className="cabecera-izquierda">
            <div className="perfil-avatar">
              <i className="fas fa-user-tie"></i>
              <div className="cambiar-avatar"><i className="fas fa-camera"></i></div>
            </div>
            <div className="perfil-info">
              <h2 className="perfil-nombre">Carlos Ruiz</h2>
              <span className="perfil-rol">Instructor - Análisis y desarrollo de Software</span>
              <span className="badge-activo"><i className="fas fa-circle"></i> Activo</span>
            </div>
          </div>
          <div className="cabecera-derecha">
            <div className="perfil-metricas">
              <div className="metrica-item">
                <i className="fas fa-tasks metrica-icono"></i>
                <span className="metrica-valor">24</span>
                <span className="metrica-label">Proyectos</span>
              </div>
              <div className="metrica-item">
                <i className="fas fa-check-circle metrica-icono"></i>
                <span className="metrica-valor">156</span>
                <span className="metrica-label">Revisiones</span>
              </div>
              <div className="metrica-item">
                <i className="fas fa-star metrica-icono"></i>
                <span className="metrica-valor">4.8</span>
                <span className="metrica-label">Calificación</span>
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
                <label htmlFor="correo" className="campo-label">Correo Electronico <span className="obligatorio">*</span></label>
                <input type="email" id="correo" className="campo-input" {...regInfo("correo", { required: true, pattern: /^[^@\s]+@[^@\s]+\.[^@\s]+$/ })} />
                {errInfo.correo && <span className="campo-error">Correo inválido</span>}
                <span className="campo-info">Usa tu correo institucional del SENA</span>
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
              <strong>Contraseña segura</strong>
              <p>Tu contraseña fue actualizada hace 2 meses. Se recomienda cambiarla cada 6 meses por seguridad.</p>
            </div>
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
          <h3 className="card-titulo"><i className="fas fa-cog"></i> Preferencias de Instructor</h3>

          <form onSubmit={submitPref((data) => { console.log(data); resetPref() })}>
            <div className="form-grid">

            </div>

            <div className="campo-grupo">
              <label className="campo-label">Notificaciones</label>
              <div className="lista-checkboxes">
                <label className="checkbox-item">
                  <input type="checkbox" {...regPref("notif_nuevos_proyectos")} />
                  <span className="checkmark"></span>
                  <div className="checkbox-info">
                    <span className="checkbox-titulo">Nuevos proyectos para revisar</span>
                    <span className="checkbox-desc">Recibe alertas cuando un aprendiz registre un nuevo proyecto.</span>
                  </div>
                </label>
                <label className="checkbox-item">
                  <input type="checkbox" {...regPref("notif_revisiones_pendientes")} />
                  <span className="checkmark"></span>
                  <div className="checkbox-info">
                    <span className="checkbox-titulo">Recordatorios de revisiones pendientes</span>
                    <span className="checkbox-desc">Recibe notificaciones cuando tengas proyectos pendientes por revisar.</span>
                  </div>
                </label>
                <label className="checkbox-item">
                  <input type="checkbox" {...regPref("notif_similitud")} />
                  <span className="checkmark"></span>
                  <div className="checkbox-info">
                    <span className="checkbox-titulo">Notificaciones de similitud de proyectos</span>
                    <span className="checkbox-desc">Alertas cuando se detecten similitudes entre proyectos de tus aprendices.</span>
                  </div>
                </label>
                <label className="checkbox-item">
                  <input type="checkbox" {...regPref("notif_noticias_sistema")} />
                  <span className="checkmark"></span>
                  <div className="checkbox-info">
                    <span className="checkbox-titulo">Noticias y actualizaciones del sistema</span>
                    <span className="checkbox-desc">Mantente informado sobre nuevas funcionalidades y cambios en la plataforma.</span>
                  </div>
                </label>
              </div>
            </div>

            <div className="campo-grupo">
              <label htmlFor="plantilla-comentarios" className="campo-label">Plantilla de Comentarios</label>
              <textarea id="plantilla-comentarios" className="campo-input" placeholder="Plantilla personalizada para comentarios en revisiones..." rows="3" {...regPref("plantilla_comentarios")}></textarea>
              <span className="campo-info">Plantilla personalizada que se usará en tus comentarios de revisión</span>
            </div>

            <div className="form-botones">
              <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Preferencias</button>
            </div>
          </form>
        </div>

      </div>
    </DashboardLayout>
  );
}

export default PerfilInstructor;
