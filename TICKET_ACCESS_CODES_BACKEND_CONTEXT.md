# LockerOps Platform - Ticket & Access Codes

Este documento explica la feature de `Ticket` y `AccessCode` implementada en el backend de LockerOps Platform.

## 1. Objetivo

La fase `Ticket & Access Codes` añade el flujo necesario para que una reserva confirmada genere:

- Un `Ticket`: comprobante visible para el cliente.
- Un `AccessCode`: código reutilizable que el kiosk puede validar para conceder acceso simulado.

El flujo queda preparado para un futuro kiosk real, impresión/reimpresión, recuperación de tickets, trazabilidad, pagos, penalizaciones o eventos de hardware, pero sin implementar esas piezas todavía.

## 2. Arquitectura

El proyecto mantiene la arquitectura actual:

```text
Vertical Slice Architecture
+
Hexagonal / Clean Architecture ligera dentro de cada slice
```

La feature se implementa como un nuevo slice:

```text
src/main/java/com/jaico/lockerops/ticket
├── api
│   ├── controller
│   └── dto
│       ├── request
│       └── response
├── application
│   ├── mapper
│   └── service
├── domain
│   ├── enums
│   └── model
└── infrastructure
    └── persistence
        └── repository
```

No se han creado puertos/interfaces hexagonales todavía. Los servicios siguen actuando como casos de uso y los repositorios son Spring Data JPA.

## 3. Relación Entre Entidades

Relación conceptual:

```text
Reservation 1 --- 1 Ticket 1 --- N AccessCode
Reservation N --- 1 LockerCompartment
```

En esta fase se crea un único `AccessCode` activo por `Ticket`, pero se modela como `Ticket 1 --- N AccessCode` para permitir regeneración futura sin rediseñar la base.

## 4. Ticket

Archivo principal:

```text
src/main/java/com/jaico/lockerops/ticket/domain/model/Ticket.java
```

Responsabilidad:

- Representa el comprobante visible de una reserva.
- Tiene un código público único: `ticketCode`.
- Se asocia a una única `Reservation`.
- Tiene ciclo de vida propio mediante `TicketStatus`.
- Sirve para consultar una reserva sin depender solo del ID interno.
- Agrupa conceptualmente los access codes emitidos para esa reserva.

No hace:

- No valida acceso.
- No abre lockers.
- No gestiona pagos.
- No reemplaza a `Reservation`.

Campos:

- `id`: identificador interno.
- `ticketCode`: código público único, no editable.
- `reservation`: relación `OneToOne` con `Reservation`.
- `status`: estado del ticket.
- `issuedAt`: fecha de emisión.
- `expiresAt`: fecha de expiración, alineada con `reservation.reservedUntil`.
- `cancelledAt`: fecha de cancelación si aplica.
- `expiredAt`: fecha de expiración si se marca expirado.
- `createdAt`: auditoría simple.
- `updatedAt`: auditoría simple.

Relación JPA:

```java
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "reservation_id", nullable = false, unique = true)
private Reservation reservation;
```

Esto garantiza que una reserva solo tenga un ticket.

## 5. TicketStatus

Archivo:

```text
ticket/domain/enums/TicketStatus.java
```

Estados:

```text
ISSUED
CANCELLED
EXPIRED
COMPLETED
```

Uso:

- `ISSUED`: ticket activo.
- `CANCELLED`: ticket cancelado porque se canceló la reserva.
- `EXPIRED`: ticket vencido.
- `COMPLETED`: reservado para un cierre futuro del flujo.

## 6. AccessCode

Archivo principal:

```text
src/main/java/com/jaico/lockerops/ticket/domain/model/AccessCode.java
```

Responsabilidad:

- Representa el código que introduce el cliente en el kiosk.
- Se valida contra estado, vigencia, ticket y reserva asociada.
- Es reutilizable mientras esté activo y no expirado.
- Registra uso mediante `firstUsedAt`, `lastUsedAt` y `useCount`.
- Puede revocarse al cancelar la reserva.

No hace:

- No crea reservas.
- No gestiona pagos.
- No simula apertura/cierre físico de puerta.
- No guarda hashing todavía.

Campos:

- `id`: identificador interno.
- `code`: código público introducido por el usuario.
- `ticket`: relación `ManyToOne` con `Ticket`.
- `status`: estado operativo.
- `validFrom`: inicio de validez.
- `expiresAt`: fin de validez.
- `firstUsedAt`: primera validación correcta.
- `lastUsedAt`: última validación correcta.
- `useCount`: número de validaciones correctas.
- `revokedAt`: fecha de revocación.
- `createdAt`: auditoría simple.
- `updatedAt`: auditoría simple.

