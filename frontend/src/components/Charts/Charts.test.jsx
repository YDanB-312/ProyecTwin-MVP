import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import axe from 'axe-core'
import { BarChart, LineChart } from './Charts'

const LINE_DATA = [
  { clave: '2026-08', label: 'ago.', fecha: 'agosto de 2026', value: 1, propuestas: [] },
  { clave: '2026-09', label: 'sep.', fecha: 'septiembre de 2026', value: 0, propuestas: [] },
  { clave: '2026-10', label: 'oct.', fecha: 'octubre de 2026', value: 3, propuestas: [] },
]

const BAR_DATA = [
  { label: 'Aprendices', value: 9, color: 'var(--c-primary)' },
  { label: 'Instructores', value: 4, color: 'var(--c-info)' },
  { label: 'Administradores', value: 2, color: 'var(--c-warning)' },
]

describe('LineChart', () => {
  it('expone un grupo accesible sin ocultar un SVG con controles enfocables', () => {
    const { container } = render(<LineChart data={LINE_DATA} ariaLabel="Propuestas por mes" />)

    expect(screen.getByRole('group', { name: /Propuestas por mes/ })).toBeInTheDocument()
    expect(container.querySelector('svg')).not.toHaveAttribute('aria-hidden')
    expect(screen.queryByRole('img')).not.toBeInTheDocument()
    expect(screen.getAllByRole('button')).toHaveLength(LINE_DATA.length)
  })

  it('mantiene el foco por teclado y muestra el tooltip del punto', async () => {
    const user = userEvent.setup()
    render(<LineChart data={LINE_DATA} ariaLabel="Propuestas por mes" />)

    const primerPunto = screen.getByRole('button', { name: 'agosto de 2026: 1 propuesta' })
    expect(primerPunto).toHaveAttribute('tabindex', '0')

    await user.tab()
    expect(primerPunto).toHaveFocus()
    expect(screen.getByText('agosto de 2026')).toBeInTheDocument()
    expect(screen.getByText('1 propuesta')).toBeInTheDocument()
  })
})

describe('Accesibilidad de los gráficos', () => {
  it('no registra aria-hidden-focus, nested-interactive ni listitem', async () => {
    const { container } = render(
      <>
        <LineChart data={LINE_DATA} ariaLabel="Propuestas por mes" />
        <BarChart data={BAR_DATA} ariaLabel="Usuarios por rol" />
      </>,
    )

    const resultados = await axe.run(container, {
      runOnly: {
        type: 'rule',
        values: ['aria-hidden-focus', 'nested-interactive', 'listitem'],
      },
    })

    expect(resultados.violations).toEqual([])
  })
})

describe('BarChart', () => {
  it('conserva la semántica de lista y el resumen accesible', () => {
    render(<BarChart data={BAR_DATA} ariaLabel="Usuarios por rol" />)

    const lista = screen.getByRole('list', { name: /Usuarios por rol/ })
    expect(lista).toBeInTheDocument()
    expect(lista).not.toHaveAttribute('role')
    expect(screen.getAllByRole('listitem')).toHaveLength(BAR_DATA.length)
    expect(screen.getByText('Aprendices')).toBeInTheDocument()
    expect(screen.getByText('9')).toBeInTheDocument()
    expect(screen.getByText('Instructores')).toBeInTheDocument()
    expect(screen.getByText('4')).toBeInTheDocument()
    expect(screen.getByText('Administradores')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()
  })
})
