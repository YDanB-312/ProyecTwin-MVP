import { useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { Camera, CheckCircle, IdentificationCard, LockKey, PencilLine, Trash } from 'phosphor-react'
import PageHeader from '../PageHeader/PageHeader'
import DataPanel from '../DataPanel/DataPanel'
import Avatar from '../Avatar/Avatar'
import FormField from '../FormField/FormField'
import Alert from '../Alert/Alert'
import Actions from '../Actions/Actions'
import Button from '../Button/Button'
import StatCard from '../StatCard/StatCard'
import { Input } from '../Input/Input'
import Lightbox from '../Lightbox/Lightbox'
import { useAuth } from '../../contexts/AuthContext'
import { useApi } from '../../lib/useApi'
import { usuarios } from '../../lib/recursos'
import { toFieldErrors } from '../../lib/api'
import { procesarFoto } from '../../utils/foto'
import s from './PerfilBase.module.css'
import { esEmailValido, esPasswordValida } from '../../utils/validation'

const SUBTITULOS = {
  aprendiz: 'Consulta y administra tu información personal',
  instructor: 'Consulta y actualiza tu información personal como instructor.',
  admin: 'Consulta y actualiza tu información personal como administrador.',
}

const ROL_LABEL = {
  aprendiz: 'Aprendiz',
  instructor: 'Instructor',
  admin: 'Administrador',
}

// Refresca el nombre/correo de la sesión guardada tras editar el perfil.
function sincronizarSesion(nombre, correo) {
  try {
    const enLocal = !!localStorage.getItem('auth_user')
    const destino = enLocal ? localStorage : sessionStorage
    const raw = destino.getItem('auth_user')
    if (!raw) return
    const sesion = JSON.parse(raw)
    destino.setItem('auth_user', JSON.stringify({ ...sesion, nombre, correo }))
  } catch {
    return
  }
}

// Campos escalares que exige PUT /general-users.
function payloadCuenta(cuenta, extra = {}) {
  return {
    nombre: cuenta.nombre,
    apellido: cuenta.apellido,
    correo: cuenta.correo,
    rol: cuenta.rol,
    estado: cuenta.estado,
    ...extra,
  }
}

export default function PerfilBase({
  user,
  role = 'aprendiz',
  detalles = [],
  stats = [],
  soloLectura = false,
  titulo = 'Mi Perfil',
  subtitulo = null,
  breadcrumb = null,
}) {
  const { cambiarMiContrasena } = useAuth()

  // Perfil real desde la API (apellido, estado, foto_url…); el `user` de
  // sesión solo trae { id, correo, nombre, rol } y sirve de respaldo.
  const { data: perfilApi, recargar: recargarPerfil } = useApi(
    () => (user?.id ? usuarios.obtener(user.id) : Promise.resolve(null)),
    [user?.id]
  )
  const perfil = perfilApi || user || {}
  const nombre = [perfil.nombre, perfil.apellido].filter(Boolean).join(' ').trim()
    || perfil.name || user?.nombre || ''
  const correo = perfil.correo || perfil.email || user?.correo || ''
  const rol = String(perfil.rol || perfil.role || role || '').toLowerCase()
  const rolLabel = ROL_LABEL[rol] || rol || role
  const foto = perfil.foto_url || perfil.fotoPerfil || null

  const [editando, setEditando] = useState(false)
  const [guardado, setGuardado] = useState(false)
  const [guardando, setGuardando] = useState(false)
  const [form, setForm] = useState(() => ({ email: user?.correo || '' }))
  const [errors, setErrors] = useState({})
  const fileRef = useRef(null)
  const [subiendoFoto, setSubiendoFoto] = useState(false)
  const [fotoError, setFotoError] = useState('')
  const [viendoFoto, setViendoFoto] = useState(false)
  const [fotoMsg, setFotoMsg] = useState(null)
  const msgTimer = useRef(null)
  const [cambiandoPass, setCambiandoPass] = useState(false)
  const [passForm, setPassForm] = useState({ actual: '', nueva: '', confirmar: '' })
  const [passErrors, setPassErrors] = useState({})
  const [passMsg, setPassMsg] = useState(null)
  const passTimer = useRef(null)

  function mostrarFotoMsg(texto, tipo = 'ok') {
    setFotoMsg({ texto, tipo })
    if (msgTimer.current) clearTimeout(msgTimer.current)
    msgTimer.current = setTimeout(() => setFotoMsg(null), 2600)
  }

  // Guarda la foto (data URL base64) manteniendo los campos requeridos.
  async function actualizarFoto(fotoUrl) {
    const cuenta = await usuarios.obtener(user.id)
    await usuarios.actualizar(user.id, payloadCuenta(cuenta, { foto_url: fotoUrl }))
    await recargarPerfil()
  }

  async function alElegirFoto(e) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    setSubiendoFoto(true)
    setFotoError('')
    try {
      const dataUrl = await procesarFoto(file)
      await actualizarFoto(dataUrl)
      mostrarFotoMsg('Foto actualizada')
    } catch (err) {
      setFotoError(err.message || 'No fue posible actualizar la foto.')
    } finally {
      setSubiendoFoto(false)
    }
  }

  async function quitarFoto() {
    setFotoError('')
    try {
      await actualizarFoto(null)
      mostrarFotoMsg('Foto eliminada')
    } catch (err) {
      setFotoError(err.message || 'No fue posible quitar la foto.')
    }
  }

  function set(campo, valor) {
    setForm((f) => ({ ...f, [campo]: valor }))
    setErrors((e) => ({ ...e, [campo]: undefined }))
    setGuardado(false)
  }

  function iniciarEdicion() {
    setForm({ email: correo })
    setErrors({})
    setEditando(true)
  }

  function cancelar() {
    setErrors({})
    setEditando(false)
  }

  async function guardar(e) {
    e.preventDefault()
    const errs = {}
    if (!esEmailValido(form.email.trim())) {
      errs.email = 'Ingresa un correo electrónico válido.'
    }
    setErrors(errs)
    if (Object.keys(errs).length > 0) return

    setGuardando(true)
    try {
      const nuevoCorreo = form.email.trim().toLowerCase()
      // GET + PUT: el backend exige nombre, apellido, correo, rol y estado.
      const cuenta = await usuarios.obtener(user.id)
      await usuarios.actualizar(user.id, payloadCuenta(cuenta, { correo: nuevoCorreo }))
      sincronizarSesion(nombre, nuevoCorreo)
      await recargarPerfil()
      setEditando(false)
      setGuardado(true)
    } catch (err) {
      const campos = toFieldErrors(err?.data)
      setErrors({ email: campos.correo || err?.data?.message || 'No se pudo actualizar el perfil.' })
    } finally {
      setGuardando(false)
    }
  }

  function mostrarPassMsg(texto, tipo = 'ok') {
    setPassMsg({ texto, tipo })
    if (passTimer.current) clearTimeout(passTimer.current)
    passTimer.current = setTimeout(() => setPassMsg(null), 3000)
  }

  function alCambiarPass(campo, valor) {
    setPassForm((f) => ({ ...f, [campo]: valor }))
    setPassErrors((errs) => ({ ...errs, [campo]: undefined }))
  }

  function iniciarCambioPass() {
    setPassForm({ actual: '', nueva: '', confirmar: '' })
    setPassErrors({})
    setCambiandoPass(true)
  }

  function cancelarCambioPass() {
    setPassErrors({})
    setCambiandoPass(false)
  }

  async function guardarPass(e) {
    e.preventDefault()
    const errs = {}
    if (!passForm.actual) errs.actual = 'Ingresa tu contraseña actual.'
    if (!passForm.nueva || !esPasswordValida(passForm.nueva)) {
      errs.nueva = 'La nueva contraseña debe tener al menos 6 caracteres.'
    }
    if (passForm.confirmar !== passForm.nueva) {
      errs.confirmar = 'Las contraseñas no coinciden.'
    }
    setPassErrors(errs)
    if (Object.keys(errs).length > 0) return

    const res = await cambiarMiContrasena(passForm.actual, passForm.nueva)
    if (!res.exito) {
      setPassErrors({ actual: res.mensaje })
      return
    }
    setCambiandoPass(false)
    mostrarPassMsg('Contraseña actualizada correctamente.')
  }

  return (
    <div className={s.wrapper}>
      <PageHeader
        title={titulo}
        subtitle={subtitulo || SUBTITULOS[role] || SUBTITULOS.aprendiz}
        icon={<IdentificationCard />}
        breadcrumb={breadcrumb || undefined}
      />

      {guardado && (
        <Alert>
          <CheckCircle size={14} /> Tus datos se actualizaron correctamente.
        </Alert>
      )}

      {stats.length > 0 && (
        <div className={s.stats}>
          {stats.map((st) => (
            <StatCard key={st.label} value={st.value} label={st.label} />
          ))}
        </div>
      )}

      <DataPanel
        title="Información personal"
        icon={<IdentificationCard />}
        action={
          !soloLectura && !editando ? (
            <Button onClick={iniciarEdicion}>
            <PencilLine size={14} /> Editar perfil
          </Button>
          ) : undefined
        }
      >
        <div className={s.profile}>
          <div className={s.fotoCol}>
              <div className={s.fotoWrap}>
                {!soloLectura && editando ? (
                  <button
                    type="button"
                    className={`${s.fotoBtn} ${foto ? s.fotoBtnVer : ''}`}
                    title={foto ? 'Ver foto' : 'Subir foto de perfil'}
                    aria-label={foto ? `Ver foto de ${nombre}` : 'Subir foto de perfil'}
                    onClick={() => (foto ? setViendoFoto(true) : fileRef.current?.click())}
                    disabled={subiendoFoto}
                  >
                    {subiendoFoto ? (
                      <span className={s.fotoOverlay}>
                        <span className={s.spinner} aria-hidden="true" />
                      </span>
                    ) : foto ? (
                      <Avatar key={foto} name={nombre} src={foto} size="xl" />
                    ) : (
                      <span className={s.fotoPlaceholder}>
                        <Camera size={28} />
                      </span>
                    )}
                  </button>
                ) : (
                  <div
                    className={`${s.fotoBtn} ${foto ? s.fotoBtnVer : ''}`}
                    title={foto ? 'Ver foto' : undefined}
                    onClick={foto ? () => setViendoFoto(true) : undefined}
                    role={foto ? 'button' : undefined}
                    tabIndex={foto ? 0 : undefined}
                    aria-label={foto ? `Ver foto de ${nombre}` : undefined}
                    onKeyDown={foto ? (e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setViendoFoto(true) } } : undefined}
                  >
                    {foto ? (
                      <Avatar key={foto} name={nombre} src={foto} size="xl" />
                    ) : (
                      <span className={s.fotoPlaceholder}>
                        <Camera size={28} />
                      </span>
                    )}
                  </div>
                )}
                {!soloLectura && editando && foto && !subiendoFoto && (
                  <button
                    type="button"
                    className={s.fotoCam}
                    title="Cambiar foto de perfil"
                    aria-label="Cambiar foto de perfil"
                    onClick={() => fileRef.current?.click()}
                  >
                    <Camera size={14} />
                  </button>
                )}
                {!soloLectura && editando && (
                  <input
                    ref={fileRef}
                    type="file"
                    accept="image/*"
                    className={s.fotoInput}
                    aria-label="Cambiar foto de perfil"
                    tabIndex={-1}
                    onChange={alElegirFoto}
                    disabled={subiendoFoto}
                  />
                )}
              </div>
              {!soloLectura && editando && foto && !subiendoFoto && (
                <button type="button" className={s.fotoQuitar} onClick={quitarFoto}>
                  <Trash size={12} /> Quitar foto
                </button>
              )}
          </div>

          <div className={s.profileInfo}>
            {!editando && (
              <>
                <h2 className={s.profileName}>{nombre}</h2>
                <p className={s.profileEmail}>{correo}</p>
              </>
            )}
            {!editando && detalles.length > 0 && (
              <dl className={s.detailList}>
                {detalles.map((d) => (
                  <div key={d.label} className={s.detailRow}>
                    <dt>{d.label}</dt>
                    <dd>{d.value}</dd>
                  </div>
                ))}
              </dl>
            )}
            {editando && (
              <form className={`${s.form} ${s.formGrow}`} onSubmit={guardar} noValidate>
                <FormField label="Rol">
                  <Input type="text" value={rolLabel} readOnly />
                </FormField>
                <FormField label="Nombre completo" help="El nombre identifica tus propuestas y equipos; no se puede cambiar.">
                  <Input
                    type="text"
                    value={nombre}
                    readOnly
                  />
                </FormField>
                <FormField label="Correo electrónico" required error={errors.email}>
                  <Input
                    type="email"
                    value={form.email}
                    onChange={(e) => set('email', e.target.value)}
                  />
                </FormField>
                <Actions form>
                  <Button type="submit" disabled={guardando}>
                    {guardando ? 'Guardando...' : 'Guardar cambios'}
                  </Button>
                  <Button variant="secondary" onClick={cancelar}>
                    Cancelar
                  </Button>
                </Actions>
              </form>
            )}
          </div>
          </div>
      </DataPanel>

      {!soloLectura && (
        <DataPanel title="Seguridad" icon={<LockKey />}>
          {!cambiandoPass ? (
          <div className={s.seguridadRow}>
            <p className={s.seguridadTexto}>
              Usa una contraseña única de al menos 6 caracteres para proteger tu cuenta.
              <br />
              <Link to="/recuperar-contrasena" className={s.link}>¿Olvidaste tu contraseña actual? Recupérala por correo</Link>
            </p>
            <Button type="button" variant="secondary" onClick={iniciarCambioPass}>
              <LockKey size={14} /> Cambiar contraseña
            </Button>
          </div>
        ) : (
          <form className={s.form} onSubmit={guardarPass} noValidate>
            <FormField label="Contraseña actual" required error={passErrors.actual}>
              <Input
                type="password"
                value={passForm.actual}
                onChange={(e) => alCambiarPass('actual', e.target.value)}
                autoComplete="current-password"
              />
            </FormField>
            <FormField label="Nueva contraseña" required error={passErrors.nueva} help="Mínimo 6 caracteres">
              <Input
                type="password"
                value={passForm.nueva}
                onChange={(e) => alCambiarPass('nueva', e.target.value)}
                autoComplete="new-password"
              />
            </FormField>
            <FormField label="Confirmar nueva contraseña" required error={passErrors.confirmar}>
              <Input
                type="password"
                value={passForm.confirmar}
                onChange={(e) => alCambiarPass('confirmar', e.target.value)}
                autoComplete="new-password"
              />
            </FormField>
            <Actions form>
              <Button type="submit">
                <CheckCircle size={14} /> Actualizar contraseña
              </Button>
              <Button variant="secondary" onClick={cancelarCambioPass}>
                Cancelar
              </Button>
            </Actions>
          </form>
        )}
        {passMsg && <Alert variant={passMsg.tipo === 'ok' ? 'success' : 'danger'}>{passMsg.texto}</Alert>}
        </DataPanel>
      )}

      {(fotoMsg || fotoError) && (
        <div className={`${s.snackbar} ${fotoError ? s.snackbarError : ''}`} role={fotoError ? 'alert' : 'status'}>
          {!fotoError && <CheckCircle size={14} weight="fill" />}
          {fotoError || fotoMsg.texto}
        </div>
      )}

      {viendoFoto && foto && (
        <Lightbox
          src={foto}
          alt={`Foto de ${nombre}`}
          caption={nombre}
          onClose={() => setViendoFoto(false)}
        />
      )}
    </div>
  )
}
