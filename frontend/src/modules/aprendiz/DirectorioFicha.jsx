{/* DirectorioFicha.jsx — Componente que muestra el directorio de miembros de la ficha del aprendiz */}
import { Link } from 'react-router-dom'
import '../../assets/styles/pages/directorio.css';
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarAprendiz from '../../components/SidebarAprendiz/SidebarAprendiz';
import FooterAprendiz from '../../components/FooterAprendiz/FooterAprendiz';

function DirectorioFicha() {
  return (
    <div className="modulo-aprendiz">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5} />
      <SidebarAprendiz />
      <main className="contenido-principal">
        <div className="contenedor-pagina">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-address-book"></i> Directorio de Ficha</h1>
            <Link to="/aprendiz/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>

          <div className="info-ficha-actual mb-30">
            <div className="ficha-detalle">
              <h3>ADSO-2568</h3>
              <p>Analisis y Desarrollo de Sistemas - Trimestre 3</p>
            </div>
            <span className="badge badge-primario"><i className="fas fa-users"></i> 12 miembros</span>
          </div>

          <div className="seccion-directorio">
            <h2 className="titulo-seccion"><i className="fas fa-chalkboard-teacher"></i> Instructor</h2>
            <div className="directorio-usuarios">
              <div className="tarjeta-usuario">
                <div className="avatar-usuario">CR</div>
                <div className="info-usuario">
                  <h4>Carlos Ruiz</h4>
                  <p>Instructor - Tecnologias de la Informacion</p>
                </div>

              </div>
            </div>
          </div>

          <div className="seccion-directorio">
            <h2 className="titulo-seccion"><i className="fas fa-user-graduate"></i> Compañeros (11)</h2>
            <div className="directorio-usuarios">
              <div className="tarjeta-usuario">
                <div className="avatar-usuario avatar-secundario">JP</div>
                <div className="info-usuario">
                  <h4>Juan Perez</h4>
                  <p>Activo</p>
                </div>

              </div>
              <div className="tarjeta-usuario">
                <div className="avatar-usuario avatar-secundario">LG</div>
                <div className="info-usuario">
                  <h4>Laura Gomez</h4>
                  <p>Activo</p>
                </div>

              </div>
              <div className="tarjeta-usuario">
                <div className="avatar-usuario avatar-secundario">AM</div>
                <div className="info-usuario">
                  <h4>Ana Martinez</h4>
                  <p>Activo</p>
                </div>

              </div>
              <div className="tarjeta-usuario">
                <div className="avatar-usuario avatar-secundario">DS</div>
                <div className="info-usuario">
                  <h4>Diana Sanchez</h4>
                  <p>Inactivo</p>
                </div>

              </div>
            </div>
          </div>
        </div>
      </main>

      <FooterAprendiz />
    </div>
  );
}

export default DirectorioFicha;
