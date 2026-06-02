import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'
import '../../assets/styles/pages/mis-proyectos.css'

const miembros = [
  { iniciales: 'AM', nombre: 'Ana Martinez', rol: 'Creador / Lider', clase: '' },
  { iniciales: 'JP', nombre: 'Juan Perez', rol: 'Integrante', clase: 'avatar-secundario' },
  { iniciales: 'LG', nombre: 'Laura Gomez', rol: 'Integrante', clase: 'avatar-secundario' },
]

const observaciones = [
  { autor: 'Carlos Ruiz | Instructor', icono: 'user-tie', fecha: '10 may 2026', texto: 'El proyecto necesita mejorar la seccion de analisis de requisitos. Se recomienda ampliar la documentacion tecnica antes de continuar con el desarrollo.' },
  { autor: 'Maria Gonzalez | Aprendiz', icono: 'user-graduate', fecha: '8 may 2026', texto: 'He realizado los ajustes sugeridos en la documentacion. La nueva version incluye diagramas de flujo y casos de uso detallados. Quedo atento a mas retroalimentacion.' },
  { autor: 'Carlos Ruiz | Instructor', icono: 'user-tie', fecha: '6 may 2026', texto: 'La propuesta inicial tiene buen enfoque, pero falta definir mejor los entregables del primer sprint. Recomiendo revisar la guia de proyectos para alinear expectativas.' },
]

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Detalle del Proyecto' },
]

function DetalleProyectoInstructor() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-revision">
        <PageHeader
          title="Detalle del Proyecto"
          icon="folder-open"
          breadcrumb={breadcrumb}
          actions={<Link to="/instructor/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Informacion General" icon="info-circle">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Nombre del Proyecto</div>
              <div className="detalle-valor">Sistema IoT para Agricultura de Precision</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className="badge badge-exito">Aprobado</span> <span className="badge badge-primario"><i className="fas fa-robot"></i> Analizado por IA</span></p>
            </div>
            <div>
              <div className="detalle-label">Aprendiz</div>
              <div className="detalle-valor">Ana Martinez</div>
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
              <div className="detalle-label">Instructor Asignado</div>
              <div className="detalle-valor">Carlos Ruiz</div>
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
              <div key={i} className="flex-row flex-wrap" style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '8px 0' }}>
                <div className={`avatar-miembro avatar-sm ${m.clase}`}>{m.iniciales}</div>
                <div>
                  <strong className="texto-md">{m.nombre}</strong>
                  <br /><span className="texto-claro">{m.rol}</span>
                </div>
              </div>
            ))}
          </div>
        </DataPanel>

        <DataPanel title="Observaciones del Proyecto" icon="comments">
          <div className="lista-observaciones">
            {observaciones.map((obs, i) => (
              <div key={i} className="observacion-item">
                <div className="observacion-header">
                  <span className="observacion-autor"><i className={`fas fa-${obs.icono}`}></i> {obs.autor}</span>
                  <span className="observacion-fecha">{obs.fecha}</span>
                  <div className="observacion-acciones">
                    <button type="button" className="btn-icono editar" title="Editar observacion"><i className="fas fa-edit"></i></button>
                    <button type="button" className="btn-icono eliminar" title="Eliminar observacion"><i className="fas fa-trash-alt"></i></button>
                  </div>
                </div>
                <div className="observacion-contenido">
                  <p>{obs.texto}</p>
                </div>
              </div>
            ))}
          </div>
          <div style={{ padding: 'var(--space-xl)', borderTop: '1px solid var(--color-borde)' }}>
            <h3><i className="fas fa-plus-circle"></i> Agregar Observacion</h3>
            <form action="#">
              <div className="grupo-formulario" style={{ marginTop: '12px' }}>
                <label htmlFor="observacion" className="etiqueta">Comentario</label>
                <textarea id="observacion" className="textarea" placeholder="Escribe tu observacion sobre el proyecto..." name="contenido"></textarea>
              </div>
              <div className="acciones-formulario" style={{ marginTop: '12px' }}>
                <button type="submit" className="btn-primario"><i className="fas fa-paper-plane"></i> Guardar Observacion</button>
              </div>
            </form>
          </div>
        </DataPanel>

        <div className="acciones-finales">
          <Link to="/instructor/dashboard" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
        </div>
      </div>
    </DashboardLayout>
  );
}

export default DetalleProyectoInstructor;
