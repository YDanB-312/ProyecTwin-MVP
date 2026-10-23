// Guardas compartidas para TODAS las entradas E2E (sprint final, Paso 1).
// Una configuración insegura debe ABORTAR la ejecución, nunca degradarse a una
// advertencia. La verificación server-side definitiva vive en el comando
// `php artisan e2e:reset --env=e2e` (backend/app/Console/Commands/E2eReset.php).
export const API_E2E = 'http://127.0.0.1:8001'
export const API_DESARROLLO = 'http://127.0.0.1:8000'

/**
 * Valida el entorno E2E de forma estricta.
 *
 * No se normaliza ni se acepta una variante de la URL: el único destino válido
 * es la cadena exacta API_E2E. Así se rechazan localhost, puertos distintos,
 * credenciales, rutas, query strings y URLs remotas.
 */
export function problemasEntornoE2E(env = process.env) {
  const problemas = []

  if (env.E2E_ALLOW_RESET !== '1') {
    problemas.push('Falta la autorización explícita del reseed: define E2E_ALLOW_RESET=1')
  }

  if (!env.PROYECTWIN_API) {
    problemas.push(`Falta PROYECTWIN_API (debe ser exactamente ${API_E2E})`)
  } else if (env.PROYECTWIN_API !== API_E2E) {
    problemas.push(`PROYECTWIN_API no coincide con el destino E2E permitido; debe ser exactamente ${API_E2E}`)
  }

  return problemas
}

export function exigirEntornoE2E(nombre) {
  const problemas = problemasEntornoE2E()

  if (problemas.length === 0) return

  console.error(`[${nombre}] ABORTADO: entorno E2E inseguro; no se ejecutó ninguna operación.`)
  for (const problema of problemas) console.error(`  - ${problema}`)
  process.exit(2)
}
