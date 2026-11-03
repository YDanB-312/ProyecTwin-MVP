import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  apiDescargar,
  apiFetch,
  apiLogin,
  EVENTO_SESION_EXPIRADA,
  invalidarCache,
} from './api'

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

  it('apiLogin envía siempre el correo normalizado', async () => {
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
    expect(cuerpo).not.toHaveProperty('username')
  })
})

describe('caché de apiFetch', () => {
  afterEach(() => {
    invalidarCache()
    vi.restoreAllMocks()
  })

  it('acota las URLs cacheadas para no crecer sin límite', async () => {
    invalidarCache()
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockImplementation(async (url) => ({
      ok: true,
      status: 200,
      text: async () => JSON.stringify({ url: String(url) }),
    }))

    for (let i = 0; i < 55; i += 1) {
      await apiFetch(`/cache-${i}`)
    }

    const antes = fetchMock.mock.calls.length
    await apiFetch('/cache-0')

    expect(fetchMock.mock.calls.length).toBe(antes + 1)
  })
})
