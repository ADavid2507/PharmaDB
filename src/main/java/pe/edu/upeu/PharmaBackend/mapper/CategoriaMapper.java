package pe.edu.upeu.PharmaBackend.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.CategoriaResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Categoria;

@Component
public class CategoriaMapper {
    public Categoria toEntity(CategoriaRequestDTO request) {
        Categoria categoria = new Categoria();
        actualizarEntidad(categoria, request);
        return categoria;
    }

    public void actualizarEntidad(Categoria categoria, CategoriaRequestDTO request) {
        categoria.setNombre(request.getNombre().trim());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setEstado(request.getEstado());
    }

    public CategoriaResponseDTO toResponse(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getIdCategoria(), categoria.getNombre(), categoria.getDescripcion(),
                categoria.getEstado(), categoria.getFechaCreacion(), categoria.getFechaModificacion());
    }
}
