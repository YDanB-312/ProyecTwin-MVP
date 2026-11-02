import { expect } from '@playwright/test'

export const MAILPIT_URL = process.env.MAILPIT_URL || 'http://127.0.0.1:8025'

export async function limpiarMailpit(request) {
  const respuesta = await request.delete(`${MAILPIT_URL}/api/v1/messages`)
  expect(respuesta.ok()).toBeTruthy()
}

function enlaces(texto) {
  return [...String(texto || '').matchAll(/https?:\/\/[^\s<>"']+/g)]
    .map((m) => m[0].replaceAll('&amp;', '&'))
}

export async function esperarEnlace(request, { destinatario, asunto, ruta, timeout = 15000 }) {
  const fin = Date.now() + timeout

  while (Date.now() < fin) {
    const listado = await request.get(`${MAILPIT_URL}/api/v1/messages?limit=50`)
    expect(listado.ok()).toBeTruthy()
    const cuerpo = await listado.json()
    const mensajes = cuerpo.messages || []

    const mensaje = mensajes.find((m) => {
      const destinatarios = (m.To || []).map((t) => t.Address).join(' ')
      return destinatarios.includes(destinatario) && String(m.Subject || '').includes(asunto)
    })

    if (mensaje) {
      const detalle = await request.get(`${MAILPIT_URL}/api/v1/message/${mensaje.ID}`)
      expect(detalle.ok()).toBeTruthy()
      const correo = await detalle.json()
      const encontrado = [...enlaces(correo.Text), ...enlaces(correo.HTML)].find((url) => url.includes(ruta))

      if (encontrado) return encontrado
    }

    await new Promise((resolve) => setTimeout(resolve, 250))
  }

  throw new Error(`No llegó a Mailpit el correo "${asunto}" para ${destinatario}. ¿Está Mailpit iniciado en ${MAILPIT_URL}?`)
}
