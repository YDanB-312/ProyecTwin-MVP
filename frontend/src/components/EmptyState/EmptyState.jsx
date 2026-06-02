export default function EmptyState({ icon, title, description, action }) {
  return (
    <div className="estado-vacio-moderno">
      <div className="estado-vacio-icono"><i className={`fas fa-${icon}`}></i></div>
      <h3 className="estado-vacio-titulo">{title}</h3>
      {description && <p className="estado-vacio-descripcion">{description}</p>}
      {action && <div className="estado-vacio-accion">{action}</div>}
    </div>
  )
}
