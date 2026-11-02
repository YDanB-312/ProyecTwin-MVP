import { test, expect, login } from './helpers'

async function confirmarConVerificacion(page, texto = 'ELIMINAR') {
  const dialog = page.getByRole('alertdialog')
  await expect(dialog).toBeVisible()

  const confirmar = dialog.getByRole('button', { name: /Sí, eliminar/i })
  await expect(confirmar).toBeDisabled()

  await dialog.getByRole('checkbox').check()
  await expect(confirmar).toBeDisabled()

  await dialog.getByRole('textbox').fill(texto)
  await expect(confirmar).toBeEnabled()
  await confirmar.click()
}

test.describe('Admin: eliminar usuarios', () => {
  test('los usuarios con historial exigen confirmación reforzada y decisión explícita', async ({ page }) => {
    await login(page, 'admin')

    // Usuario 1: aprendiz con propuestas y similitudes.
    // Usuario 9: instructor con fichas y propuestas a cargo.
    for (const id of [1, 9]) {
      await page.goto(`/admin/detalle-usuario/${id}`)
      await page.getByRole('button', { name: /^Eliminar$/i }).click()

      await confirmarConVerificacion(page)

      await expect(page.getByText('Usuario con historial')).toBeVisible()
      await page.getByRole('alertdialog').getByRole('button', { name: /Cancelar/i }).click()
    }
  })

  test('elimina un usuario sin historial y lo retira del listado', async ({ page }) => {
    const correo = 'andres.cifuentes@example.com'

    await login(page, 'admin')
    await page.goto('/admin/detalle-usuario/8')
    await page.getByRole('button', { name: /^Eliminar$/i }).click()

    await confirmarConVerificacion(page)

    await page.waitForURL('**/admin/usuarios')
    await page.getByPlaceholder(/Nombre, documento, usuario o correo/i).fill(correo)
    await expect(page.locator('tbody tr')).toHaveCount(0)
  })

  test('editar a un correo existente muestra error', async ({ page }) => {
    await login(page, 'admin')
    await page.goto('/admin/detalle-usuario/6')
    await page.getByRole('button', { name: /^Editar$/i }).click()
    await page.getByLabel(/Correo electr/i).fill('maria.gonzalez@example.com')
    await page.getByRole('button', { name: /Guardar cambios/i }).click()

    await expect(page.getByText(/Ya existe un usuario/i)).toBeVisible()
  })
})
