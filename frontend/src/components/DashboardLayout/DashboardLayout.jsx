import { useState, lazy, Suspense } from 'react'
import GovernmentBar from '../GovernmentBar/GovernmentBar'
import Header from '../Header/Header'

const SidebarAprendiz = lazy(() => import('../SidebarAprendiz/SidebarAprendiz'))
const SidebarInstructor = lazy(() => import('../SidebarInstructor/SidebarInstructor'))
const SidebarAdmin = lazy(() => import('../SidebarAdmin/SidebarAdmin'))
const FooterAprendiz = lazy(() => import('../FooterAprendiz/FooterAprendiz'))
const FooterInstructor = lazy(() => import('../FooterInstructor/FooterInstructor'))
const FooterAdmin = lazy(() => import('../FooterAdmin/FooterAdmin'))

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
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const Sidebar = sidebars[role]
  const Footer = footers[role]
  const modClass = `modulo-${role}`

  return (
    <div className={`${modClass} modulo-pagina-completa${className ? ` ${className}` : ''}`}>
      <GovernmentBar />
      <Header titulo={titulo} usuario={usuario} notificaciones={notificaciones} role={role} onToggleSidebar={() => setSidebarOpen(prev => !prev)} />
      <Suspense fallback={null}>
        <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      </Suspense>
      <div className={`sidebar-overlay${sidebarOpen ? ' visible' : ''}`} onClick={() => setSidebarOpen(false)} />
      <main className="contenido-principal">
        {children}
      </main>
      <Suspense fallback={null}>
        <Footer />
      </Suspense>
    </div>
  )
}
