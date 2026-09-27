# Testing And Verification

## Objetivo

Definir que debe comprobar Codex antes de entregar cambios en LockerOps Platform.

## Comandos disponibles

- `.\gradlew.bat bootRun` - arranca la aplicacion local.
- `.\gradlew.bat test` - ejecuta tests automatizados.
- `.\gradlew.bat build` - compila y ejecuta verificacion Gradle completa.

No hay tareas Gradle configuradas para lint, formatter o coverage, ni un plugin
Gradle independiente para Flyway/Liquibase. Flyway si esta integrado con Spring
Boot y ejecuta las migraciones versionadas al iniciar la aplicacion. La imagen de
contenedor se define en `Dockerfile`; el despliegue y su flujo estan descritos en
`docs/deployment-pipeline.md`.

## Entorno de tests

- Tests con JUnit Platform.
- Spring Boot Test.
- MockMvc para endpoints HTTP.
- Perfil `test`.
- H2 en memoria con modo PostgreSQL.
- Flyway desactivado en este perfil de tests.
- Configuracion en `src/test/resources/application-test.yml`.

## Tests actuales relevantes

- `LockerOpsPlatformApplicationTests` - carga de contexto.
- `ApiContractIntegrationTests` - contrato HTTP de la API.
- `TestingProfileApiIntegrationTests` - comportamiento del perfil publico de Testing.
- `ReadOnlyApiFilterTests` - restricciones de lectura y catalogo publico.
- `HealthControllerTest` - endpoint de salud.
- `TicketAccessCodesIntegrationTests` - emision y validacion de tickets/access codes.
- `ReservationExpirationIntegrationTests` - expiracion de reservas y efectos asociados.

## Criterio por tipo de cambio

### Solo documentacion

- Revisar diff.
- Comprobar rutas y nombres de archivos.
- No hace falta ejecutar tests de backend salvo que la doc incluya ejemplos de contrato dudosos.

### Logica de backend

- Ejecutar `.\gradlew.bat test`.
- Revisar tests relacionados con el slice tocado.
- Si se toca flujo reserva-ticket-access code, revisar tests de integracion existentes.

### Cambio amplio

- Ejecutar `.\gradlew.bat build`.
- Revisar que no haya cambios accidentales en config o docs locales.

### Cambio de API

Comprobar:

- Controller.
- Request DTO.
- Response DTO.
- Mapper si aplica.
- Service.
- Tests.
- OpenAPI annotations.
- `request.http`.
- `docs/api-contract.md` si el contrato queda estable.

### Cambio de errores

Comprobar:

- `ApiErrorCode`.
- `GlobalExceptionHandler`.
- `ApiErrorResponse`.
- `service.code` en `application.yml`.
- Tests que esperan `status` o `code`.

### Cambio de configuracion

Comprobar:

- `src/main/resources/application.yml`.
- `src/test/resources/application-test.yml`.
- `.env.example`.
- No tocar `application-local.yml` salvo permiso explicito.

### Cambio de persistencia

Precauciones:

- Revisar entidades, relaciones, constraints e indexes.
- Crear una migracion Flyway versionada en `src/main/resources/db/migration` cuando cambie el esquema.
- No asumir que Hibernate o `ddl-auto=validate` migra datos o cambia el esquema.
- Verificar compatibilidad PostgreSQL aunque tests usen H2.

## Antes de cerrar una tarea

Codex debe informar:

- Comandos ejecutados.
- Resultado de cada comando.
- Si no ejecuto una verificacion esperada, explicar por que.
- Riesgo residual.
- Archivos principales afectados.

## Antes de commit, push o abrir una PR

Requiere autorizacion explicita del usuario para el bloque de publicacion.

Antes de proponer la operacion:

- Confirmar rama actual.
- Confirmar estado local.
- Comprobar remoto si la operacion puede afectar a otros.
- Proponer archivos concretos para stage.
- Proponer mensaje de commit si aplica y la rama destino de la PR.

La autorizacion se solicita una sola vez para el bloque completo. Si el usuario
autoriza stage + commit + push + PR, Codex ejecuta esos pasos sin pedir nuevos OK
intermedios.

## Merge y promocion a produccion

- El usuario revisa y hace manualmente todos los merges de PR; Codex no mergea.
- Las PR de trabajo se dirigen a `develop`.
- La promocion de `develop` a `main` solo se prepara cuando el usuario la ordena expresamente.
