import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute'

import Login from './modules/invitado/Login'
import Home from './modules/invitado/Home'
import Register from './modules/invitado/Register'
import RecuperarContrasena from './modules/invitado/RecuperarContrasena'
import RestablecerContrasena from './modules/invitado/RestablecerContrasena'
import PaginaNoEncontrada from './modules/invitado/PaginaNoEncontrada'
import Confirmacion from './modules/invitado/Confirmacion'

import DashboardAprendiz from './modules/aprendiz/DashboardAprendiz'
import MisProyectos from './modules/aprendiz/MisProyectos'
import NuevoProyecto from './modules/aprendiz/NuevoProyecto'
import AlertasAprendiz from './modules/aprendiz/AlertasAprendiz'
import MiPerfil from './modules/aprendiz/MiPerfil'
import DetalleProyecto from './modules/aprendiz/DetalleProyecto'
import DetalleSimilitud from './modules/aprendiz/DetalleSimilitud'
import ReportarFallaAprendiz from './modules/aprendiz/ReportarFallaAprendiz'
import UnirseFicha from './modules/aprendiz/UnirseFicha'
import DetalleFicha from './modules/aprendiz/DetalleFicha'
import DetalleCompanero from './modules/aprendiz/DetalleCompanero'
import AnalizandoProyecto from './modules/aprendiz/AnalizandoProyecto'
import ResultadoAnalisis from './modules/aprendiz/ResultadoAnalisis'
import DashboardInstructor from './modules/instructor/DashboardInstructor'
import RevisionPropuestas from './modules/instructor/RevisionPropuestas'
import AlertasInstructor from './modules/instructor/AlertasInstructor'
import PerfilInstructor from './modules/instructor/PerfilInstructor'
import DetalleProyectoInstructor from './modules/instructor/DetalleProyectoInstructor'
import DetalleSimilitudInstructor from './modules/instructor/DetalleSimilitudInstructor'
import ReportarFallaInstructor from './modules/instructor/ReportarFallaInstructor'
import GestionarFichas from './modules/instructor/GestionarFichas'
import CrearFicha from './modules/instructor/CrearFicha'
import DetalleFichaInstructor from './modules/instructor/DetalleFichaInstructor'
import DirectorioFichaInstructor from './modules/instructor/DirectorioFichaInstructor'

import DashboardAdmin from './modules/admin/DashboardAdmin'
import GestionUsuarios from './modules/admin/GestionUsuarios'
import NuevoUsuario from './modules/admin/NuevoUsuario'
import ProyectosAdmin from './modules/admin/ProyectosAdmin'
import DetalleProyectoAdmin from './modules/admin/DetalleProyectoAdmin'
import SimilitudesAdmin from './modules/admin/SimilitudesAdmin'
import DetalleSimilitudAdmin from './modules/admin/DetalleSimilitudAdmin'
import DetalleUsuario from './modules/admin/DetalleUsuario'
import ReportesFallas from './modules/admin/ReportesFallas'
import DetalleReporte from './modules/admin/DetalleReporte'
import NotificacionesAdmin from './modules/admin/NotificacionesAdmin'
import PerfilAdmin from './modules/admin/PerfilAdmin'

