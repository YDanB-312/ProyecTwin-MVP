import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import ApkDownloadButton from './ApkDownloadButton'
import { useApi } from '../../lib/useApi'

vi.mock('../../lib/useApi', () => ({
  useApi: vi.fn(),
}))

describe('ApkDownloadButton', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('muestra el enlace cuando el backend anuncia una descarga', () => {
    useApi.mockReturnValue({
      data: { download_url: '/apk/ProyecTWin.apk' },
    })

    render(<ApkDownloadButton />)

    const enlace = screen.getByRole('link', { name: /Descargar APK de ProyecTWIN/i })
    expect(enlace).toHaveAttribute('href', '/apk/ProyecTWin.apk')
    expect(enlace).toHaveAttribute('download', 'ProyecTWin.apk')
  })

  it('no muestra el enlace si no hay URL configurada', () => {
    useApi.mockReturnValue({ data: { download_url: '' } })

    render(<ApkDownloadButton />)

    expect(screen.queryByRole('link', { name: /Descargar APK/i })).not.toBeInTheDocument()
  })

  it('no muestra el enlace si la URL no es HTTP(S) o relativa', () => {
    useApi.mockReturnValue({
      data: { download_url: 'javascript:alert(1)' },
    })

    render(<ApkDownloadButton />)

    expect(screen.queryByRole('link', { name: /Descargar APK/i })).not.toBeInTheDocument()
  })

  it('no muestra el enlace si el endpoint falla', () => {
    useApi.mockReturnValue({
      data: null,
      error: { status: 404 },
    })

    render(<ApkDownloadButton />)

    expect(screen.queryByRole('link', { name: /Descargar APK/i })).not.toBeInTheDocument()
  })

  it('rechaza URLs protocol-relative y HTTP', () => {
    useApi.mockReturnValue({
      data: { download_url: '//evil.example/app.apk' },
    })
    const { unmount } = render(<ApkDownloadButton />)
    expect(screen.queryByRole('link', { name: /Descargar APK/i })).not.toBeInTheDocument()
    unmount()

    useApi.mockReturnValue({
      data: { download_url: 'http://example.com/app.apk' },
    })
    render(<ApkDownloadButton />)
    expect(screen.queryByRole('link', { name: /Descargar APK/i })).not.toBeInTheDocument()
  })

  it('acepta una URL HTTPS absoluta', () => {
    useApi.mockReturnValue({
      data: { download_url: 'https://cdn.example.com/ProyecTWin.apk' },
    })

    render(<ApkDownloadButton />)

    expect(screen.getByRole('link', { name: /Descargar APK/i })).toHaveAttribute(
      'href',
      'https://cdn.example.com/ProyecTWin.apk'
    )
  })
})
