# Auditoría de datos de producción: consultas de solo lectura

**Estado:** preparado, no ejecutado. Requiere autorización escrita y acceso de solo lectura.
**Objetivo:** conocer el estado real de las cuentas antes de desplegar el login exclusivo por correo.

## 1. Reglas de ejecución

1. Autorización escrita del owner y acceso con usuario MySQL de solo `SELECT` o réplica de lectura.
2. No usar `SELECT *`, ni seleccionar `correo`, `username`, `nombre`, `numero_documento`, tokens o filas de auditoría.
3. No usar `INTO OUTFILE`, `mysqldump`, exportaciones del panel ni capturas con filas.
4. Ejecutar en transacción de solo lectura y cerrar con `ROLLBACK`.
5. Si falta una columna o tabla, registrar la diferencia y detenerse; no crearla.
6. Si un conteo es mayor que cero y se necesitan identificar cuentas, hacer un procedimiento separado con IDs y datos enmascarados, aprobado por el owner.
7. Guardar solo consultas y conteos agregados; no conservar PII.

## 2. Consultas

```sql
-- Ejecutar con usuario de solo lectura, preferiblemente en réplica.
SET SESSION TRANSACTION READ ONLY;
START TRANSACTION;

-- Q0. Metadatos: no contienen PII.
SELECT VERSION() AS mysql_version,
       @@character_set_database AS charset_db,
       @@collation_database AS collation_db;

SELECT TABLE_NAME, ENGINE, TABLE_COLLATION, TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('general_users', 'account_activations', 'password_reset_tokens', 'sessions');

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLLATION_NAME, EXTRA
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'general_users'
ORDER BY ORDINAL_POSITION;

SELECT INDEX_NAME, NON_UNIQUE, SEQ_IN_INDEX, COLUMN_NAME, COLLATION
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'general_users'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- Q1. Calidad global del correo. Solo conteos.
SELECT
  COUNT(*) AS total_usuarios,
  SUM(correo IS NULL) AS correo_nulo,
  SUM(TRIM(correo) = '') AS correo_vacio,
  SUM(CAST(correo AS BINARY) <> CAST(TRIM(correo) AS BINARY)) AS espacios_extremos,
  SUM(CAST(correo AS BINARY) <> CAST(LOWER(TRIM(correo)) AS BINARY)) AS no_normalizado,
  SUM(CAST(correo AS BINARY) REGEXP '[A-Z]') AS con_mayusculas,
  SUM(correo REGEXP '[[:space:]]') AS con_espacios,
  SUM(CHAR_LENGTH(correo) > 254) AS longitud_mayor_254,
  SUM(correo NOT REGEXP '^[^@ ]+@[^@ ]+[.][^@ ]+$') AS formato_sospechoso
FROM general_users;

-- Q2. Duplicados por correo normalizado. No devuelve correos.
SELECT
  COUNT(*) AS grupos_duplicados,
  COALESCE(SUM(n - 1), 0) AS cuentas_excedentes,
  MAX(n) AS max_cuentas_mismo_correo
FROM (
  SELECT LOWER(TRIM(correo)) AS correo_norm, COUNT(*) AS n
  FROM general_users
  WHERE correo IS NOT NULL AND TRIM(correo) <> ''
  GROUP BY LOWER(TRIM(correo))
  HAVING COUNT(*) > 1
) AS d;

-- Q3. Rol, estado y activación. Requiere email_verified_at.
SELECT rol, estado,
       CASE WHEN email_verified_at IS NULL THEN 'pendiente' ELSE 'activada' END AS activacion,
       COUNT(*) AS total
FROM general_users
GROUP BY rol, estado, activacion
ORDER BY rol, estado, activacion;

-- Q4. Administradores: riesgo de bloqueo total.
SELECT
  COUNT(*) AS admins,
  SUM(estado = 0) AS suspendidos,
  SUM(estado IS NULL) AS estado_nulo,
  SUM(email_verified_at IS NULL) AS pendientes,
  SUM(correo IS NULL OR TRIM(correo) = ''
      OR correo NOT REGEXP '^[^@ ]+@[^@ ]+[.][^@ ]+$') AS correo_sospechoso,
  SUM(estado = 1 AND email_verified_at IS NOT NULL
      AND correo REGEXP '^[^@ ]+@[^@ ]+[.][^@ ]+$') AS candidatos_login
FROM general_users
WHERE rol = 'admin';

-- Q5. Legado de contraseña temporal. Solo si existen las columnas.
SELECT
  SUM(must_change_password = 1) AS must_change_1,
  SUM(password_temporal IS NOT NULL) AS con_password_temporal,
  SUM(must_change_password = 1 AND password_temporal IS NOT NULL) AS ambos,
  SUM(must_change_password = 1 AND estado = 1) AS activos_con_cambio_pendiente
FROM general_users;

-- Q6. Activaciones. No seleccionar `error` ni tokens.
SELECT
  COUNT(*) AS total,
  SUM(used_at IS NOT NULL) AS usadas,
  SUM(used_at IS NULL AND expires_at < NOW()) AS vencidas,
  SUM(used_at IS NULL AND expires_at >= NOW()) AS vigentes,
  SUM(sent_at IS NULL) AS sin_envio,
  SUM(error IS NOT NULL) AS con_error
FROM account_activations;

-- Q7. Pendientes y estado de su enlace.
SELECT u.rol, u.estado,
       SUM(a.id IS NULL) AS sin_token,
       SUM(a.id IS NOT NULL AND a.used_at IS NULL AND a.expires_at >= NOW()) AS token_vigente,
       SUM(a.id IS NOT NULL AND (a.used_at IS NOT NULL OR a.expires_at < NOW())) AS token_no_vigente
FROM general_users u
LEFT JOIN account_activations a ON a.user_id = u.id
WHERE u.email_verified_at IS NULL
GROUP BY u.rol, u.estado
ORDER BY u.rol, u.estado;

-- Q8. Tokens de recuperación; el TTL por defecto es 60 minutos.
SELECT
  COUNT(*) AS total,
  SUM(created_at IS NULL) AS sin_fecha,
  SUM(created_at < NOW() - INTERVAL 60 MINUTE) AS vencidos,
  SUM(created_at >= NOW() - INTERVAL 60 MINUTE) AS vigentes,
  SUM(CAST(email AS BINARY) <> CAST(LOWER(TRIM(email)) AS BINARY)) AS no_normalizados
FROM password_reset_tokens;

-- Q9. Relaciones reales: confirma que las dependencias usan id, no username.
SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_SCHEMA = DATABASE()
  AND REFERENCED_TABLE_NAME = 'general_users'
ORDER BY TABLE_NAME, COLUMN_NAME;

-- Q10. Calidad de username. No es credencial de login.
SELECT
  COUNT(*) AS total,
  SUM(username IS NULL) AS username_nulo,
  SUM(TRIM(username) = '') AS username_vacio,
  COUNT(DISTINCT username) AS username_distintos
FROM general_users;

-- Q11. Posibles correos de demo/placeholder en producción.
SELECT
  SUM(LOWER(correo) REGEXP '^[^@]+@(example[.](com|org|net)|test[.]local|localhost|invalid)$') AS dominios_reservados,
  SUM(LOWER(correo) REGEXP '^(sin|no)[._-]?correo@|^n/?a@|^ninguno@') AS local_placeholder
FROM general_users;

ROLLBACK;
```

