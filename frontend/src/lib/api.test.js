import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiDescargar, apiLogin, EVENTO_SESION_EXPIRADA } from './api'

// La descarga de PDF debe manejar la sesión expirada igual que apiFetch.
describe('apiDescargar', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('avisa sesión expirada cuando la descarga recibe 401', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (url) => {
      if (String(url).includes('sanctum/csrf-cookie')) {
        return { ok: true, status: 204 }
      }
      return {
        ok: false,
        status: 401,
        text: async () => JSON.stringify({ message: 'Unauthenticated.' }),
      }
    })

    const alExpirar = vi.fn()
    window.addEventListener(EVENTO_SESION_EXPIRADA, alExpirar)

    await expect(apiDescargar('/general-users/1/credenciales')).rejects.toMatchObject({ status: 401 })
    expect(alExpirar).toHaveBeenCalledTimes(1)

    window.removeEventListener(EVENTO_SESION_EXPIRADA, alExpirar)
  })

  it('apiLogin envía correo cuando el identificador tiene @', async () => {
    let cuerpo = null
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (url, opciones) => {
      if (String(url).includes('sanctum/csrf-cookie')) {
        return { ok: true, status: 204, text: async () => '' }
      }
      cuerpo = JSON.parse(opciones.body)
      return { ok: true, status: 200, text: async () => JSON.stringify({ user: { id: 1 }, token: null }) }
    })

    await apiLogin('  Maria.Gonzalez@Example.com ', 'Frase segura 2026', true)
    expect(cuerpo).toEqual({
      correo: 'maria.gonzalez@example.com',
      password: 'Frase segura 2026',
      recordarme: true,
    })
  })

  it('apiLogin conserva el username legacy cuando no hay @', async () => {
    let cuerpo = null
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (url, opciones) => {
      if (String(url).includes('sanctum/csrf-cookie')) {
        return { ok: true, status: 204, text: async () => '' }
      }
      cuerpo = JSON.parse(opciones.body)
      return { ok: true, status: 200, text: async () => JSON.stringify({ user: { id: 1 }, token: null }) }
    })

    await apiLogin('MGonzalez', 'ClaveTest#2026', false)
    expect(cuerpo).toEqual({
      username: 'mgonzalez',
      password: 'ClaveTest#2026',
      recordarme: false,
    })
  })
})
