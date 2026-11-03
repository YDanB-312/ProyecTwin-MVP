# Manual Técnico de ProyecTwin — carpeta de entrega

> **Nota de vigencia (Fase 6S, 1-nov-2026; integración ISO, 2-nov-2026):** el
> cuerpo del manual es un **snapshot histórico** generado el 16-oct-2026 y no
> debe usarse como guía de despliegue vigente. Varias referencias están
> obsoletas: `RUN_SEED=true`, `MAIL_MAILER=log`, `SESSION_DRIVER=file`,
> credenciales temporales, `VolumenSeeder` y la exportación PDF de credenciales
> (retirada el 2-nov-2026). Para el estado actual usa `docs/deploy-railway.md` y
> `docs/release-android.md`. La versión **1.1** integra el **Anexo G** al final;
> el apartado 7.15 queda como registro histórico y remite a ese anexo.

Esta carpeta contiene el **Manual Técnico de ProyecTwin** (MVP) generado a partir de la
auditoría técnica del repositorio realizada el **16 de octubre de 2026**.

## Archivos entregables

| Archivo | Contenido |
|---|---|
| `Manual_Tecnico_ProyecTwin.docx` | **Entregable principal v1.1.** Manual completo (118 páginas, 8 capítulos, referencias APA y anexos A–G), con el Anexo G integrado al final, tabla de contenido automática, listas de figuras y tablas, encabezados/pies con numeración romana (preliminares) y árabe (cuerpo). Campos actualizados con Word/WPS. |
| `Manual_Tecnico_ProyecTwin_verificacion.pdf` | Copia PDF del manual completo v1.1 para verificación visual (118 páginas). |
| `Manual_Tecnico_ProyecTwin_Anexo_G.docx` | **Anexo G independiente.** Aplicación de las ocho referencias normativas al desarrollo, con matriz de evidencia, estados, límites, correcciones al registro histórico y referencias cruzadas al Cap. 57 del manual de usuario. 9 páginas, 2 de noviembre de 2026. |
| `Manual_Tecnico_ProyecTwin_Anexo_G_verificacion.pdf` | Copia PDF del Anexo G independiente para verificación visual. |
| `figuras/` | 8 diagramas técnicos generados con Python (arquitectura, secuencia, modelo ER, motor de similitud, estados, despliegue, autenticación y casos de uso). |
| `capturas/` | 24 capturas reales de la interfaz en ejecución (portal, tres roles, modo oscuro y vista móvil), tomadas el 16/10/2026 con Playwright sobre el sistema real. |
| `generacion/` | Scripts reproducibles de generación documental (ver abajo). |

## Regeneración del documento

Requisitos: Python 3.x con `python-docx`, `matplotlib` y `pillow` (figuras); Node con el
`node_modules` del frontend (capturas); opcionalmente Microsoft Word (actualización de campos).

```bash
# 1) Diagramas
python generacion/figuras.py

# 2) Capturas (requiere API en 127.0.0.1:8000 y SPA en localhost:5173 con la BD sembrada)
node generacion/capturas.mjs

# 3) Documento completo (incluye el Anexo G integrado)
python generacion/build_manual.py

# 4) Actualizar campos con Word/WPS y validar
powershell -ExecutionPolicy Bypass -File generacion/actualizar_campos.ps1
powershell -ExecutionPolicy Bypass -File generacion/exportar_pdf.ps1   # PDF de verificación
python generacion/validar_docx.py
```

## Anexo G — integrado y complementario (2/11/2026)

El Anexo G está **integrado al final del Manual Técnico v1.1** y también se
conserva como documento independiente. Sustituye el apartado 7.15 para efectos
de la aplicación de las normas ISO. El manual completo se genera con
`build_manual.py`; la copia independiente, con:

```bash
python generacion/build_anexo_g.py
```

El DOCX independiente se actualiza y exporta a PDF con Word/WPS mediante:

```powershell
powershell -ExecutionPolicy Bypass -File generacion/actualizar_campos_anexo_g.ps1
powershell -ExecutionPolicy Bypass -File generacion/exportar_pdf_anexo_g.ps1
python generacion/validar_anexo_g.py
```

Incluye ocho referencias (ISO/IEC 27001, ISO 14001, ISO/IEC 25010, ISO/IEC 9126-1,
ISO 37001, ISO 10005, ISO 9001 y la familia ISO/IEC 33000), la corrección de las
afirmaciones obsoletas del manual v1.0 y la declaración de no certificación.
No reescribe los capítulos 1–8 ni los anexos A–F.

## Estado de verificación del manual completo v1.1 (2/11/2026)

- Documento completo: **118 páginas**, **29.104 palabras**, 8 capítulos y anexos A–G.
- Validación estructural automática: **25 comprobaciones correctas, 0 avisos**
  (8 capítulos, TOC diligenciado, listas de figuras/tablas diligenciadas, 32 figuras y 73 tablas numeradas,
  Anexo G después del Anexo F, numeración G.n y referencia desde §7.15).
- PDF de verificación: **118 páginas, sin páginas vacías**.
- El Anexo G independiente conserva **9 páginas, 7 tablas y 22 comprobaciones OK**.
- Pruebas del sistema ejecutadas durante la auditoría histórica: **244 pruebas backend** (1.156 aserciones),
  **60 unitarias de frontend** y **ESLint sin hallazgos** (detalle en el capítulo 7 del manual). Las cifras
  vigentes y sus límites están en el Anexo G.

## Limitaciones declaradas (resumen; completo en el Anexo F del manual)

- El PDF de referencia «M.U- PopayanAllTour -SdeI.pdf» pertenece a **otro proyecto** y no se usó
  como fuente funcional.
- La suite E2E completa (54 especificaciones) y la compilación Android **no se re-ejecutaron** en
  esta auditoría; el despliegue Docker/Railway se verificó por configuración (Docker no disponible
  en el equipo de auditoría).
- Los datos institucionales (ficha, centro, nómina completa del equipo) no constan en el
  repositorio y se declaran como no confirmados, sin inventarlos.
- La auditoría usó una base de datos separada (`proyectwin_mvp_manual`) para no alterar los datos
  locales del equipo; no se modificó ningún archivo del código de la aplicación.

Estas limitaciones describen el corte del **16/10/2026**. El **Anexo G** actualiza
la evidencia ISO y corrige las afirmaciones sobre autenticación, credenciales,
CI y pruebas; para el estado vigente de esas materias, prevalece el Anexo G y no
este resumen histórico.
