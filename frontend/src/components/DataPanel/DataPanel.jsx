export default function DataPanel({ title, icon, action, children, className }) {
  return (
    <section className={`data-panel${className ? ` ${className}` : ''}`}>
      {title && (
        <div className="data-panel-header">
          <h2 className="data-panel-titulo">
            {icon && <i className={`fas fa-${icon}`}></i>}
            {title}
          </h2>
          {action && <div className="data-panel-action">{action}</div>}
        </div>
      )}
      <div className="data-panel-body">
        {children}
      </div>
    </section>
  )
}
