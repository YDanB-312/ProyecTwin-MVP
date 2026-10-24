import { useNavigate } from 'react-router-dom'
import { LockKey } from 'phosphor-react'
import DashboardLayout from '../../../layouts/DashboardLayout/DashboardLayout'
import EmptyState from '../../../components/EmptyState/EmptyState'
import { RUTA_POR_ROL } from '../../../constants/routes'

// Página reutilizable para usuarios autenticados que intentan entrar en una
// sección reservada a otro rol. No expone información de la cuenta ni del
// recurso; el backend sigue siendo la autoridad de autorización.
export default function AccesoDenegado({ role = 'aprendiz' }) {
  const navigate = useNavigate()
  const inicio = RUTA_POR_ROL[role] || '/'

  return (
    <DashboardLayout role={role} titulo="Acceso denegado">
      <EmptyState
        icon={<LockKey size={32} weight="duotone" />}
        title="403 · Acceso denegado"
        message="Tu cuenta está autenticada, pero no tiene permisos para esta sección."
        actionLabel="Volver a mi panel"
        onAction={() => navigate(inicio, { replace: true })}
      />
    </DashboardLayout>
  )
}
