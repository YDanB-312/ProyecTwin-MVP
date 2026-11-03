# Migración de activación: corrección selectiva (propuesta, no ejecutada)

**Estado:** propuesta de solo lectura. No se modificó la migración, no se ejecutó sobre producción ni sobre datos reales.
**Fecha:** 2-nov-2026.
**Archivo actual:** `backend/database/migrations/2026_11_02_000002_restore_legacy_activation_schema.php`.

## 1. Problema

La migración actual hace un backfill global:

```php
DB::table('general_users')
    ->whereNull('email_verified_at')
    ->update([
        'email_verified_at' => DB::raw('COALESCE(created_at, NOW())'),
    ]);
```

Eso marca como verificadas **todas** las cuentas pendientes, incluidas las creadas por el flujo nuevo de activación por correo. Si una cuenta nueva todavía no activó, la migración la deja verificada y su enlace de activación deja de ser válido.

## 2. Regla propuesta

Aplicar el backfill solo a cuentas legacy:

1. `email_verified_at IS NULL`.
2. `created_at < ACTIVATION_LEGACY_CUTOFF`.
3. Sin fila en `account_activations`.

La tercera condición evita tocar altas nuevas que ya generaron token. La segunda evita tocar una cuenta nueva que, por un fallo excepcional, quedó sin fila de activación. Si el corte no está configurado, la migración debe **abortar** en lugar de asumir un valor.

### Forma PHP propuesta

```php
$cutoff = config('auth.legacy_activation_cutoff');

if (empty($cutoff)) {
    throw new RuntimeException(
        'Define ACTIVATION_LEGACY_CUTOFF antes de ejecutar el backfill de activación.'
    );
}

DB::table('general_users')
    ->whereNull('email_verified_at')
    ->where('created_at', '<', $cutoff)
    ->whereNotExists(function ($query) {
        $query->select(DB::raw(1))
            ->from('account_activations')
            ->whereColumn('account_activations.user_id', 'general_users.id');
    })
    ->update([
        'email_verified_at' => DB::raw('COALESCE(created_at, NOW())'),
    ]);
```

`ACTIVATION_LEGACY_CUTOFF` debe ser la fecha/hora en que se desplegó por primera vez el flujo de activación por correo, acordada por el owner. Ejemplo de formato: `2026-11-03 00:00:00`.

## 3. Diagnóstico previo (solo lectura)

Ejecutar sobre una copia de seguridad restaurada o una réplica de lectura, nunca directamente sobre producción:

```sql
SELECT COUNT(*) AS total,
       SUM(email_verified_at IS NULL) AS pendientes
FROM general_users;

SELECT COUNT(*) AS con_token
FROM account_activations;

SELECT COUNT(*) AS pendientes_sin_token
FROM general_users u
WHERE u.email_verified_at IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM account_activations a WHERE a.user_id = u.id
  );

SELECT MIN(created_at) AS alta_mas_antigua,
       MAX(created_at) AS alta_mas_reciente
FROM general_users
WHERE email_verified_at IS NULL;
```

Si `pendientes_sin_token` es mayor que cero, revisar si son cuentas legacy o altas nuevas con un fallo excepcional antes de decidir el corte.

## 4. Prueba sobre copia de seguridad

1. Restaurar el backup en una base de staging aislada.
2. Registrar los conteos anteriores y los IDs de las cuentas pendientes.
3. Ejecutar la migración corregida en staging.
4. Verificar:
   - Las cuentas legacy del corte quedaron verificadas.
   - Las cuentas nuevas con token siguen pendientes.
   - Los tokens de activación vigentes siguen siendo válidos.
   - Un administrador puede iniciar sesión por correo.
5. Repetir la migración para comprobar idempotencia: no debe cambiar filas ya procesadas.

## 5. Rollback

- La migración actual tiene `down()` vacío; no se puede revertir con `migrate:rollback`.
- Si el backfill afecta cuentas incorrectas, restaurar el backup o corregir solo los IDs capturados antes de la ejecución:

```sql
UPDATE general_users
SET email_verified_at = NULL
WHERE id IN (...);
```

Esa corrección requiere autorización, backup y auditoría. No ejecutarla a ciegas.

## 6. Criterios de aceptación

- [ ] `ACTIVATION_LEGACY_CUTOFF` definido y aprobado por el owner.
- [ ] Backup restaurado y prueba de restauración completada.
- [ ] Conteos e IDs de cuentas pendientes registrados.
- [ ] Migración probada en staging con cuentas legacy y nuevas.
- [ ] Tokens de activación vigentes preservados.
- [ ] Al menos un administrador puede entrar por correo.
- [ ] Plan de rollback y responsable definidos.

## 7. Lo que no se ha hecho

- No se modificó la migración.
- No se ejecutó en producción ni en la base de desarrollo.
- No se normalizaron correos ni se modificaron cuentas.
- No se tocaron datos reales.
