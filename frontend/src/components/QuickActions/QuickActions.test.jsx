import { describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import QuickActions from './QuickActions'

describe('QuickActions', () => {
  it('distingue enlaces, acciones y elementos deshabilitados', () => {
    const onClick = vi.fn()
    const items = [
      { titulo: 'Nueva propuesta', descripcion: 'Crear', icon: <span />, to: '/aprendiz/propuestas?crear=1' },
      { titulo: 'Recargar', descripcion: 'Actualizar', icon: <span />, onClick },
      { titulo: 'No disponible', descripcion: 'Deshabilitado', icon: <span />, onClick, disabled: true },
    ]

    render(
      <MemoryRouter>
        <QuickActions items={items} />
      </MemoryRouter>
    )

    expect(screen.getByRole('link', { name: /Nueva propuesta/ })).toHaveAttribute(
      'href',
      '/aprendiz/propuestas?crear=1'
    )

    fireEvent.click(screen.getByRole('button', { name: /Recargar/ }))
    expect(onClick).toHaveBeenCalledTimes(1)

    const deshabilitado = screen.getByRole('button', { name: /No disponible/ })
    expect(deshabilitado).toBeDisabled()
    fireEvent.click(deshabilitado)
    expect(onClick).toHaveBeenCalledTimes(1)
  })

  it('no renderiza acciones cuando la lista está vacía', () => {
    const { container } = render(
      <MemoryRouter>
        <QuickActions items={[]} />
      </MemoryRouter>
    )
    expect(container.querySelectorAll('a, button')).toHaveLength(0)
  })
})
