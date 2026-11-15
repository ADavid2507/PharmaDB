# Capturas paso a paso para la entrega S14

Las imágenes las toma el estudiante. No se generaron capturas nuevas de la aplicación para esta entrega. Las evidencias históricas permanecen en su ubicación original. La guía exige evidencia verificable, además del video y de los registros humanos: los resultados automáticos no sustituyen los ensayos.

## Preparar la pantalla y guardar las imágenes
1. Arrancar la API real con el perfil de desarrollo y Oracle/Redis. Confirmar el puerto en el log. Si usas Angular con `npm start`, abrir http://localhost:4200; si usas Nginx en 8081, reconstruir y actualizar ese despliegue antes de capturar. No mezclar pantallas de una versión anterior con esta entrega.
2. Usar únicamente datos de prueba. Cerrar notificaciones y pestañas personales. Maximizar las ventanas y ajustar el zoom para leer tabla, título y URL.
3. No mostrar contraseñas, pestaña Authorization, tokens, Session Storage ni conexiones con claves. La pestaña Network puede mostrar URL, estado, respuesta y X-Request-Id; ocultar el encabezado Authorization.
4. Pulsar **Win+Shift+S**, elegir recorte rectangular, seleccionar título/URL y resultado. Hacer clic en la notificación y guardar PNG con el nombre indicado. Guardar en `C:\Users\David\Downloads\PharmaBackend\docs\evidencias\s14-estabilizacion\` salvo que se indique otra carpeta.
5. Insertar en el Word debajo del apartado correspondiente mediante Insertar → Imágenes → Este dispositivo. Agregar un pie con la fecha real, entorno, qué prueba y resultado. No reducir tanto una imagen que ya no se lean las cifras; usar dos imágenes cuando sea necesario.

## C01 Pantalla de auditoría con filtros y URL
1. Iniciar sesión con administrador en la SPA actualizada.
2. Abrir Auditoría desde el menú. Si no aparece, verificar que el usuario tenga USUARIOS_ADMINISTRAR y que la compilación incluya los cambios nuevos.
3. Elegir una acción existente, por ejemplo LOGIN_EXITOSO, y escribir el usuario admin. Pulsar Buscar.
4. Confirmar que la URL contiene usuario, accion y pagina=0, y que las filas coinciden.
5. Pulsar F5 y comprobar que el formulario y las filas conservan los filtros.
6. Capturar URL, formulario, encabezados y al menos una fila legible. Nombre: `C01-auditoria-filtros.png`.
7. Pie sugerido: «Consulta de auditoría como administrador con filtros persistidos en URL, después de recargar. Entorno y fecha: [reales]».

## C02 Una venta en columna de auditoría bitácora y log
Es la evidencia principal del bloque A. Preparar antes un producto de prueba con stock suficiente y un cliente activo.
1. En vendedor abrir Nueva venta, seleccionar cliente y producto y registrar una venta válida. Anotar el ID de venta. No repetir el botón de confirmación para obtener otra captura.
2. En DevTools (F12) → Network/Red, filtrar `ventas`, abrir el POST que devolvió 201. En Headers/Encabezados localizar **Response Headers → X-Request-Id**. Copiar ese identificador, sin copiar el Bearer token.
3. Iniciar sesión admin, abrir Auditoría y filtrar VENTA_REGISTRADA. Buscar la fila con entidadId igual al ID de esa venta. Capturar fecha, usuario, acción, ID y requestId. Guardar `C02a-venta-bitacora.png`.
4. En DBeaver, conexión del esquema real, ejecutar **solo esta consulta**, reemplazando 123 por el ID recién creado:

```sql
SELECT id, fecha_registro, total, creado_por
FROM ventas
WHERE id = 123;
```

5. Capturar la consulta y la fila de resultado con creado_por legible. Guardar `C02b-venta-creado-por.png`. Si la columna no existe, verificar que la migración V4 se haya aplicado antes de continuar; no ocultar ese fallo.
6. En la consola backend del perfil dev buscar el requestId copiado; el patrón del perfil dev lo muestra entre corchetes. Si se usan logs a archivo del perfil prod, buscar allí. No esperar verlo en el perfil test, que usa otro patrón.
7. Mostrar la línea POST /api/v1/ventas → 201 y las líneas de servicio con ese requestId. Guardar `C02c-venta-log.png`.
8. Las tres imágenes deben corresponder a **la misma venta**. En el Word escribir ID y requestId reales y explicar: la columna señala al creador, la bitácora guarda el hecho, el log correlaciona la petición.

## C03 Nuevos eventos de cliente y categoría
1. Con un usuario autorizado registrar un cliente ficticio nuevo con DNI de prueba único. No usar una persona real.
2. Como admin abrir Auditoría, filtrar CLIENTE_REGISTRADO. Capturar el evento y comprobar que Detalle solo diga Cliente registrado, sin DNI, correo o dirección. Guardar `C03a-cliente-registrado.png`.
3. Crear una categoría temporal de prueba con nombre reconocible y **sin productos**. Anotar su ID y eliminarla desde Categorías. Nunca elegir una categoría de uso real.
4. Abrir Auditoría y filtrar CATEGORIA_ELIMINADA; capturar el ID y detalle. Guardar `C03b-categoria-eliminada.png`.
5. La prueba de rollback está en el resultado de AuditoriaIntegracionTests. No provocar errores ni manipular transacciones de producción para obtener una imagen.

## C04 Resultado Playwright de 42 ejecuciones
1. En la carpeta backend abrir `docs\evidencias\s14-estabilizacion\automatizadas\playwright-report\index.html` en el navegador. Si el navegador requiere servidor, ejecutar desde frontend:

```powershell
    npx playwright show-report "C:\Users\David\Downloads\PharmaBackend\docs\evidencias\s14-estabilizacion\automatizadas\playwright-report"
