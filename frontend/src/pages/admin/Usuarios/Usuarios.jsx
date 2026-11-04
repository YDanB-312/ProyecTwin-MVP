import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import DashboardLayout from '../../../layouts/DashboardLayout/DashboardLayout'
import PageHeader from '../../../components/PageHeader/PageHeader'
import FilterBar from '../../../components/FilterBar/FilterBar'
import DataTable from '../../../components/DataTable/DataTable'
import DataPanel from '../../../components/DataPanel/DataPanel'
import FormField from '../../../components/FormField/FormField'
import Badge from '../../../components/Badge/Badge'
import Avatar from '../../../components/Avatar/Avatar'
import Alert from '../../../components/Alert/Alert'
import Button from '../../../components/Button/Button'
import ConfirmModal from '../../../components/ConfirmModal/ConfirmModal'
import Actions from '../../../components/Actions/Actions'
import { Input, Select } from '../../../components/Input/Input'
import Pagination from '../../../components/Pagination/Pagination'
import EmptyState from '../../../components/EmptyState/EmptyState'
import ApiState from '../../../components/ApiState/ApiState'
import {
  Users, Plus, Eye, CheckCircle, Code, ChartBar, Prohibit, ArrowCounterClockwise,
  Warning,
} from 'phosphor-react'
import { useAuth } from '../../../contexts/AuthContext'
import { useApi } from '../../../lib/useApi'
import { useDebounced } from '../../../lib/useDebounced'
import { usuarios, aprendices, fichas, programas } from '../../../lib/recursos'
import { esEmailValido, MAX_DOCUMENTO, MAX_NOMBRE } from '../../../utils/validation'
import { PAGINA_TABLA } from '../../../constants/pagination'

// Estilos reutilizados de las páginas originales (lista + formulario)
import s from '../../../components/ListaBase/ListaBase.module.css'
import nu from '../../../components/FormularioBase/FormularioBase.module.css'
import { payloadCuenta } from '../../../utils/helpers'
import { ROL_LABEL } from '../../../constants/badgeVariants'

const ITEMS_POR_PAGINA = PAGINA_TABLA

const ROL_VARIANT = { aprendiz: 'info', instructor: 'primary', admin: 'warning' }
const ESTADO_VARIANT = { activo: 'success', suspendido: 'danger' }
const ESTADO_LABEL = { activo: 'Activo', suspendido: 'Suspendido' }

const TIPOS_DOCUMENTO = ['CC', 'TI', 'CE', 'PPT']


