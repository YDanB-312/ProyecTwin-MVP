import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import '../../assets/styles/pages/login.css'
import GovernmentBar from '../../components/GovernmentBar/GovernmentBar'
import FooterSimple from '../../components/FooterSimple/FooterSimple'

export default function RestablecerContrasena() {
  const { register, handleSubmit, formState: { errors }, reset, watch } = useForm()
  const navigate = useNavigate()

  const onSubmit = () => {
    navigate('/login')
    reset()
  }

  return (
    <div className="modulo-invitado">
      <GovernmentBar />

      <main className="contenedor-login">
        <div className="tarjeta-login">
          <div className="formulario-login">
            <div className="encabezado-login">
              <img src="/images/Logo-ProyecTwin.png" alt="Logo ProyecTwin" />
              <h1>Proyec<span>Twin</span></h1>
              <p>Nueva contraseña</p>
            </div>

            <p className="texto-instruccion">Ingresa tu nueva contraseña para restablecer el acceso a tu cuenta.</p>

            <form onSubmit={handleSubmit(onSubmit)}>
              <div className="grupo-campo">
                <label><i className="fas fa-lock"></i> Nueva Contraseña</label>
                <input type="password" placeholder="Minimo 6 caracteres" {...register("password", { required: true, minLength: 6 })} />
                {errors.password && <span className="campo-error">Mínimo 6 caracteres</span>}
              </div>
              <div className="grupo-campo">
                <label><i className="fas fa-check-circle"></i> Confirmar Contraseña</label>
                <input type="password" placeholder="Repite la contraseña" {...register("confirmar", { required: true, validate: value => value === watch("password") || "Las contraseñas no coinciden" })} />
                {errors.confirmar && <span className="campo-error">{errors.confirmar.message || "Campo obligatorio"}</span>}
              </div>

              <button type="submit" className="btn-primario">
                <i className="fas fa-save"></i> Restablecer contraseña
              </button>

              <div className="contenedor-enlace-volver">
                <Link to="/login"><i className="fas fa-arrow-left"></i> Volver al inicio de sesión</Link>
              </div>
            </form>
          </div>

          <div className="panel-info-login">
            <div>
              <h2>Restablecimiento seguro</h2>
              <p>Elige una contraseña segura que no hayas usado antes en ProyecTwin.</p>
              <ul>
                <li><i className="fas fa-check-circle"></i> Minimo 6 caracteres</li>
                <li><i className="fas fa-check-circle"></i> Incluye letras y numeros</li>
                <li><i className="fas fa-check-circle"></i> No compartas tu contraseña</li>
              </ul>
            </div>
          </div>
        </div>
      </main>

      <FooterSimple />
    </div>
  )
}
