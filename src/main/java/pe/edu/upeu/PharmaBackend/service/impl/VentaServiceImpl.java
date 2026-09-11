package pe.edu.upeu.PharmaBackend.service.impl;

import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.DetalleVentaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.DetalleVentaResponseDTO;
import pe.edu.upeu.PharmaBackend.dto.VentaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.VentaResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Cliente;
import pe.edu.upeu.PharmaBackend.entity.DetalleVenta;
import pe.edu.upeu.PharmaBackend.entity.Producto;
import pe.edu.upeu.PharmaBackend.entity.Venta;
import pe.edu.upeu.PharmaBackend.enums.EstadoVenta;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.repository.ClienteRepository;
import pe.edu.upeu.PharmaBackend.repository.ProductoRepository;
import pe.edu.upeu.PharmaBackend.repository.VentaRepository;
import pe.edu.upeu.PharmaBackend.service.service.VentaService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
@Service
public class VentaServiceImpl implements VentaService {
    private final Logger LOG = LoggerFactory.getLogger(VentaServiceImpl.class);
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository) {

        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public VentaResponseDTO registrar(VentaRequestDTO request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() ->new RecursosNoEncontradosException("Cliente no encontrado con id: "+ request.getClienteId()));

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new ReglaNegocioException("No se puede registrar una venta para un cliente inactivo");
        }
        Venta venta = new Venta();

        venta.setCliente(cliente);
        venta.setEstado(EstadoVenta.REGISTRADA);

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaRequestDTO item: request.getDetalles()) {
            Producto producto = productoRepository.findById(item.getProductoId()).orElseThrow(() ->
                    new RecursosNoEncontradosException("Producto no encontrado con id: "+ item.getProductoId()));

            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new ReglaNegocioException("El producto "+ producto.getNombre()+ " se encuentra inactivo");
            }

            if (producto.getStock()< item.getCantidad()) {

                throw new ReglaNegocioException("Stock insuficiente para "+ producto.getNombre()+ ". Disponible: "+ producto.getStock()
                        + ", solicitado: "+ item.getCantidad());
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));

            DetalleVenta detalle = new DetalleVenta();

            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio(producto.getPrecio());
            detalle.setSubtotal(subtotal);

            venta.agregarDetalle(detalle);

            total = total.add(subtotal);

            producto.setStock(producto.getStock()- item.getCantidad());
        }

        venta.setTotal(total);

        Venta guardada =ventaRepository.save(venta);

        return convertirResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscar(Long id) {

        Venta venta = ventaRepository.findById(id).orElseThrow(() ->
                new RecursosNoEncontradosException("Venta no encontrada con id: "+ id));
        return convertirResponse(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listar() {
        return ventaRepository.findAll().stream().map(this::convertirResponse).toList();
    }

    @Override
    @Transactional
    public VentaResponseDTO anular(Long id) {

        Venta venta = ventaRepository.findById(id).orElseThrow(()->
                new RecursosNoEncontradosException("Venta no encontrada con id: "+ id
                )
        );
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new ReglaNegocioException("La venta ya fue anulada");
        }
        venta.setEstado(EstadoVenta.ANULADA);

        return convertirResponse(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> buscar(Long clienteId, EstadoVenta estado, LocalDate desde, LocalDate hasta, String ordenarPor, String direccion) {
        long inicio = System.currentTimeMillis();

        LOG.info("Inicio buscar ventas | clienteId={} | estado={} | "
                        + "desde={} | hasta={} | ordenarPor={} | direccion={}",
                clienteId, estado, desde, hasta, ordenarPor, direccion);

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial debe ser anterior a la fecha final");
        }
        Sort sort = construirSort(ordenarPor, direccion);

        LocalDateTime desdeHora = (desde == null) ? null : desde.atStartOfDay();
        LocalDateTime hastaHora = (hasta == null) ? null : hasta.atTime(LocalTime.MAX);

        List<VentaResponseDTO> ventas = ventaRepository.buscar(clienteId, estado, desdeHora, hastaHora, sort).stream().map(this::convertirResponse).toList();

        LOG.info("Fin buscar ventas | tiempo={}ms", System.currentTimeMillis() - inicio);

        return ventas;
    }

    @Override
    public List<VentaResponseDTO> reporteVentasPorCategoria(LocalDateTime desde, LocalDateTime hasta) {
        return List.of();
    }

    @Override
    public List<VentaResponseDTO> reporteProductosMasVendidos(LocalDateTime desde, LocalDateTime hasta) {
        return List.of();
    }


    private VentaResponseDTO convertirResponse(Venta venta) {

        List<DetalleVentaResponseDTO> detalles =
                venta.getDetalles()
                        .stream()
                        .map(detalle ->
                                new DetalleVentaResponseDTO(
                                        detalle.getProducto().getIdProducto(),
                                        detalle.getProducto().getNombre(),
                                        detalle.getCantidad(),
                                        detalle.getPrecio(),
                                        detalle.getSubtotal()
                                )
                        ).toList();

        String clienteNombre = venta.getCliente().getNombres()+ " "+ venta.getCliente().getApellidos();

        return new VentaResponseDTO(
                venta.getId(),
                venta.getFechaRegistro(),
                venta.getCliente().getId(),
                clienteNombre,
                venta.getEstado().name(),
                venta.getTotal(),
                detalles
        );
    }
    public void validarFechas(
            LocalDateTime desde,
            LocalDateTime hasta
    ) {
        if (desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial debe ser anterior a la fecha final");
        } else if (desde.isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("La fecha inicial debe ser posterior a la fecha actual");
        } else if (hasta.isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException("La fecha final debe ser anterior a la fecha actual");
        }
    }


    private static final String ORDEN_POR_DEFECTO = "ordenPorDefecto";
    private static final List<String> CAMPOS_ORDENABLES = List.of(
            "id",
            "fechaRegistro",
            "estado",
            "total"
    );

    private Sort construirSort(String ordenarPor, String direccion) {

        String campo = (ordenarPor == null || ordenarPor.isBlank())
                ? ORDEN_POR_DEFECTO
                : ordenarPor.trim();

        if (!CAMPOS_ORDENABLES.contains(campo)) {

            throw new ReglaNegocioException(
                    "El campo de ordenamiento '"
                            + campo
                            + "' no está permitido. Campos válidos: "
                            + CAMPOS_ORDENABLES);
        }

        String sentido = (direccion == null || direccion.isBlank())
                ? "desc"
                : direccion.trim();

        if (!sentido.equalsIgnoreCase("asc")
                && !sentido.equalsIgnoreCase("desc")) {

            throw new ReglaNegocioException(
                    "La dirección de ordenamiento '"
                            + sentido
                            + "' no está permitida. Valores válidos: asc, desc");
        }

        return sentido.equalsIgnoreCase("asc")
                ? Sort.by(campo).ascending()
                : Sort.by(campo).descending();
    }

}
