# Colección Postman de PharmaBackend

Importar PharmaBackend.postman_collection.json. Sus 16 solicitudes cubren 200, 201, 400, 401, 403, 404 y 409; venta válida, stock insuficiente y correlación de auditoría. Se ejecutan en orden porque el login guarda tokens temporalmente y las consultas guardan IDs.

Esta colección está restringida a `http://localhost:18080/api/v1` y registra una venta de una unidad en H2. Arrancar primero `powershell -ExecutionPolicy Bypass -File tools/iniciar-e2e.ps1`. Ese script usa el perfil test,e2e, datos ficticios y base en memoria. No cambiar el destino a Oracle. La suite E2E y esta colección deben ejecutarse una después de otra para evitar modificaciones concurrentes del stock.

En Postman crear variables locales de entorno claveAdmin y claveVendedor con las claves del laboratorio definidas en src/test/java/pe/edu/upeu/PharmaBackend/E2EDatos.java. No exportar sus valores ni tokens. Ejecutar Run collection con una iteración y sin reordenar solicitudes. Revisar que todas las aserciones estén verdes; la última compara el requestId de la venta con el de la bitácora.

Con Newman instalado: `newman run docs/postman/PharmaBackend.postman_collection.json -e ruta-a-entorno-local-no-versionado.json`. El entorno local debe contener las claves; mantenerlo fuera de Git. Los informes JSON brutos de Newman pueden incluir tokens y contraseñas de solicitudes, por eso no se incluyen en docs. Se conserva únicamente el resumen saneado en ../evidencias/s14-estabilizacion/automatizadas/postman-resumen.json.
