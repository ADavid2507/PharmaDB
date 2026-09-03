package pe.edu.upeu.PharmaBackend.service.impl;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import pe.edu.upeu.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.ProductoResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.entity.Producto;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.mapper.ProductoMapper;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.repository.ProductoRepository;
import pe.edu.upeu.PharmaBackend.service.service.ProductoService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductoServiceImpl.class);

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;


    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository, ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoMapper = productoMapper;
    }

    @Transactional
    @Override
    public ProductoResponseDTO create(ProductoRequestDTO request) {
        String nombre = request.getNombre().trim();

        LOG.info("Creando producto con nombre: {}, CategoriaId: {}", nombre, request.getCategoriaId());

        if(productoRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + nombre);
        }
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(()-> new RecursosNoEncontradosException(
                "Categoria con id " + request.getCategoriaId() + " no encontrada"
        ));


        Producto producto = productoMapper.toEntity(request, categoria);
        Producto productoCreado = productoRepository.save(producto);

        LOG.info("Producto creado con id: {}, CategoriaID: {}", productoCreado.getIdProducto(), categoria.getIdCategoria());


        return productoMapper.toResponse(productoCreado);

    }

    @Override
    @Transactional
    public ProductoResponseDTO update(Long aLong, ProductoRequestDTO request) {;
        String nombre = request.getNombre().trim();

        LOG.info("Actualizando producto con id: {}, Nombre: {}", aLong, nombre);

        if(productoRepository.existsByNombreIgnoreCaseAndIdProductoNot(nombre, aLong)){
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + nombre);
        }
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(()-> new RecursosNoEncontradosException(
                "Categoria con id " + request.getCategoriaId() + " no encontrada"
        ));
        Producto producto = productoRepository.findById(aLong).orElseThrow(()-> new RecursosNoEncontradosException(
                "Producto con id " + aLong + " no encontrado"
        ));
        productoMapper.actualizarEntidad(producto, request, categoria);

        Producto productoUpdate = productoRepository.save(producto);

        LOG.info("Producto actualizado con id: {}, Nombre: {}", productoUpdate.getIdProducto(), productoUpdate.getNombre());


        return productoMapper.toResponse(productoUpdate);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO read(Long aLong) {
        Producto producto = productoRepository.findById(aLong)
                .orElseThrow(() -> new RecursosNoEncontradosException(
                        "Producto con id " + aLong + " no encontrado"
                ));
        return productoMapper.toResponse(producto);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> readAll() {
        return productoRepository.findAll().stream().map(productoMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long aLong) {
        LOG.info("Eliminando producto con id: {}", aLong);
        Producto producto = productoRepository.findById(aLong).orElseThrow(()-> new RecursosNoEncontradosException(
                "Producto con id " + aLong + " no encontrado"
        ));
        LOG.info("Producto eliminado con id: {}", producto.getIdProducto());
        productoRepository.delete(producto);

    }
}
