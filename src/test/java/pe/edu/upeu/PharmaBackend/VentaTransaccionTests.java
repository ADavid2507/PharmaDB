package pe.edu.upeu.PharmaBackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.edu.upeu.PharmaBackend.dto.*;
import pe.edu.upeu.PharmaBackend.entity.*;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.repository.*;
import pe.edu.upeu.PharmaBackend.service.service.VentaService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Base de pruebas en memoria, aislada de Oracle y de los datos de la aplicación. */
@SpringBootTest
@ActiveProfiles("test")
class VentaTransaccionTests {
    @Autowired PlatformTransactionManager transactions;
    @Autowired CategoriaRepository categorias;
    @Autowired ClienteRepository clientes;
    @Autowired ProductoRepository productos;
    @Autowired VentaRepository ventas;
    @Autowired VentaService service;
    @Autowired EntityManager em;

    private record Datos(Long clienteId, Long productoId) {}
    private Datos preparar() {
        Categoria categoria = new Categoria(); categoria.setNombre("S9 prueba transaccional");
        categoria = categorias.saveAndFlush(categoria);
        Cliente cliente = new Cliente(); cliente.setDni("98765432"); cliente.setNombres("Prueba S9");
        cliente.setApellidos("Transaccion"); cliente.setEmail("s9@example.invalid");
        cliente = clientes.saveAndFlush(cliente);
        Producto producto = new Producto(); producto.setNombre("S9 prueba temporal");
        producto.setPrecio(new BigDecimal("12.50")); producto.setStock(5); producto.setCategoria(categoria);
        producto = productos.saveAndFlush(producto);
        return new Datos(cliente.getId(), producto.getIdProducto());
    }
    @Test void guardaCabeceraDetallesYStockPeroLaPruebaRevierteTodo() {
        AtomicReference<Long> ventaId = new AtomicReference<>();
        AtomicReference<Long> productoId = new AtomicReference<>();
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            Datos datos = preparar(); productoId.set(datos.productoId());
            var venta = service.registrar(new VentaRequestDTO(datos.clienteId(),
                    List.of(new DetalleVentaRequestDTO(datos.productoId(), 3))));
            ventaId.set(venta.getId());
            em.flush(); em.clear();
            assertEquals(2, productos.findById(datos.productoId()).orElseThrow().getStock());
            var guardada = service.buscar(venta.getId());
            assertEquals(new BigDecimal("37.50"), guardada.getTotal());
            assertEquals(1, guardada.getDetalles().size());
            assertEquals(3, guardada.getDetalles().getFirst().getCantidad());
            status.setRollbackOnly();
        });
        assertFalse(ventas.existsById(ventaId.get()));
        assertFalse(productos.existsById(productoId.get()));
    }
    @Test void stockInsuficienteEnSegundaLineaRevierteTodaLaOperacion() {
        Datos datos = new TransactionTemplate(transactions).execute(status -> preparar());
        long antes = ventas.count();
        assertThrows(ReglaNegocioException.class, () ->
            service.registrar(new VentaRequestDTO(datos.clienteId(), List.of(
                    new DetalleVentaRequestDTO(datos.productoId(), 3),
                    new DetalleVentaRequestDTO(datos.productoId(), 3)))));
        assertEquals(antes, ventas.count());
        assertEquals(5, productos.findById(datos.productoId()).orElseThrow().getStock());
    }
    @Test void updateCondicionalNoPermiteVenderDosVecesLasUltimasUnidades() {
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            Datos datos = preparar();
            assertEquals(1, productos.descontarStock(datos.productoId(), 5));
            assertEquals(0, productos.descontarStock(datos.productoId(), 1));
            em.clear();
            assertEquals(0, productos.findById(datos.productoId()).orElseThrow().getStock());
            status.setRollbackOnly();
        });
    }
    @Test void dosCajerosConcurrentesSoloRegistranUnaVenta() throws Exception {
        Datos datos = new TransactionTemplate(transactions).execute(status -> preparar());
        long antes = ventas.count();
        CountDownLatch inicio = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Boolean> cajero = () -> {
                inicio.await();
                try {
                    service.registrar(new VentaRequestDTO(datos.clienteId(),
                            List.of(new DetalleVentaRequestDTO(datos.productoId(), 5))));
                    return true;
                } catch (ReglaNegocioException e) { return false; }
            };
            var uno = executor.submit(cajero);
            var dos = executor.submit(cajero);
            inicio.countDown();
            boolean primera = uno.get(10, TimeUnit.SECONDS);
            boolean segunda = dos.get(10, TimeUnit.SECONDS);
            assertNotEquals(primera, segunda);
        }
        assertEquals(antes + 1, ventas.count());
        assertEquals(0, productos.findById(datos.productoId()).orElseThrow().getStock());
    }
}
