import { test, expect, login, crearUsuarioApi } from './helpers'

const ADMIN = { username: 'a', password: 'admin123' }
const auth = (token) => ({ Authorization: `Bearer ${token}` })

async function tokenAdmin(request) {
  const acceso = await request.post('/v1/auth/login', { data: ADMIN })
  expect(acceso.ok()).toBeTruthy()
  return (await acceso.json()).token
}

async function crearAprendiz(request, sufijo) {
  const marca = `${Date.now()}-${sufijo}`
  return crearUsuarioApi(request, {
    nombre: 'QA',
    apellido: sufijo,
    correo: `qa.${marca}@correo.com`,
    password: 'clave123',
    rol: 'aprendiz',
  })
}

async function asociarAFicha(request, token, userId, fichaId = 1) {
  const respuesta = await request.post(`/v1/class-groups/${fichaId}/aprendices`, {
    headers: auth(token),
    data: { usuario_id: userId },
  })
  expect(respuesta.ok()).toBeTruthy()
}

async function crearProyecto(request, token, userId, fichaId, titulo) {
  const respuesta = await request.post('/v1/projects', {
    headers: auth(token),
    data: {
      titulo,
      resumen: 'Propuesta temporal para validar el flujo D-01 en E2E.',
      area_aplicacion: 'Tecnología',
      objetivo_general: 'Validar la eliminación con consentimiento.',
      objetivos_especificos: ['Conservar o eliminar según la decisión.'],
      palabras_clave: 'qa, d01',
      id_creador: userId,
      id_class_group: fichaId,
    },
  })
  expect(respuesta.status()).toBe(201)
  return (await respuesta.json()).id
}

// Restaura la cuenta semilla usada para la prueba de desactivación, incluso
// si una aserción falla antes de reactivarla en la interfaz.
test.afterEach(async ({ request }) => {
  const token = await tokenAdmin(request)
  await request.put('/v1/general-users/1', {
    headers: auth(token),
    data: { estado: true },
  })
})

