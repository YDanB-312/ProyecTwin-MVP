import { describe, it, expect } from 'vitest'
import { iniciales, formatearFecha, agruparObservaciones, payloadCuenta, payloadPerfil } from './helpers'

describe('iniciales', () => {
  it('calcula iniciales para nombres completos, únicos, con espacios y vacíos', () => {
    expect(iniciales('María González')).toBe('MG')
    expect(iniciales('Carlos')).toBe('C')
    expect(iniciales('  juan   perez  ')).toBe('JP')
    expect(iniciales('')).toBe('')
    expect(iniciales(undefined)).toBe('')
  })
})

describe('formatearFecha', () => {
  it('formatea fechas válidas y conserva entradas vacías', () => {
    expect(formatearFecha('10/11/2026')).toBe('10 nov 2026')
    expect(formatearFecha('')).toBe('')
    expect(formatearFecha(undefined)).toBe('')
  })
})

describe('agruparObservaciones', () => {
  it('agrupa respuestas bajo su raíz y conserva el orden de las respuestas', () => {
    const simple = [
      { id: 1, texto: 'raiz', respuestaA: null },
      { id: 2, texto: 'respuesta', respuestaA: 1 },
    ]
    const agrupado = agruparObservaciones(simple)
    expect(agrupado).toHaveLength(1)
    expect(agrupado[0].id).toBe(1)
    expect(agrupado[0].respuestas.map((r) => r.id)).toEqual([2])

    const conVariasRespuestas = [
      { id: 3, texto: 'b', respuestaA: 2 },
      { id: 2, texto: 'a', respuestaA: null },
      { id: 4, texto: 'c', respuestaA: 2 },
    ]
    const resultado = agruparObservaciones(conVariasRespuestas)
    expect(resultado).toHaveLength(1)
    expect(resultado[0].respuestas.map((r) => r.id)).toEqual([3, 4])
  })
})

describe('payloads', () => {
  const perfil = {
    nombre: 'Ana',
    apellido: 'Pérez',
    correo: 'ana@test.local',
    rol: 'aprendiz',
    estado: true,
    tipo_documento: 'CC',
    numero_documento: '12345678',
    username: 'ana123',
    foto_url: 'data:image/jpeg;base64,abc',
  }

  const cuenta = {
    nombre: 'Ana',
    apellido: 'Pérez',
    correo: 'ana@test.local',
    rol: 'aprendiz',
    estado: true,
  }

  it('limita el perfil a nombre/apellido y conserva el contrato administrativo', () => {
    expect(payloadPerfil(perfil)).toEqual({
      nombre: 'Ana',
      apellido: 'Pérez',
    })

    const resultado = payloadPerfil(perfil)
    expect(Object.keys(resultado)).toEqual(['nombre', 'apellido'])
    expect(resultado).not.toHaveProperty('correo')
    expect(resultado).not.toHaveProperty('rol')
    expect(resultado).not.toHaveProperty('estado')
    expect(resultado).not.toHaveProperty('tipo_documento')
    expect(resultado).not.toHaveProperty('numero_documento')
    expect(resultado).not.toHaveProperty('username')
    expect(resultado).not.toHaveProperty('foto_url')

    expect(payloadCuenta(cuenta)).toEqual(cuenta)
    expect(payloadCuenta(cuenta, { estado: false, tipo_documento: 'CC' })).toEqual({
      ...cuenta,
      estado: false,
      tipo_documento: 'CC',
    })
  })
})
