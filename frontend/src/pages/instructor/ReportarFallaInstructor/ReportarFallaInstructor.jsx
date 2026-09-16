import DashboardLayout from '../../../layouts/DashboardLayout/DashboardLayout'
import ReportarFallaBase from '../../../components/ReportarFallaBase/ReportarFallaBase'
import { useAuth } from '../../../contexts/AuthContext'
import { reportes } from '../../../lib/recursos'

const hoyISO = () => new Date().toISOString().slice(0, 10)

export default function ReportarFallaInstructor() {
  const { user } = useAuth()

  // El reporte se envía directamente a la API (lo lee el panel de admin).
  const handleSubmit = async (form) => {
    await reportes.crear({
      titulo: form.titulo.trim(),
      descripcion: form.descripcion.trim(),
      tipo: form.tipo,
      estado: 'pendiente',
      fecha: hoyISO(),
      id_usuario: Number(user.id),
    })
  }

  return (
    <DashboardLayout role="instructor" titulo="Reportar Falla">
      <ReportarFallaBase role="instructor" onSubmit={handleSubmit} />
    </DashboardLayout>
  )
}
