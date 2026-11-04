import { createContext, useContext, useState, useCallback, useEffect, useMemo } from 'react'
import { RUTA_POR_ROL } from '../constants/routes'
import {
  apiLogin, apiLogout, apiMe, apiChangePassword,
  EVENTO_SESION_EXPIRADA, toFieldErrors,
} from '../lib/api'

// Sesión del usuario.
//
// Fuente única: la API. La autenticación vive en una cookie httpOnly que el
// navegador envía sola; aquí solo se guarda en memoria la versión mínima de la
// cuenta ({ id, correo, nombre, rol }) que pintan las vistas. Al arrancar se
// pregunta a /auth/me para rehidratar la sesión.

const AuthContext = createContext(null)

// Normaliza la cuenta de la API al shape mínimo de sesión.
function aSesion(u) {
  return {
    id: u.id,
    correo: u.correo,
    nombre: [u.nombre, u.apellido].filter(Boolean).join(' ').trim() || u.correo,
    rol: String(u.rol || '').toLowerCase(),
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  // Mientras se resuelve /auth/me no se puede decidir si hay sesión.
  const [cargando, setCargando] = useState(true)

  // ---------------------------------------------------------------- Login
  const login = useCallback(async (identificador, password, recordarme) => {
    try {
      const { user: cuenta } = await apiLogin(identificador.trim(), password, recordarme)
      const sesion = aSesion(cuenta)
      setUser(sesion)
      return {
        exito: true,
        ruta: RUTA_POR_ROL[sesion.rol] || '/',
      }
    } catch (err) {
      const mensaje = err?.data?.message
        || (err?.status === 0 ? 'No se pudo conectar con el servidor.' : 'Credenciales incorrectas. Verifica tus datos.')
      return { exito: false, mensaje, campos: toFieldErrors(err?.data) }
    }
  }, [])

  // ---------------------------------------------------------------- Registro
  // (El registro público no existe: las cuentas las crea el administrador.)

  // ---------------------------------------------------------------- Contraseñas
  const cambiarMiContrasena = useCallback(async (actual, nueva) => {
    if (!user?.id) return { exito: false, mensaje: 'Sesión no válida. Inicia sesión de nuevo.' }
    try {
      await apiChangePassword(actual, nueva)
      return { exito: true }
    } catch (err) {
      const campos = toFieldErrors(err?.data)
      return {
        exito: false,
        mensaje: campos.password_actual
          || campos.password
          || campos.password_confirmation
          || err?.data?.message
          || 'No se pudo actualizar la contraseña.',
        campos,
      }
    }
  }, [user])

  // ---------------------------------------------------------------- Logout
  const logout = useCallback(async () => {
    // Espera a que el backend cierre la sesión (cookie) antes de limpiar el
    // estado; si no, un refresco inmediato podría reautenticar al usuario.
    await apiLogout()
    setUser(null)
  }, [])

  // Refresca nombre/correo en memoria tras editar el perfil.
  const sincronizarSesion = useCallback((nombre, correo) => {
    setUser((actual) => {
      if (!actual) return actual
      return { ...actual, nombre: nombre || actual.nombre, correo: correo || actual.correo }
    })
  }, [])

  // Rehidratar/validar la sesión al arrancar (la cookie es httpOnly, así que la
  // única forma de saber quién es el usuario es preguntar a /auth/me).
  useEffect(() => {
    let vivo = true
    apiMe()
      .then((cuenta) => {
        if (vivo && cuenta?.id) setUser(aSesion(cuenta))
      })
      .catch(() => {
        if (vivo) setUser(null)
      })
      .finally(() => {
        if (vivo) setCargando(false)
      })
    return () => { vivo = false }
  }, [])

  // La API avisa (evento global) cuando una llamada autenticada recibe 401.
  useEffect(() => {
    const alExpirar = () => setUser(null)
    window.addEventListener(EVENTO_SESION_EXPIRADA, alExpirar)
    return () => window.removeEventListener(EVENTO_SESION_EXPIRADA, alExpirar)
  }, [])

  // El valor del contexto se crea una vez por cambio real de sesión. Sin esto,
  // cualquier render del provider entrega un objeto nuevo y vuelve a renderizar
  // a todos los consumidores.
  const value = useMemo(() => ({
    user,
    cargando,
    login,
    cambiarMiContrasena,
    sincronizarSesion,
    logout,
    isAuthenticated: !!user,
  }), [user, cargando, login, cambiarMiContrasena, sincronizarSesion, logout])

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components -- useAuth se exporta junto al provider para mantener un único contexto
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return context
}
