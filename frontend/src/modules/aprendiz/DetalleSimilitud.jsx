{/* DetalleSimilitud.jsx — Componente que muestra el detalle de una alerta de similitud entre proyectos */}
import { Link } from 'react-router-dom'
import '../../assets/styles/pages/detalle-compartido.css';
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarAprendiz from '../../components/SidebarAprendiz/SidebarAprendiz';
import FooterAprendiz from '../../components/FooterAprendiz/FooterAprendiz';

function DetalleSimilitud() {
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
            <span className="actual">Detalle Similitud</span>
          </div>
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-exclamation-triangle"></i> Detalle de Similitud</h1>
            <Link to="/aprendiz/alertas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Notificaciones</Link>
          </div>

          <div className="tarjeta-revision mb-30">
            <div className="encabezado-revision">
              <div className="info-revision">
                <h3>Comparacion de Proyectos</h3>
                <div className="meta-revision">
                  <span><i className="fas fa-calendar"></i> Detectado: 25/04/2026</span>
                  <span><i className="fas fa-percent"></i> Similitud: <strong className="texto-peligro">65%</strong></span>
                </div>
              </div>
              <span className="badge badge-peligro"><i className="fas fa-exclamation-circle"></i> Urgente</span>
            </div>

            <div className="detalle-grid">
              <div className="card-proyecto-compacto">
                <h4 className="card-titulo-verde"><i className="fas fa-file-alt"></i> Proyecto 1</h4>
                <p className="info-linea">Sistema de Gestion Academica</p>
                <p className="detalle-linea"><strong>Aprendiz:</strong> Juan Perez</p>
                <p className="detalle-linea"><strong>Programa:</strong> ADSO</p>
                <p className="detalle-linea"><strong>Fecha:</strong> 15/03/2026</p>
              </div>
              <div className="card-proyecto-compacto">
                <h4 className="card-titulo-amarillo"><i className="fas fa-file-alt"></i> Proyecto 2</h4>
                <p className="info-linea">Plataforma Educativa SENA</p>
                <p className="detalle-linea"><strong>Aprendiz:</strong> Ana Martinez</p>
                <p className="detalle-linea"><strong>Programa:</strong> ADSO</p>
                <p className="detalle-linea"><strong>Fecha:</strong> 02/02/2026</p>
              </div>
            </div>

            <div className="alerta-roja">
              <h4><i className="fas fa-align-left"></i> Secciones Coincidentes</h4>
              <ul className="lista-coincidencias">
                <li>Descripcion del proyecto: <strong>72% de similitud</strong></li>
                <li>Tecnologias propuestas: <strong>60% de similitud</strong></li>
                <li>Objetivos generales: <strong>55% de similitud</strong></li>
                <li>Metodologia: <strong>45% de similitud</strong></li>
              </ul>
            </div>
          </div>

          <div className="acciones-finales">
            <Link to="/aprendiz/alertas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Notificaciones</Link>
          </div>
        </div>
      </main>

      <FooterAprendiz />
    </div>
  );
}

export default DetalleSimilitud;
