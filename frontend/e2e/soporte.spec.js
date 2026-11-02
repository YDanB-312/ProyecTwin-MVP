import { test, expect, login, logout } from './helpers'

// Soporte de punta a punta: el aprendiz reporta una falla y el admin la cierra
// con respuesta, dejando trazabilidad y aviso al solicitante.
test('el aprendiz reporta una falla y el admin la resuelve', async ({ page }) => {
  test.setTimeout(90000)

  const titulo = `Soporte E2E ${Date.now()}`

  await login(page, 'aprendiz')
  await page.goto('/aprendiz/reportar-falla')
  await page.getByLabel(/Título de la solicitud/i).fill(titulo)
  await page.getByLabel(/Descripción/i).fill('Al abrir el detalle de una propuesta la página queda en blanco en móvil.')
  await page.getByRole('button', { name: /Enviar solicitud/i }).click()
  await expect(page.getByText(/Gracias por escribirnos/i)).toBeVisible({ timeout: 15000 })

  await logout(page)
  await login(page, 'admin')
  await page.goto('/admin/reportes-fallas')
  await page.getByPlaceholder(/Título, número de ficha, #id o reportante/i).fill(titulo)
  const fila = page.locator('tr', { hasText: titulo }).first()
  await expect(fila).toBeVisible({ timeout: 15000 })
  await fila.getByRole('link', { name: /Ver/i }).click()
  await page.waitForURL('**/admin/detalle-reporte/**')

  await page.getByLabel('Cambiar estado').selectOption('resuelto')
  await page.getByLabel(/Respuesta al solicitante/i).fill('Se corrigió el detalle en móvil; el reporte queda cerrado.')
  await page.getByRole('button', { name: /^Guardar$/i }).click()

  await expect(page.getByText(/se actualizó correctamente/i)).toBeVisible({ timeout: 15000 })
  await expect(page.getByLabel('Cambiar estado')).toHaveValue('resuelto')
})
