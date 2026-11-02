import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import ScoreDial from './ScoreDial'

vi.mock('../../contexts/MotorContext', () => ({
  useMotor: () => ({ umbralPct: 30 }),
}))

describe('ScoreDial', () => {
  it('normaliza fracciones y anuncia el grado', () => {
    window.matchMedia = vi.fn().mockReturnValue({ matches: true })
    render(<ScoreDial value={0.64} />)
    expect(screen.getByRole('img', { name: 'Coincidencia: 64% (grado A)' })).toBeInTheDocument()
  })

  it('recorta valores fuera de rango y calcula el grado', () => {
    window.matchMedia = vi.fn().mockReturnValue({ matches: true })
    const { rerender } = render(<ScoreDial value={150} />)
    expect(screen.getByRole('img', { name: 'Coincidencia: 100% (grado A)' })).toBeInTheDocument()

    rerender(<ScoreDial value={20} />)
    expect(screen.getByRole('img', { name: 'Coincidencia: 20% (grado B)' })).toBeInTheDocument()
  })
})
