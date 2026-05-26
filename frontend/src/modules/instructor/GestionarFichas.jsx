import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarInstructor from '../../components/SidebarInstructor/SidebarInstructor';
import FooterInstructor from '../../components/FooterInstructor/FooterInstructor';
import '../../assets/styles/pages/fichas.css';

function GestionarFichas() {
  return (
    <div className="modulo-instructor">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8} />
      <SidebarInstructor />
      <main className="contenido-principal">
        <div className="contenedor-pagina">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-users"></i> Gestionar Fichas</h1>
            <Link to="/instructor/crear-ficha" className="btn-primario"><i className="fas fa-plus"></i> Nueva Ficha</Link>
          </div>
          <section className="seccion-filtros">
            <div className="contenedor-filtros">
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
                <input type="text" id="buscar-ficha" className="input-text" placeholder="Código o nombre..." name="buscar" />
              </div>
            </div>
          </section>
          <div className="tarjeta tarjeta-padded mb-30">
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
                  <tr>
                    <td><span className="codigo-ficha">ADSO-2568</span></td>
                    <td><strong>Análisis y Desarrollo 2568</strong></td>
                    <td>ADSO</td>
                    <td>28</td>
                    <td>12</td>
                    <td><span className="badge badge-exito">Activa</span></td>
                    <td><Link to="/instructor/detalle-ficha" className="btn-ver"><i className="fas fa-eye"></i></Link></td>
                  </tr>
                  <tr>
                    <td><span className="codigo-ficha">MULT-3012</span></td>
                    <td><strong>Multimedia 3012</strong></td>
                    <td>Multimedia</td>
                    <td>22</td>
                    <td>8</td>
                    <td><span className="badge badge-exito">Activa</span></td>
                    <td><Link to="/instructor/detalle-ficha" className="btn-ver"><i className="fas fa-eye"></i></Link></td>
                  </tr>
                  <tr>
                    <td><span className="codigo-ficha">IOT-4105</span></td>
                    <td><strong>IoT 4105</strong></td>
                    <td>IoT</td>
                    <td>18</td>
                    <td>6</td>
                    <td><span className="badge badge-exito">Activa</span></td>
                    <td><Link to="/instructor/detalle-ficha" className="btn-ver"><i className="fas fa-eye"></i></Link></td>
                  </tr>
                  <tr>
                    <td><span className="codigo-ficha">ADSO-1980</span></td>
                    <td><strong>Análisis y Desarrollo 1980</strong></td>
                    <td>ADSO</td>
                    <td>0</td>
                    <td>0</td>
                    <td><span className="badge badge-neutral">Inactiva</span></td>
                    <td><Link to="/instructor/detalle-ficha" className="btn-ver"><i className="fas fa-eye"></i></Link></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </main>
      <FooterInstructor />
    </div>
  );
}

export default GestionarFichas;
