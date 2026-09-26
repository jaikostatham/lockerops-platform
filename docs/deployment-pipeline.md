# Flujo de cambios y despliegues

## Modelo acordado

| Entorno | Código | Direcciones previstas | Base PostgreSQL |
| --- | --- | --- | --- |
| Local | Rama de trabajo en el equipo | Frontend `localhost:9000`; API `localhost:8080` | `lockerops_test` |
| Testing | Código integrado en `develop` | URL pendiente de configurar | `lockerops_test` |
| Portfolio | Código integrado en `main` después de validar testing | URL pendiente de configurar | `lockerops_prod` |

Local y testing solo comparten los mismos datos si se conectan a la misma
instancia PostgreSQL. Ahora existe la base local; todavía no hay una base
remota de testing ni URLs públicas. La base `lockerops_prod` queda aislada
para la última etapa.

## Cómo será el recorrido

1. Trabajas en una rama local y abres un Pull Request hacia `develop`.
2. GitHub Actions comprueba el backend y el frontend. Si pasa, integras el
   Pull Request; entonces `develop` representa el código que se validará
   en testing.
3. Cuando la URL de testing esté configurada, pruebas allí la versión
   integrada usando datos sintéticos.
4. Si la validación termina bien, abres un Pull Request de `develop` a
   `main`. Después de sus comprobaciones y revisión, integrar `main` será
   la promoción a la URL de portfolio.

## Qué funciona hoy

- Cada repositorio tiene un workflow `ci.yml`: el backend ejecuta
  `./gradlew build --no-daemon` y el frontend ejecuta `npm ci` y
  `npm run build`.
- Esos workflows comprueban cambios; **no despliegan** ni crean URLs.
- No se ha elegido ni creado un servicio de alojamiento o una base remota.
- No hay secretos de despliegue configurados en GitHub y no se ha generado
  ningún coste.

El despliegue automático de `develop` y `main` queda pendiente. No añadimos
otro workflow de GitHub Actions hasta elegir el alojamiento y comprobar que
ese método realmente necesita uno.

## Seguridad y coste

La API no tiene autenticación. Las configuraciones `testing` y `prod`
limitan la API pública al catálogo de estaciones y compartimentos y
bloquean escrituras. Usa solo datos sintéticos en cualquier URL pública.
El frontend público también debe compilarse con
`VITE_RESERVATION_FLOW_ENABLED=false`; esa variable solo controla la
interfaz y no sustituye la protección del backend.

Antes de elegir un proveedor revisaremos sus límites gratuitos vigentes y
si exige tarjeta. No se seleccionará un plan de pago ni se continuará con
un alta que pueda generar cargos. Los límites gratuitos pueden suspender
un servicio; no se prometerá disponibilidad ni uso ilimitado.

Las credenciales se guardarán en el gestor de secretos del servicio, nunca
en el repositorio, en `VITE_*` ni en una imagen Docker. Antes de conectar
una base remota o aplicar una migración se revisarán la copia de seguridad
y el efecto sobre los datos.
