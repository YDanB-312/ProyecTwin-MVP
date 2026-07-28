export default function EmptyState({ icon = 'inbox', title = 'Sin resultados', message = 'No hay elementos para mostrar.', actionLabel, onAction, actionIcon }) {
  return (
    <div className="data-panel fade-in">
      <div className="data-panel-body" style={{ textAlign: 'center', padding: 'var(--space-2xl)' }}>
        <div style={{ fontSize: '2.5rem', color: 'var(--color-texto-muted)', marginBottom: 'var(--space-md)' }}>
          <i className={`fas fa-${icon}`}></i>
        </div>
        <h3 style={{ fontSize: 'var(--text-lg)', fontWeight: 600, color: 'var(--color-texto)', marginBottom: 'var(--space-sm)' }}>
          {title}
        </h3>
        <p style={{ color: 'var(--color-texto-claro)', marginBottom: actionLabel ? 'var(--space-lg)' : 0, maxWidth: 400, margin: '0 auto' }}>
          {message}
        </p>
        {actionLabel && onAction && (
          <button className="btn-primario" style={{ marginTop: 'var(--space-lg)' }} onClick={onAction}>
            {actionIcon && <i className={`fas fa-${actionIcon}`}></i>} {actionLabel}
          </button>
        )}
      </div>
    </div>
  )
}
