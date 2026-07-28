import { useState, useEffect, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import DashboardLayout from '../../components/DashboardLayout/DashboardLayout'
import { useAuth } from '../../contexts/AuthContext'
import PageHeader from '../../components/PageHeader/PageHeader'
import '../../assets/styles/pages/fichas.css'
import FormField from '../../components/FormField/FormField'
import { buscarFichaPorCodigo, obtenerFichaAprendiz, guardarFichaAprendiz } from '../../constants/fichas'

export default function UnirseFicha() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [enviado, setEnviado] = useState(false)
  const [error, setError] = useState(null)
  const navTimerRef = useRef(null)
  const { register, handleSubmit, formState: { errors } } = useForm()

  const fichaActual = obtenerFichaAprendiz()

  useEffect(() => {
    return () => { if (navTimerRef.current) clearTimeout(navTimerRef.current) }
  }, [])

  const onSubmit = (data) => {
    try {
      setError(null)
      const ficha = buscarFichaPorCodigo(data.codigo)
      if (!ficha) {
        setError('No se encontró una ficha con ese código. Verifica con tu instructor.')
        return
      }
      guardarFichaAprendiz({ ...ficha, id_usuario: user.id, fecha_union: new Date().toISOString() })
      setEnviado(true)
      navTimerRef.current = setTimeout(() => navigate('/aprendiz/dashboard'), 2000)
    } catch (err) {
      setError('Error al unirse a la ficha.')
    }
  }

  if (fichaActual) {
    return (
      <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
        <div className="contenedor-pagina fade-in">
          <PageHeader
            title="Mi Ficha"
            icon="users"
            breadcrumb={[
              { to: '/aprendiz/dashboard', icon: 'home', label: 'Inicio' },
              { label: 'Mi Ficha' }
            ]}
          />
          <div className="unirse-card">
            <div className="mensaje-feedback mensaje-exito">
              <i className="fas fa-check-circle"></i>
              <span>Ya perteneces a la ficha <strong>{fichaActual.codigo}</strong> — {fichaActual.nombre}</span>
            </div>
            <div className="beneficios">
              <div className="beneficio-item">
                <span className="beneficio-icono"><i className="fas fa-code-branch"></i></span>
                <span className="beneficio-texto">Programa: {fichaActual.programa}</span>
              </div>
              <div className="beneficio-item">
                <span className="beneficio-icono"><i className="fas fa-calendar"></i></span>
                <span className="beneficio-texto">Te uniste el {new Date(fichaActual.fecha_union).toLocaleDateString('es-CO')}</span>
              </div>
            </div>
            <Link to="/aprendiz/detalle-ficha/ADSO-2568" className="btn-primario mt-lg"><i className="fas fa-eye"></i> Ver mi ficha</Link>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout role="aprendiz" titulo="ProyecTwin - Panel del Aprendiz" usuario={user?.nombre || 'Usuario'} notificaciones={0}>
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
            <span>{error}</span>
          </div>

          <form onSubmit={handleSubmit(onSubmit)}>
            <FormField label="Código de ficha" htmlFor="codigo-ficha" required error={errors.codigo?.message} helpText="Solicita a tu instructor el código de la ficha a la que perteneces e ingrésalo aquí.">
              <input type="text" id="codigo-ficha" className="campo-input" placeholder="ADSO-2568" {...register("codigo", { required: "El código de ficha es requerido" })} />
            </FormField>

            <Link to="/aprendiz/detalle-ficha/ADSO-2568" className="ayuda-link"><i className="fas fa-question-circle"></i> ¿Que es una ficha?</Link>

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
