import http from 'node:http'
import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const ROOT = path.resolve(HERE, '..', 'public', 'manual')
const FRONTEND = path.resolve(HERE, '..')
const require = createRequire(import.meta.url)
const { chromium } = require(path.join(FRONTEND, 'node_modules', 'playwright'))
const PORT = 4175
const PDF = path.join(ROOT, 'Manual_Frontend_ProyecTwin.pdf')

const MIME = {
  '.html': 'text/html; charset=utf-8', '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8', '.json': 'application/json; charset=utf-8',
  '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg', '.png': 'image/png', '.pdf': 'application/pdf'
}

const srv = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url, `http://127.0.0.1:${PORT}`)
    const clean = decodeURIComponent(url.pathname === '/' ? '/index.html' : url.pathname)
    const file = path.resolve(ROOT, '.' + clean)
    if (!file.startsWith(ROOT)) throw new Error('Fuera del manual')
    const data = await fs.readFile(file)
    res.writeHead(200, { 'Content-Type': MIME[path.extname(file)] || 'application/octet-stream' })
    res.end(data)
  } catch {
    res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' })
    res.end('No encontrado')
  }
})

await new Promise((resolve) => srv.listen(PORT, '127.0.0.1', resolve))
let browser
try {
  browser = await chromium.launch()
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, locale: 'es-CO' })
  await page.goto(`http://127.0.0.1:${PORT}/index.html`, { waitUntil: 'networkidle' })
  await page.waitForFunction(() => window.MANUAL_CHAPTERS && document.querySelectorAll('#chapters .chapter').length > 0)
  await page.evaluate(() => {
    document.documentElement.dataset.theme = 'light'
    document.getElementById('search').value = ''
    document.querySelector('[data-role="todos"]').click()
    window.scrollTo(0, 0)
  })
  await page.waitForFunction(() => Array.from(document.images).every((img) => img.complete), null, { timeout: 30000 })
  await page.evaluate(() => document.fonts.ready)
  await page.waitForTimeout(500)

  await page.pdf({
    path: PDF,
    format: 'A4',
    printBackground: true,
    preferCSSPageSize: true,
    displayHeaderFooter: true,
    headerTemplate: '<div style="width:100%;font-family:Segoe UI,Arial,sans-serif;font-size:7px;color:#66705f;padding:0 14mm 2mm;display:flex;justify-content:space-between;border-bottom:1px solid #dbe6d2;"><span>PROYECTWIN · MANUAL DEL FRONTEND</span><span>PROYECTWIN-MU-FE-001 · V1.0</span></div>',
    footerTemplate: '<div style="width:100%;font-family:Segoe UI,Arial,sans-serif;font-size:7px;color:#66705f;padding:2mm 14mm 0;display:flex;justify-content:space-between;border-top:1px solid #dbe6d2;"><span>ProyecTWIN · SENA · Noviembre 2026</span><span>Página <span class="pageNumber"></span> de <span class="totalPages"></span></span></div>',
    margin: { top: '0', bottom: '0', left: '0', right: '0' }
  })

  const stat = await fs.stat(PDF)
  console.log(JSON.stringify({ ok: true, pdf: PDF, bytes: stat.size }, null, 2))
} finally {
  if (browser) await browser.close()
  await new Promise((resolve) => srv.close(resolve))
}
