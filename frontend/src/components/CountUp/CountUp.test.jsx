import { afterEach, describe, expect, it, vi } from 'vitest'
import { render } from '@testing-library/react'
import CountUp from './CountUp'

afterEach(() => {
  vi.unstubAllGlobals()
  vi.restoreAllMocks()
})

describe('CountUp', () => {
  it('expone el valor final en sr-only y oculta el rodillo', () => {
    vi.stubGlobal('requestAnimationFrame', (callback) => {
      callback()
      return 1
    })
    vi.stubGlobal('cancelAnimationFrame', vi.fn())
    window.matchMedia = vi.fn().mockReturnValue({ matches: false })

    const { container } = render(<CountUp value="35%" />)

    const accesible = container.querySelector('.sr-only')
    expect(accesible).toHaveTextContent('35%')
    expect(container.querySelector('[aria-hidden="true"]')).toBeInTheDocument()
  })

  it('muestra el valor tal cual si no es numérico', () => {
    const { container } = render(<CountUp value="—" />)
    expect(container).toHaveTextContent('—')
    expect(container.querySelector('.sr-only')).not.toBeInTheDocument()
  })

  it('respeta prefers-reduced-motion y evita el rodillo', () => {
    window.matchMedia = vi.fn().mockReturnValue({ matches: true })

    const { container } = render(<CountUp value="35%" />)

    expect(container).toHaveTextContent('35%')
    expect(container.querySelector('[aria-hidden="true"]')).not.toBeInTheDocument()
  })
})
