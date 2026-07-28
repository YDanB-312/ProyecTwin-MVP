import { useAuth } from '../../contexts/AuthContext'
import AlertasBase from '../../components/AlertasBase/AlertasBase'

const filters = [
  {
    name: 'tipo', id: 'tipo-alerta', label: 'Tipo de Alerta',
    options: [
      { value: '', label: 'Todos los tipos' },
      { value: 'similitud', label: 'Similitud de proyectos' },
      { value: 'revision', label: 'Comentarios y revisiones' },
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
      { value: 'Sistema IoT para Agricultura', label: 'Sistema IoT para Agricultura' },
      { value: 'App Movil para Turismo Local', label: 'App Movil para Turismo Local' },
    ],
  },
]

const notificacionesData = [
  { icono: 'exclamation-triangle', iconoClase: 'peligro', titulo: 'Similitud Urgente Detectada', descripcion: 'El proyecto "Sistema de Gestion Academica" de Maria Gonzalez tiene un 65% de similitud con el proyecto "Plataforma Educativa SENA" registrado anteriormente. Se requiere revisión inmediata.', tiempo: 'Hace 1 hora', proyecto: 'Sistema de Gestion Academica', tipo: 'similitud', tipoLabel: 'Similitud', leida: false, enlace: '/instructor/detalle-similitud', textoEnlace: 'Revisar Similitud', iconoEnlace: 'search', state: { proyecto: 'Sistema de Gestion Academica', desde: 'alertas' } },
  { icono: 'exclamation-triangle', iconoClase: 'peligro', titulo: 'Alta Similitud en App Móvil', descripcion: 'El proyecto "App Movil para Inventarios" de Maria Gonzalez presenta un 58% de similitud con "Control de Stock Digital". Verificar originalidad de la propuesta.', tiempo: 'Hace 3 horas', proyecto: 'App Movil para Inventarios', tipo: 'similitud', tipoLabel: 'Similitud', leida: false, enlace: '/instructor/detalle-similitud', textoEnlace: 'Revisar Similitud', iconoEnlace: 'search', state: { proyecto: 'App Movil para Inventarios', desde: 'alertas' } },
  { icono: 'exchange-alt', iconoClase: 'advertencia', titulo: 'Cambio de Estado del Proyecto', descripcion: 'El proyecto "Sistema IoT para Agricultura" de Ana Martínez fue aprobado por el instructor y pasa a estado "En desarrollo". El aprendiz ha sido notificado.', tiempo: 'Hace 5 horas', proyecto: 'Sistema IoT para Agricultura', tipo: 'revision', tipoLabel: 'Revisión', leida: true, enlace: '/instructor/detalle-proyecto/1', textoEnlace: 'Ver proyecto', iconoEnlace: 'eye', state: { desde: 'alertas' } },
  { icono: 'bullhorn', iconoClase: 'informativa', titulo: 'Recordatorio: Revisiones Pendientes', descripcion: 'Tienes 4 proyectos pendientes por revisar. Recuerda proporcionar retroalimentación detallada a los aprendices.', tiempo: 'Hace 2 días', proyecto: 'Todos los proyectos', tipo: 'sistema', tipoLabel: 'Sistema', leida: true, enlace: '/instructor/revision-propuestas', textoEnlace: 'Ver Revisiones', iconoEnlace: 'file-alt' },
  { icono: 'info-circle', iconoClase: 'informativa', titulo: 'Nuevo Proyecto Registrado', descripcion: 'Juan Pérez ha registrado un nuevo proyecto titulado "Plataforma de Gestion de Practicas". El proyecto esta en estado "Pendiente" y requiere revisión para asignación.', tiempo: 'Ayer', proyecto: 'Plataforma de Gestion de Practicas', tipo: 'sistema', tipoLabel: 'Sistema', leida: true, enlace: '/instructor/revision-propuestas', textoEnlace: 'Revisar Propuesta', iconoEnlace: 'search' },
]

export default function AlertasInstructor() {
  const { user } = useAuth()
  return (
    <AlertasBase
      role="instructor"
      dashboardTitulo="ProyecTwin - Panel del Instructor"
      dashboardUsuario={user?.nombre || 'Usuario'}
      notificaciones={0}
      breadcrumb={[
        { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
        { label: 'Notificaciones' },
      ]}
      filters={filters}
      notificacionesData={notificacionesData}
      volverPath="/instructor/dashboard"
      iconoMarca="fa-check-double"
    />
  )
}
