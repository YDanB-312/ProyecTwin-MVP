import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/directorio.css'

const aprendices = [
  { iniciales: 'AM', nombre: 'Ana Martinez', info: 'Activo - Ultima conexion: Hoy 14:30', clase: '' },
  { iniciales: 'JP', nombre: 'Juan Perez', info: 'Activo - Ultima conexion: Hoy 11:15', clase: 'avatar-secundario' },
  { iniciales: 'LG', nombre: 'Laura Gomez', info: 'Activo - Ultima conexion: Ayer', clase: 'avatar-secundario' },
  { iniciales: 'DS', nombre: 'Diana Sanchez', info: 'Inactivo - Ultima conexion: 05/05/2026', clase: 'avatar-secundario' },
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

        <div className="info-ficha-actual mb-30" style={{ padding: 'var(--space-lg) var(--space-xl)', background: 'var(--color-superficie)', borderRadius: 'var(--radio-xl)', boxShadow: 'var(--sombra-sm)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-lg)' }}>
          <div className="ficha-detalle">
            <h3>ADSO-2568</h3>
            <p>Analisis y Desarrollo de Sistemas - Trimestre 3</p>
          </div>
          <span className="badge badge-primario"><i className="fas fa-users"></i> 18 aprendices</span>
        </div>

        <DataPanel title="Aprendices (18)" icon="user-graduate">
          <div className="directorio-usuarios" style={{ padding: 'var(--space-xl)' }}>
            {aprendices.map((a, i) => (
              <div key={i} className="tarjeta-usuario" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-md)', padding: 'var(--space-md) 0', borderBottom: i < aprendices.length - 1 ? '1px solid var(--color-borde)' : 'none' }}>
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
