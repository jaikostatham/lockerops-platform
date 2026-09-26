# Entornos PostgreSQL

## Bases y perfiles

Trabajaremos con dos nombres de base: `lockerops_test` para local y testing,
y `lockerops_prod` para el portfolio, usando los perfiles `local`, `testing`
y `prod`.

| Entorno | Perfil Spring | Base | Puerto de la API |
| --- | --- | --- | --- |
| Local | `local` | `lockerops_test` | `8080` |
| Testing | `testing` | `lockerops_test` | `8081` en local; el servicio dará su puerto al alojarse |
| Portfolio | `prod` | `lockerops_prod` | El que asigne el alojamiento |
| Pruebas automáticas | `test` | H2 en memoria | No aplica |

El nombre por sí solo no identifica una base física: también importa el
servidor al que se conecta la aplicación. Hoy el backend local usa
PostgreSQL en este equipo y todavía no hay una instancia remota de testing.
Para que local y testing compartan exactamente los mismos datos, ambos
tendrán que conectarse a la misma instancia de `lockerops_test`. En ese
caso, los cambios hechos desde local también se verán en testing; por eso
no cambiaremos el servidor de conexión sin revisar antes el riesgo y la
copia de seguridad.

La base local `lockerops_prod` y una futura base remota con el mismo nombre
serían instancias distintas. No se ha copiado información local a un
proveedor externo.

## Arranque local

Requisitos: Java 21 y PostgreSQL en `localhost:5432`. La contraseña no se
escribe en el repositorio ni en estos comandos; permanece en el
Administrador de credenciales de Windows o en el entorno seguro del
proceso.

Desde `lockerops-platform`, en PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:DB_NAME = 'lockerops_test'
$env:DB_SSLMODE = 'disable'
.\gradlew.bat bootRun
```

El perfil `testing` también puede ejecutarse en el equipo para practicar:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'testing'
.\gradlew.bat bootRun
```

El perfil `testing` usa el puerto `8081`, bloquea las escrituras y solo expone
el catálogo. El perfil `local` conserva el flujo de prueba del backend.
No ejecutes `prod` contra una base existente sin revisar antes su destino,
sus credenciales y una copia verificada.

El frontend local deja `VITE_API_BASE_URL` vacía y usa el proxy de Vite
`/api` hacia `http://localhost:8080`. Si el backend local escucha en
`8081`, configura `VITE_DEV_PROXY_TARGET` como
`http://localhost:8081`.

## Migraciones y copias

Flyway gestiona el esquema. `V1__initial_schema.sql` crea las tablas en una
base nueva. `V2__require_locker_station_fields.sql` aplica restricciones
de campos obligatorios a las bases existentes. Flyway se ejecuta al
arrancar el backend, así que un `bootRun` puede cambiar el esquema.

Antes de aplicar una migración a una base existente, crea y valida una
copia de seguridad independiente. Las copias ya verificadas se conservan
fuera de los repositorios; nunca se suben dumps, credenciales ni datos
reales a GitHub. No elimines la base de origen hasta validar tanto la copia
como la restauración.

## Acceso de aplicaciones

Las URLs públicas no requieren autenticación. Los perfiles `testing` y
`prod` bloquean escrituras HTTP y limitan las lecturas al catálogo.
Cuando se creen servicios remotos, sus usuarios de aplicación tendrán
solo los permisos necesarios para leer; las migraciones usarán credenciales
separadas, guardadas en el gestor de secretos del proveedor. No uses el rol
`postgres` en Internet y no cargues reservas, códigos ni referencias de
personas reales en el portfolio.

El servicio `prod` requiere `DB_HOST`, `DB_USERNAME`, `DB_PASSWORD`,
`DB_MIGRATION_USERNAME`, `DB_MIGRATION_PASSWORD`, `ACCESS_CODE_HASH_SECRET`
y `CORS_ALLOWED_ORIGINS`. Este último debe contener el origen HTTPS exacto
del frontend; admite varios orígenes separados por comas, pero no `*`.
En testing remoto también habrá que configurar el origen del frontend.
CORS limita qué páginas pueden llamar a la API desde un navegador; no
autentica a quien haga una petición directa.
