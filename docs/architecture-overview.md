# Arquitectura

LockerOps Platform es una API Spring Boot organizada por funcionalidades
verticales, con capas de API, aplicación, dominio y persistencia dentro de cada
funcionalidad.

## Módulos

- `station`: estaciones de lockers.
- `compartment`: compartimentos y su relación con las estaciones.
- `reservation`: creación, consulta, cancelación y expiración de reservas.
- `payment`: registro y procesamiento de intentos de pago simulados.
- `ticket`: emisión y consulta de tickets, emisión y validación de códigos de
  acceso.
- `shared.exception`: formato y tratamiento común de errores.
- `config`: configuración transversal, incluida la descripción OpenAPI.

La estructura de una funcionalidad sigue este esquema:

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

## Flujo de una petición

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

Los controladores delegan las operaciones en servicios. Los servicios
coordinan la lógica de negocio y el acceso a los repositorios; los mappers
convierten entre modelos de dominio y DTOs.

## Persistencia

Las entidades JPA están en `domain/model`, los repositorios Spring Data en
`infrastructure/persistence/repository` y los estados de dominio se persisten
como nombres de enum. Las relaciones declaradas entre entidades usan carga
perezosa. PostgreSQL es la base de datos de ejecución. Las migraciones SQL
versionadas viven en `src/main/resources/db/migration` y se aplican de forma
explícita con la tarea Gradle `flywayMigrate`; la API no las ejecuta al
arrancar. Hibernate valida el esquema existente y no lo modifica.

## Flujos de negocio

### Crear una reserva

1. Se comprueba que el compartimento existe y está disponible.
2. Se comprueba que no haya una reserva activa para ese compartimento.
3. El servidor calcula el precio según el tamaño y la duración solicitada.
4. La reserva se crea en estado `PENDING_PAYMENT` y el compartimento pasa a
   `RESERVED`.
5. La respuesta incluye el importe, la moneda y el límite para completar el
   pago.

### Simular un pago

1. Se bloquea la reserva durante la operación y se comprueba que siga pendiente
   y dentro de su ventana de pago.
2. Cada intento aprobado o rechazado queda registrado con una referencia única.
3. Un rechazo mantiene la reserva pendiente para permitir otro intento.
4. Una aprobación confirma la reserva, inicia su duración efectiva y emite el
   ticket y el código de acceso.
5. Una reserva pendiente que agota su ventana de pago expira y libera el
   compartimento.

### Cancelar una reserva

1. Se localiza la reserva y se comprueba que esté pendiente de pago o confirmada.
2. La reserva pasa a `CANCELLED` y el compartimento a `AVAILABLE`.
3. Si ya existían, el ticket asociado se cancela y los códigos activos se
   revocan.

### Expirar una reserva

1. El proceso de expiración localiza reservas `PENDING_PAYMENT` cuya ventana de
   pago terminó y reservas `CONFIRMED` cuyo plazo de uso terminó.
2. La reserva pasa a `EXPIRED`; el compartimento reservado vuelve a
   `AVAILABLE`.
3. Si la reserva estaba confirmada, el ticket y los códigos de acceso asociados
   pasan también a estado expirado.
4. En los perfiles desplegados, el proceso de expiración está desactivado.

### Validar un código de acceso

1. Se localiza el ticket y el código mediante el hash del valor recibido.
2. Se comprueban el estado y la vigencia del código, el ticket y la reserva.
3. Una validación correcta actualiza `firstUsedAt`, `lastUsedAt` y
   `useCount`.
4. La respuesta indica si se concede el acceso simulado y devuelve los datos
   de la reserva y el compartimento.

## Errores

Los errores HTTP comparten el DTO `ApiErrorResponse`. Los códigos numéricos se
centralizan en `ApiErrorCode` y sus mensajes se configuran en `service.code`
de `application.yml`.
