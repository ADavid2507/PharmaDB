package pe.edu.upeu.PharmaBackend;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.*;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pe.edu.upeu.PharmaBackend.entity.*;
import pe.edu.upeu.PharmaBackend.repository.*;
import pe.edu.upeu.PharmaBackend.dto.*;
import pe.edu.upeu.PharmaBackend.service.service.*;
import pe.edu.upeu.PharmaBackend.auditoria.EventoAuditoria;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:auditoria_s14;MODE=Oracle;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class AuditoriaIntegracionTests {
 @Autowired PlatformTransactionManager transactions;
 @Autowired ProductoRepository productos; @Autowired CategoriaRepository categorias;
 @Autowired ClienteRepository clientes; @Autowired AuditoriaEventoRepository eventos;
 @Autowired VentaService ventas; @Autowired ProductoService productoService;
 @Autowired AuthService auth; @Autowired AuditoriaService auditoria; @Autowired WebApplicationContext context;
 private record Datos(Long producto, Long cliente) {}
 @AfterEach void limpiar(){ SecurityContextHolder.clearContext(); MDC.clear(); }
 private void usuario(String nombre) {
  SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(nombre,"",List.of(new SimpleGrantedAuthority("CATALOGO_ESCRIBIR"))));
 }
 private Datos datos() {
  return new TransactionTemplate(transactions).execute(s -> {
   Categoria c=new Categoria(); c.setNombre("S14 "+System.nanoTime()); categorias.save(c);
   Cliente cli=new Cliente(); cli.setDni("12345678"); cli.setNombres("Ana"); cli.setApellidos("Prueba"); cli.setEmail("test@example.invalid"); clientes.save(cli);
   Producto p=new Producto(); p.setNombre("Auditoria "+System.nanoTime()); p.setPrecio(new BigDecimal("5.00")); p.setStock(10); p.setCategoria(c); productos.saveAndFlush(p);
   return new Datos(p.getIdProducto(),cli.getId());
  });
 }
 @Test void columnasIdentificanAlCreadorYAlModificador() {
  usuario("creador"); var d=datos();
  Producto p=productos.findById(d.producto()).orElseThrow(); assertEquals("creador",p.getCreadoPor());
  usuario("editor");
  new TransactionTemplate(transactions).executeWithoutResult(s -> { Producto actual=productos.findById(d.producto()).orElseThrow(); actual.setPrecio(new BigDecimal("7.00")); productos.saveAndFlush(actual); });
  p=productos.findById(d.producto()).orElseThrow(); assertEquals("creador",p.getCreadoPor()); assertEquals("editor",p.getModificadoPor());
 }
 @Test void ventaConfirmadaAuditaUsuarioRequestIdYStock() {
  usuario("vendedor"); var d=datos(); MDC.put("requestId","s14-venta");
  var v=ventas.registrar(new VentaRequestDTO(d.cliente(),List.of(new DetalleVentaRequestDTO(d.producto(),2))));
  assertEquals(8,productos.findById(d.producto()).orElseThrow().getStock());
  var evento=eventos.findAll().stream().filter(a -> "VENTA_REGISTRADA".equals(a.getAccion()) && v.getId().equals(a.getEntidadId())).findFirst().orElseThrow();
  assertEquals("vendedor",evento.getUsuario()); assertEquals("s14-venta",evento.getRequestId()); assertEquals("vendedor",v.getCreadoPor());
 }
 @Test void rollbackNoDejaEventoNiDescuentaStock() {
  usuario("vendedor"); var d=datos(); long antes=eventos.count();
  new TransactionTemplate(transactions).executeWithoutResult(s -> { ventas.registrar(new VentaRequestDTO(d.cliente(),List.of(new DetalleVentaRequestDTO(d.producto(),2)))); s.setRollbackOnly(); });
  assertEquals(antes,eventos.count()); assertEquals(10,productos.findById(d.producto()).orElseThrow().getStock());
 }
 @Test void eventoLoginSobreviveRollbackExterior() {
  long antes=eventos.count();
  new TransactionTemplate(transactions).executeWithoutResult(s -> { auditoria.registrar(EventoAuditoria.login("LOGIN_FALLIDO","prueba")); s.setRollbackOnly(); });
  assertEquals(antes+1,eventos.count());
 }
 @Test void cambioPrecioRegistraValorAnteriorYNuevo() {
  usuario("admin"); var d=datos();
  Long categoria=new TransactionTemplate(transactions).execute(s -> productos.findById(d.producto()).orElseThrow().getCategoria().getIdCategoria());
  ProductoRequestDTO request=new ProductoRequestDTO(); request.setNombre("Precio "+System.nanoTime()); request.setPrecio(new BigDecimal("8.50")); request.setStock(10); request.setEstado(true); request.setCategoriaId(categoria);
  var actualizado=productoService.update(d.producto(),request); assertEquals("admin",actualizado.getModificadoPor());
  assertTrue(eventos.findAll().stream().anyMatch(a -> "PRECIO_MODIFICADO".equals(a.getAccion()) && d.producto().equals(a.getEntidadId()) && a.getDetalle().contains("5.00 -> 8.50")));
 }
 @Test void consultaExigePermisoYValidaTamanio() throws Exception {
  MockMvc mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  mvc.perform(get("/api/v1/auditoria")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/v1/auditoria").with(user("vendedor").authorities(new SimpleGrantedAuthority("VENTAS_LEER")))).andExpect(status().isForbidden());
  mvc.perform(get("/api/v1/auditoria").with(user("admin").authorities(new SimpleGrantedAuthority("USUARIOS_ADMINISTRAR")))).andExpect(status().isOk());
  mvc.perform(get("/api/v1/auditoria?tamanio=500").with(user("admin").authorities(new SimpleGrantedAuthority("USUARIOS_ADMINISTRAR")))).andExpect(status().isConflict());
 }
 @Test void sinSesionUsaSistemaYRechazaTamaniosExcesivos() {
  var d=datos(); assertEquals("sistema",productos.findById(d.producto()).orElseThrow().getCreadoPor());
  assertThrows(pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException.class,()->productoService.listar(0,500,"nombre","asc"));
 }
 @Test void loginRechazadoDejaBitacoraAunqueNoAutentique() {
  assertThrows(org.springframework.security.core.AuthenticationException.class, () -> auth.login(new pe.edu.upeu.PharmaBackend.dto.auth.LoginRequestDTO("no-existe-s14", "incorrecta")));
  assertTrue(eventos.findAll().stream().anyMatch(a -> "LOGIN_FALLIDO".equals(a.getAccion()) && "no-existe-s14".equals(a.getUsuario())));
 }
 @Test void bajaProductoPublicaEventoYConsultaFiltra() {
  usuario("almacen"); var d=datos(); productoService.delete(d.producto());
  var pagina=auditoria.buscar("almacen","PRODUCTO_ELIMINADO",java.time.LocalDate.now(),java.time.LocalDate.now(),0,20);
  assertTrue(pagina.contenido().stream().anyMatch(a -> d.producto().equals(a.entidadId())));
  assertFalse(productos.findById(d.producto()).orElseThrow().getEstado());
  assertThrows(pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException.class, () -> auditoria.buscar(null,null,java.time.LocalDate.now(),java.time.LocalDate.now().minusDays(1),0,20));
 }

}
