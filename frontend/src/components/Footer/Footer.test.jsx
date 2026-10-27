import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Footer from './Footer'

describe('Footer', () => {
  it('no incluye navegación duplicada', () => {
    const { container } = render(<Footer />)
    expect(container.querySelector('nav')).toBeNull()
  })

  it('conserva copyright y datos de contacto', () => {
    const { container } = render(<Footer />)
    expect(container.textContent).toContain('Todos los derechos reservados')
    expect(screen.getByRole('link', { name: /323/ })).toHaveAttribute('href', 'tel:3235421165')
    expect(screen.getByRole('link', { name: /sena@correo/i })).toHaveAttribute('href', 'mailto:sena@correo.edu.co')
  })

  it('muestra soporte para aprendiz e instructor', () => {
    const { rerender } = render(
      <MemoryRouter>
        <Footer role="aprendiz" />
      </MemoryRouter>
    )
    expect(screen.getByRole('link', { name: /Soporte/ })).toHaveAttribute('href', '/aprendiz/reportar-falla')

    rerender(
      <MemoryRouter>
        <Footer role="instructor" />
      </MemoryRouter>
    )
    expect(screen.getByRole('link', { name: /Soporte/ })).toHaveAttribute('href', '/instructor/reportar-falla')
  })

  it('no muestra soporte para admin', () => {
    render(
      <MemoryRouter>
        <Footer role="admin" />
      </MemoryRouter>
    )
    expect(screen.queryByRole('link', { name: /Soporte/ })).not.toBeInTheDocument()
  })
})
