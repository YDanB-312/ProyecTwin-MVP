import { lazy, Suspense } from 'react'
import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute'

const Login = lazy(() => import('./modules/invitado/Login'))
const Home = lazy(() => import('./modules/invitado/Home'))
const Register = lazy(() => import('./modules/invitado/Register'))
const RecuperarContrasena = lazy(() => import('./modules/invitado/RecuperarContrasena'))
const RestablecerContrasena = lazy(() => import('./modules/invitado/RestablecerContrasena'))
const PaginaNoEncontrada = lazy(() => import('./modules/invitado/PaginaNoEncontrada'))
const Confirmacion = lazy(() => import('./modules/invitado/Confirmacion'))

const DashboardAprendiz = lazy(() => import('./modules/aprendiz/DashboardAprendiz'))
const MisProyectos = lazy(() => import('./modules/aprendiz/MisProyectos'))
const NuevoProyecto = lazy(() => import('./modules/aprendiz/NuevoProyecto'))
const AlertasAprendiz = lazy(() => import('./modules/aprendiz/AlertasAprendiz'))
const MiPerfil = lazy(() => import('./modules/aprendiz/MiPerfil'))
const DetalleProyecto = lazy(() => import('./modules/aprendiz/DetalleProyecto'))
const DetalleSimilitud = lazy(() => import('./modules/aprendiz/DetalleSimilitud'))
const ReportarFallaAprendiz = lazy(() => import('./modules/aprendiz/ReportarFallaAprendiz'))
const UnirseFicha = lazy(() => import('./modules/aprendiz/UnirseFicha'))
const DetalleFicha = lazy(() => import('./modules/aprendiz/DetalleFicha'))
const DetalleCompanero = lazy(() => import('./modules/aprendiz/DetalleCompanero'))
const AnalizandoProyecto = lazy(() => import('./modules/aprendiz/AnalizandoProyecto'))
const ResultadoAnalisis = lazy(() => import('./modules/aprendiz/ResultadoAnalisis'))

const DashboardInstructor = lazy(() => import('./modules/instructor/DashboardInstructor'))
const RevisionPropuestas = lazy(() => import('./modules/instructor/RevisionPropuestas'))
const AlertasInstructor = lazy(() => import('./modules/instructor/AlertasInstructor'))
const PerfilInstructor = lazy(() => import('./modules/instructor/PerfilInstructor'))
const DetalleProyectoInstructor = lazy(() => import('./modules/instructor/DetalleProyectoInstructor'))
const DetalleSimilitudInstructor = lazy(() => import('./modules/instructor/DetalleSimilitudInstructor'))
const ReportarFallaInstructor = lazy(() => import('./modules/instructor/ReportarFallaInstructor'))
const GestionarFichas = lazy(() => import('./modules/instructor/GestionarFichas'))
const CrearFicha = lazy(() => import('./modules/instructor/CrearFicha'))
const DetalleFichaInstructor = lazy(() => import('./modules/instructor/DetalleFichaInstructor'))
const DirectorioFichaInstructor = lazy(() => import('./modules/instructor/DirectorioFichaInstructor'))

const DashboardAdmin = lazy(() => import('./modules/admin/DashboardAdmin'))
const GestionUsuarios = lazy(() => import('./modules/admin/GestionUsuarios'))
const NuevoUsuario = lazy(() => import('./modules/admin/NuevoUsuario'))
const ProyectosAdmin = lazy(() => import('./modules/admin/ProyectosAdmin'))
const DetalleProyectoAdmin = lazy(() => import('./modules/admin/DetalleProyectoAdmin'))
const SimilitudesAdmin = lazy(() => import('./modules/admin/SimilitudesAdmin'))
const DetalleSimilitudAdmin = lazy(() => import('./modules/admin/DetalleSimilitudAdmin'))
const DetalleUsuario = lazy(() => import('./modules/admin/DetalleUsuario'))
const ReportesFallas = lazy(() => import('./modules/admin/ReportesFallas'))
const DetalleReporte = lazy(() => import('./modules/admin/DetalleReporte'))
const NotificacionesAdmin = lazy(() => import('./modules/admin/NotificacionesAdmin'))
const PerfilAdmin = lazy(() => import('./modules/admin/PerfilAdmin'))

const PageLoader = (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
    <div className="spinner"></div>
  </div>
)

