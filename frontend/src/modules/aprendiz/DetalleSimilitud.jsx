import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'

const coincidencias = [
  { seccion: 'Descripcion del proyecto', pct: 72 },
  { seccion: 'Tecnologias propuestas', pct: 60 },
  { seccion: 'Objetivos generales', pct: 55 },
  { seccion: 'Metodologia', pct: 45 },
]

const breadcrumb = [
  { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Detalle Similitud' },
]

function DetalleSimilitud() {
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-pagina">
        <PageHeader
          title="Detalle de Similitud"
          icon="exclamation-triangle"
          breadcrumb={breadcrumb}
          actions={<Link to="/aprendiz/alertas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Notificaciones</Link>}
        />

        <DataPanel title="Comparacion de Proyectos" icon="balance-scale">
          <div className="similitud-encabezado">
            <div className="encabezado-revision">
              <div className="info-revision">
                <div className="similitud-meta">
                  <span><i className="fas fa-calendar"></i> Detectado: 25/04/2026</span>
                  <span><i className="fas fa-percent"></i> Similitud: <strong className="texto-peligro">65%</strong></span>
                </div>
              </div>
              <span className="badge badge-peligro"><i className="fas fa-exclamation-circle"></i> Urgente</span>
            </div>
          </div>
          <div className="detalle-grid similitud-proyectos-grid">
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
          <div className="similitud-coincidencias">
            <h4><i className="fas fa-align-left"></i> Secciones Coincidentes</h4>
            <ul className="coincidencias-grid">
              {coincidencias.map((c, i) => (
                <li key={i} className="coincidencia-item">
                  {c.seccion}: <strong>{c.pct}% de similitud</strong>
                </li>
              ))}
            </ul>
          </div>
        </DataPanel>

        <div className="margen-superior">
          <Link to="/aprendiz/alertas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Notificaciones</Link>
        </div>
      </div>
    </DashboardLayout>
  );
}

export default DetalleSimilitud;
