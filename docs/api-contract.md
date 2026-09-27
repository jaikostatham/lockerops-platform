# API Contract

Este documento resume el contrato HTTP actual. La fuente de verdad sigue siendo el codigo real, los tests y `request.http`.

## Reglas

- No cambiar endpoints, requests o responses sin revisar controller, DTOs, tests, OpenAPI annotations y `request.http`.
- Mantener `ApiErrorResponse` como formato comun de error.
- Mantener `ApiErrorCode` y `service.code` sincronizados.
- No exponer access codes persistidos ni hashes internos.
- Si una documentacion antigua contradice este contrato, verificar contra codigo y tests.

## Locker Stations

Base path: `/api/locker-stations`

- `GET /api/locker-stations` - lista estaciones.
- `GET /api/locker-stations/{id}` - consulta una estacion.
- `POST /api/locker-stations` - crea una estacion.
- `PUT /api/locker-stations/{id}` - actualiza una estacion.
- `DELETE /api/locker-stations/{id}` - elimina una estacion.

DTOs principales:

- `CreateLockerStationRequest`
- `UpdateLockerStationRequest`
- `LockerStationResponse`

Errores relevantes:

- `1000` validation error.
- `1001` invalid request body.
- `2001` locker station not found.

## Locker Compartments

Base path compartida: `/api`

- `POST /api/locker-stations/{lockerStationId}/compartments` - crea compartimento en estacion.
- `GET /api/locker-stations/{lockerStationId}/compartments` - lista compartimentos de una estacion.
- `GET /api/locker-compartments/{id}` - consulta compartimento.
- `PUT /api/locker-compartments/{id}` - actualiza compartimento.
- `DELETE /api/locker-compartments/{id}` - elimina compartimento.

DTOs principales:

- `CreateLockerCompartmentRequest`
- `UpdateLockerCompartmentRequest`
- `LockerCompartmentResponse`

Errores relevantes:

- `2001` locker station not found.
- `3001` locker compartment not found.
- `3002` duplicated compartment number in station.

## Reservations

Base path: `/api/reservations`

- `POST /api/reservations` - crea reserva y emite ticket + access code.
- `GET /api/reservations` - lista reservas.
- `GET /api/reservations/{id}` - consulta reserva.
- `PATCH /api/reservations/{id}/cancel` - cancela reserva.

DTOs principales:

- `CreateReservationRequest`
- `ReservationResponse`
- `ReservationTicketResponse`

Contrato critico de creacion:

```json
{
  "lockerCompartmentId": 1,
  "durationMinutes": 120,
  "customerReference": "KIOSK-SESSION-001"
}
```

La respuesta de creacion incluye datos de reserva, `ticketCode` y `accessCode`. El `accessCode` real solo debe exponerse al emitirlo.

Errores relevantes:

- `3001` locker compartment not found.
- `4001` reservation not found.
- `4002` compartment not available.
- `4003` compartment has active reservation.
- `4004` reservation cannot be cancelled.
- `5003` ticket already exists for reservation.

## Tickets

Base path: `/api/tickets`

- `GET /api/tickets/{id}` - consulta ticket por id interno.
- `GET /api/tickets/code/{ticketCode}` - consulta ticket por codigo publico.

DTOs principales:

- `TicketResponse`

Errores relevantes:

- `5001` ticket not found.
- `5002` ticket code not found.

## Access Codes

Base path: `/api/access-codes`

- `POST /api/access-codes/validate` - valida credenciales de acceso.

Request actual:

```json
{
  "ticketCode": "TCK-8F3K2Q9Z",
  "accessCode": "7K29QX4B"
}
```

Response correcto:

```json
{
  "granted": true,
  "ticketCode": "TCK-8F3K2Q9Z",
  "reservationId": 1,
  "lockerCompartmentId": 2,
  "compartmentNumber": 5,
  "reservedUntil": "2026-06-21T17:00:00Z",
  "validatedAt": "2026-06-21T16:15:00Z"
}
```

Reglas criticas:

- Credenciales invalidas devuelven error generico `5106`.
- Codigos revocados, expirados o no activos devuelven `409` con codigo especifico.
- La validacion correcta incrementa `useCount`.
- El access code se busca por hash, no por texto plano.

Errores relevantes:

- `5101` access code not found.
- `5102` access code expired.
- `5103` access code revoked.
- `5104` access code not active.
- `5105` reservation associated to code not active.
- `5106` ticket or access code invalid.

## Error Response

Los errores API usan `ApiErrorResponse`:

```json
{
  "status": 409,
  "error": "Conflict",
  "code": 5103,
  "message": "El codigo de acceso ha sido revocado",
  "path": "/api/access-codes/validate",
  "fieldErrors": {}
}
```

Campos exactos y constructor deben verificarse en `ApiErrorResponse` antes de cambiar el contrato.
