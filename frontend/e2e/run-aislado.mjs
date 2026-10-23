// Corredor E2E con aislamiento por archivo: cada spec corre en su propia
// invocación de playwright, y el globalSetup resiembra la DB local antes de
// cada una. Mismo orden alfabético que `playwright test`.
//
// Los artefactos de cada spec se escriben en una raíz temporal o en la raíz
// que reciba del gate. Así este runner no limpia el `test-results` histórico.
// Uso: npm run test:e2e:aislado [-- <filtros playwright>]
import { spawnSync } from 'node:child_process'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { exigirEntornoE2E } from './guardas-e2e.mjs'

exigirEntornoE2E('aislado')

const here = path.dirname(fileURLToPath(import.meta.url))
const raiz = path.resolve(here, '..')
const backend = path.resolve(here, '..', '..', 'backend')
const artifactRoot = path.resolve(process.env.E2E_ARTIFACT_ROOT
  || path.join(os.tmpdir(), `proyectwin-e2e-aislado-${Date.now()}`))
fs.mkdirSync(artifactRoot, { recursive: true })
console.log(`[aislado] artefactos: ${artifactRoot}`)

// Restauración final del seed, igual que en los otros runners del gate
// completo (`test:full`): solo vía `php artisan e2e:reset --env=e2e` (guardas
// server-side: autorización E2E_ALLOW_RESET, APP_ENV=e2e, nombre de BD y lock).
function reseed(motivo) {
  console.log(`[aislado] reseed E2E (${motivo}).`)
  const r = spawnSync('php', ['artisan', 'e2e:reset', '--env=e2e'], {
    cwd: backend,
    stdio: 'inherit',
    shell: true,
    timeout: 180000,
  })
  if (r.status !== 0) {
    console.error('[aislado] ABORTADO: el reseed E2E falló o fue rechazado por las guardas.')
    process.exit(2)
  }
}

// Uso: node e2e/run-aislado.mjs [-- <args playwright>] | [filtro...]
const argv = process.argv.slice(2)
const sep = argv.indexOf('--')
const filtros = (sep === -1 ? argv : argv.slice(0, sep)).filter((a) => !a.startsWith('-'))
const extraArgs = sep === -1 ? [] : argv.slice(sep + 1)

let archivos = fs.readdirSync(here).filter((f) => f.endsWith('.spec.js')).sort()
if (filtros.length) archivos = archivos.filter((f) => filtros.some((x) => f.includes(x)))
if (!archivos.length) {
  console.log('[aislado] ningún archivo coincide con el filtro')
  process.exit(0)
}
console.log(`[aislado] ${archivos.length} archivos, reseed por archivo (vía globalSetup)`)

let ok = 0
let mal = 0
const fallidos = []
const t0 = Date.now()

for (const archivo of archivos) {
  const salida = path.join(artifactRoot, archivo.replace(/\.spec\.js$/i, ''))
  fs.mkdirSync(salida, { recursive: true })
  const r = spawnSync(
    'npx',
    ['playwright', 'test', `e2e/${archivo}`, '--reporter=line', `--output=${salida}`, ...extraArgs],
    {
      cwd: raiz,
      stdio: 'inherit',
      shell: true,
      env: { ...process.env, E2E_ARTIFACT_ROOT: artifactRoot },
    }
  )
  if (r.status === 0) {
    ok++
  } else {
    mal++
    fallidos.push(archivo)
  }
}

console.log('\n[aislado] restableciendo el seed E2E.')
reseed('restauración final')

const min = ((Date.now() - t0) / 60000).toFixed(1)
console.log(`[aislado] ${ok} archivos OK, ${mal} con fallos en ${min} min`)
if (fallidos.length) {
  console.log(`[aislado] fallidos: ${fallidos.join(', ')}`)
  process.exit(1)
}
