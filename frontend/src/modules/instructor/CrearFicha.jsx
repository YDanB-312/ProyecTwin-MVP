import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import DataPanel from '../../components/DataPanel/DataPanel'
import '../../assets/styles/pages/fichas.css'
import FormField from '../../components/FormField/FormField'

export default function CrearFicha() {
  const [enviado, setEnviado] = useState(false)
  const [error, setError] = useState(null)
  const { register, handleSubmit, formState: { errors }, reset } = useForm()
  const navigate = useNavigate()
  const location = useLocation()
  const editFicha = location.state?.editFicha

  useEffect(() => {
    if (editFicha) {
      reset({
        codigo: editFicha.codigo || '',
        nombre: editFicha.nombre || '',
        id_programa: editFicha.id_programa || ''
      })
    }
  }, [editFicha, reset])

  const onSubmit = (data) => {
    try {
      setEnviado(true)
      setError(null)
      // TODO: Send data to API endpoint
      navigate('/instructor/gestionar-fichas')
      reset()
    } catch (err) {
      setError('Error al guardar la ficha.')
    }
  }

  return (
    <DashboardLayout role="instructor" titulo="ProyecTwin - Panel del Instructor" usuario="Carlos Ruiz | Instr. ADSO" notificaciones={8}>
      <div className="contenedor-pagina fade-in">
        <PageHeader
          title={editFicha ? 'Editar Ficha' : 'Crear Nueva Ficha'}
          icon={editFicha ? 'edit' : 'plus-circle'}
          breadcrumb={[
            { to: '/instructor/dashboard', icon: 'home', label: 'Inicio' },
            { to: '/instructor/gestionar-fichas', label: 'Gestionar Fichas' },
            { label: editFicha ? 'Editar Ficha' : 'Crear Nueva Ficha' }
          ]}
          actions={<Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-arrow-left"></i> Volver</Link>}
        />

        <div className={`mensaje-feedback mensaje-exito ${enviado ? '' : 'oculto'} mb-md`}>
          <i className="fas fa-check-circle"></i><span>Ficha creada correctamente.</span>
        </div>
        <div className={`mensaje-feedback mensaje-error ${error ? '' : 'oculto'} mb-md`}>
          <i className="fas fa-exclamation-circle"></i><span>No se pudo crear la ficha. Verifica los datos e intenta de nuevo.</span>
        </div>

        <DataPanel title="Información de la Ficha" icon="info-circle">
          <form className="formulario-proyecto" onSubmit={handleSubmit(onSubmit)}>
            <div className="form-content">
              <FormField label="Código de Ficha" htmlFor="codigo" required error={errors.codigo && 'El código es obligatorio'} helpText="Código único que identificará al grupo (ej: PROGRAMA-NÚMERO).">
                <input type="text" id="codigo" className="campo-input" placeholder="Ej: ADSO-2568" {...register("codigo", { required: true })} />
              </FormField>
              <FormField label="Nombre de la Ficha" htmlFor="nombre" required error={errors.nombre && 'El nombre es obligatorio'} helpText="Nombre descriptivo para identificar la ficha.">
                <input type="text" id="nombre" className="campo-input" placeholder="Ej: Análisis y Desarrollo 2568" {...register("nombre", { required: true })} />
              </FormField>
              <FormField label="Programa de Formación" htmlFor="id_programa" required error={errors.id_programa && 'Seleccione un programa'}>
                <select id="id_programa" className="campo-select" {...register("id_programa", { required: true })}>
                  <option value="">Seleccione un programa</option>
                  <option value="1">ADSO - Análisis y Desarrollo de Sistemas</option>
                  <option value="2">Multimedia</option>
                  <option value="3">IoT</option>
                </select>
              </FormField>
              <div className="acciones-finales mt-lg">
                <button type="submit" className="btn-primario"><i className="fas fa-save"></i> {editFicha ? 'Actualizar Ficha' : 'Crear Ficha'}</button>
                <Link to="/instructor/gestionar-fichas" className="btn-secundario"><i className="fas fa-times"></i> Cancelar</Link>
              </div>
            </div>
          </form>
        </DataPanel>
      </div>
    </DashboardLayout>
  );
}


