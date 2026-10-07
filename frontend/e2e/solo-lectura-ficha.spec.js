import { test, expect, login } from './helpers'

// Regla Classroom: fuera de la ficha la propuesta es SOLO LECTURA. Se ve el
// historial, pero no se comenta, ni edita, ni elimina.
test('fuera de la ficha la propuesta es solo lectura', async ({ page }) => {
  await login(page, 'aprendiz')

  // Salir de la ficha (idempotente frente a reintentos).
  await page.goto('/aprendiz/ficha')
  const salir = page.getByRole('button', { name: /Salir de la ficha/i })
  const buscar = page.getByRole('button', { name: /Buscar ficha/i })
  await expect(salir.or(buscar).first()).toBeVisible({ timeout: 15000 })
  if (await salir.isVisible().catch(() => false)) {
    await salir.click()
    await page.getByRole('button', { name: /Sí, salir/i }).click()
    await expect(buscar).toBeVisible({ timeout: 15000 })
  }

  // Propuesta 4 (creada por María): se ve, pero en solo lectura.
  await page.goto('/aprendiz/detalle-proyecto/4')
  await expect(page.getByText('Plataforma de Ventas Online').first()).toBeVisible()
  await expect(page.getByText(/Solo lectura: ya no perteneces a esta ficha/i)).toBeVisible()

  await expect(page.getByRole('button', { name: /Agregar observación/i })).toHaveCount(0)
  await expect(page.getByRole('button', { name: 'Editar', exact: true })).toHaveCount(0)
  await expect(page.getByRole('button', { name: 'Eliminar', exact: true })).toHaveCount(0)
})

// Un borrador que quedó en una ficha anterior no ofrece "Enviar" en la lista
// (el backend exige ficha activa y pertenencia actual).
test('un borrador fuera de la ficha activa no ofrece Enviar', async ({ page }) => {
  await login(page, 'aprendiz')

  // El test anterior pudo dejar a María fuera de la ficha: se reincorpora
  // (idempotente) para poder crear el borrador.
  const buscar = page.getByRole('button', { name: /Buscar ficha/i })
  const salir = page.getByRole('button', { name: /Salir de la ficha/i })
  await page.goto('/aprendiz/ficha')
  await expect(salir.or(buscar).first()).toBeVisible({ timeout: 15000 })
  if (await buscar.isVisible().catch(() => false)) {
    await page.getByLabel(/Código de la ficha/i).fill('xkp-mqwr')
    await buscar.click()
    await page.getByRole('button', { name: /Unirme a esta ficha/i }).click()
    await expect(page.getByText(/Integrantes de la ficha/i)).toBeVisible({ timeout: 15000 })
  }

  // Crear un borrador dentro de la ficha actual.
  await page.goto('/aprendiz/propuestas?crear=1')
  const titulo = `Borrador ficha anterior ${Date.now()}`
  await page.getByPlaceholder(/Sistema de monitoreo ambiental/i).fill(titulo)
  await page.getByRole('button', { name: /Guardar borrador/i }).click()
  await expect(page.getByText(titulo)).toBeVisible({ timeout: 15000 })

  // Salir de la ficha (idempotente frente a reintentos).
  await page.goto('/aprendiz/ficha')
  await expect(salir.or(buscar).first()).toBeVisible({ timeout: 15000 })
  if (await salir.isVisible().catch(() => false)) {
    await salir.click()
    await page.getByRole('button', { name: /Sí, salir/i }).click()
    await expect(buscar).toBeVisible({ timeout: 15000 })
  }

  // En la lista, ese borrador ya no ofrece Enviar.
  await page.goto('/aprendiz/propuestas')
  const card = page.locator('article', { hasText: titulo }).first()
  await expect(card).toBeVisible({ timeout: 15000 })
  await expect(card.getByRole('button', { name: /^Enviar$/ })).toHaveCount(0)
})
