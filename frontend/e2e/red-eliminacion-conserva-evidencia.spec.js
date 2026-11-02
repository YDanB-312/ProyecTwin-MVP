import { test, expect, login, apiLogin } from './helpers'

async function confirmarRed(page) {
  const dialog = page.getByRole('alertdialog')
  await expect(dialog).toBeVisible()

  await dialog.getByRole('checkbox').check()
  await dialog.getByRole('textbox').fill('ELIMINAR')

  const confirmar = dialog.getByRole('button', { name: /Sí, eliminar/i })
  await expect(confirmar).toBeEnabled()
  await confirmar.click()
}

test.describe('Admin: red con evidencia', () => {
  test('elimina la red sin 409 y conserva la propuesta y su similitud', async ({ page, request }) => {
    const token = await apiLogin(request, 'admin')
    const headers = { Authorization: `Bearer ${token}` }
    const sufijo = String(Date.now())

    const redResp = await request.post('/v1/knowledge-networks', {
      headers,
      data: { nombre: `Red evidencia ${sufijo}` },
    })
    expect(redResp.status()).toBe(201)
    const red = await redResp.json()

    const programaResp = await request.post('/v1/training-programs', {
      headers,
      data: {
        nombre: `Programa evidencia ${sufijo}`,
        nivel: 'Tecnólogo',
        knowledge_network_id: red.id,
      },
    })
    expect(programaResp.status()).toBe(201)
    const programa = await programaResp.json()

    const fichaResp = await request.post('/v1/class-groups', {
      headers,
      data: {
        numero: sufijo,
        nombre: `Ficha evidencia ${sufijo}`,
        estado: 'activo',
        id_programa: programa.id,
        id_instructor: 1,
      },
    })
    expect(fichaResp.status()).toBe(201)
    const ficha = await fichaResp.json()

    const proyectoResp = await request.post('/v1/projects', {
      headers,
      data: {
        titulo: `Propuesta evidencia ${sufijo}`,
        id_creador: 1,
        id_class_group: ficha.id,
      },
    })
    expect(proyectoResp.status()).toBe(201)
    const proyecto = await proyectoResp.json()

    // Evidencia compartida contra una propuesta aprobada del seed.
    const similitudResp = await request.post('/v1/similarities', {
      headers,
      data: {
        porcentaje: 90,
        id_proyecto_1: proyecto.id,
        id_proyecto_2: 5,
      },
    })
    expect(similitudResp.status()).toBe(201)
    const similitud = await similitudResp.json()

    await login(page, 'admin')
    await page.goto('/admin/redes-conocimiento')

    const fila = page.locator('tr', { hasText: red.nombre }).first()
    await fila.getByRole('button', { name: /Eliminar/i }).click()
    await confirmarRed(page)

    await expect(page.getByText('Red eliminada correctamente.')).toBeVisible({ timeout: 15000 })
    await expect(page.getByText(red.nombre)).toHaveCount(0)

    // La red y su estructura desaparecen; la propuesta y la similitud se conservan.
    const proyectoFinalResp = await request.get(`/v1/projects/${proyecto.id}`, { headers })
    expect(proyectoFinalResp.ok()).toBeTruthy()
    const proyectoFinal = await proyectoFinalResp.json()
    expect(proyectoFinal.id_class_group).toBeNull()

    const similitudFinalResp = await request.get(`/v1/similarities/${similitud.id}`, { headers })
    expect(similitudFinalResp.ok()).toBeTruthy()
  })
})
