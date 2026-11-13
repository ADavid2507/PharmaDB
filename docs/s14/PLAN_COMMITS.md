# Plan de commits S14

Caleb David Asmat Briceño | 202510208

Este es un plan: los comandos NO se han ejecutado. Trabajar en `feature/integracion-estabilizacion-asmat`. Ejecutar cada bloque desde el repositorio indicado y revisar el contenido preparado antes de confirmar. No usar `git add .` al principio: agruparia todo en un solo commit y los siguientes podrian quedar vacios. La migracion V4 se incorpora completa en el primer commit; no dividirla ni editarla si ya esta aplicada.

## Antes de empezar

```powershell
cd C:\Users\David\Downloads\PharmaBackend
git branch --show-current
git status --short
```

Se detecto `docs/sql/volumen_pruebas.sql` ya preparado parcialmente en el indice (AM). Para reservarlo al grupo B4, quitar SOLO esa copia del indice. No borra ni revierte el archivo de trabajo:

```powershell
git restore --staged -- docs/sql/volumen_pruebas.sql
```

## B1 Migracion y autoria

```powershell
git add -- src/main/resources/db/migration/V4__auditoria.sql src/main/java/pe/edu/upeu/PharmaBackend/config/AuditoriaConfig.java src/main/java/pe/edu/upeu/PharmaBackend/entity/Categoria.java src/main/java/pe/edu/upeu/PharmaBackend/entity/Cliente.java src/main/java/pe/edu/upeu/PharmaBackend/entity/Producto.java src/main/java/pe/edu/upeu/PharmaBackend/entity/Venta.java src/main/java/pe/edu/upeu/PharmaBackend/dto/CategoriaResponseDTO.java src/main/java/pe/edu/upeu/PharmaBackend/dto/ClienteResponseDTO.java src/main/java/pe/edu/upeu/PharmaBackend/dto/ProductoResponseDTO.java src/main/java/pe/edu/upeu/PharmaBackend/dto/VentaResponseDTO.java src/main/java/pe/edu/upeu/PharmaBackend/mapper/CategoriaMapper.java src/main/java/pe/edu/upeu/PharmaBackend/mapper/ClienteMapper.java src/main/java/pe/edu/upeu/PharmaBackend/mapper/ProductoMapper.java src/main/java/pe/edu/upeu/PharmaBackend/mapper/VentaMapper.java
git diff --cached --stat
git commit -m "feat(auditoria): agrega V4 y columnas de autoria"
```

## B2 Eventos y consulta protegida

```powershell
git add -- src/main/java/pe/edu/upeu/PharmaBackend/auditoria src/main/java/pe/edu/upeu/PharmaBackend/entity/AuditoriaEvento.java src/main/java/pe/edu/upeu/PharmaBackend/repository/AuditoriaEventoRepository.java src/main/java/pe/edu/upeu/PharmaBackend/dto/AuditoriaEventoDTO.java src/main/java/pe/edu/upeu/PharmaBackend/dto/PaginaResponseDTO.java src/main/java/pe/edu/upeu/PharmaBackend/util/PaginacionUtil.java src/main/java/pe/edu/upeu/PharmaBackend/service/service/AuditoriaService.java src/main/java/pe/edu/upeu/PharmaBackend/service/impl/AuditoriaServiceImpl.java src/main/java/pe/edu/upeu/PharmaBackend/controller/AuditoriaController.java src/main/java/pe/edu/upeu/PharmaBackend/config/SecurityConfig.java src/main/java/pe/edu/upeu/PharmaBackend/service/impl/AuthServiceImpl.java src/main/java/pe/edu/upeu/PharmaBackend/service/impl/ProductoServiceImpl.java src/main/java/pe/edu/upeu/PharmaBackend/service/impl/VentaServiceImpl.java src/test/java/pe/edu/upeu/PharmaBackend/VentaServiceTests.java
git diff --cached --stat
git commit -m "feat(auditoria): registra eventos y consulta paginada (INC-01)"
```

## B3 Pruebas y entorno aislado

```powershell
git add -- src/test/java/pe/edu/upeu/PharmaBackend/AuditoriaIntegracionTests.java src/test/java/pe/edu/upeu/PharmaBackend/E2EDatos.java src/test/resources/application-e2e.yaml tools/iniciar-e2e.ps1
git diff --cached --stat
git commit -m "test(auditoria): verifica commit rollback login y permisos"
```

## B4 Script y plan de entrega

```powershell
git add -- docs/sql/volumen_pruebas.sql docs/s14/PLAN_COMMITS.md
git diff --cached --stat
git commit -m "docs(s14): incorpora carga de volumen y plan de commits"
```

## Comprobar y publicar

```powershell
.\mvnw.cmd test
git diff --check
git status --short
git log --oneline origin/develop..HEAD
git push -u origin feature/integracion-estabilizacion-asmat
```

B1 incluye todos los indices de V4; no crear otro commit para cambiar una migracion ya aplicada. B2 incluye VentaServiceTests porque cambia el constructor de VentaServiceImpl y el limite de 100 filas de Productos (INC-01).

Crear una PR en https://github.com/ADavid2507/PharmaDB con base `develop` y compare `feature/integracion-estabilizacion-asmat`. Titulo sugerido: `S14 Auditoria e integracion de PharmaBackend`. Describir V4, eventos AFTER_COMMIT, consulta con USUARIOS_ADMINISTRAR, limite de paginacion y pruebas realizadas. Enlazar la PR del frontend. No integrar la PR si la entrega pide la solicitud abierta.

No publicar target/, logs/, .env, tokens ni claves personales. No volver a ejecutar la carga SQL solo por incluir su archivo en Git.
