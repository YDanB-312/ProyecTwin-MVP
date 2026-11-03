# Android: build debug de validación

**Fecha:** 2-nov-2026.
**HEAD:** `3d2c63981f695a0d5732845989362dd90c25f2de`.
**Alcance:** compilación y pruebas locales. Sin publicación, distribución ni firma release.

## Artefacto

| Dato | Valor |
|---|---|
| Archivo | `Movil/app/build/outputs/apk/debug/app-debug.apk` |
| Tamaño | 22.864.722 bytes |
| SHA-256 | `009BB15EF366AFED0A572E2CF4924FF7D58705CC037F3E45B3306D07DF07EDD6` |
| Paquete | `com.example.proyectwin` |
| `versionName` | `1.4.0` |
| `versionCode` | `5` |
| Firma | Debug (v2); no es la firma release `CN=Oscar` |
| `minSdk` / `targetSdk` | 26 / 36 |

El APK debug **no es distribuible**: usa la clave de depuración, no se puede instalar sobre una release firmada y no representa un artefacto de publicación.

## Pruebas ejecutadas

```powershell
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
.\gradlew.bat :app:lintDebug --console=plain
```

| Comprobación | Resultado |
|---|---|
| Pruebas unitarias | 31 suites, 196 pruebas, 0 fallos, 0 errores |
| Lint | 0 errores, 46 advertencias, 2 sugerencias |
| Build debug | Correcto |
| Dispositivo/emulador | No conectado; no se ejecutó smoke test en dispositivo |

## Limitaciones

- El APK debug apunta por defecto a `http://10.0.2.2:8000/v1/`, útil solo con emulador y backend local.
- No se validó en un dispositivo real ni con una cuenta de prueba autorizada.
- No se probó la instalación sobre 1.3.0/4 o 1.4.0/5.
- No se generó una release; la keystore `CN=Oscar` no está disponible en este equipo.
- No se publicó ni se copió el APK a `backend/public/apk/`.

## Siguiente paso para release (pendiente de autorización)

1. Confirmar custodia de la keystore y autorización escrita.
2. Subir `versionName`/`versionCode` a `1.4.1 / 6`.
3. Compilar con URL HTTPS real:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleRelease --console=plain `
  -Pproyectwin.apiBaseUrl.release=https://<host>/v1/
```

4. Verificar `aapt`, `apksigner`, SHA-256 y firma.
5. Instalar sobre 1.3.0/4 y 1.4.0/5 para comprobar la actualización.
6. No publicar hasta completar la autorización y el plan de despliegue.
