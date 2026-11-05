package pe.edu.upeu.PharmaBackend.service.impl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.CategoriaResponseDTO;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradosException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.mapper.CategoriaMapper;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;
import pe.edu.upeu.PharmaBackend.repository.ProductoRepository;

import java.util.List;


@Service
public class CategoriaServiceImpl implements CategoriaService {
    private static final Logger LOG = LoggerFactory.getLogger(CategoriaServiceImpl.class);

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;
    private final ProductoRepository productoRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
        this.productoRepository = productoRepository;
    }


    @Override
    @Transactional
    @CacheEvict(cacheNames = "categorias", allEntries = true)
    public CategoriaResponseDTO create(CategoriaRequestDTO request) {
        String nombre = request.getNombre().trim();
        if(categoriaRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException("Ya existe una categoría con el nombre: " + nombre);
        }
        Categoria categoria = categoriaMapper.toEntity(request);

        Categoria catCreate = categoriaRepository.save(categoria);

        return categoriaMapper.toResponse(catCreate);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = { "categorias", "productos" }, allEntries = true)
    public CategoriaResponseDTO update(Long id, CategoriaRequestDTO request) {
        Categoria categoria = categoriaRepository.findById(id).orElseThrow(()->
                new RecursosNoEncontradosException(
                        "Categoría con id " + id + " no encontrada"
                )
        );
        String nombre = request.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe una categoría con el nombre: " + nombre);
        }
        categoriaMapper.actualizarEntidad(categoria, request);

        Categoria catUpdate = categoriaRepository.save(categoria);

        return categoriaMapper.toResponse(catUpdate);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categorias", key = "'id-' + #id")
    public CategoriaResponseDTO read(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursosNoEncontradosException(
                        "Categoría con id " + id + " no encontrada"
                ));
        return categoriaMapper.toResponse(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categorias", key = "'todas'")
    public List<CategoriaResponseDTO> readAll() {
        long inicio = System.currentTimeMillis();
        LOG.info("Inicio listar categorias");
        var categorias = categoriaRepository.findAll().stream().map(categoriaMapper::toResponse).toList();
        LOG.info("Fin listar categorias | duracionMs={}", System.currentTimeMillis() - inicio);
        return categorias;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categorias", allEntries = true)
    public void delete(Long id) {
        Categoria categoria = categoriaRepository.findById(id).orElseThrow(()->
                new RecursosNoEncontradosException(
                        "Categoría con id " + id + " no encontrada"
                )
        );
        if (productoRepository.existsByCategoriaIdCategoria(id)){
            throw new ReglaNegocioException(
                    "No se puede eliminar la categoria porque tiene productos asociados"
            );
        }
        categoriaRepository.delete(categoria);
    }

}
