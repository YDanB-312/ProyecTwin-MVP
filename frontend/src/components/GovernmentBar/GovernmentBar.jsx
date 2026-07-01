import { useState, useEffect } from 'react'
import './GovernmentBar.css'

function getInitialTheme() {
  const stored = localStorage.getItem('theme')
  if (stored === 'dark' || stored === 'light') return stored
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export default function GovernmentBar() {
  const [theme, setTheme] = useState(getInitialTheme)

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }, [theme])

  const toggleTheme = () => setTheme(prev => prev === 'dark' ? 'light' : 'dark')

  return (
    <div className="barra-gobierno">
      <div className="contenedor-barra">
        <span>Portal del SENA - República de Colombia</span>
        <div className="accesibilidad">
          <button className="btn-accesibilidad" aria-label="Cambiar tema" type="button" onClick={toggleTheme}>
            <i className={`fas fa-${theme === 'dark' ? 'sun' : 'moon'}`}></i>
          </button>
        </div>
      </div>
    </div>
  )
}