## 3. Interpretación

| Señal | Acción |
|---|---|
| `correo_nulo > 0`, `correo_vacio > 0` o `formato_sospechoso > 0` | Detenerse. Hay datos que el nuevo login no puede usar; corregir desde administración con autorización. |
| `grupos_duplicados > 0` | Detenerse. Login y recuperación son ambiguos; deduplicar antes del despliegue. |
| `no_normalizado > 0` o `con_mayusculas > 0` | Verificar colación. Bajo MySQL CI puede funcionar, pero no es determinista; normalizar con backup. |
| `admins_pendientes > 0`, `admins_suspendidos > 0`, `correo_sospechoso > 0` o `candidatos_login = 0` | Bloqueante operativo: revisar acceso del owner y break-glass. |
| `must_change_1 > 0` | Decisión del owner: forzar reset/activación o aceptar la contraseña temporal. |
| Pendientes con `token_no_vigente > 0` o `sin_token > 0` | Reenviar activación o indicar recuperación por correo. |
| `sin_envio > 0` o `con_error > 0` | Revisar Brevo/SMTP antes de anunciar el cambio. |
| `password_reset_tokens.no_normalizados > 0` | Riesgo de tokens no revocados si la colación es sensible a mayúsculas; normalizar/limpiar. |
| `username_nulo > 0` o `username_vacio > 0` | No afecta login; sí afecta pantallas que muestran “usuario institucional”. |
| Índice único de `correo` ausente | Bloqueante de integridad: recrear después de deduplicar. |

## 4. Registro de ejecución

| Campo | Valor |
|---|---|
| Fecha y hora | |
| Autorizó | |
| Ejecutó | |
| Entorno / réplica | |
| Resultado Q1 (conteos) | |
| Resultado Q2 (duplicados) | |
| Resultado Q4 (admins) | |
| Incidencias | |
| Siguiente acción | |

## 5. Límites

- No se ejecutó ninguna consulta.
- No se accedió a producción.
- No se normalizaron, deduplicaron ni modificaron correos.
- Cualquier remediación requiere un plan separado, backup y aprobación.
