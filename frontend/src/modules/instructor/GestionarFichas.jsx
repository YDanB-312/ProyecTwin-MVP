import { useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/gestion-fichas.css'

const fichas = [
  { codigo: 'ADSO-2568', nombre: 'Analisis y Desarrollo 2568', programa: 'ADSO', aprendices: 28, proyectos: 5, estado: true },
  { codigo: 'ADSO-2634', nombre: 'Analisis y Desarrollo 2634', programa: 'ADSO', aprendices: 25, proyectos: 3, estado: true },
  { codigo: 'MM-3102', nombre: 'Produccion Multimedia 3102', programa: 'Produccion Multimedia', aprendices: 22, proyectos: 4, estado: true },
  { codigo: 'IR-2801', nombre: 'Infraestructura Redes 2801', programa: 'Infraestructura Redes', aprendices: 20, proyectos: 0, estado: false },
]

function GestionarFichas() {
  const [filtroPrograma, setFiltroPrograma] = useState('')
  const [filtroEstado, setFiltroEstado] = useState('')
  const [busqueda, setBusqueda] = useState('')

  const limpiarFiltros = () => {
    setFiltroPrograma('')
    setFiltroEstado('')
    setBusqueda('')
  }

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="gestion-fichas-container">
        <div className="contenedor-revision">

          <div className="vista-header">
            <Link to="/instructor/dashboard" className="volver-link"><i className="fas fa-arrow-left"></i> Volver al Dashboard</Link>
            <div className="vista-titulo-row">
              <h1 className="vista-titulo">Gestionar Fichas</h1>
              <span className="metrica-pill"><i className="fas fa-layer-group"></i> {fichas.length} fichas registradas</span>
              <Link to="/instructor/crear-ficha" className="btn-primario"><i className="fas fa-plus"></i> Nueva Ficha</Link>
            </div>
          </div>

          <div className="filtros-card">
            <div className="filtro-grupo">
              <label htmlFor="programa-ficha" className="filtro-label">Programa</label>
              <select id="programa-ficha" className="filtro-select" name="programa_ficha" value={filtroPrograma} onChange={(e) => setFiltroPrograma(e.target.value)}>
                <option value="">Todos los programas</option>
                <option value="adso">ADSO</option>
                <option value="multimedia">Multimedia</option>
                <option value="iot">IoT</option>
              </select>
            </div>
            <div className="filtro-grupo">
              <label htmlFor="estado-ficha" className="filtro-label">Estado</label>
              <select id="estado-ficha" className="filtro-select" name="estado_ficha" value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
                <option value="">Todos</option>
                <option value="activa">Activas</option>
                <option value="inactiva">Inactivas</option>
              </select>
            </div>
            <div className="filtro-grupo">
              <label htmlFor="buscar-ficha" className="filtro-label">Buscar</label>
              <div className="input-con-icono">
                <i className="fas fa-search"></i>
                <input type="text" id="buscar-ficha" className="filtro-input" placeholder="Buscar ficha..." name="buscar" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} />
              </div>
            </div>
            <button className="btn-limpiar" type="button" onClick={limpiarFiltros}><i className="fas fa-times"></i> Limpiar</button>
          </div>

          <div className="tabla-container">
            <table className="tabla-premium">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Nombre</th>
                  <th>Programa</th>
                  <th>Aprendices</th>
                  <th>Proyectos</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {fichas.map((f, i) => (
                  <tr key={i}>
                    <td><span className="codigo-cell">{f.codigo}</span></td>
                    <td className="nombre-cell">{f.nombre}</td>
                    <td>{f.programa}</td>
                    <td>{f.aprendices}</td>
                    <td>{f.proyectos}</td>
                    <td><span className={`badge ${f.estado ? 'badge-exito' : 'badge-neutral'}`}>{f.estado ? 'Activa' : 'Inactiva'}</span></td>
                    <td>
                      <div className="acciones-celda">
                        <Link to="/instructor/detalle-ficha" className="btn-ghost-tabla"><i className="fas fa-eye"></i></Link>
                        <Link to="/instructor/directorio-ficha" className="btn-ghost-tabla"><i className="fas fa-users"></i></Link>
                        <button className="btn-ghost-tabla" type="button" onClick={() => {}}><i className="fas fa-edit"></i></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

        </div>
      </div>
    </DashboardLayout>
  )
}

export default GestionarFichas
