export default function FormField({ label, htmlFor, required, error, helpText, fullWidth, children }) {
  return (
    <div className={`campo-grupo${fullWidth ? ' campo-completo' : ''}`}>
      <label htmlFor={htmlFor} className="campo-label">
        {label} {required && <span className="obligatorio">*</span>}
      </label>
      {children}
      {error && <span className="campo-error">{error}</span>}
      {helpText && <span className="campo-info">{helpText}</span>}
    </div>
  )
}
