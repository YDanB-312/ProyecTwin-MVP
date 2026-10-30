// El nombre del archivo se conserva por compatibilidad histórica. Desde D-10,
// una red con propuestas ya no responde 409: conserva aprendices y propuestas,
// y desvincula las propuestas de la ficha eliminada.
import { test, expect, login, crearUsuarioApi } from './helpers'

const ADMIN = { username: 'a', password: 'admin123' }
const auth = (token) => ({ Authorization: `Bearer ${token}` })

async function tokenAdmin(request) {
  const acceso = await request.post('/v1/auth/login', { data: ADMIN })
  expect(acceso.ok()).toBeTruthy()
  return (await acceso.json()).token
}

async function crearEscenarioD10(request, token) {
  const marca = `${Date.now()}-${Math.floor(Math.random() * 1000)}`
  const headers = auth(token)

  const redResp = await request.post('/v1/knowledge-networks', {
    headers,
    data: { nombre: `Red E2E D10 ${marca}` },
  })
  expect(redResp.status()).toBe(201)
  const red = await redResp.json()

  const programaResp = await request.post('/v1/training-programs', {
    headers,
    data: {
      nombre: `Programa E2E D10 ${marca}`,
      nivel: 'Tecnologo',
      knowledge_network_id: red.id,
    },
  })
  expect(programaResp.status()).toBe(201)
  const programa = await programaResp.json()

  const aprendiz = await crearUsuarioApi(request, {
    nombre: 'QA',
    apellido: 'D10',
    correo: `qa.d10.${marca}@correo.com`,
    password: 'clave123',
    rol: 'aprendiz',
  })

  const fichaResp = await request.post('/v1/class-groups', {
    headers,
    data: {
      numero: String(Date.now()).slice(-9),
      nombre: `Ficha E2E D10 ${marca}`,
      estado: 'activo',
      id_programa: programa.id,
      id_instructor: 1,
      aprendices: [aprendiz.id],
    },
  })
  expect(fichaResp.status()).toBe(201)
  const ficha = await fichaResp.json()

  const proyectoResp = await request.post('/v1/projects', {
    headers,
    data: {
      titulo: `Propuesta E2E D10 ${marca}`,
      resumen: 'Propuesta temporal para verificar la conservación D-10.',
      area_aplicacion: 'Tecnología',
      objetivo_general: 'Verificar que la propuesta se conserva sin ficha.',
      objetivos_especificos: ['Conservar la propuesta al eliminar la red.'],
      palabras_clave: 'qa, d10',
      id_creador: aprendiz.id,
      id_class_group: ficha.id,
      id_instructor_asignado: 1,
    },
  })
  expect(proyectoResp.status()).toBe(201)
  const proyecto = await proyectoResp.json()

  return { red, programa, ficha, aprendiz, proyecto }
}

test('admin: eliminar una red con propuestas conserva aprendices y propuestas', async ({ page, request }) => {
  const token = await tokenAdmin(request)
  const headers = auth(token)
  const escenario = await crearEscenarioD10(request, token)
  const { red, programa, ficha, aprendiz, proyecto } = escenario

  await login(page, 'admin')
  await page.goto('/admin/redes-conocimiento')

  const fila = page.locator('tbody tr', { hasText: red.nombre })
  await expect(fila).toBeVisible()
  await expect(fila).toContainText(programa.nombre)

  await fila.getByRole('button', { name: /Eliminar/i }).click()
  const dialog = page.getByRole('alertdialog')
  await expect(dialog).toBeVisible()
  await expect(dialog.getByText('Eliminar red')).toBeVisible()
  await expect(dialog.getByText(/aprendices y las propuestas/i)).toBeVisible()

  const [respuesta] = await Promise.all([
    page.waitForResponse(
      (r) => r.url().includes(`/v1/knowledge-networks/${red.id}`) && r.request().method() === 'DELETE'
    ),
    (async () => {
      await dialog.getByRole('checkbox').check()
      await dialog.getByRole('textbox').fill('ELIMINAR')
      await dialog.getByRole('button', { name: /Sí, eliminar/i }).click()
    })(),
  ])
  expect(respuesta.status()).toBe(200)
  await expect(page.getByText(/Red eliminada correctamente/i)).toBeVisible()
  await expect(page.locator('tbody tr', { hasText: red.nombre })).toHaveCount(0)

  // La estructura desaparece, pero el aprendiz y su propuesta se conservan.
  const redGet = await request.get(`/v1/knowledge-networks/${red.id}`, { headers })
  const programaGet = await request.get(`/v1/training-programs/${programa.id}`, { headers })
  const fichaGet = await request.get(`/v1/class-groups/${ficha.id}`, { headers })
  expect(redGet.status()).toBe(404)
  expect(programaGet.status()).toBe(404)
  expect(fichaGet.status()).toBe(404)

  const usuarioGet = await request.get(`/v1/general-users/${aprendiz.id}?included=apprentice`, {
    headers,
  })
  expect(usuarioGet.status()).toBe(200)
  const usuario = await usuarioGet.json()
  expect(usuario.apprentice).toBeTruthy()
  expect(usuario.apprentice.id_class_group).toBeNull()
  expect(usuario.apprentice.id_programa).toBeNull()

  const proyectoGet = await request.get(`/v1/projects/${proyecto.id}`, { headers })
  expect(proyectoGet.status()).toBe(200)
  expect((await proyectoGet.json()).id_class_group).toBeNull()

  // La interfaz debe mostrar los datos conservados sin errores.
  await page.goto(`/admin/detalle-usuario/${aprendiz.id}`)
  await expect(page.getByText(/Sin ficha asignada/i).first()).toBeVisible()
  await expect(page.getByText(/No asignado/i).first()).toBeVisible()
  await expect(page.getByText(proyecto.titulo).first()).toBeVisible()

  // Limpieza autocontenida del escenario (no toca datos semilla).
  const inicial = await request.delete(`/v1/general-users/${aprendiz.id}`, { headers })
  expect(inicial.status()).toBe(409)
  const { confirmacion_hash } = await inicial.json()
  const definitivo = await request.delete(`/v1/general-users/${aprendiz.id}`, {
    headers,
    data: { confirmar_eliminacion: true, confirmacion_hash },
  })
  expect(definitivo.ok()).toBeTruthy()
})