export default function Usuarios() {
  const { user } = useAuth()
  const [searchParams] = useSearchParams()
  const [creando, setCreando] = useState(() => searchParams.get('crear') === '1')

  // Reacciona si se navega a ?crear=1 ya estando en la lista
  useEffect(() => {
    if (searchParams.get('crear') === '1') {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setCreando(true)
    }
  }, [searchParams])

  const [accionMsg, setAccionMsg] = useState(null)
  const [accionOk, setAccionOk] = useState('')
  const [suspender, setSuspender] = useState(null)
  // Resultado del alta: estado del enlace de activación enviado.
  const [resultadoAlta, setResultadoAlta] = useState(null)
  const [accionOcupada, setAccionOcupada] = useState(false)

  /* ---------- Lista (paginación del servidor) ---------- */
  const [busqueda, setBusqueda] = useState('')
  const busquedaServidor = useDebounced(busqueda.trim(), 300)
  const [filtroRol, setFiltroRol] = useState('todos')
  const [filtroEstado, setFiltroEstado] = useState('todos')
  const [filtroFicha, setFiltroFicha] = useState('todos')
  const [filtroPrograma, setFiltroPrograma] = useState('todos')
  const [pagina, setPagina] = useState(1)

  const { data: paginaUsuarios, cargando, error, recargar } = useApi(
    () => usuarios.listarPaginado({
      page: pagina,
      por_pagina: ITEMS_POR_PAGINA,
      search: busquedaServidor,
      role: filtroRol,
      estado: filtroEstado,
      ficha_id: filtroFicha,
      programa: filtroPrograma,
    }),
    [pagina, busquedaServidor, filtroRol, filtroEstado, filtroFicha, filtroPrograma],
    { inicial: null }
  )

  const usuariosPagina = paginaUsuarios?.data || []
  const totalUsuarios = paginaUsuarios?.total || 0

  // Catálogos para resolver ficha/programa de cada fila.
  const { data: catalogos } = useApi(
    async () => {
      const [listaAprendices, listaFichas, listaProgramas] = await Promise.all([
        aprendices.listar('generalUser,classGroup.program'),
        fichas.listar('program', {}),
        programas.listar(),
      ])
      return { listaAprendices, listaFichas, listaProgramas }
    },
    [],
    { inicial: null }
  )

  const aprendicesLista = catalogos?.listaAprendices || []
  const fichasLista = catalogos?.listaFichas || []
  const programasLista = catalogos?.listaProgramas || []

  const aprendicesPorUsuario = new Map(aprendicesLista.map((a) => [Number(a.id_usuario), a]))
  const fichasPorId = new Map(fichasLista.map((f) => [Number(f.id), f]))

  const fichaDeUsuario = (usr) => {
    const perfil = aprendicesPorUsuario.get(Number(usr.id))
    if (!perfil) return null
    return perfil.classGroup || fichasPorId.get(Number(perfil.id_class_group)) || null
  }

  const programasFiltro = [...new Set(programasLista.map((p) => p.nombre))].sort()

  const limpiarFiltros = () => {
    setBusqueda('')
    setFiltroRol('todos')
    setFiltroEstado('todos')
    setFiltroFicha('todos')
    setFiltroPrograma('todos')
    setPagina(1)
  }

  // Reenvía el enlace de activación de una cuenta pendiente.
  const reenviar = async (usr) => {
    if (accionOcupada) return
    setAccionMsg(null)
    setAccionOk('')
    setAccionOcupada(true)
    try {
      await usuarios.reenviarActivacion(usr.id)
      setAccionOk('Enlace de activación reenviado al correo personal.')
      await recargar()
    } catch (err) {
      setAccionMsg(err?.data?.message || 'No se pudo reenviar el correo.')
    } finally {
      setAccionOcupada(false)
    }
  }

  /* ---------- Creación ---------- */
  const [form, setForm] = useState({
    nombre: '', apellido: '', tipoDocumento: 'CC', numeroDocumento: '', correo: '', rol: 'aprendiz',
  })
  const [errores, setErrores] = useState({})
  const [guardando, setGuardando] = useState(false)

  const onChange = (e) => {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    setErrores((err) => ({ ...err, [name]: undefined }))
  }

  const validar = () => {
    const err = {}
    if (!form.nombre.trim()) err.nombre = 'El nombre es obligatorio.'
    if (!form.apellido.trim()) err.apellido = 'El apellido es obligatorio.'
    if (!form.tipoDocumento) err.tipoDocumento = 'Selecciona el tipo de documento.'
    if (!form.numeroDocumento.trim()) err.numeroDocumento = 'El número de documento es obligatorio.'
    if (!form.correo.trim()) err.correo = 'El correo es obligatorio.'
    else if (!esEmailValido(form.correo.trim())) err.correo = 'Ingresa un correo válido.'
    if (!form.rol) err.rol = 'Selecciona un rol.'
    return err
  }

  const onSubmit = async (e) => {
    e.preventDefault()
    if (guardando) return
    const err = validar()
    if (Object.keys(err).length) {
      setErrores(err)
      return
    }
    setGuardando(true)
    setAccionMsg(null)
    setAccionOk('')
    try {
      // El sistema crea la cuenta pendiente y envía el enlace de activación.
      const resp = await usuarios.crear({
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        tipo_documento: form.tipoDocumento,
        numero_documento: form.numeroDocumento.trim(),
        correo: form.correo.trim().toLowerCase(),
        rol: form.rol,
        estado: true,
      })
      setResultadoAlta({
        id: resp.usuario.id,
        nombre: `${resp.usuario.nombre} ${resp.usuario.apellido}`.trim(),
        correo: resp.usuario.correo,
        enviada: !!resp.activacion?.enviada,
        mensaje: resp.activacion?.mensaje || '',
      })
      setForm({ nombre: '', apellido: '', tipoDocumento: 'CC', numeroDocumento: '', correo: '', rol: 'aprendiz' })
      setErrores({})
      setCreando(false)
      await recargar()
    } catch (error) {
      const campos = error?.data?.errors || {}
      const traducidos = {
        nombre: campos.nombre?.[0],
        apellido: campos.apellido?.[0],
        tipo_documento: campos.tipo_documento?.[0],
        numero_documento: campos.numero_documento?.[0],
        correo: campos.correo?.[0],
        rol: campos.rol?.[0],
      }
      const limpios = Object.fromEntries(Object.entries(traducidos).filter(([, v]) => v))
      if (Object.keys(limpios).length) {
        setErrores({
          nombre: limpios.nombre,
          apellido: limpios.apellido,
          tipoDocumento: limpios.tipo_documento,
          numeroDocumento: limpios.numero_documento,
          correo: limpios.correo,
          rol: limpios.rol,
        })
      } else {
        setAccionMsg(error?.data?.message || 'No se pudo crear el usuario.')
      }
    } finally {
      setGuardando(false)
    }
  }

  /* ---------- Estado (activar / suspender) ---------- */
  const cambiarEstado = async (usr, nuevoEstado) => {
    if (accionOcupada) return
    setAccionMsg(null)
    setAccionOcupada(true)
    try {
      const cuenta = await usuarios.obtener(usr.id)
      await usuarios.actualizar(usr.id, payloadCuenta(cuenta, { estado: nuevoEstado }))
      await recargar()
    } catch (err2) {
      setAccionMsg(err2?.data?.message || 'No se pudo actualizar el estado del usuario.')
    } finally {
      setAccionOcupada(false)
    }
  }

  return (
    <DashboardLayout role="admin" titulo={creando ? 'Nuevo Usuario' : 'Usuarios'}>
      <div className={s.page}>
        <PageHeader
          title={creando ? 'Crear Nuevo Usuario' : 'Gestión de Usuarios'}
          subtitle={
            creando
              ? 'El sistema crea la cuenta y envía un enlace de activación al correo personal.'
              : 'Administra las cuentas de aprendices e instructores de la plataforma.'
          }
          icon={creando ? <Code /> : <Users />}
          breadcrumb={
            creando
              ? [
                  { label: 'Dashboard', to: '/admin/dashboard', icon: <ChartBar size={14} /> },
                  { label: 'Usuarios', icon: <Users size={14} />, onClick: () => setCreando(false) },
                  { label: 'Nuevo usuario' },
                ]
              : []
          }
          onBack={creando ? () => setCreando(false) : undefined}
          actions={
            !creando ? (
              <Button
                type="button"
                onClick={() => { setResultadoAlta(null); setCreando(true) }}
              >
                <Plus size={14} /> Nuevo Usuario
              </Button>
            ) : undefined
          }
        />

        {creando ? (
          <DataPanel title="Datos del usuario" icon={<Code />}>
            <form className={nu.form} onSubmit={onSubmit} noValidate>
              <div className={nu.grid2}>
                <FormField label="Nombres" required error={errores.nombre}>
                  <Input
                    name="nombre"
                    value={form.nombre}
                    onChange={onChange}
                    placeholder="Ej. María José"
                    maxLength={MAX_NOMBRE}
                  />
                </FormField>
                <FormField label="Apellidos" required error={errores.apellido}>
                  <Input
                    name="apellido"
                    value={form.apellido}
                    onChange={onChange}
                    placeholder="Ej. González Ruiz"
                    maxLength={MAX_NOMBRE}
                  />
                </FormField>
              </div>

              <div className={nu.grid2}>
                <FormField label="Tipo de documento" required error={errores.tipoDocumento}>
                  <Select name="tipoDocumento" value={form.tipoDocumento} onChange={onChange}>
                    {TIPOS_DOCUMENTO.map((t) => (
                      <option key={t} value={t}>{t}</option>
                    ))}
                  </Select>
                </FormField>
                <FormField label="Número de documento" required error={errores.numeroDocumento} help="Único: evita cuentas duplicadas.">
                  <Input
                    name="numeroDocumento"
                    inputMode="numeric"
                    value={form.numeroDocumento}
                    onChange={onChange}
                    placeholder="Ej. 1234567890"
                    maxLength={MAX_DOCUMENTO}
                  />
                </FormField>
              </div>

              <div className={nu.grid2}>
                <FormField label="Correo personal" required error={errores.correo} help="Allí se envía el enlace de activación y la recuperación de contraseña.">
                  <Input
                    name="correo"
                    type="email"
                    value={form.correo}
                    onChange={onChange}
                    placeholder="Correo personal"
                  />
                </FormField>
                <FormField label="Rol" required error={errores.rol}>
                  <Select name="rol" value={form.rol} onChange={onChange}>
                    <option value="aprendiz">Aprendiz</option>
                    <option value="instructor">Instructor</option>
                  </Select>
                </FormField>
              </div>

              <Actions form>
                <Button type="submit" disabled={guardando}>
                  <CheckCircle size={14} /> {guardando ? 'Creando…' : 'Crear usuario'}
                </Button>
                <Button type="button" variant="secondary" onClick={() => setCreando(false)}>
                  Cancelar
                </Button>
              </Actions>
            </form>
          </DataPanel>
        ) : (
          <ApiState cargando={cargando} error={error} onReintentar={recargar}>
            {resultadoAlta && (
              <Alert variant={resultadoAlta.enviada ? 'success' : 'warning'}>
                <CheckCircle size={14} /> Usuario <strong>{resultadoAlta.nombre}</strong> creado.
                {resultadoAlta.enviada
                  ? <> Enlace de activación enviado a <strong>{resultadoAlta.correo}</strong>.</>
                  : <> La cuenta quedó pendiente, pero el correo no pudo enviarse. Reenvía la activación.</>}
                <div className={s.actions} style={{ marginTop: 'var(--sp-2)' }}>
                  Correo: <code className={s.codigo}>{resultadoAlta.correo}</code>
                  {!resultadoAlta.enviada && (
                    <Button
                      type="button"
                      size="sm"
                      variant="secondary"
                      onClick={() => reenviar({ id: resultadoAlta.id })}
                    >
                      <ArrowCounterClockwise size={14} /> Reenviar activación
                    </Button>
                  )}
                </div>
              </Alert>
            )}

            {accionMsg && (
              <Alert variant="danger">
                <Warning size={14} /> {accionMsg}
              </Alert>
            )}

            {accionOk && (
              <Alert variant="success">
                <CheckCircle size={14} /> {accionOk}
              </Alert>
            )}

            <FilterBar
              title="Buscar y filtrar"
              actions={
                totalUsuarios > 0 && (
                  <Button type="button" variant="secondary" size="sm" onClick={limpiarFiltros}>
                    Limpiar filtros
                  </Button>
                )
              }
            >
              <label className={s.field}>
                <span className={s.label}>Buscar</span>
                <Input
                  value={busqueda}
                  onChange={(e) => { setBusqueda(e.target.value); setPagina(1) }}
                  placeholder="Nombre, documento, usuario o correo…"
                />
              </label>
              <label className={s.field}>
                <span className={s.label}>Rol</span>
                <Select
                  value={filtroRol}
                  onChange={(e) => { setFiltroRol(e.target.value); setPagina(1) }}
                >
                  <option value="todos">Todos</option>
                  <option value="aprendiz">Aprendiz</option>
                  <option value="instructor">Instructor</option>
                  <option value="admin">Administrador</option>
                </Select>
              </label>
              <label className={s.field}>
                <span className={s.label}>Estado</span>
                <Select
                  value={filtroEstado}
                  onChange={(e) => { setFiltroEstado(e.target.value); setPagina(1) }}
                >
                  <option value="todos">Todos</option>
                  <option value="activo">{ESTADO_LABEL.activo}</option>
                  <option value="suspendido">Suspendido</option>
                </Select>
              </label>
              <label className={s.field}>
                <span className={s.label}>Ficha</span>
                <Select
                  value={filtroFicha}
                  onChange={(e) => { setFiltroFicha(e.target.value); setPagina(1) }}
                >
                  <option value="todos">Todas</option>
                  <option value="sin">Sin ficha</option>
                  {fichasLista.map((f) => (
                    <option key={f.id} value={String(f.id)}>
                      {f.codigo} · {f.nombre}
                    </option>
                  ))}
                </Select>
              </label>
              <label className={s.field}>
                <span className={s.label}>Programa</span>
                <Select
                  value={filtroPrograma}
                  onChange={(e) => { setFiltroPrograma(e.target.value); setPagina(1) }}
                >
                  <option value="todos">Todos</option>
                  {programasFiltro.map((p) => (
                    <option key={p} value={p}>{p}</option>
                  ))}
                </Select>
              </label>
              <p className={s.info}>
                {totalUsuarios} usuario{totalUsuarios !== 1 ? 's' : ''}
              </p>
            </FilterBar>

            {usuariosPagina.length === 0 ? (
              <EmptyState
                icon={<Users />}
                title="Sin usuarios"
                message={
                  totalUsuarios === 0
                    ? 'No hay usuarios que coincidan. Crea el primero para comenzar.'
                    : 'Ningún usuario coincide con los filtros aplicados.'
                }
                actionLabel="Limpiar filtros"
                onAction={limpiarFiltros}
              />
            ) : (
              <>
                <DataTable
                  ariaLabel="Usuarios registrados"
                  columns={[
                    {
                      key: 'usuario',
                      header: 'Usuario',
                      render: (usr) => (
                        <Link to={`/admin/detalle-usuario/${usr.id}`} viewTransition className={s.userCell}>
                          <Avatar name={`${usr.nombre || ''} ${usr.apellido || ''}`} src={usr.foto_url} size="sm" />
                          <span className={s.userName}>{`${usr.nombre || ''} ${usr.apellido || ''}`.trim()}</span>
                        </Link>
                      ),
                    },
                    {
                      key: 'documento',
                      header: 'Documento',
                      render: (usr) => (
                        usr.numero_documento
                          ? <span>{usr.tipo_documento} {usr.numero_documento}</span>
                          : <span className={s.muted}>—</span>
                      ),
                    },
                    { key: 'correo', header: 'Correo', render: (usr) => usr.correo },
                    {
                      key: 'rol',
                      header: 'Rol',
                      render: (usr) => (
                        <Badge variant={ROL_VARIANT[usr.rol] || 'neutral'}>
                          {ROL_LABEL[usr.rol] || usr.rol}
                        </Badge>
                      ),
                    },
                    {
                      key: 'ficha',
                      header: 'Ficha',
                      render: (usr) => {
                        const ficha = fichaDeUsuario(usr)
                        return ficha ? <code className={s.codigo}>{ficha.codigo}</code> : <span className={s.muted}>—</span>
                      },
                    },
                    {
                      key: 'acceso',
                      header: 'Acceso',
                      render: (usr) => {
                        if (usr.rol === 'admin') return <span className={s.muted}>—</span>
                        if (!usr.email_verified_at) {
                          return <Badge variant="warning">Pendiente de activación</Badge>
                        }
                        return <Badge variant="success">Activa</Badge>
                      },
                    },
                    {
                      key: 'estado',
                      header: 'Estado',
                      render: (usr) => {
                        const estado = usr.estado === false ? 'suspendido' : 'activo'
                        return (
                          <Badge variant={ESTADO_VARIANT[estado] || 'neutral'}>
                            {ESTADO_LABEL[estado]}
                          </Badge>
                        )
                      },
                    },
                    {
                      key: 'acciones',
                      header: 'Acciones',
                      align: 'end',
                      render: (usr) => (
                        <div className={s.actions}>
                          <Button
                            as="link"
                            to={`/admin/detalle-usuario/${usr.id}`}
                            viewTransition
                            size="sm"
                            variant="secondary"
                          >
                            <Eye size={14} /> Ver
                          </Button>
                          {usr.rol !== 'admin' && !usr.email_verified_at && (
                            <Button
                              type="button"
                              size="sm"
                              variant="ghost"
                              title="Reenviar el enlace de activación"
                              disabled={accionOcupada}
                              onClick={() => reenviar(usr)}
                            >
                              <ArrowCounterClockwise size={14} /> Reenviar activación
                            </Button>
                          )}
                          {usr.estado === false ? (
                            <Button
                              type="button"
                              size="sm"
                              variant="ghost"
                              title="Reactivar cuenta"
                              disabled={accionOcupada}
                              onClick={() => cambiarEstado(usr, true)}
                            >
                              <ArrowCounterClockwise size={14} /> Activar
                            </Button>
                          ) : (
                            <Button
                              type="button"
                              size="sm"
                              variant="dangerGhost"
                              title={Number(user?.id) === Number(usr.id) ? 'No puedes suspender tu propia cuenta' : 'Suspender cuenta'}
                              disabled={Number(user?.id) === Number(usr.id)}
                              onClick={() => setSuspender(usr)}
                            >
                              <Prohibit size={14} /> Suspender
                            </Button>
                          )}
                        </div>
                      ),
                    },
                  ]}
                  rows={usuariosPagina}
                  keyOf={(usr) => usr.id}
                />

                <Pagination
                  totalItems={totalUsuarios}
                  itemsPerPage={ITEMS_POR_PAGINA}
                  paginaActual={pagina}
                  setPaginaActual={setPagina}
                  itemName="usuarios"
                />
              </>
            )}
          </ApiState>
        )}
      </div>

      <ConfirmModal
        open={!!suspender}
        titulo="Suspender cuenta"
        mensaje={suspender
          ? `¿Suspender la cuenta de ${suspender.nombre} ${suspender.apellido}? No podrá iniciar sesión hasta que la reactives.`
          : ''}
        textoConfirmar="Sí, suspender"
        onCancelar={() => setSuspender(null)}
        onConfirmar={async () => {
          await cambiarEstado(suspender, false)
          setSuspender(null)
        }}
      />
    </DashboardLayout>
  )
}
