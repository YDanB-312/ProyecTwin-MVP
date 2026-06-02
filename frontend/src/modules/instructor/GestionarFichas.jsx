import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import FilterBar from '../../components/FilterBar/FilterBar'
import '../../assets/styles/pages/fichas.css'

const fichas = [
  { codigo: 'ADSO-2568', nombre: 'Análisis y Desarrollo 2568', programa: 'ADSO', aprendices: 28, proyectos: 12, estado: 'Activa', badge: 'exito' },
  { codigo: 'MULT-3012', nombre: 'Multimedia 3012', programa: 'Multimedia', aprendices: 22, proyectos: 8, estado: 'Activa', badge: 'exito' },
  { codigo: 'IOT-4105', nombre: 'IoT 4105', programa: 'IoT', aprendices: 18, proyectos: 6, estado: 'Activa', badge: 'exito' },
  { codigo: 'ADSO-1980', nombre: 'Análisis y Desarrollo 1980', programa: 'ADSO', aprendices: 0, proyectos: 0, estado: 'Inactiva', badge: 'neutral' },
]

function GestionarFichas() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-pagina">
        <PageHeader
          title="Gestionar Fichas"
          icon="users"
          actions={<Link to="/instructor/crear-ficha" className="btn-primario"><i className="fas fa-plus"></i> Nueva Ficha</Link>}
        />

        <FilterBar title="Filtrar Fichas">
          <div className="grupo-filtro">
            <label htmlFor="programa-ficha">Programa</label>
            <select id="programa-ficha" className="select-filtro" name="programa_ficha">
              <option value="">Todos los programas</option>
              <option value="adso">ADSO</option>
              <option value="multimedia">Multimedia</option>
              <option value="iot">IoT</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="estado-ficha">Estado</label>
            <select id="estado-ficha" className="select-filtro" name="estado_ficha">
              <option value="">Todos</option>
              <option value="activa">Activas</option>
              <option value="inactiva">Inactivas</option>
            </select>
          </div>
          <div className="grupo-filtro">
            <label htmlFor="buscar-ficha">Buscar</label>
            <input type="text" id="buscar-ficha" className="input-filtro" placeholder="Código o nombre..." name="buscar" />
          </div>
        </FilterBar>

        <DataPanel title="Fichas Registradas" icon="table">
          <div className="tabla-scroll">
            <table className="tabla-fichas">
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
                    <td><span className="codigo-ficha">{f.codigo}</span></td>
                    <td><strong>{f.nombre}</strong></td>
                    <td>{f.programa}</td>
                    <td>{f.aprendices}</td>
                    <td>{f.proyectos}</td>
                    <td><span className={`badge badge-${f.badge}`}>{f.estado}</span></td>
                    <td><Link to="/instructor/detalle-ficha" className="btn-ver"><i className="fas fa-eye"></i></Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  );
}

export default GestionarFichas;
