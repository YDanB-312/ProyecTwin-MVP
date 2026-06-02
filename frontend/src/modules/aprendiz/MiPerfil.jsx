import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/mi-perfil.css'

function MiPerfil() {

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-perfil">

        <div className="perfil-header">
          <Link to="/aprendiz/dashboard" className="volver-link"><i className="fas fa-arrow-left"></i> Volver al Dashboard</Link>
          <h1 className="perfil-titulo">Mi Perfil</h1>
        </div>

        <div className="perfil-card cabecera-card">
          <div className="cabecera-izquierda">
            <div className="perfil-avatar">MG</div>
            <div className="perfil-info">
              <h2 className="perfil-nombre">Maria Gonzalez</h2>
              <span className="perfil-rol">Aprendiz - Analisis y desarrollo de Software</span>
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
            <div className="cabecera-botones">
              <button className="btn-primario" type="button"><i className="fas fa-sync-alt"></i> Actualizar Informacion</button>
              <button className="btn-secundario" type="button"><i className="fas fa-download"></i> Exportar Datos</button>
            </div>
          </div>
        </div>

        <div className="perfil-card">
          <h3 className="card-titulo"><i className="fas fa-user"></i> Informacion Personal</h3>

          <div className="mensaje-feedback mensaje-exito oculto">
            <i className="fas fa-check-circle"></i><span>Operacion realizada exitosamente.</span>
          </div>
          <div className="mensaje-feedback mensaje-error oculto">
            <i className="fas fa-exclamation-circle"></i><span>Ha ocurrido un error. Intenta nuevamente.</span>
          </div>

          <p className="campos-obligatorios">Los campos marcados con <span className="obligatorio">*</span> son obligatorios.</p>

          <form action="#">
            <div className="form-grid">
              <div className="campo-grupo">
                <label htmlFor="nombre" className="campo-label">Nombre <span className="obligatorio">*</span></label>
                <input type="text" id="nombre" className="campo-input" value="Maria" required name="nombre" />
              </div>
              <div className="campo-grupo">
                <label htmlFor="apellido" className="campo-label">Apellido <span className="obligatorio">*</span></label>
                <input type="text" id="apellido" className="campo-input" value="Gonzalez" required name="apellido" />
              </div>
              <div className="campo-grupo">
                <label htmlFor="documento" className="campo-label">Documento</label>
                <input type="text" id="documento" className="campo-input" value="1023456789" name="documento" />
              </div>
              <div className="campo-grupo">
                <label htmlFor="correo" className="campo-label">Correo Electronico <span className="obligatorio">*</span></label>
                <input type="email" id="correo" className="campo-input" value="maria.gonzalez@sena.edu.co" required name="correo" />
                <span className="campo-info">Usa tu correo institucional del SENA</span>
              </div>
              <div className="campo-grupo">
                <label htmlFor="telefono" className="campo-label">Telefono</label>
                <input type="tel" id="telefono" className="campo-input" value="3235421165" name="telefono" />
              </div>
              <div className="campo-grupo">
                <label htmlFor="fecha-nacimiento" className="campo-label">Fecha de Nacimiento</label>
                <input type="date" id="fecha-nacimiento" className="campo-input" value="2000-05-15" name="fecha_nacimiento" />
              </div>
              <div className="campo-grupo campo-completo">
                <label htmlFor="id_programa" className="campo-label">Programa de Formacion <span className="obligatorio">*</span></label>
                <select id="id_programa" className="campo-select" required name="id_programa">
                  <option value="1">Analisis y desarrollo de Software</option>
                  <option value="2">Tecnologia en Sistemas</option>
                  <option value="3">diseno y desarrollo Multimedia</option>
                  <option value="4">Tecnologia en Redes</option>
                </select>
              </div>
              <div className="campo-grupo campo-completo">
                <label htmlFor="centro" className="campo-label">Centro de Formacion <span className="obligatorio">*</span></label>
                <select id="centro" className="campo-select" required name="centro_formacion">
                  <option value="centro-bogota">Centro de Tecnologias para la Academia - Bogota</option>
                  <option value="centro-medellin">Centro de diseno y Metrologia - Medellin</option>
                  <option value="centro-cali">Centro de Electricidad y Automatizacion - Cali</option>
                </select>
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
              <strong>Tu contraseña tiene mas de 6 meses.</strong>
              <p>Te recomendamos cambiarla por seguridad.</p>
            </div>
            <button className="btn-cambiar-ahora" type="button">Cambiar ahora</button>
          </div>

          <form action="#">
            <div className="cambio-password">
              <div className="campo-grupo">
                <label htmlFor="contrasena-actual" className="campo-label">Contraseña Actual <span className="obligatorio">*</span></label>
                <input type="password" id="contrasena-actual" className="campo-input" required name="password_actual" />
              </div>
              <div className="campo-grupo">
                <label htmlFor="nueva-contrasena" className="campo-label">Nueva Contraseña <span className="obligatorio">*</span></label>
                <input type="password" id="nueva-contrasena" className="campo-input" required name="password_nueva" />
                <span className="campo-ayuda">Minimo 8 caracteres, incluyendo mayusculas, minusculas y numeros</span>
              </div>
              <div className="campo-grupo">
                <label htmlFor="confirmar-contrasena" className="campo-label">Confirmar Nueva Contraseña <span className="obligatorio">*</span></label>
                <input type="password" id="confirmar-contrasena" className="campo-input" required name="password_confirmar" />
              </div>
            </div>
            <div className="form-botones">
              <button type="submit" className="btn-primario"><i className="fas fa-key"></i> Actualizar contraseña</button>
            </div>
          </form>
        </div>

        <div className="perfil-card">
          <h3 className="card-titulo"><i className="fas fa-cog"></i> Preferencias</h3>
          <form action="#">
            <div className="preferencias-lista">
              <label className="checkbox-item">
                <input type="checkbox" defaultChecked name="notif_similitud" />
                <span className="checkbox-custom"></span>
                <div className="checkbox-info">
                  <span className="checkbox-titulo">Notificaciones de similitud</span>
                  <span className="checkbox-desc">Recibe alertas cuando se detecten similitudes en tus proyectos.</span>
                </div>
              </label>
              <label className="checkbox-item">
                <input type="checkbox" defaultChecked name="notif_comentarios_instructor" />
                <span className="checkbox-custom"></span>
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
