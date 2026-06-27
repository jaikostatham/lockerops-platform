# AI Workflow And Harness

## Objetivo

Este documento define como usar Codex en LockerOps sin perder contexto, sin gastar tokens de forma innecesaria y sin romper el flujo Git Flow del proyecto.

## Principio base

Codex puede analizar, preparar y proponer. Las acciones permanentes se validan con el usuario.

Acciones que requieren permiso explicito:

- Crear o renombrar ramas.
- Hacer commits.
- Hacer merge o rebase.
- Hacer push.
- Resolver conflictos que afecten logica.
- Instalar dependencias.
- Cambiar configuracion sensible.
- Tocar base de datos, entidades o relaciones.

## Antes de empezar una tarea

Codex debe comprobar:

1. Rama actual.
2. Estado local.
3. Si hay cambios no hechos por Codex.
4. Relacion con remoto cuando vaya a crear rama, commit, push o merge.
5. Docs minimos que necesita leer.

Para tareas no triviales, Codex debe proponer plan antes de editar.

## Uso de contexto

Regla de minima carga:

- Primero leer `AGENTS.md`.
- Despues leer `docs/project-context.md` si hace falta contexto general.
- Leer solo el doc especifico de la tarea.
- Para tareas de API, leer `docs/api-contract.md`.
- Para tareas de verificacion, leer `docs/testing-and-verification.md`.
- Leer solo archivos de codigo relacionados.
- No releer todo el repo por defecto.

Si la conversacion crece mucho:

1. Preparar resumen de handoff.
2. Incluir rama, objetivo, decisiones, archivos tocados, tests y pendientes.
3. Compactar la conversacion despues de dejar el resumen.

## Git Flow con Codex

Flujo normal:

1. Trabajar desde `develop`.
2. Comprobar estado local y remoto.
3. Proponer nombre de rama.
4. Esperar validacion.
5. Crear rama local.
6. Hacer cambios.
7. Ejecutar verificacion acordada.
8. Mostrar resumen y archivos modificados.
9. Esperar revision del usuario.
10. Proponer una unica accion agrupada para stage, commit y push si procede.
11. Si el usuario aprueba, ejecutar todo el bloque aprobado sin pedir confirmaciones intermedias.
12. Antes de push o merge, volver a comprobar remoto.

Codex no debe hacer `git add .`. Debe proponer archivos concretos.

La propuesta agrupada debe incluir:

- Rama actual.
- Archivos a stagear.
- Mensaje de commit.
- Destino remoto si hay push.
- Base/destino si hay merge o PR.
- Verificaciones realizadas.
- Riesgos o advertencias.

## Verificacion

Seleccionar la verificacion segun riesgo:

- Docs solamente: revisar diff y enlaces/rutas.
- Logica de backend: `.\gradlew.bat test`.
- Cambios amplios: `.\gradlew.bat build`.
- API: tests + `request.http` + OpenAPI annotations.
- Configuracion: revisar perfiles y `.env.example`.

## Resumen de feature

Al cerrar una rama, Codex debe proponer un resumen usando `docs/feature-summary-template.md`.

Si el usuario lo aprueba, guardarlo en:

```text
docs/feature-summaries/YYYY-MM-DD-nombre-rama.md
```

El resumen no debe duplicar todo el diff. Debe conservar solo:

- Objetivo.
- Cambios importantes.
- Contratos afectados.
- Decisiones tecnicas.
- Verificacion.
- Riesgos o pendientes.
- Contexto que una futura conversacion necesita.

Despues, actualizar docs principales solo si hay conocimiento estable que deba quedar siempre disponible.

## Skills futuras

No crear skills todavia. Primero consolidar este harness.

Candidatas futuras:

- Cambio de contrato API.
- Revision de flujo reserva-ticket-access code.
- Revision de errores centralizados.
- Creacion de tests de integracion.
- Sincronizacion de documentacion.

Crear una skill solo si el workflow se repite, tiene pasos claros y ahorra contexto.
