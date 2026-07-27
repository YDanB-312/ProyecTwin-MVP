import { useState } from 'react'
import { Link, useParams, useLocation } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'
import '../../assets/styles/pages/mis-proyectos.css'
import { propuestas, observacionesData } from '../../data/propuestasMock'

export default function DetalleProyectoInstructor() {
  const { id } = useParams()
  const location = useLocation()
  const [observacion, setObservacion] = useState('')
  const [listaObservaciones, setListaObservaciones] = useState(observacionesData)
  const [mensaje, setMensaje] = useState(null)
  const propuesta = propuestas.find(p => p.id === Number(id))

  if (!propuesta) {
    return (
      <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
        <div className="contenedor-revision fade-in">
          <PageHeader title="Proyecto no encontrado" icon="exclamation-circle" breadcrumb={[{ to: '/instructor/dashboard', icon: 'home', label: 'Inicio' }, { label: 'No encontrado' }]} actions={<Link to="/instructor/revision-propuestas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>} />
          <div className="estado-vacio-moderno">
            <div className="estado-vacio-icono"><i className="fas fa-folder-open"></i></div>
            <h3 className="estado-vacio-titulo">Proyecto no encontrado</h3>
            <p className="estado-vacio-descripcion">El proyecto que buscás no existe o fue eliminado.</p>
            <Link to="/instructor/revision-propuestas" className="btn-primario"><i className="fas fa-arrow-left"></i> Volver a Revisión</Link>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  const estadoBadge = {
    pendiente: { clase: 'badge-pendiente', texto: 'Pendiente' },
    aprobado: { clase: 'badge-exito', texto: 'Aprobado' },
    rechazado: { clase: 'badge-peligro', texto: 'Rechazado' },
    requiere_ajustes: { clase: 'badge-advertencia', texto: 'Requiere Ajustes' },
  }

  const badge = estadoBadge[propuesta.estado] || estadoBadge.pendiente

  const agregarObservacion = (e) => {
    e.preventDefault()
    if (!observacion.trim()) return
    const nueva = { autor: 'Carlos Ruiz | Instructor', icono: 'user-tie', fecha: new Date().toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' }), texto: observacion.trim() }
    setListaObservaciones(prev => [nueva, ...prev])
    setObservacion('')
    setMensaje({ tipo: 'exito', texto: 'Observación guardada exitosamente' })
    setTimeout(() => setMensaje(null), 3000)
  }

  const eliminarObservacion = (index) => {
    if (!window.confirm('¿Estás seguro de eliminar esta observación?')) return
    setListaObservaciones(prev => prev.filter((_, i) => i !== index))
    setMensaje({ tipo: 'exito', texto: 'Observación eliminada' })
    setTimeout(() => setMensaje(null), 3000)
  }

  const desde = location.state?.desde
  const volverMap = {
    dashboard: { path: '/instructor/dashboard', label: 'Volver al Panel' },
    alertas: { path: '/instructor/alertas', label: 'Volver a Notificaciones' },
  }
  const volver = volverMap[desde] || { path: '/instructor/revision-propuestas', label: 'Volver a propuestas' }

  const breadcrumb = [
    { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  ]
  if (desde !== 'dashboard') {
    breadcrumb.push({ to: '/instructor/revision-propuestas', label: 'Revisión de Propuestas' })
  }
  breadcrumb.push({ label: propuesta.titulo })

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-revision fade-in">
        <PageHeader
          title={propuesta.titulo}
          icon="folder-open"
          breadcrumb={breadcrumb}
          actions={<Link to={volver.path} className="btn-secundario"><i className="fas fa-arrow-left"></i> {volver.label}</Link>}
        />

        <DataPanel title="Información General" icon="info-circle">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Nombre del Proyecto</div>
              <div className="detalle-valor">{propuesta.titulo}</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <span className={`badge ${badge.clase}`}>{badge.texto}</span>
              {propuesta.similitud > 0 && (
                <span className="badge badge-peligro ml-8"><i className="fas fa-robot"></i> {propuesta.similitud}% similitud detectada</span>
              )}
            </div>
            <div>
              <div className="detalle-label">Aprendiz</div>
              <div className="detalle-valor">{propuesta.aprendiz}</div>
            </div>
            <div>
              <div className="detalle-label">Programa de Formación</div>
              <div className="detalle-valor">{propuesta.programa}</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de Creación</div>
              <div className="detalle-valor">{propuesta.created_at}</div>
            </div>
            <div>
              <div className="detalle-label">Instructor Asignado</div>
              <div className="detalle-valor">{propuesta.instructor}</div>
            </div>
            <div>
              <div className="detalle-label">Área de Aplicación</div>
              <div className="detalle-valor">{propuesta.area_aplicacion}</div>
            </div>
            <div>
              <div className="detalle-label">Tipo de Proyecto</div>
              <div className="detalle-valor">{propuesta.tipo_proyecto}</div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Resumen y Palabras Clave" icon="file-alt">
          <div className="detalle-grid-moderno">
            <div className="detalle-grid-full">
              <div className="detalle-label">Resumen del Proyecto</div>
              <div className="detalle-valor-texto">{propuesta.resumen}</div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Palabras Clave</div>
              <div className="detalle-valor">
                {propuesta.palabras_clave.split(', ').map((palabra, i) => (
                  <span key={i} className="tag-pill tag-pill-gris">{palabra}</span>
                ))}
              </div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Detalles Técnicos" icon="cogs">
          <div className="detalle-grid-moderno">
            <div className="detalle-grid-full">
              <div className="detalle-label">Tecnologías a Utilizar</div>
              <div className="detalle-valor mb-16">
                {propuesta.tecnologias.split(', ').map((tech, i) => (
                  <span key={i} className="tag-pill tag-pill-azul">{tech}</span>
                ))}
              </div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Objetivos Específicos</div>
              <ul className="detalle-ul-reset">
                {propuesta.objetivos.map((obj, i) => (
                  <li key={i}>{obj}</li>
                ))}
              </ul>
            </div>
            <div className="detalle-grid-full mt-16">
              <div className="detalle-label">Entregables Esperados</div>
              <ul className="detalle-ul-reset">
                {propuesta.entregables.map((ent, i) => (
                  <li key={i}>{ent}</li>
                ))}
              </ul>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Integrantes del Equipo" icon="users">
          <div className="detalle-grid-moderno">
            {propuesta.miembros.map((m, i) => (
              <div key={i} className="flex-row flex-wrap team-row">
                <div className={`avatar-miembro avatar-sm ${m.clase}`}>{m.iniciales}</div>
                <div>
                  <strong className="texto-md">{m.nombre}</strong>
                  <br /><span className="texto-claro">{m.rol}</span>
                </div>
              </div>
            ))}
          </div>
        </DataPanel>

        {propuesta.observaciones_adicionales && (
          <DataPanel title="Observaciones Adicionales" icon="clipboard">
            <div className="detalle-grid-moderno">
              <div className="detalle-grid-full">
                <div className="detalle-valor-texto">{propuesta.observaciones_adicionales}</div>
              </div>
            </div>
          </DataPanel>
        )}

        <DataPanel title="Historial de Observaciones" icon="comments">
          {mensaje && <div className={`mensaje-feedback mensaje-${mensaje.tipo} mb-md`}><i className={`fas fa-${mensaje.tipo === 'exito' ? 'check-circle' : 'exclamation-triangle'}`}></i> {mensaje.texto}</div>}
          <div className="lista-observaciones">
            {listaObservaciones.map((obs, i) => (
              <div key={i} className="observacion-item">
                <div className="observacion-header">
                  <span className="observacion-autor"><i className={`fas fa-${obs.icono}`}></i> {obs.autor}</span>
                  <span className="observacion-fecha">{obs.fecha}</span>
                  <div className="observacion-acciones">
                    <button type="button" className="btn-icono eliminar" title="Eliminar observación" aria-label="Eliminar observación" onClick={() => eliminarObservacion(i)}><i className="fas fa-trash-alt"></i></button>
                  </div>
                </div>
                <div className="observacion-contenido">
                  <p>{obs.texto}</p>
                </div>
              </div>
            ))}
          </div>
          <div className="observaciones-section">
            <h3><i className="fas fa-plus-circle"></i> Agregar Observación</h3>
              <form onSubmit={agregarObservacion}>
              <div className="grupo-formulario">
                <label htmlFor="observacion" className="etiqueta">Comentario</label>
                <textarea id="observacion" className="textarea" placeholder="Escribe tu observación sobre el proyecto..." value={observacion} onChange={e => setObservacion(e.target.value)}></textarea>
              </div>
              <div className="acciones-formulario">
                <button type="submit" className="btn-primario" disabled={!observacion.trim()}><i className="fas fa-paper-plane"></i> Guardar Observación</button>
              </div>
            </form>
          </div>
        </DataPanel>

        <div className="acciones-finales">
          <Link to={volver.path} className="btn-secundario"><i className="fas fa-arrow-left"></i> {volver.label}</Link>
        </div>
      </div>
    </DashboardLayout>
  );
}


