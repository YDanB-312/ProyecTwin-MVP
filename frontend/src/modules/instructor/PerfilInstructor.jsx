import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/perfil-instructor.css'

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Mi Perfil' },
]

function PerfilInstructor() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-perfil">
        <PageHeader
          title="Mi Perfil"
          icon="user-cog"
          breadcrumb={breadcrumb}
        />

        <div className="layout-perfil">
          <div className="tarjeta tarjeta-perfil">
            <div className="avatar-perfil">
              <i className="fas fa-user-tie"></i>
              <div className="cambiar-avatar"><i className="fas fa-camera"></i></div>
            </div>
            <h2 className="nombre-usuario">Carlos Ruiz</h2>
            <p className="rol-usuario">Instructor - Analisis y desarrollo de Software</p>
            <div className="estadisticas-perfil">
              <div className="estadistica">
                <span className="valor-estadistica">24</span>
                <span className="etiqueta-estadistica">Proyectos Asignados</span>
              </div>
              <div className="estadistica">
                <span className="valor-estadistica">156</span>
                <span className="etiqueta-estadistica">Revisiones</span>
              </div>
              <div className="estadistica">
                <span className="valor-estadistica">4.8</span>
                <span className="etiqueta-estadistica">Calificacion</span>
              </div>
            </div>
            <div className="info-adicional">
              <div className="info-item"><i className="fas fa-id-card"></i><span>Codigo: INS-2023-001</span></div>
              <div className="info-item"><i className="fas fa-building"></i><span>Centro de Tecnologias</span></div>
              <div className="info-item"><i className="fas fa-calendar-alt"></i><span>Ingreso: 15/03/2020</span></div>
            </div>
            <button className="btn-primario" type="button"><i className="fas fa-sync-alt"></i> Actualizar Informacion</button>
            <button className="btn-secundario" type="button"><i className="fas fa-download"></i> Exportar Datos</button>
          </div>

          <div className="formulario-perfil">
            <DataPanel title="Informacion Personal" icon="user">
              <div className="mensaje-feedback mensaje-exito oculto">
                <i className="fas fa-check-circle"></i><span>Operacion realizada exitosamente.</span>
              </div>
              <div className="mensaje-feedback mensaje-error oculto">
                <i className="fas fa-exclamation-circle"></i><span>Ha ocurrido un error. Intenta nuevamente.</span>
              </div>
              <form action="#">
                <div style={{ padding: 'var(--space-xl)' }}>
                  <div className="grupo-campos">
                    <div className="grupo-formulario">
                      <label htmlFor="nombre" className="etiqueta requerido">Nombre</label>
                      <input type="text" id="nombre" className="input-text" value="Carlos" required name="nombre" />
                    </div>
                    <div className="grupo-formulario">
                      <label htmlFor="apellido" className="etiqueta requerido">Apellido</label>
                      <input type="text" id="apellido" className="input-text" value="Ruiz" required name="apellido" />
                    </div>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="documento" className="etiqueta">Documento</label>
                    <input type="text" id="documento" className="input-text" value="79876543" name="documento" />
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="correo" className="etiqueta requerido">Correo Electronico</label>
                    <input type="email" id="correo" className="input-text" value="carlos.ruiz@sena.edu.co" required name="correo" />
                    <div className="campo-informacion">Usa tu correo institucional del SENA</div>
                  </div>
                  <div className="grupo-campos">
                    <div className="grupo-formulario">
                      <label htmlFor="telefono" className="etiqueta">Telefono</label>
                      <input type="tel" id="telefono" className="input-text" value="3235421165" name="telefono" />
                    </div>
                    <div className="grupo-formulario">
                      <label htmlFor="fecha-nacimiento" className="etiqueta">Fecha de Nacimiento</label>
                      <input type="date" id="fecha-nacimiento" className="input-text" value="1985-08-20" name="fecha_nacimiento" />
                    </div>
                  </div>
                  <div className="grupo-campos">
                    <div className="grupo-formulario">
                      <label htmlFor="codigo_instructor" className="etiqueta requerido">Codigo de Instructor</label>
                      <input type="text" id="codigo_instructor" className="input-text" value="INS-2023-001" required name="codigo_instructor" />
                    </div>
                    <div className="grupo-formulario">
                      <label htmlFor="fecha_ingreso" className="etiqueta requerido">Fecha de Ingreso</label>
                      <input type="date" id="fecha_ingreso" className="input-text" value="2020-03-15" required name="fecha_ingreso" />
                    </div>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="especialidad" className="etiqueta requerido">Especialidad</label>
                    <select id="especialidad" className="select" required name="especialidad">
                      <option value="adso">Analisis y desarrollo de Software</option>
                      <option value="sistemas">Tecnologia en Sistemas</option>
                      <option value="multimedia">Diseno y desarrollo Multimedia</option>
                      <option value="redes">Tecnologia en Redes</option>
                    </select>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="centro" className="etiqueta requerido">Centro de Formacion</label>
                    <select id="centro" className="select" required name="centro_formacion">
                      <option value="centro-bogota">Centro de comercio y servicios - Popayan</option>
                    </select>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="biografia" className="etiqueta">Biografia Profesional</label>
                    <textarea id="biografia" className="textarea" placeholder="Describe tu experiencia profesional y especialidades..." rows="4" name="biografia_profesional">Instructor especializado en desarrollo de software con mas de 10 anos de experiencia en la industria. Especialista en bases de datos, arquitectura de software y metodologias agiles.</textarea>
                  </div>
                  <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Cambios</button>
                </div>
              </form>
            </DataPanel>

            <DataPanel title="Seguridad y Acceso" icon="shield-alt">
              <div className="alerta-seguridad" style={{ padding: 'var(--space-lg) var(--space-xl)', borderBottom: '1px solid var(--color-borde)' }}>
                <div className="icono-alerta"><i className="fas fa-check-circle"></i></div>
                <div className="contenido-alerta">
                  <h4>Contrasena segura</h4>
                  <p>Tu contrasena fue actualizada hace 2 meses. Se recomienda cambiarla cada 6 meses por seguridad.</p>
                </div>
              </div>
              <form action="#">
                <div style={{ padding: 'var(--space-xl)' }}>
                  <div className="grupo-formulario">
                    <label htmlFor="contrasena-actual" className="etiqueta requerido">Contrasena Actual</label>
                    <input type="password" id="contrasena-actual" className="input-text" required name="password_actual" />
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="nueva-contrasena" className="etiqueta requerido">Nueva Contrasena</label>
                    <input type="password" id="nueva-contrasena" className="input-text" required name="password_nueva" />
                    <div className="campo-informacion">Minimo 8 caracteres, incluyendo mayusculas, minusculas y numeros</div>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="confirmar-contrasena" className="etiqueta requerido">Confirmar Nueva Contrasena</label>
                    <input type="password" id="confirmar-contrasena" className="input-text" required name="password_confirmar" />
                  </div>
                  <button type="submit" className="btn-primario"><i className="fas fa-key"></i> Actualizar contrasena</button>
                </div>
              </form>
            </DataPanel>

            <DataPanel title="Preferencias de Instructor" icon="cog">
              <form action="#">
                <div style={{ padding: 'var(--space-xl)' }}>
                  <div className="grupo-formulario">
                    <label htmlFor="max-proyectos" className="etiqueta">Limite de proyectos Asignados</label>
                    <select id="max-proyectos" className="select" name="limite_proyectos">
                      <option value="10">10 proyectos</option>
                      <option value="15">15 proyectos</option>
                      <option value="20" selected>20 proyectos</option>
                      <option value="25">25 proyectos</option>
                      <option value="30">30 proyectos</option>
                    </select>
                    <div className="campo-informacion">Numero maximo de proyectos que puedes tener asignados simultaneamente</div>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="tiempo-revision" className="etiqueta">Tiempo Maximo de Revision</label>
                    <select id="tiempo-revision" className="select" name="tiempo_maximo_revision">
                      <option value="1">1 dia</option>
                      <option value="2">2 dias</option>
                      <option value="3" selected>3 dias</option>
                      <option value="5">5 dias</option>
                      <option value="7">7 dias</option>
                    </select>
                    <div className="campo-informacion">Tiempo maximo para revisar proyectos antes de notificar al aprendiz</div>
                  </div>
                  <div className="grupo-formulario">
                    <label className="etiqueta">Notificaciones</label>
                    <div className="lista-checkboxes">
                      <label className="checkbox-item">
                        <input type="checkbox" checked name="notif_nuevos_proyectos" />
                        <span className="checkmark"></span>
                        Nuevos proyectos para revisar
                      </label>
                      <label className="checkbox-item">
                        <input type="checkbox" checked name="notif_revisiones_pendientes" />
                        <span className="checkmark"></span>
                        Recordatorios de revisiones pendientes
                      </label>
                      <label className="checkbox-item">
                        <input type="checkbox" name="notif_similitud" />
                        <span className="checkmark"></span>
                        Notificaciones de similitud de proyectos
                      </label>
                      <label className="checkbox-item">
                        <input type="checkbox" checked name="notif_noticias_sistema" />
                        <span className="checkmark"></span>
                        Noticias y actualizaciones del sistema
                      </label>
                    </div>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="plantilla-comentarios" className="etiqueta">Plantilla de Comentarios</label>
                    <textarea id="plantilla-comentarios" className="textarea" placeholder="Plantilla personalizada para comentarios en revisiones..." rows="3" name="plantilla_comentarios">Estimado aprendiz,

He revisado tu proyecto y tengo los siguientes comentarios:

Aspectos positivos:
-

Aspectos a mejorar:
-

Recomendaciones:
-

Quedo atento a cualquier inquietud.

Saludos cordiales,
Carlos Ruiz
Instructor SENA</textarea>
                    <div className="campo-informacion">Plantilla personalizada que se usara en tus comentarios de revision</div>
                  </div>
                  <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Preferencias</button>
                </div>
              </form>
            </DataPanel>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}

export default PerfilInstructor;
