export default function PageHeader({ title, subtitle, icon, actions, breadcrumb }) {
  return (
    <div className="encabezado-pagina-moderno">
      {breadcrumb && (
        <div className="breadcrumb-moderno">
          {breadcrumb.map((item, i) => (
            <span key={i} className="breadcrumb-item">
              {i > 0 && <span className="breadcrumb-sep"><i className="fas fa-chevron-right"></i></span>}
              {item.to ? (
                <a href={item.to}>{item.icon && <i className={`fas fa-${item.icon}`}></i>} {item.label}</a>
              ) : (
                <span className="breadcrumb-actual">{item.label}</span>
              )}
            </span>
          ))}
        </div>
      )}
      <div className="encabezado-row">
        <div className="encabezado-info">
          {icon && <div className="encabezado-icono"><i className={`fas fa-${icon}`}></i></div>}
          <div>
            <h1 className="encabezado-titulo">{title}</h1>
            {subtitle && <p className="encabezado-subtitulo">{subtitle}</p>}
          </div>
        </div>
        {actions && <div className="encabezado-acciones">{actions}</div>}
      </div>
    </div>
  )
}
