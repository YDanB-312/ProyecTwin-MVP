import { test, expect, login } from './helpers'

test.describe('Hover de DataTable', () => {
  test('la fila y la celda sticky comparten el fondo en escritorio', async ({ page }) => {
    await login(page, 'admin')
    await page.goto('/admin/usuarios')
    // La tabla se rellena desde la API; esperar a que termine de estabilizarse
    // antes de mover el mouse evita medir una fila que luego se reemplaza.
    await page.waitForLoadState('networkidle')

    const fila = page.locator('tbody tr').first()
    await expect(fila).toBeVisible()

    // Instalar los listeners antes del hover y esperar los transitionend de
    // fondo de la fila y de la celda sticky. No se usa un timeout fijo.
    await page.evaluate(() => {
      const row = document.querySelector('tbody tr')
      if (!row) return
      const sticky = row.querySelector('td:last-child')
      if (!sticky) return
      window.__hoverTransitions = 0
      const onEnd = (event) => {
        if (event.propertyName === 'background-color') window.__hoverTransitions += 1
      }
      row.addEventListener('transitionend', onEnd)
      sticky.addEventListener('transitionend', onEnd)
    })

    await fila.hover()
    await page.waitForFunction(() => window.__hoverTransitions >= 2, null, { timeout: 2000 })

    const fondoFila = await fila.evaluate((el) => getComputedStyle(el).backgroundColor)
    const fondoSticky = await fila.locator('td').last().evaluate((el) => getComputedStyle(el).backgroundColor)
    expect(fondoFila).not.toBe('rgba(0, 0, 0, 0)')
    expect(fondoSticky).toBe(fondoFila)
  })

  test('en móvil la celda sticky no conserva un fondo opaco', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 })
    await login(page, 'admin')
    await page.goto('/admin/usuarios')

    const sticky = page.locator('tbody tr').first().locator('td').last()
    const fondo = await sticky.evaluate((el) => getComputedStyle(el).backgroundColor)
    expect(fondo).toBe('rgba(0, 0, 0, 0)')
  })
})
