import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import FichasAdmin from './FichasAdmin'

const { aprendices } = vi.hoisted(() => ({
  aprendices: Array.from({ length: 8 }, (_, i) => ({
    id: i + 1,
    nombre: `Aprendiz ${i + 1}`,
    apellido: 'Prueba',
    numero_documento: String(1000 + i),
  })),
}))

vi.mock('../../../layouts/DashboardLayout/DashboardLayout', () => ({
  default: ({ children }) => <div>{children}</div>,
}))

vi.mock('../../../components/PageHeader/PageHeader', () => ({
  default: ({ title }) => <h1>{title}</h1>,
}))

vi.mock('../../../components/DataPanel/DataPanel', () => ({
  default: ({ title, children }) => (
    <section>
      <h2>{title}</h2>
      {children}
    </section>
  ),
}))

vi.mock('../../../components/ConfirmModal/ConfirmModal', () => ({
  default: () => null,
}))

vi.mock('../../../lib/useApi', () => ({
  useApi: (_fn, _deps, opciones) =>
    opciones?.inicial === null
      ? {
          data: {
            listaProgramas: [],
            listaRedes: [],
            listaInstructores: [],
            listaProyectos: [],
            listaAprendices: aprendices,
          },
          cargando: false,
          error: null,
          recargar: vi.fn(),
        }
      : { data: [], cargando: false, error: null, recargar: vi.fn() },
}))

describe('FichasAdmin', () => {
  it('agrupa los aprendices en un fieldset y nombra el buscador', () => {
    render(
      <MemoryRouter initialEntries={['/admin/fichas?crear=1']}>
        <FichasAdmin />
      </MemoryRouter>
    )

    const grupo = screen.getByRole('group', { name: /Aprendices de la ficha/ })
    const buscador = screen.getByRole('textbox', { name: 'Buscar aprendices' })

    expect(grupo.tagName).toBe('FIELDSET')
    expect(buscador).toHaveAccessibleName('Buscar aprendices')
    expect(buscador).toHaveAccessibleDescription(
      /Marca los aprendices que pertenecen a esta ficha/
    )
    expect(screen.getAllByRole('checkbox')).toHaveLength(8)
  })
})
