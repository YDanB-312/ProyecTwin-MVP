import PerfilBase from '../../components/PerfilBase/PerfilBase'
import '../../assets/styles/pages/mi-perfil.css'

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Mi Perfil' },
]

export default function PerfilInstructor() {
  return (
    <PerfilBase
      role="instructor"
      dashboardTitulo="ProyecTwin - Panel del Instructor"
      dashboardUsuario="Carlos Ruiz | Instr. ADSO"
      notificaciones={8}
      breadcrumb={breadcrumb}
      avatarContent={
        <div className="cabecera-card-content">
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
      }
      infoDefaultValues={{ nombre: 'Carlos', apellido: 'Ruiz', correo: 'carlos.ruiz@sena.edu.co' }}
      extraInfoFields={null}
      securityBannerContent={() => (
        <>
          <i className="fas fa-exclamation-triangle banner-icono"></i>
          <div className="banner-texto">
            <strong>Tu contraseña tiene más de 6 meses sin actualizar.</strong>
            <p>Te recomendamos cambiarla por seguridad.</p>
          </div>
          <button className="btn-cambiar-ahora" type="button" onClick={() => document.getElementById('contrasena-actual')?.focus()}>Cambiar ahora</button>
        </>
      )}
      prefsSectionTitle="Preferencias de Instructor"
      prefsDefaultValues={{
        notif_nuevos_proyectos: true, notif_revisiones_pendientes: true,
        notif_similitud: false, notif_noticias_sistema: true,
        plantilla_comentarios: 'Estimado aprendiz,\n\nHe revisado tu proyecto y tengo los siguientes comentarios:\n\nAspectos positivos:\n-\n\nAspectos a mejorar:\n-\n\nRecomendaciones:\n-\n\nQuedo atento a cualquier inquietud.\n\nSaludos cordiales,\nCarlos Ruiz\nInstructor SENA'
      }}
      prefsContent={(regPref) => (
        <>
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
        </>
      )}
    />
  )
}
