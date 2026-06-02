import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/fichas.css'

const miembros = [
  { iniciales: 'MG', nombre: 'Maria Gonzalez', correo: 'maria.gonzalez@soy.sena.edu.co', estado: 'Activo', badge: 'exito' },
  { iniciales: 'JP', nombre: 'Juan Perez', correo: 'juan.perez@soy.sena.edu.co', estado: 'Activo', badge: 'exito' },
  { iniciales: 'LG', nombre: 'Laura Gomez', correo: 'laura.gomez@soy.sena.edu.co', estado: 'Activo', badge: 'exito' },
  { iniciales: 'AM', nombre: 'Ana Martinez', correo: 'ana.martinez@soy.sena.edu.co', estado: 'Activo', badge: 'exito' },
  { iniciales: 'DS', nombre: 'Diana Sanchez', correo: 'diana.sanchez@soy.sena.edu.co', estado: 'Inactivo', badge: 'neutral' },
]

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/instructor/gestionar-fichas', label: 'Gestionar Fichas' },
  { label: 'Detalle de Ficha' },
]

function DetalleFichaInstructor() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-pagina">
        <PageHeader
          title="Detalle de Ficha"
          icon="users"
          breadcrumb={breadcrumb}
          actions={<Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Informacion de la Ficha" icon="info-circle">
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: 'var(--space-lg) var(--space-xl)', alignItems: 'center' }}>
            <div>
              <h2 className="ficha-titulo">Analisis y Desarrollo 2568</h2>
              <p className="ficha-subtitulo">ADSO - Trimestre 3</p>
            </div>
            <div className="texto-derecha" style={{ textAlign: 'right' }}>
              <div className="codigo-ficha">ADSO-2568</div>
              <span className="estado-ficha-activa"><i className="fas fa-circle"></i> Activa</span>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Codigo de Invitacion" icon="link">
          <div style={{ padding: 'var(--space-xl)', textAlign: 'center' }}>
            <div className="codigo-grande" style={{ fontSize: '2rem', fontWeight: 700, letterSpacing: '4px', padding: 'var(--space-lg)', background: 'var(--color-fondo)', borderRadius: 'var(--radio-lg)', display: 'inline-block', marginBottom: '12px' }}>ADSO-2568</div>
            <p style={{ color: 'var(--color-texto-secundario)' }}>Comparte este codigo con tus aprendices para que se unan a la ficha.</p>
            <button className="btn-primario" type="button" style={{ marginTop: '12px' }}><i className="fas fa-copy"></i> Copiar Codigo</button>
          </div>
        </DataPanel>

        <DataPanel title="Aprendices (28)" icon="user-graduate">
          <div className="grid-miembros" style={{ padding: 'var(--space-xl)' }}>
            {miembros.map((m, i) => (
              <div key={i} className="tarjeta-miembro">
                <div className="avatar-miembro">{m.iniciales}</div>
                <div className="info-miembro">
                  <h4>{m.nombre}</h4>
                  <p>{m.correo}</p>
                </div>
                <span className={`badge badge-${m.badge}`}>{m.estado}</span>
              </div>
            ))}
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  );
}

export default DetalleFichaInstructor;
