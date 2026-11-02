import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import RestablecerContrasena from './RestablecerContrasena'

const mocks = vi.hoisted(() => ({
  reset: vi.fn(),
}))

vi.mock('../../../lib/api', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    apiResetPassword: mocks.reset,
  }
})

vi.mock('../../../layouts/AuthLayout/AuthLayout', () => ({
  default: ({ children }) => <div>{children}</div>,
}))

function renderPagina(entrada = '/restablecer-contrasena#token=token-valido&correo=ana%40test.local') {
  return render(
    <MemoryRouter initialEntries={[entrada]}>
      <RestablecerContrasena />
    </MemoryRouter>
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  mocks.reset.mockResolvedValue({ message: 'Contraseña restablecida.' })
})

describe('RestablecerContrasena', () => {
  it('lee token y correo del fragmento y restablece la contraseña', async () => {
    const user = userEvent.setup()
    const { container } = renderPagina()

    expect(screen.getByDisplayValue('ana@test.local')).toBeInTheDocument()

    const campos = container.querySelectorAll('input[type="password"]')
    expect(campos).toHaveLength(2)
    await user.type(campos[0], 'Frase segura 2026')
    await user.type(campos[1], 'Frase segura 2026')
    await user.click(screen.getByRole('button', { name: /Restablecer contraseña/i }))

    expect(await screen.findByRole('heading', { name: /Contraseña restablecida/i })).toBeInTheDocument()
    expect(mocks.reset).toHaveBeenCalledWith({
      correo: 'ana@test.local',
      token: 'token-valido',
      password: 'Frase segura 2026',
      password_confirmation: 'Frase segura 2026',
    })
  })

  it('mantiene compatibilidad con enlaces antiguos que usan query string', async () => {
    renderPagina('/restablecer-contrasena?token=token-viejo&correo=ana%40test.local')

    expect(screen.getByDisplayValue('ana@test.local')).toBeInTheDocument()
  })

  it('muestra enlace inválido si falta el token', async () => {
    renderPagina('/restablecer-contrasena')

    expect(await screen.findByRole('heading', { name: /Enlace inválido/i })).toBeInTheDocument()
  })

  it('no trata 429 como enlace vencido', async () => {
    const user = userEvent.setup()
    const error = new Error('Too Many Requests')
    error.status = 429
    mocks.reset.mockRejectedValue(error)

    const { container } = renderPagina()
    const campos = container.querySelectorAll('input[type="password"]')
    await user.type(campos[0], 'Frase segura 2026')
    await user.type(campos[1], 'Frase segura 2026')
    await user.click(screen.getByRole('button', { name: /Restablecer contraseña/i }))

    expect(await screen.findByText(/Demasiados intentos/i)).toBeInTheDocument()
    expect(screen.queryByText(/El enlace es inválido o venció/i)).not.toBeInTheDocument()
  })

  it('distingue un fallo de conexión de un enlace vencido', async () => {
    const user = userEvent.setup()
    const error = new Error('No se pudo conectar')
    error.status = 0
    mocks.reset.mockRejectedValue(error)

    const { container } = renderPagina()
    const campos = container.querySelectorAll('input[type="password"]')
    await user.type(campos[0], 'Frase segura 2026')
    await user.type(campos[1], 'Frase segura 2026')
    await user.click(screen.getByRole('button', { name: /Restablecer contraseña/i }))

    expect(await screen.findByText(/No se pudo conectar con el servidor/i)).toBeInTheDocument()
  })
})
