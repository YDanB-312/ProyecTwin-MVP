// Enlaces de correo con el token en el fragmento (#token=...).
//
// El fragmento no se envía al servidor: no aparece en logs de Nginx, proxies
// ni en el Referer. Se acepta también ?token= como compatibilidad con enlaces
// antiguos; la URL se limpia en cuanto se lee para no dejarla en el historial.

function parametros(valor) {
  return new URLSearchParams(String(valor || '').replace(/^#/, ''))
}

export function leerParametrosDeEnlace(location) {
  const hash = parametros(location?.hash)
  const search = new URLSearchParams(location?.search || '')
  const leer = (clave) => hash.get(clave) || search.get(clave) || ''

  return { token: leer('token'), correo: leer('correo') }
}

// Misma ruta sin token/correo, para reemplazar la entrada del historial.
export function urlSinToken(location) {
  const search = new URLSearchParams(location?.search || '')
  search.delete('token')
  search.delete('correo')
  const query = search.toString()

  return {
    pathname: location?.pathname || '/',
    search: query ? `?${query}` : '',
    hash: '',
  }
}
