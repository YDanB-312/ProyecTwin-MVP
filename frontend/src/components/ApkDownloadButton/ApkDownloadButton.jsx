import Button from '../Button/Button'
import { useApi } from '../../lib/useApi'
import { appVersion } from '../../lib/recursos'
import { urlApkSegura } from '../../lib/urlApk'

// Solo se muestra si el backend anuncia una URL de descarga segura. La URL
// vacía significa que no hay un APK autorizado/publicado; así nunca se ofrece
// un enlace que terminaría en 404.
export default function ApkDownloadButton() {
  const { data } = useApi(() => appVersion.android(), [])
  const url = urlApkSegura(data?.download_url)

  if (!url) return null

  return (
    <Button
      as="a"
      href={url}
      variant="secondary"
      download="ProyecTWin.apk"
      rel="noopener noreferrer"
    >
      Descargar aplicación Android
    </Button>
  )
}
