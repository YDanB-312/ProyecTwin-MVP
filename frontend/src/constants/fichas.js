const CARACTERES = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'

export function generarCodigoFicha() {
  let codigo = 'FT-'
  for (let i = 0; i < 6; i++) {
    codigo += CARACTERES.charAt(Math.floor(Math.random() * CARACTERES.length))
  }
  return codigo
}

export const FICHAS = [
  { codigo: 'FT-X7K2MN', nombre: 'Analisis y Desarrollo 2568', programa: 'ADSO', id_programa: 1 },
  { codigo: 'FT-P4R8TL', nombre: 'Analisis y Desarrollo 2634', programa: 'ADSO', id_programa: 1 },
  { codigo: 'FT-W2J5HQ', nombre: 'Produccion Multimedia 3102', programa: 'Produccion Multimedia', id_programa: 2 },
  { codigo: 'FT-B9N3VK', nombre: 'Infraestructura Redes 2801', programa: 'Infraestructura Redes', id_programa: 3 },
]

export function buscarFichaPorCodigo(codigo) {
  return FICHAS.find(f => f.codigo.toLowerCase() === codigo.trim().toLowerCase()) || null
}

export function obtenerFichaAprendiz() {
  const guardada = sessionStorage.getItem('ficha_aprendiz')
  return guardada ? JSON.parse(guardada) : null
}

export function guardarFichaAprendiz(ficha) {
  sessionStorage.setItem('ficha_aprendiz', JSON.stringify(ficha))
}
