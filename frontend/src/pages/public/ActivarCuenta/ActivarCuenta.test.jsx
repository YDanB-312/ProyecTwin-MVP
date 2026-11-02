import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import ActivarCuenta from './ActivarCuenta'

const mocks = vi.hoisted(() => ({
  validar: vi.fn(),
  activar: vi.fn(),
}))

vi.mock('../../../lib/api', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    apiValidarActivacion: mocks.validar,
    apiActivarCuenta: mocks.activar,
  }
})

vi.mock('../../../layouts/AuthLayout/AuthLayout', () => ({
  default: ({ children }) => <div>{children}</div>,
}))

function renderPagina(token = 'token-valido') {
  return render(
    <MemoryRouter initialEntries={[`/activar-cuenta#token=${token}`]}>
      <ActivarCuenta />
    </MemoryRouter>
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  mocks.validar.mockResolvedValue({
    nombre: 'Ana',
    correo: 'a***@test.local',
  })
  mocks.activar.mockResolvedValue({ message: 'Cuenta activada.' })
})

describe('ActivarCuenta', () => {
  it('valida el token, muestra el formulario y activa con una frase de 12+', async () => {
    const user = userEvent.setup()
    const { container } = renderPagina()

    expect(await screen.findByRole('heading', { name: /Activa tu cuenta/i })).toBeInTheDocument()

    const campos = container.querySelectorAll('input[type="password"]')
    expect(campos).toHaveLength(2)
    await user.type(campos[0], 'Frase segura 2026')
    await user.type(campos[1], 'Frase segura 2026')

    await user.click(screen.getByRole('button', { name: /Activar mi cuenta/i }))

    expect(await screen.findByRole('heading', { name: /Cuenta activada/i })).toBeInTheDocument()
    expect(mocks.activar).toHaveBeenCalledWith({
      token: 'token-valido',
      password: 'Frase segura 2026',
      password_confirmation: 'Frase segura 2026',
    })
  })

  it('muestra enlace inválido cuando el token no es válido', async () => {
    mocks.validar.mockRejectedValue(new Error('inválido'))
    renderPagina('token-vencido')

    expect(await screen.findByRole('heading', { name: /Enlace inválido o vencido/i })).toBeInTheDocument()
  })

  it('no envía si las contraseñas no coinciden', async () => {
    const user = userEvent.setup()
    const { container } = renderPagina()

    await screen.findByRole('heading', { name: /Activa tu cuenta/i })
    const campos = container.querySelectorAll('input[type="password"]')
    await user.type(campos[0], 'Frase segura 2026')
    await user.type(campos[1], 'Otra frase distinta')

    await user.click(screen.getByRole('button', { name: /Activar mi cuenta/i }))

    await waitFor(() => {
      expect(screen.getByText(/Las contraseñas no coinciden/i)).toBeInTheDocument()
    })
    expect(mocks.activar).not.toHaveBeenCalled()
  })
})
