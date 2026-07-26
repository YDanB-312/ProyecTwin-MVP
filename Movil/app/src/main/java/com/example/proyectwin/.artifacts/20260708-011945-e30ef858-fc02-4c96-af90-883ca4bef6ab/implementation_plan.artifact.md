# Mejorar la Pantalla de Perfil (Modernización UI/UX)

El objetivo es transformar la pantalla de perfil actual de una estructura básica y funcional a una experiencia móvil moderna y "premium", similar a las aplicaciones de alta calidad en la Play Store.

## Cambios Propuestos

### Componentes de UI (`SenaComponents.kt`)

Actualizaremos y añadiremos componentes para mejorar la estética general.

#### [SenaComponents.kt](file:///D:/ADSO YDMV/Proyecto/ProyecTwin-master/MVP/Movil/app/src/main/java/com/example/proyectwin/ui/components/SenaComponents.kt)

- **Actualizar `SenaTopBar`**: Añadir soporte para un icono de navegación (botón de atrás) y mejorar la personalización de colores.
- **Añadir `SenaSettingsRow`**: Un nuevo componente para items de configuración con icono, título, subtítulo y un control (Switch, Chevron, o Texto).
- **Mejorar `SenaCard`**: Ajustar el padding y las esquinas para un look más moderno.

### Pantalla de Perfil (`ProfileScreen.kt`)

Rediseño completo de la estructura visual.

#### [ProfileScreen.kt](file:///D:/ADSO YDMV/Proyecto/ProyecTwin-master/MVP/Movil/app/src/main/java/com/example/proyectwin/ui/screens/aprendiz/ProfileScreen.kt)

- **Cabecera Inmersiva**: Reemplazar la tarjeta de perfil actual por una sección de cabecera con un fondo degradado que se integre con la barra superior.
- **Avatar Mejorado**: Un avatar más grande y con mejor estilo visual.
- **Listado de Ajustes**: Organizar la información personal, seguridad y preferencias en una lista coherente usando `SenaSettingsRow`.
- **Uso de Switches**: Cambiar los `Checkbox` por `Switch` para una experiencia más móvil.
- **Botón de Cerrar Sesión**: Añadir un botón de acción destructiva con estilo propio al final de la pantalla.
- **Limpieza de Navegación**: Eliminar el botón "Regresar al Dashboard" del cuerpo y moverlo a la `SenaTopBar`.

## Plan de Verificación

### Verificación Manual
- **Inspección Visual**: Usar `render_compose_preview` para verificar el nuevo diseño de `ProfileScreen`.
- **Interactividad**: Verificar que los Switches y botones de edición funcionen correctamente en la vista previa (o mediante despliegue si es posible).
- **Consistencia**: Asegurar que los colores y tipografía sigan el tema de la aplicación.

### Comandos de Verificación
```bash
# No hay tests automatizados específicos para UI, se usará render_compose_preview.
```
