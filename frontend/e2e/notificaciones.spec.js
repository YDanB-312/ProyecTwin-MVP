import { test, expect, login, crearNotificacionApi } from './helpers'

test.describe('Notificaciones', () => {
  test('una notificación de ficha del seed navega al detalle', async ({ page }) => {
    await login(page, 'instructor')
    await page.goto('/instructor/alertas')

    const aviso = page.getByRole('button', { name: /Cifuentes se uni/i }).first()
    await expect(aviso).toBeVisible({ timeout: 20000 })
    await expect(aviso).toHaveAttribute('title', 'Abrir detalle')

    await aviso.click()
    await page.waitForURL('**/instructor/detalle-ficha/1')
    await expect(page.getByRole('heading', { name: /Analisis y Desarrollo 2568/ })).toBeVisible()
  })

  test('el aprendiz marca como leída una notificación sin enlace', async ({ page, request }) => {
    const titulo = `Notificación E2E ${Date.now()}`
    await crearNotificacionApi(request, { titulo, idUsuario: 1 })

    await login(page, 'aprendiz')
    await page.goto('/aprendiz/alertas')

    const aviso = page.getByRole('button', { name: new RegExp(titulo) })
    await expect(aviso).toBeVisible({ timeout: 20000 })
    await expect(aviso).toHaveAttribute('title', 'Marcar como leída')
    await expect(aviso.getByText('Nueva')).toBeVisible()

    const marcado = page.waitForResponse(
      (r) => r.request().method() === 'PUT' && /\/v1\/notifications\/\d+$/.test(new URL(r.url()).pathname) && r.ok()
    )
    await aviso.click()
    await marcado

    await expect(page).toHaveURL(/\/aprendiz\/alertas$/)
    await expect(aviso.getByText('Nueva')).toHaveCount(0)
    await expect(aviso).toBeVisible()
  })
})
