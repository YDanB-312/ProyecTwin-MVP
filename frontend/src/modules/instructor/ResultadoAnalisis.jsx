import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarInstructor from '../../components/SidebarInstructor/SidebarInstructor';
import FooterInstructor from '../../components/FooterInstructor/FooterInstructor';
import '../../assets/styles/pages/ia.css';

function ResultadoAnalisis() {
  return (
    <div className="modulo-instructor">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8} />
      <SidebarInstructor />
      <main className="contenido-principal">
        <div className="contenedor-ia">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-robot"></i> Resultado del Análisis</h1>
            <Link to="/instructor/revision-propuestas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Mis proyectos</Link>
          </div>
          <div className="card-analisis resultado-exitoso">
            <div className="ia-icono-resultado">
              <i className="fas fa-check-circle"></i>
            </div>
            <h2 className="titulo-ia">Análisis completado</h2>
            <p className="descripcion-ia">El sistema IA ha analizado tu proyecto <strong>"Sistema de Gestión Académica"</strong> y no encontró similitudes críticas con otros proyectos registrados.</p>
          </div>
          <div className="grid-resultados">
            <div className="tarjeta-resultado verde">
              <div className="resultado-icono"><i className="fas fa-check-circle"></i></div>
              <div className="resultado-contenido">
                <span className="resultado-label">Similitud General</span>
                <span className="resultado-valor">12%</span>
                <span className="resultado-badge badge badge-exito">Bajo riesgo</span>
              </div>
            </div>
            <div className="tarjeta-resultado amarillo">
              <div className="resultado-icono"><i className="fas fa-exclamation-triangle"></i></div>
              <div className="resultado-contenido">
                <span className="resultado-label">Palabras clave comunes</span>
                <span className="resultado-valor">3</span>
                <span className="resultado-badge badge badge-advertencia">Revisar</span>
              </div>
            </div>
            <div className="tarjeta-resultado verde">
              <div className="resultado-icono"><i className="fas fa-file-alt"></i></div>
              <div className="resultado-contenido">
                <span className="resultado-label">Proyectos similares</span>
                <span className="resultado-valor">2</span>
                <span className="resultado-badge badge badge-exito">Bajo impacto</span>
              </div>
            </div>
            <div className="tarjeta-resultado info">
              <div className="resultado-icono"><i className="fas fa-lightbulb"></i></div>
              <div className="resultado-contenido">
                <span className="resultado-label">Recomendación IA</span>
                <span className="resultado-valor">Aprobado</span>
                <span className="resultado-badge badge badge-exito">Continuar</span>
              </div>
            </div>
          </div>
          <div className="card-analisis">
            <h3 className="titulo-seccion"><i className="fas fa-clipboard-list"></i> Detalle de Coincidencias</h3>
            <div className="tabla-scroll">
              <table className="tabla-similitudes">
                <thead>
                  <tr>
                    <th>Proyecto</th>
                    <th>Aprendiz</th>
                    <th>Similitud</th>
                    <th>Sección</th>
                    <th>Acción</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>Plataforma Educativa SENA</td>
                    <td>Ana Martínez</td>
                    <td><span className="badge badge-exito">15%</span></td>
                    <td>Resumen</td>
                    <td><Link to="/instructor/detalle-similitud" className="btn-ver"><i className="fas fa-eye"></i> Ver</Link></td>
                  </tr>
                  <tr>
                    <td>Sistema de Notas Web</td>
                    <td>Juan Pérez</td>
                    <td><span className="badge badge-exito">8%</span></td>
                    <td>Tecnologías</td>
                    <td><Link to="/instructor/detalle-similitud" className="btn-ver"><i className="fas fa-eye"></i> Ver</Link></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div className="card-analisis">
            <h3 className="titulo-seccion"><i className="fas fa-lightbulb"></i> Recomendaciones de la IA</h3>
            <ul className="lista-recomendaciones">
              <li><i className="fas fa-check-circle texto-exito"></i> El título de tu proyecto es original y descriptivo.</li>
              <li><i className="fas fa-check-circle texto-exito"></i> Las tecnologías propuestas son adecuadas para el alcance del proyecto.</li>
              <li><i className="fas fa-exclamation-circle texto-advertencia"></i> Considera ampliar la sección de metodología para diferenciar tu enfoque.</li>
              <li><i className="fas fa-info-circle texto-info"></i> Revisa las palabras clave para incluir términos más específicos de tu dominio.</li>
            </ul>
          </div>
          <div className="acciones-finales">
            <Link to="/instructor/revision-propuestas" className="btn-primario"><i className="fas fa-check"></i> Guardar y continuar</Link>
            <Link to="/instructor/revision-propuestas" className="btn-secundario"><i className="fas fa-edit"></i> Editar proyecto</Link>
          </div>
        </div>
      </main>
      <FooterInstructor />
    </div>
  );
}

export default ResultadoAnalisis;
