# Propuestas de endurecimiento de autenticación

**Estado:** propuestas para revisión. No implementadas, no desplegadas.
**Fecha:** 2-nov-2026.
**Contexto:** login exclusivo por correo, activación por enlace y eliminación de la exportación PDF de credenciales.

## 1. Cuentas legacy con `must_change_password = 1`

### Situación actual

- Las columnas `must_change_password` y `password_temporal` siguen en la base y ocultas en el modelo.
- El middleware que obligaba el cambio de contraseña ya no está registrado.
- Una cuenta legacy puede iniciar sesión con su contraseña temporal y no se le exige cambiarla.

### Riesgo

- La contraseña temporal antigua puede seguir siendo válida de forma indefinida.
- No se debe aceptar como solución permanente ni cambiar cuentas en masa sin conocer su estado.

### Propuesta recomendada

1. **Guarda de login:** si `must_change_password = 1`, el backend no emite sesión completa; devuelve `403` con código `cambio_password_requerido` y un enlace de recuperación por correo.
2. **Reset seguro:** el usuario establece una contraseña nueva mediante el flujo de recuperación; al completarlo, `must_change_password` pasa a `0`.
3. **Reenvío administrativo:** el admin puede enviar el enlace desde el detalle del usuario, sin ver ni generar contraseñas.
4. **Sin cambios masivos:** no actualizar `must_change_password` ni `password_temporal` en bloque. Primero contar las cuentas y revisar su estado.
5. **Compatibilidad:** la guarda debe aplicarse solo a cuentas legacy; las cuentas nuevas ya nacen sin contraseña utilizable y con activación pendiente.

### Pruebas necesarias

- Login legacy con `must_change_password=1` → 403 con código.
- Reset completado → login normal y `must_change_password=0`.
- Cuenta nueva pendiente → sigue el flujo de activación, no el de cambio de contraseña.
- Admin reenvía enlace sin exponer la contraseña temporal.

## 2. Break-glass para el administrador único

### Objetivo

Permitir recuperar el acceso cuando el único administrador pierde su correo o su contraseña, sin crear una puerta trasera permanente.

### Diseño propuesto

- Comando Artisan restringido, por ejemplo `admin:recuperar-acceso`.
- Requiere `--confirmar` y el identificador del administrador.
- No imprime ni genera contraseñas; solo:
  - verifica que el operador está autorizado;
  - marca el correo como verificado si corresponde;
  - envía un enlace de recuperación al correo registrado;
  - si el correo es inaccesible, permite actualizarlo con un segundo paso aprobado por el owner.
- Registra en auditoría: operador, motivo, ID de la cuenta, acción y resultado. No registra tokens.
- No se expone por HTTP ni por la interfaz web.
- No crea un usuario nuevo ni un bypass de login.

### Controles

- Ejecución solo en el servidor o consola autorizada.
- Doble confirmación del owner para cambiar el correo de un administrador.
- Registro en la bitácora y en el canal de incidentes.
- Revisión posterior obligatoria.

### Lo que no se debe hacer

- No un comando que establezca una contraseña fija.
- No un endpoint público de “recuperar admin”.
- No un usuario de emergencia permanente.
- No desactivar la verificación de correo de forma global.

## 3. Verificación del nuevo correo al cambiarlo

### Situación actual

`PUT /v1/auth/email` actualiza el correo y conserva `email_verified_at`. El nuevo buzón no se verifica antes de considerarlo confirmado.

### Propuesta

1. Añadir una tabla `email_changes` (o columnas `pending_email`, `pending_email_token_hash`, `pending_email_expires_at`).
2. Al solicitar el cambio:
   - validar el correo nuevo y su unicidad;
   - guardar el correo pendiente, no el definitivo;
   - enviar un enlace de confirmación al correo nuevo;
   - mantener el correo actual y la sesión hasta la confirmación.
3. Endpoint de confirmación con token de un solo uso y vencimiento.
4. Al confirmar:
   - actualizar `correo`;
   - marcar `email_verified_at`;
   - revocar las demás sesiones;
   - avisar al correo anterior.
5. Si el token vence o se rechaza, el correo actual no cambia.
6. Aplicar el mismo flujo al cambio administrativo de correo.

### Pruebas necesarias

- Solicitud de cambio no altera el correo actual.
- Token válido confirma el nuevo correo.
- Token vencido o reutilizado falla.
- Correo nuevo duplicado se rechaza.
- Se revocan las demás sesiones y se avisa al correo anterior.
- Un admin no puede saltarse la confirmación.

## 4. `username`: estado y decisión aplicada

- El login web, backend y Android usa solo correo; `username` está prohibido en el payload.
- La exportación PDF de credenciales se eliminó; ya no se presenta `username` como credencial.
- En la tabla de administración se rotuló como “Usuario institucional” y se añadió una nota de que no sirve para iniciar sesión.
- La columna histórica se conserva en la base.
- No se elimina `username` de la base ni se alteran nombres y apellidos.

### Pendiente de decidir

- Si se retira también de las pantallas de detalle o se mantiene como referencia histórica.
- Si se usa para búsqueda administrativa; hoy el backend permite buscar por `username`.
- Si se documenta en el manual como referencia no válida para login.

## 5. Orden recomendado

1. Auditar datos de producción con `docs/auditoria-datos-produccion.md`.
2. Definir el corte legacy y corregir la migración de activación con `docs/migracion-activacion-selectiva.md`.
3. Implementar la guarda de `must_change_password` y el break-glass.
4. Implementar la verificación del nuevo correo.
5. Probar en staging con cuentas sintéticas y un backup restaurado.
6. Desplegar solo con autorización escrita y plan de reversión.

## 6. Lo que no se ha hecho

- No se modificó el backend para estas propuestas.
- No se crearon comandos, tablas ni endpoints.
- No se ejecutaron migraciones ni se tocaron datos reales.
