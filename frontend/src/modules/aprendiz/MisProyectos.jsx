// MisProyectos - Pagina de listado de proyectos del aprendiz con filtros y acciones de gestion
import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar';
import Header from '../../components/Header/Header';
import SidebarAprendiz from '../../components/SidebarAprendiz/SidebarAprendiz';
import FooterAprendiz from '../../components/FooterAprendiz/FooterAprendiz';
import '../../assets/styles/pages/mis-proyectos.css';

function MisProyectos() {
  return (
    <div className="modulo-aprendiz">
      <GovernmentBar />
      <Header titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5} />
      <SidebarAprendiz />
      <main className="contenido-principal">
        <div className="contenedor-proyectos">
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-folder-open"></i> Mis proyectos</h1>
            <Link to="/aprendiz/nuevo-proyecto" className="btn-primario"><i className="fas fa-plus"></i> Nuevo Proyecto</Link>
          </div>
          <section className="seccion-filtros">
            <h3 className="filtros-titulo"><i className="fas fa-filter"></i> Filtros de Busqueda</h3>
            <div className="contenedor-filtros">
              <div className="grupo-filtro">
                <label htmlFor="estado">Estado del proyecto</label>
                <select id="estado" className="select-filtro" name="estado">
                  <option value="">Todos los estados</option>
                  <option value="borrador">Borrador</option>
                  <option value="Revision">En Revision</option>
                  <option value="aprobado">Aprobado</option>
                  <option value="rechazado">Rechazado</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="fecha">Fecha de Creacion</label>
                <select id="fecha" className="select-filtro" name="fecha">
                  <option value="">Cualquier fecha</option>
                  <option value="ultima-semana">Ultima semana</option>
                  <option value="ultimo-mes">Ultimo mes</option>
                  <option value="ultimo-trimestre">Ultimo trimestre</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="instructor">Instructor</label>
                <select id="instructor" className="select-filtro" name="instructor">
                  <option value="">Todos los instructores</option>
                  <option value="ruiz">Carlos Ruiz</option>
                  <option value="gomez">Ana Gomez</option>
                  <option value="lopez">Miguel Lopez</option>
                </select>
              </div>
              <div className="grupo-filtro">
                <label htmlFor="busqueda">Buscar por nombre</label>
                <input type="text" id="busqueda" className="input-filtro" placeholder="Nombre del proyecto..." name="busqueda" />
              </div>
            </div>
          </section>
          <section className="lista-proyectos">
            <div className="tarjeta-proyecto">
              <div className="info-proyecto">
                <h3><i className="fas fa-file-alt"></i> Sistema de Gestion Academica</h3>
                <p><i className="fas fa-user-tie"></i> Instructor: Carlos Ruiz &middot; <i className="fas fa-calendar"></i> Creado: 15/03/2023</p>
              </div>
              <span className="equipo-badge"><i className="fas fa-users"></i> <span className="contador-equipo">3</span></span>
              <span className="estado-proyecto estado-revision">En Revision</span>
              <div className="acciones-proyecto">
                <Link to="/aprendiz/detalle-proyecto" className="btn-secundario btn-sm" aria-label="Ver detalles"><i className="fas fa-eye"></i> Ver</Link>
                <Link to="/aprendiz/nuevo-proyecto" className="btn-secundario btn-sm" aria-label="Editar proyecto"><i className="fas fa-edit"></i> Editar</Link>
              </div>
            </div>
            <div className="tarjeta-proyecto">
              <div className="info-proyecto">
                <h3><i className="fas fa-file-alt"></i> Aplicacion Web de Inventarios</h3>
                <p><i className="fas fa-user-tie"></i> Instructor: Ana Gomez &middot; <i className="fas fa-calendar"></i> Creado: 22/04/2023</p>
              </div>
              <span className="equipo-badge"><i className="fas fa-users"></i> <span className="contador-equipo">2</span></span>
              <span className="estado-proyecto estado-aprobado">Aprobado</span>
              <div className="acciones-proyecto">
                <Link to="/aprendiz/detalle-proyecto" className="btn-secundario btn-sm" aria-label="Ver detalles"><i className="fas fa-eye"></i> Ver</Link>
              </div>
            </div>
            <div className="tarjeta-proyecto">
              <div className="info-proyecto">
                <h3><i className="fas fa-file-alt"></i> Plataforma E-Learning</h3>
                <p><i className="fas fa-user-tie"></i> Instructor: Miguel Lopez &middot; <i className="fas fa-calendar"></i> Creado: 10/05/2023</p>
              </div>
              <span className="equipo-badge"><i className="fas fa-users"></i> <span className="contador-equipo">4</span></span>
              <span className="estado-proyecto estado-pendiente">Borrador</span>
              <div className="acciones-proyecto">
                <Link to="/aprendiz/detalle-proyecto" className="btn-secundario btn-sm" aria-label="Ver detalles"><i className="fas fa-eye"></i> Ver</Link>
                <Link to="/aprendiz/nuevo-proyecto" className="btn-secundario btn-sm" aria-label="Editar proyecto"><i className="fas fa-edit"></i> Editar</Link>
              </div>
            </div>
          </section>
          <div className="estado-vacio oculto">
            <div className="icono-vacio"><i className="fas fa-folder-open"></i></div>
            <h3>No tienes proyectos registrados</h3>
            <p>Crea tu primer proyecto para comenzar a trabajar en equipo.</p>
            <Link to="/aprendiz/nuevo-proyecto" className="btn-primario"><i className="fas fa-plus"></i> Nuevo proyecto</Link>
          </div>
          <div className="paginacion">
            <button className="btn-paginacion" disabled type="button"><i className="fas fa-chevron-left"></i></button>
            <button className="btn-paginacion activa" type="button">1</button>
            <button className="btn-paginacion" type="button">2</button>
            <button className="btn-paginacion" type="button">3</button>
            <button className="btn-paginacion" type="button"><i className="fas fa-chevron-right"></i></button>
          </div>
        </div>
      </main>
      <FooterAprendiz />
    </div>
  );
}

export default MisProyectos;
