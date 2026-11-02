import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import GradeBadge from './GradeBadge'
import { gradeForScore } from './grade'

vi.mock('../../contexts/MotorContext', () => ({
  useMotor: () => ({ umbralPct: 75 }),
}))

describe('GradeBadge', () => {
  it('clasifica A/B/C en los bordes del umbral', () => {
    expect(gradeForScore(75, 75)).toEqual({ grade: 'A', pct: 75 })
    expect(gradeForScore(74, 75)).toEqual({ grade: 'B', pct: 74 })
    expect(gradeForScore(38, 75)).toEqual({ grade: 'B', pct: 38 })
    expect(gradeForScore(37, 75)).toEqual({ grade: 'C', pct: 37 })
    expect(gradeForScore(0.8, 75)).toEqual({ grade: 'A', pct: 80 })
  })

  it('usa el umbral del motor y expone el porcentaje accesible', () => {
    render(<GradeBadge score={74} />)

    const badge = screen.getByRole('img', { name: 'Coincidencia media: 74%' })
    expect(badge).toHaveTextContent('B')
    expect(badge).toHaveTextContent('74%')
  })

  it('oculta el porcentaje visible cuando showPct es false', () => {
    render(<GradeBadge score={75} showPct={false} />)

    const badge = screen.getByRole('img', { name: 'Coincidencia alta: 75%' })
    expect(badge).toHaveTextContent('A')
    expect(badge).not.toHaveTextContent('75%')
  })
})
