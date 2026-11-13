# Backend aislado de pruebas: H2 en memoria, puerto 18080.
# Los datos de E2EDatos solo se cargan en el classpath de pruebas.
$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '..')
try {
    & .\mvnw.cmd spring-boot:test-run '-Dspring-boot.run.profiles=test,e2e' '-Dspring-boot.run.arguments=--server.port=18080 --app.cors.allowed-origins=http://localhost:14200'
    if ($LASTEXITCODE -ne 0) { throw "El backend de pruebas termino con codigo $LASTEXITCODE" }
} finally {
    Pop-Location
}
