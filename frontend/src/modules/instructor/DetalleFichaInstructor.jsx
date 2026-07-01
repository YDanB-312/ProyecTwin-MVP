import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/fichas.css'

const miembros = [
  { iniciales: 'MG', nombre: 'Maria Gonzalez', correo: 'maria.gonzalez@soy.sena.edu.co', estado: true },
  { iniciales: 'JP', nombre: 'Juan Pérez', correo: 'juan.perez@soy.sena.edu.co', estado: true },
  { iniciales: 'LG', nombre: 'Laura Gómez', correo: 'laura.gomez@soy.sena.edu.co', estado: true },
  { iniciales: 'AM', nombre: 'Ana Martínez', correo: 'ana.martinez@soy.sena.edu.co', estado: true },
  { iniciales: 'DS', nombre: 'Diana Sánchez', correo: 'diana.sanchez@soy.sena.edu.co', estado: false },
]

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/instructor/gestionar-fichas', label: 'Gestionar Fichas' },
  { label: 'Detalle de Ficha' },
]

function DetalleFichaInstructor() {
  const [copiado, setCopiado] = useState(false)

  useEffect(() => {
    if (!copiado) return
    const timer = setTimeout(() => setCopiado(false), 2000)
    return () => clearTimeout(timer)
  }, [copiado])

  const handleCopiar = () => {
    navigator.clipboard?.writeText('ADSO-2568')
    setCopiado(true)
  }

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-pagina">
        <PageHeader
          title="Detalle de Ficha"
          icon="users"
          breadcrumb={breadcrumb}
          actions={<Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <DataPanel title="Información de la Ficha" icon="info-circle">
          <div className="ficha-info-header">
            <div>
              <h2 className="ficha-titulo">Análisis y Desarrollo 2568</h2>
              <p className="ficha-subtitulo">ADSO</p>
            </div>
            <div className="texto-derecha">
              <div className="codigo-ficha">ADSO-2568</div>
              <span className="estado-ficha-activa"><i className="fas fa-circle"></i> Activa</span>
            </div>
          </div>
        </DataPanel>

        <DataPanel title="Código de Invitación" icon="link">
          <div className="codigo-invitacion-wrap">
            <div className="codigo-grande">ADSO-2568</div>
            <p>Comparte este código con tus aprendices para que se unan a la ficha.</p>
            <button className="btn-primario btn-copy-mt" type="button" onClick={handleCopiar}><i className="fas fa-copy"></i> {copiado ? '¡Copiado!' : 'Copiar Código'}</button>
          </div>
        </DataPanel>

        <DataPanel title={`Aprendices (${miembros.length})`} icon="user-graduate">
          <div className="grid-miembros">
            {miembros.map((m, i) => (
              <div key={i} className="tarjeta-miembro">
                <div className="avatar-miembro">{m.iniciales}</div>
                <div className="info-miembro">
                  <h4>{m.nombre}</h4>
                  <p>{m.correo}</p>
                </div>
                <span className={`badge badge-${m.estado ? 'exito' : 'neutral'}`}>{m.estado ? 'Activo' : 'Inactivo'}</span>
              </div>
            ))}
          </div>
        </DataPanel>
      </div>
    </DashboardLayout>
  );
}

export default DetalleFichaInstructor;
