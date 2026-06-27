# Code Review Checklist

## Objetivo

Checklist para revisar cambios hechos por humanos o por Codex antes de aceptar, commitear, subir o mergear.

## Revision general

- El cambio responde a la tarea pedida.
- No hay archivos no relacionados.
- No hay refactors globales innecesarios.
- No hay cambios de formato masivos sin motivo.
- No hay secretos, credenciales ni valores locales reales.
- No se ha tocado `application-local.yml` salvo peticion explicita.

## Arquitectura

- Se mantiene vertical slice architecture.
- El controller no accede a repositorios.
- La logica de negocio esta en servicios.
- Los DTOs no exponen entidades JPA.
- Los mappers siguen siendo manuales.
- Las transacciones estan en servicios.
- No se introduce una abstraccion nueva sin necesidad real.

## Contrato API

- Si cambia un endpoint, revisar controller, DTOs, tests, OpenAPI annotations y `request.http`.
- Mantener codigos HTTP coherentes.
- Mantener `ApiErrorResponse` como formato comun de error.
- No cambiar codigos numericos de `ApiErrorCode` sin justificar compatibilidad.
- No devolver datos sensibles.

## Dominio

- Crear reserva debe seguir emitiendo ticket y access code.
- Cancelar reserva debe liberar compartimento y revocar access codes.
- Expirar reserva debe liberar compartimento y expirar ticket/access codes.
- Validar access code debe comprobar ticket, hash, estado, vigencia y reserva.
- Los access codes reales no deben persistirse en claro.

## Persistencia

- Revisar cualquier cambio en entidades JPA.
- Revisar relaciones, constraints, indexes y nombres de columnas.
- Recordar que no hay Flyway/Liquibase.
- No asumir que `ddl-auto=update` resuelve cambios de datos.
- Tests usan H2, pero runtime usa PostgreSQL.

## Tests

- Cambios de logica deben tener tests o justificar por que no.
- Tests de integracion deben usar perfil `test`.
- Verificar flujos felices y errores relevantes.
- Evitar tests dependientes de orden o datos locales.
- Para bugs, preferir test que reproduzca el caso.

## Git

- Confirmar rama correcta.
- Confirmar que no hay cambios ajenos mezclados.
- Proponer stage selectivo.
- Proponer mensaje de commit.
- Antes de push o merge, comprobar remoto.
- El usuario revisa una propuesta agrupada antes de commit, push o merge.

## Entrega

El resumen final debe incluir:

- Que se cambio.
- Archivos principales.
- Verificacion ejecutada.
- Riesgos o pendientes.
- Si no se ejecuto algo, explicar por que.
