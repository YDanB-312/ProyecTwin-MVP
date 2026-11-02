import { describe, expect, it } from 'vitest'
import { esConflictoVersion, esErrorEnlaceInvalido, mensajeErrorEnlace } from './errores'

describe('errores de enlace', () => {
  it('solo clasifica como enlace inválido los estados esperados', () => {
    for (const status of [404, 410, 422]) {
      expect(esErrorEnlaceInvalido({ status })).toBe(true)
    }

    for (const status of [0, 400, 401, 409, 419, 429, 500]) {
      expect(esErrorEnlaceInvalido({ status })).toBe(false)
    }
  })

  it('distingue red, CSRF vencido, rate limit y error de servidor', () => {
    expect(mensajeErrorEnlace(new Error('Failed to fetch'))).toContain('No se pudo conectar')
    expect(mensajeErrorEnlace({ status: 419 })).toContain('sesión de seguridad')
    expect(mensajeErrorEnlace({ status: 429 })).toContain('Demasiados intentos')
    expect(mensajeErrorEnlace({ status: 503 })).toContain('servidor no pudo')
  })

  it('reutiliza el mensaje del backend para otros 4xx', () => {
    expect(mensajeErrorEnlace({ status: 400, data: { message: 'Solicitud inválida.' } }))
      .toBe('Solicitud inválida.')
  })

  it('reconoce el conflicto de versión de propuestas', () => {
    expect(esConflictoVersion({ status: 409, data: { code: 'payload_obsoleto' } })).toBe(true)
    expect(esConflictoVersion({ status: 409, data: { code: 'otro' } })).toBe(false)
    expect(esConflictoVersion({ status: 500, data: { code: 'payload_obsoleto' } })).toBe(false)
  })
})
