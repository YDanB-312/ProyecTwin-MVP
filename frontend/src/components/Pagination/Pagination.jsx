export default function Pagination({ totalItems, itemsPerPage, paginaActual, setPaginaActual, itemName = '', showInfo = false, filteredCount }) {
  const totalPaginas = Math.max(1, Math.ceil(totalItems / itemsPerPage))

  if (totalPaginas <= 1) return null

  return (
    <div className="contenedor-paginacion">
      {showInfo && (
        <span className="info-paginacion">
          Mostrando {filteredCount !== undefined ? filteredCount : Math.min(itemsPerPage, totalItems)} de {totalItems} {itemName}
        </span>
      )}
      <div className="paginacion">
        <button className="btn-paginacion" disabled={paginaActual === 1} type="button" aria-label="Página anterior"
          onClick={() => setPaginaActual(prev => Math.max(1, prev - 1))}>
          <i className="fas fa-chevron-left"></i>
        </button>
        {Array.from({ length: totalPaginas }, (_, i) => i + 1).map(p => (
          <button key={p} className={`btn-paginacion${paginaActual === p ? ' activo' : ''}`}
            type="button" onClick={() => setPaginaActual(p)}>
            {p}
          </button>
        ))}
        <button className="btn-paginacion" disabled={paginaActual === totalPaginas} type="button" aria-label="Página siguiente"
          onClick={() => setPaginaActual(prev => Math.min(totalPaginas, prev + 1))}>
          <i className="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>
  )
}
