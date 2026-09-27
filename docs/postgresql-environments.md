# Entornos PostgreSQL

## Instancias y perfiles

| Entorno | Perfil Spring | Servidor y base | Acceso |
| --- | --- | --- | --- |
| Local | `local` | PostgreSQL `localhost:5432`, base `lockerops_test` por defecto | Flujo local de backend |
| Testing | `testing` | Neon proyecto `lockerops-test`, rama `testing`, base `lockerops_test` | Solo lectura de estaciones y compartimentos |
| Producción | `prod` | Neon proyecto `lockerops-prod`, rama `production`, base `lockerops_prod` | Solo lectura de estaciones y compartimentos |
| Tests | `test` | H2 en memoria | Automatización |

El nombre de la base no identifica el servidor. La base local `lockerops_test` y la base de Neon Testing `lockerops_test` son instancias distintas. El host efectivo del backend local puede cambiarse desde la configuración de ejecución de IntelliJ.

## Conexión local

`application.yml` usa PostgreSQL local, puerto `5432`, base `lockerops_test` y `sslmode=disable` por defecto. Configura `DB_PASSWORD` de forma segura y selecciona el perfil `local` antes de ejecutar `bootRun`.

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:DB_HOST = 'localhost'
$env:DB_PORT = '5432'
$env:DB_NAME = 'lockerops_test'
$env:DB_SSLMODE = 'disable'
.\gradlew.bat bootRun
```

No guardes contraseñas en Git ni las pegues en documentación, capturas o historial compartido. El perfil local puede ejecutar los flujos de reserva; úsalo únicamente con datos de desarrollo.

## Variables de los backends remotos

| Variable | Propósito |
| --- | --- |
| `DB_HOST` | Endpoint de Neon del entorno correspondiente |
| `DB_PORT` | Puerto PostgreSQL; Neon usa `5432` |
| `DB_NAME_TESTING` / `DB_NAME_PROD` | Base del entorno `testing` / `prod` |
| `DB_SSLMODE` | `require` para conexiones a Neon |
| `DB_USERNAME` / `DB_PASSWORD` | Usuario de ejecución de la API: `lockerops_runtime` y su contraseña |
| `DB_MIGRATION_USERNAME` / `DB_MIGRATION_PASSWORD` | Credenciales que Flyway utiliza al iniciar el backend; actualmente el usuario de migraciones de Neon |
| `CORS_ALLOWED_ORIGINS` | Origen HTTPS exacto del kiosco del mismo entorno |
| `ACCESS_CODE_HASH_SECRET` | Secreto aleatorio requerido por el perfil remoto; no debe ser igual a una contraseña de base de datos |

Configura Testing y Producción con los valores de sus propias conexiones de Neon. No copies el host o la base de un entorno al otro. Los perfiles remotos usan `sslmode=require`, son de solo lectura, restringen la API al catálogo público y desactivan Swagger UI y `/v3/api-docs`.

## Separación de roles

La API se conecta como `lockerops_runtime`. En Testing y Producción se comprobó que puede conectar a la base, usar el esquema `public` y leer `locker_stations` y `locker_compartments`; no puede leer `reservations`, `tickets` ni `access_codes`. No uses `neondb_owner` como usuario de ejecución.

Flyway recibe credenciales separadas mediante `DB_MIGRATION_USERNAME` y `DB_MIGRATION_PASSWORD`. El usuario de migración actual tiene más privilegios que el runtime y esas credenciales se conservan como secretos en Render. El proceso de la API las necesita durante el arranque porque Flyway migra el esquema en esa fase. Antes de manejar datos reales, evalúa un usuario de migración dedicado o un proceso separado.

No incluyas usuarios o contraseñas reales en comandos, scripts, GitHub, capturas ni archivos de configuración versionados. CORS limita llamadas desde navegadores; no autentica solicitudes directas.

## Flyway, datos y copias

Flyway aplica las migraciones al arrancar el backend. Las migraciones actuales son:

- `V1__initial_schema.sql`: esquema inicial.
- `V2__require_locker_station_fields.sql`: restricciones de campos obligatorios.
- `V3__seed_public_demo_catalog.sql`: añade tres estaciones ficticias y doce compartimentos únicamente si `locker_stations` está vacía; si ya hay estaciones, conserva el catálogo existente.

V3 se aplicó correctamente en Testing y Producción; los servicios quedaron en esquema v3. El endpoint público devolvió tres estaciones tras la promoción a Producción.

Antes de cualquier migración que altere o elimine datos existentes, revisa el SQL y valida una copia de seguridad restaurable. V3 es de inserción condicional; no sustituye ni borra estaciones existentes. No subas dumps, secretos ni datos reales al repositorio.
