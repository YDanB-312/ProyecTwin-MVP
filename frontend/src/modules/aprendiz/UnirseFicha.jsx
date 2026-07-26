import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/fichas.css'
import FormField from '../../components/FormField/FormField'

export default function UnirseFicha() {
  const navigate = useNavigate()
  const [enviado, setEnviado] = useState(false)
  const [error, setError] = useState(null)
  const { register, handleSubmit, formState: { errors } } = useForm()

  const onSubmit = (data) => {
    setEnviado(true)
    setError(null)
    setTimeout(() => navigate('/aprendiz/dashboard'), 2000)
  }
  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario="Maria Gonzalez | ADSO" notificaciones={5}>
      <div className="contenedor-pagina fade-in">

        <PageHeader
          title="Unirse a una Ficha"
          subtitle="Ingresa el código de tu ficha para unirte al grupo de formación."
          icon="user-plus"
          breadcrumb={[
            { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
            { label: 'Unirse a una Ficha' }
          ]}
        />

        <div className="unirse-card">

          <div className={`mensaje-feedback mensaje-exito ${enviado ? '' : 'oculto'}`}>
            <i className="fas fa-check-circle"></i>
            <span>Te has unido a la ficha correctamente.</span>
          </div>
          <div className={`mensaje-feedback mensaje-error ${error ? '' : 'oculto'}`}>
            <i className="fas fa-exclamation-circle"></i>
            <span>No se pudo unir a la ficha. Verifica el código e intenta de nuevo.</span>
          </div>

          <form onSubmit={handleSubmit(onSubmit)}>
            <FormField label="Código de ficha" htmlFor="codigo-ficha" required error={errors.codigo?.message} helpText="Solicita a tu instructor el código de la ficha a la que perteneces e ingrésalo aquí.">
              <input type="text" id="codigo-ficha" className="campo-input" placeholder="ADSO-2568" {...register("codigo", { required: "El código de ficha es requerido" })} />
            </FormField>

            <Link to="/aprendiz/detalle-ficha" className="ayuda-link"><i className="fas fa-question-circle"></i> ¿Que es una ficha?</Link>

            <button type="submit" className="btn-unirse"><i className="fas fa-sign-in-alt"></i> Unirse a la Ficha</button>
          </form>

          <div className="beneficios">
            <div className="beneficio-item">
              <span className="beneficio-icono"><i className="fas fa-users"></i></span>
              <span className="beneficio-texto">Formar parte del grupo de trabajo con tus companeros</span>
            </div>
            <div className="beneficio-item">
              <span className="beneficio-icono"><i className="fas fa-code-branch"></i></span>
              <span className="beneficio-texto">Trabajar en equipo en los proyectos asignados</span>
            </div>
            <div className="beneficio-item">
              <span className="beneficio-icono"><i className="fas fa-bell"></i></span>
              <span className="beneficio-texto">Recibir notificaciones de tu instructor</span>
            </div>
          </div>

        </div>
      </div>
    </DashboardLayout>
  );
}


