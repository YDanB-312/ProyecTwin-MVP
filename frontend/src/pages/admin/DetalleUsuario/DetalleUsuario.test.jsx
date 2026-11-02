import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import DetalleUsuario from './DetalleUsuario'

const mocks = vi.hoisted(() => ({
  data: null,
  recargar: vi.fn(),
  usuariosActualizar: vi.fn(),
  aprendicesActualizar: vi.fn(),
  auth: {
    user: { id: 99, rol: 'admin' },
    sincronizarSesion: vi.fn(),
  },
}))

vi.mock('../../../lib/useApi', () => ({
  useApi: () => ({
    data: mocks.data,
    cargando: false,
    error: null,
    recargar: mocks.recargar,
    setData: vi.fn(),
  }),
}))

vi.mock('../../../contexts/AuthContext', () => ({
  useAuth: () => mocks.auth,
}))

vi.mock('../../../lib/recursos', () => ({
  usuarios: {
    obtener: vi.fn(),
    actualizar: mocks.usuariosActualizar,
    eliminar: vi.fn(),
    eliminarDefinitivo: vi.fn(),
    restablecerCredenciales: vi.fn(),
    cambiarCorreo: vi.fn(),
  },
  aprendices: {
    listar: vi.fn(),
    actualizar: mocks.aprendicesActualizar,
  },
  fichas: { listar: vi.fn() },
  proyectos: { listar: vi.fn() },
  similitudes: { listar: vi.fn() },
}))

vi.mock('../../../layouts/DashboardLayout/DashboardLayout', () => ({
  default: ({ children }) => <div>{children}</div>,
}))

vi.mock('../../../components/PerfilBase/PerfilBase', () => ({
  default: () => <div data-testid="perfil-base" />,
}))

function crearDatos({ conPerfil = true } = {}) {
  const usuario = {
    id: 10,
    nombre: 'Ana',
    apellido: 'Pérez',
    correo: 'ana@example.com',
    rol: 'aprendiz',
    estado: true,
    tipo_documento: 'CC',
    numero_documento: '123456789',
    email_verified_at: '2026-01-01T00:00:00Z',
    foto_url: null,
  }

  const perfilAprendiz = conPerfil
    ? {
        id: 5,
        id_usuario: 10,
        id_class_group: 1,
        codigo: 'APR-001',
        classGroup: {
          id: 1,
          nombre: 'Ficha Uno',
          codigo: 'F1',
          program: { id: 1, nombre: 'ADSO' },
        },
      }
    : null

  return {
    usuario,
    listaAprendices: perfilAprendiz ? [perfilAprendiz] : [],
    listaFichas: [
      {
        id: 1,
        nombre: 'Ficha Uno',
        codigo: 'F1',
        estado: 'activo',
        id_programa: 1,
        program: { id: 1, nombre: 'ADSO' },
      },
      {
        id: 2,
        nombre: 'Ficha Dos',
        codigo: 'F2',
        estado: 'activo',
        id_programa: 2,
        program: { id: 2, nombre: 'Inglés' },
      },
    ],
    listaProyectos: [],
    listaSimilitudes: [],
  }
}

function renderizarPagina() {
  return render(
    <MemoryRouter initialEntries={['/admin/detalle-usuario/10']}>
      <Routes>
        <Route path="/admin/detalle-usuario/:id" element={<DetalleUsuario />} />
      </Routes>
    </MemoryRouter>
  )
}

async function abrirEdicion(user) {
  await user.click(screen.getByRole('button', { name: /editar/i }))
  await screen.findByLabelText(/rol/i)
}

describe('DetalleUsuario: guardado de rol y ficha', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.data = crearDatos()
    mocks.recargar.mockResolvedValue(undefined)
    mocks.usuariosActualizar.mockResolvedValue({})
    mocks.aprendicesActualizar.mockResolvedValue({})
  })

  it.each(['instructor', 'admin'])(
    'no actualiza la ficha si el nuevo rol %s deja de ser aprendiz',
    async (rolNuevo) => {
      const user = userEvent.setup()
      renderizarPagina()
      await abrirEdicion(user)

      // Primero se cambia la ficha y después el rol: al dejar de ser aprendiz,
      // el selector desaparece porque depende del rol del formulario.
      await user.selectOptions(screen.getByLabelText('Ficha'), '2')
      await user.selectOptions(screen.getByLabelText(/rol/i), rolNuevo)
      expect(screen.queryByLabelText('Ficha')).not.toBeInTheDocument()

      await user.click(screen.getByRole('button', { name: /guardar cambios/i }))

      await waitFor(() => expect(mocks.usuariosActualizar).toHaveBeenCalledTimes(1))
      expect(mocks.usuariosActualizar).toHaveBeenCalledWith(
        10,
        expect.objectContaining({ rol: rolNuevo })
      )
      expect(mocks.aprendicesActualizar).not.toHaveBeenCalled()
    }
  )

  it('actualiza la ficha si el nuevo rol sigue siendo aprendiz y la selección cambió', async () => {
    const user = userEvent.setup()
    renderizarPagina()
    await abrirEdicion(user)

    await user.selectOptions(screen.getByLabelText('Ficha'), '2')
    await user.click(screen.getByRole('button', { name: /guardar cambios/i }))

    await waitFor(() => expect(mocks.aprendicesActualizar).toHaveBeenCalledTimes(1))
    expect(mocks.aprendicesActualizar).toHaveBeenCalledWith(5, {
      codigo: 'APR-001',
      id_class_group: 2,
      id_usuario: 10,
      id_programa: 2,
    })
  })

  it('no actualiza la ficha si el rol es aprendiz y la selección no cambió', async () => {
    const user = userEvent.setup()
    renderizarPagina()
    await abrirEdicion(user)

    await user.click(screen.getByRole('button', { name: /guardar cambios/i }))

    await waitFor(() => expect(mocks.usuariosActualizar).toHaveBeenCalledTimes(1))
    expect(mocks.aprendicesActualizar).not.toHaveBeenCalled()
  })

  it('no actualiza la ficha si no existe perfil de aprendiz', async () => {
    mocks.data = crearDatos({ conPerfil: false })
    const user = userEvent.setup()
    renderizarPagina()
    await abrirEdicion(user)

    expect(screen.queryByLabelText('Ficha')).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /guardar cambios/i }))

    await waitFor(() => expect(mocks.usuariosActualizar).toHaveBeenCalledTimes(1))
    expect(mocks.aprendicesActualizar).not.toHaveBeenCalled()
  })
})
