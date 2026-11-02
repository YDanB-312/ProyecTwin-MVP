import { test, expect, login } from './helpers'

// El corpus se construye solo con propuestas aprobadas y cada rol ve su
// alcance: el aprendiz sus coincidencias, el instructor sus fichas y el admin
// la supervisión completa.
test.describe('Similitudes por rol', () => {
  test('el aprendiz ve y expande sus coincidencias', async ({ page }) => {
    await login(page, 'aprendiz')
    await page.goto('/aprendiz/similitudes')

    await expect(page.getByRole('heading', { name: /^Similitudes$/ })).toBeVisible()
    const grupo = page.getByRole('button', { name: /coincidencia/i }).first()
    await expect(grupo).toBeVisible({ timeout: 20000 })
    await expect(grupo).toHaveAttribute('aria-expanded', 'false')

    await grupo.click()
    await expect(grupo).toHaveAttribute('aria-expanded', 'true')
    await expect(page.locator('a[href*="/aprendiz/detalle-similitud/"]').first()).toBeVisible()
  })

  test('el instructor ve las coincidencias de sus fichas', async ({ page }) => {
    await login(page, 'instructor')
    await page.goto('/instructor/similitudes')

    await expect(page.getByRole('heading', { name: /Similitudes Detectadas/i })).toBeVisible()
    await expect(page.getByText(/coincidencia/i).first()).toBeVisible({ timeout: 20000 })
    await expect(page.getByRole('link', { name: /^Ver$/i }).first()).toBeVisible()
  })

  test('el admin ve el resumen de supervisión del motor', async ({ page }) => {
    await login(page, 'admin')
    await page.goto('/admin/similitudes')

    const resumen = page.getByRole('status', { name: /Resumen de coincidencias/i })
    await expect(resumen).toBeVisible()
    await expect(resumen.getByText('Pares', { exact: true })).toBeVisible()
    await expect(resumen.getByText('Programas', { exact: true })).toBeVisible()
    await expect(resumen.getByText('Umbral', { exact: true })).toBeVisible()
  })
})
