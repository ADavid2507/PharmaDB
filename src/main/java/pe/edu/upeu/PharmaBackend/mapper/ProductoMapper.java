package pe.edu.upeu.PharmaBackend.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.PharmaBackend.dto.CategoriaResumenDTO;
import pe.edu.upeu.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.ProductoResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.entity.Producto;

@Component
public class ProductoMapper {

    public Producto toEntity(ProductoRequestDTO request, Categoria categoria) {
        Producto producto = new Producto();
        actualizarEntidad(producto, request, categoria);
        return producto;
    }

    public void actualizarEntidad(Producto producto, ProductoRequestDTO request, Categoria categoria) {
        producto.setNombre(request.getNombre().trim());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setCategoria(categoria);
    }

    public ProductoResponseDTO toResponse(Producto producto) {
        CategoriaResumenDTO categoria = new CategoriaResumenDTO(
                producto.getCategoria().getIdCategoria(),
                producto.getCategoria().getNombre()
        );
        return new ProductoResponseDTO(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                categoria,
                producto.getEstado(),
                producto.getFechaCreacion(),
                producto.getFechaModificacion()
        );
    }
}
