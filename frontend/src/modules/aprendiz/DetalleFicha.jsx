{/* DetalleFicha.jsx — Componente que muestra el detalle de la ficha a la que pertenece el aprendiz */}
import { Link } from 'react-router-dom'
import '../../assets/styles/pages/fichas.css';
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarAprendiz from '../../components/SidebarAprendiz/SidebarAprendiz';
import FooterAprendiz from '../../components/FooterAprendiz/FooterAprendiz';

function DetalleFicha() {
  return (
    <div className="modulo-aprendiz">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5} />
      <SidebarAprendiz />
      <main className="contenido-principal">
        <div className="contenedor-pagina">
          <div className="breadcrumb">
            <Link to="/aprendiz/dashboard"><i className="fas fa-home"></i> Inicio</Link>
            <span className="separador"><i className="fas fa-chevron-right"></i></span>
            <span className="actual">Mi Ficha</span>
          </div>
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-users"></i> Mi Ficha</h1>
            <Link to="/aprendiz/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>

          <div className="tarjeta tarjeta-padded mb-30">
            <div className="flex-between">
              <div>
                <h2 className="ficha-titulo">Analisis y Desarrollo 2568</h2>
                <p className="ficha-subtitulo">ADSO - Trimestre 3</p>
              </div>
              <div className="texto-derecha">
                <div className="codigo-ficha">ADSO-2568</div>
                <span className="estado-ficha-activa"><i className="fas fa-circle"></i> Activa</span>
              </div>
            </div>
          </div>

          <div className="info-ficha-actual mb-30">
            <div className="ficha-detalle">
              <h3><i className="fas fa-chalkboard-teacher"></i> Instructor Asignado</h3>
              <p>Carlos Ruiz - Tecnologias de la Informacion</p>
            </div>
            </div>

          <h2 className="titulo-seccion mb-24"><i className="fas fa-user-graduate"></i> Compañeros (12)</h2>

          <div className="grid-miembros">
            <div className="tarjeta-miembro">
              <div className="avatar-miembro">MG</div>
              <div className="info-miembro">
                <h4>Maria Gonzalez</h4>
                <p>maria.gonzalez@soy.sena.edu.co</p>
              </div>
              <span className="badge badge-exito">Activo</span>
            </div>
            <div className="tarjeta-miembro">
              <div className="avatar-miembro">JP</div>
              <div className="info-miembro">
                <h4>Juan Perez</h4>
                <p>juan.perez@soy.sena.edu.co</p>
              </div>
              <span className="badge badge-exito">Activo</span>
            </div>
            <div className="tarjeta-miembro">
              <div className="avatar-miembro">LG</div>
              <div className="info-miembro">
                <h4>Laura Gomez</h4>
                <p>laura.gomez@soy.sena.edu.co</p>
              </div>
              <span className="badge badge-exito">Activo</span>
            </div>
            <div className="tarjeta-miembro">
              <div className="avatar-miembro">AM</div>
              <div className="info-miembro">
                <h4>Ana Martinez</h4>
                <p>ana.martinez@soy.sena.edu.co</p>
              </div>
              <span className="badge badge-exito">Activo</span>
            </div>
            <div className="tarjeta-miembro">
              <div className="avatar-miembro">DS</div>
              <div className="info-miembro">
                <h4>Diana Sanchez</h4>
                <p>diana.sanchez@soy.sena.edu.co</p>
              </div>
              <span className="badge badge-neutral">Inactivo</span>
            </div>
          </div>
        </div>
      </main>

      <FooterAprendiz />
    </div>
  );
}

export default DetalleFicha;
