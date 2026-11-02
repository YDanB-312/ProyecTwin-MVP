import { useEffect } from 'react'
import Tooltip from '../Tooltip/Tooltip'
import s from './Pagination.module.css'

export default function Pagination({
  totalItems = 0,
  itemsPerPage = 10,
  paginaActual = 1,
  setPaginaActual,
  itemName = 'elementos',
  showInfo = true,
  filteredCount,
}) {
  const count = filteredCount ?? totalItems
  const totalPages = Math.max(1, Math.ceil(count / itemsPerPage))

  // Si el total encoge (por ejemplo, al borrar el último elemento de la última
  // página), corrige también el estado del padre. Sin esto, el componente
  // mostraba la página recortada pero el padre seguía cortando una página vacía.
  useEffect(() => {
    if (paginaActual > totalPages) {
      setPaginaActual?.(totalPages)
    }
  }, [paginaActual, totalPages, setPaginaActual])

  if (count === 0) return null

  // Clamp defensivo para el render actual.
  const pagina = Math.min(Math.max(1, paginaActual), totalPages)
  const start = (pagina - 1) * itemsPerPage + 1
  const end = Math.min(pagina * itemsPerPage, count)

  const pages = []
  const from = Math.max(1, pagina - 2)
  const to = Math.min(totalPages, pagina + 2)
  if (from > 1) {
    pages.push(1)
    if (from > 2) pages.push('…')
  }
  for (let i = from; i <= to; i++) pages.push(i)
  if (to < totalPages) {
    if (to < totalPages - 1) pages.push('…')
    pages.push(totalPages)
  }

  return (
    <nav className={s.wrap} aria-label="Paginación">
      {showInfo && (
        <p className={s.info}>
          Mostrando <strong>{start}–{end}</strong> de <strong>{count}</strong> {itemName}
        </p>
      )}
      <div className={s.pages}>
        <Tooltip content="Página anterior">
          <button
            type="button"
            className={s.navBtn}
            disabled={pagina === 1}
            onClick={() => setPaginaActual(pagina - 1)}
            aria-label="Página anterior"
          >
            ‹
          </button>
        </Tooltip>
        {pages.map((p, i) =>
          p === '…' ? (
            <span key={`e${i}`} className={s.ellipsis}>…</span>
          ) : (
            <button
              key={p}
              type="button"
              className={`${s.pageBtn} ${p === pagina ? s.active : ''}`}
              aria-current={p === pagina ? 'page' : undefined}
              onClick={() => setPaginaActual(p)}
            >
              {p}
            </button>
          )
        )}
        <Tooltip content="Página siguiente">
          <button
            type="button"
            className={s.navBtn}
            disabled={pagina === totalPages}
            onClick={() => setPaginaActual(pagina + 1)}
            aria-label="Página siguiente"
          >
            ›
          </button>
        </Tooltip>
      </div>
    </nav>
  )
}
