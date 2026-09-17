import { chromium } from 'playwright'

const base = process.argv[2] || 'http://localhost:4173'
const rol = process.argv[3] || 'admin'

const cuentas = {
  admin: { email: 'admin@sena.edu.co', pass: 'admin123', home: '/admin/dashboard', perfil: '/admin/perfil' },
  instructor: { email: 'carlos.ruiz@sena.edu.co', pass: '123456', home: '/instructor/dashboard', perfil: '/instructor/perfil' },
}

const c = cuentas[rol]
const nav = []
const browser = await chromium.launch()
const ctx = await browser.newContext()
const page = await ctx.newPage()

page.on('request', (r) => {
  if (r.url().includes('/v1/')) nav.push(`${r.method().padEnd(6)} ${r.url().replace(base, '')}`)
})
page.on('response', (r) => {
  if (r.url().includes('/v1/') && r.status() >= 400) nav.push(`   >>> ${r.status()} ${r.url().replace(base, '')}`)
})

await page.goto(base + '/login')
await page.getByPlaceholder('tu.correo@ejemplo.com').fill(c.email)
await page.locator('input[type="password"]').fill(c.pass)
await page.getByRole('button', { name: /Iniciar Sesi/i }).click()
await page.waitForURL(`**${c.home}`, { timeout: 20000 })
await page.waitForTimeout(1500)
nav.length = 0

await page.goto(base + c.perfil)
await page.waitForURL(`**${c.perfil}`, { timeout: 20000 })
await page.waitForTimeout(4000)

console.log(`\n[${rol} perfil] peticiones: ${nav.length}`)
nav.forEach((x) => console.log('   ' + x))

await browser.close()
