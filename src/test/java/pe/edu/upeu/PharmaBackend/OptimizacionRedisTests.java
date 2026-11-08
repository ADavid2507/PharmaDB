package pe.edu.upeu.PharmaBackend;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pe.edu.upeu.PharmaBackend.config.RequestIdFilter;
import pe.edu.upeu.PharmaBackend.dto.*;
import pe.edu.upeu.PharmaBackend.entity.Cliente;
import pe.edu.upeu.PharmaBackend.repository.ClienteRepository;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.repository.ProductoRepository;
import pe.edu.upeu.PharmaBackend.repository.VentaRepository;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;
import pe.edu.upeu.PharmaBackend.service.service.ProductoService;
import pe.edu.upeu.PharmaBackend.service.service.VentaService;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** H2 aislado + Redis real; no escribe en Oracle ni utiliza las claves de la aplicación. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:s13_redis;MODE=Oracle;DB_CLOSE_DELAY=-1",
        "spring.cache.type=redis",
        "spring.cache.redis.key-prefix=s13-tests::",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.stat=OFF",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF"
})
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PHARMA_REDIS_TESTS", matches = "true")
class OptimizacionRedisTests {
    @Autowired CategoriaService categorias;
    @Autowired ProductoService productos;
    @Autowired VentaService ventas;
    @Autowired ClienteRepository clientes;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired VentaRepository ventaRepository;
    @Autowired CacheManager caches;
    @Autowired StringRedisTemplate redis;
    @Autowired EntityManagerFactory emf;
    @Autowired WebApplicationContext context;
    @Autowired RequestIdFilter requestId;

    @BeforeEach @AfterEach
    void limpiarCachesDePrueba() {
        caches.getCache("categorias").clear();
        caches.getCache("productos").clear();
        ventaRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
        clientes.deleteAll();
    }

    private CategoriaResponseDTO categoria() {
        return categorias.create(new CategoriaRequestDTO("Prueba S13", null, true));
    }

    private ProductoResponseDTO producto(Long categoriaId) {
        return productos.create(new ProductoRequestDTO("Producto S13", new BigDecimal("12.50"), 5, true, categoriaId));
    }

    @Test void segundaLecturaNoConsultaLaBaseYRedisTieneTtl() {
        var categoria = categoria();
        var stats = emf.unwrap(SessionFactory.class).getStatistics();
        long antes = stats.getQueryExecutionCount();
        categorias.readAll();
        long primera = stats.getQueryExecutionCount();
        assertThat(primera).isGreaterThan(antes);
        assertThat(categorias.readAll()).extracting(CategoriaResponseDTO::getNombre).contains("Prueba S13");
        assertThat(stats.getQueryExecutionCount()).isEqualTo(primera);
        assertThat(redis.getExpire("s13-tests::categorias::todas", TimeUnit.SECONDS)).isBetween(1L, 600L);
        assertThat(categorias.read(categoria.getId_categoria()).getNombre()).isEqualTo("Prueba S13");
    }

    @Test void crearActualizarYEliminarInvalidanCategoriasYElNombreEnProductos() {
        categorias.readAll();
        var categoria = categoria();
        assertThat(categorias.readAll()).hasSize(1);
        var producto = producto(categoria.getId_categoria());
        productos.listar(0, 10, "nombre", "asc");
        productos.read(producto.getId());
        categorias.update(categoria.getId_categoria(), new CategoriaRequestDTO("Nombre nuevo", null, true));
        assertThat(productos.read(producto.getId()).getCategoriaNombre()).isEqualTo("Nombre nuevo");
        assertThat(productos.listar(0, 10, "nombre", "asc").contenido().getFirst().getCategoriaNombre()).isEqualTo("Nombre nuevo");
        var libre = categorias.create(new CategoriaRequestDTO("Sin productos", null, true));
        categorias.readAll();
        categorias.delete(libre.getId_categoria());
        assertThat(categorias.readAll()).hasSize(1);
    }

    @Test void paginasTienenClavesDistintasYLasEscriturasInvalidanProductos() {
        var categoria = categoria();
        productos.listar(0, 10, "nombre", "asc");
        var producto = producto(categoria.getId_categoria());
        assertThat(productos.listar(0, 10, "nombre", "asc").contenido()).hasSize(1);
        productos.listar(1, 5, "stock", "desc");
        assertThat(redis.hasKey("s13-tests::productos::0-10-nombre-asc")).isTrue();
        assertThat(redis.hasKey("s13-tests::productos::1-5-stock-desc")).isTrue();
        productos.read(producto.getId());
        productos.update(producto.getId(), new ProductoRequestDTO("Modificado S13", BigDecimal.TEN, 8, true, categoria.getId_categoria()));
        assertThat(productos.read(producto.getId()).getNombre()).isEqualTo("Modificado S13");
        productos.delete(producto.getId());
        assertThat(productos.read(producto.getId()).getEstado()).isFalse();
    }

    @Test void registrarVentaInvalidaElStockCacheado() {
        var producto = producto(categoria().getId_categoria());
        Cliente cliente = new Cliente();
        cliente.setDni("98765432"); cliente.setNombres("Prueba"); cliente.setApellidos("S13");
        cliente.setEmail("s13@example.invalid");
        cliente = clientes.saveAndFlush(cliente);
        productos.listar(0, 10, "nombre", "asc");
        productos.read(producto.getId());
        ventas.registrar(new VentaRequestDTO(cliente.getId(), List.of(new DetalleVentaRequestDTO(producto.getId(), 2))));
        assertThat(caches.getCache("productos").get("id-" + producto.getId())).isNull();
        assertThat(caches.getCache("productos").get("0-10-nombre-asc")).isNull();
        assertThat(productos.listar(0, 10, "nombre", "asc").contenido().getFirst().getStock()).isEqualTo(3);
    }

    @Test void healthPublicoMetricasRestringidasYRequestIdEnRespuesta() throws Exception {
        categoria();
        categorias.readAll();
        categorias.readAll();
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(requestId).apply(springSecurity()).build();
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
        mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/metrics").with(user("vendedor")
                .authorities(new SimpleGrantedAuthority("VENTAS_LEER"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/actuator/metrics/cache.gets").param("tag", "name:categorias", "result:hit").with(user("admin")
                .authorities(new SimpleGrantedAuthority("USUARIOS_ADMINISTRAR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements[0].value", org.hamcrest.Matchers.greaterThanOrEqualTo(1.0)));
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk()).andExpect(header().string("X-Request-Id", org.hamcrest.Matchers.matchesPattern("[0-9a-f]{8}")));
        assertThat(org.slf4j.MDC.get("requestId")).isNull();
    }
}
