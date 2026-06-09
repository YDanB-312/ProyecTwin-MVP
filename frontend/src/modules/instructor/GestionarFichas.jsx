import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import '../../assets/styles/pages/gestion-fichas.css'

const fichas = [
  { codigo: 'ADSO-2568', nombre: 'Análisis y Desarrollo 2568', programa: 'ADSO', aprendices: 28, proyectos: 12, estado: 'Activa', badge: 'exito' },
  { codigo: 'MULT-3012', nombre: 'Multimedia 3012', programa: 'Multimedia', aprendices: 22, proyectos: 8, estado: 'Activa', badge: 'exito' },
  { codigo: 'IOT-4105', nombre: 'IoT 4105', programa: 'IoT', aprendices: 18, proyectos: 6, estado: 'Activa', badge: 'exito' },
  { codigo: 'ADSO-1980', nombre: 'Análisis y Desarrollo 1980', programa: 'ADSO', aprendices: 0, proyectos: 0, estado: 'Inactiva', badge: 'neutral' },
]

function GestionarFichas() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="gestion-fichas-container">
        <div className="contenedor-revision">

          <div className="vista-header">
            <Link to="/instructor/dashboard" className="volver-link"><i className="fas fa-arrow-left"></i> Volver al Dashboard</Link>
            <div className="vista-titulo-row">
              <h1 className="vista-titulo">Gestionar Fichas</h1>
              <span className="metrica-pill"><i className="fas fa-layer-group"></i> 4 fichas registradas</span>
              <Link to="/instructor/crear-ficha" className="btn-primario"><i className="fas fa-plus"></i> Nueva Ficha</Link>
            </div>
          </div>

          <div className="filtros-card">
            <div className="filtro-grupo">
              <label htmlFor="programa-ficha" className="filtro-label">Programa</label>
              <select id="programa-ficha" className="filtro-select" name="programa_ficha">
                <option value="">Todos los programas</option>
                <option value="adso">ADSO</option>
                <option value="multimedia">Multimedia</option>
                <option value="iot">IoT</option>
              </select>
            </div>
            <div className="filtro-grupo">
              <label htmlFor="estado-ficha" className="filtro-label">Estado</label>
              <select id="estado-ficha" className="filtro-select" name="estado_ficha">
                <option value="">Todos</option>
                <option value="activa">Activas</option>
                <option value="inactiva">Inactivas</option>
              </select>
            </div>
            <div className="filtro-grupo">
              <label htmlFor="buscar-ficha" className="filtro-label">Buscar</label>
              <div className="input-con-icono">
                <i className="fas fa-search"></i>
                <input type="text" id="buscar-ficha" className="filtro-input" placeholder="Buscar ficha..." name="buscar" />
              </div>
            </div>
            <button className="btn-limpiar"><i className="fas fa-times"></i> Limpiar</button>
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
                    <td><span className={`badge ${f.estado === 'Activa' ? 'badge-activo' : 'badge-inactivo'}`}>{f.estado}</span></td>
                    <td>
                      <div className="acciones-celda">
                        <Link to="/instructor/detalle-ficha" className="btn-ghost-tabla"><i className="fas fa-eye"></i></Link>
                        <button className="btn-ghost-tabla"><i className="fas fa-users"></i></button>
                        <button className="btn-ghost-tabla"><i className="fas fa-edit"></i></button>
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
