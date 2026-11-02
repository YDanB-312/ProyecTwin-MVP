import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import FormField from './FormField'

describe('FormField', () => {
  it('asocia label, ayuda y marca de requerido con el control', () => {
    render(
      <FormField label="Correo" help="Solo letras" required>
        <input type="email" />
      </FormField>
    )

    const input = screen.getByLabelText(/Correo/)
    const ayuda = screen.getByText('Solo letras')

    expect(input.tagName).toBe('INPUT')
    expect(input).toHaveAttribute('aria-required', 'true')
    expect(input).toHaveAttribute('aria-describedby', ayuda.id)
    expect(input).not.toHaveAttribute('aria-invalid')
    expect(screen.getByText('*')).toBeInTheDocument()
  })

  it('muestra el mensaje de error con rol alert y lo asocia al control', () => {
    render(
      <FormField label="Nombre" error="Campo requerido">
        <input />
      </FormField>
    )

    const input = screen.getByRole('textbox')
    const error = screen.getByRole('alert')

    expect(error).toHaveTextContent('Campo requerido')
    expect(input).toHaveAttribute('aria-invalid', 'true')
    expect(input).toHaveAttribute('aria-describedby', error.id)
  })
})
