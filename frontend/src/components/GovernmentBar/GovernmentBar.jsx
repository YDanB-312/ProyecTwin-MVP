import { useState, useEffect } from 'react'
import './GovernmentBar.css'

export default function GovernmentBar() {
  const [altoContraste, setAltoContraste] = useState(false)

  useEffect(() => {
    document.body.classList.toggle('alto-contraste', altoContraste)
  }, [altoContraste])

  return (
    <div className="barra-gobierno">
      <div className="contenedor-barra">
        <span>Portal del SENA - República de Colombia</span>
        <div className="accesibilidad">
          <button className={`btn-accesibilidad${altoContraste ? ' activo' : ''}`} aria-label="Alto contraste" type="button" onClick={() => setAltoContraste(prev => !prev)}>
            <i className="fas fa-adjust"></i>
          </button>
        </div>
      </div>
    </div>
  )
}
