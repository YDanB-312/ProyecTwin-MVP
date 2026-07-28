import { useState } from 'react'

export default function FilterBar({ title, children, actions }) {
  const [expanded, setExpanded] = useState(false)

  return (
    <div className="filter-bar">
      <div className="filter-bar-header" onClick={() => setExpanded(prev => !prev)}>
        <span className="filter-bar-titulo"><i className="fas fa-filter"></i> {title || 'Filtros'}</span>
        <i className={`fas fa-chevron-${expanded ? 'up' : 'down'} filter-bar-toggle`}></i>
      </div>
      <div className={`filter-bar-body${expanded ? ' filter-bar-open' : ''}`}>
        <div className="filter-bar-grid">
          {children}
        </div>
        {actions && <div className="filter-bar-acciones">{actions}</div>}
      </div>
    </div>
  )
}
