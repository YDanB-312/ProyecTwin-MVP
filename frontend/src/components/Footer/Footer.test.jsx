import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Footer from './Footer'

describe('Footer', () => {
  it('muestra soporte para aprendiz e instructor', () => {
    const { rerender } = render(
      <MemoryRouter>
        <Footer role="aprendiz" />
      </MemoryRouter>
    )
    expect(screen.getByRole('link', { name: /Soporte/i })).toHaveAttribute('href', '/aprendiz/reportar-falla')

    rerender(
      <MemoryRouter>
        <Footer role="instructor" />
      </MemoryRouter>
    )
    expect(screen.getByRole('link', { name: /Soporte/i })).toHaveAttribute('href', '/instructor/reportar-falla')
  })

  it('no muestra soporte para admin y conserva teléfono/correo', () => {
    render(
      <MemoryRouter>
        <Footer role="admin" />
      </MemoryRouter>
    )

    expect(screen.queryByRole('link', { name: /Soporte/i })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: /323 542 1165/ })).toHaveAttribute('href', 'tel:3235421165')
    expect(screen.getByRole('link', { name: /sena@correo.edu.co/ })).toHaveAttribute('href', 'mailto:sena@correo.edu.co')
  })
})
