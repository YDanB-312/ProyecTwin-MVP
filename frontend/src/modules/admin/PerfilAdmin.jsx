import PerfilBase from '../../components/PerfilBase/PerfilBase'

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Mi Perfil' },
]

export default function PerfilAdmin() {
  return (
    <PerfilBase
      role="admin"
      dashboardTitulo="ProyecTwin - Panel de Administración"
      dashboardUsuario="Admin Sistema"
      notificaciones={2}
      breadcrumb={breadcrumb}
      avatarContent={
        <div className="cabecera-card-content">
          <div className="cabecera-izquierda">
            <div className="perfil-avatar">AS</div>
            <div className="perfil-info">
              <h2 className="perfil-nombre">Admin Sistema</h2>
              <span className="perfil-rol">Administrador del Sistema</span>
              <span className="badge-activo"><i className="fas fa-circle"></i> Activo</span>
            </div>
          </div>
          <div className="cabecera-derecha">
            <div className="perfil-metricas">
              <div className="metrica-item">
                <i className="fas fa-users metrica-icono"></i>
                <span className="metrica-valor">156</span>
                <span className="metrica-label">Usuarios</span>
              </div>
              <div className="metrica-item">
                <i className="fas fa-bug metrica-icono"></i>
                <span className="metrica-valor">8</span>
                <span className="metrica-label">Reportes pend.</span>
              </div>
              <div className="metrica-item">
                <i className="fas fa-bell metrica-icono"></i>
                <span className="metrica-valor">6</span>
                <span className="metrica-label">Notificaciones</span>
              </div>
            </div>
          </div>
        </div>
      }
      infoDefaultValues={{ nombre: 'Admin', apellido: 'Sistema', correo: 'admin@proyectwin.sena.edu.co' }}
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
      prefsSectionTitle="Preferencias del Sistema"
      prefsDefaultValues={{ notif_correo: true, alertas_usuarios: true }}
      prefsContent={(regPref) => (
        <div className="lista-checkboxes">
          <label className="checkbox-item">
            <input type="checkbox" {...regPref("notif_correo")} />
            <span className="checkmark"></span>
            <div className="checkbox-info">
              <span className="checkbox-titulo">Notificaciones por correo</span>
              <span className="checkbox-desc">Recibe notificaciones del sistema en tu correo electrónico.</span>
            </div>
          </label>
          <label className="checkbox-item">
            <input type="checkbox" {...regPref("alertas_usuarios")} />
            <span className="checkmark"></span>
            <div className="checkbox-info">
              <span className="checkbox-titulo">Alertas de nuevos usuarios</span>
              <span className="checkbox-desc">Notifícame cuando se registre un nuevo usuario en la plataforma.</span>
            </div>
          </label>
        </div>
      )}
    />
  )
}
