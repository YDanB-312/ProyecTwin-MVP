// Clasificación compartida de errores HTTP para no confundir un fallo de red,
// un límite de peticiones o un CSRF vencido con un enlace inválido.

export function esErrorEnlaceInvalido(err) {
  const status = Number(err?.status)
  return status === 404 || status === 410 || status === 422
}

export function esConflictoVersion(err) {
  return Number(err?.status) === 409 && err?.data?.code === 'payload_obsoleto'
}

export function mensajeErrorEnlace(err) {
  const status = Number(err?.status)

  if (!status || Number.isNaN(status)) {
    return 'No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.'
  }

  if (status === 419) {
    return 'La sesión de seguridad expiró. Recarga la página e inténtalo de nuevo.'
  }

  if (status === 429) {
    return 'Demasiados intentos. Espera unos minutos y vuelve a intentarlo.'
  }

  if (status >= 500) {
    return 'El servidor no pudo completar la solicitud. Inténtalo de nuevo en unos minutos.'
  }

  return err?.data?.message || 'No se pudo completar la solicitud. Inténtalo de nuevo.'
}
