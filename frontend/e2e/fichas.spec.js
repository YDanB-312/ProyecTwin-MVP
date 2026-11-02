import { test, expect, login } from './helpers'

const RED_INFO = 'Informática, Diseño y Desarrollo de Software'

test.describe('Catálogos y fichas', () => {
  test('el admin crea una red con un programa', async ({ page }) => {
    await login(page, 'admin')
    await page.goto('/admin/redes-conocimiento')
    await page.getByRole('button', { name: /Nueva Red/i }).click()

    const sufijo = String(Date.now())
    const red = `Red E2E ${sufijo}`
    const programa = `Programa E2E ${sufijo}`

    await page.getByLabel(/Nombre de la red/i).fill(red)
    const nuevoPrograma = page.getByLabel('Nuevo programa')
    await nuevoPrograma.fill(programa)
    await nuevoPrograma.press('Enter')
    await expect(page.getByLabel('Nombre del programa 1')).toHaveValue(programa)

    await page.getByRole('button', { name: /Crear red/i }).click()
    await expect(page.getByText('Red creada correctamente.')).toBeVisible({ timeout: 15000 })
    await expect(page.getByText(red).first()).toBeVisible()
    await expect(page.getByText(programa).first()).toBeVisible()
  })

  test('el admin crea una ficha y abre su detalle', async ({ page }) => {
    await login(page, 'admin')
    await page.goto('/admin/fichas')
    await page.getByRole('button', { name: /Crear Ficha/i }).click()

    const sufijo = String(Date.now())
    const nombre = `Ficha Admin E2E ${sufijo}`
    const numero = sufijo.slice(-5)
    const formulario = page.locator('form').filter({
      has: page.getByRole('button', { name: /Crear ficha/i }),
    })

    await formulario.locator('select[name="red"]').selectOption({ label: RED_INFO })
    await formulario.locator('select[name="programa"]').selectOption({ label: 'ADSO' })
    await formulario.locator('select[name="instructorId"]').selectOption({ index: 1 })
    await formulario.locator('input[name="nombre"]').fill(nombre)
    await formulario.locator('input[name="numero"]').fill(numero)
    await formulario.getByRole('button', { name: /Crear ficha/i }).click()

    await expect(page.getByText('Ficha creada correctamente.')).toBeVisible({ timeout: 15000 })
    await page.getByPlaceholder(/Escribe código, nombre o número/i).fill(nombre)
    const fila = page.locator('tr', { hasText: nombre }).first()
    await expect(fila).toBeVisible({ timeout: 15000 })
    await fila.getByRole('link', { name: /Ver/i }).click()
    await page.waitForURL('**/admin/detalle-ficha/**')
    await expect(page.getByRole('heading', { name: new RegExp(nombre) })).toBeVisible()
  })

  test('el instructor crea una ficha con la cascada red/programa', async ({ page }) => {
    await login(page, 'instructor')
    await page.goto('/instructor/fichas?crear=1')

    const sufijo = String(Date.now())
    const nombre = `Ficha Instructor E2E ${sufijo}`
    const numero = sufijo.slice(-5)
    const formulario = page.locator('form').filter({
      has: page.getByRole('button', { name: /Crear ficha/i }),
    })

    await formulario.locator('select[name="red"]').selectOption({ label: RED_INFO })
    await formulario.locator('select[name="programaId"]').selectOption({ label: 'ADSO' })
    await formulario.locator('input[name="nombre"]').fill(nombre)
    await formulario.locator('input[name="numero"]').fill(numero)
    await formulario.getByRole('button', { name: /Crear ficha/i }).click()

    await expect(page.getByText('Ficha creada correctamente.')).toBeVisible({ timeout: 15000 })
    await expect(page.getByText(nombre).first()).toBeVisible()
    await expect(page.getByText(new RegExp(`N. ${numero}`)).first()).toBeVisible()
  })
})
