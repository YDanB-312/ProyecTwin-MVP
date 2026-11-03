import process from 'node:process'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// La API Laravel se consume en el mismo origen a través de este proxy. Así se
// elimina el preflight CORS de cada petición autenticada y funciona igual en
// desarrollo (5173) y en preview/E2E (4173).
// Se usa 127.0.0.1 y no localhost: en Windows `localhost` puede resolver a ::1
// mientras `php artisan serve` solo escucha en IPv4.
const API = process.env.PROYECTWIN_API || 'http://127.0.0.1:8000'

const proxy = {
  '/v1': { target: API, changeOrigin: true },
  // Cookie CSRF de Sanctum (misma-origen para la sesión por cookie).
  '/sanctum': { target: API, changeOrigin: true },
}

// El manual de usuario vive en public/manual; Vite lo sirve en /manual/ y lo
// copia a dist/manual/ durante el build, sin plugins adicionales.
export default defineConfig({
  plugins: [react()],
  server: { proxy },
  preview: { proxy },
})
