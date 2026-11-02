import { cloneElement, isValidElement, useId } from 'react'
import s from './FormField.module.css'

export default function FormField({ label, error, help, required, children, className = '' }) {
  const id = useId()
  const errorId = `${id}-error`
  const helpId = `${id}-help`
  const describedBy = [error ? errorId : null, !error && help ? helpId : null]
    .filter(Boolean)
    .join(' ') || undefined

  // FormField es el único dueño de los ids de ayuda/error: el control recibe
  // la asociación y el mensaje visible conserva el mismo id.
  const control = isValidElement(children)
    ? cloneElement(children, {
        id,
        'aria-invalid': error ? true : undefined,
        'aria-describedby': describedBy,
        'aria-required': required ? true : undefined,
      })
    : children

  return (
    <div className={`${s.field} ${error ? s.hasError : ''} ${className}`}>
      {label && (
        <label htmlFor={id} className={s.label}>
          {label}
          {required && <span className={s.required} aria-hidden="true"> *</span>}
        </label>
      )}
      <div className={s.control}>{control}</div>
      {error ? (
        <p id={errorId} className={s.error} role="alert">⚠ {error}</p>
      ) : help ? (
        <p id={helpId} className={s.help}>{help}</p>
      ) : null}
    </div>
  )
}
