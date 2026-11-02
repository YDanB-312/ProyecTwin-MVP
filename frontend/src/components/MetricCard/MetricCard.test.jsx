import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import MetricCard from './MetricCard'

describe('MetricCard', () => {
  it('conserva valor, etiqueta y tendencia', () => {
    window.matchMedia = vi.fn().mockReturnValue({ matches: true })

    render(
      <MemoryRouter>
        <MetricCard label="Propuestas" value="12" trend="+2" trendDir="up" to="/aprendiz/propuestas" />
      </MemoryRouter>
    )

    expect(screen.getByRole('link', { name: /Propuestas/ })).toHaveAttribute('href', '/aprendiz/propuestas')
    expect(screen.getByText('12')).toBeInTheDocument()
    expect(screen.getByText('+2')).toBeInTheDocument()
  })

  it('renderiza un contenedor si no recibe destino', () => {
    window.matchMedia = vi.fn().mockReturnValue({ matches: true })
    render(<MetricCard label="Fichas" value="3" />)
    expect(screen.queryByRole('link')).not.toBeInTheDocument()
    expect(screen.getByText('Fichas')).toBeInTheDocument()
  })
})
