# Flujo de cambios y despliegues

## Mapa de entornos

| Entorno | Rama | Kiosco | API | Base PostgreSQL |
| --- | --- | --- | --- | --- |
| Local | Rama de trabajo | `http://localhost:9000` | `http://localhost:8080` | PostgreSQL local `localhost:5432/lockerops_test` por defecto |
| Testing | `develop` | [lockerops-kiosk-frontend-test](https://lockerops-kiosk-frontend-test.onrender.com/stations) | [lockerops-platform-test](https://lockerops-platform-test.onrender.com) | Neon `lockerops-test`, rama `testing`, base `lockerops_test` |
| Producción | `main` | [lockerops-kiosk-frontend-prod](https://lockerops-kiosk-frontend-prod.onrender.com/stations) | [lockerops-platform-prod](https://lockerops-platform-prod.onrender.com) | Neon `lockerops-prod`, rama `production`, base `lockerops_prod` |

La instancia PostgreSQL local y la de Neon Testing tienen el mismo nombre de base, pero son servidores distintos. Verifica la configuración efectiva de IntelliJ antes de asumir que los cambios locales aparecen en Testing.

## Gitflow

1. Crea una rama semántica (`feature/...`, `fix/...` o `docs/...`) desde `develop` actualizado y mantén el cambio dentro del repositorio que corresponda.
2. Abre un Pull Request de esa rama a `develop`. GitHub Actions ejecuta CI; revisa el diff y el resultado antes de integrar.
3. Tras integrar, valida el servicio de Testing en Render: revisa el deploy y sus logs, consulta `GET /api/locker-stations` y comprueba `/stations` en el kiosco.
4. Cuando Testing esté validado, abre un Pull Request separado de `develop` a `main`. La promoción también pasa por CI y revisión.
5. El responsable del repositorio hace el merge manual. Render recibe los cambios de la rama conectada; comprueba el resultado en Deploys en vez de presuponer que comenzó o terminó correctamente.
6. En Producción, confirma que la API queda Live, que Flyway termina sin errores y que el kiosco consume el catálogo esperado.

## CI y despliegue

- GitHub Actions valida cambios; no publica las aplicaciones.
- Los servicios de Render están conectados a las ramas de Testing y Producción. La ejecución de Producción del 27 de septiembre de 2026 se inició como `Auto-Deploy` después de integrar el PR de promoción y terminó Live.
- Los backends ejecutan Flyway al arrancar. El despliegue de Producción del commit `9f525d9` validó las tres migraciones y terminó en el esquema v3.
- Después de ese despliegue, `GET /api/locker-stations` devolvió tres estaciones y el kiosco público mostró el mismo catálogo.
- Revisa en Render el estado, la rama, el commit, el trigger y los logs de cada servicio. Las opciones de despliegue pueden variar según la configuración del servicio.

## Configuración pública

Cada frontend apunta a la API de su entorno mediante `VITE_API_BASE_URL`; Testing apunta a la API de Testing y Producción a la API de Producción. En ambos sitios públicos `VITE_RESERVATION_FLOW_ENABLED=false` oculta el flujo de reservas. La API también impone lectura de catálogo, así que la variable del frontend no es el control de seguridad.

Los servicios públicos no requieren autenticación y solo contienen datos ficticios. No publiques claves, contraseñas, reservas reales ni códigos de acceso. Guarda secretos de backend en las variables de entorno de Render; las variables `VITE_*` son públicas una vez incluidas en el bundle.

La guía de roles, variables y migraciones está en [`postgresql-environments.md`](postgresql-environments.md).
