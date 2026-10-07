// Estados de una consulta a la API: cargando, error y vacío.
//
//   <ApiState cargando={cargando} error={error} vacio={!filas.length} onReintentar={recargar}>
//     ...contenido...
//   </ApiState>
import Button from '../Button/Button'
import EmptyState from '../EmptyState/EmptyState'
import LatticeLoader from '../LatticeLoader/LatticeLoader'
import { Warning, ArrowClockwise } from 'phosphor-react'
import s from './ApiState.module.css'

// Mensaje útil según el status (evita "Failed to fetch" / "Error 500" crudos).
function mensajeDeError(error) {
  const status = Number(error?.status)
  const delBackend = error?.data?.message
  if (status === 0) return 'No se pudo conectar con el servidor. Revisa tu conexión.'
  if (status === 401) return 'Tu sesión expiró. Inicia sesión nuevamente.'
  if (status === 403) return delBackend || 'No tienes permiso para ver esta información.'
  if (status === 404) return 'No encontramos lo que buscas.'
  if (status >= 500) return 'Ocurrió un error en el servidor. Intenta de nuevo.'
  return delBackend || error?.message || 'No se pudo cargar la información.'
}

export default function ApiState({ cargando, error, vacio = false, onReintentar, mensajeVacio, iconoVacio, children }) {
  if (cargando) {
    return (
      <div className={s.cargando}>
        <LatticeLoader
          label="Cargando información"
          pattern="orbit"
          grid={3}
          shape="round"
          showTimer={false}
          idleOpacity={0.25}
        />
      </div>
    )
  }

  if (error) {
    return (
      <div className={s.error} role="alert">
        <p className={s.errorTexto}>
          <Warning size={16} weight="fill" /> {mensajeDeError(error)}
        </p>
        {onReintentar && (
          <Button type="button" variant="secondary" size="sm" onClick={onReintentar}>
            <ArrowClockwise size={14} /> Reintentar
          </Button>
        )}
      </div>
    )
  }

  if (vacio) {
    return <EmptyState icon={iconoVacio} message={mensajeVacio} />
  }

  return children
}
