import AlertasBase from '../../components/AlertasBase/AlertasBase'

const filters = [
  {
    name: 'tipo', id: 'tipo-alerta', label: 'Tipo de Alerta',
    options: [
      { value: '', label: 'Todos los tipos' },
      { value: 'similitud', label: 'Similitud de proyectos' },
      { value: 'revision', label: 'Comentarios del instructor' },
      { value: 'sistema', label: 'Notificaciones del sistema' },
    ],
  },
  {
    name: 'leida', id: 'estado-alerta', label: 'Estado',
    options: [
      { value: '', label: 'Todos los estados' },
      { value: false, label: 'No leídas' },
      { value: true, label: 'Leídas' },
    ],
  },
  {
    name: 'proyecto', id: 'proyecto-alerta', label: 'Proyecto',
    options: [
      { value: '', label: 'Todos los proyectos' },
      { value: 'Sistema de Gestion Academica', label: 'Sistema de Gestion Academica' },
      { value: 'App Movil para Inventarios', label: 'App Movil para Inventarios' },
    ],
  },
]

const notificacionesData = [
  { icono: 'chart-line', titulo: 'Alta Similitud Detectada', descripcion: 'Tu proyecto "Sistema de Gestion Academica" tiene un 65% de similitud con otro proyecto existente en la plataforma. Se recomienda revisar y ajustar tu propuesta.', tiempo: 'Hace 2 horas', proyecto: 'Sistema de Gestion Academica', tipo: 'similitud', tipoLabel: 'Similitud', leida: false, enlace: '/aprendiz/detalle-similitud', textoEnlace: 'Revisar Similitud', iconoEnlace: 'search', state: { desde: 'alertas', proyecto: 'Sistema de Gestion Academica' } },
  { icono: 'clock', titulo: 'Revisión Pendiente', descripcion: 'Tu instructor Carlos Ruiz tiene pendiente la revisión de tu proyecto "App Movil para Inventarios". Recibirás notificaciones cuando haya comentarios.', tiempo: 'Ayer', proyecto: 'App Movil para Inventarios', tipo: 'revision', tipoLabel: 'Revisión', leida: false, enlace: '/aprendiz/detalle-proyecto/1', textoEnlace: 'Ver proyecto', iconoEnlace: 'eye' },
  { icono: 'comment', titulo: 'Comentario del Instructor', descripcion: 'Carlos Ruiz ha agregado comentarios a tu proyecto "Sistema de Gestion Academica". Revisa los comentarios y realiza los ajustes necesarios.', tiempo: 'Hace 3 días', proyecto: 'Sistema de Gestion Academica', tipo: 'mensaje', tipoLabel: 'Mensaje', leida: true, enlace: '/aprendiz/detalle-proyecto/0', textoEnlace: 'Ver Comentarios', iconoEnlace: 'eye' },
  { icono: 'check-circle', titulo: 'Proyecto Aprobado', descripcion: 'Felicidades! Tu proyecto "App Movil para Inventarios" ha sido aprobado por el instructor. Puedes continuar con el desarrollo.', tiempo: 'Hace 1 semana', proyecto: 'App Movil para Inventarios', tipo: 'sistema', tipoLabel: 'Sistema', leida: true, enlace: '/aprendiz/detalle-proyecto/1', textoEnlace: 'Ver proyecto', iconoEnlace: 'eye' },
]

export default function AlertasAprendiz() {
  return (
    <AlertasBase
      role="aprendiz"
      dashboardTitulo="ProyecTwin - Panel del Aprendiz"
      dashboardUsuario="Maria Gonzalez | ADSO"
      notificaciones={5}
      breadcrumb={[
        { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
        { label: 'Notificaciones' },
      ]}
      filters={filters}
      notificacionesData={notificacionesData}
      volverPath="/aprendiz/dashboard"
    />
  )
}
