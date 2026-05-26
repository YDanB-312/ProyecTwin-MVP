import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import Header from '../../components/Header/Header';
import SidebarAdmin from '../../components/SidebarAdmin/SidebarAdmin';
import FooterAdmin from '../../components/FooterAdmin/FooterAdmin';
import '../../assets/styles/pages/reportes-fallas.css'

export default function DetalleReporte() {
  return (
    <div className="modulo-admin">
      <GovernmentBar />

      <Header titulo="ProyecTwin - Panel de Administracion" usuario="Admin Sistema" notificaciones={12} />

      <SidebarAdmin />

      <main className="contenido-principal">
        <div className="contenedor-gestion">
          <div className="breadcrumb">
            <Link to="/admin/dashboard"><i className="fas fa-home"></i> Inicio</Link>
            <span className="separador"><i className="fas fa-chevron-right"></i></span>
            <Link to="/admin/reportes-fallas">Reportes de Fallas</Link>
            <span className="separador"><i className="fas fa-chevron-right"></i></span>
            <span className="actual">Detalle Reporte</span>
          </div>
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-bug"></i> Detalle del Reporte de Falla</h1>
            <Link to="/admin/reportes-fallas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>

          <section className="seccion-filtros">
            <div className="filtros-titulo"><i className="fas fa-info-circle"></i> Informacion del Reporte</div>
            <div className="detalle-grid">
              <div>
                <label className="campo-label">Titulo</label>
                <p className="campo-valor">Error al cargar el modulo de similitudes</p>
              </div>
              <div>
                <label className="campo-label">Estado</label>
                <p><span className="badge badge-advertencia">En proceso</span></p>
              </div>
              <div>
                <label className="campo-label">Reportado por</label>
                <p className="campo-valor">Maria Gonzalez (Aprendiz)</p>
              </div>
              <div>
                <label className="campo-label">Fecha de reporte</label>
                <p className="campo-valor">08/04/2026</p>
              </div>
              <div className="detalle-full">
                <label className="campo-label">Descripcion</label>
                <p className="texto-descripcion">Al intentar acceder al modulo de deteccion de similitudes desde el panel del aprendiz, el sistema muestra un error interno y no permite completar la busqueda. El error aparece despues de seleccionar el proyecto y hacer clic en "Buscar similitudes".</p>
              </div>
              <div className="detalle-full">
                <label className="campo-label">Pasos para reproducir</label>
                <p className="texto-descripcion">1. Iniciar sesion como aprendiz. 2. Ir a "Mis proyectos". 3. Seleccionar un proyecto existente. 4. Hacer clic en "Buscar similitudes". 5. El sistema muestra un error 500.</p>
              </div>
            </div>
          </section>

          <div className="tarjeta tarjeta-padded mb-30">
            <h2 className="titulo-seccion"><i className="fas fa-comments"></i> Historial de Actualizaciones</h2>
            <div className="lista-actividad">
              <div className="actividad-item borde-primario">
                <div className="flex-between">
                  <strong>Admin Principal</strong>
                  <span className="actividad-fecha">09/04/2026 10:30</span>
                </div>
                <p className="detalle-linea">He asignado el reporte al equipo de desarrollo. Se esta investigando la causa raiz del error en el modulo de similitudes.</p>
              </div>
              <div className="actividad-item borde-advertencia">
                <div className="flex-between">
                  <strong>Tecnico de Soporte</strong>
                  <span className="actividad-fecha">09/04/2026 14:15</span>
                </div>
                <p className="detalle-linea">Se identifico un problema de compatibilidad con la version de PHP en el servidor. Se esta preparando un parche correctivo.</p>
              </div>
            </div>
          </div>

          <div className="seccion-acciones acciones-finales mb-30">
            <Link to="/admin/reportes-fallas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>
          </div>
        </div>
      </main>

      <FooterAdmin />
    </div>
  )
}
