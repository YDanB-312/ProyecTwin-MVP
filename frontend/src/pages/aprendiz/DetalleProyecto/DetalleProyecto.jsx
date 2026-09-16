import { useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import DashboardLayout from '../../../layouts/DashboardLayout/DashboardLayout'
import PageHeader from
'../../../components/PageHeader/PageHeader'
import DataPanel from '../../../components/DataPanel/DataPanel'
import Button from '../../../components/Button/Button'
import { Textarea } from '../../../components/Input/Input'
import EmptyState from '../../../components/EmptyState/EmptyState'
import ApiState from '../../../components/ApiState/ApiState'
import ObservacionHilo from
'../../../components/ObservacionHilo/ObservacionHilo'
import GradeBadge from '../../../components/GradeBadge/GradeBadge'
import { useAuth } from '../../../contexts/AuthContext'
import { useApi } from '../../../lib/useApi'
import { proyectos, similitudes as similitudesApi, observaciones as observacionesApi } from '../../../lib/recursos'
import { agruparObservaciones, formatearFecha } from '../../../utils/helpers'
import s from '../../../components/DetalleProyectoBase/DetalleProyectoBase.module.css'
import InformacionProyecto from '../../../components/DetalleProyectoBase/InformacionProyecto'
import { ChatCircle, FileText, FolderOpen, MagnifyingGlass, Plus, X } from 'phosphor-react'

const ROL_LABEL = { aprendiz: 'Aprendiz', instructor: 'Instructor', admin: 'Admin' }

function nombreUsuario(u) {
  if (!u) return 'Usuario'
  return [u.nombre, u.apellido].filter(Boolean).join(' ').trim() || u.correo || 'Usuario'
}

// Una propuesta es del aprendiz si la creó o si figura en su equipo.
function esMia(proyecto, userId) {
  if (!proyecto) return false
  if (Number(proyecto.id_creador) === Number(userId)) return true
  return (proyecto.apprentices || []).some(
    (a) => Number(a.generalUser?.id) === Number(userId) || Number(a.id_usuario) === Number(userId)
  )
}

export default function DetalleProyecto() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  // Propuesta con sus relaciones, similitudes del par y comentarios del hilo.
  const { data: project, cargando, error, recargar } = useApi(
    () => proyectos.obtener(id),
    [id],
    { inicial: null }
  )
  const { data: todasSimilitudes } = useApi(() => similitudesApi.listar(), [], { inicial: [] })
  const { data: comentariosApi, cargando: cargandoObs, recargar: recargarObs } = useApi(
    () => observacionesApi.listar('user', { id_proyecto: id }),
    [id],
    { inicial: [] }
  )

  const [texto, setTexto] = useState('')
  const [respondiendoA, setRespondiendoA] = useState(null)
  const [enviando, setEnviando] = useState(false)

  // Comentarios de la API → shape que consume ObservacionHilo.
  const observaciones = useMemo(
    () => (comentariosApi || []).map((c) => ({
      id: c.id,
      respuestaA: c.respuesta_a,
      autor: `${nombreUsuario(c.user)} | ${ROL_LABEL[c.user?.rol] || 'Comentario'}`,
      texto: c.texto,
      fecha: formatearFecha(c.created_at),
    })),
    [comentariosApi]
  )

  const similitudes = useMemo(
    () => (project
      ? (todasSimilitudes || []).filter(
          (x) => Number(x.id_proyecto_1) === Number(project.id) || Number(x.id_proyecto_2) === Number(project.id)
        )
      : []),
    [project, todasSimilitudes]
  )

  if (cargando) {
    return (
      <DashboardLayout role="aprendiz" titulo="Detalle del Proyecto">
        <div className={s.page}><ApiState cargando /></div>
      </DashboardLayout>
    )
  }

  if (error) {
    return (
      <DashboardLayout role="aprendiz" titulo="Detalle del Proyecto">
        <div className={s.page}><ApiState error={error} onReintentar={recargar} /></div>
      </DashboardLayout>
    )
  }

  if (!project) {
    return (
      <DashboardLayout role="aprendiz" titulo="Detalle del Proyecto">
        <div className={s.page}>
          <EmptyState
            icon={<MagnifyingGlass />}
            title="Proyecto no encontrado"
            message="El proyecto que buscas no existe o fue eliminado."
            actionLabel="Volver a mis propuestas"
            onAction={() => navigate('/aprendiz/propuestas')}
          />
        </div>
      </DashboardLayout>
    )
  }

  // Creador o integrante del equipo → derechos plenos sobre la propuesta
  const esPropio = esMia(project, user.id)
  const ficha = project.classGroup || null

  async function agregarObservacion(e) {
    e.preventDefault()
    const t = texto.trim()
    if (!t) return
    setEnviando(true)
    try {
      await observacionesApi.crear({
        texto: t,
        id_proyecto: Number(project.id),
        id_usuario: Number(user.id),
        respuesta_a: respondiendoA?.id || null,
      })
      await recargarObs()
      setTexto('')
      setRespondiendoA(null)
    } catch {
      // Se conserva el texto para que el usuario pueda reintentar.
    } finally {
      setEnviando(false)
    }
  }

  return (
    <DashboardLayout role="aprendiz" titulo="Detalle del Proyecto">
      <div className={s.page}>
        <PageHeader
          title={project.titulo}
          subtitle={`Enviado el ${formatearFecha(project.created_at)} por ${nombreUsuario(project.creator)}`}
          icon={<FolderOpen />}
          breadcrumb={[
            { label: 'Dashboard', to: '/aprendiz/dashboard' },
            { label: 'Mis Proyectos', to: '/aprendiz/propuestas' },
            { label: project.titulo },
          ]}
        />

        <div className={esPropio ? s.dossier : s.dossierSolo}>
          <div className={s.colPrincipal}>
            <DataPanel title="Información del proyecto" icon={<FileText />}>
              <InformacionProyecto proyecto={project} ficha={ficha} fichaHref={`/aprendiz/detalle-ficha/${project.id_class_group}`} />
            </DataPanel>
          </div>

          {esPropio && (
            <aside className={s.rail} aria-label="Similitudes y observaciones">
              <DataPanel title="Similitudes detectadas" icon={<MagnifyingGlass />}>
                {similitudes.length === 0 ? (
                  <p className={s.muted}>No se han detectado similitudes para esta propuesta.</p>
                ) : (
                  <ul className={s.simList}>
                    {similitudes.map((sim) => {
                      const pct = Math.round(Number(sim.porcentaje) || 0)
                      const otroTitulo = Number(sim.id_proyecto_1) === Number(project.id)
                        ? sim.project2?.titulo
                        : sim.project1?.titulo
                      return (
                        <li key={sim.id}>
                          <Link to={`/aprendiz/detalle-similitud/${sim.id}`} viewTransition className={s.simRow}>
                            <span className={s.simPair}>vs. {otroTitulo || 'Proyecto no disponible'}</span>
                            <span className={s.simRight}>
                              <GradeBadge score={pct} size="sm" />
                            </span>
                          </Link>
                        </li>
                      )
                    })}
                  </ul>
                )}
              </DataPanel>

              <DataPanel title={`Observaciones (${observaciones.length})`} icon={<ChatCircle />}>
                {respondiendoA && (
                  <div className={s.respondiendoChip}>
                    Respondiendo a {String(respondiendoA.autor).split(' | ')[0]}
                    <button type="button" onClick={() => setRespondiendoA(null)} aria-label="Cancelar respuesta"><X size={12} /></button>
                  </div>
                )}
                {cargandoObs ? (
                  <ApiState cargando />
                ) : (
                  <ObservacionHilo
                    grupos={agruparObservaciones(observaciones)}
                    permitirResponder
                    onRespuesta={(o) => setRespondiendoA(o)}
                    className={s.hilosScroll}
                  />
                )}

                <form className={s.obsForm} onSubmit={agregarObservacion}>
                  <Textarea
                    rows={3}
                    value={texto}
                    onChange={(e) => setTexto(e.target.value)}
                    aria-label="Escribe un comentario sobre la propuesta"
                    placeholder={respondiendoA ? 'Escribe tu respuesta al instructor…' : 'Escribe tu comentario sobre la propuesta...'}
                  />
                  <Button type="submit" disabled={!texto.trim() || enviando}>
                    <Plus size={14} /> Agregar observación
                  </Button>
                </form>
              </DataPanel>
            </aside>
          )}
        </div>
      </div>
    </DashboardLayout>
  )
}
