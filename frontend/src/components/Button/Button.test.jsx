import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Button from './Button'

describe('Button', () => {
  it('renderiza un botón con type, disabled y children', () => {
    render(<Button type="submit" disabled>Guardar</Button>)

    const boton = screen.getByRole('button', { name: 'Guardar' })
    expect(boton.tagName).toBe('BUTTON')
    expect(boton).toHaveAttribute('type', 'submit')
    expect(boton).toBeDisabled()
  })

  it('renderiza un enlace interno cuando as="link"', () => {
    render(
      <MemoryRouter>
        <Button as="link" to="/login">Entrar</Button>
      </MemoryRouter>
    )

    const enlace = screen.getByRole('link', { name: 'Entrar' })
    expect(enlace).toHaveAttribute('href', '/login')
  })
})
