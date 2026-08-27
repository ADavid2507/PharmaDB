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
import java.util.Optional;

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
        if(productoRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + nombre);
        }
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(()-> new RecursosNoEncontradosException(
                "Categoria con id " + request.getCategoriaId() + " no encontrada"
        ));


        Producto producto = productoMapper.toEntity(request, categoria);
        Producto productoCreado = productoRepository.save(producto);


        return productoMapper.toResponse(productoCreado);

    }

    @Override
    @Transactional
    public ProductoResponseDTO update(Long aLong, ProductoRequestDTO request) {;
        String nombre = request.getNombre().trim();
        if(productoRepository.existsByNombreIgnoreCaseAndIdProductoNot(nombre, aLong)){
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + nombre);
        }
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(()-> new RecursosNoEncontradosException(
                "Categoria con id " + request.getCategoriaId() + " no encontrada"
        ));
        Producto producto = productoRepository.findById(aLong).orElseThrow(()-> new RecursosNoEncontradosException(
                "Producto con id " + aLong + " no encontrado"
        ));
        productoMapper.actualizarEntitidad(producto, request, categoria);

        Producto productoUpdate = productoRepository.save(producto);
        return productoMapper.toResponse(productoUpdate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoResponseDTO> read(Long aLong) {
        return productoRepository.findById(aLong).map(productoMapper::toResponse);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> readAll() {
        return productoRepository.findAll().stream().map(productoMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long aLong) {
        Producto producto = productoRepository.findById(aLong).orElseThrow(()-> new RecursosNoEncontradosException(
                "Producto con id " + aLong + " no encontrado"
        ));
        productoRepository.delete(producto);

    }
}
