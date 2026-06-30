import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/directorio.css'

const aprendices = [
  { iniciales: 'AM', nombre: 'Ana Martínez', info: 'Activo - Última conexión: Hoy 14:30', clase: '' },
  { iniciales: 'JP', nombre: 'Juan Pérez', info: 'Activo - Última conexión: Hoy 11:15', clase: 'avatar-secundario' },
  { iniciales: 'LG', nombre: 'Laura Gómez', info: 'Activo - Última conexión: Ayer', clase: 'avatar-secundario' },
  { iniciales: 'DS', nombre: 'Diana Sánchez', info: 'Inactivo - Última conexión: 05/05/2026', clase: 'avatar-secundario' },
]

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/instructor/gestionar-fichas', label: 'Gestionar Fichas' },
  { label: 'Directorio de Ficha' },
]

function DirectorioFichaInstructor() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-pagina">
        <PageHeader
          title="Directorio de Ficha"
          icon="address-book"
          breadcrumb={breadcrumb}
          actions={<Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <div className="info-ficha-actual mb-30">
          <div className="ficha-detalle">
            <h3>ADSO-2568</h3>
            <p>Análisis y Desarrollo de Sistemas</p>
          </div>
          <span className="badge badge-primario"><i className="fas fa-users"></i> {aprendices.length} aprendices</span>
        </div>

        <DataPanel title={`Aprendices (${aprendices.length})`} icon="user-graduate">
          <div className="directorio-usuarios">
            {aprendices.map((a, i) => (
              <div key={i} className="tarjeta-usuario">
                <div className={`avatar-usuario ${a.clase}`}>{a.iniciales}</div>
                <div className="info-usuario">
                  <h4>{a.nombre}</h4>
                  <p>{a.info}</p>
                </div>
              </div>
            ))}
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  );
}

export default DirectorioFichaInstructor;
