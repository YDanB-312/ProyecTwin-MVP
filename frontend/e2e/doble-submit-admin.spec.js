import { test, expect, login } from './helpers'

// El guardado de estado (admin) no debe disparar dos peticiones por doble clic.
test('guardar estado no envía dos peticiones por doble clic', async ({ page }) => {
  await login(page, 'admin')

  let puts = 0
  await page.route('**/v1/projects/4', async (route) => {
    if (route.request().method() === 'PUT') {
      puts++
      await new Promise((r) => setTimeout(r, 800))
    }
    await route.continue()
  })

  await page.goto('/admin/detalle-proyecto/4')
  await page.getByLabel('Cambiar estado').selectOption('aprobado')

  const guardar = page.getByRole('button', { name: /^Guardar$/ })
  await guardar.click()
  await expect(guardar).toBeDisabled()
  // Segundo clic durante la petición: no debe enviar nada.
  await guardar.click({ force: true, timeout: 1000 }).catch(() => {})

  await expect(page.getByText(/Estado actualizado correctamente/)).toBeVisible({ timeout: 15000 })
  expect(puts).toBe(1)
})
