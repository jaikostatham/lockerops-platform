# Contrato de la API

Este documento describe las rutas y los payloads de la API REST. Los valores
de los ejemplos son sintéticos. Los perfiles desplegados exponen el catálogo
público de estaciones y compartimentos; los demás flujos están disponibles en
el entorno local de desarrollo.

## Modelo de error

Las respuestas de error usan `ApiErrorResponse`. El campo `fieldErrors`
contiene una lista de mensajes por campo cuando hay errores de validación.

```json
{
  "timestamp": "2026-06-21T16:15:00",
  "status": 409,
  "error": "Conflict",
  "code": 5103,
  "message": "El código de acceso ha sido revocado",
  "path": "/api/access-codes/validate",
  "fieldErrors": {}
}
```

Los códigos numéricos se definen en `ApiErrorCode`; los mensajes asociados
están en la configuración `service.code`.

## Estaciones de lockers

Base path: `/api/locker-stations`

- `GET /api/locker-stations` — lista estaciones.
- `GET /api/locker-stations/{id}` — consulta una estación.
- `POST /api/locker-stations` — crea una estación.
- `PUT /api/locker-stations/{id}` — actualiza una estación.
- `DELETE /api/locker-stations/{id}` — elimina una estación.

DTOs: `CreateLockerStationRequest`, `UpdateLockerStationRequest` y
`LockerStationResponse`.

Errores relevantes: `1000` validación; `1001` cuerpo de petición no válido;
`2001` estación no encontrada.

## Compartimentos

Base path: `/api`

- `POST /api/locker-stations/{lockerStationId}/compartments` — crea un
  compartimento en una estación.
- `GET /api/locker-stations/{lockerStationId}/compartments` — lista los
  compartimentos de una estación.
- `GET /api/locker-compartments/{id}` — consulta un compartimento.
- `PUT /api/locker-compartments/{id}` — actualiza un compartimento.
- `DELETE /api/locker-compartments/{id}` — elimina un compartimento.

DTOs: `CreateLockerCompartmentRequest`, `UpdateLockerCompartmentRequest` y
`LockerCompartmentResponse`.

Errores relevantes: `2001` estación no encontrada; `3001` compartimento no
encontrado; `3002` número de compartimento duplicado en la misma estación.

## Reservas

Base path: `/api/reservations`

- `POST /api/reservations` — crea una reserva pendiente de pago y bloquea
  temporalmente el compartimento.
- `GET /api/reservations` — lista reservas.
- `GET /api/reservations/{id}` — consulta una reserva.
- `PATCH /api/reservations/{id}/cancel` — cancela una reserva pendiente o
  confirmada.

DTOs: `CreateReservationRequest`, `ReservationResponse` y
`ReservationTicketResponse`.

Ejemplo de petición:

```json
{
  "lockerCompartmentId": 1,
  "durationMinutes": 120,
  "customerReference": "KIOSK-SESSION-001"
}
```

La respuesta de creación contiene el estado `PENDING_PAYMENT`, el precio en
`amountMinor`, la moneda `currency` y `paymentExpiresAt`. El importe se expresa
en la unidad menor de la moneda; por ejemplo, `400` representa 4,00 EUR. El
servidor calcula el precio y no acepta importes enviados por el cliente.

Errores relevantes: `3001` compartimento no encontrado; `4001` reserva no
encontrada; `4002` compartimento no disponible; `4003` compartimento con
reserva activa; `4004` reserva no cancelable; `5003` ticket ya emitido para
la reserva.

## Pagos simulados

Base path: `/api/payments`

- `POST /api/payments/simulate` — registra un intento simulado aprobado o
  rechazado para una reserva pendiente.

Ejemplo de petición:

```json
{
  "reservationId": 1,
  "outcome": "APPROVED"
}
```

Los resultados admitidos son `APPROVED` y `DECLINED`. Un rechazo conserva la
reserva pendiente mientras siga abierta su ventana de pago. Una aprobación
confirma la reserva y devuelve `ticket`, que contiene `ticketCode` y
`accessCode`. Ese es el único momento en que se devuelve el código de acceso
completo; la persistencia guarda su hash y una vista parcial.

El simulador no recibe ni almacena números de tarjeta, fechas de caducidad ni
códigos de seguridad. Cada intento conserva su referencia, resultado, importe,
moneda y fecha de procesamiento.

Errores relevantes: `4001` reserva no encontrada; `6001` reserva no pendiente
de pago; `6002` ventana de pago expirada.

## Tickets

Base path: `/api/tickets`

- `GET /api/tickets/{id}` — consulta un ticket por identificador interno.
- `GET /api/tickets/code/{ticketCode}` — consulta un ticket por código público.

DTO: `TicketResponse`.

Errores relevantes: `5001` ticket no encontrado por identificador; `5002`
código de ticket no encontrado.

## Códigos de acceso

Base path: `/api/access-codes`

- `POST /api/access-codes/validate` — valida un código para la simulación de
  acceso del kiosco.

Petición:

```json
{
  "ticketCode": "TCK-8F3K2Q9Z",
  "accessCode": "7K29QX4B"
}
```

Respuesta cuando se concede el acceso:

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

La validación busca el código mediante su hash y registra la fecha y el número
de usos correctos. Las credenciales no válidas producen el error `5106`;
los códigos expirados, revocados o no activos devuelven `409` con un código
específico.

Errores relevantes: `5101` código no encontrado; `5102` código expirado;
`5103` código revocado; `5104` código no activo; `5105` reserva asociada
no activa; `5106` ticket o código de acceso no válido.
