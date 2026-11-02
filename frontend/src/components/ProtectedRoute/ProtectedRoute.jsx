import { Navigate } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'
import AccesoDenegado from '../../pages/public/AccesoDenegado/AccesoDenegado'
import s from './ProtectedRoute.module.css'

// Autoriza rutas privadas. La sesión vive en una cookie httpOnly, así que hay
// que esperar a que AuthContext resuelva /auth/me antes de decidir.
export default function ProtectedRoute({ allowedRoles, children }) {
  const { user, isAuthenticated, cargando } = useAuth()
  if (cargando) {
    return (
      <p className={s.loading} role="status" aria-live="polite" aria-busy="true">
        Cargando sesión…
      </p>
    )
  }
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (allowedRoles && !allowedRoles.includes(user.rol)) return <AccesoDenegado role={user.rol} />
  return children
}
