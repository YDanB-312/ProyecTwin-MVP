// Reseed de la base desechable de Playwright. Solo se ejecuta con autorización
// explícita (E2E_ALLOW_RESET=1) y el comando valida APP_ENV=e2e, base
// proyectwin_e2e, loopback, ausencia de DATABASE_URL y lock exclusivo.
import { spawnSync } from 'node:child_process'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

export default async function globalSetup() {
  if (process.env.E2E_ALLOW_RESET !== '1') {
    throw new Error(
      '[e2e] ABORTADO: falta autorización explícita para reiniciar la base desechable.\n' +
        '  Define E2E_ALLOW_RESET=1 en el proceso antes de ejecutar Playwright.'
    )
  }

  const backend = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..', 'backend')

  const resultado = spawnSync('php', ['artisan', 'e2e:reset'], {
    cwd: backend,
    stdio: 'pipe',
    shell: true,
    timeout: 180000,
    env: {
      ...process.env,
      APP_ENV: 'e2e',
      DB_CONNECTION: 'mysql',
      DB_HOST: '127.0.0.1',
      DB_PORT: '3306',
      DB_DATABASE: 'proyectwin_e2e',
      E2E_ALLOW_RESET: '1',
      BCRYPT_ROUNDS: '4',
      CACHE_DRIVER: 'file',
      SESSION_DRIVER: 'file',
      QUEUE_CONNECTION: 'sync',
      MAIL_MAILER: 'log',
    },
  })

  if (resultado.status !== 0) {
    const detalle = String(resultado.stderr || resultado.stdout || resultado.error?.message || '').slice(0, 800)
    throw new Error('[e2e] ABORTADO: el reseed de proyectwin_e2e falló o fue rechazado por las guardas.\n' + detalle)
  }

  console.log('[e2e] proyectwin_e2e resembrada para la corrida actual.')
}
