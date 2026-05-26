import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarInstructor from '../../components/SidebarInstructor/SidebarInstructor';
import FooterInstructor from '../../components/FooterInstructor/FooterInstructor';
import '../../assets/styles/pages/fichas.css';

function DetalleFichaInstructor() {
  return (
    <div className="modulo-instructor">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8} />
      <SidebarInstructor />
      <main className="contenido-principal">
        <div className="contenedor-pagina">
          <div className="breadcrumb">
            <Link to="/instructor/dashboard"><i className="fas fa-home"></i> Inicio</Link>
            <span className="separador"><i className="fas fa-chevron-right"></i></span>
            <Link to="/instructor/gestionar-fichas">Gestionar Fichas</Link>
            <span className="separador"><i className="fas fa-chevron-right"></i></span>
            <span className="actual">Detalle de Ficha</span>
          </div>
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-users"></i> Detalle de Ficha</h1>
            <Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>
          <div className="tarjeta tarjeta-padded mb-30">
            <div className="flex-between">
              <div>
                <h2 className="ficha-titulo">Análisis y Desarrollo 2568</h2>
                <p className="ficha-subtitulo">ADSO - Trimestre 3</p>
              </div>
              <div className="texto-derecha">
                <div className="codigo-ficha">ADSO-2568</div>
                <span className="estado-ficha-activa"><i className="fas fa-circle"></i> Activa</span>
              </div>
            </div>
          </div>
          <div className="invite-card">
            <i className="fas fa-link icono-link"></i>
            <h3>Código de Invitación</h3>
            <div className="codigo-grande">ADSO-2568</div>
            <p>Comparte este código con tus aprendices para que se unan a la ficha.</p>
            <div className="acciones-invite">
              <button className="btn-primario" type="button"><i className="fas fa-copy"></i> Copiar Código</button>
            </div>
          </div>
          <h2 className="titulo-seccion mb-24"><i className="fas fa-user-graduate"></i> Aprendices (28)</h2>
          <div className="grid-miembros">
            <div className="tarjeta-miembro">
              <div className="avatar-miembro">MG</div>
              <div className="info-miembro">
                <h4>María González</h4>
                <p>maria.gonzalez@soy.sena.edu.co</p>
              </div>
              <span className="badge badge-exito">Activo</span>
            </div>
            <div className="tarjeta-miembro">
              <div className="avatar-miembro">JP</div>
              <div className="info-miembro">
                <h4>Juan Pérez</h4>
                <p>juan.perez@soy.sena.edu.co</p>
              </div>
              <span className="badge badge-exito">Activo</span>
            </div>
            <div className="tarjeta-miembro">
              <div className="avatar-miembro">LG</div>
              <div className="info-miembro">
                <h4>Laura Gómez</h4>
                <p>laura.gomez@soy.sena.edu.co</p>
              </div>
              <span className="badge badge-exito">Activo</span>
            </div>
            <div className="tarjeta-miembro">
              <div className="avatar-miembro">AM</div>
              <div className="info-miembro">
                <h4>Ana Martínez</h4>
                <p>ana.martinez@soy.sena.edu.co</p>
              </div>
              <span className="badge badge-exito">Activo</span>
            </div>
            <div className="tarjeta-miembro">
              <div className="avatar-miembro">DS</div>
              <div className="info-miembro">
                <h4>Diana Sánchez</h4>
                <p>diana.sanchez@soy.sena.edu.co</p>
              </div>
              <span className="badge badge-neutral">Inactivo</span>
            </div>
          </div>
        </div>
      </main>
      <FooterInstructor />
    </div>
  );
}

export default DetalleFichaInstructor;
