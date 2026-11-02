import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import GovernmentBar from './GovernmentBar'

vi.mock('../../contexts/useTheme', () => ({
  useTheme: () => ({ theme: 'light', alternarTema: vi.fn() }),
}))

vi.mock('../../lib/useApi', () => ({
  useApi: () => ({ data: { umbral: 0.3, meses: 12 }, error: null }),
}))

describe('GovernmentBar', () => {
  it('expone el estado del motor con texto accesible y sin aria-label inválido', () => {
    render(<GovernmentBar />)

    const descripcion = screen.getByText(
      'Motor de similitud: umbral 30 por ciento, corpus de 12 meses'
    )
    const parrafo = descripcion.closest('p')

    expect(descripcion).toHaveClass('sr-only')
    expect(parrafo).not.toHaveAttribute('aria-label')
    expect(screen.getByText('MOTOR · UMBRAL 30% · CORPUS 12M')).toHaveAttribute(
      'aria-hidden',
      'true'
    )
  })
})
