# Manual de usuario del frontend de ProyecTWIN

Manual interactivo y PDF del frontend web de ProyecTWIN. Cubre la zona pública y los roles aprendiz, instructor y administrador.

## Abrir el manual

Con el frontend en desarrollo:

```powershell
npm run dev
```

Abre `http://localhost:5173/manual/index.html`.

También puedes servir solo esta carpeta:

```powershell
python -m http.server 4174
```

Y abrir `http://127.0.0.1:4174/`.

## Archivos

- `index.html`: sitio del manual.
- `styles/frontend-tokens.css`: copia de `frontend/src/tokens/index.css`.
- `styles/manual.css`: estilos del manual; consume los tokens del frontend.
- `scripts/manual-data.js`: contenido de los 67 capítulos, incluido el capítulo de aplicación de las normas ISO en el desarrollo.
- `scripts/manual.js`: navegación, búsqueda, filtros, pines, recorridos, progreso y lightbox.
- `assets/capturas/`: 55 capturas del entorno demo/E2E.
- `Manual_Frontend_ProyecTwin.pdf`: versión PDF de 193 páginas.

## Integración con el Home

El Home público muestra el botón **“Ver manual”**, que abre `/manual/index.html` en una pestaña nueva.

El manual vive en `frontend/public/manual/`, por lo que Vite lo sirve directamente y lo copia a `frontend/dist/manual/` durante el build.

## Actualizar los tokens

Si cambian los estilos base del frontend, copia el archivo de tokens:

```powershell
Copy-Item src/tokens/index.css public/manual/styles/frontend-tokens.css -Force
```

## Regenerar el PDF

Desde `frontend/`:

```powershell
node scripts/generate-manual-pdf.mjs
```

El script usa Playwright y Chromium del propio frontend.

## Datos y alcance

Las capturas se generaron con el wrapper E2E sobre bases desechables `ptwin_e2e_6an_*` y datos demo sintéticos. No se usaron datos personales reales ni producción.

El manual documenta únicamente el frontend. No incluye backend, base de datos, app Android, Docker, Railway ni infraestructura.
