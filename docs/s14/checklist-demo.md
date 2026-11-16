# Checklist de demostración

## Antes de ensayar
- [ ] Abrir Oracle y Redis del entorno de desarrollo; confirmar conexión y esquema PHARMADB sin mostrar contraseñas.
- [ ] Arrancar backend con el perfil real usado en el proyecto. Confirmar puerto en el log; no asumir que 8081 sirve la API.
- [ ] Si se usa Angular de desarrollo: `cd C:\Users\David\Downloads\pharma-frontend` y `npm start`; SPA en 4200 y API según environment.development.ts.
- [ ] Si se usa el despliegue servido por Nginx en 8081, reconstruir el frontend y actualizar el servicio mediante el procedimiento del README. Abrir 8081 solo si ese despliegue contiene la nueva compilación.
- [ ] Tener usuarios con permisos de administrador, vendedor y almacén; probarlos sin mostrar contraseñas en el video.
- [ ] Preparar un cliente activo y un producto activo con precio y al menos seis unidades para los recorridos, sin tocar registros que no sean de pruebas.
- [ ] Ya existen 15015 productos según la captura práctica: no volver a ejecutar docs/sql/volumen_pruebas.sql.
- [ ] Mantener pestañas listas: SPA, Network, auditoría, DBeaver, consola backend, informe Playwright, incidencias, arquitectura.
- [ ] Ocultar Authorization, tokens, claves y datos de personas reales antes de grabar o capturar.
- [ ] Comprobar que los cambios están publicados, ambos enlaces abren y la etiqueta v1.0-demo apunta al commit elegido.
- [ ] Tener plan alternativo: informes HTML/PDF locales y capturas reales; si hay una falla en vivo, explicar la falla, sin presentar un respaldo como ejecución en vivo.

## Laboratorio automático separado
Terminal backend: `cd C:\Users\David\Downloads\PharmaBackend` y `powershell -ExecutionPolicy Bypass -File tools/iniciar-e2e.ps1`. Usa H2 memoria en 18080, no Oracle.

Terminal frontend: `cd C:\Users\David\Downloads\pharma-frontend`; `$env:E2E_AISLADO='1'`; `npm run e2e:estable`. Playwright arranca Angular en 14200. No confundirlo con la demo de 15015 productos en Oracle. Cerrar el backend H2 con Ctrl+C al terminar; reiniciarlo restablece el catálogo de laboratorio y sus 65 cabeceras sintéticas de venta, sin detalle y total cero, usadas solo para probar paginación.

## Tres recorridos manuales obligatorios
Preparar nuevamente la base de pruebas antes de cada recorrido; usar una copia o respaldo del esquema, nunca borrar la base principal. Registrar estos tiempos solo al realizar la actividad. Seguir el recorrido login vendedor → cliente → producto → revisar → confirmar → comprobante → stock → auditoría. Guardar evidencia de cada preparación.

| Recorrido | Fecha y hora reales | Base o respaldo utilizado | Duración real | Venta y requestId | Desviación y solución |
|---|---|---|---|---|---|
| 1 | PENDIENTE | PENDIENTE | PENDIENTE | PENDIENTE | PENDIENTE |
| 2 | PENDIENTE | PENDIENTE | PENDIENTE | PENDIENTE | PENDIENTE |
| 3 | PENDIENTE | PENDIENTE | PENDIENTE | PENDIENTE | PENDIENTE |

## Dos ensayos del guion completo
| Ensayo | Fecha y hora reales | Duración total | Sección que excedió tiempo | Ajuste aplicado |
|---|---|---|---|---|
| 1 | PENDIENTE | PENDIENTE | PENDIENTE | PENDIENTE |
| 2 | PENDIENTE | PENDIENTE | PENDIENTE | PENDIENTE |

## Antes de enviar
- [ ] Grabar video máximo 10 minutos; habilitar acceso mediante enlace y comprobarlo en ventana privada.
- [ ] Sustituir PENDIENTE de video en docs/README.md y en el Word.
- [ ] Completar registros y capturas; actualizar versión exacta en resultados-pruebas.md después de los commits.
- [ ] Exportar Word como PDF final Asmat_LP2_S14_Autonoma.pdf.
- [ ] Verificar los commits de ambos repositorios con git log. Según la aclaración del profesor comunicada por el estudiante, pueden realizarse en un día.
- [ ] Crear y publicar v1.0-demo en ambos repositorios cuando la versión final esté lista.
