import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/fichas.css'

function DetalleFicha() {
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-pagina">

        <PageHeader
          title="Mi Ficha"
          icon="users"
          breadcrumb={[
            { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
            { label: 'Mi Ficha' }
          ]}
        />

        <div className="ficha-card">
          <div className="ficha-grid">
            <div className="ficha-dato">
              <span className="ficha-label">Programa</span>
              <span className="ficha-valor">Análisis y Desarrollo 2568</span>
            </div>
            <div className="ficha-dato">
              <span className="ficha-label">Codigo</span>
              <span className="ficha-valor"><span className="codigo-ficha">ADSO-2568</span></span>
            </div>
            <div className="ficha-dato">
              <span className="ficha-label">Estado</span>
              <span className="badge-activo"><i className="fas fa-circle"></i> Activa</span>
            </div>
            <div className="ficha-dato ficha-dato-full">
              <span className="ficha-label">Instructor</span>
              <div className="instructor-info">
                <div className="instructor-avatar">CR</div>
                <div>
                  <span className="ficha-valor">Carlos Ruiz</span>
                  <span className="instructor-area">Tecnologías de la Información</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="seccion-header">
          <h2 className="seccion-titulo">Companeros</h2>
          <span className="contador-pill">5</span>
        </div>

        <div className="companeros-grid">

          <div className="companero-card">
            <div className="companero-avatar">MG</div>
            <div className="companero-info">
              <span className="companero-nombre">Maria Gonzalez</span>
              <span className="companero-correo">maria.gonzalez@soy.sena.edu.co</span>
            </div>
            <span className="badge-activo">Activo</span>
          </div>

          <div className="companero-card">
            <div className="companero-avatar">JP</div>
            <div className="companero-info">
              <span className="companero-nombre">Juan Perez</span>
              <span className="companero-correo">juan.perez@soy.sena.edu.co</span>
            </div>
            <span className="badge-activo">Activo</span>
          </div>

          <div className="companero-card">
            <div className="companero-avatar">LG</div>
            <div className="companero-info">
              <span className="companero-nombre">Laura Gomez</span>
              <span className="companero-correo">laura.gomez@soy.sena.edu.co</span>
            </div>
            <span className="badge-activo">Activo</span>
          </div>

          <div className="companero-card">
            <div className="companero-avatar">AM</div>
            <div className="companero-info">
              <span className="companero-nombre">Ana Martinez</span>
              <span className="companero-correo">ana.martinez@soy.sena.edu.co</span>
            </div>
            <span className="badge-activo">Activo</span>
          </div>

          <div className="companero-card">
            <div className="companero-avatar">DS</div>
            <div className="companero-info">
              <span className="companero-nombre">Diana Sanchez</span>
              <span className="companero-correo">diana.sanchez@soy.sena.edu.co</span>
            </div>
            <span className="badge-inactivo">Inactivo</span>
          </div>

        </div>
      </div>
    </DashboardLayout>
  )
}

export default DetalleFicha
