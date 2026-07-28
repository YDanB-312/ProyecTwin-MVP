import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

const USUARIOS = [
  {
    id: 1,
    correo: 'maria.gonzalez@soy.sena.edu.co',
    password: '123456',
    nombre: 'María González',
    rol: 'aprendiz',
    ruta: '/aprendiz/dashboard'
  },
  {
    id: 2,
    correo: 'carlos.ruiz@sena.edu.co',
    password: '123456',
    nombre: 'Carlos Ruiz',
    rol: 'instructor',
    ruta: '/instructor/dashboard'
  },
  {
    id: 3,
    correo: 'admin@sena.edu.co',
    password: 'admin123',
    nombre: 'Administrador',
    rol: 'admin',
    ruta: '/admin/dashboard'
  }
]

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const guardado = sessionStorage.getItem('auth_user')
    return guardado ? JSON.parse(guardado) : null
  })

  const login = useCallback((correo, password) => {
    const encontrado = USUARIOS.find(
      u => u.correo === correo && u.password === password
    )
    if (!encontrado) return { exito: false, mensaje: 'Credenciales inválidas. Verifica tu correo y contraseña.' }

    const sesion = { id: encontrado.id, correo: encontrado.correo, nombre: encontrado.nombre, rol: encontrado.rol }
    sessionStorage.setItem('auth_user', JSON.stringify(sesion))
    setUser(sesion)
    return { exito: true, ruta: encontrado.ruta }
  }, [])

  const logout = useCallback(() => {
    sessionStorage.removeItem('auth_user')
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return context
}
