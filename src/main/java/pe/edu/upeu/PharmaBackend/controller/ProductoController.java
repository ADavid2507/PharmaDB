package pe.edu.upeu.PharmaBackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.ProductoResponseDTO;
import pe.edu.upeu.PharmaBackend.service.service.ProductoService;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {
    private final ProductoService productoService;


    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<Iterable<ProductoResponseDTO>> findAll() {
        return ResponseEntity.ok(productoService.readAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> findById(@PathVariable Long id){
        ProductoResponseDTO producto = productoService.read(id);
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> create(@Valid @RequestBody ProductoRequestDTO request){
        ProductoResponseDTO producto = productoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO request){
        return ResponseEntity.ok(productoService.update(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
