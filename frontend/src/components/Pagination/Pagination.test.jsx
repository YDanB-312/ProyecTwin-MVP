import { describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import Pagination from './Pagination'

describe('Pagination', () => {
  it('corrige página y rango cuando el filtro reduce la lista', () => {
    render(
      <Pagination
        totalItems={20}
        itemsPerPage={10}
        filteredCount={8}
        paginaActual={2}
        setPaginaActual={vi.fn()}
      />
    )

    expect(screen.getByText(/1–8/)).toBeInTheDocument()
    expect(screen.getByText('8', { selector: 'strong' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '1' })).toHaveAttribute('aria-current', 'page')
    expect(screen.queryByRole('button', { name: '2' })).not.toBeInTheDocument()
  })

  it('sincroniza la página del padre cuando el total encoge', () => {
    const setPagina = vi.fn()

    render(
      <Pagination
        totalItems={20}
        itemsPerPage={10}
        filteredCount={8}
        paginaActual={2}
        setPaginaActual={setPagina}
      />
    )

    expect(setPagina).toHaveBeenCalledWith(1)
  })

  it('navega a la página siguiente y deshabilita en el extremo', () => {
    const setPagina = vi.fn()
    const { rerender } = render(
      <Pagination totalItems={30} itemsPerPage={10} paginaActual={1} setPaginaActual={setPagina} />
    )

    fireEvent.click(screen.getByRole('button', { name: 'Página siguiente' }))
    expect(setPagina).toHaveBeenCalledWith(2)
    expect(screen.getByRole('button', { name: 'Página anterior' })).toBeDisabled()

    rerender(
      <Pagination totalItems={30} itemsPerPage={10} paginaActual={3} setPaginaActual={setPagina} />
    )
    expect(screen.getByRole('button', { name: 'Página siguiente' })).toBeDisabled()
  })

  it('no renderiza nada sin elementos', () => {
    const { container } = render(
      <Pagination totalItems={0} paginaActual={1} setPaginaActual={vi.fn()} />
    )
    expect(container).toBeEmptyDOMElement()
  })
})
