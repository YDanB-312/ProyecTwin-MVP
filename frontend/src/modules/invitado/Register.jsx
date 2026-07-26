import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import '../../assets/styles/pages/register.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterSimple from '../../components/FooterSimple/FooterSimple'
import FormField from '../../components/FormField/FormField'

export default function Register() {
  const { register, handleSubmit, formState: { errors }, reset } = useForm({
    defaultValues: { rol: 'aprendiz' }
  })
  const navigate = useNavigate()

  const onSubmit = (data) => {
    navigate('/confirmacion')
    reset()
  }

  return (
    <div className="modulo-invitado modulo-pagina-completa fade-in">
      <GovernmentBar />

      <main className="contenedor-register">
        <div className="tarjeta-register">
          <div className="encabezado-register">
            <Link to="/login" className="btn-secundario">
              <i className="fas fa-arrow-left"></i> Volver al login
            </Link>
            <img src="/images/Logo-ProyecTwin.png" alt="Logo ProyecTwin" />
            <h1>Proyec<span>Twin</span></h1>
            <p>Crea tu cuenta para acceder a la plataforma</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)}>
            <div className="seccion-register">
              <h2 className="titulo-seccion-register">
                <i className="fas fa-user"></i> Información Personal
              </h2>
              <div className="formulario-grid">
                <FormField label="Nombre" htmlFor="nombre" required error={errors.nombre && 'El nombre es obligatorio'}>
                  <input type="text" id="nombre" className="campo-input" placeholder="Ej: Maria" {...register("nombre", { required: true })} />
                </FormField>
                <FormField label="Apellido" htmlFor="apellido" required error={errors.apellido && 'El apellido es obligatorio'}>
                  <input type="text" id="apellido" className="campo-input" placeholder="Ej: Gonzalez" {...register("apellido", { required: true })} />
                </FormField>
                <FormField label="Correo Electrónico" htmlFor="correo" required error={errors.correo && 'Correo inválido'}>
                  <input type="email" id="correo" className="campo-input" placeholder="tu@correo.com" {...register("correo", { required: true, pattern: /^[^@\s]+@[^@\s]+\.[^@\s]+$/ })} />
                </FormField>
                <FormField label="Contrasena" htmlFor="password" required error={errors.password && 'Mínimo 6 caracteres'}>
                  <input type="password" id="password" className="campo-input" placeholder="Minimo 6 caracteres" {...register("password", { required: true, minLength: 6 })} />
                </FormField>
              </div>
            </div>

            <div className="seccion-register">
              <h2 className="titulo-seccion-register">
                <i className="fas fa-id-badge"></i> Selecciona tu Rol
              </h2>
              <div className="selector-rol">
                <label className="tarjeta-rol">
                  <input type="radio" value="aprendiz" {...register("rol", { required: true })} />
                  <div className="icono-rol"><i className="fas fa-user-graduate"></i></div>
                  <h3>Aprendiz</h3>
                  <p>Registra y Gestiona tus proyectos</p>
                </label>
                <label className="tarjeta-rol">
                  <input type="radio" value="instructor" {...register("rol", { required: true })} />
                  <div className="icono-rol"><i className="fas fa-chalkboard-teacher"></i></div>
                  <h3>Instructor</h3>
                  <p>Evalúa proyectos y da retroalimentación</p>
                </label>
              </div>
            </div>

            <button type="submit" className="btn-primario">
              <i className="fas fa-user-check"></i> Registrarse
            </button>
          </form>

          <div className="login-link">
            ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
          </div>
        </div>
      </main>

      <FooterSimple />
    </div>
  )
}
