# Defensa técnica de PharmaSoft

Estas diez respuestas describen el código real. Repasarlas y explicarlas con palabras propias; las rutas de backend parten de src/main/java/pe/edu/upeu/PharmaBackend y las de frontend de src/app.

## 1 Por qué paginar en el servidor y cómo lo ejecuta Oracle
ProductoServiceImpl.listar construye PageRequest con orden y un máximo de 100 validado en PaginacionUtil. Spring Data devuelve Page, con consulta limitada y conteo; el dialecto Hibernate de Oracle genera la sintaxis de paginación compatible con la versión configurada. La SPA consume contenido y metadatos, sin descargar la tabla completa para el listado. Ver repository/ProductoRepository.java y dto/PaginaResponseDTO.java. Para demostrar el SQL real se debe observar el log del perfil Oracle, no afirmar que la ejecución H2 prueba ese dialecto.

## 2 Cómo se conserva la atomicidad de la venta y la auditoría
VentaServiceImpl.registrar usa @Transactional y valida cliente, líneas y productos. ProductoRepository.descontarStock hace una actualización condicional por stock suficiente; si falla una línea se lanza una regla de negocio y se revierte cabecera, detalle y descuentos anteriores. VentaTransaccionTests comprueba rollback y concurrencia. El evento VENTA_REGISTRADA se persiste con AFTER_COMMIT: no aparece al revertirse la venta. La bitácora usa otra transacción; una falla de bitácora posterior al commit se registra y no revierte la venta. Una garantía estricta de entrega exigiría outbox, que no está implementado.

## 3 Qué sucede cuando vence o se altera el JWT
core/auth/auth-service.ts guarda sesión y expiración, programa el cierre y el auth guard impide rutas sin sesión. El interceptor agrega Bearer; ante 401 cierra sesión y redirige al login con expirada y returnUrl. Un 403 informa falta de permiso sin borrar una sesión válida. El backend valida JWT y carga autoridades; alterar permisos en almacenamiento no concede permisos al servidor. Ver security/JwtAuthenticationFilter.java, config/SecurityConfig.java y e2e/estabilizacion.spec.ts E2E-14.

## 4 Qué caché se invalida después de una venta
VentaServiceImpl tiene @CacheEvict(cacheNames = productos, allEntries = true) para no servir stock antiguo. La configuración de caché y transacciones está en config/CacheConfig.java. También se invalidan productos cuando cambia el catálogo; el renombre de categoría invalida productos porque contienen categoriaNombre. Las pruebas H2 con caché de test no equivalen a medir Redis; consultar las evidencias S13 para la ejecución Redis.

## 5 Diferencia entre columnas de auditoría bitácora y logs
AuditoriaConfig aporta el actor a creado_por y modificado_por; son el creador y el último modificador. auditoria_eventos guarda hechos históricos consultables por el administrador. RequestIdFilter y MDC relacionan una petición con sus logs de diagnóstico. EventoAuditoria captura usuario y requestId antes de que el listener registre el hecho. Ninguna de estas fuentes debe guardar contraseñas o tokens. Ver docs/auditoria.md.

## 6 Cómo se evitan pruebas E2E inestables
Playwright usa locators por rol, etiquetas y testid, espera respuestas o condiciones observables y no usa waitForTimeout. Configuración: un worker, retries 0, contexto limpio por prueba y repetición triple. Los datos nuevos tienen valores únicos, la prueba de venta compara stock relativo y H2 se reinicia para un laboratorio limpio. Las primeras ejecuciones revelaron un selector de Acción que se corrigió antes de repetir la suite; el informe final registra el resultado de la versión verificada, sin ocultarlo mediante reintentos.

## 7 Por qué el menú y el guard no bastan para autorizar
El menú es presentación y el guard evita navegación accidental. Una persona puede llamar al API directamente; por eso SecurityConfig exige USUARIOS_ADMINISTRAR en /api/v1/auditoria y aplica permisos por método y recurso. E2E-13 comprueba navegación y Postman comprueba 403 del vendedor directamente en el servidor.

## 8 Cómo se conserva el estado de un listado al recargar
Productos, ventas y auditoría leen query params. Al cambiar filtros se conserva el tamaño y se reinicia pagina a cero; al cambiar página se conservan los filtros. F5 y Atrás reconstruyen la consulta desde URL. AuditoriaList sincroniza además el formulario con ActivatedRoute. Ver features/auditoria/pages/auditoria-list.ts, e2e/paginacion.spec.ts y E2E-11/E2E-12.

## 9 Por qué el login fallido se audita de otra manera
Un intento fallido no representa una operación de negocio confirmada; su rechazo no debe borrar la evidencia del intento. AuthServiceImpl registra ese hecho mediante AuditoriaServiceImpl en REQUIRES_NEW. Los cambios de cliente, categoría, producto y venta utilizan el listener AFTER_COMMIT para evitar registrar operaciones revertidas. Las pruebas de AuditoriaIntegracionTests cubren ambos comportamientos.

## 10 Qué límites tiene la evidencia de pruebas
Las pruebas automáticas actuales verifican la lógica, el API y la SPA con H2 aislado. No demuestran por sí solas rendimiento con 15015 productos, SQL de Oracle, disponibilidad de Redis, una demostración humana o la calidad del video. Las evidencias históricas de Oracle/Redis permanecen separadas. Para la entrega se añaden capturas actuales del entorno real, dos ensayos y tres recorridos con tiempos reales; los commits y el tag se crean manualmente sin inventar fechas.
