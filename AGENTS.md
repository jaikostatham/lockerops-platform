# LockerOps Platform

Backend REST de lockers con Java 21, Spring Boot 3.5.14, Gradle, JPA, PostgreSQL runtime, H2 tests y springdoc-openapi. Este archivo debe ser corto; el detalle vive en `docs/`.

## Comandos

- `.\gradlew.bat bootRun` - local.
- `.\gradlew.bat test` - tests.
- `.\gradlew.bat build` - verificacion completa.
- Flyway gestiona las migraciones SQL; Hibernate valida el esquema al arrancar.
- No hay lint ni formatter configurados.

## Codigo y arquitectura

- Vertical slices: `station`, `compartment`, `reservation`, `ticket`.
- Flujo: `Controller -> Service -> Repository`.
- DTOs en `api/dto/request|response`; mappers manuales en `application/mapper`.
- Constructor injection, transacciones en servicios, entidades JPA sin Lombok.
- Errores API con `ApiException`, `ApiErrorCode`, `ApiErrorResponse`, `GlobalExceptionHandler` y `service.code`.

## Contratos criticos

- No cambiar requests/responses sin revisar controller, DTO, tests, OpenAPI y `request.http`.
- `POST /api/reservations` crea reserva y emite ticket + access code.
- `POST /api/access-codes/validate` requiere `ticketCode` y `accessCode`.
- Access codes: persistir hash/preview, nunca codigo real en claro.
- Mantener coherencia entre estados de reserva, ticket, access code y compartimento.
- Si docs antiguas contradicen codigo/tests, priorizar codigo/tests.

## Git Flow y permisos

- Base habitual: `develop`; no trabajar directo en `main`.
- Ramas desde `develop` actualizado: `feature/...`, `fix/...`, `docs/...`.
- Para stage/commit/push/merge, Codex hace una unica propuesta completa; si el usuario aprueba, ejecuta todo el bloque aprobado.
- La propuesta debe incluir rama, archivos, mensaje de commit, destino remoto y riesgos.
- No hacer `commit`, `merge`, `rebase`, `push`, `pull` con cambios, borrar ramas ni resolver conflictos sin permiso explicito.
- Antes de rama/commit/push/merge: comprobar rama actual, estado local y remoto.
- No usar `git add .`; proponer archivos concretos. Si hay cambios ajenos, avisar y no tocarlos.

## Verificacion

- Logica: `.\gradlew.bat test`.
- Cambio amplio/cierre de feature: `.\gradlew.bat build`.
- API: revisar `request.http`, OpenAPI y tests.
- Config: revisar `application.yml`, `.env.example`, `application-test.yml`.
- Si no se ejecuta algo esperado, explicar por que.

## Contexto y memoria

- Consumir minimo contexto: leer solo docs necesarios.
- Antes de compactar una conversacion larga, preparar handoff.
- Al cerrar rama, proponer resumen con `docs/feature-summary-template.md`.
- Si se aprueba, guardarlo en `docs/feature-summaries/YYYY-MM-DD-nombre-rama.md`.
- El contexto estable vive en `docs/`, no en la conversacion.

## No hacer

- No tocar secretos, `.env`, `.env.*` ni `application-local.yml` salvo peticion explicita.
- No instalar dependencias sin permiso.
- No cambiar entidades, relaciones JPA, DB o codigos numericos de error sin analisis previo.
- No hacer refactors globales ni subir nada sin revision del usuario.

## Docs bajo demanda

`docs/project-context.md`, `docs/architecture-overview.md`, `docs/api-contract.md`, `docs/testing-and-verification.md`, `docs/ai-workflow.md`, `docs/code-review.md`.
