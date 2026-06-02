import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/dashboard-admin.css'

export default function DashboardAdmin() {
  return (
    <DashboardLayout role="admin" titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12}>
      <div className="contenedor-gestion">
        <div className="tarjeta-bienvenida-moderna">
          <div className="bienvenida-contenido">
            <h1>Bienvenido al Panel de Administrador!</h1>
            <p>Gestiona usuarios, programas de formacion y mantén el control total del sistema ProyecTwin. Revisa reportes y asegura el correcto funcionamiento de la plataforma.</p>
          </div>
          <div className="bienvenida-ilustracion">
            <i className="fas fa-shield-alt"></i>
          </div>
        </div>

        <PageHeader title="Acciones Rapidas" icon="bolt" />

        <div className="acciones-grid-moderna">
          <div className="tarjeta-accion-moderna">
            <div className="accion-icono"><i className="fas fa-users"></i></div>
            <h3>Usuarios Registrados</h3>
            <p>Consulta y administra aprendices, instructores y administradores</p>
            <Link to="/admin/gestion-usuarios" className="btn-accion-principal"><i className="fas fa-eye"></i> Ver Usuarios</Link>
          </div>
          <div className="tarjeta-accion-moderna">
            <div className="accion-icono"><i className="fas fa-chart-bar"></i></div>
            <h3>Generar Reportes</h3>
            <p>Visualiza estadisticas y exporta informes</p>
            <Link to="/admin/reportes" className="btn-accion-principal"><i className="fas fa-chart-bar"></i> Ver Reportes</Link>
          </div>
          <div className="tarjeta-accion-moderna">
            <div className="accion-icono"><i className="fas fa-bug"></i></div>
            <h3>Reportes de Fallas</h3>
            <p>Revisa y gestiona los reportes de errores del sistema</p>
            <Link to="/admin/reportes-fallas" className="btn-accion-principal"><i className="fas fa-wrench"></i> Ver Reportes</Link>
          </div>
        </div>

        <DataPanel
          title="Proyectos Recientes"
          icon="folder-open"
          action={<Link to="/admin/reportes" className="enlace-ver-todo">Ver todos <i className="fas fa-arrow-right"></i></Link>}
        >
          <div className="contenedor-tabla">
            <table className="tabla-usuarios">
              <thead>
                <tr>
                  <th>Proyecto</th>
                  <th>Creador</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>
                    <div className="info-usuario">
                      <div className="avatar-tabla"><i className="fas fa-microchip"></i></div>
                      <div>
                        <strong>Sistema IoT para Agricultura</strong>
                        <span className="telefono-usuario">Monitoreo inteligente de cultivos</span>
                      </div>
                    </div>
                  </td>
                  <td>Ana Martinez</td>
                  <td>15 Nov 2023</td>
                  <td><span className="badge badge-exito"><i className="fas fa-circle"></i> Activo</span></td>
                  <td>
                    <div className="acciones-tabla">
                      <Link to="/admin/detalle-proyecto" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td>
                    <div className="info-usuario">
                      <div className="avatar-tabla"><i className="fas fa-mobile-alt"></i></div>
                      <div>
                        <strong>App Movil para Turismo Local</strong>
                        <span className="telefono-usuario">Promocion turistica regional</span>
                      </div>
                    </div>
                  </td>
                  <td>Juan Perez</td>
                  <td>14 Nov 2023</td>
                  <td><span className="badge badge-advertencia"><i className="fas fa-circle"></i> Pendiente</span></td>
                  <td>
                    <div className="acciones-tabla">
                      <Link to="/admin/detalle-proyecto" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td>
                    <div className="info-usuario">
                      <div className="avatar-tabla"><i className="fas fa-laptop-code"></i></div>
                      <div>
                        <strong>Plataforma de Gestion Academica</strong>
                        <span className="telefono-usuario">Sistema integral de gestion</span>
                      </div>
                    </div>
                  </td>
                  <td>Carlos Rodriguez</td>
                  <td>13 Nov 2023</td>
                  <td><span className="badge badge-exito"><i className="fas fa-circle"></i> Activo</span></td>
                  <td>
                    <div className="acciones-tabla">
                      <Link to="/admin/detalle-proyecto" className="btn-accion-tabla btn-ver" title="Ver detalle"><i className="fas fa-eye"></i></Link>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </DataPanel>

        <DataPanel
          title="Notificaciones Recientes"
          icon="exclamation-triangle"
          action={<Link to="/admin/reportes" className="enlace-ver-todo">Ver todas <i className="fas fa-arrow-right"></i></Link>}
        >
          <div className="lista-alertas-moderna">
            <div className="alerta-card urgente">
              <div className="alerta-card-icono"><i className="fas fa-exclamation-triangle"></i></div>
              <div className="alerta-card-contenido">
                <div className="alerta-card-titulo">Alta Similitud Detectada</div>
                <div className="alerta-card-descripcion"><p>Proyecto "Sistema de Gestion Academica" tiene 65% de similitud con otro proyecto registrado</p></div>
                <div className="alerta-card-meta">
                  <span className="alerta-card-fecha"><i className="fas fa-clock"></i> Hace 2 horas</span>
                  <Link to="/admin/detalle-similitud" className="btn-alerta-accion"><i className="fas fa-search"></i> Revisar</Link>
                </div>
              </div>
            </div>
            <div className="alerta-card advertencia">
              <div className="alerta-card-icono"><i className="fas fa-bug"></i></div>
              <div className="alerta-card-contenido">
                <div className="alerta-card-titulo">Reporte de Falla - Error en Sistema</div>
                <div className="alerta-card-descripcion"><p>Usuario reporta error al intentar acceder al modulo de proyectos</p></div>
                <div className="alerta-card-meta">
                  <span className="alerta-card-fecha"><i className="fas fa-clock"></i> Hace 5 horas</span>
                  <Link to="/admin/detalle-reporte" className="btn-alerta-accion"><i className="fas fa-wrench"></i> Gestionar</Link>
                </div>
              </div>
            </div>
            <div className="alerta-card informativa">
              <div className="alerta-card-icono"><i className="fas fa-user-plus"></i></div>
              <div className="alerta-card-contenido">
                <div className="alerta-card-titulo">Nuevo Usuario Registrado</div>
                <div className="alerta-card-descripcion"><p>Se ha registrado un nuevo aprendiz en el sistema</p></div>
                <div className="alerta-card-meta">
                  <span className="alerta-card-fecha"><i className="fas fa-clock"></i> Hace 1 dia</span>
                  <Link to="/admin/gestion-usuarios" className="btn-alerta-accion"><i className="fas fa-user-check"></i> Verificar</Link>
                </div>
              </div>
            </div>
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  )
}
