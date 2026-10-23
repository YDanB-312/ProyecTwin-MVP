// Gate completo: corre todas las suites y NO se detiene en el primer fallo.
// Al final imprime el resumen y sale con código != 0 si algo falló.
//
// Cada etapa conserva su propio directorio de artefactos y un stage.log con su
// salida y código final. No escribe en frontend/test-results ni en
// frontend/agentes-reportes.
// Uso: npm run test:full
import { spawnSync } from 'node:child_process'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { exigirEntornoE2E } from './guardas-e2e.mjs'

exigirEntornoE2E('full')

const here = path.dirname(fileURLToPath(import.meta.url))
const raiz = path.resolve(here, '..')
const artifactRoot = path.resolve(process.env.E2E_ARTIFACT_ROOT
  || path.join(os.tmpdir(), `proyectwin-e2e-gate-${Date.now()}`))

// El gate no debe usar los directorios históricos como raíz de salida.
const protegidos = [
  path.resolve(raiz, 'test-results'),
  path.resolve(raiz, 'agentes-reportes'),
]
if (protegidos.some((p) => artifactRoot === p || artifactRoot.startsWith(p + path.sep))) {
  throw new Error(`La raíz de artefactos no puede estar dentro de ${protegidos.join(' ni ')}`)
}
fs.mkdirSync(artifactRoot, { recursive: true })
console.log(`[full] artefactos del gate: ${artifactRoot}`)

const ETAPAS = [
  {
    slug: '01-aislado',
    nombre: 'E2E determinista + navegación',
    cmd: ['node', ['e2e/run-aislado.mjs']],
  },
  {
    slug: '02-agentes',
    nombre: 'Agentes de acción + simulaciones',
    cmd: ['node', ['e2e/run-agentes-acciones.mjs']],
  },
  {
    slug: '03-cobertura',
    nombre: 'Cobertura de controles',
    cmd: ['npx', ['playwright', 'test', 'e2e/agentes/cobertura.spec.js', '--workers=1']],
    outputDirecto: true,
  },
  {
    slug: '04-negativos',
    nombre: 'Negativos',
    cmd: ['npx', ['playwright', 'test', 'e2e/agentes/negativos.spec.js', '--workers=1']],
    outputDirecto: true,
  },
  {
    slug: '05-volumen',
    nombre: 'Volumen de datos',
    cmd: ['node', ['e2e/run-volumen.mjs']],
  },
]

const resultados = []
const t0 = Date.now()

for (const etapa of ETAPAS) {
  console.log(`\n=========== ${etapa.nombre} ===========`)
  const stageDir = path.join(artifactRoot, etapa.slug)
  fs.mkdirSync(stageDir, { recursive: true })

  const [bin, argsBase] = etapa.cmd
  const args = [...argsBase]
  if (etapa.outputDirecto) {
    args.push(`--output=${path.join(stageDir, 'playwright')}`)
  }

  const r = spawnSync(bin, args, {
    cwd: raiz,
    stdio: 'pipe',
    encoding: 'utf8',
    shell: true,
    env: { ...process.env, E2E_ARTIFACT_ROOT: stageDir },
  })
  const salida = `${r.stdout || ''}${r.stderr || ''}${r.error ? String(r.error.message || r.error) : ''}`
  const log = `${salida}\n[e2e] exit=${r.status} signal=${r.signal ?? 'null'}\n`
  fs.writeFileSync(path.join(stageDir, 'stage.log'), log, 'utf8')
  if (salida) process.stdout.write(salida)
  resultados.push({ nombre: etapa.nombre, ok: r.status === 0 })
}

const min = ((Date.now() - t0) / 60000).toFixed(1)
console.log('\n=========== RESUMEN GATE COMPLETO ===========')
for (const r of resultados) console.log(`${r.ok ? 'OK  ' : 'FALLA'} - ${r.nombre}`)
const fallos = resultados.filter((r) => !r.ok)
console.log(`\n${resultados.length - fallos.length}/${resultados.length} etapas OK en ${min} min`)
if (fallos.length) {
  console.log(`Fallaron: ${fallos.map((f) => f.nombre).join(', ')}`)
  process.exit(1)
}
