package pe.edu.upeu.PharmaBackend;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pe.edu.upeu.PharmaBackend.dto.*;
import pe.edu.upeu.PharmaBackend.entity.*;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.mapper.VentaMapper;
import pe.edu.upeu.PharmaBackend.repository.*;
import pe.edu.upeu.PharmaBackend.service.impl.VentaServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VentaServiceTests {
    private final VentaRepository ventas = mock(VentaRepository.class);
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final VentaServiceImpl service = new VentaServiceImpl(ventas, clientes, productos, new VentaMapper());

    private void preparar() {
        Cliente c = new Cliente(); c.setId(1L); c.setEstado(true);
        Producto p = new Producto(); p.setIdProducto(2L); p.setNombre("Paracetamol");
        p.setEstado(true); p.setPrecio(new BigDecimal("12.50")); p.setStock(5);
        when(clientes.findById(1L)).thenReturn(Optional.of(c));
        when(productos.findById(2L)).thenReturn(Optional.of(p));
    }
    @Test void calculaMontosEnServidorYDescuentaConCondicionAtomica() {
        preparar(); when(productos.descontarStock(2L, 3)).thenReturn(1);
        when(ventas.save(any())).thenAnswer(i -> i.getArgument(0));
        var result = service.registrar(new VentaRequestDTO(1L, List.of(new DetalleVentaRequestDTO(2L, 3))));
        assertEquals(new BigDecimal("37.50"), result.getTotal());
        verify(productos).descontarStock(2L, 3);
        verify(productos, never()).save(any());
    }
    @Test void fallaSinGuardarSiOtroCajeroAgotoElStock() {
        preparar(); when(productos.descontarStock(2L, 3)).thenReturn(0);
        assertThrows(ReglaNegocioException.class, () -> service.registrar(new VentaRequestDTO(1L, List.of(new DetalleVentaRequestDTO(2L, 3)))));
        verify(ventas, never()).save(any());
    }
    @Test void validaRangoYPaginacionAntesDeConsultar() {
        assertThrows(ReglaNegocioException.class, () -> service.buscarPaginado(null, null, LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 1), null, null, 0, 10));
        assertThrows(ReglaNegocioException.class, () -> service.buscarPaginado(null, null, null, null, null, null, -1, 10));
        assertThrows(ReglaNegocioException.class, () -> service.buscarPaginado(null, null, null, null, null, null, 0, 101));
        verifyNoInteractions(ventas);
    }
    @Test void paginaEnRepositorioConOrdenEstable() {
        when(ventas.buscarPaginado(isNull(), isNull(), isNull(), isNull(), any(Pageable.class))).thenAnswer(i -> {
            Pageable p = i.getArgument(4);
            assertEquals(2, p.getPageNumber()); assertEquals(10, p.getPageSize());
            assertTrue(p.getSort().getOrderFor("fechaRegistro").isDescending());
            assertNotNull(p.getSort().getOrderFor("id"));
            return new PageImpl<Venta>(List.of(), p, 20);
        });
        var result = service.buscarPaginado(null, null, null, null, "fecha", "desc", 2, 10);
        assertEquals(20, result.totalElementos()); assertTrue(result.ultima());
    }
}
