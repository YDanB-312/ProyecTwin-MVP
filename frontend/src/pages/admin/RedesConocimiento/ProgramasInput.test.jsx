import { describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { ProgramasInput } from './RedesConocimiento'

describe('ProgramasInput', () => {
  it('reenvía id y atributos ARIA de FormField al control de alta', () => {
    render(
      <ProgramasInput
        programas={[]}
        setProgramas={vi.fn()}
        id="programas"
        aria-invalid="true"
        aria-describedby="programas-error"
        aria-required="true"
      />
    )

    const input = screen.getByLabelText('Nuevo programa')

    expect(input).toHaveAttribute('id', 'programas')
    expect(input).toHaveAttribute('aria-invalid', 'true')
    expect(input).toHaveAttribute('aria-describedby', 'programas-error')
    expect(input).toHaveAttribute('aria-required', 'true')
  })

  it('asocia el error local al control de alta', () => {
    render(<ProgramasInput programas={[]} setProgramas={vi.fn()} id="programas" />)

    fireEvent.click(screen.getByRole('button', { name: /Agregar/i }))

    const input = screen.getByLabelText('Nuevo programa')
    const error = screen.getByRole('alert')

    expect(error).toHaveTextContent('Escribe un programa.')
    expect(input).toHaveAttribute('aria-invalid', 'true')
    expect(input.getAttribute('aria-describedby')).toContain(error.id)
  })
})
