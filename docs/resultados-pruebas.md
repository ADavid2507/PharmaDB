# Resultados de pruebas de estabilización

Fecha de revisión: 15 de noviembre de 2026, zona America/Bogota. Versión: backend base `15fef45`, frontend base `91e5038`, más los cambios de trabajo de la actividad autónoma **sin commit por instrucción del estudiante**. No se asigna un hash ficticio al trabajo nuevo. Después de hacer los commits, registrar aquí los hashes finales y repetir las pruebas si se cambia código.

La huella de las fuentes verificadas es `184f10d93277f02e4bc942099c9edf2a4ace9829f061016a209ccd3f5de964be`. El manifiesto completo se conserva en [version-fuentes.json](evidencias/s14-estabilizacion/automatizadas/version-fuentes.json).

## Resultados actuales
| Comprobación | Resultado | Duración registrada | Entorno |
|---|---|---|---|
| Compilación Angular producción | Aprobada | 5.499 s | Node y Angular locales |
| TypeScript E2E noEmit | Aprobada | No cronometrada separadamente | e2e/tsconfig.json con Node |
| Unitarias Angular | 114 aprobadas, 35 archivos | 15.83 s | Vitest, jsdom |
| Backend Maven | 62 aprobadas, 5 omitidas; 0 fallos y 0 errores | 36.855 s sumadas entre suites, no tiempo total Maven | H2; Redis no ejecutado |
| Playwright Chromium | 42 aprobadas, 0 fallidas, 0 flaky | 60.899 s | H2 memoria, 18080; Angular E2E 14200 |
| Postman con Newman | 16 solicitudes; 20 aserciones; 0 fallos | 1.328 s | H2 memoria, 18080 |

Playwright: 14 escenarios × 3 repeticiones; workers=1 y retries=0. No se utiliza waitForTimeout. La primera ejecución detectó un selector inadecuado para el campo Acción; se corrigió a un locator por rol y se volvió a ejecutar la suite completa. El informe enlazado corresponde a la ejecución final aprobada.

## Cobertura E2E
Los siete originales cubren ruta protegida sin sesión, login incorrecto, returnUrl, venta y stock, paginación persistente, permisos de almacén y catálogo de vendedor. El flujo original ahora comprueba también la venta en la pantalla de Auditoría con su requestId.

Siete casos nuevos en frontend/e2e/estabilizacion.spec.ts:
1. E2E-08: stock excedido impide confirmar y el API devuelve 409 sin añadir una venta.
2. E2E-09: venta sin productos no se envía al servidor.
3. E2E-10: cliente recién creado aparece inmediatamente en Nueva venta.
4. E2E-11: desde la tercera página, aplicar fecha reinicia página y F5 conserva filtros.
5. E2E-12: administrador consulta auditoría con usuario/acción persistidos en URL.
6. E2E-13: vendedor no ve el menú y no accede a /auditoria.
7. E2E-14: token alterado provoca cierre de sesión por 401 y aviso de expiración.

## Evidencias descargables
- [Informe HTML Playwright](evidencias/s14-estabilizacion/automatizadas/playwright-report/index.html)
- [JSON original Playwright](evidencias/s14-estabilizacion/automatizadas/playwright.json)
- [Resumen backend sin secretos](evidencias/s14-estabilizacion/automatizadas/backend-resumen.json)
- [Resumen Postman saneado](evidencias/s14-estabilizacion/automatizadas/postman-resumen.json)
- [Colección reutilizable](postman/PharmaBackend.postman_collection.json)

Los resultados Postman originales no se versionan: pueden incluir solicitudes de login y tokens. El resumen conserva nombres, estados y aserciones sin esas credenciales. Los reportes HTML deben abrirse localmente o mediante playwright show-report; GitHub no ejecuta el HTML al abrirlo como archivo de código.

## Cómo repetir
Backend unitarias: `.\mvnw.cmd test` desde PharmaBackend. Frontend: `npm test -- --watch=false`, `npx tsc -p e2e/tsconfig.json --noEmit` y `npm run build`.

Para E2E, iniciar desde backend `powershell -ExecutionPolicy Bypass -File tools/iniciar-e2e.ps1`. Desde frontend establecer `$env:E2E_AISLADO='1'` y ejecutar `npm run e2e:estable`. El proceso reinicia H2 al arrancar; contiene 45 productos y 65 cabeceras sintéticas de venta sin detalle para probar página tres. No usar esas cabeceras para demostrar reportes de negocio.

Ejecutar Postman después de terminar E2E, siguiendo docs/postman/README.md. La colección añade una venta de una unidad en H2; las tres repeticiones E2E registran tres ventas de dos unidades en memoria. No se ejecutaron nuevas ventas de la actividad autónoma sobre Oracle.

## Evidencia histórica y límites
La práctica S14 anterior conserva en el frontend 21/21 ejecuciones sobre Oracle y Redis, junto con sus mediciones y su fecha. Esta revisión nueva no repite esa validación: las cinco pruebas Redis se omitieron y H2 no demuestra SQL de Oracle ni rendimiento con más de 5000 productos. Las capturas reales, dos ensayos y tres recorridos manuales los realiza el estudiante; seguir guia-capturas.md y checklist-demo.md.
