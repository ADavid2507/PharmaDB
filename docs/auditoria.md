# Auditoría de operaciones

La vista `/auditoria` y el endpoint `GET /api/v1/auditoria` requieren `USUARIOS_ADMINISTRAR`. El menú y el guard de Angular ocultan y protegen la ruta; `SecurityConfig` aplica la autorización real en el servidor. La tabla presenta fecha, usuario, acción, entidad, ID, detalle y requestId. Los filtros usuario, acción, desde y hasta se conservan en URL, junto con pagina y tamanio. Las páginas se consultan en servidor y reutilizan el componente Paginador.

## Catálogo de eventos
| Acción | Momento | Detalle almacenado |
|---|---|---|
| LOGIN_EXITOSO | Autenticación correcta | Sin contraseña ni token |
| LOGIN_FALLIDO | Intento rechazado | Usuario intentado; sin contraseña ni token |
| VENTA_REGISTRADA | Venta confirmada | Identificador y resumen de la venta |
| PRECIO_MODIFICADO | Actualización confirmada del precio | Precio anterior y nuevo |
| PRODUCTO_ELIMINADO | Baja lógica confirmada | Identificación de la operación |
| CLIENTE_REGISTRADO | Alta de cliente confirmada | Texto fijo Cliente registrado; sin DNI, correo o dirección |
| CATEGORIA_ELIMINADA | Eliminación confirmada de categoría sin productos | Texto fijo sobre la eliminación |

Los últimos dos eventos se incorporaron para la actividad autónoma. Se publican desde ClienteServiceImpl y CategoriaServiceImpl dentro de sus transacciones. AuditoriaListener escucha con `@TransactionalEventListener(phase = AFTER_COMMIT)`; AuditoriaServiceImpl registra en `REQUIRES_NEW`. Un rollback no genera estos eventos. Las pruebas de integración comprueban commit y rollback, usuario y correlación.

## Tres fuentes complementarias
Las columnas `creado_por` y `modificado_por` describen al creador y al último modificador del registro. No guardan todo el historial. La tabla `auditoria_eventos` conserva hechos de negocio con fecha, actor e identificador. Los logs ayudan a diagnosticar peticiones y duraciones; RequestIdFilter asigna `X-Request-Id` y lo incorpora al MDC.

## Confirmación y fallo de acceso
Los hechos de negocio se guardan después del commit: no debe quedar una venta auditada como confirmada si se revirtió su transacción. Los fallos de login deben sobrevivir al rechazo de la autenticación; AuthServiceImpl invoca el registro independiente para ese caso. Una caída al guardar la bitácora después del commit se registra en logs: no revierte retroactivamente la venta ya confirmada. Esto implica una limitación de entrega de eventos; no se implementó un outbox persistente.

## Trazabilidad de una venta
Registrar una venta y conservar su ID y el encabezado X-Request-Id. Verificar `ventas.creado_por`, buscar `VENTA_REGISTRADA` por ese ID en la pantalla y localizar la petición POST /api/v1/ventas en el log usando el mismo requestId. El test flujo-venta.spec.ts valida venta, stock, API de auditoría, reporte y fila de auditoría en la SPA. Las evidencias de SQL, SPA y log se presentan en el informe de entrega.

## Datos que no se registran
No incluir contraseñas, JWT, encabezados Authorization, cuerpos completos de solicitudes ni datos personales innecesarios. Los nuevos eventos usan mensajes fijos. Los intentos de acceso identifican al usuario intentado, nunca su contraseña. No exportar sesiones Postman o almacenamiento del navegador con tokens.

## Rutas de implementación
- Backend: src/main/java/pe/edu/upeu/PharmaBackend/auditoria/{EventoAuditoria,AuditoriaListener}.java
- Backend: service/impl/{AuditoriaServiceImpl,ClienteServiceImpl,CategoriaServiceImpl,VentaServiceImpl,AuthServiceImpl}.java
- Backend: controller/AuditoriaController.java; config/{SecurityConfig,AuditoriaConfig,RequestIdFilter}.java
- Backend: src/test/java/pe/edu/upeu/PharmaBackend/AuditoriaIntegracionTests.java
- Frontend: src/app/features/auditoria/; src/app/app.routes.ts; src/app/core/config/menu.ts
