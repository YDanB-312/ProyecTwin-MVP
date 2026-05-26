import { Link } from 'react-router-dom'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import '../../assets/styles/pages/detalle-compartido.css'
import Header from '../../components/Header/Header';
import SidebarAdmin from '../../components/SidebarAdmin/SidebarAdmin';
import FooterAdmin from '../../components/FooterAdmin/FooterAdmin';

export default function DetalleSimilitudAdmin() {
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
            <Link to="/admin/reportes">Reportes</Link>
            <span className="separador"><i className="fas fa-chevron-right"></i></span>
            <span className="actual">Detalle Similitud</span>
          </div>
          <div className="encabezado-pagina">
            <h1 className="titulo-pagina"><i className="fas fa-exclamation-triangle"></i> Detalle de Similitud</h1>
            <Link to="/admin/reportes" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Reportes</Link>
          </div>

          <section className="seccion-reporte">
            <div className="filtros-titulo"><i className="fas fa-info-circle"></i> Informacion General</div>
            <div className="tarjeta tarjeta-padded mt-20">
              <div className="flex-between mb-20">
                <div>
                  <span className="campo-label">ID Similitud</span>
                  <p className="valor-similitud-id">SIM-001</p>
                </div>
                <div>
                  <span className="campo-label">Porcentaje</span>
                  <p><span className="badge badge-peligro badge-lg">85%</span></p>
                </div>
              </div>
              <div className="detalle-grid">
                <div className="card-proyecto-compacto">
                  <h4 className="card-titulo-verde"><i className="fas fa-file-alt"></i> Proyecto 1</h4>
                  <p className="info-linea">Sistema de Gestion Academica</p>
                  <p className="detalle-linea"><strong>Creador:</strong> Juan Perez (Aprendiz)</p>
                  <p className="detalle-linea"><strong>Fecha:</strong> 15/03/2026</p>
                </div>
                <div className="card-proyecto-compacto">
                  <h4 className="card-titulo-amarillo"><i className="fas fa-file-alt"></i> Proyecto 2</h4>
                  <p className="info-linea">Plataforma Educativa SENA</p>
                  <p className="detalle-linea"><strong>Creador:</strong> Ana Martinez (Aprendiz)</p>
                  <p className="detalle-linea"><strong>Fecha:</strong> 02/02/2026</p>
                </div>
              </div>
              <div className="alerta-roja mt-20">
                <h4><i className="fas fa-list"></i> Detalles de Coincidencia</h4>
                <div className="grid-coincidencias">
                  <div className="card-coincidencia"><strong>Descripcion:</strong> 72% coincidencia</div>
                  <div className="card-coincidencia"><strong>Tecnologias:</strong> 60% coincidencia</div>
                  <div className="card-coincidencia"><strong>Objetivos:</strong> 55% coincidencia</div>
                  <div className="card-coincidencia"><strong>Metodologia:</strong> 45% coincidencia</div>
                </div>
              </div>
            </div>
          </section>

          <div className="acciones-finales mb-30">
            <button type="button" className="btn-primario"><i className="fas fa-check"></i> Resolver Similitud</button>
            <button type="button" className="btn-secundario"><i className="fas fa-robot"></i> Analizar con IA</button>
            <Link to="/admin/reportes" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver a Reportes</Link>
          </div>
        </div>
      </main>

      <FooterAdmin />
    </div>
  )
}
