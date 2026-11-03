import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ThemeProvider } from '../../../contexts/ThemeContext'
import Home from './Home'

let respuestaApi = {
  download_url: 'https://proyectwin.test/apk/ProyecTWin.apk',
  total_propuestas: 10,
  total_programas: 2,
}

vi.mock('../../../lib/useApi', () => ({
  useApi: () => ({ data: respuestaApi, cargando: false, error: null, recargar: vi.fn() }),
}))

vi.mock('../../../lib/recursos', () => ({
  appVersion: { android: vi.fn() },
  demo: { resumen: vi.fn(), comparar: vi.fn() },
}))

describe('Home Android download', () => {
  beforeEach(() => {
    respuestaApi = {
      download_url: 'https://proyectwin.test/apk/ProyecTWin.apk',
      total_propuestas: 10,
      total_programas: 2,
    }
  })

  const renderizar = () => render(
    <MemoryRouter>
      <ThemeProvider>
        <Home />
      </ThemeProvider>
    </MemoryRouter>,
  )

  it('muestra el enlace HTTPS cuando hay versión publicada', () => {
    renderizar()

    const enlace = screen.getByRole('link', { name: /descargar aplicación android/i })
    expect(enlace).toHaveAttribute('href', 'https://proyectwin.test/apk/ProyecTWin.apk')
    expect(enlace).toHaveAttribute('download')
  })

  it('no muestra un enlace roto cuando no hay URL publicada', () => {
    respuestaApi = { download_url: '', total_propuestas: 10, total_programas: 2 }
    renderizar()

    expect(screen.queryByRole('link', { name: /descargar aplicación android/i })).not.toBeInTheDocument()
  })

  it('muestra la ruta same-origin preparada para el APK', () => {
    respuestaApi = { ...respuestaApi, download_url: '/apk/ProyecTWin.apk' }
    renderizar()

    const enlace = screen.getByRole('link', { name: /descargar aplicación android/i })
    expect(enlace).toHaveAttribute('href', '/apk/ProyecTWin.apk')
    expect(enlace).toHaveAttribute('download', 'ProyecTWin.apk')
  })

  it('oculta una URL HTTP no segura', () => {
    respuestaApi = { ...respuestaApi, download_url: 'http://example.com/app.apk' }
    renderizar()

    expect(screen.queryByRole('link', { name: /descargar aplicación android/i })).not.toBeInTheDocument()
  })
})
