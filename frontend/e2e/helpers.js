import { test, expect } from '@playwright/test'

// Credencial demo única para local/testing. El seeder la lee de
// config/demo.php (DEMO_PASSWORD opcional). No se usa en producción.
export const DEMO_PASSWORD = process.env.E2E_DEMO_PASSWORD || 'ProyecTwin.Demo#2026'

export const CUENTAS = {
  aprendiz: {
    correo: 'maria.gonzalez@example.com',
    username: 'mgonzalez',
    password: DEMO_PASSWORD,
    home: '/aprendiz/dashboard',
    nombre: 'Maria',
  },
  instructor: {
    correo: 'carlos.ruiz@example.com',
    username: 'cruiz',
    password: DEMO_PASSWORD,
    home: '/instructor/dashboard',
    nombre: 'Carlos',
  },
  admin: {
    correo: 'admin.proyectwin@example.com',
    username: 'a',
    password: DEMO_PASSWORD,
    home: '/admin/dashboard',
    nombre: 'Administrador',
  },
  otro: {
    correo: 'carlos.rodriguez@example.com',
    username: 'crodriguez',
    password: DEMO_PASSWORD,
    home: '/instructor/dashboard',
    nombre: 'Carlos Rodriguez',
  },
}

// Login con un identificador explícito (correo o username legacy).
export async function entrar(page, identificador, password, home) {
  await page.goto('/login')
  await page.getByPlaceholder(/Correo electr/i).fill(identificador)
  await page.locator('input[type="password"]').fill(password)
  await page.getByRole('button', { name: /Iniciar Sesi/i }).click()
  await page.waitForURL(`**${home}`, { timeout: 15000 })
}

export async function login(page, role) {
  const cuenta = CUENTAS[role]
  await entrar(page, cuenta.correo, cuenta.password, cuenta.home)
  return cuenta
}

export async function logout(page) {
  await page.getByRole('button', { name: /Cerrar sesi/i }).click()
  await page.waitForURL('**/login')
}

// Login por API útil para preparar datos aislados desde un test.
export async function apiLogin(request, role = 'admin') {
  const cuenta = CUENTAS[role]
  const respuesta = await request.post('/v1/auth/login', {
    data: { correo: cuenta.correo, password: cuenta.password },
  })
  expect(respuesta.ok()).toBeTruthy()
  return (await respuesta.json()).token
}

// El backend solo genera "sistema" internamente; la API rechaza ese tipo con
// 422. Las notificaciones preparadas por los tests usan un tipo manual válido.
export async function crearNotificacionApi(request, { titulo, idUsuario = 1, enlace = null, tipo = 'mensaje' }) {
  const token = await apiLogin(request, 'admin')
  const hoy = new Date()
  const fecha = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`

  const respuesta = await request.post('/v1/notifications', {
    headers: { Authorization: `Bearer ${token}` },
    data: { titulo, tipo, enlace, fecha, id_usuario: idUsuario },
  })
  expect(respuesta.ok()).toBeTruthy()

  return respuesta.json()
}

export { test, expect }
