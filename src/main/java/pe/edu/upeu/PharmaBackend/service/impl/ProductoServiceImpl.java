package pe.edu.upeu.PharmaBackend.service.impl;


import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import pe.edu.upeu.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.ProductoResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.entity.Producto;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.repository.ProductoRepository;
import pe.edu.upeu.PharmaBackend.service.service.ProductoService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class ProductoServiceImpl implements ProductoService {

    private static final Logger LOG = Logger.getLogger(ProductoServiceImpl.class.getName());

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;


    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository, RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;

    }

    @Transactional
    @Override
    public ProductoResponseDTO create(ProductoRequestDTO request) {
        String nombre = request.getNombre().trim();
        if(productoRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + nombre);
        }
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria()).orElseThrow(()-> new RecursosNoEncontradosException(
                "Categoria con id " + request.getIdCategoria() + " no encontrada"
        ));


        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setCategoria(categoria);

        Producto productoCreate = productoRepository.save(producto);

        return convertirResponse(productoCreate);

    }

    @Override
    @Transactional
    public ProductoResponseDTO update(Long aLong, ProductoRequestDTO request) {;
        String nombre = request.getNombre().trim();
        if(productoRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + nombre);
        }
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria()).orElseThrow(()-> new RecursosNoEncontradosException(
                "Categoria con id " + request.getIdCategoria() + " no encontrada"
        ));
        Producto producto = productoRepository.findById(aLong).orElseThrow(()-> new RecursosNoEncontradosException(
                "Producto con id " + aLong + " no encontrado"
        ));
        producto.setNombre(nombre);
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setCategoria(categoria);

        Producto productoUpdate = productoRepository.save(producto);
        return convertirResponse(productoUpdate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoResponseDTO> read(Long aLong) {
        return productoRepository.findById(aLong).map(this::convertirResponse);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> readAll() {
        return productoRepository.findAll().stream().map(this::convertirResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long aLong) {
        Producto producto = productoRepository.findById(aLong).orElseThrow(()-> new RecursosNoEncontradosException(
                "Producto con id " + aLong + " no encontrado"
        ));
        productoRepository.delete(producto);

    }

    private ProductoResponseDTO convertirResponse(Producto producto) {
        return new ProductoResponseDTO(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria().getIdCategoria(),
                producto.getCategoria().getNombre(),
                producto.getEstado(),
                producto.getFechaCreacion(),
                producto.getFechaModificacion()
        );
    }
}
