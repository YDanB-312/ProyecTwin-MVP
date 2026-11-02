import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { CheckCircle, Key, Warning } from 'phosphor-react'
import AuthLayout from '../../../layouts/AuthLayout/AuthLayout'
import FormField from '../../../components/FormField/FormField'
import Button from '../../../components/Button/Button'
import { Input, PasswordInput } from '../../../components/Input/Input'
import { apiActivarCuenta, apiValidarActivacion, toFieldErrors } from '../../../lib/api'
import { esErrorEnlaceInvalido, mensajeErrorEnlace } from '../../../lib/errores'
import { esPasswordValida, MIN_PASSWORD_LENGTH } from '../../../utils/validation'
import { leerParametrosDeEnlace, urlSinToken } from '../../../utils/enlacesToken'
import s from './ActivarCuenta.module.css'

// Activación de una cuenta nueva: el token llega en el fragmento de la URL, se
// valida contra el backend y solo entonces se muestra el formulario.
export default function ActivarCuenta() {
  const location = useLocation()
  const navigate = useNavigate()
  const [token] = useState(() => leerParametrosDeEnlace(location).token)

  // Quita el token de la barra de direcciones (y del historial) en cuanto se
  // leyó: el fragmento nunca llegó al servidor, pero tampoco debe quedarse ahí.
  useEffect(() => {
    if (location.hash || location.search) {
      navigate(urlSinToken(location), { replace: true })
    }
  }, [location, navigate])

  const [estado, setEstado] = useState(token ? 'verificando' : 'invalido')
  const [datos, setDatos] = useState(null)
  const [password, setPassword] = useState('')
  const [confirmar, setConfirmar] = useState('')
  const [error, setError] = useState('')
  const [errorValidacion, setErrorValidacion] = useState('')
  const [errores, setErrores] = useState({})
  const [enviando, setEnviando] = useState(false)
  const [reintento, setReintento] = useState(0)

  useEffect(() => {
    let vivo = true

    if (!token) {
      return undefined
    }

    apiValidarActivacion(token)
      .then((respuesta) => {
        if (!vivo) return
        setDatos(respuesta)
        setEstado('formulario')
      })
      .catch((err) => {
        if (!vivo) return

        if (esErrorEnlaceInvalido(err)) {
          setEstado('invalido')
        } else {
          setErrorValidacion(mensajeErrorEnlace(err))
          setEstado('error')
        }
      })

    return () => { vivo = false }
  }, [token, reintento])

  async function handleSubmit(e) {
    e.preventDefault()
    if (enviando) return

    setError('')
    setErrores({})

    const errs = {}
    if (!password || !esPasswordValida(password)) {
      errs.password = `La contraseña debe tener al menos ${MIN_PASSWORD_LENGTH} caracteres.`
    }
    if (confirmar !== password) {
      errs.password_confirmation = 'Las contraseñas no coinciden.'
    }
    if (Object.keys(errs).length > 0) {
      setErrores(errs)
      return
    }

    setEnviando(true)
    try {
      await apiActivarCuenta({
        token,
        password,
        password_confirmation: confirmar,
      })
      setEstado('exito')
    } catch (err) {
      const campos = toFieldErrors(err?.data)
      setErrores(campos)
      if (campos.token) {
        setEstado('invalido')
      } else if (Object.keys(campos).length === 0) {
        setError(mensajeErrorEnlace(err))
      }
    } finally {
      setEnviando(false)
    }
  }

  if (estado === 'verificando') {
    return (
      <AuthLayout>
        <div className={s.wrapper}>
          <header className={s.header}>
            <h1 className={s.title}>Validando enlace…</h1>
            <p className={s.subtitle}>Estamos comprobando que el enlace de activación siga vigente.</p>
          </header>
        </div>
      </AuthLayout>
    )
  }

  if (estado === 'error') {
    return (
      <AuthLayout>
        <div className={s.wrapper}>
          <Warning size={48} weight="light" className={s.warningIcon} />
          <header className={s.header}>
            <h1 className={s.title}>No pudimos validar el enlace</h1>
            <p className={s.subtitle}>{errorValidacion}</p>
          </header>
          <div className={s.actions}>
            <Button
              type="button"
              onClick={() => {
                setErrorValidacion('')
                setEstado('verificando')
                setReintento((n) => n + 1)
              }}
            >
              Reintentar
            </Button>
            <Button as="link" to="/login" variant="secondary">Volver al login</Button>
          </div>
        </div>
      </AuthLayout>
    )
  }

  if (estado === 'invalido') {
    return (
      <AuthLayout>
        <div className={s.wrapper}>
          <Warning size={48} weight="light" className={s.warningIcon} />
          <header className={s.header}>
            <h1 className={s.title}>Enlace inválido o vencido</h1>
            <p className={s.subtitle}>
              Este enlace ya se usó o dejó de ser válido. Solicita al administrador un nuevo enlace de activación.
            </p>
          </header>
          <div className={s.actions}>
            <Button as="link" to="/login">Volver al login</Button>
            <Button as="link" to="/recuperar-contrasena" variant="secondary">
              Ir a recuperación
            </Button>
          </div>
        </div>
      </AuthLayout>
    )
  }

  if (estado === 'exito') {
    return (
      <AuthLayout>
        <div className={s.wrapper}>
          <CheckCircle size={48} weight="light" className={s.successIcon} />
          <header className={s.header}>
            <h1 className={s.title}>Cuenta activada</h1>
            <p className={s.subtitle}>
              Tu contraseña quedó establecida. Ya puedes iniciar sesión con tu correo electrónico.
            </p>
          </header>
          <div className={s.actions}>
            <Button as="link" to="/login">Ir al login</Button>
          </div>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout>
      <div className={s.wrapper}>
        <Key size={44} weight="light" className={s.keyIcon} />
        <header className={s.header}>
          <h1 className={s.title}>Activa tu cuenta</h1>
          <p className={s.subtitle}>
            Hola{datos?.nombre ? `, ${datos.nombre}` : ''}. Define la contraseña con la que entrarás a ProyecTwin.
          </p>
        </header>

        <form className={s.form} onSubmit={handleSubmit} noValidate>
          {error && (
            <p className={s.error} role="alert">
              <Warning size={16} weight="fill" /> {error}
            </p>
          )}

          <FormField label="Correo electrónico">
            <Input type="text" value={datos?.correo || ''} readOnly />
          </FormField>

          <FormField
            label="Nueva contraseña"
            required
            error={errores.password}
            help={`Mínimo ${MIN_PASSWORD_LENGTH} caracteres. Puedes usar espacios y frases largas.`}
          >
            <PasswordInput
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••••••"
              autoComplete="new-password"
              autoFocus
            />
          </FormField>

          <FormField label="Confirmar nueva contraseña" required error={errores.password_confirmation}>
            <PasswordInput
              value={confirmar}
              onChange={(e) => setConfirmar(e.target.value)}
              placeholder="••••••••••••"
              autoComplete="new-password"
            />
          </FormField>

          <Button type="submit" size="lg" fullWidth disabled={enviando}>
            {enviando ? 'Activando…' : 'Activar mi cuenta'}
          </Button>
        </form>

        <p className={s.footer}>
          <Link to="/login" className={s.link}>Volver al login</Link>
        </p>
      </div>
    </AuthLayout>
  )
}
