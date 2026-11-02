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

    expect(screen.getByLabelText(/Correo/).tagName).toBe('INPUT')
    expect(screen.getByText('Solo letras')).toBeInTheDocument()
    expect(screen.getByText('*')).toBeInTheDocument()
  })

  it('muestra el mensaje de error con rol alert', () => {
    render(
      <FormField label="Nombre" error="Campo requerido">
        <input />
      </FormField>
    )

    expect(screen.getByRole('alert')).toHaveTextContent('Campo requerido')
  })
})
