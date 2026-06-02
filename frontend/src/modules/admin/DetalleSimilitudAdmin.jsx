import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'

const coincidencias = [
  { label: 'Descripcion', pct: 72 },
  { label: 'Tecnologias', pct: 60 },
  { label: 'Objetivos', pct: 55 },
  { label: 'Metodologia', pct: 45 },
]

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/admin/reportes', label: 'Reportes' },
  { label: 'Detalle Similitud' },
]

export default function DetalleSimilitudAdmin() {
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12}>
      <div className="contenedor-gestion">
        <PageHeader
          title="Detalle de Similitud"
          icon="exclamation-triangle"
          breadcrumb={breadcrumb}
          actions={<Link to="/admin/reportes" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Reportes</Link>}
        />

        <DataPanel title="Informacion General" icon="info-circle">
          <div style={{ padding: 'var(--space-xl)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 'var(--space-lg)' }}>
              <div>
                <div className="detalle-label">ID Similitud</div>
                <p className="valor-similitud-id" style={{ fontSize: '1.2rem', fontWeight: 700 }}>SIM-001</p>
              </div>
              <div>
                <div className="detalle-label">Porcentaje</div>
                <p><span className="badge badge-peligro badge-lg">85%</span></p>
              </div>
            </div>
            <div className="detalle-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 'var(--space-lg)' }}>
              <div className="card-proyecto-compacto">
                <h4 className="card-titulo-verde"><i className="fas fa-file-alt"></i> Proyecto 1</h4>
                <p className="info-linea">Sistema de Gestion Academica</p>
                <p className="detalle-linea"><strong>Creador:</strong> Juan Perez (Aprendiz)</p>
                <p className="detalle-linea"><strong>Fecha:</strong> 15/03/2026</p>
              </div>
              <div className="card-proyecto-compacto">
                <h4 className="card-titulo-amarillo"><i className="fas fa-file-alt"></i> Proyecto 2</h4>
                <p className="info-linea">Plataforma Educativa SENA</p>
                <p className="detalle-linea"><strong>Creador:</strong> Ana Martinez (Aprendiz)</p>
                <p className="detalle-linea"><strong>Fecha:</strong> 02/02/2026</p>
              </div>
            </div>
            <div className="alerta-roja" style={{ marginTop: 'var(--space-lg)' }}>
              <h4><i className="fas fa-list"></i> Detalles de Coincidencia</h4>
              <div className="grid-coincidencias" style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '10px', marginTop: '12px' }}>
                {coincidencias.map((c, i) => (
                  <div key={i} className="card-coincidencia" style={{ padding: '8px 12px', background: 'var(--color-fondo)', borderRadius: '6px' }}>
                    <strong>{c.label}:</strong> {c.pct}% coincidencia
                  </div>
                ))}
              </div>
            </div>
          </div>
        </DataPanel>

        <div style={{ marginTop: 'var(--space-lg)', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
          <button type="button" className="btn-primario"><i className="fas fa-check"></i> Resolver Similitud</button>
          <button type="button" className="btn-secundario"><i className="fas fa-robot"></i> Analizar con IA</button>
          <Link to="/admin/reportes" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Reportes</Link>
        </div>
      </div>
    </DashboardLayout>
  )
}
