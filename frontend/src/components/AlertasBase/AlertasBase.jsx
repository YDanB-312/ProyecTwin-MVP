import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../DashboardLayout/DashboardLayout'
import PageHeader from '../PageHeader/PageHeader'
import '../../assets/styles/pages/alertas.css'
import Pagination from '../Pagination/Pagination'

const badgeClaseTipo = {
  similitud: 'badge-similitud',
  revision: 'badge-revision',
  mensaje: 'badge-mensaje',
  sistema: 'badge-sistema',
}

const ITEMS_PER_PAGE = 4

export default function AlertasBase({
  role, dashboardTitulo, dashboardUsuario, notificaciones,
  breadcrumb,
  filters,
  notificacionesData,
  volverPath,
  iconoMarca = 'fa-check',
}) {
  const filterStateNames = filters.map(f => f.name)
  const [filtroVals, setFiltroVals] = useState(Object.fromEntries(filterStateNames.map(n => [n, ''])))
  const [leidas, setLeidas] = useState(notificacionesData.map(n => n.leida))
  const [paginaActual, setPaginaActual] = useState(1)

  const setFiltro = (name, value) => {
    setFiltroVals(prev => ({ ...prev, [name]: value }))
    setPaginaActual(1)
  }

  const marcarLeida = (i) => {
    setLeidas(prev => {
      const next = [...prev]
      next[i] = true
      return next
    })
  }

  const marcarTodasLeidas = () => {
    setLeidas(prev => prev.map(() => true))
  }

  const notificacionesFiltradas = notificacionesData.filter((n) => {
    for (const [name, value] of Object.entries(filtroVals)) {
      if (value !== '' && n[name] !== value) return false
    }
    return true
  })

  const notificacionesPagina = notificacionesFiltradas.slice((paginaActual - 1) * ITEMS_PER_PAGE, paginaActual * ITEMS_PER_PAGE)
  const indicesPagina = notificacionesPagina.map(n => notificacionesData.indexOf(n))

  return (
    <DashboardLayout role={role} titulo={dashboardTitulo} usuario={dashboardUsuario} notificaciones={notificaciones}>
      <div className="contenedor-alertas fade-in">
        <PageHeader
          title="Notificaciones"
          icon="bell"
          breadcrumb={breadcrumb}
          actions={<><Link to={volverPath} className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link><button className="btn-marcar-todas" type="button" onClick={marcarTodasLeidas}><i className="fas fa-check-double"></i> Marcar todas como leídas</button></>}
        />

        <div className="filtros-card">
          {filters.map((f, i) => (
            <div className="grupo-filtro" key={i}>
              <label htmlFor={f.id}>{f.label}</label>
              <select id={f.id} className="campo-select" name={f.name} value={filtroVals[f.name] || ''} onChange={(e) => setFiltro(f.name, e.target.value)}>
                {f.options.map((o, j) => (
                  <option key={j} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>
          ))}
        </div>

        <div className="lista-notificaciones">
          {notificacionesPagina.length === 0 ? (
            <div className="estado-vacio-moderno"><div className="estado-vacio-icono"><i className="fas fa-bell-slash"></i></div><h3 className="estado-vacio-titulo">No hay notificaciones</h3><p className="estado-vacio-descripcion">No se encontraron notificaciones que coincidan con los filtros.</p></div>
          ) : notificacionesPagina.map((n, idx) => {
            const i = indicesPagina[idx]
            return (
            <div className="notificacion-card" key={i}>
              <div className={`notificacion-icono${n.iconoClase ? ' ' + n.iconoClase : ''}`}><i className={`fas fa-${n.icono}`}></i></div>
              <div className="notificacion-cuerpo">
                <div className="notificacion-fila-superior">
                  <h3 className="notificacion-titulo">{n.titulo}</h3>
                  <span className={leidas[i] ? 'badge-leida' : 'badge-no-leida'}>{leidas[i] ? 'Leída' : 'No leída'}</span>
                </div>
                <p className="notificacion-descripcion">{n.descripcion}</p>
                <div className="notificacion-fila-inferior">
                  <div className="notificacion-metas">
                    <span className="notificacion-tiempo">{n.tiempo}</span>
                    <span className="notificacion-proyecto">{n.proyecto}</span>
                    <span className={`badge-estado ${badgeClaseTipo[n.tipo] || ''}`}>{n.tipoLabel || n.tipo}</span>
                  </div>
                  <div className="notificacion-acciones">
                    <Link to={{ pathname: n.enlace, state: n.state || {} }} className="btn-accion"><i className={`fas fa-${n.iconoEnlace}`}></i> {n.textoEnlace}</Link>
                    <button className="btn-accion-secundaria" type="button" onClick={() => marcarLeida(i)} disabled={leidas[i]}><i className={`fas ${iconoMarca}`}></i> Marcar como leída</button>
                  </div>
                </div>
              </div>
            </div>
            )
          })}
        </div>

        <Pagination totalItems={notificacionesFiltradas.length} itemsPerPage={ITEMS_PER_PAGE} paginaActual={paginaActual} setPaginaActual={setPaginaActual} />

      </div>
    </DashboardLayout>
  )
}
