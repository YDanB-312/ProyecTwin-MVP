import { test, expect, login } from './helpers'

async function crearUsuario(page, { rol, nombre, apellido, correo, documento }) {
  await page.getByRole('button', { name: /Nuevo Usuario/i }).click()

  const formulario = page.locator('form').filter({
    has: page.getByRole('button', { name: /Crear usuario/i }),
  })

  await formulario.locator('input[name="nombre"]').fill(nombre)
  await formulario.locator('input[name="apellido"]').fill(apellido)
  await formulario.locator('select[name="tipoDocumento"]').selectOption('CC')
  await formulario.locator('input[name="numeroDocumento"]').fill(documento)
  await formulario.locator('input[name="correo"]').fill(correo)
  await formulario.locator('select[name="rol"]').selectOption(rol)
  await formulario.getByRole('button', { name: /Crear usuario/i }).click()

  await expect(page.getByText(/creado\./i).first()).toBeVisible({ timeout: 15000 })
}

async function buscarYVer(page, nombre, correo) {
  await page.getByPlaceholder(/Nombre, documento, usuario o correo/i).fill(correo)
  const fila = page.locator('tr', { hasText: nombre }).first()
  await expect(fila).toBeVisible({ timeout: 15000 })
  await fila.getByRole('link', { name: /Ver/i }).click()
  await page.waitForURL('**/admin/detalle-usuario/**')
  await expect(page.getByText(correo).first()).toBeVisible()
}

test.describe('Administración de usuarios', () => {
  test('el admin crea un instructor y un aprendiz y consulta ambos detalles', async ({ page }) => {
    await login(page, 'admin')
    await page.goto('/admin/usuarios')

    const base = Date.now()
    const instructor = {
      nombre: 'Instructor',
      apellido: `E2E ${base}`,
      correo: `instructor.e2e.${base}@example.com`,
      documento: String(base),
    }
    await crearUsuario(page, { rol: 'instructor', ...instructor })
    await buscarYVer(page, `${instructor.nombre} ${instructor.apellido}`, instructor.correo)

    await page.goto('/admin/usuarios')
    const aprendiz = {
      nombre: 'Aprendiz',
      apellido: `E2E ${base + 1}`,
      correo: `aprendiz.e2e.${base + 1}@example.com`,
      documento: String(base + 1),
    }
    await crearUsuario(page, { rol: 'aprendiz', ...aprendiz })
    await buscarYVer(page, `${aprendiz.nombre} ${aprendiz.apellido}`, aprendiz.correo)
  })
})
