package pe.edu.upeu.PharmaBackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackend.dto.ClienteRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.ClienteResponseDTO;
import pe.edu.upeu.PharmaBackend.service.service.ClienteService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> findAll() {
        return ResponseEntity.ok(clienteService.readAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> findById(@PathVariable Long id) {
        ClienteResponseDTO cliente = clienteService.read(id);
        return ResponseEntity.ok(cliente);
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> create(@Valid @RequestBody ClienteRequestDTO request) {
        ClienteResponseDTO cliente = clienteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.ok(clienteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
