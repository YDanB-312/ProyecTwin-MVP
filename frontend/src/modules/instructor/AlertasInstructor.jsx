import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/alertas.css'

const alertas = [
  {
    tipo: 'urgente', icono: 'exclamation-triangle',
    titulo: 'Similitud Urgente Detectada',
    desc: 'El proyecto "Sistema de Gestion Academica" de Juan Perez tiene un 65% de similitud con el proyecto "Plataforma Administrativa Escolar" registrado en el trimestre anterior. Se requiere revision inmediata.',
    fecha: 'Hace 1 hora', proyecto: 'Sistema de Gestion Academica', badge: 'peligro', badgeTexto: 'Urgente',
    link: '/instructor/detalle-similitud', linkTexto: 'Revisar Similitud'
  },
  {
    tipo: 'urgente', icono: 'exclamation-triangle',
    titulo: 'Alta Similitud en App Movil',
    desc: 'El proyecto "App Movil para Inventarios" de Carlos Ruiz presenta un 58% de similitud con "Control de Stock Digital". Verificar originalidad de la propuesta.',
    fecha: 'Hace 3 horas', proyecto: 'App Movil para Inventarios', badge: 'peligro', badgeTexto: 'Urgente',
    link: '/instructor/detalle-similitud', linkTexto: 'Revisar Similitud'
  },
  {
    tipo: 'advertencia', icono: 'exchange-alt',
    titulo: 'Cambio de Estado del Proyecto',
    desc: 'El proyecto "Sistema IoT para Agricultura" de Ana Martinez fue aprobado por el instructor y pasa a estado "En desarrollo". El aprendiz ha sido notificado.',
    fecha: 'Hace 5 horas', proyecto: 'Sistema IoT para Agricultura', badge: 'advertencia', badgeTexto: 'Advertencia',
    link: '/instructor/detalle-proyecto', linkTexto: 'Ver proyecto'
  },
  {
    tipo: 'informativa', icono: 'bullhorn',
    titulo: 'Recordatorio: Revisiones Pendientes',
    desc: 'Tienes 4 proyectos pendientes por revisar. Recuerda proporcionar retroalimentacion detallada a los aprendices.',
    fecha: 'Hace 2 dias', proyecto: 'Todos los proyectos', badge: 'primario', badgeTexto: 'Informativa',
    link: '/instructor/revision-propuestas', linkTexto: 'Ver Revisiones'
  },
  {
    tipo: 'informativa', icono: 'info-circle',
    titulo: 'Nuevo Proyecto Registrado',
    desc: 'Juan Perez ha registrado un nuevo proyecto titulado "Plataforma de Gestion de Practicas". El proyecto esta en estado "Pendiente" y requiere revision para asignacion.',
    fecha: 'Ayer', proyecto: 'Plataforma de Gestion de Practicas', badge: 'primario', badgeTexto: 'Informativa',
    link: '/instructor/revision-propuestas', linkTexto: 'Revisar Propuesta'
  },
]

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { label: 'Notificaciones' },
]

function AlertasInstructor() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-alertas">
        <PageHeader
          title="Notificaciones"
          icon="bell"
          breadcrumb={breadcrumb}
          actions={<button className="btn-secundario" type="button"><i className="fas fa-check-double"></i> Marcar todas como leidas</button>}
        />

        <section className="seccion-filtros">
          <h3 className="filtros-titulo"><i className="fas fa-filter"></i> Filtrar Notificaciones</h3>
          <div className="contenedor-filtros">
            <div className="grupo-filtro">
              <label htmlFor="tipo-alerta">Tipo de Alerta</label>
              <select id="tipo-alerta" className="select-filtro" name="tipo_alerta">
                <option value="">Todos los tipos</option>
                <option value="similitud">Similitud de proyectos</option>
                <option value="revision">Comentarios y revisiones</option>
                <option value="sistema">Notificaciones del sistema</option>
              </select>
            </div>
            <div className="grupo-filtro">
              <label htmlFor="estado-alerta">Estado</label>
              <select id="estado-alerta" className="select-filtro" name="estado_alerta">
                <option value="">Todos los estados</option>
                <option value="no-leida">No leidas</option>
                <option value="leida">Leidas</option>
              </select>
            </div>
            <div className="grupo-filtro">
              <label htmlFor="proyecto-alerta">Proyecto</label>
              <select id="proyecto-alerta" className="select-filtro" name="proyecto_alerta">
                <option value="">Todos los proyectos</option>
                <option value="sistema-gestion">Sistema de Gestion Academica</option>
                <option value="app-inventarios">App Movil para Inventarios</option>
                <option value="iot-agricultura">Sistema IoT para Agricultura</option>
                <option value="turismo-local">App Movil para Turismo Local</option>
              </select>
            </div>
            <div className="grupo-filtro">
              <label htmlFor="fecha-alerta">Fecha</label>
              <select id="fecha-alerta" className="select-filtro" name="fecha_alerta">
                <option value="">Cualquier fecha</option>
                <option value="hoy">Hoy</option>
                <option value="semana">Ultima semana</option>
                <option value="mes">Ultimo mes</option>
              </select>
            </div>
          </div>
        </section>

        <div className="lista-alertas">
          {alertas.map((a, i) => (
            <div key={i} className={`tarjeta-alerta alerta-${a.tipo}`}>
              <div className="contenido-alerta">
                <div className="icono-alerta"><i className={`fas fa-${a.icono}`}></i></div>
                <div className="info-alerta">
                  <h3>{a.titulo}</h3>
                  <p>{a.desc}</p>
                  <div className="meta-alerta">
                    <div>
                      <span className="fecha-alerta">{a.fecha}</span>
                      <span className="proyecto-alerta">{a.proyecto}</span>
                      <span className={`badge badge-${a.badge}`}>{a.badgeTexto}</span>
                    </div>
                  </div>
                  <div className="acciones-alerta">
                    <Link to={a.link} className="btn-primario"><i className="fas fa-search"></i> {a.linkTexto}</Link>
                    <button className="btn-primario" type="button"><i className="fas fa-check"></i> Marcar como leida</button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="paginacion">
          <button className="btn-paginacion" type="button"><i className="fas fa-chevron-left"></i></button>
          <button className="btn-paginacion activa" type="button">1</button>
          <button className="btn-paginacion" type="button">2</button>
          <button className="btn-paginacion" type="button">3</button>
          <button className="btn-paginacion" type="button"><i className="fas fa-chevron-right"></i></button>
        </div>
      </div>
    </DashboardLayout>
  );
}

export default AlertasInstructor;
