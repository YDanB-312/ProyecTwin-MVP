import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'
import '../../assets/styles/pages/mis-proyectos.css'

const miembros = [
  { iniciales: 'MG', nombre: 'Maria Gonzalez', rol: 'Creador / Lider', clase: '' },
  { iniciales: 'JP', nombre: 'Juan Perez', rol: 'Integrante', clase: 'avatar-secundario' },
  { iniciales: 'LG', nombre: 'Laura Gomez', rol: 'Integrante', clase: 'avatar-secundario' },
]

const breadcrumb = [
  { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/aprendiz/mis-proyectos', label: 'Mis Proyectos' },
  { label: 'Detalle del Proyecto' },
]

function DetalleProyecto() {
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-proyectos">
        <PageHeader
          title="Detalle del Proyecto"
          icon="folder-open"
          breadcrumb={breadcrumb}
          actions={<Link to="/aprendiz/mis-proyectos" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Informacion General" icon="info-circle">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Nombre del Proyecto</div>
              <div className="detalle-valor">Sistema IoT para Agricultura de Precision</div>
            </div>
            <div>
              <div className="detalle-label">Programa de Formacion</div>
              <div className="detalle-valor">ADSO - Analisis y Desarrollo de Sistemas</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de Creacion</div>
              <div className="detalle-valor">15/03/2026</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className="badge badge-exito">Aprobado</span> <span className="badge badge-primario"><i className="fas fa-robot"></i> Analizado por IA</span></p>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Descripcion</div>
              <div className="detalle-valor-texto">Desarrollo de un sistema basado en sensores IoT para monitorear variables ambientales en cultivos, permitiendo la toma de decisiones en tiempo real para optimizar el riego y la fertilizacion.</div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Integrantes del Equipo" icon="users">
          <div className="detalle-grid-moderno">
            {miembros.map((m, i) => (
              <div key={i} className="flex-row flex-wrap" style={{ padding: '8px 0' }}>
                <div className={`avatar-miembro avatar-sm ${m.clase}`}>{m.iniciales}</div>
                <div>
                  <strong className="texto-md">{m.nombre}</strong>
                  <br /><span className="texto-claro">{m.rol}</span>
                </div>
              </div>
            ))}
          </div>
        </DataPanel>

        <DataPanel title="Observaciones del Instructor" icon="comments">
          <div className="lista-observaciones">
            <div className="observacion-item">
              <div className="observacion-header">
                <span className="observacion-autor"><i className="fas fa-user-tie"></i> Carlos Ruiz | Instructor</span>
                <span className="observacion-fecha">10 may 2026</span>
              </div>
              <div className="observacion-contenido">
                <p>El proyecto necesita mejorar la seccion de analisis de requisitos. Se recomienda ampliar la documentacion tecnica antes de continuar con el desarrollo.</p>
              </div>
            </div>
          </div>
        </DataPanel>

        <div className="margen-superior">
          <Link to="/aprendiz/mis-proyectos" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
        </div>
      </div>
    </DashboardLayout>
  );
}

export default DetalleProyecto;
