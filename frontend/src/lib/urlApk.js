// Valida la URL de descarga que anuncia el backend antes de mostrarla.
// Solo se aceptan rutas same-origin (sin `//`, `..`, espacios ni backslashes)
// o URLs HTTPS absolutas sin credenciales embebidas.
const PROHIBIDO = /[\s\\]/

function tieneControl(valor) {
  for (let i = 0; i < valor.length; i += 1) {
    const codigo = valor.charCodeAt(i)
    if (codigo < 32 || codigo === 127) return true
  }
  return false
}

export function urlApkSegura(valor) {
  if (typeof valor !== 'string' || !valor) return null
  if (PROHIBIDO.test(valor) || tieneControl(valor)) return null

  if (valor.startsWith('/')) {
    if (valor.startsWith('//')) return null
    if (valor.split('/').includes('..')) return null
    return valor
  }

  if (!/^https:\/\//i.test(valor)) return null

  const autoridad = valor.match(/^https:\/\/([^/?#]*)/i)?.[1] ?? ''
  if (!autoridad || autoridad.includes('@')) return null

  try {
    const url = new URL(valor)
    if (url.protocol !== 'https:' || !url.hostname) return null
    if (url.username || url.password) return null
    return url.href
  } catch {
    return null
  }
}
