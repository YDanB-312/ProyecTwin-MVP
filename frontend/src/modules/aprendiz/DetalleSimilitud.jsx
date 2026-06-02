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
          <div className="tarjeta-revision">
            <div className="encabezado-revision" style={{ padding: 'var(--space-lg) var(--space-xl)', borderBottom: '1px solid var(--color-borde)' }}>
              <div className="info-revision">
                <div className="meta-revision" style={{ display: 'flex', gap: '20px', fontSize: '0.9rem', color: 'var(--color-texto-secundario)' }}>
                  <span><i className="fas fa-calendar"></i> Detectado: 25/04/2026</span>
                  <span><i className="fas fa-percent"></i> Similitud: <strong className="texto-peligro" style={{ color: 'var(--color-peligro)' }}>65%</strong></span>
                </div>
              </div>
              <span className="badge badge-peligro"><i className="fas fa-exclamation-circle"></i> Urgente</span>
            </div>
          </div>
          <div className="detalle-grid" style={{ padding: 'var(--space-xl)' }}>
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
          <div className="alerta-roja" style={{ padding: 'var(--space-lg) var(--space-xl)', borderTop: '1px solid var(--color-borde)' }}>
            <h4><i className="fas fa-align-left"></i> Secciones Coincidentes</h4>
            <ul className="lista-coincidencias" style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '10px', marginTop: '12px' }}>
              {coincidencias.map((c, i) => (
                <li key={i} style={{ padding: '8px 12px', background: 'var(--color-fondo)', borderRadius: '6px' }}>
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
