import { describe, it, expect } from 'vitest'
import { RUTA_POR_ROL, RUTA_ALERTAS_POR_ROL, RUTA_SOPORTE_POR_ROL } from './routes'

describe('rutas por rol', () => {
  it('fija dashboard, alertas y soporte para cada rol', () => {
    expect(RUTA_POR_ROL).toEqual({
      aprendiz: '/aprendiz/dashboard',
      instructor: '/instructor/dashboard',
      admin: '/admin/dashboard',
    })

    expect(RUTA_ALERTAS_POR_ROL).toEqual({
      aprendiz: '/aprendiz/alertas',
      instructor: '/instructor/alertas',
      admin: '/admin/notificaciones',
    })

    expect(RUTA_SOPORTE_POR_ROL).toEqual({
      aprendiz: '/aprendiz/reportar-falla',
      instructor: '/instructor/reportar-falla',
      admin: '/admin/reportes-fallas',
    })
  })
})
