import { describe, expect, it } from 'vitest'
import {
  esSimilitudPendiente,
  puedeAprobarConSimilitud,
} from './similitudEstado'

describe('estado de similitud', () => {
  it('bloquea la aprobación mientras el cálculo está pendiente', () => {
    expect(esSimilitudPendiente('pendiente')).toBe(true)
    expect(esSimilitudPendiente('en_proceso')).toBe(true)
    expect(puedeAprobarConSimilitud({ similitud_estado: 'pendiente' })).toBe(false)
    expect(puedeAprobarConSimilitud({ similitud_estado: 'error' })).toBe(false)
  })

  it('permite aprobar con resultado vigente o sin referencias', () => {
    expect(puedeAprobarConSimilitud({ similitud_estado: 'actualizado' })).toBe(true)
    expect(puedeAprobarConSimilitud({ similitud_estado: 'sin_referencias' })).toBe(true)
    expect(puedeAprobarConSimilitud({})).toBe(true)
  })
})
