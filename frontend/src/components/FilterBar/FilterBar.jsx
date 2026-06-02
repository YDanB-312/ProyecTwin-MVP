export default function FilterBar({ title, children, actions }) {
  return (
    <div className="filter-bar">
      <div className="filter-bar-header">
        <span className="filter-bar-titulo"><i className="fas fa-filter"></i> {title || 'Filtros'}</span>
      </div>
      <div className="filter-bar-body">
        <div className="filter-bar-grid">
          {children}
        </div>
        {actions && <div className="filter-bar-acciones">{actions}</div>}
      </div>
    </div>
  )
}
