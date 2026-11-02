import { describe, expect, it, vi } from 'vitest'
import { render } from '@testing-library/react'
import TextType from './TextType'

describe('TextType', () => {
  it('respeta reduced motion y muestra el texto completo', () => {
    window.matchMedia = vi.fn().mockReturnValue({ matches: true })

    const { container } = render(<TextType text="Hola ProyecTwin" />)

    expect(container).toHaveTextContent('Hola ProyecTwin')
    expect(container.querySelector('.sr-only')).not.toBeInTheDocument()
  })

  it('mantiene el texto real en sr-only durante la animación', () => {
    window.matchMedia = vi.fn().mockReturnValue({ matches: false })
    vi.stubGlobal('requestAnimationFrame', (cb) => { cb(); return 1 })
    vi.stubGlobal('cancelAnimationFrame', vi.fn())

    const { container } = render(<TextType text="Hola" speed={1000} startDelay={1000} />)

    expect(container.querySelector('.sr-only')).toHaveTextContent('Hola')
    expect(container.querySelector('[aria-hidden="true"]')).toBeInTheDocument()
    vi.unstubAllGlobals()
  })
})
