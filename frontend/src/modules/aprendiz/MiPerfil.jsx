import { useAuth } from '../../contexts/AuthContext'
import PerfilBase from '../../components/PerfilBase/PerfilBase'

const ROLES = {
  aprendiz: { label: 'Aprendiz', badge: 'exito' },
  instructor: { label: 'Instructor', badge: 'advertencia' },
  admin: { label: 'Administrador', badge: 'peligro' },
}

function getInitials(name) {
  if (!name) return '?'
  return name.split(' ').filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase()
}

const breadcrumb = [
  { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Mi Perfil' },
]

export default function MiPerfil() {
  const { user } = useAuth()
  const initials = getInitials(user?.nombre)
  const roleInfo = ROLES[user?.rol] || ROLES.aprendiz

  return (
    <PerfilBase
      role="aprendiz"
      dashboardTitulo="ProyecTwin - Panel del Aprendiz"
      dashboardUsuario={user?.nombre || 'Usuario'}
      notificaciones={0}
      breadcrumb={breadcrumb}
      avatarContent={
        <div className="cabecera-card-content">
          <div className="cabecera-izquierda">
            <div className="perfil-avatar">{initials}</div>
            <div className="perfil-info">
              <h2 className="perfil-nombre">{user?.nombre || 'Usuario'}</h2>
              <span className="perfil-rol">{roleInfo.label} - Análisis y desarrollo de Software</span>
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
      }
      infoDefaultValues={{ nombre: user?.nombre?.split(' ')[0] || '', apellido: user?.nombre?.split(' ').slice(1).join(' ') || '', correo: user?.correo || '', id_programa: '1' }}
      extraInfoFields={(regInfo, errInfo) => (
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
      )}
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
      prefsSectionTitle="Preferencias"
      prefsDefaultValues={{ notif_similitud: true, notif_comentarios_instructor: true }}
      prefsContent={(regPref) => (
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
      )}
    />
  )
}