export default function App() {
  return (
    <Suspense fallback={PageLoader}>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/recuperar-contrasena" element={<RecuperarContrasena />} />
        <Route path="/restablecer-contrasena" element={<RestablecerContrasena />} />
        <Route path="/confirmacion" element={<Confirmacion />} />

        <Route path="/aprendiz/dashboard" element={<ProtectedRoute allowedRoles={['aprendiz']}><DashboardAprendiz /></ProtectedRoute>} />
        <Route path="/aprendiz/mis-proyectos" element={<ProtectedRoute allowedRoles={['aprendiz']}><MisProyectos /></ProtectedRoute>} />
        <Route path="/aprendiz/nuevo-proyecto" element={<ProtectedRoute allowedRoles={['aprendiz']}><NuevoProyecto /></ProtectedRoute>} />
        <Route path="/aprendiz/unirse-ficha" element={<ProtectedRoute allowedRoles={['aprendiz']}><UnirseFicha /></ProtectedRoute>} />
        <Route path="/aprendiz/alertas" element={<ProtectedRoute allowedRoles={['aprendiz']}><AlertasAprendiz /></ProtectedRoute>} />
        <Route path="/aprendiz/reportar-falla" element={<ProtectedRoute allowedRoles={['aprendiz']}><ReportarFallaAprendiz /></ProtectedRoute>} />
        <Route path="/aprendiz/perfil" element={<ProtectedRoute allowedRoles={['aprendiz']}><MiPerfil /></ProtectedRoute>} />
        <Route path="/aprendiz/detalle-proyecto/:id" element={<ProtectedRoute allowedRoles={['aprendiz']}><DetalleProyecto /></ProtectedRoute>} />
        <Route path="/aprendiz/detalle-similitud/:id" element={<ProtectedRoute allowedRoles={['aprendiz']}><DetalleSimilitud /></ProtectedRoute>} />
        <Route path="/aprendiz/detalle-ficha/:id" element={<ProtectedRoute allowedRoles={['aprendiz']}><DetalleFicha /></ProtectedRoute>} />
        <Route path="/aprendiz/perfil-companero/:id" element={<ProtectedRoute allowedRoles={['aprendiz']}><DetalleCompanero /></ProtectedRoute>} />
        <Route path="/aprendiz/analizando-proyecto" element={<ProtectedRoute allowedRoles={['aprendiz']}><AnalizandoProyecto /></ProtectedRoute>} />
        <Route path="/aprendiz/resultado-analisis" element={<ProtectedRoute allowedRoles={['aprendiz']}><ResultadoAnalisis /></ProtectedRoute>} />

        <Route path="/instructor/dashboard" element={<ProtectedRoute allowedRoles={['instructor']}><DashboardInstructor /></ProtectedRoute>} />
        <Route path="/instructor/revision-propuestas" element={<ProtectedRoute allowedRoles={['instructor']}><RevisionPropuestas /></ProtectedRoute>} />
        <Route path="/instructor/alertas" element={<ProtectedRoute allowedRoles={['instructor']}><AlertasInstructor /></ProtectedRoute>} />
        <Route path="/instructor/perfil" element={<ProtectedRoute allowedRoles={['instructor']}><PerfilInstructor /></ProtectedRoute>} />
        <Route path="/instructor/detalle-proyecto/:id" element={<ProtectedRoute allowedRoles={['instructor']}><DetalleProyectoInstructor /></ProtectedRoute>} />
        <Route path="/instructor/detalle-similitud/:id" element={<ProtectedRoute allowedRoles={['instructor']}><DetalleSimilitudInstructor /></ProtectedRoute>} />
        <Route path="/instructor/reportar-falla" element={<ProtectedRoute allowedRoles={['instructor']}><ReportarFallaInstructor /></ProtectedRoute>} />
        <Route path="/instructor/gestionar-fichas" element={<ProtectedRoute allowedRoles={['instructor']}><GestionarFichas /></ProtectedRoute>} />
        <Route path="/instructor/crear-ficha" element={<ProtectedRoute allowedRoles={['instructor']}><CrearFicha /></ProtectedRoute>} />
        <Route path="/instructor/detalle-ficha/:id" element={<ProtectedRoute allowedRoles={['instructor']}><DetalleFichaInstructor /></ProtectedRoute>} />
        <Route path="/instructor/directorio-ficha/:id" element={<ProtectedRoute allowedRoles={['instructor']}><DirectorioFichaInstructor /></ProtectedRoute>} />

        <Route path="/admin/dashboard" element={<ProtectedRoute allowedRoles={['admin']}><DashboardAdmin /></ProtectedRoute>} />
        <Route path="/admin/gestion-usuarios" element={<ProtectedRoute allowedRoles={['admin']}><GestionUsuarios /></ProtectedRoute>} />
        <Route path="/admin/nuevo-usuario" element={<ProtectedRoute allowedRoles={['admin']}><NuevoUsuario /></ProtectedRoute>} />
        <Route path="/admin/proyectos" element={<ProtectedRoute allowedRoles={['admin']}><ProyectosAdmin /></ProtectedRoute>} />
        <Route path="/admin/detalle-proyecto/:id" element={<ProtectedRoute allowedRoles={['admin']}><DetalleProyectoAdmin /></ProtectedRoute>} />
        <Route path="/admin/similitudes" element={<ProtectedRoute allowedRoles={['admin']}><SimilitudesAdmin /></ProtectedRoute>} />
        <Route path="/admin/detalle-similitud/:id" element={<ProtectedRoute allowedRoles={['admin']}><DetalleSimilitudAdmin /></ProtectedRoute>} />
        <Route path="/admin/detalle-usuario/:id" element={<ProtectedRoute allowedRoles={['admin']}><DetalleUsuario /></ProtectedRoute>} />
        <Route path="/admin/reportes-fallas" element={<ProtectedRoute allowedRoles={['admin']}><ReportesFallas /></ProtectedRoute>} />
        <Route path="/admin/detalle-reporte/:id" element={<ProtectedRoute allowedRoles={['admin']}><DetalleReporte /></ProtectedRoute>} />
        <Route path="/admin/notificaciones" element={<ProtectedRoute allowedRoles={['admin']}><NotificacionesAdmin /></ProtectedRoute>} />
        <Route path="/admin/perfil" element={<ProtectedRoute allowedRoles={['admin']}><PerfilAdmin /></ProtectedRoute>} />

        <Route path="*" element={<PaginaNoEncontrada />} />
      </Routes>
    </Suspense>
  )
}
