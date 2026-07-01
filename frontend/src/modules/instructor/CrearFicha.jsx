import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
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
  const { register, handleSubmit, formState: { errors }, reset } = useForm()
  const navigate = useNavigate()

  const onSubmit = () => {
    navigate('/instructor/gestionar-fichas')
    reset()
  }

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-pagina">
        <PageHeader
          title="Crear Nueva Ficha"
          icon="plus-circle"
          breadcrumb={breadcrumb}
          actions={<Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <div className="mensaje-feedback mensaje-exito oculto mb-md">
          <i className="fas fa-check-circle"></i><span>Operación realizada exitosamente.</span>
        </div>
        <div className="mensaje-feedback mensaje-error oculto mb-md">
          <i className="fas fa-exclamation-circle"></i><span>Ha ocurrido un error. Intenta nuevamente.</span>
        </div>

        <DataPanel title="Información de la Ficha" icon="info-circle">
          <form className="formulario-proyecto" onSubmit={handleSubmit(onSubmit)}>
            <div className="form-content">
              <div className="grupo-formulario">
                <label htmlFor="codigo" className="etiqueta requerido">Código de Ficha</label>
                <input type="text" id="codigo" className="input-text" placeholder="Ej: ADSO-2568" {...register("codigo", { required: true })} />
                {errors.codigo && <span className="campo-error">El código es obligatorio</span>}
                <div className="campo-informacion">Código único que identificará al grupo (ej: PROGRAMA-NÚMERO).</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="nombre" className="etiqueta requerido">Nombre de la Ficha</label>
                <input type="text" id="nombre" className="input-text" placeholder="Ej: Análisis y Desarrollo 2568" {...register("nombre", { required: true })} />
                {errors.nombre && <span className="campo-error">El nombre es obligatorio</span>}
                <div className="campo-informacion">Nombre descriptivo para identificar la ficha.</div>
              </div>
              <div className="grupo-formulario">
                <label htmlFor="id_programa" className="etiqueta requerido">Programa de Formación</label>
                <select id="id_programa" className="select-filtro" {...register("id_programa", { required: true })}>
                  <option value="">Seleccione un programa</option>
                  <option value="1">ADSO - Análisis y Desarrollo de Sistemas</option>
                  <option value="2">Multimedia</option>
                  <option value="3">IoT</option>
                </select>
                {errors.id_programa && <span className="campo-error">Seleccione un programa</span>}
              </div>
              <div className="acciones-finales mt-lg">
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
