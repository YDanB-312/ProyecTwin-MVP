# Informe de aprobación del APK — Oscar

> **Estado: NO APROBADO para publicación definitiva hasta que Oscar confirme
> por escrito la autorización y la custodia de la clave.** Este documento no
> sustituye esa confirmación.
>
> **Política vigente:** el APK histórico se conserva tracked en
> `backend/public/apk/ProyecTWin.apk`, pero **no se distribuye**. No se copia a
> nuevas imágenes Docker, `/apk` responde 404 y `/v1/app-version/android`
> devuelve `download_url` vacío. La publicación requiere autorización escrita
> de Oscar y la custodia de la firma.

## Artefacto verificado

| Dato | Valor |
|---|---|
| Archivo | `backend/public/apk/ProyecTWin.apk` |
| Tamaño | 14.866.767 bytes |
| SHA-256 | `A6C63BFE0B01C1020509649B8B163715AA8F21852D664740392FB8256B69B99E` |
| Paquete | `com.example.proyectwin` |
| Versión | 1.3.0 |
| versionCode | 4 |
| minSdk / targetSdk | 26 / 36 |
| Firma | APK Signature Scheme v2 y v3 |
| Certificado | `C=co, ST=Cauca, L=Popayan, CN=Oscar` |
| Clave | RSA 2048 |
| SHA-256 del certificado | `DC:53:39:53:7A:01:85:C7:F8:08:C4:58:7A:7E:FB:1B:12:03:41:54:E2:B1:F6:A8:B0:DB:67:7D:A1:E9:77:3D` |

Verificado con `apksigner verify --print-certs` y `aapt dump badging`. No se
reconstruyó ni re-firmó.

**6AP:** se volvió a comprobar el archivo existente: tamaño 14.866.767 bytes y
SHA-256 `A6C63BFE...B99E`, sin cambios. El APK sigue **no aprobado** hasta la
confirmación escrita de Oscar.

## Estado de distribución

- El archivo histórico permanece en `backend/public/apk/ProyecTWin.apk` sin
  cambios.
- No se copia a nuevas imágenes Docker ni al contexto de Railway.
- Nginx responde 404 en `/apk/ProyecTWin.apk` y en todo `/apk/`.
- `GET /v1/app-version/android` mantiene el contrato de versión, pero devuelve
  `download_url=""`.
- El Home no muestra botón ni enlace de descarga.
- La descarga solo podrá habilitarse después de una autorización escrita y un
  cambio de código explícito; no se activa por variable de entorno.

## Consecuencias de distribuir esta firma

1. Las actualizaciones de la app deben firmarse con **la misma clave**. Si se
   pierde, no se podrán publicar actualizaciones para las instalaciones
   existentes.
2. Si la clave se filtra, un tercero podría firmar un APK que Android acepte
   como actualización. En distribución directa no existe revocación central.
3. Cambiar de clave obliga a una transición: desinstalar/reinstalar o publicar
   una app con otro `applicationId`.
4. El paquete actual es `com.example.proyectwin`; si se decide un
   `applicationId` institucional, será una identidad nueva para las
   instalaciones existentes.
5. La custodia debe incluir backup offline probado y restauración verificada;
   la vigencia del certificado y el procedimiento están en
   [`release-android.md`](release-android.md).

## Confirmaciones que debe dar Oscar

- [ ] Autoriza publicar **1.3.0 / versionCode 4** en una imagen futura.
- [ ] Confirma que `CN=Oscar` es la clave oficial y su custodia/continuidad.
- [ ] Confirma si se mantiene `com.example.proyectwin` o se planifica un cambio.
- [ ] Designa custodio y suplente de la clave, fuera del repositorio.
- [ ] Autoriza el SHA-256 y la versión anunciados por
  `/v1/app-version/android`.
- [ ] Define la política de actualización (`ANDROID_APP_MIN_VERSION`,
  `ANDROID_APP_ENFORCE_MINIMUM`).

## Estado mientras no se apruebe

El escenario seguro es el vigente:

- El APK histórico permanece tracked, sin modificarse.
- El controlador devuelve `download_url` vacío.
- El Home no muestra botón de descarga.
- Nginx responde 404 en `/apk/ProyecTWin.apk` y en todo `/apk/`.
- El APK no se copia a nuevas imágenes.
- No se debe declarar el APK aprobado ni distribuir una versión distinta sin la
  confirmación escrita de Oscar.