test.describe('Admin: eliminar usuarios', () => {
  test('un usuario sin historial se elimina con confirmación', async ({ page, request }) => {
    const aprendiz = await crearAprendiz(request, 'SinHistorial')
    const token = await tokenAdmin(request)
    const headers = auth(token)

    await login(page, 'admin')
    await page.goto(`/admin/detalle-usuario/${aprendiz.id}`)
    await page.getByRole('button', { name: /^Eliminar$/i }).click()

    const dialog = page.getByRole('alertdialog')
    await expect(dialog.getByRole('button', { name: /Sí, eliminar/i })).toBeDisabled()
    await dialog.getByRole('checkbox').check()
    await dialog.getByRole('textbox').fill('ELIMINAR')
    await dialog.getByRole('button', { name: /Sí, eliminar/i }).click()

    await page.waitForURL('**/admin/usuarios')
    const consulta = await request.get(`/v1/general-users/${aprendiz.id}`, { headers })
    expect(consulta.status()).toBe(404)
  })

  test('un instructor con fichas a cargo exige confirmación reforzada', async ({ page }) => {
    await login(page, 'admin')
    await page.goto('/admin/detalle-usuario/8')

    await page.getByRole('button', { name: /^Eliminar$/i }).click()
    const dialog = page.getByRole('alertdialog')
    await expect(dialog.getByRole('button', { name: /Sí, eliminar/i })).toBeDisabled()
    await dialog.getByRole('button', { name: /Cancelar/i }).click()
  })

  test('desactivar conserva propuestas y autoría, y reactivar restaura el acceso', async ({ page, request }) => {
    const token = await tokenAdmin(request)
    const headers = auth(token)

    await login(page, 'admin')
    await page.goto('/admin/detalle-usuario/1')
    await page.getByRole('button', { name: /^Eliminar$/i }).click()

    let dialog = page.getByRole('alertdialog')
    await dialog.getByRole('checkbox').check()
    await dialog.getByRole('textbox').fill('ELIMINAR')
    await dialog.getByRole('button', { name: /Sí, eliminar/i }).click()

    await expect(page.getByText(/Usuario con historial/i)).toBeVisible()
    await expect(page.getByRole('alertdialog').getByText(/3 propuesta/i)).toBeVisible()
    await page.getByRole('button', { name: /^Desactivar cuenta$/i }).click()
    await expect(page.getByRole('button', { name: /^Activar cuenta$/i })).toBeVisible()
    await expect(page.getByText('Plataforma de Ventas Online').first()).toBeVisible()

    const usuario = await request.get('/v1/general-users/1', { headers })
    expect((await usuario.json()).estado).toBe(false)
    const proyecto = await request.get('/v1/projects/4', { headers })
    expect((await proyecto.json()).id_creador).toBe(1)

    await page.getByRole('button', { name: /^Activar cuenta$/i }).click()
    await expect(page.getByRole('button', { name: /^Suspender$/i })).toBeVisible()
  })

  test('eliminación definitiva exige nueva confirmación si cambia el impacto', async ({ page, request }) => {
    const aprendiz = await crearAprendiz(request, 'Definitivo')
    const token = await tokenAdmin(request)
    const headers = auth(token)
    const marca = Date.now()

    await asociarAFicha(request, token, aprendiz.id)
    const proyecto1 = await crearProyecto(
      request,
      token,
      aprendiz.id,
      1,
      `E2E D01 impacto ${marca}`
    )

    await login(page, 'admin')
    await page.goto(`/admin/detalle-usuario/${aprendiz.id}`)
    await page.getByRole('button', { name: /^Eliminar$/i }).click()

    let dialog = page.getByRole('alertdialog')
    await dialog.getByRole('checkbox').check()
    await dialog.getByRole('textbox').fill('ELIMINAR')
    await dialog.getByRole('button', { name: /Sí, eliminar/i }).click()
    await expect(page.getByText(/Usuario con historial/i)).toBeVisible()
    await expect(page.getByRole('alertdialog').getByText(/1 propuesta/i)).toBeVisible()

    const proyecto2 = await crearProyecto(
      request,
      token,
      aprendiz.id,
      1,
      `E2E D01 impacto adicional ${marca}`
    )

    await page.getByRole('alertdialog').getByRole('button', { name: /Eliminar definitivamente/i }).click()
    await page.getByRole('button', { name: /^Eliminar definitivamente$/i }).waitFor()

    dialog = page.getByRole('alertdialog')
    await dialog.getByRole('checkbox').check()
    await dialog.getByRole('textbox').fill('ELIMINAR DEFINITIVAMENTE')
    await dialog.getByRole('button', { name: /^Eliminar definitivamente$/i }).click()

    await expect(page.getByText(/El impacto del borrado cambió/i)).toBeVisible()
    await expect(page.getByRole('alertdialog').getByText(/2 propuesta/i)).toBeVisible()

    await page.getByRole('alertdialog').getByRole('button', { name: /Eliminar definitivamente/i }).click()
    await page.getByRole('button', { name: /^Eliminar definitivamente$/i }).waitFor()

    dialog = page.getByRole('alertdialog')
    await dialog.getByRole('checkbox').check()
    await dialog.getByRole('textbox').fill('ELIMINAR DEFINITIVAMENTE')
    await dialog.getByRole('button', { name: /^Eliminar definitivamente$/i }).click()
    await page.waitForURL('**/admin/usuarios')

    const usuario = await request.get(`/v1/general-users/${aprendiz.id}`, { headers })
    expect(usuario.status()).toBe(404)

    // El listado admin es el contrato público de las propuestas; el detalle
    // individual aplica reglas de visibilidad y no se usa para esta cascada.
    const listado = await request.get('/v1/projects', { headers })
    expect(listado.ok()).toBeTruthy()
    const ids = (await listado.json()).map((p) => Number(p.id))
    expect(ids).not.toContain(Number(proyecto1))
    expect(ids).not.toContain(Number(proyecto2))
  })

  test('editar a un correo existente muestra error', async ({ page }) => {
    await login(page, 'admin')
    await page.goto('/admin/detalle-usuario/9')
    await page.getByRole('button', { name: /^Editar$/i }).click()
    await page.getByLabel('Correo electrónico').fill('maria.gonzalez@soy.sena.edu.co')
    await page.getByRole('button', { name: /Guardar cambios/i }).click()
    await expect(page.getByText(/Ya existe un usuario/i)).toBeVisible()
  })
})
