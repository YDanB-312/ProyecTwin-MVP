import ReportarFallaBase from '../../components/ReportarFallaBase/ReportarFallaBase'

const tipoOptions = [
  { value: 'sistema', label: 'Error del sistema' },
  { value: 'proyecto', label: 'Problema con mi proyecto' },
  { value: 'datos', label: 'Error de datos' },
  { value: 'otro', label: 'Otro' },
]

const reportesAnteriores = [
  { tipo: 'Error del sistema', tipoClase: 'sistema', descripcion: 'El sistema no permite subir archivos al crear un proyecto', estado: 'Resuelto', estadoClase: 'badge-exito', estadoIcono: 'fa-check-circle', fecha: '10 Jun 2026' },
  { tipo: 'Error de datos', tipoClase: 'datos', descripcion: 'Mi perfil muestra información de programa desactualizada', estado: 'En Revisión', estadoClase: 'badge-advertencia', estadoIcono: 'fa-spinner', fecha: '25 Jun 2026' },
  { tipo: 'Problema con mi proyecto', tipoClase: 'proyecto', descripcion: 'No puedo editar los entregables de mi proyecto registrado', estado: 'Pendiente', estadoClase: 'badge-advertencia', estadoIcono: 'fa-clock', fecha: '28 Jun 2026' },
]

export default function ReportarFallaAprendiz() {
  return (
    <ReportarFallaBase
      role="aprendiz"
      dashboardPath="/aprendiz/dashboard"
      dashboardTitulo="ProyecTwin - Panel del Aprendiz"
      dashboardUsuario="Maria Gonzalez | ADSO"
      notificaciones={5}
      tipoOptions={tipoOptions}
      reportesAnteriores={reportesAnteriores}
      cancelPath="/aprendiz/dashboard"
    />
  )
}
