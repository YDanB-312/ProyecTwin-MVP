import { test, expect, login, CUENTAS } from './helpers'

// BUG-019: las fechas crudas de la API no deben mostrarse en las vistas.
// La bitacora usa fechaHoraDesdeApi (dd/mm/aaaa, hh:mm a. m.).
// Las vistas de fecha usan fechaDesdeApi (d mmm aaaa).
// Esta prueba protege que el ISO crudo no vuelva a las vistas.
const HUMANA = /^\d{1,2} (ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic) \d{4}$/
const FECHA_HORA = /^\d{1,2}\/\d{1,2}\/\d{4}, \d{1,2}:\d{2} [ap]\.\s*[mM]\.$/
const ISO = /\d{4}-\d{2}-\d{2}/

// Mismo orden que MESES_CORTOS en src/utils/helpers.js (para el esperado exacto).
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

// Convierte la fecha de la API (date-only o ISO con hora) al texto esperado en
// la UI, con el mismo criterio que fechaDesdeApi: toma sus tres primeros
// componentes YYYY-MM-DD sin convertir de zona (para created_at, el día UTC).
function fechaHumana(iso) {
  const [a, m, d] = String(iso).slice(0, 10).split('-')
  return `${Number(d)} ${MESES[Number(m) - 1]} ${a}`
}

test.describe('Formato de fechas: humano y sin ISO crudo', () => {
  test('la bitácora admin formatea created_at (con hora) y nunca muestra ISO', async ({ page, request }) => {
    // La API es la fuente de verdad del día exacto: created_at llega como ISO
    // con hora y fechaDesdeApi usa su parte YYYY-MM-DD (día UTC del registro).
    // La bitácora ordena por id desc, así que la primera fila corresponde al
    // primer elemento de GET /v1/audit-logs.
    const acceso = await request.post('/v1/auth/login', {
      data: { username: CUENTAS.admin.username, password: CUENTAS.admin.password },
    })
    expect(acceso.ok()).toBeTruthy()
    const { token } = await acceso.json()

    const respuesta = await request.get('/v1/audit-logs', {
      headers: { Authorization: `Bearer ${token}` },
    })
    expect(respuesta.ok()).toBeTruthy()
    const cuerpo = await respuesta.json()
    const registros = Array.isArray(cuerpo) ? cuerpo : cuerpo?.data || []
    expect(registros.length).toBeGreaterThan(0)
    expect(registros[0].created_at).toBeTruthy()
    const esperadaFecha = new Date(registros[0].created_at).toLocaleDateString('es-CO', { day: '2-digit', month: '2-digit', year: 'numeric' })

    await login(page, 'admin')
    await page.goto('/admin/bitacora')

    // El seed registra auditoría de sobra (usuarios, fichas, propuestas…).
    const fechas = page.locator('td[data-label="Fecha y hora"]')
    await expect(fechas.first()).toBeVisible()

    // Día exacto (no solo formato): la primera fila es el último registro.
    expect((await fechas.first().innerText()).trim()).toContain(esperadaFecha)

    const total = await fechas.count()
    expect(total).toBeGreaterThan(0)
    for (let i = 0; i < total; i++) {
      const texto = (await fechas.nth(i).innerText()).trim()
      expect(texto, `fecha de la fila ${i}`).toMatch(FECHA_HORA)
      expect(texto, `fecha de la fila ${i}`).not.toMatch(ISO)
      expect(texto, `fecha de la fila ${i}`).not.toContain('T')
    }
  })

  test('las similitudes respetan el día local de una fecha date-only', async ({ page, request }) => {
    // La API es la fuente de verdad: de ahí sale el día que debe verse.
    const acceso = await request.post('/v1/auth/login', {
      data: { username: CUENTAS.admin.username, password: CUENTAS.admin.password },
    })
    expect(acceso.ok()).toBeTruthy()
    const { token } = await acceso.json()

    const respuesta = await request.get('/v1/similarities', {
      headers: { Authorization: `Bearer ${token}` },
    })
    expect(respuesta.ok()).toBeTruthy()
    const cuerpo = await respuesta.json()
    const similitudes = Array.isArray(cuerpo) ? cuerpo : cuerpo?.data || []
    const conFecha = similitudes.filter((s) => s.fecha)
    expect(conFecha.length).toBeGreaterThan(0)

    // El esperado se deriva de la API, no del reloj del navegador: si el
    // formateo interpretara "YYYY-MM-DD" como UTC, en zonas al oeste el día
    // se correría y el texto no estaría en este conjunto.
    const esperadas = new Set(conFecha.map((s) => fechaHumana(s.fecha)))

    await login(page, 'admin')
    await page.goto('/admin/similitudes')

    const fechas = page.locator('td[data-label="Fecha"]')
    await expect(fechas.first()).toBeVisible()

    const total = await fechas.count()
    expect(total).toBeGreaterThan(0)
    for (let i = 0; i < total; i++) {
      const texto = (await fechas.nth(i).innerText()).trim()
      expect(texto, `fecha de la fila ${i}`).toMatch(HUMANA)
      expect(texto, `fecha de la fila ${i}`).not.toMatch(ISO)
      expect(esperadas.has(texto), `"${texto}" no es una fecha sembrada del motor`).toBeTruthy()
    }
  })
})
