export default function MetricCard({ icon, value, label, trend, trendUp, color }) {
  const colorClass = color ? `metric-card-${color}` : ''
  return (
    <div className={`metric-card ${colorClass}`}>
      <div className="metric-card-icono"><i className={`fas fa-${icon}`}></i></div>
      <div className="metric-card-contenido">
        <span className="metric-card-valor">{value}</span>
        <span className="metric-card-label">{label}</span>
      </div>
      {trend !== undefined && (
        <span className={`metric-card-trend ${trendUp ? 'trend-up' : 'trend-down'}`}>
          <i className={`fas fa-${trendUp ? 'arrow-up' : 'arrow-down'}`}></i> {trend}%
        </span>
      )}
    </div>
  )
}
