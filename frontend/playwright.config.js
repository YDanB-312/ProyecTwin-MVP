import { defineConfig } from '@playwright/test'

const API = 'http://127.0.0.1:8001'
const APP = 'http://localhost:4173'

// Configuración única E2E. Playwright usa `proyectwin_e2e`; PHPUnit usa
// `proyectwin_test`; la base local `proyectwin_mvp` nunca es objetivo de tests.
export default defineConfig({
  testDir: './e2e',
  globalSetup: './e2e/global-setup.js',
  timeout: 60000,
  expect: { timeout: 20000 },
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [['list']],
  outputDir: './test-results',
  use: {
    baseURL: APP,
    locale: 'es-CO',
    viewport: { width: 1280, height: 800 },
    actionTimeout: 20000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  webServer: [
    {
      command: 'php artisan serve --host=127.0.0.1 --port=8001',
      cwd: '../backend',
      url: `${API}/v1/app-version/android`,
      timeout: 120000,
      reuseExistingServer: false,
      env: {
        ...process.env,
        APP_ENV: 'e2e',
        DB_CONNECTION: 'mysql',
        DB_HOST: '127.0.0.1',
        DB_PORT: '3306',
        DB_DATABASE: 'proyectwin_e2e',
        CACHE_DRIVER: 'file',
        SESSION_DRIVER: 'file',
        QUEUE_CONNECTION: 'sync',
        BCRYPT_ROUNDS: '4',
        API_RATE_LIMIT: '1000',
        SANCTUM_STATEFUL_DOMAINS: 'localhost:4173,127.0.0.1:4173',
        FRONTEND_URL: APP,
        MAIL_MAILER: 'smtp',
        MAIL_HOST: '127.0.0.1',
        MAIL_PORT: '1025',
        MAIL_USERNAME: '',
        MAIL_PASSWORD: '',
        MAIL_ENCRYPTION: '',
        MAIL_FROM_ADDRESS: 'no-responder@proyecttwin.local',
      },
    },
    {
      command: 'npm run build && npm run preview -- --strictPort',
      cwd: '.',
      url: APP,
      timeout: 120000,
      reuseExistingServer: false,
      env: {
        ...process.env,
        PROYECTWIN_API: API,
      },
    },
  ],
})
