import { Link } from 'react-router-dom'
import s from './QuickActions.module.css'

export default function QuickActions({ items, variant = 'grid' }) {
  return (
    <div className={variant === 'stack' ? s.gridStack : s.grid}>
      {items.map((a) => {
        const contenido = (
          <>
            <span className={s.icon} aria-hidden="true">{a.icon}</span>
            <span className={s.title}>{a.titulo}</span>
            <span className={s.desc}>{a.descripcion}</span>
          </>
        )

        if (a.onClick) {
          return (
            <button
              key={a.titulo}
              type="button"
              className={s.card}
              onClick={a.onClick}
              disabled={a.disabled}
            >
              {contenido}
            </button>
          )
        }

        return (
          <Link key={a.titulo} to={a.to} className={s.card}>
            {contenido}
          </Link>
        )
      })}
    </div>
  )
}