Relación JPA:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "ticket_id", nullable = false)
private Ticket ticket;
```

`AccessCode` no referencia directamente a `Reservation`. Se llega por:

```text
AccessCode -> Ticket -> Reservation
```

## 7. AccessCodeStatus

Archivo:

```text
ticket/domain/enums/AccessCodeStatus.java
```

Estados:

```text
ACTIVE
EXPIRED
REVOKED
```

No existe estado `USED`, porque el código es reutilizable durante la vigencia de la reserva.

## 8. Capa API

Controladores:

```text
ticket/api/controller/TicketController.java
ticket/api/controller/AccessCodeController.java
```

Endpoints añadidos:

```text
GET  /api/tickets/{id}
GET  /api/tickets/code/{ticketCode}
POST /api/access-codes/validate
```

También cambia:

```text
POST /api/reservations
```

Ahora crea automáticamente `Reservation`, `Ticket` y `AccessCode`, y devuelve un response orientado al kiosk con `ticketCode` y `accessCode`.

## 9. DTOs

Requests:

```text
ValidateAccessCodeRequest
```

Campo:

- `code`

Responses:

```text
TicketResponse
AccessCodeResponse
AccessValidationResponse
ReservationTicketResponse
```

`ReservationTicketResponse` es la respuesta de creación de reserva. Incluye:

- Datos principales de la reserva.
- Datos principales del compartimento.
- `ticketCode`.
- `accessCode`.

## 10. Capa Application

Servicios:

```text
TicketService
AccessCodeService
```

Mapper:

```text
TicketMapper
```

### TicketService

Responsabilidades:

- Emitir ticket automáticamente para una reserva confirmada.
- Generar `ticketCode` único.
- Crear el `AccessCode` inicial mediante `AccessCodeService`.
- Consultar ticket por ID.
- Consultar ticket por `ticketCode`.
- Cancelar ticket y revocar códigos activos al cancelar una reserva.

### AccessCodeService

Responsabilidades:

- Emitir access code único.
- Validar access code desde el kiosk.
- Registrar usos correctos.
- Marcar códigos expirados cuando se detecta expiración.
- Revocar códigos activos.

Reglas de validación:

- El código debe existir.
- Debe estar `ACTIVE`.
- No debe estar revocado.
- No debe estar expirado.
- `now` debe estar entre `validFrom` y `expiresAt`.
- El ticket debe estar `ISSUED`.
- La reserva asociada debe estar `CONFIRMED`.

Si es válido:

- Incrementa `useCount`.
- Rellena `firstUsedAt` si está vacío.
- Actualiza `lastUsedAt`.
- Devuelve `granted = true`.

## 11. Capa Infrastructure

Repositorios:

```text
TicketRepository
AccessCodeRepository
```

Consultas principales:

- Buscar ticket por `ticketCode`.
- Buscar ticket por `reservation.id`.
- Comprobar ticket existente para reserva.
- Buscar access code por `code`.
- Buscar códigos activos por ticket.

## 12. Integración Con Reservation

Archivo modificado:

```text
reservation/application/service/ReservationService.java
```

Cambios:

- `createReservation` ahora devuelve `ReservationTicketResponse`.
- Después de guardar la reserva, llama a `ticketService.issueTicketForReservation(savedReservation)`.
- `cancelReservation` llama a `ticketService.cancelTicketForReservation(reservation, now)`.

Archivo modificado:

```text
reservation/api/controller/ReservationController.java
```

Cambios:

- `POST /api/reservations` devuelve `ReservationTicketResponse`.

## 13. Errores Centralizados

Se añadieron códigos en:

```text
shared/exception/ApiErrorCode.java
```

Nuevos códigos:

```text
5001 TICKET_NOT_FOUND
5002 TICKET_CODE_NOT_FOUND
5003 TICKET_ALREADY_EXISTS_FOR_RESERVATION
5004 TICKET_NOT_ACTIVE
5101 ACCESS_CODE_NOT_FOUND
5102 ACCESS_CODE_EXPIRED
5103 ACCESS_CODE_REVOKED
5104 ACCESS_CODE_NOT_ACTIVE
5105 ACCESS_CODE_RESERVATION_NOT_ACTIVE
```

Mensajes añadidos en:

```text
src/main/resources/application.yml
```

No se han creado excepciones concretas nuevas. Se mantiene el patrón:

```java
throw new ApiException(ApiErrorCode.X, args);
```

## 14. Transacciones

`ReservationService.createReservation`:

- `@Transactional`
- Crea reserva, ticket y access code en una única operación.

`ReservationService.cancelReservation`:

- `@Transactional`
- Cancela reserva, libera compartimento, cancela ticket y revoca códigos.

`AccessCodeService.validateAccessCode`:

- `@Transactional(noRollbackFor = ApiException.class)`
- Permite persistir el marcado de expiración aunque se devuelva error controlado.

Consultas de ticket:

- `@Transactional(readOnly = true)`

## 15. Tests

Se añadió:

```text
src/test/java/com/jaico/lockerops/TicketAccessCodesIntegrationTests.java
```

Cubre:

- Crear reserva y generar automáticamente ticket/access code.
- Validar el mismo access code dos veces porque es reutilizable.
- Confirmar que `useCount` aumenta.
- Cancelar reserva.
- Confirmar que validar un código revocado devuelve `409` con código `5103`.

Build validada:

```powershell
.\gradlew.bat clean build
```

Resultado:

```text
BUILD SUCCESSFUL
```

## 16. Qué Queda Fuera

No se implementó:

- Pagos.
- Penalizaciones.
- Overdue handling.
- Apertura/cierre real de puerta.
- Scheduler de expiración.
- Seguridad/JWT.
- Roles.
- Hashing de códigos.
- Reimpresión real.
- Admin dashboard.
- Flyway/Docker.
- Puertos hexagonales puros.

