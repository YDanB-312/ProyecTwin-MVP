import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import FilterBar from '../../components/FilterBar/FilterBar'
import '../../assets/styles/pages/reportes-fallas.css'

const reportes = [
  { id: 1, usuario: 'Carlos Rodriguez Diaz', descripcion: 'Error al cargar la pagina de Dashboard, muestra pantalla blanca despues de iniciar sesion', estado: 'Pendiente', badge: 'advertencia', icono: 'clock', fecha: '12/04/2026' },
  { id: 2, usuario: 'Maria Gonzalez Torres', descripcion: 'No se pueden subir archivos PDF en la seccion de evidencias del proyecto', estado: 'Pendiente', badge: 'advertencia', icono: 'clock', fecha: '11/04/2026' },
  { id: 3, usuario: 'Andres Martinez Lopez', descripcion: 'El sistema no envia Notificaciones cuando un instructor revisa un proyecto', estado: 'En Proceso', badge: 'primario', icono: 'cog', fecha: '10/04/2026' },
  { id: 4, usuario: 'Laura Sanchez Perez', descripcion: 'El boton de Cerrar sesion no funciona correctamente en navegador Chrome', estado: 'En Proceso', badge: 'primario', icono: 'cog', fecha: '09/04/2026' },
  { id: 5, usuario: 'Diego Ramirez Castro', descripcion: 'Error en la generacion de reportes PDF, el archivo descargado esta corrupto', estado: 'Resuelto', badge: 'exito', icono: 'check', fecha: '08/04/2026' },
  { id: 6, usuario: 'Patricia Morales Vega', descripcion: 'Las imagenes de los logos de proyectos no se muestran en la vista de lista', estado: 'Cerrado', badge: 'neutral', icono: 'lock', fecha: '05/04/2026' },
]

export default function ReportesFallas() {
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12}>
      <div className="contenedor-gestion">
        <PageHeader title="Reportes de Fallas" icon="bug" />

        <FilterBar title="Filtros de Busqueda">
          <div className="grupo-campo">
            <label htmlFor="filtroEstado"><i className="fas fa-tag"></i> Estado</label>
            <select id="filtroEstado" name="filtro_estado">
              <option value="">Todos los estados</option>
              <option value="pendiente">Pendiente</option>
              <option value="en_proceso">En Proceso</option>
              <option value="resuelto">Resuelto</option>
              <option value="cerrado">Cerrado</option>
            </select>
          </div>
          <div className="grupo-campo">
            <label htmlFor="filtroFecha"><i className="fas fa-calendar"></i> Fecha</label>
            <input type="date" id="filtroFecha" name="filtro_fecha" />
          </div>
          <div className="grupo-campo">
            <label htmlFor="filtroUsuario"><i className="fas fa-user"></i> Buscar por usuario</label>
            <input type="text" id="filtroUsuario" placeholder="Nombre o correo del usuario" name="filtro_usuario" />
          </div>
          <div className="filter-bar-acciones" slot="actions">
            <button className="btn-primario" type="button"><i className="fas fa-search"></i> Buscar</button>
            <button className="btn-secundario" type="button"><i className="fas fa-eraser"></i> Limpiar</button>
          </div>
        </FilterBar>

        <DataPanel
          title="Listado de reportes"
          icon="list-alt"
          action={<span className="total-registros">Mostrando 6 de 45 reportes</span>}
        >
          <table className="tabla-reportes">
            <thead>
              <tr>
                <th>ID</th>
                <th>Usuario</th>
                <th>Descripcion</th>
                <th>Estado</th>
                <th>Fecha</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {reportes.map((r, i) => (
                <tr key={i}>
                  <td>{r.id}</td>
                  <td>{r.usuario}</td>
                  <td className="descripcion-corta" title={r.descripcion}>{r.descripcion}</td>
                  <td><span className={`badge badge-${r.badge}`}><i className={`fas fa-${r.icono}`}></i> {r.estado}</span></td>
                  <td>{r.fecha}</td>
                  <td>
                    <div className="acciones-tabla">
                      <Link to="/admin/detalle-reporte" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                      <button className="btn-accion-tabla btn-ver" title="Cambiar estado" type="button"><i className="fas fa-exchange-alt"></i></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="contenedor-paginacion">
            <span className="info-paginacion">Mostrando 1 - 6 de 45 reportes</span>
            <div className="paginacion">
              <button className="btn-paginacion" disabled title="Anterior" type="button"><i className="fas fa-chevron-left"></i></button>
              <button className="btn-paginacion activa" type="button">1</button>
              <button className="btn-paginacion" type="button">2</button>
              <button className="btn-paginacion" type="button">3</button>
              <button className="btn-paginacion" type="button">4</button>
              <button className="btn-paginacion" type="button">5</button>
              <button className="btn-paginacion" type="button">6</button>
              <button className="btn-paginacion" type="button">7</button>
              <button className="btn-paginacion" title="Siguiente" type="button"><i className="fas fa-chevron-right"></i></button>
            </div>
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
