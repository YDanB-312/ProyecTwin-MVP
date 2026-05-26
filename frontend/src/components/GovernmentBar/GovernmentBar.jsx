/*
Componente: GovernmentBar
Funcion: Muestra la barra de gobierno del SENA en la parte superior
Proposito: Identificacion institucional
*/

import './GovernmentBar.css'

export default function GovernmentBar() {
  return (
    <div className="barra-gobierno">
      <div className="contenedor-barra">
        <span>Portal del SENA - República de Colombia</span>
        <div className="accesibilidad">
          <button className="btn-accesibilidad" aria-label="Alto contraste" type="button">
            <i className="fas fa-adjust"></i>
          </button>
        </div>
      </div>
    </div>
  )
}
