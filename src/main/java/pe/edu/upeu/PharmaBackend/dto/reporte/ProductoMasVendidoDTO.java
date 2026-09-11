package pe.edu.upeu.PharmaBackend.dto.reporte;

import java.math.BigDecimal;

public record ProductoMasVendidoDTO(
        Long productoId,
        String nombreProducto,
        String nombreCategoria,
        Long cantidadVendida,
        BigDecimal subtotalVendido
) {
}
