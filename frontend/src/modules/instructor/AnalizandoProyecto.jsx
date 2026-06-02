import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/ia.css'

function AnalizandoProyecto() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-ia">
        <div className="card-analisis">
          <div className="ia-spinner">
            <i className="fas fa-robot"></i>
          </div>
          <h1 className="titulo-ia">Analizando tu proyecto</h1>
          <p className="descripcion-ia">Nuestro sistema de inteligencia artificial esta revisando tu proyecto en busca de posibles similitudes con otros trabajos registrados en la plataforma.</p>
          <div className="barra-progreso-ia">
            <div className="progreso-ia"></div>
          </div>
          <ul className="lista-pasos-ia">
            <li className="paso-ia completado"><i className="fas fa-check-circle"></i> Resumen recibido</li>
            <li className="paso-ia completado"><i className="fas fa-check-circle"></i> Palabras clave identificadas</li>
            <li className="paso-ia activo"><i className="fas fa-spinner fa-pulse"></i> Analizando similitudes...</li>
            <li className="paso-ia pendiente"><i className="fas fa-circle"></i> Generando recomendaciones</li>
          </ul>
          <p className="nota-ia">
            <i className="fas fa-info-circle"></i>
            Este proceso puede tomar hasta 30 segundos. Por favor no cierres esta pagina.
          </p>
          <div className="acciones-centro">
            <Link to="/instructor/dashboard" className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}

export default AnalizandoProyecto;
