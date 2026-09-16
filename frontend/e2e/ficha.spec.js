import { test, expect, login } from './helpers'

// La vinculación a una ficha la gestiona coordinación/instructor: ya no existe
// "unirse por código". Mi Ficha muestra la ficha asignada y sus integrantes.

const unico = () => `aprend.e2e.${Date.now()}@soy.sena.edu.co`

async function registrarAprendiz(page) {
  const email = unico()
  await page.goto('/register')
  await page.getByPlaceholder('María José').fill('Test E2E')
  await page.getByPlaceholder('González Ruiz').fill('Automatizado')
  await page.getByPlaceholder('tu.correo@ejemplo.com').fill(email)
  await page.locator('input[type="password"]').first().fill('clave123')
  await page.locator('input[type="password"]').nth(1).fill('clave123')
  await page.getByRole('button', { name: /Crear Cuenta/i }).click()
  await page.waitForURL('**/confirmacion')
  return email
}

test.describe('Mi Ficha', () => {
  test('aprendiz sin ficha ve el aviso de coordinación', async ({ page }) => {
    const email = await registrarAprendiz(page)
    await page.goto('/login')
    await page.getByPlaceholder('tu.correo@ejemplo.com').fill(email)
    await page.locator('input[type="password"]').fill('clave123')
    await page.getByRole('button', { name: /Iniciar Sesión/i }).click()
    await page.waitForURL('**/aprendiz/dashboard')

    await page.getByLabel(/Navegación principal/).getByRole('link', { name: 'Ficha' }).click()
    await expect(page).toHaveURL(/\/aprendiz\/ficha/)
    await expect(page.getByText(/Aún no tienes una ficha asignada/i)).toBeVisible()
    await expect(page.getByText(/gestiona coordinación/i).first()).toBeVisible()
  })

  test('aprendiz con ficha ve su ficha y sus compañeros', async ({ page }) => {
    await login(page, 'aprendiz')
    await page.getByLabel(/Navegación principal/).getByRole('link', { name: 'Ficha' }).click()
    await expect(page).toHaveURL(/\/aprendiz\/ficha/)

    // Datos de la ficha asignada (seed: María pertenece a xkp-mqwr)
    await expect(page.getByRole('heading', { name: /Analisis y Desarrollo 2568/ })).toBeVisible()
    await expect(page.getByText('xkp-mqwr').first()).toBeVisible()
    await expect(page.getByText(/Integrantes de la ficha/i)).toBeVisible()
  })
})
