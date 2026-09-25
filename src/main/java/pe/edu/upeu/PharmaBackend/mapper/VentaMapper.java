package pe.edu.upeu.PharmaBackend.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.PharmaBackend.dto.DetalleVentaResponseDTO;
import pe.edu.upeu.PharmaBackend.dto.VentaResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Venta;

import java.util.List;

@Component
public class VentaMapper {
    public VentaResponseDTO toResponse(Venta venta) {
        List<DetalleVentaResponseDTO> detalles = venta.getDetalles().stream()
                .map(detalle -> new DetalleVentaResponseDTO(
                        detalle.getProducto().getIdProducto(), detalle.getProducto().getNombre(),
                        detalle.getCantidad(), detalle.getPrecio(), detalle.getSubtotal()))
                .toList();

        return new VentaResponseDTO(
                venta.getId(), venta.getFechaRegistro(), venta.getCliente().getId(),
                venta.getCliente().getNombres() + " " + venta.getCliente().getApellidos(),
                venta.getEstado().name(), venta.getTotal(), detalles);
    }
}
