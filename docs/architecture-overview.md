# Architecture Overview

## Estilo general

El proyecto usa vertical slice architecture con una separacion ligera por capas dentro de cada slice.

Estructura tipica:

```text
slice/
  api/
    controller/
    dto/
      request/
      response/
  application/
    mapper/
    service/
  domain/
    enums/
    model/
  infrastructure/
    persistence/
      repository/
```

## Slices actuales

- `station`: CRUD de estaciones de lockers.
- `compartment`: CRUD de compartimentos y pertenencia a estaciones.
- `reservation`: creacion, consulta, cancelacion y expiracion de reservas.
- `ticket`: emision de tickets, access codes y validacion de acceso.
- `shared.exception`: contrato comun de errores.
- `config`: configuracion transversal como OpenAPI.

## Flujo de capas

```text
HTTP request
  -> Controller
  -> Request DTO + validation
  -> Service
  -> Repository
  -> JPA/Hibernate
  -> Database
  -> Mapper
  -> Response DTO
```

Los controladores no deben acceder directamente a repositorios. La logica de negocio vive en servicios.

## Reglas de persistencia

- Entidades JPA en `domain/model`.
- Repositorios Spring Data en `infrastructure/persistence/repository`.
- Relaciones con `FetchType.LAZY` cuando ya esta en el patron existente.
- Identificadores internos con `Long`.
- Estados persistidos como `EnumType.STRING`.
- Timestamps con `Instant`.
- Crear o cambiar relaciones JPA requiere analisis previo.

## Flujos criticos

### Crear reserva

1. Validar que el compartimento existe.
2. Validar que esta disponible.
3. Validar que no tiene reserva activa.
4. Crear reserva `CONFIRMED`.
5. Cambiar compartimento a `RESERVED`.
6. Emitir ticket.
7. Emitir access code.
8. Devolver respuesta orientada al kiosk.

### Cancelar reserva

1. Buscar reserva.
2. Validar que puede cancelarse.
3. Cambiar reserva a `CANCELLED`.
4. Liberar compartimento.
5. Cancelar ticket asociado.
6. Revocar access codes activos.

### Expirar reserva

1. Buscar reservas `CONFIRMED` vencidas.
2. Marcar reserva como `EXPIRED`.
3. Liberar compartimento si estaba `RESERVED`.
4. Expirar ticket asociado si estaba `ISSUED`.
5. Expirar access codes activos.
6. Mantener el proceso idempotente.

### Validar access code

1. Buscar ticket por `ticketCode`.
2. Hashear `accessCode`.
3. Buscar access code por ticket y hash.
4. Validar estado del access code.
5. Validar ventana temporal.
6. Validar estado del ticket.
7. Validar estado de la reserva.
8. Registrar uso correcto.

## Errores

El contrato de errores se centraliza en:

- `ApiErrorCode`.
- `ApiException`.
- `ApiErrorResponse`.
- `GlobalExceptionHandler`.
- `service.code` en `application.yml`.

No cambiar codigos numericos sin revisar compatibilidad.

## Consideraciones de diseño

- Mantener coherentes los estados de reserva, ticket, código de acceso y compartimento.
- Persistir los códigos de acceso como hashes, no como texto claro.
- Gestionar los cambios de esquema mediante migraciones versionadas.
- Mantener la lógica de negocio en los servicios y verificar los contratos de API al modificarlos.
