import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import DashboardMetrics from './DashboardMetrics'
import { useApi } from '../../lib/useApi'

vi.mock('../../lib/useApi', () => ({
  useApi: vi.fn(),
}))

function metricasBase(overrides = {}) {
  return {
    propuestas: {
      total: 4,
      borrador: 1,
      pendiente: 2,
      aprobado: 1,
      rechazado: 0,
    },
    evolucion: [
      { clave: '2026-08', label: 'ago', fecha: 'agosto de 2026', total: 2, propuestas: [] },
    ],
    actividad: [],
    ...overrides,
  }
}

describe('DashboardMetrics', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('muestra las propuestas por ficha y la evolución de revisiones del instructor', () => {
    useApi.mockReturnValue({
      data: metricasBase({
        fichas: {
          total: 1,
          detalle: [
            {
              id: 1,
              codigo: 'FICHA-01',
              nombre: 'Ficha uno',
              aprendices: 3,
              propuestas: 4,
              pendientes: 2,
            },
          ],
        },
        aprendices: { total: 3 },
      }),
    })

    render(<DashboardMetrics rol="instructor" />)

    expect(screen.getByText('Propuestas por ficha')).toBeInTheDocument()
    expect(screen.getByLabelText(/Propuestas por ficha/)).toBeInTheDocument()
    expect(screen.getByText('Revisiones por mes')).toBeInTheDocument()
    expect(screen.getByLabelText(/Revisiones por mes/)).toBeInTheDocument()
  })

  it('muestra las acciones rápidas y la similitud del aprendiz', () => {
    useApi.mockReturnValue({
      data: metricasBase({
        similitudesPropuestas: [
          { id: 10, titulo: 'Propuesta uno', max: 82, count: 2 },
        ],
      }),
    })

    render(
      <MemoryRouter>
        <DashboardMetrics
          rol="aprendiz"
          acciones={[
            {
              to: '/aprendiz/propuestas?crear=1',
              icon: <span aria-hidden="true" />,
              titulo: 'Nueva propuesta',
              descripcion: 'Crea una propuesta.',
            },
          ]}
        />
      </MemoryRouter>
    )

    expect(screen.getByText('Estado de mis propuestas')).toBeInTheDocument()
    expect(screen.getByText('Similitud máxima por propuesta')).toBeInTheDocument()
    expect(screen.getByLabelText(/Porcentaje máximo de similitud por propuesta/)).toBeInTheDocument()
    expect(screen.getByText('Acciones rápidas')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Nueva propuesta/ })).toBeInTheDocument()
    expect(screen.queryByText('Observaciones por propuesta')).not.toBeInTheDocument()
    expect(screen.queryByText('Observaciones por mes')).not.toBeInTheDocument()
    expect(screen.queryByText('Propuestas por mes')).not.toBeInTheDocument()
  })

  it('mantiene la serie temporal para el admin', () => {
    useApi.mockReturnValue({
      data: metricasBase({
        usuarios: { total: 4, aprendices: 2, instructores: 1, admins: 1 },
        fichas: { total: 1 },
        aprendices: { total: 2 },
      }),
    })

    render(<DashboardMetrics rol="admin" />)

    expect(screen.getByText('Propuestas por mes')).toBeInTheDocument()
    expect(screen.getByLabelText(/Propuestas por mes/)).toBeInTheDocument()
  })
})
