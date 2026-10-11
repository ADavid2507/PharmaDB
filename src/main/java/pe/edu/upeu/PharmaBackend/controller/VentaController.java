package pe.edu.upeu.PharmaBackend.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackend.dto.VentaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.VentaResponseDTO;
import pe.edu.upeu.PharmaBackend.enums.EstadoVenta;
import pe.edu.upeu.PharmaBackend.service.service.VentaService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(
            VentaService ventaService) {

        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrar(
            @Valid
            @RequestBody VentaRequestDTO request) {

        VentaResponseDTO response =
                ventaService.registrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> buscar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ventaService.buscar(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listar() {

        return ResponseEntity.ok(
                ventaService.listar()
        );
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<VentaResponseDTO> anular(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.anular(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscar(
            @RequestParam(required = false)
            Long idCliente,
            @RequestParam(required = false)
            EstadoVenta estadoVenta,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta,
            @RequestParam(required = false)
            String ordenarPor,
            @RequestParam(required = false)
            String direccion,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) EstadoVenta estado,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanio

    ){
        Long clienteFiltro = clienteId != null ? clienteId : idCliente;
        EstadoVenta estadoFiltro = estado != null ? estado : estadoVenta;
        // Mantiene la lista para consumidores anteriores; con paginación devuelve el contrato de la SPA.
        if (pagina != null || tamanio != null) {
            return ResponseEntity.ok(ventaService.buscarPaginado(clienteFiltro, estadoFiltro, desde, hasta,
                    ordenarPor, direccion, pagina == null ? 0 : pagina, tamanio == null ? 10 : tamanio));
        }
        return ResponseEntity.ok(ventaService.buscar(clienteFiltro, estadoFiltro, desde, hasta, ordenarPor, direccion));
    }
}
