import { test, expect, login } from './helpers'

test.describe('Dashboards por rol', () => {
  test('el aprendiz ve sus indicadores', async ({ page }) => {
    await login(page, 'aprendiz')
    await expect(page.getByText('Propuestas registradas').first()).toBeVisible()
    await expect(page.getByText('Notificaciones sin leer').first()).toBeVisible()
  })

  test('el instructor ve su turno y sus fichas', async ({ page }) => {
    await login(page, 'instructor')
    await expect(page.getByText('Por revisar', { exact: true }).first()).toBeVisible()
    await expect(page.getByText('Tus cohortes').first()).toBeVisible()
  })

  test('el admin ve la salud del motor y sus colas', async ({ page }) => {
    await login(page, 'admin')
    await expect(page.getByText('Salud del motor').first()).toBeVisible()
    await expect(page.getByText('Colas por atender').first()).toBeVisible()
  })
})

test.describe('Permisos de navegación', () => {
  test('aprendiz e instructor no pueden abrir el panel de administración', async ({ page }) => {
    await login(page, 'aprendiz')
    await page.goto('/admin/usuarios')
    await expect(page.getByText(/Acceso denegado/i).first()).toBeVisible()

    await page.context().clearCookies()
    await login(page, 'instructor')
    await page.goto('/admin/dashboard')
    await expect(page.getByText(/Acceso denegado/i).first()).toBeVisible()
  })
})
