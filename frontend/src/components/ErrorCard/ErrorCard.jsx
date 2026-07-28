export default function ErrorCard({ message = 'Algo salió mal.', onRetry, detail }) {
  return (
    <div className="data-panel fade-in">
      <div className="data-panel-body" style={{ textAlign: 'center', padding: 'var(--space-2xl)' }}>
        <div style={{ fontSize: '2.5rem', color: 'var(--color-peligro)', marginBottom: 'var(--space-md)' }}>
          <i className="fas fa-exclamation-triangle"></i>
        </div>
        <h3 style={{ fontSize: 'var(--text-lg)', fontWeight: 600, color: 'var(--color-texto)', marginBottom: 'var(--space-sm)' }}>
          Error al cargar
        </h3>
        <p style={{ color: 'var(--color-texto-claro)', maxWidth: 400, margin: '0 auto var(--space-lg)' }}>
          {message}
        </p>
        {detail && (
          <details style={{ marginBottom: 'var(--space-lg)', textAlign: 'left', maxWidth: 500, margin: '0 auto var(--space-lg)' }}>
            <summary style={{ cursor: 'pointer', color: 'var(--color-texto-muted)', fontSize: 'var(--text-sm)' }}>
              Detalles del error
            </summary>
            <pre style={{
              marginTop: 'var(--space-sm)',
              padding: 'var(--space-md)',
              background: 'var(--color-fondo)',
              borderRadius: 'var(--radio-borde)',
              fontSize: 'var(--text-xs)',
              color: 'var(--color-peligro)',
              overflow: 'auto',
              whiteSpace: 'pre-wrap'
            }}>
              {detail}
            </pre>
          </details>
        )}
        {onRetry && (
          <button className="btn-primario" onClick={onRetry}>
            <i className="fas fa-redo"></i> Reintentar
          </button>
        )}
      </div>
    </div>
  )
}
