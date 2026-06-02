import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/fichas.css'

const breadcrumb = [
  { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
  { to: '/instructor/gestionar-fichas', label: 'Gestionar Fichas' },
  { label: 'Crear Nueva Ficha' },
]

function CrearFicha() {
  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-pagina">
        <PageHeader
          title="Crear Nueva Ficha"
          icon="plus-circle"
          breadcrumb={breadcrumb}
          actions={<Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <div className="mensaje-feedback mensaje-exito oculto" style={{ marginBottom: 'var(--space-md)' }}>
          <i className="fas fa-check-circle"></i><span>Operacion realizada exitosamente.</span>
        </div>
        <div className="mensaje-feedback mensaje-error oculto" style={{ marginBottom: 'var(--space-md)' }}>
          <i className="fas fa-exclamation-circle"></i><span>Ha ocurrido un error. Intenta nuevamente.</span>
        </div>

        <DataPanel title="Informacion de la Ficha" icon="info-circle">
          <form action="#" className="formulario-proyecto">
            <div style={{ padding: 'var(--space-xl)' }}>
              <div className="grupo-formulario">
                <label htmlFor="codigo" className="etiqueta requerido">Codigo de Ficha</label>
                <input type="text" id="codigo" className="input-text" placeholder="Ej: ADSO-2568" required name="codigo" />
                <div className="campo-informacion">Codigo unico que identificara al grupo (ej: PROGRAMA-NUMERO).</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="id_programa" className="etiqueta requerido">Programa de Formacion</label>
                <select id="id_programa" className="select-filtro" required name="id_programa">
                  <option value="">Seleccione un programa</option>
                  <option value="1">ADSO - Analisis y Desarrollo de Sistemas</option>
                  <option value="2">Multimedia</option>
                  <option value="3">IoT</option>
                </select>
              </div>
              <div className="acciones-finales" style={{ marginTop: 'var(--space-lg)' }}>
                <button type="submit" className="btn-primario"><i className="fas fa-save"></i> Crear Ficha</button>
                <Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
              </div>
            </div>
          </form>
        </DataPanel>
      </div>
    </DashboardLayout>
  );
}

export default CrearFicha;
