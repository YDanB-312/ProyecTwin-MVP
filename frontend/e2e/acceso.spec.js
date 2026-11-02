import { test, expect, login, logout } from './helpers'

test.describe('Acceso y sesión', () => {
  test('inicia y cierra sesión como aprendiz', async ({ page }) => {
    await login(page, 'aprendiz')
    await expect(page).toHaveURL(/\/aprendiz\/dashboard/)
    await expect(page.getByText(/Hola,/i).first()).toBeVisible()
    await expect(page.getByRole('button', { name: /Cerrar sesi/i })).toBeVisible()

    await logout(page)
    await expect(page).toHaveURL(/\/login$/)
  })

  test('rechaza credenciales inválidas sin iniciar sesión', async ({ page }) => {
    await page.goto('/login')
    await page.getByPlaceholder(/Correo electr/i).fill('maria.gonzalez@example.com')
    await page.locator('input[type="password"]').fill('clave-incorrecta')
    await page.getByRole('button', { name: /Iniciar Sesi/i }).click()

    await expect(page.locator('[role="alert"]')).toBeVisible()
    await expect(page).toHaveURL(/\/login$/)
  })

  test('protege las rutas privadas sin sesión', async ({ page }) => {
    await page.goto('/admin/usuarios')
    await page.waitForURL('**/login')
    await expect(page.getByPlaceholder(/Correo electr/i)).toBeVisible()
  })

  test('la landing pública muestra la demo del motor sin autenticarse', async ({ page }) => {
    const noAutorizado = []
    page.on('response', (r) => {
      if (r.status() === 401 && !r.url().includes('/auth/me')) noAutorizado.push(r.url())
    })

    await page.goto('/')
    await expect(page.getByText(/SIMILITUD/i).first()).toBeVisible({ timeout: 15000 })
    await expect(page.locator('#demo-idea')).toBeVisible()

    await page.locator('#demo-idea').fill('plataforma de inventarios para tienda con control de stock')
    await expect(page.locator('[class*="termMatch"]').first()).toBeVisible({ timeout: 15000 })
    expect(noAutorizado).toHaveLength(0)
  })
})
