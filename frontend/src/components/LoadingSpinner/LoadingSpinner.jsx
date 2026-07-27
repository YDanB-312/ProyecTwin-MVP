import './LoadingSpinner.css'

export default function LoadingSpinner({ texto = 'Cargando...', size = 'normal' }) {
  return (
    <div className={`loading-spinner-container loading-spinner-${size}`}>
      <div className="loading-spinner-ring">
        <div className="loading-spinner-segment"></div>
        <div className="loading-spinner-segment"></div>
        <div className="loading-spinner-segment"></div>
        <div className="loading-spinner-segment"></div>
      </div>
      <p className="loading-spinner-texto">{texto}</p>
    </div>
  )
}
