import { useAuth } from '../../contexts/AuthContext'
import AlertasBase from '../../components/AlertasBase/AlertasBase'

const filters = [
  {
    name: 'tipo', id: 'tipo-alerta', label: 'Tipo de Alerta',
    options: [
      { value: '', label: 'Todos los tipos' },
      { value: 'similitud', label: 'Similitud' },
      { value: 'revision', label: 'Revisión' },
      { value: 'mensaje', label: 'Mensaje' },
      { value: 'sistema', label: 'Sistema' },
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
    name: 'proyecto', id: 'modulo-alerta', label: 'Módulo',
    options: [
      { value: '', label: 'Todos los módulos' },
      { value: 'Módulo Usuarios', label: 'Usuarios' },
      { value: 'Módulo Reportes', label: 'Reportes de falla' },
      { value: 'Módulo Similitudes', label: 'Similitudes' },
      { value: 'Módulo Sistema', label: 'Sistema' },
    ],
  },
]

const notificacionesData = [
  { icono: 'user-plus', titulo: 'Nuevo Usuario Registrado', descripcion: 'Se ha registrado un nuevo aprendiz en el programa ADSO. Revisa los detalles y verifica que la información sea correcta antes de activar la cuenta.', tiempo: 'Hace 1 hora', proyecto: 'Módulo Usuarios', tipo: 'sistema', tipoLabel: 'Sistema', leida: false, enlace: '/admin/gestion-usuarios', textoEnlace: 'Ver Usuario', iconoEnlace: 'eye' },
  { icono: 'bug', titulo: 'Reporte de Falla Recibido', descripcion: 'Carlos Rodriguez ha reportado una falla en el módulo de similitudes. Se requiere gestionar el reporte y asignarlo al equipo técnico correspondiente.', tiempo: 'Hace 2 horas', proyecto: 'Módulo Reportes', tipo: 'revision', tipoLabel: 'Revisión', leida: false, enlace: '/admin/detalle-reporte', textoEnlace: 'Gestionar Reporte', iconoEnlace: 'wrench' },
  { icono: 'shield-alt', titulo: 'Alerta de Seguridad', descripcion: 'Se ha detectado un intento de acceso no autorizado desde una dirección IP desconocida. Revisa los registros de actividad del sistema para identificar la fuente.', tiempo: 'Ayer', proyecto: 'Módulo Sistema', tipo: 'sistema', tipoLabel: 'Sistema', leida: false, enlace: '/admin/dashboard', textoEnlace: 'Revisar Actividad', iconoEnlace: 'search' },
  { icono: 'chart-line', titulo: 'Similitud Crítica Detectada', descripcion: 'El proyecto "Sistema de Gestión Académica" presenta un 65% de similitud con otro proyecto. Se requiere notificar al instructor asignado para revisión manual.', tiempo: 'Hace 3 dias', proyecto: 'Módulo Similitudes', tipo: 'similitud', tipoLabel: 'Similitud', leida: true, enlace: '/admin/similitudes', textoEnlace: 'Revisar Similitud', iconoEnlace: 'search' },
]

export default function NotificacionesAdmin() {
  const { user } = useAuth()
  return (
    <AlertasBase
      role="admin"
      dashboardTitulo="ProyecTwin - Panel de Administración"
      dashboardUsuario={user?.nombre || 'Usuario'}
      notificaciones={0}
      breadcrumb={[
        { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
        { label: 'Notificaciones' },
      ]}
      filters={filters}
      notificacionesData={notificacionesData}
      volverPath="/admin/dashboard"
    />
  )
}
