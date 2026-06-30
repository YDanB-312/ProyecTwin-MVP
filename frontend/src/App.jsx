import { Routes, Route } from 'react-router-dom'

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

        <Route path="/aprendiz/dashboard" element={<DashboardAprendiz />} />
        <Route path="/aprendiz/mis-proyectos" element={<MisProyectos />} />
        <Route path="/aprendiz/nuevo-proyecto" element={<NuevoProyecto />} />
        <Route path="/aprendiz/unirse-ficha" element={<UnirseFicha />} />
        <Route path="/aprendiz/alertas" element={<AlertasAprendiz />} />
        <Route path="/aprendiz/reportar-falla" element={<ReportarFallaAprendiz />} />
        <Route path="/aprendiz/perfil" element={<MiPerfil />} />
        <Route path="/aprendiz/detalle-proyecto/:id" element={<DetalleProyecto />} />
        <Route path="/aprendiz/detalle-similitud" element={<DetalleSimilitud />} />
        <Route path="/aprendiz/detalle-ficha" element={<DetalleFicha />} />
        <Route path="/aprendiz/analizando-proyecto" element={<AnalizandoProyecto />} />
        <Route path="/aprendiz/resultado-analisis" element={<ResultadoAnalisis />} />
        <Route path="/instructor/dashboard" element={<DashboardInstructor />} />
        <Route path="/instructor/revision-propuestas" element={<RevisionPropuestas />} />
        <Route path="/instructor/alertas" element={<AlertasInstructor />} />
        <Route path="/instructor/perfil" element={<PerfilInstructor />} />
        <Route path="/instructor/detalle-proyecto/:id" element={<DetalleProyectoInstructor />} />
        <Route path="/instructor/detalle-similitud" element={<DetalleSimilitudInstructor />} />
        <Route path="/instructor/reportar-falla" element={<ReportarFallaInstructor />} />
        <Route path="/instructor/gestionar-fichas" element={<GestionarFichas />} />
        <Route path="/instructor/crear-ficha" element={<CrearFicha />} />
        <Route path="/instructor/detalle-ficha" element={<DetalleFichaInstructor />} />
        <Route path="/instructor/directorio-ficha" element={<DirectorioFichaInstructor />} />

        <Route path="/admin/dashboard" element={<DashboardAdmin />} />
        <Route path="/admin/gestion-usuarios" element={<GestionUsuarios />} />
        <Route path="/admin/nuevo-usuario" element={<NuevoUsuario />} />
        <Route path="/admin/proyectos" element={<ProyectosAdmin />} />
        <Route path="/admin/detalle-proyecto" element={<DetalleProyectoAdmin />} />
        <Route path="/admin/similitudes" element={<SimilitudesAdmin />} />
        <Route path="/admin/detalle-similitud" element={<DetalleSimilitudAdmin />} />
        <Route path="/admin/detalle-usuario" element={<DetalleUsuario />} />
        <Route path="/admin/reportes-fallas" element={<ReportesFallas />} />
        <Route path="/admin/detalle-reporte" element={<DetalleReporte />} />
        <Route path="/admin/notificaciones" element={<NotificacionesAdmin />} />
        <Route path="/admin/perfil" element={<PerfilAdmin />} />

        <Route path="*" element={<PaginaNoEncontrada />} />
      </Routes>
  )
}
