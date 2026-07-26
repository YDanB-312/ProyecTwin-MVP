import ReportarFallaBase from '../../components/ReportarFallaBase/ReportarFallaBase'

const tipoOptions = [
  { value: 'sistema', label: 'Error del sistema' },
  { value: 'proyecto', label: 'Problema con proyectos asignados' },
  { value: 'datos', label: 'Error de datos' },
  { value: 'otro', label: 'Otro' },
]

const reportesAnteriores = [
  { tipo: 'Error del sistema', tipoClase: 'sistema', descripcion: 'El sistema no permite calificar propuestas de los aprendices', estado: 'Resuelto', estadoClase: 'badge-exito', estadoIcono: 'fa-check-circle', fecha: '10 Jun 2026' },
  { tipo: 'Error de datos', tipoClase: 'datos', descripcion: 'Los proyectos asignados no muestran correctamente la información', estado: 'En Revisión', estadoClase: 'badge-advertencia', estadoIcono: 'fa-spinner', fecha: '25 Jun 2026' },
  { tipo: 'Problema con proyectos', tipoClase: 'proyecto', descripcion: 'No puedo asignar instructores a nuevos proyectos', estado: 'Pendiente', estadoClase: 'badge-advertencia', estadoIcono: 'fa-clock', fecha: '28 Jun 2026' },
]

export default function ReportarFallaInstructor() {
  return (
    <ReportarFallaBase
      role="instructor"
      dashboardPath="/instructor/dashboard"
      dashboardTitulo="ProyecTwin - Panel del Instructor"
      dashboardUsuario="Carlos Ruiz | Instr. ADSO"
      notificaciones={8}
      tipoOptions={tipoOptions}
      reportesAnteriores={reportesAnteriores}
      cancelPath="/instructor/dashboard"
    />
  )
}
