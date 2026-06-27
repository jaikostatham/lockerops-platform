# Testing And Verification

## Objetivo

Definir que debe comprobar Codex antes de entregar cambios en LockerOps Platform.

## Comandos disponibles

- `.\gradlew.bat bootRun` - arranca la aplicacion local.
- `.\gradlew.bat test` - ejecuta tests automatizados.
- `.\gradlew.bat build` - compila y ejecuta verificacion Gradle completa.

No hay comandos configurados para lint, formatter, coverage, Docker, Flyway o Liquibase.

## Entorno de tests

- Tests con JUnit Platform.
- Spring Boot Test.
- MockMvc para endpoints HTTP.
- Perfil `test`.
- H2 en memoria con modo PostgreSQL.
- Configuracion en `src/test/resources/application-test.yml`.

## Tests actuales relevantes

- `LockerOpsPlatformApplicationTests` - carga de contexto.
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
- Recordar que no hay Flyway/Liquibase.
- No asumir que `ddl-auto=update` migra datos correctamente.
- Verificar compatibilidad PostgreSQL aunque tests usen H2.

## Antes de cerrar una tarea

Codex debe informar:

- Comandos ejecutados.
- Resultado de cada comando.
- Si no ejecuto una verificacion esperada, explicar por que.
- Riesgo residual.
- Archivos principales afectados.

## Antes de commit, push o merge

Requiere permiso explicito del usuario.

Antes de proponer la operacion:

- Confirmar rama actual.
- Confirmar estado local.
- Comprobar remoto si la operacion puede afectar a otros.
- Proponer archivos concretos para stage.
- Proponer mensaje de commit si aplica.
- Esperar revision del usuario.

La confirmacion debe pedirse una sola vez para el bloque completo. Si el usuario aprueba stage + commit + push, Codex ejecuta esos pasos sin pedir nuevos OK intermedios.