```

2. Mostrar el total aprobado, duración y lista de pruebas. La revisión guardada corresponde a H2 aislado, 14 escenarios × 3 repeticiones, retries=0.
3. Capturar el resumen: `C04a-playwright-42.png`. Abrir un caso de auditoría o stock y capturar los pasos si se desea: `C04b-playwright-detalle.png`.
4. Para una **nueva ejecución**, arrancar primero backend con `tools/iniciar-e2e.ps1`; después en frontend `$env:E2E_AISLADO='1'` y `npm run e2e:estable`. No ejecutar Postman al mismo tiempo. La prueba de venta modifica solo H2.
5. No confundir este resultado con las 21 ejecuciones Oracle de la práctica anterior. Al capturar una ejecución nueva registrar fecha y duración nuevas en resultados-pruebas.md.

## C05 Colección Postman
1. Arrancar el laboratorio H2 en 18080 como indica docs/postman/README.md.
2. Importar `docs\postman\PharmaBackend.postman_collection.json` en Postman. Configurar las claves **solo como valores locales** del entorno, según E2EDatos.java. No exportar el entorno con claves o tokens.
3. Ejecutar Run collection, las 16 solicitudes en orden y una iteración. Esperar que termine: incluye una venta de una unidad en H2.
4. Capturar el resumen de aserciones aprobadas sin abrir los cuerpos de login: `C05a-postman-resumen.png`.
5. Mostrar los resultados 400, 401, 403, 404 y 409, con nombre del caso y estado. Si no caben, usar dos capturas: `C05b-postman-estados.png` y `C05c-postman-estados.png`.
6. Verificar el caso final de correlación de venta y auditoría. No mostrar variables tokenAdmin/tokenVendedor.

## C06 Seguridad en SPA y API
1. Iniciar sesión vendedor y capturar menú sin Auditoría. Guardar en `docs\evidencias\u2-spa-segura\C06a-menu-vendedor.png`.
2. Escribir /auditoria en la barra de direcciones; capturar URL y Acceso denegado. Guardar `C06b-ruta-denegada.png` en esa carpeta.
3. En Postman mostrar Auditoría denegada al vendedor con 403 y sin token con 401; capturar únicamente nombre, URL, estado y cuerpo de error, ocultando credenciales.
4. Para el retorno al login puede usarse el caso E2E-14 del informe. Si se demuestra token alterado manualmente, hacerlo únicamente en sesión de prueba y no capturar su valor. Explicar que ese caso prueba el manejo de 401, no el transcurso natural de la duración del JWT.
5. No es necesario inventar errores de consola: si no hay excepciones, no hay una captura de error que buscar. La fixture de Playwright comprueba ausencia de excepciones y 5xx.

## C07 Paginación y volumen real
1. Abrir Productos del entorno **Oracle**. No volver a ejecutar el script de volumen: ya tenías 15015 productos.
2. Elegir Filas 20; comprobar rango 1–20, cantidad de filas y selector 20. Cambiar a otra página y pulsar F5.
3. Capturar URL con pagina/tamanio, rango y selector: `C07a-paginacion-oracle.png`, en `docs\evidencias\s13-optimizacion\`.
4. En Network abrir el GET /productos de esa página, Response/Respuesta. Mostrar contenido limitado y totalElementos mayor a 5000; ocultar Authorization. Guardar `C07b-respuesta-paginada.png`.
5. Si se requiere SQL, ejecutar solo `SELECT COUNT(*) AS total_productos FROM productos;` y capturar resultado. No INSERT ni DELETE para esta imagen.

## C08 Redis y Actuator
1. Revisar las capturas existentes en docs/s13/capturas-directas. Puedes reutilizarlas identificándolas como evidencia histórica de S13, sin atribuirles la fecha actual.
2. Para evidencia nueva, seguir el procedimiento Redis del README y del informe S13 con los servicios de desarrollo preparados. Mostrar primera lectura, segunda lectura, invalidación por actualización o venta y estado de salud. No ejecutar FLUSHALL ni borrar datos de la base.
3. Abrir /actuator/health en el puerto backend real. El estado público no muestra necesariamente detalles; los detalles del administrador requieren la autenticación configurada.
4. Guardar imágenes nuevas en docs/evidencias/s13-optimizacion con pie que distinga fecha, entorno y medición. Si no se repite la medición, usar la evidencia histórica y declararlo.

## C09 Registros de ensayos y recorridos
1. Abrir docs/checklist-demo.md. Completar las tablas con fecha/hora, duración real, base preparada, venta/requestId y cualquier desviación.
2. Hacer tres recorridos manuales desde una base de pruebas recién preparada cada vez y dos ensayos del guion completo. Preparar mediante copia o respaldo autorizado; no borrar la base principal.
3. Después de completarlos, copiar las tablas al Word o adjuntar una captura legible `C09-registros-manuales.png`. Las casillas pendientes no equivalen a actividades realizadas.

## C10 Historial Git y etiqueta final
1. Después de que tú hagas los commits reales, ejecutar en cada repositorio:

```powershell
git log -10 --date=short --pretty=format:"%h %ad %s"
git show --no-patch --oneline v1.0-demo
git remote -v
```

2. Capturar el historial que demuestre al menos tres días distintos por repositorio y la etiqueta final. Guardar `C10a-git-backend.png` y `C10b-git-frontend.png`.
3. No modificar fechas ni crear evidencia falsa si todavía no se cumple el requisito. Los enlaces remotos se verifican después del push.

## Finalizar el Word y PDF
1. Abrir docs/entrega/Asmat_LP2_S14_Autonoma.docx. Insertar las capturas debajo de sus apartados y reemplazar cada campo PENDIENTE que ya tenga evidencia real.
2. Añadir enlace de video con acceso público por enlace, los hashes de los commits y el tag.
3. Revisar que las imágenes se lean al 100 %, que ninguna tabla quede cortada y que los enlaces abran sin depender de rutas C:\ locales.
4. Archivo → Exportar/Guardar como → PDF, nombre exacto `Asmat_LP2_S14_Autonoma.pdf`. Abrir el PDF final y revisar todas sus páginas antes de subirlo a la plataforma.
