import { Link } from 'react-router-dom'
import { CONTACTO } from '../../constants/contacto'
import { RUTA_SOPORTE_POR_ROL } from '../../constants/routes'
import { Phone, Envelope, Copyright, Lifebuoy } from 'phosphor-react'
import s from './Footer.module.css'

export default function Footer({ role }) {
  const soporte = role === 'aprendiz' || role === 'instructor'
    ? RUTA_SOPORTE_POR_ROL[role]
    : null

  return (
    <footer className={s.footer}>
      <div className={s.container}>
        <div className={s.left}>
          <p className={s.copy}>
            <Copyright size={13} weight="regular" />
            {CONTACTO.copyright}. Todos los derechos reservados.
          </p>
        </div>

        <div className={s.contact}>
          {soporte && (
            <>
              <Link to={soporte} className={s.support}>
                <Lifebuoy size={12} weight="regular" />
                Soporte
              </Link>
              <span className={s.sep} aria-hidden="true">|</span>
            </>
          )}
          <a href={`tel:${CONTACTO.telefono.replace(/\s/g, '')}`}>
            <Phone size={12} weight="regular" />
            {CONTACTO.telefono}
          </a>
          <span className={s.sep} aria-hidden="true">|</span>
          <a href={`mailto:${CONTACTO.email}`}>
            <Envelope size={12} weight="regular" />
            {CONTACTO.email}
          </a>
        </div>
      </div>
    </footer>
  )
}
