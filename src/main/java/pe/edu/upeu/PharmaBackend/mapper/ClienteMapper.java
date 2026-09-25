package pe.edu.upeu.PharmaBackend.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.PharmaBackend.dto.ClienteRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.ClienteResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Cliente;

@Component
public class ClienteMapper {
    public Cliente toEntity(ClienteRequestDTO request) {
        Cliente cliente = new Cliente();
        actualizarEntidad(cliente, request);
        return cliente;
    }

    public void actualizarEntidad(Cliente cliente, ClienteRequestDTO request) {
        cliente.setDni(request.getDni().trim());
        cliente.setNombres(request.getNombres().trim());
        cliente.setApellidos(request.getApellidos().trim());
        cliente.setEmail(request.getEmail().trim().toLowerCase());
        cliente.setTelefono(normalizar(request.getTelefono()));
        cliente.setDireccion(normalizar(request.getDireccion()));
        cliente.setEstado(request.getEstado());
    }

    public ClienteResponseDTO toResponse(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(), cliente.getDni(), cliente.getNombres(), cliente.getApellidos(),
                cliente.getEmail(), cliente.getTelefono(), cliente.getDireccion(), cliente.getEstado(),
                cliente.getFechaCreacion(), cliente.getFechaModificacion());
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
