# Registro de incidencias S13 y S14

Se registran seis incidencias observadas durante la estabilización. Para incidencias históricas sin fecha individual registrada se indica esa limitación; no se asigna una fecha ficticia. Los cambios de la actividad autónoma están sin commit por decisión del estudiante. Un commit pendiente no significa que se haya creado un identificador nuevo.

## INC01 Tamaño de página excesivo
- Fecha: S14 práctica; fecha individual no registrada, corrección presente en el historial del 13 de noviembre de 2026.
- Módulo: API de productos y auditoría. Severidad: media. Estado: corregida.
- Reproducción: solicitar `/api/v1/productos?tamanio=500` con sesión válida.
- Esperado: rechazar tamaños mayores al máximo de 100. Observado original: se admitía una página excesiva.
- Causa: validación incompleta del límite superior.
- Corrección: PaginacionUtil valida página y tamaño antes de construir la consulta.
- Commit backend: aa1bd28. Regresión: AuditoriaIntegracionTests y caso Postman Tamaño excesivo verifican 409.

## INC02 Selector de filas inconsistente
- Fecha: S14 práctica; observada por el estudiante antes del commit del 13 de noviembre de 2026.
- Módulo: paginador SPA. Severidad: media. Estado: corregida.
- Reproducción: abrir Productos; la tabla muestra 20 filas y el selector indica 5.
- Esperado: rango, cantidad real y selector deben coincidir. Observado: 1–20 con Filas 5.
- Causa: desajuste entre valor enlazado, opciones numéricas y tamaño inicial.
- Corrección: opciones numéricas y sincronización del tamaño real en el componente compartido.
- Commit frontend: 4de8b2b. Regresión: pruebas unitarias de Paginador y paginacion.spec.ts.

## INC03 Paginación perdida al recargar
- Fecha: S14 práctica; corrección registrada el 13 de noviembre de 2026.
- Módulo: listados SPA. Severidad: media. Estado: corregida.
- Reproducción: navegar a una página posterior, pulsar F5 o volver con Atrás.
- Esperado: restaurar página y filtros. Observado original: la vista reiniciaba su estado.
- Causa: estado de listado local sin sincronización completa con query params.
- Corrección: URL como fuente de los parámetros y reinicio a página cero al filtrar.
- Commit frontend: f086445. Regresión: paginacion.spec.ts y E2E-11 de estabilizacion.spec.ts.

## INC04 Tipos de Node ausentes en E2E
- Fecha: S14 práctica; corrección registrada el 13 de noviembre de 2026.
- Módulo: configuración TypeScript de Playwright. Severidad: baja. Estado: corregida.
- Reproducción: abrir apoyo.ts y flujo-venta.spec.ts en el editor; process aparece como nombre desconocido.
- Esperado: resolver process.env en pruebas Node. Observado: Cannot find name process.
- Causa: las pruebas no tenían un proyecto TypeScript con tipos Node.
- Corrección: @types/node y e2e/tsconfig.json con types node.
- Commit frontend: 04f34b4. Regresión: `npx tsc -p e2e/tsconfig.json --noEmit`; no requiere otro test de comportamiento porque es una comprobación del compilador.

## INC05 Falta de consulta visual de auditoría
- Fecha: 15 de noviembre de 2026. Módulo: auditoría SPA. Severidad: media. Estado: corregida y verificada; commit pendiente del estudiante.
- Reproducción: administrador intenta abrir /auditoria en la versión previa.
- Esperado: tabla filtrable protegida. Observado: no existía ruta ni opción de menú aunque el API ya estaba disponible.
- Causa: implementación previa limitada al backend.
- Corrección: ruta lazy, guard por permiso, servicio, tabla, fechas y filtros URL, paginador compartido.
- Commit: pendiente; base frontend 91e5038 más cambios de trabajo.
- Regresión: E2E-12, E2E-13 y flujo-venta.spec.ts; validan consulta, F5, denegación y correlación de venta.

## INC06 Alta de cliente y eliminación de categoría sin eventos
- Fecha: 15 de noviembre de 2026. Módulo: auditoría backend. Severidad: media. Estado: corregida y verificada; commit pendiente del estudiante.
- Reproducción: registrar cliente o eliminar categoría vacía en la versión previa y consultar la bitácora.
- Esperado: tener los dos hechos confirmados exigidos para ampliar el catálogo. Observado: los servicios no publicaban esos eventos.
- Causa: cobertura del catálogo de eventos incompleta.
- Corrección: CLIENTE_REGISTRADO y CATEGORIA_ELIMINADA mediante el listener AFTER_COMMIT existente, sin copiar datos personales.
- Commit: pendiente; base backend 15fef45 más cambios de trabajo.
- Regresión: AuditoriaIntegracionTests comprueba confirmación y rollback en ambos hechos; E2E-10 valida integración del nuevo cliente.

## Resumen
| Severidad | Total | Abiertas |
|---|---:|---:|
| Crítica | 0 | 0 |
| Alta | 0 | 0 |
| Media | 5 | 0 |
| Baja | 1 | 0 |

Por módulo: API/paginación 1; listados/paginador SPA 2; configuración E2E 1; auditoría SPA 1; auditoría backend 1. Quedan pendientes los commits de INC05 e INC06 y su publicación, no una corrección técnica abierta. Añadir los hashes reales después del commit sin alterar fechas históricas.
