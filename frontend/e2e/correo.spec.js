import { test, expect, login } from './helpers'
import { limpiarMailpit, esperarEnlace } from './mailpit'

// Flujos reales de correo: Laravel envía por SMTP a Mailpit y el enlace se
// consume desde el mismo correo. No hay atajo local ni mensaje simulado.
test.describe('Correo local con Mailpit', () => {
  test('el admin crea una cuenta, llega la activación y el usuario inicia sesión', async ({ page, request }) => {
    test.setTimeout(120000)
    await limpiarMailpit(request)

    const sufijo = String(Date.now())
    const correo = `activacion.e2e.${sufijo}@example.com`
    const password = 'Activacion#2026'

    await login(page, 'admin')
    await page.goto('/admin/usuarios')
    await page.getByRole('button', { name: /Nuevo Usuario/i }).click()

    const formulario = page.locator('form').filter({
      has: page.getByRole('button', { name: /Crear usuario/i }),
    })

    await formulario.locator('input[name="nombre"]').fill('Activación')
    await formulario.locator('input[name="apellido"]').fill('Mailpit')
    await formulario.locator('select[name="tipoDocumento"]').selectOption('CC')
    await formulario.locator('input[name="numeroDocumento"]').fill(sufijo)
    await formulario.locator('input[name="correo"]').fill(correo)
    await formulario.locator('select[name="rol"]').selectOption('aprendiz')
    await formulario.getByRole('button', { name: /Crear usuario/i }).click()
    await expect(page.getByText(/creado\./i).first()).toBeVisible({ timeout: 15000 })

    const enlace = await esperarEnlace(request, {
      destinatario: correo,
      asunto: 'Activa tu cuenta de ProyecTwin',
      ruta: '/activar-cuenta#',
    })

    await page.goto(enlace)
    await expect(page.getByRole('heading', { name: /Activa tu cuenta|Tu cuenta/i })).toBeVisible({ timeout: 15000 })

    const campos = page.locator('input[type="password"]')
    await campos.nth(0).fill(password)
    await campos.nth(1).fill(password)
    await page.getByRole('button', { name: /Activar cuenta|Activar/i }).click()
    await expect(page.getByText(/Cuenta activada|contraseña quedó establecida/i).first()).toBeVisible({ timeout: 15000 })

    // Cerrar la sesión del admin para probar el login del usuario activado.
    await page.context().clearCookies()
    await page.goto('/login')
    await page.getByPlaceholder(/Correo electr/i).fill(correo)
    await page.locator('input[type="password"]').fill(password)
    await page.getByRole('button', { name: /Iniciar Sesi/i }).click()
    await page.waitForURL('**/aprendiz/dashboard', { timeout: 15000 })
  })

  test('la recuperación envía el enlace real y permite cambiar la contraseña', async ({ page, request }) => {
    test.setTimeout(120000)
    await limpiarMailpit(request)

    const correo = 'patricia.morales@example.com'
    const nueva = 'Recuperada#2026'

    await page.goto('/recuperar-contrasena')
    await page.getByPlaceholder(/Correo electr/i).fill(correo)
    await page.getByRole('button', { name: /Enviar enlace/i }).click()
    await expect(page.getByRole('heading', { name: /Revisa tu correo/i })).toBeVisible({ timeout: 15000 })

    const enlace = await esperarEnlace(request, {
      destinatario: correo,
      asunto: 'Restablece tu contraseña de ProyecTwin',
      ruta: '/restablecer-contrasena#',
    })

    await page.goto(enlace)
    const campos = page.locator('input[type="password"]')
    await campos.nth(0).fill(nueva)
    await campos.nth(1).fill(nueva)
    await page.getByRole('button', { name: /Restablecer|Guardar/i }).click()
    await expect(page.getByText(/Contraseña restablecida|contraseña actualizada/i).first()).toBeVisible({ timeout: 15000 })

    await page.goto('/login')
    await page.getByPlaceholder(/Correo electr/i).fill(correo)
    await page.locator('input[type="password"]').fill(nueva)
    await page.getByRole('button', { name: /Iniciar Sesi/i }).click()
    await page.waitForURL('**/aprendiz/dashboard', { timeout: 15000 })
  })
})
