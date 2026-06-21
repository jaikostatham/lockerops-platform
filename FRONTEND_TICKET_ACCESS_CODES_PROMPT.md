# Prompt Para Codex - Frontend LockerOps Kiosk

Actúa como arquitecto frontend senior especializado en Vue 3, Quasar y consumo seguro de APIs REST.

Quiero adaptar el frontend `lockerops-kiosk-frontend` para consumir la nueva feature backend `Ticket & Access Codes` de LockerOps Platform, sin romper el flujo actual.

## Contexto Backend

Backend: Java 21, Spring Boot 3, API REST.

La rama backend implementa una nueva fase:

```text
Ticket & Access Codes
```

Ahora, al crear una reserva:

```text
POST /api/reservations
```

el backend crea automáticamente:

- `Reservation`
- `Ticket`
- `AccessCode`

El response de creación de reserva ya no es solo `ReservationResponse`, sino un response orientado al kiosk:

```json
{
  "reservationId": 1,
  "reservationReference": "uuid",
  "reservationStatus": "CONFIRMED",
  "lockerCompartmentId": 10,
  "lockerStationId": 2,
  "compartmentNumber": 5,
  "reservedFrom": "2026-06-21T16:00:00Z",
  "reservedUntil": "2026-06-21T17:00:00Z",
  "customerReference": "optional",
  "ticketCode": "TCK-12345678",
  "accessCode": "123456"
}
```

## Endpoints Nuevos

Consultar ticket por ID:

```text
GET /api/tickets/{id}
```

Consultar ticket por código:

```text
GET /api/tickets/code/{ticketCode}
```

Validar access code:

```text
POST /api/access-codes/validate
```

Body:

```json
{
  "code": "123456"
}
```

Response correcto:

```json
{
  "granted": true,
  "ticketCode": "TCK-12345678",
  "reservationId": 1,
  "lockerCompartmentId": 10,
  "compartmentNumber": 5,
  "reservedUntil": "2026-06-21T17:00:00Z",
  "validatedAt": "2026-06-21T16:15:00Z"
}
```

Errores posibles:

- `404` si el código no existe.
- `409` si el código expiró, fue revocado, no está activo o la reserva ya no está activa.

El backend devuelve errores con esta forma aproximada:

```json
{
  "timestamp": "...",
  "status": 409,
  "error": "Conflict",
  "code": 5103,
  "message": "El código de acceso ha sido revocado",
  "path": "/api/access-codes/validate",
  "fieldErrors": {}
}
```

## Objetivo Frontend

Actualizar el Kiosk Simulator para que:

1. Mantenga el flujo actual de seleccionar estación, ver lockers y crear reserva.
2. Tras crear una reserva, muestre claramente:
   - `ticketCode`
   - `accessCode`
   - número de compartimento
   - `reservedUntil`
3. Añada una pantalla o modo para introducir un access code.
4. Llame a:

```text
POST /api/access-codes/validate
```

5. Si el backend devuelve `granted = true`, mostrar acceso concedido simulado.
6. Si devuelve error, mostrar el mensaje del backend sin romper la navegación.

## Requisitos Importantes

- No romper el flujo actual.
- No cambiar endpoints antiguos salvo ajustar el tipo de respuesta de `POST /api/reservations`.
- No implementar login, JWT, roles ni admin dashboard.
- No simular puerta real todavía.
- No inventar pagos.
- No hardcodear URLs si ya existe capa de API/config.
- Mantener el estilo visual actual del proyecto.
- Mantener el código organizado según los patrones existentes del frontend.
- Si hay store/composables/services existentes, reutilizarlos.
- No introducir una librería nueva salvo que sea estrictamente necesario.
- Añadir manejo de loading y error para la validación de código.
- Evitar duplicar lógica de llamadas HTTP.

## Tareas Sugeridas

1. Inspecciona primero la estructura real del frontend.
2. Localiza dónde se crea actualmente una reserva.
3. Ajusta el tipo/modelo de respuesta para incluir `ticketCode` y `accessCode`.
4. Añade una vista, paso o componente para mostrar el ticket tras crear reserva.
5. Añade un formulario de validación de access code.
6. Añade llamada API a `POST /api/access-codes/validate`.
7. Muestra estado de acceso concedido o error controlado.
8. Mantén navegación simple para volver a pantalla inicial.
9. Ejecuta build/lint/test disponible en el frontend.

## Criterios De Aceptación

- El usuario puede crear una reserva desde el kiosk.
- Después de crearla, ve `ticketCode` y `accessCode`.
- El usuario puede introducir el `accessCode`.
- Si el código es válido, ve acceso concedido.
- Si el código es inválido/revocado/expirado, ve el mensaje de error del backend.
- La app compila.
- No se rompe el flujo previo de estaciones/lockers/reserva.

## Importante

Antes de tocar código, inspecciona el proyecto frontend real y adapta esta propuesta a su estructura. No asumas nombres de stores, rutas o servicios sin comprobarlos.
