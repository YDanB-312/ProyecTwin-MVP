import { Link } from 'react-router-dom'
import DashboardLayout from '../../../layouts/DashboardLayout/DashboardLayout'
import { GraduationCap, Users } from 'phosphor-react'
import PageHeader from '../../../components/PageHeader/PageHeader'
import Badge from '../../../components/Badge/Badge'
import Avatar from '../../../components/Avatar/Avatar'
import Alert from '../../../components/Alert/Alert'
import EmptyState from '../../../components/EmptyState/EmptyState'
import ApiState from '../../../components/ApiState/ApiState'
import Button from '../../../components/Button/Button'
import { useAuth } from '../../../contexts/AuthContext'
import { useApi } from '../../../lib/useApi'
import { aprendices, fichas, proyectos } from '../../../lib/recursos'
import sd from '../../../components/DetalleFichaBase/DetalleFichaBase.module.css'

function nombreUsuario(u) {
  if (!u) return 'Usuario'
  return [u.nombre, u.apellido].filter(Boolean).join(' ').trim() || u.correo || 'Usuario'
}

export default function MiFicha() {
  const { user } = useAuth()

  // La ficha del aprendiz se deriva de su fila de aprendices (id_class_group).
  const { data: aprendicesApi, cargando: cargandoA, error: errorA, recargar: recargarA } =
    useApi(() => aprendices.listar(), [], { inicial: [] })
  const { data: fichasApi, cargando: cargandoF, error: errorF, recargar: recargarF } =
    useApi(() => fichas.listar(), [], { inicial: [] })
  const { data: todosProyectos } = useApi(() => proyectos.listar(), [], { inicial: [] })

  const miAprendiz = aprendicesApi.find((a) => Number(a.id_usuario) === Number(user.id)) || null
  const ficha = miAprendiz
    ? fichasApi.find((f) => Number(f.id) === Number(miAprendiz.id_class_group)) || null
    : null

  const compañeros = ficha
    ? aprendicesApi.filter((a) => Number(a.id_class_group) === Number(ficha.id))
    : []
  const proyectosFicha = ficha
    ? todosProyectos.filter((p) => Number(p.id_class_group) === Number(ficha.id))
    : []

  const cargando = cargandoA || cargandoF
  const error = errorA || errorF
  const recargar = () => { recargarA(); recargarF() }

  if (cargando) {
    return (
      <DashboardLayout role="aprendiz" titulo="Mi Ficha">
        <div><ApiState cargando /></div>
      </DashboardLayout>
    )
  }

  if (error) {
    return (
      <DashboardLayout role="aprendiz" titulo="Mi Ficha">
        <div><ApiState error={error} onReintentar={recargar} /></div>
      </DashboardLayout>
    )
  }

  /* ---------- SIN FICHA: la asignación la gestiona coordinación ---------- */
  if (!ficha) {
    return (
      <DashboardLayout role="aprendiz" titulo="Ficha">
        <div>
          <PageHeader
            title="Mi Ficha"
            subtitle="Consulta aquí tu ficha de formación y tus compañeros"
            icon={<GraduationCap />}
          />

          <EmptyState
            icon={<GraduationCap size={40} weight="light" />}
            title="Aún no tienes una ficha asignada"
            message="La vinculación a las fichas la gestiona coordinación o tu instructor. Cuando estés asignado, verás aquí tu ficha y tus compañeros."
          />

          <Alert variant="info">
            ¿Crees que es un error? Contacta a tu instructor o a coordinación académica.
          </Alert>

          <Button as="link" to="/aprendiz/propuestas" variant="secondary">
            Ir a mis propuestas
          </Button>
        </div>
      </DashboardLayout>
    )
  }

  /* ---------- CON FICHA: mi ficha + compañeros ---------- */
  const instructorUserId = ficha.instructor?.generalUser?.id
  const programa = ficha.program?.nombre || '—'

  return (
    <DashboardLayout role="aprendiz" titulo="Mi Ficha">
      <div>
        <PageHeader
          title="Mi Ficha"
          subtitle={`Código ${ficha.codigo} · N° ${ficha.numero} · ${programa}`}
          icon={<GraduationCap />}
          breadcrumb={[{ label: 'Dashboard', to: '/aprendiz/dashboard' }, { label: 'Mi Ficha' }]}
        />

        <section className={sd.infoCard}>
          <header className={sd.infoHeader}>
            <span className={sd.infoIcon} aria-hidden="true"><GraduationCap size={22} /></span>
            <div>
              <h2 className={sd.infoTitle}>{ficha.nombre}</h2>
              <span className={`${sd.infoCodigo} ${sd.mono}`}>{ficha.codigo}</span>
            </div>
            <Badge variant={ficha.estado === 'activo' ? 'success' : 'danger'}>
              {ficha.estado === 'activo' ? 'Activa' : 'Inactiva'}
            </Badge>
          </header>

          <dl className={sd.infoGrid}>
            <div className={sd.infoItem}>
              <dt>Número de ficha</dt>
              <dd>N° {ficha.numero}</dd>
            </div>
            <div className={sd.infoItem}>
              <dt>Programa</dt>
              <dd>{programa}</dd>
            </div>
            <div className={sd.infoItem}>
              <dt>Instructor</dt>
              <dd>
                {instructorUserId ? (
                  <Link to={`/aprendiz/perfil-instructor?id=${instructorUserId}`} className={sd.link}>
                    {nombreUsuario(ficha.instructor?.generalUser) }
                  </Link>
                ) : (
                  'Sin asignar'
                )}
              </dd>
            </div>
            <div className={sd.infoItem}>
              <dt>Aprendices</dt>
              <dd>{compañeros.length}</dd>
            </div>
            <div className={sd.infoItem}>
              <dt>Propuestas</dt>
              <dd>{proyectosFicha.length}</dd>
            </div>
          </dl>
        </section>

        <Alert variant="info">
          La asignación de aprendices a la ficha la gestiona coordinación académica.
        </Alert>

        {compañeros.length === 0 ? (
          <EmptyState
            icon={<Users />}
            title="Sin aprendices registrados"
            message="Aún no hay aprendices vinculados a esta ficha."
          />
        ) : (
          <>
            <h3 className={sd.sectionTitle}>Integrantes de la ficha ({compañeros.length})</h3>
            <ul className={sd.studentsGrid}>
              {compañeros.map((est, i) => {
                const g = est.generalUser || {}
                return (
                  <li key={est.id} className="fx-rise" style={{ '--fx-i': i }}>
                    <Link to={`/aprendiz/perfil-companero/${g.id}`} viewTransition className={sd.studentCard}>
                      <Avatar name={nombreUsuario(g)} src={g.foto_url} size="md" />
                      <span className={sd.studentInfo}>
                        <span className={sd.studentName}>{nombreUsuario(g)}</span>
                        <span className={sd.studentEmail}>{g.correo}</span>
                      </span>
                    </Link>
                  </li>
                )
              })}
            </ul>
          </>
        )}
      </div>
    </DashboardLayout>
  )
}
