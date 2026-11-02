# Documentación de ProyecTWIN

Índice de la documentación vigente. La fuente de verdad del código es el
repositorio; este índice solo separa lo operativo de lo histórico.

## Operación

| Documento | Uso |
|---|---|
| [`ENTREGA-DESARROLLADOR.md`](ENTREGA-DESARROLLADOR.md) | Guía breve de entrega: qué hay, qué instalar, cómo probar y qué sigue pendiente. |
| [`../backend/README.md`](../backend/README.md) | Instalación y operación del backend; cada desarrollador crea su propia base vacía. |
| [`../frontend/README.md`](../frontend/README.md) | Instalación, scripts y pruebas del frontend. |
| [`INSTALACION-LOCAL.md`](INSTALACION-LOCAL.md) | Instalación local; cada desarrollador crea su propia base vacía. `proyectwin_mvp` es solo el nombre de ejemplo y la base del propietario. |
| [`CHECKLIST-INSTALACION-WINDOWS.md`](CHECKLIST-INSTALACION-WINDOWS.md) | Aceptación de instalación en un segundo equipo Windows. |
| [`deploy-railway.md`](deploy-railway.md) | Despliegue en Railway. El APK histórico no se distribuye ni se incluye en imágenes. |
| [`entorno-pruebas-seguro.md`](entorno-pruebas-seguro.md) | Aislamiento de PHPUnit/E2E con bases desechables. |
| [`E2E-GATE.md`](E2E-GATE.md) | Evidencia de las corridas E2E 6AN/6AP y Fase 6. |
| [`CI.md`](../CI.md) | Workflow de GitHub Actions. |
| [`release-android.md`](release-android.md) | Release y firma del APK (responsabilidad de Oscar). |
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
- [`APK-APROBACION-OSCAR.md`](APK-APROBACION-OSCAR.md): estado **NO APROBADO**
  del APK y confirmaciones pendientes.
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

[`manual-tecnico/`](manual-tecnico/LEEME.md) es un entregable histórico
generado el 16-oct-2026. No debe usarse como guía de despliegue vigente; para
operación actual usa `deploy-railway.md` y `release-android.md`.

## Criterio de mantenimiento

- Lo operativo debe ser reproducible desde un clon limpio.
- Lo histórico se archiva, no se reescribe.
- No se añaden documentos extensos si no tienen un lector y una tarea concretos.
- Los secretos reales y las credenciales de producción nunca se documentan aquí.
