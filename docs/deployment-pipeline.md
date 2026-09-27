# Flujo de cambios y despliegues

## Mapa de entornos

| Entorno | Rama | Kiosco | API | Base PostgreSQL |
| --- | --- | --- | --- | --- |
| Local | Rama de trabajo | `http://localhost:9000` | `http://localhost:8080` | PostgreSQL local `localhost:5432/lockerops_test` por defecto |
| Testing | `develop` | [lockerops-kiosk-frontend-test](https://lockerops-kiosk-frontend-test.onrender.com/stations) | [lockerops-platform-test](https://lockerops-platform-test.onrender.com) | Neon `lockerops-test`, rama `testing`, base `lockerops_test` |
| Producción | `main` | [lockerops-kiosk-frontend-prod](https://lockerops-kiosk-frontend-prod.onrender.com/stations) | [lockerops-platform-prod](https://lockerops-platform-prod.onrender.com) | Neon `lockerops-prod`, rama `production`, base `lockerops_prod` |

La base local `lockerops_test` y la de Neon Testing tienen el mismo nombre, pero
son servidores distintos. IntelliJ puede sustituir `DB_HOST` y `DB_NAME`; comprueba
la configuración efectiva antes de asumir que una operación local afecta a Testing.

## Flujo Git

1. Actualiza `develop` y crea una rama semántica (`feature/...`, `fix/...` o
   `docs/...`) para un único tema, dentro del repositorio afectado.
2. Abre una PR de esa rama a `develop`. Revisa el diff y los resultados de CI.
3. El responsable del repositorio hace el merge manualmente. No se integran
   cambios directamente en `develop`.
4. Después del merge, valida Testing: comprueba el despliegue y sus logs en
   Render, consulta `GET /api/locker-stations` y abre `/stations` en el kiosco.
5. Producción se actualiza solo cuando se decide publicar un release. Se agrupan
   los cambios aprobados y se abre una única PR de `develop` a `main` por
   repositorio; no se promueve cada cambio pequeño.
6. El responsable hace el merge manual de la PR de promoción. Después, comprueba
   en Render el servicio, el commit y los logs de Producción.

## CI y despliegue

- El workflow del backend ejecuta `./gradlew build --no-daemon` en pushes a
  `develop` y `main`, y en PRs dirigidas a esas ramas. El build incluye las
  comprobaciones automatizadas del proyecto.
- GitHub Actions valida el código; no publica las aplicaciones. Render aloja
  los servicios conectados a las ramas de Testing y Producción. Revisa la página
  Deploys para confirmar el resultado de cada despliegue.
- El backend incluye `GET /healthz`, que confirma que la aplicación responde.
  La ruta no verifica la conexión con PostgreSQL.
- Los perfiles remotos limitan la API al catálogo público y bloquean escrituras.
  El frontend público usa `VITE_RESERVATION_FLOW_ENABLED=false`; esa opción solo
  oculta la interfaz y no reemplaza las restricciones del backend.
- La guía de roles, variables y migraciones está en
  [`postgresql-environments.md`](postgresql-environments.md).

No publiques claves, contraseñas, reservas reales ni códigos de acceso. Guarda
los secretos del backend en el gestor de variables del servicio; las variables
`VITE_*` quedan expuestas en el bundle público del frontend.
