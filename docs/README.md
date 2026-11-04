# Documentación de ProyecTWIN

Índice de la documentación vigente. La fuente de verdad del código es el
repositorio; este índice solo separa lo operativo de lo histórico.

## Entregables documentales

| Entregable | Archivo | Estado |
|---|---|---|
| Manual de usuario interactivo | [`frontend/public/manual/index.html`](../frontend/public/manual/index.html) | 67 capítulos; incluye el Cap. 57 ISO. |
| PDF del manual de usuario | [`frontend/public/manual/Manual_Frontend_ProyecTwin.pdf`](../frontend/public/manual/Manual_Frontend_ProyecTwin.pdf) | 193 páginas. |
| Manual técnico completo | [`manual-tecnico/Manual_Tecnico_ProyecTwin.docx`](manual-tecnico/Manual_Tecnico_ProyecTwin.docx) | v1.1; 118 páginas; Anexo G integrado. |
| PDF del manual técnico completo | [`manual-tecnico/Manual_Tecnico_ProyecTwin_verificacion.pdf`](manual-tecnico/Manual_Tecnico_ProyecTwin_verificacion.pdf) | 118 páginas. |
| Anexo G independiente | [`manual-tecnico/Manual_Tecnico_ProyecTwin_Anexo_G.docx`](manual-tecnico/Manual_Tecnico_ProyecTwin_Anexo_G.docx) | 9 páginas; material complementario. |
| PDF del Anexo G independiente | [`manual-tecnico/Manual_Tecnico_ProyecTwin_Anexo_G_verificacion.pdf`](manual-tecnico/Manual_Tecnico_ProyecTwin_Anexo_G_verificacion.pdf) | 9 páginas. |

## Operación

| Documento | Uso |
|---|---|
| [`ENTREGA-DESARROLLADOR.md`](ENTREGA-DESARROLLADOR.md) | Guía breve de entrega: qué hay, qué instalar, cómo probar y qué sigue pendiente. |
| [`../backend/README.md`](../backend/README.md) | Instalación y operación del backend; cada desarrollador crea su propia base vacía. |
| [`../frontend/README.md`](../frontend/README.md) | Instalación, scripts y pruebas del frontend. |
| [`INSTALACION-LOCAL.md`](INSTALACION-LOCAL.md) | Instalación local; cada desarrollador crea su propia base vacía. `proyectwin_mvp` es solo el nombre de ejemplo y la base del propietario. |
| [`CHECKLIST-INSTALACION-WINDOWS.md`](CHECKLIST-INSTALACION-WINDOWS.md) | Aceptación de instalación en un segundo equipo Windows. |
| [`deploy-railway.md`](deploy-railway.md) | Despliegue en Railway. Incluye el APK autorizado `1.4.1/6` servido en `/apk/ProyecTWin.apk`. |
| [`entorno-pruebas-seguro.md`](entorno-pruebas-seguro.md) | Aislamiento de PHPUnit/E2E con bases desechables. |
| [`E2E-GATE.md`](E2E-GATE.md) | Evidencia de las corridas E2E 6AN/6AP y Fase 6. |
| [`CI.md`](../CI.md) | Workflow de GitHub Actions. |
| [`release-android.md`](release-android.md) | Release y firma del APK (responsabilidad de Oscar). |
| [`manual-usuario-android.md`](manual-usuario-android.md) | Manual Android integrado, capturas reales, paridad de dashboards y notificaciones de actualización. |
| [`ejecutar-android.md`](ejecutar-android.md) | Ejecución local de la app Android. |
| [`normativa-visual.md`](normativa-visual.md) | Tokens y criterios visuales compartidos. |

## Arquitectura

- [`arquitectura.md`](arquitectura.md): recorrido de una petición GET.
- [`arquitectura.html`](arquitectura.html): versión exportada del mismo recorrido.
- [`arquitectura-figma.svg`](arquitectura-figma.svg): ilustración de apoyo.
- [`diagrama-completo.drawio`](diagrama-completo.drawio),
  [`diagrama-get.drawio`](diagrama-get.drawio) y
  [`diagrama.excalidraw`](diagrama.excalidraw): fuentes editables.

`arquitectura.md` es la referencia textual; los diagramas son material de
apoyo y pueden estar desactualizados si cambia el flujo.

## Decisiones y estado

- [`alcance-y-exclusiones.md`](alcance-y-exclusiones.md): límites del MVP.
- [`APK-APROBACION-OSCAR.md`](APK-APROBACION-OSCAR.md): antecedente del APK
  1.3.0/4.
- **APK `1.4.1/6` preparado.** El owner autorizó la actualización y el despliegue
  desde Railway Dashboard. El APK firmado está en
  `backend/public/apk/ProyecTWin.apk`; la descarga se corrigió para servir solo
  `/apk/ProyecTWin.apk`. El despliegue y la actualización de variables quedan a
  cargo del owner.
- [`guion-presentacion.md`](guion-presentacion.md): guion de demostración.
- [`inventario-pendientes.md`](inventario-pendientes.md): inventario de deuda y
  estado histórico de los lotes. Para el estado vigente, usa la sección
  «Estado vigente» y la guía de entrega.

## Histórico

Los documentos que ya no son guía operativa se conservan en
[`archive/`](archive/README.md) sin borrar su contenido:

- `AUDITORIA-INTEGRAL.md` (snapshot de octubre de 2026).
- `HISTORIAL-FIXES.md`.
- `backend-README-ALCANCE.md`.
- `frontend-CHANGELOG.md`.
- `ALINEACION-FRONTEND-BACKEND.md`.
- `plan-versionado-android.md` (plan aplicado).
- `privacidad-auditoria.md` (revisión estática).

## Manual técnico

[`manual-tecnico/`](manual-tecnico/LEEME.md) contiene el Manual Técnico v1.1
(2-nov-2026), que conserva el cuerpo histórico del 16-oct-2026 e integra el
**Anexo G** al final, y la copia independiente del Anexo G. El Anexo G documenta
la aplicación vigente de las normas ISO y corrige las afirmaciones obsoletas del
apartado 7.15. El cuerpo histórico no debe usarse como guía de despliegue
vigente; para operación actual usa `deploy-railway.md` y `release-android.md`.

## Criterio de mantenimiento

- Lo operativo debe ser reproducible desde un clon limpio.
- Lo histórico se archiva, no se reescribe.
- No se añaden documentos extensos si no tienen un lector y una tarea concretos.
- Los secretos reales y las credenciales de producción nunca se documentan aquí.
