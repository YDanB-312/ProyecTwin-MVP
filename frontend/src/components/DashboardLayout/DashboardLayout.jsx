import GovernmentBar from '../GovernmentBar/GovernmentBar'
import Header from '../Header/Header'
import SidebarAprendiz from '../SidebarAprendiz/SidebarAprendiz'
import SidebarInstructor from '../SidebarInstructor/SidebarInstructor'
import SidebarAdmin from '../SidebarAdmin/SidebarAdmin'
import FooterAprendiz from '../FooterAprendiz/FooterAprendiz'
import FooterInstructor from '../FooterInstructor/FooterInstructor'
import FooterAdmin from '../FooterAdmin/FooterAdmin'

const sidebars = {
  aprendiz: SidebarAprendiz,
  instructor: SidebarInstructor,
  admin: SidebarAdmin,
}

const footers = {
  aprendiz: FooterAprendiz,
  instructor: FooterInstructor,
  admin: FooterAdmin,
}

export default function DashboardLayout({ role, titulo, usuario, notificaciones, children, className }) {
  const Sidebar = sidebars[role]
  const Footer = footers[role]
  const modClass = `modulo-${role}`

  return (
    <div className={`${modClass}${className ? ` ${className}` : ''}`}>
      <GovernmentBar />
      <Header titulo={titulo} usuario={usuario} notificaciones={notificaciones} />
      <Sidebar />
      <main className="contenido-principal">
        {children}
      </main>
      <Footer />
    </div>
  )
}
