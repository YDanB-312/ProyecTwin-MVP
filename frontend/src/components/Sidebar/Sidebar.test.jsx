import { describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Sidebar from './Sidebar'

describe('Sidebar', () => {
  it('marca activeFor, agrupa una vez y cierra al navegar o pulsar cerrar', () => {
    const onClose = vi.fn()
    const links = [
      { to: '/admin/usuarios', label: 'Usuarios', icon: <span />, group: 'Gestión', activeFor: ['/admin/detalle-usuario'] },
      { to: '/admin/fichas', label: 'Fichas', icon: <span />, group: 'Gestión' },
    ]

    render(
      <MemoryRouter initialEntries={['/admin/detalle-usuario/7']}>
        <Sidebar isOpen onClose={onClose} role="admin" links={links} />
      </MemoryRouter>
    )

    const usuarios = screen.getByRole('link', { name: /Usuarios/ })
    expect(usuarios.className).toMatch(/linkActive/)
    expect(screen.getAllByText('Gestión')).toHaveLength(1)

    fireEvent.click(usuarios)
    expect(onClose).toHaveBeenCalledTimes(1)

    fireEvent.click(screen.getByRole('button', { name: 'Cerrar menú' }))
    expect(onClose).toHaveBeenCalledTimes(2)
  })
})