export default function App() {
  return (
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
        <Route path="/aprendiz/detalle-similitud" element={<ProtectedRoute allowedRoles={['aprendiz']}><DetalleSimilitud /></ProtectedRoute>} />
        <Route path="/aprendiz/detalle-ficha" element={<ProtectedRoute allowedRoles={['aprendiz']}><DetalleFicha /></ProtectedRoute>} />
        <Route path="/aprendiz/perfil-companero" element={<ProtectedRoute allowedRoles={['aprendiz']}><DetalleCompanero /></ProtectedRoute>} />
        <Route path="/aprendiz/analizando-proyecto" element={<ProtectedRoute allowedRoles={['aprendiz']}><AnalizandoProyecto /></ProtectedRoute>} />
        <Route path="/aprendiz/resultado-analisis" element={<ProtectedRoute allowedRoles={['aprendiz']}><ResultadoAnalisis /></ProtectedRoute>} />
        <Route path="/instructor/dashboard" element={<ProtectedRoute allowedRoles={['instructor']}><DashboardInstructor /></ProtectedRoute>} />
        <Route path="/instructor/revision-propuestas" element={<ProtectedRoute allowedRoles={['instructor']}><RevisionPropuestas /></ProtectedRoute>} />
        <Route path="/instructor/alertas" element={<ProtectedRoute allowedRoles={['instructor']}><AlertasInstructor /></ProtectedRoute>} />
        <Route path="/instructor/perfil" element={<ProtectedRoute allowedRoles={['instructor']}><PerfilInstructor /></ProtectedRoute>} />
        <Route path="/instructor/detalle-proyecto/:id" element={<ProtectedRoute allowedRoles={['instructor']}><DetalleProyectoInstructor /></ProtectedRoute>} />
        <Route path="/instructor/detalle-similitud" element={<ProtectedRoute allowedRoles={['instructor']}><DetalleSimilitudInstructor /></ProtectedRoute>} />
        <Route path="/instructor/reportar-falla" element={<ProtectedRoute allowedRoles={['instructor']}><ReportarFallaInstructor /></ProtectedRoute>} />
        <Route path="/instructor/gestionar-fichas" element={<ProtectedRoute allowedRoles={['instructor']}><GestionarFichas /></ProtectedRoute>} />
        <Route path="/instructor/crear-ficha" element={<ProtectedRoute allowedRoles={['instructor']}><CrearFicha /></ProtectedRoute>} />
        <Route path="/instructor/detalle-ficha" element={<ProtectedRoute allowedRoles={['instructor']}><DetalleFichaInstructor /></ProtectedRoute>} />
        <Route path="/instructor/directorio-ficha" element={<ProtectedRoute allowedRoles={['instructor']}><DirectorioFichaInstructor /></ProtectedRoute>} />

        <Route path="/admin/dashboard" element={<ProtectedRoute allowedRoles={['admin']}><DashboardAdmin /></ProtectedRoute>} />
        <Route path="/admin/gestion-usuarios" element={<ProtectedRoute allowedRoles={['admin']}><GestionUsuarios /></ProtectedRoute>} />
        <Route path="/admin/nuevo-usuario" element={<ProtectedRoute allowedRoles={['admin']}><NuevoUsuario /></ProtectedRoute>} />
        <Route path="/admin/proyectos" element={<ProtectedRoute allowedRoles={['admin']}><ProyectosAdmin /></ProtectedRoute>} />
        <Route path="/admin/detalle-proyecto" element={<ProtectedRoute allowedRoles={['admin']}><DetalleProyectoAdmin /></ProtectedRoute>} />
        <Route path="/admin/similitudes" element={<ProtectedRoute allowedRoles={['admin']}><SimilitudesAdmin /></ProtectedRoute>} />
        <Route path="/admin/detalle-similitud" element={<ProtectedRoute allowedRoles={['admin']}><DetalleSimilitudAdmin /></ProtectedRoute>} />
        <Route path="/admin/detalle-usuario" element={<ProtectedRoute allowedRoles={['admin']}><DetalleUsuario /></ProtectedRoute>} />
        <Route path="/admin/reportes-fallas" element={<ProtectedRoute allowedRoles={['admin']}><ReportesFallas /></ProtectedRoute>} />
        <Route path="/admin/detalle-reporte" element={<ProtectedRoute allowedRoles={['admin']}><DetalleReporte /></ProtectedRoute>} />
        <Route path="/admin/notificaciones" element={<ProtectedRoute allowedRoles={['admin']}><NotificacionesAdmin /></ProtectedRoute>} />
        <Route path="/admin/perfil" element={<ProtectedRoute allowedRoles={['admin']}><PerfilAdmin /></ProtectedRoute>} />

        <Route path="*" element={<PaginaNoEncontrada />} />
      </Routes>
  )
}
