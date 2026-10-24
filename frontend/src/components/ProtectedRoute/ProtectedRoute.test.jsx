import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import ProtectedRoute from './ProtectedRoute'
import { useAuth } from '../../contexts/AuthContext'

vi.mock('../../contexts/AuthContext', () => ({
  useAuth: vi.fn(),
}))

// Se sustituye el layout autenticado por un contenedor simple: este test
// verifica el contrato de ProtectedRoute, no la navegación del dashboard.
vi.mock('../../layouts/DashboardLayout/DashboardLayout', () => ({
  default: ({ children }) => <div>{children}</div>,
}))

function auth(overrides = {}) {
  return {
    user: { rol: 'admin', mustChangePassword: false },
    cargando: false,
    isAuthenticated: true,
    ...overrides,
  }
}

function renderRuta(authState, initial = '/admin/usuarios', allowedRoles = ['admin']) {
  useAuth.mockReturnValue(authState)

  return render(
    <MemoryRouter initialEntries={[initial]}>
      <Routes>
        <Route path="/login" element={<div>Pantalla login</div>} />
        <Route path="/cambio-obligatorio" element={<div>Cambio obligatorio</div>} />
        <Route
          path="/admin/usuarios"
          element={(
            <ProtectedRoute allowedRoles={allowedRoles}>
              <div>Contenido permitido</div>
            </ProtectedRoute>
          )}
        />
        <Route path="/instructor/dashboard" element={<div>Panel instructor</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('redirige a login cuando no hay sesión', () => {
    renderRuta(auth({ user: null, isAuthenticated: false }))

    expect(screen.getByText('Pantalla login')).toBeInTheDocument()
    expect(screen.queryByText('403 · Acceso denegado')).not.toBeInTheDocument()
  })

  it('muestra acceso denegado para una sesión autenticada con rol incorrecto', () => {
    renderRuta(auth({ user: { rol: 'instructor', mustChangePassword: false } }))

    expect(screen.getByText('403 · Acceso denegado')).toBeInTheDocument()
    expect(screen.queryByText('Pantalla login')).not.toBeInTheDocument()
    expect(screen.queryByText('Contenido permitido')).not.toBeInTheDocument()
  })

  it('permite el acceso con el rol correcto', () => {
    renderRuta(auth())

    expect(screen.getByText('Contenido permitido')).toBeInTheDocument()
    expect(screen.queryByText('403 · Acceso denegado')).not.toBeInTheDocument()
  })

  it('mantiene la redirección obligatoria de contraseña antes del control de rol', () => {
    renderRuta(auth({ user: { rol: 'instructor', mustChangePassword: true } }))

    expect(screen.getByText('Cambio obligatorio')).toBeInTheDocument()
    expect(screen.queryByText('403 · Acceso denegado')).not.toBeInTheDocument()
  })

  it('ofrece una salida segura al panel del rol sin crear un bucle', async () => {
    const user = userEvent.setup()
    renderRuta(auth({ user: { rol: 'instructor', mustChangePassword: false } }))

    await user.click(screen.getByRole('button', { name: /Volver a mi panel/i }))

    expect(screen.getByText('Panel instructor')).toBeInTheDocument()
    expect(screen.queryByText('403 · Acceso denegado')).not.toBeInTheDocument()
  })

  it('mantiene la ruta pública de login fuera de ProtectedRoute', () => {
    render(
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<div>Pantalla login</div>} />
        </Routes>
      </MemoryRouter>,
    )

    expect(screen.getByText('Pantalla login')).toBeInTheDocument()
  })
})
