import './Skeleton.css'

export function SkeletonLine({ width, height = '1rem', className = '' }) {
  return <div className={`skeleton-line ${className}`} style={{ width, height }} />
}

export function SkeletonCircle({ size = 40, className = '' }) {
  return <div className={`skeleton-circle ${className}`} style={{ width: size, height: size }} />
}

export function SkeletonCard({ lines = 3, hasAvatar = false, hasImage = false, className = '' }) {
  return (
    <div className={`skeleton-card ${className}`}>
      <div className="skeleton-card-header">
        {hasAvatar && <SkeletonCircle size={40} />}
        <div className="skeleton-card-lines">
          <SkeletonLine width="60%" height="0.9rem" />
          <SkeletonLine width="40%" height="0.75rem" />
        </div>
      </div>
      {hasImage && <SkeletonLine width="100%" height="120px" className="skeleton-mt" />}
      <div className="skeleton-card-body">
        {Array.from({ length: lines }, (_, i) => (
          <SkeletonLine key={i} width={i === lines - 1 ? '70%' : '100%'} height="0.8rem" className="skeleton-mt" />
        ))}
      </div>
    </div>
  )
}

export function SkeletonTable({ rows = 5, cols = 4 }) {
  return (
    <div className="skeleton-table">
      <div className="skeleton-table-header">
        {Array.from({ length: cols }, (_, i) => (
          <SkeletonLine key={i} width={`${100 / cols}%`} height="0.8rem" />
        ))}
      </div>
      {Array.from({ length: rows }, (_, r) => (
        <div className="skeleton-table-row" key={r}>
          {Array.from({ length: cols }, (_, c) => (
            <SkeletonLine key={c} width={c === 0 ? '50%' : `${60 + Math.random() * 30}%`} height="0.8rem" />
          ))}
        </div>
      ))}
    </div>
  )
}

export function SkeletonStats({ count = 4 }) {
  return (
    <div className="skeleton-stats">
      {Array.from({ length: count }, (_, i) => (
        <div className="skeleton-stat-card" key={i}>
          <SkeletonCircle size={36} />
          <SkeletonLine width="50%" height="1.5rem" className="skeleton-mt" />
          <SkeletonLine width="70%" height="0.7rem" className="skeleton-mt" />
        </div>
      ))}
    </div>
  )
}
