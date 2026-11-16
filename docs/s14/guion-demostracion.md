# Guion de demostración de diez minutos

Objetivo: mostrar integración de venta, seguridad, paginación, caché, auditoría y estabilidad de PharmaSoft con evidencia verificable. El estudiante realiza dos ensayos con cronómetro y graba un video de máximo diez minutos. Las frases siguientes son una guía; decirlas con palabras propias y reemplazar ID y requestId por los valores reales.

## Preparación fuera de cámara
Seguir checklist-demo.md. Abrir arquitectura.png como la única lámina de contexto. Preparar usuarios sin claves visibles, un cliente y producto activos, consola backend, DBeaver, navegador con Network, informe HTML y docs/incidencias.md. Tener Oracle con al menos 5000 productos; la captura anterior muestra 15015. El laboratorio Playwright H2 es otra instancia, en 18080/14200. Precalentar servicios sin realizar la venta del video. Ajustar tamaño de fuente para que el texto se lea.

## 00 00 a 01 00 Contexto y arquitectura
Pantalla: arquitectura.png.

Decir: «Soy Caleb David Asmat Briceño, código 202510208. PharmaSoft integra catálogo, clientes y ventas. Angular presenta la aplicación y conserva filtros en la URL. La API Spring Boot valida JWT y permisos, registra la venta transaccionalmente en Oracle y usa Redis para lecturas de catálogo. Las operaciones confirmadas generan auditoría y el requestId permite relacionarlas con los logs. En esta demostración recorreré una venta, la seguridad y las pruebas de estabilización».

## 01 00 a 04 30 Venta completa con vendedor
1. Iniciar sesión como vendedor. Mostrar que el menú coincide con sus permisos.
2. Abrir Nueva venta. Buscar cliente por nombre o DNI de prueba; seleccionarlo. Si se demuestra alta, crear un cliente de prueba antes y regresar a seleccionarlo.
3. Buscar producto por nombre. Antes de seleccionarlo señalar stock y precio visibles. Agregar cantidad dos y señalar total calculado.
4. Pulsar Revisar venta. Explicar que todavía no se registra y que stock insuficiente impide confirmar. Mantener el ejemplo válido.
5. Confirmar una sola vez. Mostrar mensaje de éxito, número de venta y comprobante. Si el diálogo de impresión bloquea, cerrarlo y abrir el comprobante por la opción de la aplicación.
6. Abrir Productos y comprobar stock anterior menos dos. Anotar ID de venta para la auditoría.

Decir: «El servidor recalcula el total y descuenta stock de forma condicional dentro de la transacción. La venta solo se confirma si todas las líneas son válidas. El catálogo actualizado refleja el descuento».

## 04 30 a 06 00 Seguridad
1. Con vendedor mostrar que Auditoría no aparece; escribir /auditoria y mostrar Acceso denegado.
2. En Postman ejecutar Auditoría denegada al vendedor y mostrar 403. El laboratorio de la colección es H2: identificarlo claramente si se usa ese resultado.
3. Para mostrar expiración sin esperar una hora, usar una sesión de prueba preparada o el caso E2E-14: token alterado provoca 401 y retorno al login con aviso. No afirmar que alterar un token demuestra el temporizador natural; aclarar que el 401 dispara el mismo cierre de sesión. Puede mostrarse adicionalmente la expiración programada desde AuthService.

Decir: «El guard controla navegación; la autorización efectiva está en el API. Un 403 conserva la sesión y un 401 la cierra. La suite también cubre el retorno al login».

## 06 00 a 07 30 Paginación caché y salud
1. Iniciar sesión admin. Abrir Productos del entorno Oracle y mostrar total mayor a 5000.
2. Cambiar página y tamaño; comprobar Filas coincide con el rango. Mostrar query params y pulsar F5. En Network abrir una respuesta: contenido limitado y totalElementos, sin mostrar Authorization.
3. Mostrar evidencia Redis de S13 o la medición actual preparada, identificando su fecha. Mostrar /actuator/health; los detalles protegidos deben consultarse autenticado. No detener Redis durante el video si no se puede recuperar a tiempo.

Decir: «El listado solicita solo la página. La URL restaura su estado. La caché acelera lecturas y la venta invalida productos para no servir stock obsoleto. Esta evidencia Redis corresponde a la fecha indicada».

## 07 30 a 08 30 Auditoría correlacionada
1. Abrir Auditoría admin y filtrar acción VENTA_REGISTRADA; localizar la venta creada.
2. Mostrar usuario, entidadId y requestId. Comparar con ventas.creado_por en SQL y la línea de log preparada con el mismo identificador.
3. Mencionar CLIENTE_REGISTRADO y CATEGORIA_ELIMINADA como los nuevos hechos auditados; mostrar un evento o las pruebas de commit/rollback.

Decir: «Las columnas indican creador y último modificador. La bitácora guarda hechos y los logs diagnostican peticiones. El listener registra los hechos después del commit, y por eso el rollback no deja una operación confirmada en la bitácora».

## 08 30 a 10 00 Pruebas e incidencias
1. Ejecutar en vivo `npm run e2e:estable` con E2E_AISLADO=1 y backend H2 preparado. Si tarda más que el ensayo, iniciar el proceso con anticipación y decir con claridad cuándo comenzó; mostrar al menos una ejecución en vivo y el informe completo guardado.
2. Mostrar 14 escenarios por tres repeticiones, sin retries. Abrir el informe HTML de la revisión y señalar el entorno H2. No presentar resultados Oracle históricos como si fueran de esta ejecución.
3. Abrir docs/incidencias.md: seis incidencias y cero críticas/altas abiertas. Resaltar selector de filas, URL y auditoría.
4. Cerrar señalando índice de docs, repositorios y etiqueta final cuando ya estén publicados.

Decir: «La estabilización cubre login, permisos, paginación, venta, stock, cliente nuevo, auditoría y token inválido. Los resultados y sus límites están documentados. Las evidencias, el código y las decisiones técnicas se encuentran en el índice del repositorio».

## Grabación y publicación
Usar OBS u otra grabadora disponible. Capturar pantalla y micrófono, resolución legible 1920×1080 si está disponible, sin notificaciones. Hacer una prueba breve de audio antes. Grabar el guion sin superar diez minutos; revisar inicio, audio, legibilidad y final. Subir a Drive o YouTube no listado, activar acceso por enlace y probar en ventana privada sin sesión. Pegar la URL en el Word y en docs/README.md y volver a exportar PDF. No publicar contraseñas o tokens en pantalla.
