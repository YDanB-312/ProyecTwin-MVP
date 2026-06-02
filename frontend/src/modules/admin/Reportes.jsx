import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/reportes.css'

const similitudes = [
  { num: 1, p1: 'Sistema de gestion Academica', p2: 'Plataforma Educativa SENA', pct: 85, badge: 'peligro', fecha: '10/04/2026' },
  { num: 2, p1: 'App Movil para Turismo', p2: 'Turismo Colombia Digital', pct: 72, badge: 'peligro', fecha: '09/04/2026' },
  { num: 3, p1: 'Sistema IoT Agricultura', p2: 'Monitoreo Agricola Inteligente', pct: 58, badge: 'advertencia', fecha: '08/04/2026' },
  { num: 4, p1: 'E-commerce Artesanias', p2: 'Mercado Artesanal Virtual', pct: 45, badge: 'advertencia', fecha: '07/04/2026' },
  { num: 5, p1: 'Chatbot Soporte Tecnico', p2: 'Asistente Virtual SENA', pct: 30, badge: 'exito', fecha: '06/04/2026' },
]

const instructores = [
  { rank: 1, nombre: 'Carlos Rodriguez Diaz', area: 'desarrollo de Software', revisiones: 45, rankClase: 'ranking-1' },
  { rank: 2, nombre: 'Maria Gonzalez Torres', area: 'Analisis y desarrollo de Software', revisiones: 38, rankClase: 'ranking-2' },
  { rank: 3, nombre: 'Andres Martinez Lopez', area: 'Programacion de Software', revisiones: 32, rankClase: 'ranking-3' },
  { rank: 4, nombre: 'Laura Sanchez Perez', area: 'desarrollo de Software', revisiones: 28, rankClase: '' },
  { rank: 5, nombre: 'Diego Ramirez Castro', area: 'Infraestructura de Redes', revisiones: 22, rankClase: '' },
  { rank: 6, nombre: 'Patricia Morales Vega', area: 'desarrollo de Medios Audiovisuales', revisiones: 18, rankClase: '' },
]

export default function Reportes() {
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12}>
      <div className="contenedor-gestion">
        <PageHeader title="Reportes y Estadisticas" icon="chart-bar" />

        <DataPanel
          title="Ultimas Similitudes Detectadas"
          icon="clone"
          action={<span className="total-registros">Mostrando 5 de 15 Similitudes</span>}
        >
          <table className="tabla-similitudes">
            <thead>
              <tr>
                <th>#</th>
                <th>Proyecto 1</th>
                <th>Proyecto 2</th>
                <th>Porcentaje</th>
                <th>Fecha</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {similitudes.map((s, i) => (
                <tr key={i}>
                  <td>{s.num}</td>
                  <td>{s.p1}</td>
                  <td>{s.p2}</td>
                  <td><span className={`badge badge-${s.badge}`}>{s.pct}%</span></td>
                  <td>{s.fecha}</td>
                  <td>
                    <div className="acciones-tabla">
                      <Link to="/admin/detalle-similitud" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      <Link to="/admin/detalle-similitud" className="btn-accion-tabla btn-ver" title="Revisar"><i className="fas fa-search"></i></Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </DataPanel>

        <div className="seccion-reporte">
          <h2 className="titulo-seccion"><i className="fas fa-trophy"></i> Top Instructores</h2>
          <div className="grid-instructores">
            {instructores.map((inst, i) => (
              <div key={i} className="tarjeta-instructor">
                <div className={`tarjeta-instructor-ranking ${inst.rankClase}`}>{inst.rank}</div>
                <div className="tarjeta-instructor-avatar"><i className="fas fa-user-tie"></i></div>
                <div className="tarjeta-instructor-info">
                  <h4>{inst.nombre}</h4>
                  <p>{inst.area}</p>
                </div>
                <div className="tarjeta-instructor-revisiones">
                  <div className="numero">{inst.revisiones}</div>
                  <div className="texto">Revisiones</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="acciones-exportar">
          <button className="btn-secundario" type="button"><i className="fas fa-file-csv"></i> Exportar CSV</button>
          <button className="btn-secundario" type="button"><i className="fas fa-file-pdf"></i> Generar PDF</button>
        </div>
      </div>
    </DashboardLayout>
  )
}
