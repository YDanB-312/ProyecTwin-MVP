import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarInstructor from '../../components/SidebarInstructor/SidebarInstructor';
import FooterInstructor from '../../components/FooterInstructor/FooterInstructor';
import '../../assets/styles/pages/detalle-compartido.css';

function DetalleSimilitudInstructor() {
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
            <Link to="/instructor/alertas">Notificaciones</Link>
            <span className="separador"><i className="fas fa-chevron-right"></i></span>
            <span className="actual">Detalle Similitud</span>
          </div>
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-exclamation-triangle"></i> Detalle de Similitud</h1>
            <Link to="/instructor/alertas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Notificaciones</Link>
          </div>
          <div className="tarjeta-revision mb-30">
            <div className="encabezado-revision">
              <div className="info-revision">
                <h3>Comparación de Proyectos</h3>
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
                <p className="info-linea">Sistema de Gestión Académica</p>
                <p className="detalle-linea"><strong>Aprendiz:</strong> Juan Pérez</p>
                <p className="detalle-linea"><strong>Programa:</strong> ADSO</p>
                <p className="detalle-linea"><strong>Fecha:</strong> 15/03/2026</p>
              </div>
              <div className="card-proyecto-compacto">
                <h4 className="card-titulo-amarillo"><i className="fas fa-file-alt"></i> Proyecto 2</h4>
                <p className="info-linea">Plataforma Educativa SENA</p>
                <p className="detalle-linea"><strong>Aprendiz:</strong> Ana Martínez</p>
                <p className="detalle-linea"><strong>Programa:</strong> ADSO</p>
                <p className="detalle-linea"><strong>Fecha:</strong> 02/02/2026</p>
              </div>
            </div>
            <div className="alerta-roja">
              <h4><i className="fas fa-align-left"></i> Secciones Coincidentes</h4>
              <ul className="lista-coincidencias">
                <li>Descripción del proyecto: <strong>72% de similitud</strong></li>
                <li>Tecnologías propuestas: <strong>60% de similitud</strong></li>
                <li>Objetivos generales: <strong>55% de similitud</strong></li>
                <li>Metodología: <strong>45% de similitud</strong></li>
              </ul>
            </div>
            <div className="comentarios-revision">
              <h4><i className="fas fa-clipboard-check"></i> Acciones</h4>
              <p className="texto-ayuda">Revisa los detalles de similitud y toma una decisión sobre este caso.</p>
              <div className="flex-row flex-wrap">
                <button type="button" className="btn-primario"><i className="fas fa-check"></i> Marcar como Revisada</button>
                <button type="button" className="btn-secundario"><i className="fas fa-envelope"></i> Contactar Aprendiz</button>
                <Link to="/instructor/analizando-proyecto" className="btn-secundario"><i className="fas fa-robot"></i> Analizar con IA</Link>
              </div>
            </div>
          </div>
          <div className="acciones-finales">
            <Link to="/instructor/alertas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Notificaciones</Link>
          </div>
        </div>
      </main>
      <FooterInstructor />
    </div>
  );
}

export default DetalleSimilitudInstructor;
