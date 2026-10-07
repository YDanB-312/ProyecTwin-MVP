import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiDescargar, EVENTO_SESION_EXPIRADA } from './api'

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
})
