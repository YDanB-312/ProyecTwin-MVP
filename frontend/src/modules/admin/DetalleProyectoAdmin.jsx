import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/detalle-compartido.css'

const miembros = [
  { nombre: 'Maria Gonzalez', rol: 'Creador' },
  { nombre: 'Juan Pérez', rol: 'Integrante' },
  { nombre: 'Laura Gómez', rol: 'Integrante' },
]

const breadcrumb = [
  { to: '/admin/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/admin/proyectos', label: 'Proyectos' },
  { label: 'Detalle del Proyecto' },
]

export default function DetalleProyectoAdmin() {
  const location = useLocation()
  const proyecto = location.state?.proyecto || null
  const [estado, setEstado] = useState(proyecto?.estado || 'aprobado')
  const [mensaje, setMensaje] = useState(null)

  const cambiarEstado = (nuevoEstado) => {
    setEstado(nuevoEstado)
    const textos = { aprobado: 'Proyecto aprobado', rechazado: 'Proyecto rechazado', requiere_ajustes: 'Requiere ajustes' }
    setMensaje({ tipo: 'exito', texto: textos[nuevoEstado] || 'Estado actualizado' })
    setTimeout(() => setMensaje(null), 3000)
  }

  const badgeEstado = {
    aprobado: { clase: 'badge-exito', texto: 'Aprobado' },
    en_revision: { clase: 'badge-advertencia', texto: 'En Revisión' },
    rechazado: { clase: 'badge-peligro', texto: 'Rechazado' },
    requiere_ajustes: { clase: 'badge-advertencia', texto: 'Requiere Ajustes' },
  }
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administración" usuario="Admin Sistema" notificaciones={2}>
      <div className="contenedor-gestion fade-in">
        <PageHeader
          title="Detalle del Proyecto"
          icon="folder-open"
          breadcrumb={breadcrumb}
          actions={<Link to="/admin/proyectos" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Información General" icon="info-circle">
          <div className="detalle-grid-moderno">
            <div>
              <div className="detalle-label">Nombre del Proyecto</div>
              <div className="detalle-valor">{proyecto ? proyecto.titulo : 'Sistema IoT para Agricultura de Precisión'}</div>
            </div>
            <div>
              <div className="detalle-label">Programa de Formación</div>
              <div className="detalle-valor">{proyecto?.programa || 'ADSO - Análisis y Desarrollo de Sistemas'}</div>
            </div>
            <div>
              <div className="detalle-label">Fecha de Creación</div>
              <div className="detalle-valor">{proyecto ? proyecto.fecha : '15/03/2026'}</div>
            </div>
            <div>
              <div className="detalle-label">Estado</div>
              <p><span className={`badge ${badgeEstado[estado]?.clase || 'badge-neutral'}`}>{badgeEstado[estado]?.texto || 'Sin estado'}</span></p>
            </div>
            <div>
              <div className="detalle-label">Instructor</div>
              <div className="detalle-valor">{proyecto ? proyecto.instructor : 'Carlos Ruiz'}</div>
            </div>
            <div>
              <div className="detalle-label">Ficha</div>
              <div className="detalle-valor">{proyecto?.ficha || 'Sin ficha asignada'}</div>
            </div>
            <div className="detalle-grid-full">
              <div className="detalle-label">Descripción</div>
              <div className="detalle-valor-texto">{proyecto?.descripcion || 'Sin descripción disponible'}</div>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Integrantes del Equipo" icon="users">
          <div className="detalle-grid-moderno">
            {miembros.map((m, i) => (
              <div key={i} className="flex-row team-row">
                <div>
                  <strong>{m.nombre}</strong>
                  <br /><span className="texto-claro">{m.rol}</span>
                </div>
              </div>
            ))}
          </div>
        </DataPanel>

        <div className={`mensaje-feedback mensaje-exito ${mensaje ? '' : 'oculto'} mb-md`}>
          <i className="fas fa-check-circle"></i><span>{mensaje?.texto || ''}</span>
        </div>

        <div className="acciones-finales">
          <button type="button" className="btn-primario" onClick={() => cambiarEstado('aprobado')}><i className="fas fa-check"></i> Aprobar</button>
          <button type="button" className="btn-rechazar" onClick={() => cambiarEstado('rechazado')}><i className="fas fa-times"></i> Rechazar</button>
          <button type="button" className="btn-secundario" onClick={() => cambiarEstado('requiere_ajustes')}><i className="fas fa-exclamation-circle"></i> Requiere Ajustes</button>
        </div>
      </div>
    </DashboardLayout>
  )
}
