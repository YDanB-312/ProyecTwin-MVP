import { describe, expect, it } from 'vitest'
import { EMAIL_REGEX, MIN_PASSWORD_LENGTH, esEmailValido, esPasswordValida } from './validation'

describe('validation', () => {
  it('valida correo y contraseña con los límites compartidos', () => {
    expect(EMAIL_REGEX).toEqual(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)
    expect(MIN_PASSWORD_LENGTH).toBe(12)

    expect(esEmailValido('a@b.co')).toBe(true)
    expect(esEmailValido('  a@b.co  ')).toBe(true)
    expect(esEmailValido('no-es-correo')).toBe(false)
    expect(esEmailValido('')).toBe(false)
    expect(esEmailValido(null)).toBe(false)

    expect(esPasswordValida('123456789012')).toBe(true)
    expect(esPasswordValida('12345678901')).toBe(false)
    expect(esPasswordValida('12345')).toBe(false)
    expect(esPasswordValida('')).toBe(false)
    expect(esPasswordValida(null)).toBe(false)
  })
})
