import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarInstructor from '../../components/SidebarInstructor/SidebarInstructor';
import FooterInstructor from '../../components/FooterInstructor/FooterInstructor';
import '../../assets/styles/pages/directorio.css';

function DirectorioFichaInstructor() {
  return (
    <div className="modulo-instructor">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8} />
      <SidebarInstructor />
      <main className="contenido-principal">
        <div className="contenedor-pagina">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-address-book"></i> Directorio de Ficha</h1>
            <Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>
          <div className="info-ficha-actual mb-30">
            <div className="ficha-detalle">
              <h3>ADSO-2568</h3>
              <p>Análisis y Desarrollo de Sistemas - Trimestre 3</p>
            </div>
            <span className="badge badge-primario"><i className="fas fa-users"></i> 18 aprendices</span>
          </div>
          <div className="seccion-directorio">
            <h2 className="titulo-seccion"><i className="fas fa-user-graduate"></i> Aprendices (18)</h2>
            <div className="directorio-usuarios">
              <div className="tarjeta-usuario">
                <div className="avatar-usuario">AM</div>
                <div className="info-usuario">
                  <h4>Ana Martínez</h4>
                  <p>Activo - Última conexión: Hoy 14:30</p>
                </div>

              </div>
              <div className="tarjeta-usuario">
                <div className="avatar-usuario avatar-secundario">JP</div>
                <div className="info-usuario">
                  <h4>Juan Pérez</h4>
                  <p>Activo - Última conexión: Hoy 11:15</p>
                </div>

              </div>
              <div className="tarjeta-usuario">
                <div className="avatar-usuario avatar-secundario">LG</div>
                <div className="info-usuario">
                  <h4>Laura Gómez</h4>
                  <p>Activo - Última conexión: Ayer</p>
                </div>

              </div>
              <div className="tarjeta-usuario">
                <div className="avatar-usuario avatar-secundario">DS</div>
                <div className="info-usuario">
                  <h4>Diana Sánchez</h4>
                  <p>Inactivo - Última conexión: 05/05/2026</p>
                </div>

              </div>
            </div>
          </div>
        </div>
      </main>
      <FooterInstructor />
    </div>
  );
}

export default DirectorioFichaInstructor;
