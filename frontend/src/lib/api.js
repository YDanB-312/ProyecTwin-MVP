// Cliente HTTP de ProyecTwin.
//
// Única puerta de entrada a la API Laravel. Sin mocks, sin fusión de fuentes y
// sin estado en memoria: cada llamada va al servidor con fetch().
// El token de Sanctum se guarda en localStorage (o sessionStorage si el
// usuario no marcó "recordarme") y se envía como Bearer.

const BASE = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '')

const CLAVE_TOKEN = 'auth_token'

// Evento que avisa a la app de que la sesión dejó de ser válida (401).
export const EVENTO_SESION_EXPIRADA = 'auth:expirada'

// ---------------------------------------------------------------- Token

function getToken() {
  try {
    return localStorage.getItem(CLAVE_TOKEN) || sessionStorage.getItem(CLAVE_TOKEN)
  } catch {
    return null
  }
}

export function setAuthToken(token, remember = true) {
  const destino = remember ? localStorage : sessionStorage
  const otro = remember ? sessionStorage : localStorage
  try {
    if (token) destino.setItem(CLAVE_TOKEN, token)
    else { localStorage.removeItem(CLAVE_TOKEN); sessionStorage.removeItem(CLAVE_TOKEN) }
    otro.removeItem(CLAVE_TOKEN)
  } catch {
    /* almacenamiento no disponible */
  }
}

export function haySesion() {
  return !!getToken()
}

// ---------------------------------------------------------------- Fetch

// Construye un query string ignorando valores vacíos.
export function qs(params = {}) {
  const partes = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '' && v !== 'todos')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
  return partes.length ? `?${partes.join('&')}` : ''
}

// Petición base. Valida `response.ok` (fetch resuelve incluso en 4xx/5xx) y
// lanza un Error enriquecido con `status` y `data` para que la UI decida.
export async function apiFetch(path, { method = 'GET', body, auth = true, timeout = 15000 } = {}) {
  if (!BASE) throw new Error('VITE_API_URL no configurado')
  const url = `${BASE}${path.startsWith('/') ? '' : '/'}${path}`

  const headers = { 'Content-Type': 'application/json', Accept: 'application/json' }
  const token = auth ? getToken() : null
  if (token) headers.Authorization = `Bearer ${token}`

  const ctrl = new AbortController()
  const timer = setTimeout(() => ctrl.abort(), timeout)
  try {
    const res = await fetch(url, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
      signal: ctrl.signal,
    })
    const text = await res.text()
    let data = null
    try { data = text ? JSON.parse(text) : null } catch { data = text }

    if (!res.ok) {
      // 401 en una llamada autenticada = token inválido o vencido. Se descarta
      // la sesión local y se avisa a la app (AuthContext redirige a /login).
      if (res.status === 401 && auth) {
        setAuthToken(null)
        try { window.dispatchEvent(new Event(EVENTO_SESION_EXPIRADA)) } catch { /* SSR/entornos sin window */ }
      }
      const err = new Error(data?.message || `Error ${res.status}`)
      err.status = res.status
      err.data = data
      throw err
    }
    return data
  } catch (err) {
    if (err?.name === 'AbortError') {
      const e = new Error('El servidor tardó demasiado en responder.')
      e.status = 0
      throw e
    }
    throw err
  } finally {
    clearTimeout(timer)
  }
}

// ---------------------------------------------------------------- Sesión

export async function apiLogin(correo, password, remember = true) {
  const data = await apiFetch('/auth/login', { method: 'POST', body: { correo, password }, auth: false })
  if (data.token) setAuthToken(data.token, remember)
  return data // { user, token, rol }
}

export function apiLogout() {
  return apiFetch('/auth/logout', { method: 'POST' })
    .catch(() => null)
    .finally(() => setAuthToken(null))
}

export function apiMe() {
  return apiFetch('/auth/me')
}

export function apiPasswordReset(correo, password) {
  return apiFetch('/auth/password-reset', { method: 'POST', body: { correo, password }, auth: false })
}

// ---------------------------------------------------------------- Utilidades

// Errores 422 de Laravel → { campo: mensaje } para los formularios.
export function toFieldErrors(payload) {
  if (!payload?.errors) return {}
  const out = {}
  for (const [k, v] of Object.entries(payload.errors)) out[k] = Array.isArray(v) ? v[0] : v
  return out
}
