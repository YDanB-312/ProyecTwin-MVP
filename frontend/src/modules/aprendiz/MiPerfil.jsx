// MiPerfil - Pagina de perfil del aprendiz con informacion personal, seguridad y preferencias de configuracion
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarAprendiz from '../../components/SidebarAprendiz/SidebarAprendiz';
import FooterAprendiz from '../../components/FooterAprendiz/FooterAprendiz';
import '../../assets/styles/pages/mi-perfil.css';

function MiPerfil() {
  return (
    <div className="modulo-aprendiz">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5} />
      <SidebarAprendiz />
      <main className="contenido-principal">
        <div className="contenedor-perfil">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-user-cog"></i> Mi Perfil</h1>
            <p className="descripcion-pagina">Gestiona tu Informacion personal, preferencias y configuracion de cuenta</p>
          </div>
          <div className="layout-perfil">
            <div className="tarjeta tarjeta-perfil">
              <div className="avatar-perfil">
                <i className="fas fa-user"></i>
                <div className="cambiar-avatar"><i className="fas fa-camera"></i></div>
              </div>
              <h2 className="nombre-usuario">Maria Gonzalez</h2>
              <p className="rol-usuario">Aprendiz - Analisis y desarrollo de Software</p>
              <div className="estadisticas-perfil">
                <div className="estadistica"><span className="valor-estadistica">3</span><span className="etiqueta-estadistica">Proyectos</span></div>
                <div className="estadistica"><span className="valor-estadistica">12</span><span className="etiqueta-estadistica">Meses</span></div>
                <div className="estadistica"><span className="valor-estadistica">85%</span><span className="etiqueta-estadistica">Avance</span></div>
              </div>
              <button className="btn-primario" type="button"><i className="fas fa-sync-alt"></i> Actualizar Informacion</button>
              <button className="btn-secundario" type="button"><i className="fas fa-download"></i> Exportar Datos</button>
            </div>
            <div className="formulario-perfil">
              <section className="tarjeta">
                <h2 className="titulo-seccion"><i className="fas fa-user"></i> Informacion Personal</h2>
                <div className="mensaje-feedback mensaje-exito oculto">
                  <i className="fas fa-check-circle"></i>
                  <span>Operacion realizada exitosamente.</span>
                </div>
                <div className="mensaje-feedback mensaje-error oculto">
                  <i className="fas fa-exclamation-circle"></i>
                  <span>Ha ocurrido un error. Intenta nuevamente.</span>
                </div>
                <form action="#">
                  <div className="grupo-campos">
                    <div className="grupo-formulario">
                      <label htmlFor="nombre"><i className="fas fa-user"></i> Nombre</label>
                      <input type="text" id="nombre" className="input-text" value="Maria" required name="nombre" />
                    </div>
                    <div className="grupo-formulario">
                      <label htmlFor="apellido" className="etiqueta requerido">Apellido</label>
                      <input type="text" id="apellido" className="input-text" value="Gonzalez" required name="apellido" />
                    </div>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="documento" className="etiqueta">Documento</label>
                    <input type="text" id="documento" className="input-text" value="1023456789" name="documento" />
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="correo" className="etiqueta requerido">Correo electronico</label>
                    <input type="email" id="correo" className="input-text" value="maria.gonzalez@sena.edu.co" required name="correo" />
                    <div className="campo-informacion">Usa tu correo institucional del SENA</div>
                  </div>
                  <div className="grupo-campos">
                    <div className="grupo-formulario">
                      <label htmlFor="telefono" className="etiqueta">telefono</label>
                      <input type="tel" id="telefono" className="input-text" value="3235421165" name="telefono" />
                    </div>
                    <div className="grupo-formulario">
                      <label htmlFor="fecha-nacimiento" className="etiqueta">Fecha de Nacimiento</label>
                      <input type="date" id="fecha-nacimiento" className="input-text" value="2000-05-15" name="fecha_nacimiento" />
                    </div>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="id_programa" className="etiqueta requerido">Programa de Formacion</label>
                    <select id="id_programa" className="select" required name="id_programa">
                      <option value="1">Analisis y desarrollo de Software</option>
                      <option value="2">Tecnologia en Sistemas</option>
                      <option value="3">diseno y desarrollo Multimedia</option>
                      <option value="4">Tecnologia en Redes</option>
                    </select>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="centro" className="etiqueta requerido">Centro de Formacion</label>
                    <select id="centro" className="select" required name="centro_formacion">
                      <option value="centro-bogota">Centro de Tecnologias para la Academia - Bogota</option>
                      <option value="centro-medellin">Centro de diseno y Metrologia - Medellin</option>
                      <option value="centro-cali">Centro de Electricidad y Automatizacion - Cali</option>
                    </select>
                  </div>
                  <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Cambios</button>
                </form>
              </section>
              <section className="tarjeta seccion-seguridad">
                <h2 className="titulo-seccion"><i className="fas fa-shield-alt"></i> Seguridad y Acceso</h2>
                <div className="alerta-seguridad">
                  <div className="icono-alerta"><i className="fas fa-exclamation-circle"></i></div>
                  <div className="contenido-alerta">
                    <h4>contraseña antigua</h4>
                    <p>Tu contraseña tiene mas de 6 meses. Te recomendamos cambiarla por seguridad.</p>
                  </div>
                </div>
                <form action="#">
                  <div className="grupo-formulario">
                    <label htmlFor="contrasena-actual" className="etiqueta requerido">contraseña Actual</label>
                    <input type="password" id="contrasena-actual" className="input-text" required name="password_actual" />
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="nueva-contrasena" className="etiqueta requerido">Nueva contraseña</label>
                    <input type="password" id="nueva-contrasena" className="input-text" required name="password_nueva" />
                    <div className="campo-informacion">Minimo 8 caracteres, incluyendo mayusculas, minusculas y numeros</div>
                  </div>
                  <div className="grupo-formulario">
                    <label htmlFor="confirmar-contrasena" className="etiqueta requerido">Confirmar Nueva contraseña</label>
                    <input type="password" id="confirmar-contrasena" className="input-text" required name="password_confirmar" />
                  </div>
                  <button type="submit" className="btn-primario"><i className="fas fa-key"></i> Actualizar contraseña</button>
                </form>
              </section>
              <section className="tarjeta">
                <h2 className="titulo-seccion"><i className="fas fa-cog"></i> Preferencias</h2>
                <form action="#">
                  <div className="grupo-formulario">
                    <label className="etiqueta">Notificaciones</label>
                    <div className="checkbox-container">
                      <label className="checkbox-item">
                        <input type="checkbox" checked name="notif_similitud" />
                        Notificaciones de similitud de proyectos
                      </label>
                      <label className="checkbox-item">
                        <input type="checkbox" checked name="notif_comentarios_instructor" />
                        Observaciones de instructores
                      </label>
                    </div>
                  </div>
                  <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Guardar Preferencias</button>
                </form>
              </section>
            </div>
          </div>
        </div>
      </main>
      <FooterAprendiz />
    </div>
  );
}

export default MiPerfil;
