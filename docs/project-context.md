# LockerOps Platform - Project Context

## Proposito

LockerOps Platform es un backend REST para gestionar estaciones fisicas de lockers, sus compartimentos, reservas, tickets y codigos de acceso reutilizables.

El objetivo actual es mantener un backend claro, verificable y preparado para que un frontend kiosk o administrativo consuma sus endpoints sin romper contratos.

## Estado actual

- Proyecto backend monolitico en Java 21 y Spring Boot 3.5.14.
- Arquitectura por vertical slices: `station`, `compartment`, `reservation`, `ticket`.
- Persistencia con Spring Data JPA e Hibernate.
- PostgreSQL para ejecucion local y en los servicios desplegados; H2 en memoria para tests.
- Flyway gestiona las migraciones SQL versionadas en `src/main/resources/db/migration`; Hibernate valida el esquema con `ddl-auto=validate`.
- OpenAPI/Swagger mediante springdoc-openapi.
- Manejo centralizado de errores con codigos numericos propios.
- No hay frontend dentro de este repositorio.
- No hay lint/formatter formal configurado.

Los perfiles desplegados `testing` y `prod` mantienen la API en solo lectura y
publican unicamente el catalogo de estaciones y compartimentos. Consulta
`docs/deployment-pipeline.md` y `docs/postgresql-environments.md` antes de cambiar
el comportamiento de un entorno o sus permisos de base de datos.

## Fuentes de verdad

Cuando haya dudas, priorizar en este orden:

1. Codigo real en `src/main/java`.
2. Tests en `src/test/java`.
3. Configuracion versionada en `src/main/resources` y `src/test/resources`.
4. `request.http` para escenarios manuales.
5. `docs/api-contract.md` para resumen del contrato HTTP.
6. Documentacion en `docs/`.
7. Documentacion local ignorada por Git, solo como referencia secundaria.

## Contratos importantes

- `POST /api/reservations` crea una reserva y devuelve tambien `ticketCode` y `accessCode`.
- `POST /api/access-codes/validate` valida combinando `ticketCode` y `accessCode`.
- Los access codes reales solo deben mostrarse al emitirlos. En persistencia se guarda hash y preview.
- La cancelacion de una reserva libera el compartimento y revoca access codes activos.
- La expiracion de una reserva libera el compartimento y expira ticket/access codes.
- Los errores publicos usan `ApiErrorCode`, mensajes `service.code` y `ApiErrorResponse`.

## Git Flow del proyecto

- Se trabaja desde `develop`.
- Cada tema tiene su propia rama semantica creada desde `develop` actualizado.
- Las PR de trabajo se dirigen a `develop`.
- El usuario revisa y hace los merges manualmente; Codex no mergea PRs.
- La promocion de `develop` a `main` se hace solo cuando el usuario la ordena expresamente.
- No se trabaja directamente en `main`.
- Los commits y pushes se realizan dentro del bloque autorizado por el usuario.
- Antes de subir o mergear, comprobar si hay cambios remotos pendientes.

## Politica de contexto

Para ahorrar tokens:

- No cargar todo el repositorio si la tarea toca un slice concreto.
- No cargar toda la documentacion si basta con `AGENTS.md` y un doc puntual.
- Al cerrar una feature, proponer resumen durable en `docs/feature-summaries/` si aporta valor futuro.
- Integrar en docs principales solo lo que pase a ser contexto estable del proyecto.
- Compactar conversaciones largas despues de dejar un handoff claro.

## Fuera de alcance por ahora

- MCP.
- Context7.
- Automatizaciones.
- Comandos personalizados avanzados.
- Integraciones externas.
- Frontend externo.
